package org.ak.gitanalyzer;

import org.ak.gitanalyzer.step2.data.DataRepository;
import org.ak.gitanalyzer.step3.FileAuthorsAnalyzer;
import org.ak.gitanalyzer.step3.data.FileAuthors;
import org.junit.Test;

import java.util.List;

import static org.ak.gitanalyzer.util.TestHelper.buildDataRepository;
import static org.junit.Assert.assertEquals;

/**
 * Created by Andrew on 26.11.2016.
 */
public class FileAuthorsAnalyzerTest extends AnalyserTestBase {

    @Test
    public void getSharedFilesTest() throws Exception {
        String[][] links = {
                {"email_1", "author_1", "10000", "Mon Nov 21 13:10:55 2016 EST", "comment_1", "module1/src/file1", "0.25"},
                {"email_1", "author_1", "10001", "Mon Nov 21 13:11:55 2016 EST", "comment_2", "module1/src/file1", "0.25"},
                {"email_2", "author_2", "10002", "Mon Nov 21 13:12:55 2016 EST", "comment_3", "module1/src/file1"},
                {"email_2", "author_2", "10003", "Mon Nov 21 13:13:55 2016 EST", "comment_4", "module1/src/file1"},
                {"email_3", "author_3", "10004", "Mon Nov 21 13:14:55 2016 EST", "comment_5", "module1/src/file2", "0.3"},
                {"email_3", "author_3", "10005", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "module1/src/file2", "0.3"}
        };
        DataRepository dataRepository = buildDataRepository(links);
        List<FileAuthors> sharedFiles = new FileAuthorsAnalyzer().getSharedFiles(dataRepository);
        assertEquals(2, sharedFiles.size());
        assertEquals(2, sharedFiles.get(0).getAuthorsCount());
        assertEquals("module1/src/file1", sharedFiles.get(0).getFile().getPath());
        assertEquals(1, sharedFiles.get(1).getAuthorsCount());
        assertEquals("module1/src/file2", sharedFiles.get(1).getFile().getPath());

        sharedFiles = new FileAuthorsAnalyzer().setMinAuthorsCount(2).getSharedFiles(dataRepository);
        assertEquals(1, sharedFiles.size());
        assertEquals(2, sharedFiles.get(0).getAuthorsCount());
        assertEquals("module1/src/file1", sharedFiles.get(0).getFile().getPath());

        sharedFiles = new FileAuthorsAnalyzer().setMinAuthorsCount(5).getSharedFiles(dataRepository);
        assertEquals(0, sharedFiles.size());
    }

    @Test
    public void getSharedModulesTest() throws Exception {
        String[][] links = {
                {"email_1", "author_1", "10000", "Mon Nov 21 13:10:55 2016 EST", "comment_1", "module1/src/file1", "0.25"},
                {"email_1", "author_1", "10001", "Mon Nov 21 13:11:55 2016 EST", "comment_1", "module1/src/file1", "0.25"},
                {"email_1", "author_1", "10002", "Mon Nov 21 13:12:55 2016 EST", "comment_1", "module1/src/file1"},
                {"email_1", "author_1", "10003", "Mon Nov 21 13:13:55 2016 EST", "comment_1", "module1/src/file1"},
                {"email_2", "author_2", "10004", "Mon Nov 21 13:14:55 2016 EST", "comment_1", "module2/src/file1", "0.3"},
                {"email_2", "author_2", "10005", "Mon Nov 21 13:15:55 2016 EST", "comment_1", "module2/src/file1", "0.3"},
                {"email_2", "author_2", "10006", "Mon Nov 21 13:16:55 2016 EST", "comment_1", "module2/src/file1", "0.25"},
                {"email_3", "author_3", "10007", "Mon Nov 21 13:17:55 2016 EST", "comment_1", "module2/src/file1", "0.25"},
                {"email_3", "author_3", "10008", "Mon Nov 21 13:18:55 2016 EST", "comment_1", "module3/src/file1"},
                {"email_3", "author_3", "10009", "Mon Nov 21 13:19:55 2016 EST", "comment_1", "module3/src/file1"},
                {"email_4", "author_4", "10010", "Mon Nov 21 13:20:55 2016 EST", "comment_1", "module3/src/file1", "0.3"},
                {"email_5", "author_5", "10011", "Mon Nov 21 13:21:55 2016 EST", "comment_1", "module3/src/file1", "0.3"}
        };
        DataRepository dataRepository = buildDataRepository(links);
        List<FileAuthors> sharedFiles = new FileAuthorsAnalyzer().getSharedFiles(dataRepository);
        List<FileAuthors> sharedModules = new FileAuthorsAnalyzer().getSharedModules(sharedFiles);
        assertEquals(3, sharedModules.size());
        assertEquals(3, sharedModules.get(0).getAuthorsCount());
        assertEquals("module3", sharedModules.get(0).getFile().getPath());
        assertEquals(2, sharedModules.get(1).getAuthorsCount());
        assertEquals("module2", sharedModules.get(1).getFile().getPath());
        assertEquals(1, sharedModules.get(2).getAuthorsCount());
        assertEquals("module1", sharedModules.get(2).getFile().getPath());

        sharedModules = new FileAuthorsAnalyzer().setMinAuthorsCount(2).getSharedModules(sharedFiles);
        assertEquals(2, sharedModules.size());
        assertEquals(3, sharedModules.get(0).getAuthorsCount());
        assertEquals("module3", sharedModules.get(0).getFile().getPath());
        assertEquals(2, sharedModules.get(1).getAuthorsCount());
        assertEquals("module2", sharedModules.get(1).getFile().getPath());

        sharedModules = new FileAuthorsAnalyzer().setMinAuthorsCount(5).getSharedModules(sharedFiles);
        assertEquals(0, sharedModules.size());
    }

}
