package org.ak.gitanalyzer.step3.data;

/**
 * Created by Andrew on 30.11.2016.
 */
public class Forest {
    private Graph fileGraph;
    private Graph moduleGraph;

    public Forest(Graph fileGraph, Graph moduleGraph) {
        this.fileGraph = fileGraph;
        this.moduleGraph = moduleGraph;
    }

    public Graph getFileGraph() {
        return fileGraph;
    }

    public Graph getModuleGraph() {
        return moduleGraph;
    }
}
