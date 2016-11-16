package org.ak.step3;

import org.ak.step2.data.DataRepository;
import org.ak.step2.data.Link;
import org.ak.step3.data.Graph;

/**
 * Created by Andrew on 06.10.2016.
 */
public class GraphAnalyzer extends Analyzer {

    public Graph getUndesirableFileDependencies(DataRepository dataRepository, int minWeight) {
        return new Graph(dataRepository, this::filter, minWeight);
    }

    public Graph getUndesirableModuleDependencies(Graph graph) {
        return graph.extractModuleDependencies();
    }

    @Override
    protected boolean filter(Link link) {
        return super.filter(link) &&
                !link.getFile().isVCSFile();
    }

}
