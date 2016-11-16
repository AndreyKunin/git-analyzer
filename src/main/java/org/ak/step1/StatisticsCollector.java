package org.ak.step1;

import org.ak.step1.data.RawFile;
import org.ak.step1.data.RawRepository;
import org.ak.step1.git.Subprocess;
import org.ak.step1.git.SubprocessException;
import org.ak.step1.git.builder.GitLogBuilder;
import org.ak.step1.git.builder.GitLsTreeBuilder;
import org.ak.step1.git.parser.LogParser;
import org.ak.step1.git.parser.LsTreeParser;
import org.ak.util.Configuration;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Andrew on 01.10.2016.
 */
public class StatisticsCollector {

    private final int DEFAULT_ATTEMPTS_COUNT = Configuration.INSTANCE.getInt("GIT.max.connection.attempts", 5);

    private static final String[] GIT_REPOSITORY_PATHS = Configuration.INSTANCE.getStringArray("GIT.repository.paths");
    private static final String[] GIT_BRANCH_NAMES = Configuration.INSTANCE.getStringArray("GIT.branch.names");
    private static final String[] GIT_FILENAME_PREFIXES = Configuration.INSTANCE.getStringArray("GIT.repository.filename.prefixes");

    private static final Date GIT_MIN_DATE = Configuration.INSTANCE.getDate("GIT.log.min.date", null);
    private static final Date GIT_MAX_DATE = Configuration.INSTANCE.getDate("GIT.log.max.date", null);

    private int filesCount;
    private AtomicInteger filesRemained = new AtomicInteger();

    public RawRepository collect(Date dateFrom) throws SubprocessException {
        Date creationDate = new Date();

        if (dateFrom == null) {
            dateFrom = GIT_MIN_DATE;
        }
        if (GIT_REPOSITORY_PATHS.length != GIT_BRANCH_NAMES.length) {
            throw new SubprocessException("Invalid configuration. List of repositories should be parallel to the list of branches.");
        }
        if (GIT_REPOSITORY_PATHS.length > 1 && GIT_REPOSITORY_PATHS.length != GIT_FILENAME_PREFIXES.length) {
            throw new SubprocessException("Invalid configuration. List of repositories should be parallel to the list of filename prefixes.");
        }

        Map<String, String> repositories = new HashMap<>();
        Map<String, String> prefixes = new HashMap<>();
        for (int i = 0; i < GIT_REPOSITORY_PATHS.length; ++i) {
            repositories.put(GIT_REPOSITORY_PATHS[i], GIT_BRANCH_NAMES[i]);
            if (GIT_FILENAME_PREFIXES.length > 0) {
                prefixes.put(GIT_REPOSITORY_PATHS[i], GIT_FILENAME_PREFIXES[i]);
            }
        }

        List<RawFile> rawFiles = new ArrayList<>();
        for (String repositoryPath : GIT_REPOSITORY_PATHS) {
            System.out.println("Processing " + repositoryPath);
            List<RawFile> files = getFiles(repositoryPath, repositories.get(repositoryPath), dateFrom, GIT_MAX_DATE);

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
        Subprocess.execute(gitRepositoryPath, false, lsTreeCommand)
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
        int attemptCount = DEFAULT_ATTEMPTS_COUNT;
        SubprocessException lastError = null;
        while (attemptCount > 0) {
            try {
                LogParser gitOutputParser = new LogParser();
                String[] logCommand = new GitLogBuilder().setFileName(file.getPath()).setSinceDate(dateFrom).setUntilDate(dateTo).buildCommand();
                Subprocess.execute(gitRepositoryPath, false, logCommand)
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
            RawFile newFile = new RawFile(prefix + "/" + file.getPath());
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
