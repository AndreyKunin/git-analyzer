package org.ak.util.writer;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Andrew on 09.11.2016.
 */
public class HTMLWriter {

    private static AtomicInteger idCounter = new AtomicInteger(0);

    private NumberFormat nf;

    public HTMLWriter(NumberFormat nf) {
        this.nf = nf;
    }

    public void appendString(StringBuilder result, String name, String value) {
        appendString(result, name, value, false);
    }

    public void appendString(StringBuilder result, String name, String value, boolean last) {
        result.append("    \"").append(name).append("\": \"").append(value == null ? "" : value).append("\"").append(last ? "\n" : ",\n");
    }

    public void appendLink(StringBuilder result, String name, String idPrefix, String functionName, String ... parameters) {
        appendLink(result, name, idPrefix, functionName, false, parameters);
    }

    public void appendLink(StringBuilder result, String name, String idPrefix, String functionName, boolean last, String ... parameters) {
        int recordId = idCounter.getAndIncrement();
        result.append("    \"").append(name).append("\": \"").append("<a id=\\\"").append(idPrefix).append(recordId)
                .append("\\\" href=\\\"javascript:").append(functionName).append("('").append(idPrefix).append(recordId).append("'");
        if (parameters != null) {
            Arrays.stream(parameters).forEach(param -> result.append(", '").append(param).append("'"));
        }
        result.append(")\\\">Open</a>").append("\"").append(last ? "\n" : ",\n");
    }

    public void appendInteger(StringBuilder result, String name, int value) {
        appendInteger(result, name, value, false);
    }

    public void appendInteger(StringBuilder result, String name, int value, boolean last) {
        result.append("    \"").append(name).append("\": ").append(nf.format(value)).append(last ? "\n" : ",\n");
    }

    public void appendDouble(StringBuilder result, String name, double value) {
        appendDouble(result, name, value, false);
    }

    public void appendDouble(StringBuilder result, String name, double value, boolean last) {
        result.append("    \"").append(name).append("\": ").append(nf.format(value)).append(last ? "\n" : ",\n");
    }

    public void appendMapEntry(StringBuilder result, String key, double value) {
        result.append("\"").append(key).append("\":\"").append(nf.format(value)).append("\",");
    }

    public void appendChartEntry(StringBuilder result, double value) {
        result.append(nf.format(value)).append(",");
    }

    public void appendChartLabel(StringBuilder result, String label) {
        result.append("\"").append(truncate(label)).append("\",");
    }

    public void appendScatterPoint(StringBuilder result, long x, long y) {
        result.append("{").append("x:").append(x).append(",y:").append(y).append("},");
    }

    private String truncate(String content) {
        return content.length() > 21 ? content.substring(0, 9) + "..." + content.substring(content.length() - 9) : content;
    }
}
