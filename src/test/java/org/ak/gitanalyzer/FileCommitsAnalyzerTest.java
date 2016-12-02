package org.ak.gitanalyzer;

import org.ak.gitanalyzer.step2.data.Author;
import org.ak.gitanalyzer.step2.data.DataRepository;
import org.ak.gitanalyzer.step3.FileCommitsAnalyzer;
import org.ak.gitanalyzer.step3.data.FileStatistics;
import org.ak.gitanalyzer.util.FileHelper;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.ak.gitanalyzer.util.TestHelper.assertValueInNeighbourhood;
import static org.ak.gitanalyzer.util.TestHelper.buildDataRepository;
import static org.junit.Assert.assertEquals;

/**
 * Created by Andrew on 26.11.2016.
 */
public class FileCommitsAnalyzerTest extends AnalyserTestBase {

    @Test
    public void getFileStatisticsTest() throws Exception {
        String[][] links = {
                {"email_1", "author_1", "10000", "Mon Nov 21 13:10:55 2016 EST", "comment_1", "module1/src/file1", "0.25"},
                {"email_2", "author_2", "10001", "Mon Nov 21 13:11:55 2016 EST", "comment_2", "module1/src/file1", "0.25"},
                {"email_2", "author_2", "10002", "Mon Nov 21 13:12:55 2016 EST", "comment_3", "module1/src/file1"},
                {"email_2", "author_2", "10003", "Mon Nov 21 13:13:55 2016 EST", "comment_4", "module1/src/file2"},
                {"email_3", "author_3", "10004", "Mon Nov 21 13:14:55 2016 EST", "comment_5", "module1/src/file2", "0.8"},
                {"email_3", "author_3", "10005", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "module1/src/file3", "0.3"}
        };
        DataRepository dataRepository = buildDataRepository(links);
        FileStatistics fileStatistics = new FileCommitsAnalyzer().getFileStatistics(dataRepository);
        assertEquals(3, fileStatistics.getFileSummaries().size());
        assertValueInNeighbourhood(3.6, fileStatistics.getSummaryWeight());
        assertEquals("module1/src/file2", fileStatistics.getFileSummaries().get(0).getFile().getPath());
        assertValueInNeighbourhood(1.8, fileStatistics.getFileSummaries().get(0).getFileWeight());
        assertEquals("module1/src/file1", fileStatistics.getFileSummaries().get(1).getFile().getPath());
        assertValueInNeighbourhood(1.5, fileStatistics.getFileSummaries().get(1).getFileWeight());
        assertEquals("module1/src/file3", fileStatistics.getFileSummaries().get(2).getFile().getPath());
        assertValueInNeighbourhood(0.3, fileStatistics.getFileSummaries().get(2).getFileWeight());

        //author filter
        fileStatistics = new FileCommitsAnalyzer().setAuthor(new Author("email_2", "author_2")).getFileStatistics(dataRepository);
        assertEquals(2, fileStatistics.getFileSummaries().size());
        assertValueInNeighbourhood(2.25, fileStatistics.getSummaryWeight());
        assertEquals("module1/src/file1", fileStatistics.getFileSummaries().get(0).getFile().getPath());
        assertValueInNeighbourhood(1.25, fileStatistics.getFileSummaries().get(0).getFileWeight());
        assertEquals("module1/src/file2", fileStatistics.getFileSummaries().get(1).getFile().getPath());
        assertValueInNeighbourhood(1.0, fileStatistics.getFileSummaries().get(1).getFileWeight());

        //team filter
        String[][] links2 = {
                {"author1@email.org", "author_1", "10000", "Mon Nov 21 13:10:55 2016 EST", "comment_1", "module1/src/file1", "0.25"},
                {"author2@email.org", "author_2", "10001", "Mon Nov 21 13:11:55 2016 EST", "comment_2", "module1/src/file1", "0.25"},
                {"author2@email.org", "author_2", "10002", "Mon Nov 21 13:12:55 2016 EST", "comment_3", "module1/src/file1"},
                {"author2@email.org", "author_2", "10003", "Mon Nov 21 13:13:55 2016 EST", "comment_4", "module1/src/file2"},
                {"author3@email.org", "author_3", "10004", "Mon Nov 21 13:14:55 2016 EST", "comment_5", "module1/src/file2", "0.8"},
                {"author_3",          "author_3", "10005", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "module1/src/file3", "0.3"}
        };
        DataRepository dataRepository2 = buildDataRepository(links2);
        fileStatistics = new FileCommitsAnalyzer().setTeam("team2").getFileStatistics(dataRepository2);
        assertEquals(2, fileStatistics.getFileSummaries().size());
        assertValueInNeighbourhood(3.05, fileStatistics.getSummaryWeight());
        assertEquals("module1/src/file2", fileStatistics.getFileSummaries().get(0).getFile().getPath());
        assertValueInNeighbourhood(1.8, fileStatistics.getFileSummaries().get(0).getFileWeight());
        assertEquals("module1/src/file1", fileStatistics.getFileSummaries().get(1).getFile().getPath());
        assertValueInNeighbourhood(1.25, fileStatistics.getFileSummaries().get(1).getFileWeight());

        //mask filter

        //mask filter
        String[][] links3 = {
                {"email_1", "author_1", "10000", "Mon Nov 21 13:10:55 2016 EST", "comment_1", "module6/src/file1", "0.25"},
                {"email_2", "author_2", "10001", "Mon Nov 21 13:11:55 2016 EST", "comment_2", "module5/src/file2", "0.25"},
                {"email_3", "author_3", "10002", "Mon Nov 21 13:12:55 2016 EST", "comment_3", "module4/src/file3"},
                {"email_4", "author_4", "10003", "Mon Nov 21 13:13:55 2016 EST", "comment_4", "module3/src/file4"},
                {"email_5", "author_5", "10004", "Mon Nov 21 13:14:55 2016 EST", "comment_5", "module2/src/file5", "0.3"},
                {"email_6", "author_6", "10005", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "module1/src/file6", "0.3"}
        };
        DataRepository dataRepository3 = buildDataRepository(links3);
        fileStatistics = new FileCommitsAnalyzer().setFileMask(Pattern.compile(FileHelper.quoteFileMask("module1/src/*"))).getFileStatistics(dataRepository3);
        assertValueInNeighbourhood(0.3, fileStatistics.getSummaryWeight());
        assertEquals(1, fileStatistics.getFileSummaries().size());
        fileStatistics = new FileCommitsAnalyzer().setFileMask(Pattern.compile(FileHelper.quoteFileMask("module"))).getFileStatistics(dataRepository3);
        assertValueInNeighbourhood(3.1, fileStatistics.getSummaryWeight());
        assertEquals(6, fileStatistics.getFileSummaries().size());
        fileStatistics = new FileCommitsAnalyzer().setFileMask(Pattern.compile(FileHelper.quoteFileMask("unknown"))).getFileStatistics(dataRepository3);
        assertValueInNeighbourhood(0, fileStatistics.getSummaryWeight());
        assertEquals(0, fileStatistics.getFileSummaries().size());
        fileStatistics = new FileCommitsAnalyzer().setFileMask(Pattern.compile(FileHelper.quoteFileMask("module*/src/file*"))).getFileStatistics(dataRepository3);
        assertValueInNeighbourhood(3.1, fileStatistics.getSummaryWeight());
        assertEquals(6, fileStatistics.getFileSummaries().size());
        fileStatistics = new FileCommitsAnalyzer().setFileMask(Pattern.compile(FileHelper.quoteFileMask("*5*"))).getFileStatistics(dataRepository3);
        assertValueInNeighbourhood(0.55, fileStatistics.getSummaryWeight());
        assertEquals(2, fileStatistics.getFileSummaries().size());
    }

    @Test
    public void getModuleStatisticsTest() throws Exception {
        String[][] links = {
                {"email_1", "author_1", "10000", "Mon Nov 21 13:10:55 2016 EST", "comment_1", "module1/src/file1", "0.25"},
                {"email_2", "author_2", "10001", "Mon Nov 21 13:11:55 2016 EST", "comment_2", "module1/src/file1", "0.25"},
                {"email_2", "author_2", "10002", "Mon Nov 21 13:12:55 2016 EST", "comment_3", "module1/src/file2"},
                {"email_2", "author_2", "10003", "Mon Nov 21 13:13:55 2016 EST", "comment_4", "module2/src/file2"},
                {"email_3", "author_3", "10004", "Mon Nov 21 13:14:55 2016 EST", "comment_5", "module2/src/file2", "0.8"},
                {"email_3", "author_3", "10005", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "module3/src/file3", "0.3"}
        };
        DataRepository dataRepository = buildDataRepository(links);
        FileCommitsAnalyzer analyzer = new FileCommitsAnalyzer();
        FileStatistics fileStatistics = analyzer.getFileStatistics(dataRepository);
        FileStatistics moduleStatistics = analyzer.getModuleStatistics(fileStatistics);
        assertEquals(3, moduleStatistics.getFileSummaries().size());
        assertValueInNeighbourhood(3.6, moduleStatistics.getSummaryWeight());
        assertEquals("module2", moduleStatistics.getFileSummaries().get(0).getFile().getPath());
        assertValueInNeighbourhood(1.8, moduleStatistics.getFileSummaries().get(0).getFileWeight());
        assertEquals("module1", moduleStatistics.getFileSummaries().get(1).getFile().getPath());
        assertValueInNeighbourhood(1.5, moduleStatistics.getFileSummaries().get(1).getFileWeight());
        assertEquals("module3", moduleStatistics.getFileSummaries().get(2).getFile().getPath());
        assertValueInNeighbourhood(0.3, moduleStatistics.getFileSummaries().get(2).getFileWeight());

    }
}
