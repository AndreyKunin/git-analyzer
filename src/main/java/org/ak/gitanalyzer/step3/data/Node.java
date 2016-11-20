package org.ak.gitanalyzer.step3.data;

import org.ak.gitanalyzer.step2.data.File;

/**
 * Created by Andrew on 31.10.2016.
 */
public class Node implements Comparable<Node> {

    private File file;
    private double weight;
    private int id;

    public Node(int id, File file, double weight) {
        this.id = id;
        this.file = file;
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public File getFile() {
        return file;
    }

    public double getWeight() {
        return weight;
    }

    public void addWeight(double weight) {
        this.weight += weight;
    }

    @Override
    public int compareTo(Node o) {
        return weight > o.weight ? -1 : weight == o.weight ? 0 : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return file.equals(node.file);

    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
