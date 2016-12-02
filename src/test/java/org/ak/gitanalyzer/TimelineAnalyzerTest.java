package org.ak.gitanalyzer;

import org.ak.gitanalyzer.step2.data.Author;
import org.ak.gitanalyzer.step2.data.DataRepository;
import org.ak.gitanalyzer.step3.TimelineAnalyzer;
import org.ak.gitanalyzer.step3.data.ActivitySummary;
import org.ak.gitanalyzer.step3.data.AgeStatistics;
import org.ak.gitanalyzer.step3.data.DateMarker;
import org.junit.Test;

import java.util.List;

import static org.ak.gitanalyzer.util.TestHelper.assertValueInNeighbourhood;
import static org.ak.gitanalyzer.util.TestHelper.buildDataRepository;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by Andrew on 26.11.2016.
 */
public class TimelineAnalyzerTest extends AnalyserTestBase {

    @Test
    public void analyzeFilesAgeTest() throws Exception {
        String[][] links = {
                {"email_1", "author_1", "10000", "Sun Nov 20 13:15:55 2016 EST", "comment_1", "module1/src/file1", "0.25"},
                {"email_1", "author_1", "10001", "Mon Nov 21 13:15:55 2016 EST", "comment_2", "module1/src/file1", "0.25"},
                {"email_1", "author_1", "10002", "Tue Nov 22 13:15:55 2016 EST", "comment_3", "module1/src/file1"},
                {"email_1", "author_1", "10004", "Sun Nov 20 13:15:55 2016 EST", "comment_5", "module1/src/file2", "0.3"},
                {"email_1", "author_1", "10001", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "module1/src/file2", "0.3"},
                {"email_1", "author_1", "10006", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "module1/src/file3", "0.3"}
        };
        DataRepository dataRepository = buildDataRepository(links);
        List<AgeStatistics> filesAge = new TimelineAnalyzer().analyzeFilesAge(dataRepository);
        assertEquals(3, filesAge.size());
        long nowMinusNov21 = filesAge.stream().filter(fs -> fs.getFile().getFileName().equals("file3")).map(AgeStatistics::getNowMinusLast).findFirst().orElse(0L);
        long nowMinusNov22 = nowMinusNov21 - AgeStatistics.MS_IN_DAY;
        long nowMinusNov20 = nowMinusNov21 + AgeStatistics.MS_IN_DAY;
        filesAge.forEach(fs -> {
            if (fs.getFile().getFileName().equals("file1")) {
                assertEquals(nowMinusNov20, fs.getAge());
                assertEquals(nowMinusNov22, fs.getNowMinusLast());
                assertEquals(AgeStatistics.MS_IN_DAY * 2, fs.getLastMinusFirst());
                assertValueInNeighbourhood((nowMinusNov22) / (double) (AgeStatistics.MS_IN_DAY * 2 + (nowMinusNov22)) * 100.0, fs.getStabilityPercent());
                assertEquals("2 days", fs.intervalToString(fs.getLastMinusFirst()));
            } else if (fs.getFile().getFileName().equals("file2")) {
                assertEquals(nowMinusNov20, fs.getAge());
                assertEquals(nowMinusNov21, fs.getNowMinusLast());
                assertEquals(AgeStatistics.MS_IN_DAY, fs.getLastMinusFirst());
                assertValueInNeighbourhood(nowMinusNov21 / (double) (AgeStatistics.MS_IN_DAY + nowMinusNov21) * 100.0, fs.getStabilityPercent());
                assertEquals("1 day", fs.intervalToString(fs.getLastMinusFirst()));
            } else if (fs.getFile().getFileName().equals("file3")) {
                assertEquals(nowMinusNov21, fs.getAge());
                assertEquals(nowMinusNov21, fs.getNowMinusLast());
                assertEquals(0, fs.getLastMinusFirst());
                assertValueInNeighbourhood(nowMinusNov21 / (double) nowMinusNov21 * 100.0, fs.getStabilityPercent());
                assertEquals("Less than a minute", fs.intervalToString(fs.getLastMinusFirst()));
            } else {
                fail("Unknown file: " + fs.getFile().getFileName());
            }
        });
    }

    @Test
    public void analyzeModulesAgeTest() throws Exception {
        String[][] links = {
                {"email_1", "author_1", "10000", "Sun Nov 20 13:15:55 2016 EST", "comment_1", "module1/src/file1", "0.25"},
                {"email_1", "author_1", "10001", "Mon Nov 21 13:15:55 2016 EST", "comment_2", "module1/src/file5", "0.25"},
                {"email_1", "author_1", "10002", "Tue Nov 22 13:15:55 2016 EST", "comment_3", "module1/src/file4"},
                {"email_1", "author_1", "10004", "Sun Nov 20 13:15:55 2016 EST", "comment_5", "module2/src/file2", "0.3"},
                {"email_1", "author_1", "10004", "Sun Nov 20 13:15:55 2016 EST", "comment_5", "module2/src/file6", "0.3"},
                {"email_1", "author_1", "10001", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "module2/src/file6", "0.3"},
                {"email_1", "author_1", "10006", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "module3/src/file3", "0.3"}
        };
        DataRepository dataRepository = buildDataRepository(links);
        List<AgeStatistics> modulesAge = new TimelineAnalyzer().analyzeModulesAge(dataRepository);
        assertEquals(3, modulesAge.size());
        long nowMinusNov21 = modulesAge.stream().filter(fs -> fs.getFile().getPath().equals("module3")).map(AgeStatistics::getNowMinusLast).findFirst().orElse(0L);
        long nowMinusNov22 = nowMinusNov21 - AgeStatistics.MS_IN_DAY;
        long nowMinusNov20 = nowMinusNov21 + AgeStatistics.MS_IN_DAY;
        modulesAge.forEach(fs -> {
            if (fs.getFile().getPath().equals("module1")) {
                assertEquals(nowMinusNov20, fs.getAge());
                assertEquals(nowMinusNov22, fs.getNowMinusLast());
                assertEquals(AgeStatistics.MS_IN_DAY * 2, fs.getLastMinusFirst());
                assertValueInNeighbourhood((nowMinusNov22) / (double) (AgeStatistics.MS_IN_DAY * 2 + (nowMinusNov22)) * 100.0, fs.getStabilityPercent());
                assertEquals("2 days", fs.intervalToString(fs.getLastMinusFirst()));
            } else if (fs.getFile().getPath().equals("module2")) {
                assertEquals(nowMinusNov20, fs.getAge());
                assertEquals(nowMinusNov21, fs.getNowMinusLast());
                assertEquals(AgeStatistics.MS_IN_DAY, fs.getLastMinusFirst());
                assertValueInNeighbourhood(nowMinusNov21 / (double) (AgeStatistics.MS_IN_DAY + nowMinusNov21) * 100.0, fs.getStabilityPercent());
                assertEquals("1 day", fs.intervalToString(fs.getLastMinusFirst()));
            } else if (fs.getFile().getFileName().equals("module3")) {
                assertEquals(nowMinusNov21, fs.getAge());
                assertEquals(nowMinusNov21, fs.getNowMinusLast());
                assertEquals(0, fs.getLastMinusFirst());
                assertValueInNeighbourhood(nowMinusNov21 / (double) nowMinusNov21 * 100.0, fs.getStabilityPercent());
                assertEquals("Less than a minute", fs.intervalToString(fs.getLastMinusFirst()));
            } else {
                fail("Unknown module: " + fs.getFile().getFileName());
            }
        });
    }

    @Test
    public void analyzeAuthorsActivityTest() throws Exception {
        String[][] links = {
                {"author1@email.org", "author_1", "10000", "Sat Aug 20 13:15:55 2016 EST", "comment_1", "module1/src/file1", "0.35"},
                {"author1@email.org", "author_1", "10001", "Tue Sep 20 13:15:55 2016 EST", "comment_2", "module1/src/file5", "0.35"},
                {"author1@email.org", "author_1", "10002", "Thu Oct 20 13:15:55 2016 EST", "comment_3", "module1/src/file4", "0.1"},
                {"author2@email.org", "author_2", "10003", "Sat Aug 20 13:15:55 2016 EST", "comment_5", "module2/src/file2", "0.3"},
                {"author2@email.org", "author_2", "10004", "Tue Sep 20 13:15:55 2016 EST", "comment_6", "module2/src/file6", "0.3"},
                {"author3@email.org", "author_3", "10006", "Tue Sep 20 13:15:55 2016 EST", "comment_7", "module3/src/file3", "1.0"}
        };
        DataRepository dataRepository = buildDataRepository(links);

        List<ActivitySummary<Author>> activity = new TimelineAnalyzer().analyzeAuthorsActivity(dataRepository);
        assertEquals(3, activity.size());
        activity.forEach(as -> {
            List<DateMarker> dateMarkers;
            switch (as.getEntity().getName()) {
                case "author_1":
                    dateMarkers = as.getDateMarkers();
                    assertEquals(3, dateMarkers.size());
                    assertEquals(2016, dateMarkers.get(0).getYear());
                    assertEquals(7, dateMarkers.get(0).getMonth());
                    assertValueInNeighbourhood(0.35, as.get(dateMarkers.get(0)));
                    assertEquals(2016, dateMarkers.get(1).getYear());
                    assertEquals(8, dateMarkers.get(1).getMonth());
                    assertValueInNeighbourhood(0.35, as.get(dateMarkers.get(1)));
                    assertEquals(2016, dateMarkers.get(2).getYear());
                    assertEquals(9, dateMarkers.get(2).getMonth());
                    assertValueInNeighbourhood(0.1, as.get(dateMarkers.get(2)));
                    break;
                case "author_2":
                    dateMarkers = as.getDateMarkers();
                    assertEquals(2, as.getDateMarkers().size());
                    assertEquals(2016, dateMarkers.get(0).getYear());
                    assertEquals(7, dateMarkers.get(0).getMonth());
                    assertValueInNeighbourhood(0.3, as.get(dateMarkers.get(0)));
                    assertEquals(2016, dateMarkers.get(1).getYear());
                    assertEquals(8, dateMarkers.get(1).getMonth());
                    assertValueInNeighbourhood(0.3, as.get(dateMarkers.get(1)));
                    break;
                case "author_3":
                    dateMarkers = as.getDateMarkers();
                    assertEquals(3, as.getDateMarkers().size());
                    assertEquals(2016, dateMarkers.get(0).getYear());
                    assertEquals(7, dateMarkers.get(0).getMonth());
                    assertNull(as.get(dateMarkers.get(0)));
                    assertEquals(2016, dateMarkers.get(1).getYear());
                    assertEquals(8, dateMarkers.get(1).getMonth());
                    assertValueInNeighbourhood(1.0, as.get(dateMarkers.get(1)));
                    assertEquals(2016, dateMarkers.get(2).getYear());
                    assertEquals(9, dateMarkers.get(2).getMonth());
                    assertNull(as.get(dateMarkers.get(2)));
                    break;
            }
        });


    }

    @Test
    public void analyzeTeamsActivityTest() throws Exception {
        String[][] links = {
                {"author1@email.org", "author_1", "10000", "Sat Aug 20 13:15:55 2016 EST", "comment_1", "module1/src/file1", "0.35"},
                {"author1@email.org", "author_1", "10001", "Tue Sep 20 13:15:55 2016 EST", "comment_2", "module1/src/file5", "0.35"},
                {"author1@email.org", "author_1", "10002", "Thu Oct 20 13:15:55 2016 EST", "comment_3", "module1/src/file4", "0.1"},
                {"author2@email.org", "author_2", "10003", "Sat Aug 20 13:15:55 2016 EST", "comment_5", "module2/src/file2", "0.3"},
                {"author2@email.org", "author_2", "10004", "Tue Sep 20 13:15:55 2016 EST", "comment_6", "module2/src/file6", "0.3"},
                {"author3@email.org", "author_3", "10006", "Tue Sep 20 13:15:55 2016 EST", "comment_7", "module3/src/file3", "1.0"}
        };
        DataRepository dataRepository = buildDataRepository(links);

        List<ActivitySummary<Author>> authorsActivity = new TimelineAnalyzer().analyzeAuthorsActivity(dataRepository);
        List<ActivitySummary<String>> activity = new TimelineAnalyzer().analyzeTeamsActivity(authorsActivity);

        assertEquals(2, activity.size());
        activity.forEach(as -> {
            List<DateMarker> dateMarkers;
            switch (as.getEntity()) {
                case "team1":
                    dateMarkers = as.getDateMarkers();
                    assertEquals(3, dateMarkers.size());
                    assertEquals(2016, dateMarkers.get(0).getYear());
                    assertEquals(7, dateMarkers.get(0).getMonth());
                    assertValueInNeighbourhood(0.35, as.get(dateMarkers.get(0)));
                    assertEquals(2016, dateMarkers.get(1).getYear());
                    assertEquals(8, dateMarkers.get(1).getMonth());
                    assertValueInNeighbourhood(0.35, as.get(dateMarkers.get(1)));
                    assertEquals(2016, dateMarkers.get(2).getYear());
                    assertEquals(9, dateMarkers.get(2).getMonth());
                    assertValueInNeighbourhood(0.1, as.get(dateMarkers.get(2)));
                    break;
                case "team2":
                    dateMarkers = as.getDateMarkers();
                    assertEquals(2, as.getDateMarkers().size());
                    assertEquals(2016, dateMarkers.get(0).getYear());
                    assertEquals(7, dateMarkers.get(0).getMonth());
                    assertValueInNeighbourhood(0.3, as.get(dateMarkers.get(0)));
                    assertEquals(2016, dateMarkers.get(1).getYear());
                    assertEquals(8, dateMarkers.get(1).getMonth());
                    assertValueInNeighbourhood(1.3, as.get(dateMarkers.get(1)));
                    break;
            }
        });

    }

    @Test
    public void analyzeProjectActivityTest() throws Exception {
        String[][] links = {
                {"author1@email.org", "author_1", "10000", "Sat Aug 20 13:15:55 2016 EST", "comment_1", "module1/src/file1", "0.35"},
                {"author1@email.org", "author_1", "10001", "Tue Sep 20 13:15:55 2016 EST", "comment_2", "module1/src/file5", "0.35"},
                {"author1@email.org", "author_1", "10002", "Thu Oct 20 13:15:55 2016 EST", "comment_3", "module1/src/file4", "0.1"},
                {"author2@email.org", "author_2", "10003", "Sat Aug 20 13:15:55 2016 EST", "comment_5", "module2/src/file2", "0.3"},
                {"author2@email.org", "author_2", "10004", "Tue Sep 20 13:15:55 2016 EST", "comment_6", "module2/src/file6", "0.3"},
                {"author3@email.org", "author_3", "10006", "Tue Sep 20 13:15:55 2016 EST", "comment_7", "module3/src/file3", "1.0"}
        };
        DataRepository dataRepository = buildDataRepository(links);

        List<ActivitySummary<Author>> authorsActivity = new TimelineAnalyzer().analyzeAuthorsActivity(dataRepository);
        ActivitySummary<String> as = new TimelineAnalyzer().analyzeProjectActivity(authorsActivity);
        assertEquals("", as.getEntity());
        List<DateMarker> dateMarkers = as.getDateMarkers();
        assertEquals(3, dateMarkers.size());
        assertEquals(2016, dateMarkers.get(0).getYear());
        assertEquals(7, dateMarkers.get(0).getMonth());
        assertValueInNeighbourhood(0.65, as.get(dateMarkers.get(0)));
        assertEquals(2016, dateMarkers.get(1).getYear());
        assertEquals(8, dateMarkers.get(1).getMonth());
        assertValueInNeighbourhood(1.65, as.get(dateMarkers.get(1)));
        assertEquals(2016, dateMarkers.get(2).getYear());
        assertEquals(9, dateMarkers.get(2).getMonth());
        assertValueInNeighbourhood(0.1, as.get(dateMarkers.get(2)));
    }
}
