package org.ak.gitanalyzer.step3;

import org.ak.gitanalyzer.step2.data.DataRepository;
import org.ak.gitanalyzer.step2.data.Link;
import org.ak.gitanalyzer.step3.data.Forest;
import org.ak.gitanalyzer.step3.data.Graph;

/**
 * Created by Andrew on 06.10.2016.
 */
public class GraphAnalyzer extends Analyzer {

    public Forest getDependencies(DataRepository dataRepository, int minWeight) {
        return Graph.extractDependencies(dataRepository, this::filter, minWeight);
    }

    @Override
    protected boolean filter(Link link) {
        return super.filter(link) &&
                !link.getFile().isVCSFile();
    }

}
