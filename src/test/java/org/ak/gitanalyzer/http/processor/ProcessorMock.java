package org.ak.gitanalyzer.http.processor;

import org.ak.gitanalyzer.util.writer.CSVWriter;
import org.ak.gitanalyzer.util.writer.HTMLWriter;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by Andrew on 02.12.2016.
 */
public class ProcessorMock extends BaseAnalysisProcessor {

    public ProcessorMock(NumberFormat nf) {
        super(nf);
    }

    @Override
    public <T> String getCSVForReport(String[] headers, Collection<T> collection, BiConsumer<T, StringBuilder> consumer) {
        return super.getCSVForReport(headers, collection, consumer);
    }

    @Override
    public <T> String getJSONForTable(Collection<T> collection, BiConsumer<T, StringBuilder> consumer) {
        return super.getJSONForTable(collection, consumer);
    }

    @Override
    public <T> String getJSONFor2D(Collection<T> collection, BiConsumer<T, StringBuilder> consumer) {
        return super.getJSONFor2D(collection, consumer);
    }

    @Override
    public <T> String getJSONForGraph(Collection<T> collection, Function<T, Integer> nodeIdSupplier, TriConsumer<T, StringBuilder, Integer> consumer) {
        return super.getJSONForGraph(collection, nodeIdSupplier, consumer);
    }

    @Override
    public String getTypeString(double weight, double thickThreshold, double normalThreshold) {
        return super.getTypeString(weight, thickThreshold, normalThreshold);
    }


    public HTMLWriter getHtmlWriter() {
        return htmlWriter;
    }

    public CSVWriter getCsvWriter() {
        return csvWriter;
    }
}
