package org.ak.http.processor;

import org.ak.util.writer.CSVWriter;
import org.ak.util.writer.HTMLWriter;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by Andrew on 16.10.2016.
 */
abstract class BaseAnalysisProcessor {

    protected HTMLWriter htmlWriter;
    protected CSVWriter csvWriter;

    BaseAnalysisProcessor(NumberFormat nf) {
        this.htmlWriter = new HTMLWriter(nf);
        this.csvWriter = new CSVWriter(nf);
    }

    <T> String getCSVForReport(String[] headers, Collection<T> collection, BiConsumer<T, StringBuilder> consumer) {
        StringBuilder result = new StringBuilder();
        Arrays.stream(headers).forEach(header -> csvWriter.appendString(result, header));
        result.setCharAt(result.length() - 1, '\n');
        collection.forEach(as -> {
            consumer.accept(as, result);
            result.append('\n');
        });
        return result.toString();
    }

    <T> String getJSONForTable(Collection<T> collection, BiConsumer<T, StringBuilder> consumer) {
        StringBuilder result = new StringBuilder("[\n");
        collection.forEach(as -> {
            result.append("  {\n");
            consumer.accept(as, result);
            result.append("  },\n");
        });
        if (result.length() > 2) {
            result.deleteCharAt(result.lastIndexOf(","));
        }
        return result.append("]").toString();
    }

    <T> String getJSONFor2D(Collection<T> collection, BiConsumer<T, StringBuilder> consumer) {
        StringBuilder result = new StringBuilder();
        collection.forEach(key -> consumer.accept(key, result));
        if (result.length() > 0) {
            result.deleteCharAt(result.lastIndexOf(","));
        }
        return result.toString();
    }

    <T> String getJSONForGraph(Collection<T> collection, Function<T, Integer> nodeIdSupplier, TriConsumer<T, StringBuilder, Integer> consumer) {
        StringBuilder result = new StringBuilder();
        collection.forEach(entry -> {
            int recordId = nodeIdSupplier.apply(entry);
            result.append("{\n");
            consumer.accept(entry, result, recordId);
            result.append("},\n");
        });
        if (result.length() > 0) {
            result.deleteCharAt(result.lastIndexOf(","));
        }
        return result.toString();
    }

    String getTypeString(double weight, double thickThreshold, double normalThreshold) {
        return weight >= thickThreshold ? "thick" : weight >= normalThreshold ? "normal" : "thin";
    }
}
