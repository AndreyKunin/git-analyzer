package org.ak.gitanalyzer;

import org.ak.gitanalyzer.mock.SubprocessMock;
import org.ak.gitanalyzer.step1.data.RawCommit;
import org.ak.gitanalyzer.step1.data.RawFile;
import org.ak.gitanalyzer.step1.data.RawRepository;
import org.ak.gitanalyzer.step1.git.Subprocess;
import org.ak.gitanalyzer.step1.git.SubprocessException;
import org.ak.gitanalyzer.util.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.IntStream;

import static org.ak.gitanalyzer.util.TestHelper.*;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;

/**
 * Created by Andrew on 21.11.2016.
 */
public class GitLoaderTest extends TestFixture implements Serializable {

    private static final SimpleDateFormat df = new SimpleDateFormat("E MMM d HH:mm:ss yyyy Z", Locale.US);

    @Before
    public void init() throws Exception {
        Configuration.INSTANCE.clean();
        deleteDir(new File("./target/classes/conf"));
        deleteDir(new File("./target/classes/cache"));
        Main.main(new String[] {"-install"});
        Configuration.INSTANCE.initConfiguration();
    }

    @Test
    public void loadFromCacheSuccess() throws Exception {
        SubprocessMock subprocess = new SubprocessMock(new HashMap<>(), new HashMap<>());

        ArrayList<RawFile> rawFiles = new ArrayList<>();
        RawFile rawFile = new RawFile("test/path");
        rawFile.getCommits().add(new RawCommit("1234", new Date(), "author1", "author1@email.com", "test comment"));
        rawFile.setException(new SubprocessException("message", new Throwable("message2")));
        rawFiles.add(rawFile);
        RawRepository rawRepository = new RawRepository(rawFiles, new Date());
        rawRepository.persist();

        RawRepository loadedRepository = new Initializer(subprocess).loadRawRepository();

        assertEquals(rawRepository.getBuildDate(), loadedRepository.getBuildDate());
        assertEquals(rawRepository.getRawFiles().size(), loadedRepository.getRawFiles().size());

        RawFile rawFile1 = rawRepository.getRawFiles().get(0);
        RawFile rawFile2 = loadedRepository.getRawFiles().get(0);
        assertEquals(rawFile1.getPath(), rawFile2.getPath());
        assertEquals(rawFile1.getException().getMessage(), rawFile2.getException().getMessage());
        assertEquals(rawFile1.getException().getCause().getMessage(), rawFile2.getException().getCause().getMessage());
        assertEquals(rawFile1.getCommits().size(), rawFile2.getCommits().size());

        RawCommit rawCommit1 = rawFile1.getCommits().get(0);
        RawCommit rawCommit2 = rawFile2.getCommits().get(0);
        assertEquals(rawCommit1.getHash(), rawCommit2.getHash());
        assertEquals(rawCommit1.getCommitDateTime(), rawCommit2.getCommitDateTime());
        assertEquals(rawCommit1.getAuthorName(), rawCommit2.getAuthorName());
        assertEquals(rawCommit1.getAuthorEmail(), rawCommit2.getAuthorEmail());
        assertEquals(rawCommit1.getComment(), rawCommit2.getComment());

        subprocess.assertCalls();
    }

    @Test
    public void loadFromCacheNoFile() throws Exception {
        Configuration.INSTANCE.setString("GIT.repository.paths", "");
        Configuration.INSTANCE.setString("GIT.branch.names", "");
        Configuration.INSTANCE.setString("GIT.repository.filename.prefixes", "");

        SubprocessMock subprocess = new SubprocessMock(new HashMap<>(), new HashMap<>());
        assertFalse(new File("./target/classes/cache/git-analyzer.tmp").exists());

        RawRepository loadedRepository = new Initializer(subprocess).loadRawRepository();
        assertNotNull(loadedRepository);
        assertTrue(new File("./target/classes/cache/git-analyzer.tmp").exists());

        subprocess.assertCalls();

    }

    @Test
    public void loadFromCacheInvalidFile() throws Exception {
        Configuration.INSTANCE.setString("GIT.repository.paths", "");
        Configuration.INSTANCE.setString("GIT.branch.names", "");
        Configuration.INSTANCE.setString("GIT.repository.filename.prefixes", "");

        SubprocessMock subprocess = new SubprocessMock(new HashMap<>(), new HashMap<>());
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("./target/classes/cache/git-analyzer.tmp"))) {
            out.writeObject(this);
        }
        assertTrue(new File("./target/classes/cache/git-analyzer.tmp").exists());

        RawRepository loadedRepository = new Initializer(subprocess).loadRawRepository();
        assertNotNull(loadedRepository);
        assertTrue(new File("./target/classes/cache/git-analyzer.tmp").exists());
        assertTrue(loadedRepository instanceof RawRepository);

        subprocess.assertCalls();
    }

    @Test
    public void loadFromGitSuccessNoPrefixes() throws Exception {
        String[] paths = {
                "/module1/file1",
                "/module1/file2",
                "module20/file3",
                "module20/file4"
        };
        String[][] commits = new String[][] {
                {"123456]|[author1]|[author1@email.com]|[Mon Nov 21 13:10:55 2016 EST]|[comment1", "123457]|[author2]|[author2@email.com]|[Wrong Date]|[comment2"},
                {""},
                {"123458]|[]|[author1@email.com]|[Mon Nov 21 13:11:55 2016 EST]|[comment   ...   ", "123459]|[author1]|[]|[]|[", "123460]|[author1]|["},
                {}
        };

        SubprocessMock subprocess = new SubprocessMock(toResults("master", paths, commits), new HashMap<>());
        assertFalse(new File("./target/classes/cache/git-analyzer.tmp").exists());

        RawRepository loadedRepository = new Initializer(subprocess).loadRawRepository();
        assertRepository(loadedRepository, paths, commits, new int[] {2,0,2,0});

        subprocess.assertCalls();
    }

    @Test
    public void loadFromGitSuccessWithPrefixes() throws Exception {
        Configuration.INSTANCE.setString("GIT.repository.paths", "./path1,./path2");
        Configuration.INSTANCE.setString("GIT.branch.names", "master,develop");
        Configuration.INSTANCE.setString("GIT.repository.filename.prefixes", "repo1,repo2");
        String[] paths1 = {
                "/module1/file1",
                "module20/file2"
        };
        String[][] commits1 = new String[][] {
                {"123456]|[author1]|[author1@email.com]|[Mon Nov 21 13:10:55 2016 EST]|[comment1"},
                {"123458]|[]|[author1@email.com]|[Mon Nov 21 13:11:55 2016 EST]|[comment   ...   "}
        };
        String[] paths2 = {
                "/module2/file1",
                "module30/file2"
        };
        String[][] commits2 = new String[][] {
                {"123459]|[author1]|[author1@email.com]|[Mon Nov 21 13:10:55 2016 EST]|[comment1"},
                {"123460]|[]|[author1@email.com]|[Mon Nov 21 13:11:55 2016 EST]|[comment   ...   "}
        };

        Map<String, Subprocess.SubprocessResult> expectedResults = toResults("master", paths1, commits1);
        expectedResults.putAll(toResults("develop", paths2, commits2));
        SubprocessMock subprocess = new SubprocessMock(expectedResults, new HashMap<>());

        RawRepository loadedRepository = new Initializer(subprocess).loadRawRepository();
        assertRepository(loadedRepository,
                new String[] {"repo1/module1/file1", "repo1/module20/file2", "repo2/module2/file1", "repo2/module30/file2"},
                new String[][] {commits1[0], commits1[1], commits2[0], commits2[1]},
                new int[] {1,1,1,1}
        );

        subprocess.assertCalls();
    }

    @Test
    public void loadFromGitInvalidBranchNames() throws Exception {
        Configuration.INSTANCE.setString("GIT.repository.paths", "./path1,./path2");
        Configuration.INSTANCE.setString("GIT.branch.names", "master");
        Configuration.INSTANCE.setString("GIT.repository.filename.prefixes", "repo1,repo2");

        SubprocessMock subprocess = new SubprocessMock(new HashMap<>(), new HashMap<>());

        RawRepository loadedRepository = new Initializer(subprocess).loadRawRepository();
        assertNull(loadedRepository);

        subprocess.assertCalls();
    }

    @Test
    public void loadFromGitInvalidPrefixes() throws Exception {
        Configuration.INSTANCE.setString("GIT.repository.paths", "./path1,./path2");
        Configuration.INSTANCE.setString("GIT.branch.names", "master,develop");
        Configuration.INSTANCE.setString("GIT.repository.filename.prefixes", "repo1");

        SubprocessMock subprocess = new SubprocessMock(new HashMap<>(), new HashMap<>());

        RawRepository loadedRepository = new Initializer(subprocess).loadRawRepository();
        assertNull(loadedRepository);

        subprocess.assertCalls();
    }

    @Test
    public void loadFromGitLsTreeSubprocessException() throws Exception {
        String[] paths = {
                "/module1/file1",
                "module20/file2"
        };
        SubprocessMock subprocess = new SubprocessMock(new HashMap<>(), toExceptions("master", paths, "Test exception", null));

        RawRepository loadedRepository = new Initializer(subprocess).loadRawRepository();
        assertNull(loadedRepository);

        subprocess.assertCalls();
    }

    @Test
    public void loadFromGitLsTreeOutputError() throws Exception {
        String[] paths = {
                "/module1/file1",
                "module20/file2"
        };
        SubprocessMock subprocess = new SubprocessMock(toLsTreeErrOut("master", new String[] {paths[0]}), new HashMap<>());

        RawRepository loadedRepository = new Initializer(subprocess).loadRawRepository();
        assertNull(loadedRepository);

        subprocess.assertCalls();
    }

    @Test
    public void loadFromGitLsTreeErrorExit() throws Exception {
        SubprocessMock subprocess = new SubprocessMock(toLsTreeExitCode1("master"), new HashMap<>());

        RawRepository loadedRepository = new Initializer(subprocess).loadRawRepository();
        assertNull(loadedRepository);

        subprocess.assertCalls();
    }

    @Test
    public void loadFromGitLsTreeNoOutput() throws Exception {
        SubprocessMock subprocess = new SubprocessMock(toResults("master", new String[0], null), new HashMap<>());

        RawRepository loadedRepository = new Initializer(subprocess).loadRawRepository();
        assertNull(loadedRepository);

        subprocess.assertCalls();
    }

    @Test
    public void loadFromGitLogSubprocessException() throws Exception {
        String[] paths = {
                "/module1/file1",
                "module20/file2"
        };
        String[] exceptions = {
                "error1",
                "error2"
        };
        SubprocessMock subprocess = new SubprocessMock(toResults("master", paths, null), toExceptions("master", paths, null, exceptions));

        RawRepository loadedRepository = new Initializer(subprocess).loadRawRepository();
        assertNull(loadedRepository);

        subprocess.assertCalls();
    }

    @Test
    public void loadFromGitLogOutputError() throws Exception {
        String[] paths = {
                "/module1/file1",
                "module20/file2"
        };
        String[][] errors = new String[][] {
                {null},
                {"error2"},
        };
        SubprocessMock subprocess = new SubprocessMock(toLogErrOut("master", paths, errors), new HashMap<>());

        RawRepository loadedRepository = new Initializer(subprocess).loadRawRepository();
        assertNull(loadedRepository);

        subprocess.assertCalls();
    }

    @Test
    public void loadFromGitLogErrorExit() throws Exception {
        String[] paths = {
                "/module1/file1",
                "module20/file2"
        };
        SubprocessMock subprocess = new SubprocessMock(toLogExitCode1("master", paths), new HashMap<>());

        RawRepository loadedRepository = new Initializer(subprocess).loadRawRepository();
        assertNull(loadedRepository);

        subprocess.assertCalls();
    }

    @Test
    public void loadFromGitLogNoOutput() throws Exception {
        String[] paths = {
                "/module1/file1",
                "module20/file2"
        };
        String[][] commits = new String[][] {
                {}, {}
        };
        SubprocessMock subprocess = new SubprocessMock(toResults("master", paths, commits), new HashMap<>());

        RawRepository loadedRepository = new Initializer(subprocess).loadRawRepository();
        assertNotNull(loadedRepository);
        assertEquals(2, loadedRepository.getRawFiles().size());
        loadedRepository.getRawFiles().forEach(rf -> assertEquals(0, rf.getCommits().size()));

        subprocess.assertCalls();
    }

    private void assertRepository(RawRepository loadedRepository, String[] paths, String[][] commits, int[] commitLengths) {
        assertNotNull(loadedRepository);
        assertEquals(4, loadedRepository.getRawFiles().size());
        IntStream.range(0, paths.length).forEach(i -> {
            RawFile rawFile = loadedRepository.getRawFiles().get(i);
            assertEquals(paths[i], rawFile.getPath());
            assertEquals(commitLengths[i], rawFile.getCommits().size());
            IntStream.range(0, commitLengths[i])
                    .filter(j -> commits[i][j] != null && commits[i][j].length() > 0)
                    .forEach(j -> {
                RawCommit rawCommit = rawFile.getCommits().get(j);
                assertThat(commits[i][j], containsString(nullable(rawCommit.getAuthorName())));
                assertThat(commits[i][j], containsString(nullable(rawCommit.getAuthorEmail())));
                assertThat(commits[i][j], containsString(nullable(rawCommit.getComment())));
                assertThat(commits[i][j], containsString(nullable(rawCommit.getHash())));
                if (commits[i][j].contains("EST")) {
                    try {
                        int estIndex = commits[i][j].indexOf("EST");
                        assertEquals(df.parse(commits[i][j].substring(commits[i][j].lastIndexOf("[", estIndex) + 1, estIndex + 3)), rawCommit.getCommitDateTime());
                    } catch (ParseException e) {
                        fail(e.getMessage());
                    }
                } else {
                    assertDateIsAlmostNow(rawCommit.getCommitDateTime());
                }
            });
        });
    }

    private String nullable(String s) {
        return s == null ? "" : s;
    }

    private void assertDateIsAlmostNow(Date date) {
        Calendar before = new GregorianCalendar();
        before.add(Calendar.MINUTE, -5);
        Calendar after = new GregorianCalendar();
        after.add(Calendar.MINUTE, 5);
        assertTrue(date.getTime() > before.getTimeInMillis());
        assertTrue(date.getTime() < after.getTimeInMillis());
    }

}
