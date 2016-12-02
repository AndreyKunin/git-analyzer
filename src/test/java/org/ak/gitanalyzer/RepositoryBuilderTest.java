package org.ak.gitanalyzer;

import org.ak.gitanalyzer.http.Application;
import org.ak.gitanalyzer.mock.SubprocessMock;
import org.ak.gitanalyzer.step1.data.RawFile;
import org.ak.gitanalyzer.step1.data.RawRepository;
import org.ak.gitanalyzer.step2.RepositoryBuilder;
import org.ak.gitanalyzer.step2.data.Author;
import org.ak.gitanalyzer.step2.data.Commit;
import org.ak.gitanalyzer.step2.data.DataRepository;
import org.ak.gitanalyzer.step2.data.File;
import org.ak.gitanalyzer.util.Configuration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.ak.gitanalyzer.util.TestHelper.*;
import static org.junit.Assert.*;

/**
 * Created by Andrew on 24.11.2016.
 */
public class RepositoryBuilderTest {

    @BeforeClass
    public static void prepare() throws Exception {
        Configuration.INSTANCE.clean();
        deleteDir(new java.io.File("./target/classes/conf"));
        deleteDir(new java.io.File("./target/classes/cache"));
        Main.main(new String[] {"-install"});
        Configuration.INSTANCE.initConfiguration();
    }

    @Before
    public void init() throws Exception {
        Configuration.INSTANCE.clean();
        Configuration.INSTANCE.initConfiguration();
    }

    @Test
    public void testSplitAndMerge() throws Exception {
        Configuration.INSTANCE.setString("GIT.read.batch.size", "2");

        RawRepository rawRepository = buildRepository(
                new String[] {
                        "module1/file1",
                        "module2/file2",
                        "module3/file3",
                        "module4/file4"
                },
                new String[][][] {
                        {
                                {"12345", "Mon Nov 21 13:10:55 2016 EST", "author_1", "email_1", "comment_1"},
                                {"12346", "Mon Nov 21 13:11:55 2016 EST", "author_2", "email_2", "comment_2"},
                                {"12347", "Mon Nov 21 13:12:55 2016 EST", "author_3", "email_3", "comment_3"},
                                {"12348", "Mon Nov 21 13:13:55 2016 EST", "author_4", "email_4", "comment_4"},
                                {"12349", "Mon Nov 21 13:14:55 2016 EST", "author_5", "email_5", "comment_5"}
                        }, {
                                {"12355", "Mon Nov 21 13:15:55 2016 EST", "author_1", "email_1", "comment_6"},
                                {"12356", "Mon Nov 21 13:16:55 2016 EST", "author_2", "email_2", "comment_7"},
                                {"12357", "Mon Nov 21 13:17:55 2016 EST", "author_3", "email_3", "comment_8"},
                                {"12358", "Mon Nov 21 13:18:55 2016 EST", "author_4", "email_4", "comment_9"},
                                {"12359", "Mon Nov 21 13:19:55 2016 EST", "author_5", "email_5", "comment_0"}
                        }, {
                                {"12345", "Mon Nov 21 13:10:55 2016 EST", "author_1", "email_1", "comment_1"},
                                {"12346", "Mon Nov 21 13:11:55 2016 EST", "author_2", "email_2", "comment_2"},
                                {"12347", "Mon Nov 21 13:12:55 2016 EST", "author_3", "email_3", "comment_3"},
                                {"12348", "Mon Nov 21 13:13:55 2016 EST", "author_4", "email_4", "comment_4"},
                                {"12349", "Mon Nov 21 13:14:55 2016 EST", "author_5", "email_5", "comment_5"}
                        }, {
                                {"12355", "Mon Nov 21 13:15:55 2016 EST", "author_1", "email_1", "comment_6"},
                                {"12356", "Mon Nov 21 13:16:55 2016 EST", "author_2", "email_2", "comment_7"},
                                {"12357", "Mon Nov 21 13:17:55 2016 EST", "author_3", "email_3", "comment_8"},
                                {"12358", "Mon Nov 21 13:18:55 2016 EST", "author_4", "email_4", "comment_9"},
                                {"12359", "Mon Nov 21 13:19:55 2016 EST", "author_5", "email_5", "comment_0"}
                        }
                }
        );

        DataRepository dataRepository = new RepositoryBuilder().build(rawRepository);
        assertEquals(4, dataRepository.getFiles().size());
        assertEquals(10, dataRepository.getCommits().size());
        assertEquals(5, dataRepository.getAuthors().size());
        assertEquals(20, dataRepository.getLinks().size());

        assertEquals(5, dataRepository.getFiles().get(new File("module1/file1")).size());
        assertEquals(2, dataRepository.getCommits().get(new Commit("12345", null, null)).size());
        assertEquals(4, dataRepository.getAuthors().get(new Author("email_1", "author_1")).size());

        assertConsistency(dataRepository);

        dataRepository.getLinks().forEach(link -> assertValueInNeighbourhood(1.0, link.getWeight()));
    }

    @Test
    public void testRebaseCommitsFiltering() throws Exception {
        RawRepository rawRepository = buildRepository(
                new String[] {
                        "module1/file1"
                },
                new String[][][] {
                        {
                                {"12345", "Mon Nov 21 13:10:55 2016 EST", "author_1", "email_1", "initial commit"},
                                {"12346", "Mon Nov 21 13:10:55 2016 EST", "author_2", "email_2", "rebase commit"}
                        }
                }
        );

        DataRepository dataRepository = new RepositoryBuilder().build(rawRepository);
        assertEquals(1, dataRepository.getFiles().size());
        assertEquals(1, dataRepository.getCommits().size());
        assertEquals(1, dataRepository.getAuthors().size());
        assertEquals(1, dataRepository.getLinks().size());
        assertEquals("author_1", dataRepository.getAuthors().keySet().iterator().next().getName());
        assertEquals("initial commit", dataRepository.getCommits().keySet().iterator().next().getComment());

        assertConsistency(dataRepository);

        dataRepository.getLinks().forEach(link -> assertValueInNeighbourhood(1.0, link.getWeight()));
    }

    @Test
    public void testDuplicateCommitsFiltering() throws Exception {
        RawRepository rawRepository = buildRepository(
                new String[] {
                        "module1/file1"
                },
                new String[][][] {
                        {
                                {"12345", "Mon Nov 21 13:10:55 2016 EST", "author_1", "email_1", "comment_1"},
                                {"12345", "Mon Nov 21 13:10:55 2016 EST", "author_1", "email_1", "comment_1"}
                        }
                }
        );

        DataRepository dataRepository = new RepositoryBuilder().build(rawRepository);
        assertEquals(1, dataRepository.getFiles().size());
        assertEquals(1, dataRepository.getCommits().size());
        assertEquals(1, dataRepository.getAuthors().size());
        assertEquals(1, dataRepository.getLinks().size());
        assertEquals("author_1", dataRepository.getAuthors().keySet().iterator().next().getName());
        assertEquals("comment_1", dataRepository.getCommits().keySet().iterator().next().getComment());

        assertConsistency(dataRepository);

        dataRepository.getLinks().forEach(link -> assertValueInNeighbourhood(1.0, link.getWeight()));
    }

    @Test
    public void testServiceCommitsFiltering() throws Exception {
        RawRepository rawRepository = buildRepository(
                new String[] {
                        "module1/file1"
                },
                new String[][][] {
                        {
                                {"12345", "Mon Nov 21 13:10:55 2016 EST", "author_1", "email_1", "comment_1"},
                                {"12346", "Mon Nov 21 13:11:55 2016 EST", "author_2", "email_2", "comment_2"},
                                {"12347", "Mon Nov 21 13:12:55 2016 EST", "author_3", "email_3", "initial upload from SVN"},
                                {"12348", "Mon Nov 21 13:13:55 2016 EST", "author_4", "email_4", "merge master to branch 1"}
                        }
                }
        );

        DataRepository dataRepository = new RepositoryBuilder().build(rawRepository);
        assertEquals(1, dataRepository.getFiles().size());
        assertEquals(2, dataRepository.getCommits().size());
        assertEquals(2, dataRepository.getAuthors().size());
        assertEquals(2, dataRepository.getLinks().size());

        assertConsistency(dataRepository);

        dataRepository.getLinks().forEach(link -> assertValueInNeighbourhood(1.0, link.getWeight()));
    }

    @Test
    public void testServiceAuthorsFiltering() throws Exception {
        RawRepository rawRepository = buildRepository(
                new String[] {
                        "module1/file1"
                },
                new String[][][] {
                        {
                                {"12345", "Mon Nov 21 13:10:55 2016 EST", "Author 1", "email_1", "comment_1"},
                                {"12346", "Mon Nov 21 13:11:55 2016 EST", "Author 2", "email_2", "comment_2"},
                                {"12347", "Mon Nov 21 13:12:55 2016 EST", "author3", "email_3", "comment_3"},
                                {"12348", "Mon Nov 21 13:13:55 2016 EST", "Author 4", "email_4", "comment_4"}
                        }
                }
        );

        DataRepository dataRepository = new RepositoryBuilder().build(rawRepository);
        assertEquals(1, dataRepository.getFiles().size());
        assertEquals(2, dataRepository.getCommits().size());
        assertEquals(2, dataRepository.getAuthors().size());
        assertEquals(2, dataRepository.getLinks().size());

        assertConsistency(dataRepository);

        dataRepository.getLinks().forEach(link -> assertValueInNeighbourhood(1.0, link.getWeight()));
    }

    @Test
    public void testAuthorAndEmailMerge() throws Exception {
        RawRepository rawRepository = buildRepository(
                new String[] {
                        "module1/file1"
                },
                new String[][][] {
                        {
                                {"12345", "Mon Nov 21 13:10:55 2016 EST", "author1", "author1.1@email.org", "comment_1"},
                                {"12346", "Mon Nov 21 13:11:55 2016 EST", "Author 1", "author1.2@email.org", "comment_2"},
                                {"12347", "Mon Nov 21 13:12:55 2016 EST", "Author 1", "email_1", "comment_2"}
                        }
                }
        );

        DataRepository dataRepository = new RepositoryBuilder().build(rawRepository);
        assertEquals(1, dataRepository.getFiles().size());
        assertEquals(3, dataRepository.getCommits().size());
        assertEquals(2, dataRepository.getAuthors().size());
        assertEquals(3, dataRepository.getLinks().size());
        dataRepository.getAuthors().keySet().forEach(author -> assertEquals("Author 1", author.getName()));

        assertConsistency(dataRepository);

        dataRepository.getLinks().forEach(link -> assertValueInNeighbourhood(1.0, link.getWeight()));
    }

    @Test
    public void testWeightAssignment() throws Exception {
        RawRepository rawRepository = buildRepository(
                new String[] {
                        "module1/file1"
                },
                new String[][][] {
                        {
                                {"12345", "Mon Nov 21 13:10:55 2016 EST", "author_1", "email_1", "TEST-12: commit 1"},
                                {"12346", "Mon Nov 21 13:11:55 2016 EST", "author_1", "email_1", "TEST-12"},
                                {"12347", "Mon Nov 21 13:12:55 2016 EST", "author_1", "email_1", "next commit for TEST-12"},
                                {"12348", "Mon Nov 21 13:13:55 2016 EST", "author_1", "email_1", "fixing TEST-12, TEST-13"},
                                {"12349", "Mon Nov 21 13:14:55 2016 EST", "author_1", "email_1", "TEST-13"}
                        }
                }
        );

        DataRepository dataRepository = new RepositoryBuilder().build(rawRepository);

        assertConsistency(dataRepository);

        assertValueInNeighbourhood(0.25, dataRepository.getCommits().get(new Commit("12345", null, null)).get(0).getWeight());
        assertValueInNeighbourhood(0.25, dataRepository.getCommits().get(new Commit("12346", null, null)).get(0).getWeight());
        assertValueInNeighbourhood(0.25, dataRepository.getCommits().get(new Commit("12347", null, null)).get(0).getWeight());
        assertValueInNeighbourhood(0.75, dataRepository.getCommits().get(new Commit("12348", null, null)).get(0).getWeight());
        assertValueInNeighbourhood(0.5, dataRepository.getCommits().get(new Commit("12349", null, null)).get(0).getWeight());
    }

    @Test
    public void testBuildFileMarkersAssignment() throws Exception {
        RawRepository rawRepository = buildRepository(
                new String[] {
                        "module1/file1",
                        "module1/pom.xml"
                },
                new String[][][] {
                        {
                                {"12345", "Mon Nov 21 13:10:55 2016 EST", "author_1", "email_1", "commit_1"}
                        }, {
                                {"12346", "Mon Nov 21 13:11:55 2016 EST", "author_2", "email_2", "commit_2"}
                        }
                }
        );

        DataRepository dataRepository = new RepositoryBuilder().build(rawRepository);
        assertFalse(dataRepository.getFiles().get(new File("module1/file1")).get(0).getFile().isVCSFile());
        assertTrue(dataRepository.getFiles().get(new File("module1/pom.xml")).get(0).getFile().isVCSFile());

        assertConsistency(dataRepository);
    }

    @Test
    public void testModuleNamesAssignment() throws Exception {
        RawRepository rawRepository = buildRepository(
                new String[] {
                        "module1/src/file1",
                        "module1/file2",
                        "module2/module2.1/pom.xml",
                        "module2/pom.xml",
                        "module2/module2.1/file3",
                        "module2/file4",
                        "module3/file5"
                },
                new String[][][] {
                        {
                                {"12345", "Mon Nov 21 13:10:55 2016 EST", "author_1", "email_1", "commit_1"}
                        }, {
                                {"12346", "Mon Nov 21 13:11:55 2016 EST", "author_2", "email_2", "commit_2"}
                        }, {
                                {"12347", "Mon Nov 21 13:12:55 2016 EST", "author_3", "email_3", "commit_3"}
                        }, {
                                {"12348", "Mon Nov 21 13:13:55 2016 EST", "author_4", "email_4", "commit_4"}
                        }, {
                                {"12349", "Mon Nov 21 13:14:55 2016 EST", "author_5", "email_5", "commit_5"}
                        }, {
                                {"12340", "Mon Nov 21 13:15:55 2016 EST", "author_6", "email_6", "commit_6"}
                        }, {
                                {"12341", "Mon Nov 21 13:16:55 2016 EST", "author_7", "email_7", "commit_7"}
                        }
                }
        );

        DataRepository dataRepository = new RepositoryBuilder().build(rawRepository);
        assertEquals("module1", dataRepository.getFiles().get(new File("module1/src/file1")).get(0).getFile().getModuleName());
        assertEquals("module1", dataRepository.getFiles().get(new File("module1/file2")).get(0).getFile().getModuleName());
        assertEquals("module2/module2.1", dataRepository.getFiles().get(new File("module2/module2.1/pom.xml")).get(0).getFile().getModuleName());
        assertEquals("module2", dataRepository.getFiles().get(new File("module2/pom.xml")).get(0).getFile().getModuleName());
        assertEquals("module2/module2.1", dataRepository.getFiles().get(new File("module2/module2.1/file3")).get(0).getFile().getModuleName());
        assertEquals("module2", dataRepository.getFiles().get(new File("module2/file4")).get(0).getFile().getModuleName());
        assertEquals("/", dataRepository.getFiles().get(new File("module3/file5")).get(0).getFile().getModuleName());

        assertConsistency(dataRepository);
    }

    @Test
    public void testLocationAssignment() throws Exception {
        RawRepository rawRepository = buildRepository(
                new String[] {
                        "module1/file1"
                },
                new String[][][] {
                        {
                                {"12345", "Mon Nov 21 13:10:55 2016 EST", "author_1", "author1@email.org", "commit_1"},
                                {"12346", "Mon Nov 21 13:11:55 2016 EST", "author_2", "author2@email.org", "commit_2"},
                                {"12347", "Mon Nov 21 13:12:55 2016 EST", "author_2", "author1.2@email.org", "commit_3"},
                                {"12348", "Mon Nov 21 13:13:55 2016 EST", "author_2", "other@email.org", "commit_4"},
                                {"12349", "Mon Nov 21 13:14:55 2016 EST", "author3", "author3@email.org", "commit_5"}
                        }
                }
        );

        DataRepository dataRepository = new RepositoryBuilder().build(rawRepository);
        dataRepository.getAuthors().keySet().forEach(author -> {
            switch (author.getEmail()) {
                case "author1@email.org":
                    assertEquals("us", author.getLocation());
                    break;
                case "author2@email.org":
                    assertEquals("ru", author.getLocation());
                    break;
                case "author1.2@email.org":
                    fail("Not merged");
                    break;
                case "author3@email.org":
                    fail("Not filtered");
                    break;
                case "other@email.org":
                    assertNull(author.getLocation());
                    break;
            }
        });

        assertConsistency(dataRepository);
    }

    @Test
    public void testTeamAssignment() throws Exception {
        RawRepository rawRepository = buildRepository(
                new String[] {
                        "module1/file1"
                },
                new String[][][] {
                        {
                                {"12345", "Mon Nov 21 13:10:55 2016 EST", "author_1", "author1@email.org", "commit_1"},
                                {"12346", "Mon Nov 21 13:11:55 2016 EST", "author_2", "author2@email.org", "commit_2"},
                                {"12347", "Mon Nov 21 13:12:55 2016 EST", "author_2", "author1.2@email.org", "commit_3"},
                                {"12347", "Mon Nov 21 13:13:55 2016 EST", "author_2", "other@email.org", "commit_4"},
                                {"12348", "Mon Nov 21 13:14:55 2016 EST", "author3", "author3@email.org", "commit_5"},
                                {"12349", "Mon Nov 21 13:15:55 2016 EST", "author4", "author3@email.org", "commit_6"}
                        }
                }
        );

        DataRepository dataRepository = new RepositoryBuilder().build(rawRepository);
        dataRepository.getAuthors().keySet().forEach(author -> {
            switch (author.getEmail()) {
                case "author1@email.org":
                    assertEquals("team1", author.getTeam());
                    break;
                case "author2@email.org":
                    assertEquals("team2", author.getTeam());
                    break;
                case "author1.2@email.org":
                    fail("Not merged");
                    break;
                case "author3@email.org":
                    if (author.getName().equals("author3")) {
                        fail("Not filtered");
                    } else {
                        assertEquals("team2", author.getTeam());
                    }
                    break;
                case "other@email.org":
                    assertNull(author.getTeam());
                    break;
            }
        });

        assertConsistency(dataRepository);
    }

    @Test
    public void testNoCommitsForFile() throws Exception {
        RawRepository rawRepository = buildRepository(
                new String[] {
                        "module1/file1",
                        "module1/file2"
                },
                new String[][][] {
                        {
                                {"12345", "Mon Nov 21 13:10:55 2016 EST", "author_1", "author1@email.org", "commit_1"}
                        }, {
                        }
                }
        );

        DataRepository dataRepository = new RepositoryBuilder().build(rawRepository);
        assertEquals(1, dataRepository.getFiles().size());
        assertEquals(1, dataRepository.getCommits().size());
        assertEquals(1, dataRepository.getAuthors().size());
        assertEquals(1, dataRepository.getLinks().size());
        assertEquals("module1/file1", dataRepository.getFiles().keySet().iterator().next().getPath());

        assertConsistency(dataRepository);
    }

    @Test
    public void testFindAuthor() throws Exception {
        RawRepository rawRepository = buildRepository(
                new String[] {
                        "module1/file1"
                },
                new String[][][] {
                        {
                                {"12345", "Mon Nov 21 13:10:55 2016 EST", "", "author1@email.org", "commit_1"},
                                {"12346", "Mon Nov 21 13:11:55 2016 EST", "author_2", "", "commit_2"},
                                {"12347", "Mon Nov 21 13:12:55 2016 EST", null, "author3@email.org", "commit_3"},
                                {"12348", "Mon Nov 21 13:13:55 2016 EST", "author_4", null, "commit_4"},
                                {"12349", "Mon Nov 21 13:14:55 2016 EST", "author_5", "author5@email.org", "commit_5"}
                        }
                }
        );
        DataRepository dataRepository = new RepositoryBuilder().build(rawRepository);
        assertConsistency(dataRepository);

        assertNull(dataRepository.findAuthor("()"));
        assertNull(dataRepository.findAuthor("author_5 ()"));
        assertNull(dataRepository.findAuthor(" (author5@email.org)"));
        assertNull(dataRepository.findAuthor("author_5 (author3@email.org)"));
        assertNotNull(dataRepository.findAuthor("author_5 (author5@email.org)"));
        assertNotNull(dataRepository.findAuthor(" author_5 ( author5@email.org )"));
        assertNotNull(dataRepository.findAuthor(" (author3@email.org)"));
        assertNotNull(dataRepository.findAuthor(" (author1@email.org)"));
        assertNotNull(dataRepository.findAuthor("author_2 ()"));
        assertNotNull(dataRepository.findAuthor("author_4 ()"));
    }

    @Test
    public void testIncrementalUpdate() throws Exception {
        //1. If called in wrong application mode
        SubprocessMock subprocess = new SubprocessMock(new HashMap<>(), new HashMap<>());
        Application.INSTANCE.setDataRepository(null);
        new Initializer(subprocess).new RepositoryUpdateTask().run();
        subprocess.assertCalls();
        assertNull(Application.INSTANCE.getDataRepository());

        //Test success
        RawRepository rawRepository = buildRepository(
                new String[] {
                        "module1/file1",
                        "module1/file2",
                        "module1/fileToDelete"
                },
                new String[][][] {
                        {
                                {"12345", "Mon Nov 21 13:10:55 2016 EST", "author_1", "email_1", "commit_1"},
                                {"12346", "Mon Nov 21 13:11:55 2016 EST", "author_2", "email_2", "commit_2"},
                        }, {
                        }, {
                                {"12344", "Mon Nov 21 13:12:55 2016 EST", "author_3", "email_3", "commit_3"},
                        }
                },
                df.parse("Tue Nov 22 13:10:55 2016 EST")
        );
        Map<String, RawFile> rawFiles = rawRepository.getRawFiles().stream().collect(Collectors.toMap(RawFile::getPath, rf -> rf));
        assertEquals(3, rawFiles.size());
        assertEquals(2, rawFiles.get("module1/file1").getCommits().size());
        assertEquals(0, rawFiles.get("module1/file2").getCommits().size());
        assertEquals(1, rawFiles.get("module1/fileToDelete").getCommits().size());
        rawRepository.persist();

        DataRepository dataRepository = new RepositoryBuilder().build(rawRepository);
        assertEquals(2, dataRepository.getFiles().size());
        assertEquals(3, dataRepository.getAuthors().size());
        assertEquals(3, dataRepository.getCommits().size());
        Application.INSTANCE.setDataRepository(dataRepository);

        String[] paths = {
                "module1/file1",
                "module1/file2",
                "module1/newFile"
        };
        String[][] commits = new String[][] {
                {"12347]|[author_1]|[email_1]|[Mon Nov 23 13:12:55 2016 EST]|[comment1"},
                {"12348]|[author_2]|[email_2]|[Mon Nov 23 13:13:55 2016 EST]|[comment2", "12349]|[author_3]|[email_3]|[Mon Nov 21 13:14:55 2016 EST]|[comment3"},
                {"12340]|[author_1]|[email_1]|[Mon Nov 23 13:15:55 2016 EST]|[comment1"},
        };
        subprocess = new SubprocessMock(toResults("master", paths, commits, "2016-11-22"), new HashMap<>());
        new Initializer(subprocess).new RepositoryUpdateTask().run();
        subprocess.assertCalls();

        rawRepository = RawRepository.restore();
        rawFiles = rawRepository.getRawFiles().stream().collect(Collectors.toMap(RawFile::getPath, rf -> rf));
        assertEquals(4, rawFiles.size());
        assertEquals(3, rawFiles.get("module1/file1").getCommits().size());
        assertEquals(2, rawFiles.get("module1/file2").getCommits().size());
        assertEquals(1, rawFiles.get("module1/fileToDelete").getCommits().size());
        assertEquals(1, rawFiles.get("module1/newFile").getCommits().size());
        assertValueInNeighbourhood(new Date(), rawRepository.getBuildDate());

        assertNotNull(Application.INSTANCE.getDataRepository());
        assertNotSame(dataRepository, Application.INSTANCE.getDataRepository());
        dataRepository = Application.INSTANCE.getDataRepository();
        assertEquals(4, dataRepository.getFiles().size());
        assertEquals(3, dataRepository.getAuthors().size());
        assertEquals(7, dataRepository.getCommits().size());
        assertValueInNeighbourhood(new Date(), dataRepository.getBuildDate());
        assertValueInNeighbourhood(rawRepository.getBuildDate(), dataRepository.getBuildDate());

        //Test failure
        subprocess = new SubprocessMock(new HashMap<>(), toExceptions("master", paths, "Test exception", null));
        new Initializer(subprocess).new RepositoryUpdateTask().run();
        subprocess.assertCalls();

        assertNotNull(Application.INSTANCE.getDataRepository());
        assertSame(dataRepository, Application.INSTANCE.getDataRepository());
    }
}
