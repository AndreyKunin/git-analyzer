package org.ak.gitanalyzer.util;

import org.ak.gitanalyzer.step1.data.RawCommit;
import org.ak.gitanalyzer.step1.data.RawFile;
import org.ak.gitanalyzer.step1.data.RawRepository;
import org.ak.gitanalyzer.step1.git.Subprocess;
import org.ak.gitanalyzer.step1.git.SubprocessException;
import org.ak.gitanalyzer.step2.data.Author;
import org.ak.gitanalyzer.step2.data.Commit;
import org.ak.gitanalyzer.step2.data.DataRepository;
import org.ak.gitanalyzer.step2.data.Link;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Andrew on 22.11.2016.
 */
public class TestHelper {

    public static final SimpleDateFormat df = new SimpleDateFormat("E MMM d HH:mm:ss yyyy Z", Locale.US);

    public static void deleteDir(File dir) {
        String[] entries = dir.list();
        if (entries != null) {
            for (String name : entries) {
                File file = new File(dir.getPath(), name);
                file.delete();
            }
        }
        dir.delete();
    }

    public static void assertConsistency(DataRepository dataRepository) {
        Map<org.ak.gitanalyzer.step2.data.File, org.ak.gitanalyzer.step2.data.File> fileMap = dataRepository.getFiles().keySet().stream().collect(Collectors.toMap(x -> x, x -> x));
        Map<Author, Author> authorMap = dataRepository.getAuthors().keySet().stream().collect(Collectors.toMap(x -> x, x -> x));
        Map<Commit, Commit> commitMap = dataRepository.getCommits().keySet().stream().collect(Collectors.toMap(x -> x, x -> x));

        dataRepository.getLinks().forEach(link -> {
            assertSame(fileMap.get(link.getFile()), link.getFile());
            assertSame(authorMap.get(link.getAuthor()), link.getAuthor());
            assertSame(commitMap.get(link.getCommit()), link.getCommit());
        });
    }

    public static void assertValueInNeighbourhood(double expected, double value) {
        assertTrue("Value mismatch. Expected: " + expected + ", actual: " + value, value > expected - 0.00001);
        assertTrue("Value mismatch. Expected: " + expected + ", actual: " + value, value < expected + 0.00001);
    }

    public static void assertValueInNeighbourhood(Date expected, Date value) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(expected);
        calendar.add(Calendar.MINUTE, 1);
        assertTrue("Value mismatch. Expected: " + df.format(expected) + ", actual: " + df.format(value), calendar.getTimeInMillis() > value.getTime());

        calendar.add(Calendar.MINUTE, -2);
        assertTrue("Value mismatch. Expected: " + df.format(expected) + ", actual: " + df.format(value), calendar.getTimeInMillis() < value.getTime());
    }

    public static DataRepository buildDataRepository(String[][] links) {
        DataRepository dataRepository = new DataRepository(new Date());
        IntStream.range(0, links.length).forEach(i -> {
            String[] link = links[i];
            try {
                Link linkObject = new Link(
                        new Author(link[0], link[1]),
                        new Commit(link[2], df.parse(link[3]), link[4]),
                        new org.ak.gitanalyzer.step2.data.File(link[5])
                );
                if (link.length > 6) {
                    linkObject.setWeight(Double.valueOf(link[6]));
                }
                dataRepository.addLink(linkObject);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        dataRepository.updateFiles();
        dataRepository.updateAuthors(Configuration.INSTANCE.getTeams(), Configuration.INSTANCE.getLocations());
        dataRepository.arrangeRepository();
        assertConsistency(dataRepository);
        return dataRepository;
    }

    public static RawRepository buildRepository(String[] paths, String[][][] commits) {
        return buildRepository(paths, commits, new Date());
    }

    public static RawRepository buildRepository(String[] paths, String[][][] commits, Date buildDate) {
        List<RawFile> rawFiles = new ArrayList<>();
        IntStream.range(0, paths.length).forEach(i -> {
            RawFile rawFile = new RawFile(paths[i]);
            rawFiles.add(rawFile);
            IntStream.range(0, commits[i].length).forEach(j -> {
                String[] desc = commits[i][j];
                try {
                    rawFile.getCommits().add(new RawCommit(desc[0], df.parse(desc[1]), desc[2], desc[3], desc[4]));
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            });
        });
        return new RawRepository(rawFiles, buildDate);
    }

    public static Map<String, Subprocess.SubprocessResult> toResults(String branch, String[] paths, String[][] commits) {
        return toResults(branch, paths, commits, "2011-11-21");
    }

    public static Map<String, Subprocess.SubprocessResult> toResults(String branch, String[] paths, String[][] commits, String sinceDate) {
        Map<String, Subprocess.SubprocessResult> expectedResults = new HashMap<>();
        addResult(expectedResults, "git,ls-tree,-r,--name-only," + branch, paths, null);
        if (commits != null) {
            IntStream.range(0, paths.length).forEach(i -> {
                addResult(expectedResults, "git,log,--all,--no-merges,--pretty=format:\"%H]|[%an]|[%ae]|[%ad]|[%s\",--since=\"" + sinceDate + "\",--before=\"2016-11-21\",\"" + paths[i] + "\"", commits[i], null);
            });
        }
        return expectedResults;
    }

    public static Map<String, Subprocess.SubprocessResult> toLsTreeExitCode1(String branch) {
        Map<String, Subprocess.SubprocessResult> expectedResults = new HashMap<>();
        addResult(expectedResults, "git,ls-tree,-r,--name-only," + branch, 1);
        return expectedResults;
    }

    public static Map<String, Subprocess.SubprocessResult> toLogExitCode1(String branch, String[] paths) {
        Map<String, Subprocess.SubprocessResult> expectedResults = new HashMap<>();
        addResult(expectedResults, "git,ls-tree,-r,--name-only," + branch, paths, null);
        IntStream.range(0, paths.length).forEach(i -> {
            addResult(expectedResults, "git,log,--all,--no-merges,--pretty=format:\"%H]|[%an]|[%ae]|[%ad]|[%s\",--since=\"2011-11-21\",--before=\"2016-11-21\",\"" + paths[i] + "\"", 1);
        });
        return expectedResults;
    }

    public static Map<String, Subprocess.SubprocessResult> toLsTreeErrOut(String branch, String[] paths) {
        Map<String, Subprocess.SubprocessResult> expectedResults = new HashMap<>();
        addResult(expectedResults, "git,ls-tree,-r,--name-only," + branch, null, paths);
        return expectedResults;
    }

    public static Map<String, Subprocess.SubprocessResult> toLogErrOut(String branch, String[] paths, String[][] commits) {
        Map<String, Subprocess.SubprocessResult> expectedResults = new HashMap<>();
        addResult(expectedResults, "git,ls-tree,-r,--name-only," + branch, paths, null);
        IntStream.range(0, paths.length).forEach(i -> {
            addResult(expectedResults, "git,log,--all,--no-merges,--pretty=format:\"%H]|[%an]|[%ae]|[%ad]|[%s\",--since=\"2011-11-21\",--before=\"2016-11-21\",\"" + paths[i] + "\"", null, commits[i]);
        });
        return expectedResults;
    }

    public static Map<String, SubprocessException> toExceptions(String branch, String[] paths, String lsTreeException, String[] logExceptions) {
        Map<String, SubprocessException> expectedExceptions = new HashMap<>();
        if (lsTreeException != null) {
            expectedExceptions.put("git,ls-tree,-r,--name-only," + branch, new SubprocessException(lsTreeException));
        }
        if (logExceptions != null) {
            IntStream.range(0, paths.length)
                    .filter(i -> logExceptions[i] != null)
                    .forEach(i -> expectedExceptions.put(
                            "git,log,--all,--no-merges,--pretty=format:\"%H]|[%an]|[%ae]|[%ad]|[%s\",--since=\"2011-11-21\",--before=\"2016-11-21\",\"" + paths[i] + "\"",
                            new SubprocessException(logExceptions[i])
                    ));
        }
        return expectedExceptions;
    }

    private static void addResult(Map<String, Subprocess.SubprocessResult> expectedResults, String command, String[] out, String[] errOut) {
        expectedResults.put(command, getResult(command, out, errOut));
    }

    private static void addResult(Map<String, Subprocess.SubprocessResult> expectedResults, String command, int exitCode) {
        expectedResults.put(command, new Subprocess.SubprocessResult(exitCode, command));
    }

    private static Subprocess.SubprocessResult getResult(String command, String[] out, String[] errOut) {
        Subprocess.SubprocessResult result = new Subprocess.SubprocessResult(command);
        if (out != null) {
            result.getOut().addAll(Arrays.asList(out));
        }
        if (errOut != null) {
            result.getErrorOut().addAll(Arrays.asList(errOut));
        }
        return result;
    }
}
