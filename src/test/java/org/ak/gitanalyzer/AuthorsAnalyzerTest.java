package org.ak.gitanalyzer;

import org.ak.gitanalyzer.step2.data.Author;
import org.ak.gitanalyzer.step2.data.DataRepository;
import org.ak.gitanalyzer.step3.AuthorsAnalyzer;
import org.ak.gitanalyzer.step3.data.AuthorGroupStatistics;
import org.ak.gitanalyzer.step3.data.AuthorStatistics;
import org.ak.gitanalyzer.util.FileHelper;
import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.ak.gitanalyzer.util.TestHelper.assertValueInNeighbourhood;
import static org.ak.gitanalyzer.util.TestHelper.buildDataRepository;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by Andrew on 26.11.2016.
 */
public class AuthorsAnalyzerTest extends AnalyserTestBase {

    @Test
    public void getAllContributorsTestWeightCalculation() throws Exception {
        String[][] links = {
                {"email_1", "author_1", "10000", "Mon Nov 21 13:10:55 2016 EST", "comment_1", "module1/src/file1", "0.25"},
                {"email_1", "author_1", "10001", "Mon Nov 21 13:11:55 2016 EST", "comment_2", "module1/src/file1", "0.25"},
                {"email_2", "author_2", "10002", "Mon Nov 21 13:12:55 2016 EST", "comment_3", "module1/src/file1"},
                {"email_2", "author_2", "10003", "Mon Nov 21 13:13:55 2016 EST", "comment_4", "module1/src/file1"},
                {"email_3", "author_3", "10004", "Mon Nov 21 13:14:55 2016 EST", "comment_5", "module1/src/file2", "0.3"},
                {"email_3", "author_3", "10005", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "module1/src/file2", "0.3"}
        };
        DataRepository dataRepository = buildDataRepository(links);
        AuthorStatistics statistics = new AuthorsAnalyzer().getAllContributors(dataRepository);
        assertEquals(3, statistics.getAuthorSummaries().size());
        assertEquals("author_2", statistics.getAuthorSummaries().get(0).getAuthor().getName());
        assertEquals("author_3", statistics.getAuthorSummaries().get(1).getAuthor().getName());
        assertEquals("author_1", statistics.getAuthorSummaries().get(2).getAuthor().getName());
        assertValueInNeighbourhood(2.0, statistics.getAuthorSummaries().get(0).getContributionWeight());
        assertValueInNeighbourhood(0.6, statistics.getAuthorSummaries().get(1).getContributionWeight());
        assertValueInNeighbourhood(0.5, statistics.getAuthorSummaries().get(2).getContributionWeight());
        assertValueInNeighbourhood(3.1, statistics.getSummaryContribution());
        assertValueInNeighbourhood(200/3.1, statistics.getContributionPercent(statistics.getAuthorSummaries().get(0)));
        assertValueInNeighbourhood(60/3.1, statistics.getContributionPercent(statistics.getAuthorSummaries().get(1)));
        assertValueInNeighbourhood(50/3.1, statistics.getContributionPercent(statistics.getAuthorSummaries().get(2)));

        //sample test
        statistics = new AuthorsAnalyzer().setSampleSize(2).getAllContributors(dataRepository);
        assertEquals(2, statistics.getAuthorSummaries().size());
        assertEquals("author_2", statistics.getAuthorSummaries().get(0).getAuthor().getName());
        assertEquals("author_3", statistics.getAuthorSummaries().get(1).getAuthor().getName());
        assertValueInNeighbourhood(2.0, statistics.getAuthorSummaries().get(0).getContributionWeight());
        assertValueInNeighbourhood(0.6, statistics.getAuthorSummaries().get(1).getContributionWeight());
        assertValueInNeighbourhood(3.1, statistics.getSummaryContribution());
        assertValueInNeighbourhood(200/3.1, statistics.getContributionPercent(statistics.getAuthorSummaries().get(0)));
        assertValueInNeighbourhood(60/3.1, statistics.getContributionPercent(statistics.getAuthorSummaries().get(1)));

        //path filter
        statistics = new AuthorsAnalyzer().setPath("module1/src/file1").getAllContributors(dataRepository);
        assertEquals(2, statistics.getAuthorSummaries().size());
        assertEquals("author_2", statistics.getAuthorSummaries().get(0).getAuthor().getName());
        assertEquals("author_1", statistics.getAuthorSummaries().get(1).getAuthor().getName());
        assertValueInNeighbourhood(2.0, statistics.getAuthorSummaries().get(0).getContributionWeight());
        assertValueInNeighbourhood(0.5, statistics.getAuthorSummaries().get(1).getContributionWeight());
        assertValueInNeighbourhood(2.5, statistics.getSummaryContribution());
        assertValueInNeighbourhood(200/2.5, statistics.getContributionPercent(statistics.getAuthorSummaries().get(0)));
        assertValueInNeighbourhood(50/2.5, statistics.getContributionPercent(statistics.getAuthorSummaries().get(1)));

        statistics = new AuthorsAnalyzer().setPath("module1/src/unknown file").getAllContributors(dataRepository);
        assertEquals(0, statistics.getAuthorSummaries().size());
        assertValueInNeighbourhood(0, statistics.getSummaryContribution());

        //mask filter
        String[][] links2 = {
                {"email_1", "author_1", "10000", "Mon Nov 21 13:10:55 2016 EST", "comment_1", "module6/src/file1", "0.25"},
                {"email_2", "author_2", "10001", "Mon Nov 21 13:11:55 2016 EST", "comment_2", "module5/src/file2", "0.25"},
                {"email_3", "author_3", "10002", "Mon Nov 21 13:12:55 2016 EST", "comment_3", "module4/src/file3"},
                {"email_4", "author_4", "10003", "Mon Nov 21 13:13:55 2016 EST", "comment_4", "module3/src/file4"},
                {"email_5", "author_5", "10004", "Mon Nov 21 13:14:55 2016 EST", "comment_5", "module2/src/file5", "0.3"},
                {"email_6", "author_6", "10005", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "module1/src/file6", "0.3"}
        };
        DataRepository dataRepository2 = buildDataRepository(links2);
        statistics = new AuthorsAnalyzer().setFileMask(Pattern.compile(FileHelper.quoteFileMask("module1/src/*"))).getAllContributors(dataRepository2);
        assertEquals(1, statistics.getAuthorSummaries().size());
        statistics = new AuthorsAnalyzer().setFileMask(Pattern.compile(FileHelper.quoteFileMask("module"))).getAllContributors(dataRepository2);
        assertEquals(6, statistics.getAuthorSummaries().size());
        statistics = new AuthorsAnalyzer().setFileMask(Pattern.compile(FileHelper.quoteFileMask("unknown"))).getAllContributors(dataRepository2);
        assertEquals(0, statistics.getAuthorSummaries().size());
        statistics = new AuthorsAnalyzer().setFileMask(Pattern.compile(FileHelper.quoteFileMask("module*/src/file*"))).getAllContributors(dataRepository2);
        assertEquals(6, statistics.getAuthorSummaries().size());
        statistics = new AuthorsAnalyzer().setFileMask(Pattern.compile(FileHelper.quoteFileMask("*5*"))).getAllContributors(dataRepository2);
        assertEquals(2, statistics.getAuthorSummaries().size());

        //module filter
        statistics = new AuthorsAnalyzer().setModuleName("module3").getAllContributors(dataRepository2);
        assertEquals(1, statistics.getAuthorSummaries().size());

        statistics = new AuthorsAnalyzer().setModuleName("module1").getAllContributors(dataRepository);
        assertEquals(3, statistics.getAuthorSummaries().size());
        statistics = new AuthorsAnalyzer().setModuleName("moduleX").getAllContributors(dataRepository);
        assertEquals(0, statistics.getAuthorSummaries().size());
    }

    @Test
    public void getTeamsTest() throws Exception {
        String[][] links = {
                {"author1@email.org", "author_3", "10000", "Mon Nov 21 13:10:55 2016 EST", "comment_1", "module6/src/file1", "0.25"},
                {"author2@email.org", "author_1", "10001", "Mon Nov 21 13:11:55 2016 EST", "comment_2", "module5/src/file2", "0.25"},
                {"author3@email.org", "author_4", "10002", "Mon Nov 21 13:12:55 2016 EST", "comment_3", "module4/src/file3"},
                {"author4@email.org", "author_2", "10003", "Mon Nov 21 13:13:55 2016 EST", "comment_4", "module3/src/file4"}
        };
        DataRepository dataRepository = buildDataRepository(links);
        List<String> result = new AuthorsAnalyzer().getTeams(dataRepository.getAuthors().keySet());
        assertEquals(2, result.size());
        assertArrayEquals(new String[] {"team1", "team2"}, result.toArray(new String[0]));
    }

    @Test
    public void getAuthorsTest() throws Exception {
        String[][] links = {
                {"", "author_3", "10000", "Mon Nov 21 13:10:55 2016 EST", "comment_1", "module6/src/file1", "0.25"},
                {null, "author_1", "10001", "Mon Nov 21 13:11:55 2016 EST", "comment_2", "module5/src/file2", "0.25"},
                {"author3@email.org", "", "10002", "Mon Nov 21 13:12:55 2016 EST", "comment_3", "module4/src/file3"},
                {"author4@email.org", null, "10003", "Mon Nov 21 13:13:55 2016 EST", "comment_4", "module3/src/file4"},
                {"author5@email.org", "author_5", "10004", "Mon Nov 21 13:14:55 2016 EST", "comment_5", "module4/src/file5"},
                {"author6@email.org", "Author_5", "10005", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "module5/src/file6"}
        };
        DataRepository dataRepository = buildDataRepository(links);
        List<Author> result = new AuthorsAnalyzer().getAuthors(dataRepository);
        assertEquals(6, result.size());
        assertEquals("author3@email.org", result.get(0).getEmail());
        assertEquals("author4@email.org", result.get(1).getEmail());
        assertEquals("author_1", result.get(2).getName());
        assertEquals("author_3", result.get(3).getName());
        assertEquals("author5@email.org", result.get(4).getEmail());
        assertEquals("author6@email.org", result.get(5).getEmail());
    }

    @Test
    public void getContributionsTest() throws Exception {
        String[][] links = {
                {"author1@email.org", "author_1", "10000", "Mon Nov 21 13:10:55 2016 EST", "comment_1", "module6/src/file1", "0.25"},
                {"author2@email.org", "author_2", "10001", "Mon Nov 21 13:11:55 2016 EST", "comment_2", "module5/src/file2", "0.25"},
                {"author3@email.org", "author_3", "10002", "Mon Nov 21 13:12:55 2016 EST", "comment_3", "module4/src/file3"},
                {"author4@email.org", "author_4", "10003", "Mon Nov 21 13:13:55 2016 EST", "comment_4", "module3/src/file4"},
                {"author1@email.org", "author_5", "10004", "Mon Nov 21 13:14:55 2016 EST", "comment_5", "module2/src/file5", "0.3"},
                {"author2@email.org", "author_6", "10005", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "module1/src/file6", "0.3"}
        };
        DataRepository dataRepository = buildDataRepository(links);
        AuthorsAnalyzer authorsAnalyzer = new AuthorsAnalyzer();
        AuthorGroupStatistics statistics = authorsAnalyzer.getContributions(authorsAnalyzer.getAllContributors(dataRepository), as -> as.getAuthor().getTeam());
        assertEquals(2, statistics.getContributionMap().size());
        assertValueInNeighbourhood(0.55, statistics.getContributionMap().get("team1"));
        assertValueInNeighbourhood(1.55, statistics.getContributionMap().get("team2"));
        assertEquals(2, statistics.getCountsMap().size());
        assertEquals(2, statistics.getCountsMap().get("team1").intValue());
        assertEquals(3, statistics.getCountsMap().get("team2").intValue());
        assertValueInNeighbourhood(2.1, statistics.getSummaryContribution());
        assertValueInNeighbourhood(0.55/2, statistics.getRelativeContribution("team1"));
        assertValueInNeighbourhood(55/2.1, statistics.getContributionPercent("team1"));
        assertValueInNeighbourhood(1.55/3, statistics.getRelativeContribution("team2"));
        assertValueInNeighbourhood(155/2.1, statistics.getContributionPercent("team2"));

        statistics = authorsAnalyzer.getContributions(authorsAnalyzer.getAllContributors(dataRepository), as -> as.getAuthor().getLocation());
        assertEquals(3, statistics.getContributionMap().size());
        assertValueInNeighbourhood(0.55, statistics.getContributionMap().get("us"));
        assertValueInNeighbourhood(0.55, statistics.getContributionMap().get("ru"));
        assertValueInNeighbourhood(1.0, statistics.getContributionMap().get("de"));
        assertEquals(3, statistics.getCountsMap().size());
        assertEquals(2, statistics.getCountsMap().get("us").intValue());
        assertEquals(2, statistics.getCountsMap().get("ru").intValue());
        assertEquals(1, statistics.getCountsMap().get("de").intValue());
        assertValueInNeighbourhood(2.1, statistics.getSummaryContribution());
        assertValueInNeighbourhood(0.55/2, statistics.getRelativeContribution("us"));
        assertValueInNeighbourhood(55/2.1, statistics.getContributionPercent("us"));
        assertValueInNeighbourhood(0.55/2, statistics.getRelativeContribution("ru"));
        assertValueInNeighbourhood(55/2.1, statistics.getContributionPercent("ru"));
        assertValueInNeighbourhood(1.0/1, statistics.getRelativeContribution("de"));
        assertValueInNeighbourhood(100/2.1, statistics.getContributionPercent("de"));
    }
}
