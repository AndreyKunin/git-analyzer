package org.ak.gitanalyzer.http.processor;

import org.ak.gitanalyzer.step2.data.File;
import org.ak.gitanalyzer.step3.data.Graph;
import org.ak.gitanalyzer.step3.data.Node;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andrew on 24.10.2016.
 */
public class GraphProcessor extends BaseAnalysisProcessor {

    public GraphProcessor(NumberFormat nf) {
        super(nf);
    }

    public String getTableData(Graph dependencies) {
        return getJSONForTable(dependencies.getEdges(), (ad, result) -> {
            htmlWriter.appendDouble(result, "weight", ad.getWeight());
            htmlWriter.appendString(result, "path1", ad.getNode1().getPath());
            htmlWriter.appendString(result, "path2", ad.getNode2().getPath(), true);
        });
    }

    public String getCSVData(Graph dependencies) {
        return getCSVForReport(new String[] {"Count of connections", "Path 1", "Path 2"}, dependencies.getEdges(), (ad, result) -> {
            csvWriter.appendDouble(result, ad.getWeight());
            csvWriter.appendString(result, ad.getNode1().getPath());
            csvWriter.appendString(result, ad.getNode2().getPath(), true);
        });
    }

    public String getGraphData(Graph dependencies, String type) {
        if (type.equals("nodes")) {
            return getNodes(dependencies);
        }
        if (type.equals("edges")) {
            return getEdges(dependencies);
        }
        return null;
    }

    private String getNodes(Graph dependencies) {
        return getJSONForGraph(dependencies.getNodes(), Node::getId, (node, result, recordId) -> {
            File key = node.getFile();
            htmlWriter.appendInteger(result, "id", recordId);
            htmlWriter.appendString(result, "caption", key.getPath());
            htmlWriter.appendString(result, "type", getTypeString(node.getWeight(), dependencies.getNodeThreshold().getDanger(), dependencies.getNodeThreshold().getWarning()), true);
        });
    }

    private String getEdges(Graph dependencies) {
        Map<File, Integer> nodesMap = new HashMap<>();
        dependencies.getNodes().forEach(node -> nodesMap.put(node.getFile(), node.getId()));

        return getJSONForGraph(dependencies.getEdges(), d -> 0, (edge, result, recordId) -> {
            htmlWriter.appendInteger(result, "source", nodesMap.get(edge.getNode1()));
            htmlWriter.appendInteger(result, "target", nodesMap.get(edge.getNode2()));
            htmlWriter.appendDouble(result, "caption", edge.getWeight());
            htmlWriter.appendString(result, "type", getTypeString(edge.getWeight(), dependencies.getEdgeThreshold().getDanger(), dependencies.getEdgeThreshold().getWarning()), true);
        });
    }

}
