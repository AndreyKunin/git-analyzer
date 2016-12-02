package org.ak.gitanalyzer.step3.data;

import org.ak.gitanalyzer.step2.data.DataRepository;
import org.ak.gitanalyzer.step2.data.File;
import org.ak.gitanalyzer.step2.data.Link;
import org.ak.gitanalyzer.util.Configuration;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Andrew on 31.10.2016.
 */
public class Graph {

    public static final int DANGER_PERCENT = 10;
    public static final int WARNING_PERCENT = 50;

    private List<Node> nodes = new ArrayList<>();
    private List<ActualDependency> edges = new ArrayList<>();
    private Limit nodeThreshold;
    private Limit edgeThreshold;
    private static int nodeId = 0;

    private Graph() {
    }

    public static Forest extractDependencies(DataRepository dataRepository, Predicate<Link> filter, int minWeight) {
        final int refactoringCommitsIndicator = Configuration.INSTANCE.getInt("GIT.refactoring.commits.min.size", 500);
        Graph fileGraph = new Graph();
        Graph moduleGraph = new Graph();

        Map<File, Node> fileNodes = new HashMap<>();
        Map<File, Node> moduleNodes = new HashMap<>();

        Map<ActualDependency, ActualDependency> fileEdges = new HashMap<>();
        Map<ActualDependency, ActualDependency> moduleEdges = new HashMap<>();

        dataRepository.getCommits().values().stream().filter(list -> list.size() < refactoringCommitsIndicator).forEach(list -> {
            for (int i = 0; i < list.size() - 1; ++i) {
                Link l1 = list.get(i);
                if (!filter.test(l1)) {
                    continue;
                }
                File f1 = l1.getFile();

                for (int j = i + 1; j < list.size(); ++j) {
                    Link l2 = list.get(j);
                    if (!filter.test(l2)) {
                        continue;
                    }
                    File f2 = l2.getFile();
                    if (f1.getModuleName().equals(f2.getModuleName())) {
                        continue;
                    }

                    putEdge(fileEdges, f1, f2, 1);
                    putNode(fileNodes, f1, 1);
                    putNode(fileNodes, f2, 1);
                }
            }
        });

        fileEdges.keySet().forEach(edge -> {
            File f1 = edge.getNode1();
            File f2 = edge.getNode2();
            File m1 = new File(f1.getModuleName());
            File m2 = new File(f2.getModuleName());

            putEdge(moduleEdges, m1, m2, edge.getWeight());
            putNode(moduleNodes, m1, edge.getWeight());
            putNode(moduleNodes, m2, edge.getWeight());

            filterEdge(minWeight, fileGraph, fileNodes, edge);
        });

        moduleEdges.keySet().forEach(edge -> filterEdge(minWeight, moduleGraph, moduleNodes, edge));

        addNodes(fileGraph, fileNodes);
        addNodes(moduleGraph, moduleNodes);

        return new Forest(fileGraph, moduleGraph);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<ActualDependency> getEdges() {
        return edges;
    }

    public Limit getNodeThreshold() {
        return nodeThreshold;
    }

    public Limit getEdgeThreshold() {
        return edgeThreshold;
    }

    private static void addNodes(Graph graph, Map<File, Node> fileNodes) {
        graph.nodes.addAll(fileNodes.values());

        graph.nodes.sort(Node::compareTo);
        graph.edges.sort(ActualDependency::compareTo);

        graph.nodeThreshold = findThreshold(graph.nodes, Node::getWeight, DANGER_PERCENT, WARNING_PERCENT);
        graph.edgeThreshold = findThreshold(graph.edges, ActualDependency::getWeight, DANGER_PERCENT, WARNING_PERCENT);
    }

    private static void filterEdge(int minWeight, Graph graph, Map<File, Node> nodeMap, ActualDependency edge) {
        if (edge.getWeight() < minWeight) {
            removeNode(nodeMap, edge, edge.getNode1());
            removeNode(nodeMap, edge, edge.getNode2());
        } else {
            graph.edges.add(edge);
        }
    }

    private static void removeNode(Map<File, Node> nodes, ActualDependency edge, File file) {
        Node node = nodes.get(file);
        node.addWeight(-edge.getWeight());
        if (node.getWeight() <= 0) {
            nodes.remove(file);
        }
    }

    private static void putEdge(Map<ActualDependency, ActualDependency> edges, File f1, File f2, double weight) {
        ActualDependency actualDependency = new ActualDependency(f1, f2, weight);
        if (edges.containsKey(actualDependency)) {
            edges.get(actualDependency).addWeight(weight);
        } else {
            edges.put(actualDependency, actualDependency);
        }
    }

    private static void putNode(Map<File, Node> nodes, File file, double weight) {
        if (!nodes.containsKey(file)) {
            nodes.put(file, new Node(nodeId++, file, weight));
        } else {
            nodes.get(file).addWeight(weight);
        }
    }

    private static <T> Limit findThreshold(List<T> list, Function<T, Double> getWeight, int p1, int p2) {
        int size = list.size();
        if (size == 0) {
            return new Limit(0, 0);
        }
        return new Limit(
                getWeight.apply(list.get(size * p1 < 100 ? 0 : size * p1 / 100 - 1)),
                getWeight.apply(list.get(size * p2 < 100 ? 0 : size * p2 / 100 - 1))
        );
    }

    public static class Limit {
        double danger;
        double warning;

        Limit(double danger, double warning) {
            this.danger = danger;
            this.warning = warning;
        }

        public double getDanger() {
            return danger;
        }

        public double getWarning() {
            return warning;
        }
    }
}
