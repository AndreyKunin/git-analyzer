package org.ak.gitanalyzer;

import org.ak.gitanalyzer.step2.data.DataRepository;
import org.ak.gitanalyzer.step3.GraphAnalyzer;
import org.ak.gitanalyzer.step3.data.Forest;
import org.ak.gitanalyzer.step3.data.Graph;
import org.ak.gitanalyzer.util.Configuration;
import org.junit.Test;

import static org.ak.gitanalyzer.util.TestHelper.assertValueInNeighbourhood;
import static org.ak.gitanalyzer.util.TestHelper.buildDataRepository;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Andrew on 26.11.2016.
 */
public class GraphAnalyzerTest extends AnalyserTestBase {

    @Test
    public void getUndesirableFileDependenciesTest() throws Exception {
        String[][] links = {
                {"email_1", "author_1", "10000", "Mon Nov 21 13:10:55 2016 EST", "comment_1", "M1/src/F1", "0.25"},
                {"email_1", "author_1", "10000", "Mon Nov 21 13:10:55 2016 EST", "comment_1", "M2/src/F4", "0.25"},

                {"email_2", "author_2", "10001", "Mon Nov 21 13:11:55 2016 EST", "comment_2", "M1/src/F1", "0.25"},
                {"email_2", "author_2", "10001", "Mon Nov 21 13:11:55 2016 EST", "comment_2", "M2/src/F4", "0.25"},

                {"email_1", "author_1", "10002", "Mon Nov 21 13:12:55 2016 EST", "comment_3", "M1/src/F1", "0.25"},
                {"email_1", "author_1", "10002", "Mon Nov 21 13:12:55 2016 EST", "comment_3", "M2/src/F4", "0.25"},

                {"email_2", "author_2", "10003", "Mon Nov 21 13:13:55 2016 EST", "comment_4", "M1/src/F2", "0.25"},
                {"email_2", "author_2", "10003", "Mon Nov 21 13:13:55 2016 EST", "comment_4", "M3/src/F5", "0.25"},

                {"email_1", "author_1", "10004", "Mon Nov 21 13:14:55 2016 EST", "comment_5", "M1/src/F2", "0.25"},
                {"email_1", "author_1", "10004", "Mon Nov 21 13:14:55 2016 EST", "comment_5", "M3/src/F5", "0.25"},

                {"email_2", "author_2", "10005", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "M1/src/F2", "0.25"},
                {"email_2", "author_2", "10005", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "M4/src/F8", "0.25"},

                {"email_1", "author_1", "10006", "Mon Nov 21 13:16:55 2016 EST", "comment_7", "M1/src/F3", "0.25"},
                {"email_1", "author_1", "10006", "Mon Nov 21 13:16:55 2016 EST", "comment_7", "M3/src/F6", "0.25"},

                {"email_2", "author_2", "10007", "Mon Nov 21 13:17:55 2016 EST", "comment_8", "M2/src/F7", "0.25"},
                {"email_2", "author_2", "10007", "Mon Nov 21 13:17:55 2016 EST", "comment_8", "M4/src/F8", "0.25"},

                {"email_1", "author_1", "10008", "Mon Nov 21 13:18:55 2016 EST", "comment_9", "M2/src/F7", "0.25"},
                {"email_1", "author_1", "10008", "Mon Nov 21 13:18:55 2016 EST", "comment_9", "M4/src/F8", "0.25"},

                {"email_2", "author_2", "10009", "Mon Nov 21 13:19:55 2016 EST", "comment_10", "M2/src/F7", "0.25"},
                {"email_2", "author_2", "10009", "Mon Nov 21 13:19:55 2016 EST", "comment_10", "M4/src/F8", "0.25"}
        };
        DataRepository dataRepository = buildDataRepository(links);

        Forest forest = new GraphAnalyzer().getDependencies(dataRepository, 3);

        Graph fileGraph = forest.getFileGraph();
        assertEquals(4, fileGraph.getNodes().size());
        assertTrue(fileGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M1/src/F1")).findFirst().isPresent());
        assertTrue(fileGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M2/src/F4")).findFirst().isPresent());
        assertTrue(fileGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M2/src/F7")).findFirst().isPresent());
        assertTrue(fileGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M4/src/F8")).findFirst().isPresent());
        fileGraph.getNodes().forEach(node -> assertValueInNeighbourhood(3.0, node.getWeight()));
        assertEquals(2, fileGraph.getEdges().size());
        assertTrue(fileGraph.getEdges().stream().filter(edge -> edge.getNode1().getPath().equals("M1/src/F1") && edge.getNode2().getPath().equals("M2/src/F4")).findFirst().isPresent());
        assertTrue(fileGraph.getEdges().stream().filter(edge -> edge.getNode1().getPath().equals("M2/src/F7") && edge.getNode2().getPath().equals("M4/src/F8")).findFirst().isPresent());
        fileGraph.getEdges().forEach(edge -> assertValueInNeighbourhood(3.0, edge.getWeight()));

        Graph moduleGraph = forest.getModuleGraph();
        assertEquals(4, moduleGraph.getNodes().size());
        assertTrue(moduleGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M1")).findFirst().isPresent());
        assertTrue(moduleGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M2")).findFirst().isPresent());
        assertTrue(moduleGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M3")).findFirst().isPresent());
        assertTrue(moduleGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M4")).findFirst().isPresent());
        moduleGraph.getNodes().forEach(node -> {
            switch (node.getFile().getPath()) {
                case "M1":
                    assertValueInNeighbourhood(6.0, node.getWeight());
                    break;
                case "M2":
                    assertValueInNeighbourhood(6.0, node.getWeight());
                    break;
                case "M3":
                    assertValueInNeighbourhood(3.0, node.getWeight());
                    break;
                case "M4":
                    assertValueInNeighbourhood(3.0, node.getWeight());
                    break;
            }
        });
        assertEquals(3, moduleGraph.getEdges().size());
        assertTrue(moduleGraph.getEdges().stream().filter(edge -> edge.getNode1().getPath().equals("M1") && edge.getNode2().getPath().equals("M2")).findFirst().isPresent());
        assertTrue(moduleGraph.getEdges().stream().filter(edge -> edge.getNode1().getPath().equals("M1") && edge.getNode2().getPath().equals("M3")).findFirst().isPresent());
        assertTrue(moduleGraph.getEdges().stream().filter(edge -> edge.getNode1().getPath().equals("M2") && edge.getNode2().getPath().equals("M4")).findFirst().isPresent());
        moduleGraph.getEdges().forEach(edge -> assertValueInNeighbourhood(3.0, edge.getWeight()));

        forest = new GraphAnalyzer().getDependencies(dataRepository, 2);

        fileGraph = forest.getFileGraph();
        assertEquals(6, fileGraph.getNodes().size());
        assertTrue(fileGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M1/src/F2")).findFirst().isPresent());
        assertTrue(fileGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M1/src/F1")).findFirst().isPresent());
        assertTrue(fileGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M2/src/F4")).findFirst().isPresent());
        assertTrue(fileGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M2/src/F7")).findFirst().isPresent());
        assertTrue(fileGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M3/src/F5")).findFirst().isPresent());
        assertTrue(fileGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M4/src/F8")).findFirst().isPresent());
        fileGraph.getNodes().forEach(node -> {
            switch (node.getFile().getPath()) {
                case "M1/src/F2":
                    assertValueInNeighbourhood(2.0, node.getWeight());
                    break;
                case "M3/src/F5":
                    assertValueInNeighbourhood(2.0, node.getWeight());
                    break;
                default:
                    assertValueInNeighbourhood(3.0, node.getWeight());
                    break;
            }
        });
        assertEquals(3, fileGraph.getEdges().size());
        assertTrue(fileGraph.getEdges().stream().filter(edge -> edge.getNode1().getPath().equals("M1/src/F2") && edge.getNode2().getPath().equals("M3/src/F5")).findFirst().isPresent());
        assertTrue(fileGraph.getEdges().stream().filter(edge -> edge.getNode1().getPath().equals("M1/src/F1") && edge.getNode2().getPath().equals("M2/src/F4")).findFirst().isPresent());
        assertTrue(fileGraph.getEdges().stream().filter(edge -> edge.getNode1().getPath().equals("M2/src/F7") && edge.getNode2().getPath().equals("M4/src/F8")).findFirst().isPresent());
        fileGraph.getEdges().forEach(edge -> {
            if (edge.getNode1().getPath().equals("M1/src/F2")) {
                assertValueInNeighbourhood(2.0, edge.getWeight());
            } else {
                assertValueInNeighbourhood(3.0, edge.getWeight());
            }
        });

        moduleGraph = forest.getModuleGraph();
        assertEquals(4, moduleGraph.getNodes().size());
        assertTrue(moduleGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M1")).findFirst().isPresent());
        assertTrue(moduleGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M2")).findFirst().isPresent());
        assertTrue(moduleGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M3")).findFirst().isPresent());
        assertTrue(moduleGraph.getNodes().stream().filter(node -> node.getFile().getPath().equals("M4")).findFirst().isPresent());
        moduleGraph.getNodes().forEach(node -> {
            switch (node.getFile().getPath()) {
                case "M1":
                    assertValueInNeighbourhood(6.0, node.getWeight());
                    break;
                case "M2":
                    assertValueInNeighbourhood(6.0, node.getWeight());
                    break;
                case "M3":
                    assertValueInNeighbourhood(3.0, node.getWeight());
                    break;
                case "M4":
                    assertValueInNeighbourhood(3.0, node.getWeight());
                    break;
            }
        });
        assertEquals(3, moduleGraph.getEdges().size());
        assertTrue(moduleGraph.getEdges().stream().filter(edge -> edge.getNode1().getPath().equals("M1") && edge.getNode2().getPath().equals("M2")).findFirst().isPresent());
        assertTrue(moduleGraph.getEdges().stream().filter(edge -> edge.getNode1().getPath().equals("M1") && edge.getNode2().getPath().equals("M3")).findFirst().isPresent());
        assertTrue(moduleGraph.getEdges().stream().filter(edge -> edge.getNode1().getPath().equals("M2") && edge.getNode2().getPath().equals("M4")).findFirst().isPresent());
        moduleGraph.getEdges().forEach(edge -> assertValueInNeighbourhood(3.0, edge.getWeight()));

        Configuration.INSTANCE.setString("GIT.refactoring.commits.min.size", "1");
        forest = new GraphAnalyzer().getDependencies(dataRepository, 2);

        fileGraph = forest.getFileGraph();
        assertEquals(0, fileGraph.getNodes().size());
        assertEquals(0, fileGraph.getEdges().size());

        moduleGraph = forest.getModuleGraph();
        assertEquals(0, moduleGraph.getNodes().size());
        assertEquals(0, moduleGraph.getEdges().size());
    }

    @Test
    public void vcsFilterTest() throws Exception {
        String[][] links = {
                {"email_1", "author_1", "10000", "Mon Nov 21 13:10:55 2016 EST", "comment_1", "M1/src/pom.xml", "0.25"},
                {"email_1", "author_1", "10000", "Mon Nov 21 13:10:55 2016 EST", "comment_1", "M2/src/pom.xml", "0.25"},

                {"email_2", "author_2", "10001", "Mon Nov 21 13:11:55 2016 EST", "comment_2", "M1/src/pom.xml", "0.25"},
                {"email_2", "author_2", "10001", "Mon Nov 21 13:11:55 2016 EST", "comment_2", "M2/src/pom.xml", "0.25"},

                {"email_1", "author_1", "10002", "Mon Nov 21 13:12:55 2016 EST", "comment_3", "M1/src/pom.xml", "0.25"},
                {"email_1", "author_1", "10002", "Mon Nov 21 13:12:55 2016 EST", "comment_3", "M2/src/pom.xml", "0.25"},

                {"email_2", "author_2", "10003", "Mon Nov 21 13:13:55 2016 EST", "comment_4", "M1/src/pom.xml", "0.25"},
                {"email_2", "author_2", "10003", "Mon Nov 21 13:13:55 2016 EST", "comment_4", "M3/src/pom.xml", "0.25"},

                {"email_1", "author_1", "10004", "Mon Nov 21 13:14:55 2016 EST", "comment_5", "M1/src/pom.xml", "0.25"},
                {"email_1", "author_1", "10004", "Mon Nov 21 13:14:55 2016 EST", "comment_5", "M3/src/pom.xml", "0.25"},

                {"email_2", "author_2", "10005", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "M1/src/pom.xml", "0.25"},
                {"email_2", "author_2", "10005", "Mon Nov 21 13:15:55 2016 EST", "comment_6", "M4/src/pom.xml", "0.25"},

                {"email_1", "author_1", "10006", "Mon Nov 21 13:16:55 2016 EST", "comment_7", "M1/src/pom.xml", "0.25"},
                {"email_1", "author_1", "10006", "Mon Nov 21 13:16:55 2016 EST", "comment_7", "M3/src/pom.xml", "0.25"},

                {"email_2", "author_2", "10007", "Mon Nov 21 13:17:55 2016 EST", "comment_8", "M2/src/pom.xml", "0.25"},
                {"email_2", "author_2", "10007", "Mon Nov 21 13:17:55 2016 EST", "comment_8", "M4/src/pom.xml", "0.25"},

                {"email_1", "author_1", "10008", "Mon Nov 21 13:18:55 2016 EST", "comment_9", "M2/src/pom.xml", "0.25"},
                {"email_1", "author_1", "10008", "Mon Nov 21 13:18:55 2016 EST", "comment_9", "M4/src/pom.xml", "0.25"},

                {"email_2", "author_2", "10009", "Mon Nov 21 13:19:55 2016 EST", "comment_10", "M2/src/pom.xml", "0.25"},
                {"email_2", "author_2", "10009", "Mon Nov 21 13:19:55 2016 EST", "comment_10", "M4/src/pom.xml", "0.25"}
        };
        DataRepository dataRepository = buildDataRepository(links);

        Forest forest = new GraphAnalyzer().getDependencies(dataRepository, 3);

        Graph fileGraph = forest.getFileGraph();
        assertEquals(0, fileGraph.getNodes().size());
        assertEquals(0, fileGraph.getEdges().size());

        Graph moduleGraph = forest.getModuleGraph();
        assertEquals(0, moduleGraph.getNodes().size());
        assertEquals(0, moduleGraph.getEdges().size());

    }
}
