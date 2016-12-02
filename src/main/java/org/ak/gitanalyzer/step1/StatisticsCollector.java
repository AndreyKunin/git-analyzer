package org.ak.gitanalyzer.step1;

import org.ak.gitanalyzer.step1.data.RawFile;
import org.ak.gitanalyzer.step1.data.RawRepository;
import org.ak.gitanalyzer.step1.git.Subprocess;
import org.ak.gitanalyzer.step1.git.SubprocessException;
import org.ak.gitanalyzer.step1.git.builder.GitLogBuilder;
import org.ak.gitanalyzer.step1.git.builder.GitLsTreeBuilder;
import org.ak.gitanalyzer.step1.git.parser.LogParser;
import org.ak.gitanalyzer.step1.git.parser.LsTreeParser;
import org.ak.gitanalyzer.util.Configuration;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Andrew on 01.10.2016.
 */
public class StatisticsCollector {

    private Subprocess subprocess;

    private int filesCount;
    private AtomicInteger filesRemained = new AtomicInteger();

    public StatisticsCollector(Subprocess subprocess) {
        if (subprocess == null) {
            subprocess = new Subprocess();
        }
        this.subprocess = subprocess;
    }

    public RawRepository collect(Date dateFrom) throws SubprocessException {
        Date creationDate = new Date();

        final String[] gitRepositoryPaths = Configuration.INSTANCE.getStringArray("GIT.repository.paths");
        final String[] gitBranchNames = Configuration.INSTANCE.getStringArray("GIT.branch.names");
        final String[] gitFilenamePrefixes = Configuration.INSTANCE.getStringArray("GIT.repository.filename.prefixes");

        final Date gitMinDate = Configuration.INSTANCE.getDate("GIT.log.min.date", null);
        final Date gitMaxDate = Configuration.INSTANCE.getDate("GIT.log.max.date", null);

        if (dateFrom == null) {
            dateFrom = gitMinDate;
        }
        if (gitRepositoryPaths.length != gitBranchNames.length) {
            throw new SubprocessException("Invalid configuration. List of repositories should be parallel to the list of branches.");
        }
        if (gitRepositoryPaths.length > 1 && gitRepositoryPaths.length != gitFilenamePrefixes.length) {
            throw new SubprocessException("Invalid configuration. List of repositories should be parallel to the list of filename prefixes.");
        }

        Map<String, String> repositories = new HashMap<>();
        Map<String, String> prefixes = new HashMap<>();
        for (int i = 0; i < gitRepositoryPaths.length; ++i) {
            repositories.put(gitRepositoryPaths[i], gitBranchNames[i]);
            if (gitFilenamePrefixes.length > 0) {
                prefixes.put(gitRepositoryPaths[i], gitFilenamePrefixes[i]);
            }
        }

        List<RawFile> rawFiles = new ArrayList<>();
        for (String repositoryPath : gitRepositoryPaths) {
            System.out.println("Processing " + repositoryPath);
            List<RawFile> files = getFiles(repositoryPath, repositories.get(repositoryPath), dateFrom, gitMaxDate);

            String prefix = prefixes.get(repositoryPath);
            if (prefix != null && prefix.length() > 0) {
                files = addPrefixes(files, prefix);
            }

            rawFiles.addAll(files);
        }
        return new RawRepository(rawFiles, creationDate);
    }

    private List<RawFile> getFiles(String gitRepositoryPath, String branchName, Date dateFrom, Date dateTo) throws SubprocessException {
        LsTreeParser gitOutputParser = new LsTreeParser();
        String[] lsTreeCommand = new GitLsTreeBuilder().setBranchName(branchName).buildCommand();
        List<RawFile> rawFiles = new ArrayList<>();
        subprocess.execute(gitRepositoryPath, false, lsTreeCommand)
                .assertErrors()
                .assertExitCode()
                .assertOutputPresent()
                .getOut()
                .forEach(line -> gitOutputParser.parseLsTree(rawFiles, line));
        Timer timer = startTimer(rawFiles.size());
        rawFiles.parallelStream().forEach(rawFile -> getCommits(gitRepositoryPath, rawFile, dateFrom, dateTo));
        timer.cancel();
        assertExceptions(rawFiles);
        return rawFiles;
    }

    private Timer startTimer(int rawFilesSize) {
        filesCount = rawFilesSize;
        filesRemained.set(filesCount);
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new Logger(), 0, 10 * 1000);
        return timer;
    }

    private void getCommits(String gitRepositoryPath, RawFile file, Date dateFrom, Date dateTo) {
        int attemptCount = Configuration.INSTANCE.getInt("GIT.max.connection.attempts", 5);
        SubprocessException lastError = null;
        while (attemptCount > 0) {
            try {
                LogParser gitOutputParser = new LogParser();
                String[] logCommand = new GitLogBuilder().setFileName(file.getPath()).setSinceDate(dateFrom).setUntilDate(dateTo).buildCommand();
                subprocess.execute(gitRepositoryPath, false, logCommand)
                        .assertErrors()
                        .assertExitCode()
                        .getOut()
                        .forEach(line -> gitOutputParser.parseLog(file, line));
                attemptCount = 0;
                lastError = null;
                filesRemained.decrementAndGet();
            } catch (SubprocessException e) {
                lastError = e;
                System.out.println("Error retrieving commits for " + file.getPath());
                System.out.println(e.getMessage());
                e.printStackTrace(System.out);
                attemptCount--;
            }
        }
        if (lastError != null) {
            file.setException(lastError);
        }
    }

    private void assertExceptions(List<RawFile> rawFiles) throws SubprocessException {
        for (RawFile rawFile : rawFiles) {
            if (rawFile.getException() != null) {
                throw rawFile.getException();
            }
        }
    }

    private List<RawFile> addPrefixes(List<RawFile> files, String prefix) {
        List<RawFile> newFiles = new ArrayList<>();
        files.forEach(file -> {
            RawFile newFile = new RawFile(prefix + (file.getPath().startsWith("/") ? "" : "/") + file.getPath());
            newFile.getCommits().addAll(file.getCommits());
            newFiles.add(newFile);
        });
        return newFiles;
    }

    private class Logger extends TimerTask {
        @Override
        public void run() {
            System.out.println(Math.round((double) (filesCount - filesRemained.get()) / (double) filesCount * 100.0) + "% done.");
        }
    }
}
