package org.ak.step3.data;

import org.ak.step2.data.File;

/**
 * Created by Andrew on 09.10.2016.
 */
public class ActualDependency implements Comparable<ActualDependency> {
    private File node1;
    private File node2;
    private double weight;

    public ActualDependency(File node1, File node2, double weight) {
        int comparisonResult = node1.getPath().compareTo(node2.getPath());
        this.node1 = comparisonResult < 0 ? node1 : node2;
        this.node2 = comparisonResult < 0 ? node2 : node1;
        this.weight = weight;
    }

    public File getNode1() {
        return node1;
    }

    public File getNode2() {
        return node2;
    }

    public double getWeight() {
        return weight;
    }

    public void addWeight(double weight) {
        this.weight += weight;
    }

    @Override
    public int compareTo(ActualDependency o) {
        return weight > o.weight ? -1 : weight == o.weight ? 0 : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActualDependency that = (ActualDependency) o;

        if (!node1.equals(that.node1)) return false;
        if (!node2.equals(that.node2)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = node1.hashCode();
        result = 31 * result + node2.hashCode();
        return result;
    }
}
