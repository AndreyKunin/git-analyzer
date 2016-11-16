package org.ak.step3.data;

import org.ak.step2.data.DataRepository;
import org.ak.step2.data.File;
import org.ak.step2.data.Link;
import org.ak.util.Configuration;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Andrew on 31.10.2016.
 */
public class Graph {

    public static final int DANGER_PERCENT = 10;
    public static final int WARNING_PERCENT = 50;
    private final int REFACTORING_COMMITS_INDICATOR = Configuration.INSTANCE.getInt("GIT.refactoring.commits.min.size", 500);

    private List<Node> nodes = new ArrayList<>();
    private List<ActualDependency> edges = new ArrayList<>();
    private Limit nodeThreshold;
    private Limit edgeThreshold;
    private int nodeId = 0;

    private Graph() {
    }

    public Graph(DataRepository dataRepository, Predicate<Link> filter, int minWeight) {
        Map<File, Node> nodes = new HashMap<>();
        Map<ActualDependency, ActualDependency> edges = new HashMap<>();
        dataRepository.getCommits().values().stream().filter(list -> list.size() < REFACTORING_COMMITS_INDICATOR).forEach(list -> {
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

                    putEdge(edges, f1, f2, 1);
                    putNode(nodes, f1, 1);
                    putNode(nodes, f2, 1);
                }
            }
        });

        edges.keySet().forEach(edge -> {
            if (edge.getWeight() < minWeight) {
                removeNode(nodes, edge, edge.getNode1());
                removeNode(nodes, edge, edge.getNode2());
            } else {
                this.edges.add(edge);
            }
        });
        this.nodes.addAll(nodes.values());

        this.nodes.sort(Node::compareTo);
        this.edges.sort(ActualDependency::compareTo);

        nodeThreshold = findThreshold(this.nodes, Node::getWeight, DANGER_PERCENT, WARNING_PERCENT);
        edgeThreshold = findThreshold(this.edges, ActualDependency::getWeight, DANGER_PERCENT, WARNING_PERCENT);
    }

    public Graph extractModuleDependencies() {
        Graph moduleGraph = new Graph();
        Map<File, Node> moduleNodes = new HashMap<>();
        Map<ActualDependency, ActualDependency> moduleDependencies = new HashMap<>();
        edges.forEach(edge -> {
            File f1 = edge.getNode1();
            File f2 = edge.getNode2();
            File m1 = new File(f1.getModuleName());
            File m2 = new File(f2.getModuleName());

            putEdge(moduleDependencies, m1, m2, edge.getWeight());
            putNode(moduleNodes, m1, edge.getWeight());
            putNode(moduleNodes, m2, edge.getWeight());
        });

        moduleGraph.edges.addAll(moduleDependencies.values());
        moduleGraph.nodes.addAll(moduleNodes.values());

        moduleGraph.nodes.sort(Node::compareTo);
        moduleGraph.edges.sort(ActualDependency::compareTo);

        moduleGraph.nodeThreshold = findThreshold(moduleGraph.nodes, Node::getWeight, DANGER_PERCENT, WARNING_PERCENT);
        moduleGraph.edgeThreshold = findThreshold(moduleGraph.edges, ActualDependency::getWeight, DANGER_PERCENT, WARNING_PERCENT);

        return moduleGraph;
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

    private void removeNode(Map<File, Node> nodes, ActualDependency edge, File file) {
        Node node = nodes.get(file);
        node.addWeight(-edge.getWeight());
        if (node.getWeight() <= 0) {
            nodes.remove(file);
        }
    }

    private void putEdge(Map<ActualDependency, ActualDependency> edges, File f1, File f2, double weight) {
        ActualDependency actualDependency = new ActualDependency(f1, f2, weight);
        if (edges.containsKey(actualDependency)) {
            edges.get(actualDependency).addWeight(weight);
        } else {
            edges.put(actualDependency, actualDependency);
        }
    }

    private void putNode(Map<File, Node> nodes, File file, double weight) {
        if (!nodes.containsKey(file)) {
            nodes.put(file, new Node(nodeId++, file, weight));
        } else {
            nodes.get(file).addWeight(weight);
        }
    }

    private <T> Limit findThreshold(List<T> list, Function<T, Double> getWeight, int p1, int p2) {
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
