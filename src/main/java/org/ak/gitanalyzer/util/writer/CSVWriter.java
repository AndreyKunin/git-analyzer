package org.ak.gitanalyzer.util.writer;

import java.text.NumberFormat;
import java.util.stream.IntStream;

/**
 * Created by Andrew on 09.11.2016.
 */
public class CSVWriter {

    private NumberFormat nf;

    public CSVWriter(NumberFormat nf) {
        this.nf = nf;
    }

    public void appendString(StringBuilder result, String value) {
        appendString(result, value, false);
    }

    public void appendString(StringBuilder result, String value, boolean last) {
        if (value != null) {
            result.append("\"");
            int length = value.length();
            IntStream.range(0, length).forEach(i -> {
                char c = value.charAt(i);
                switch (c) {
                    case '\\':
                    case ',':
                    case '"':
                        result.append("\\");
                    default:
                        result.append(c);
                }
            });
            result.append("\"");
        }
        if (!last) {
            result.append(",");
        }
    }

    public void appendInteger(StringBuilder result, int value) {
        appendInteger(result, value, false);
    }

    public void appendInteger(StringBuilder result, int value, boolean last) {
        result.append(nf.format(value));
        if (!last) {
            result.append(",");
        }
    }

    public void appendDouble(StringBuilder result, double value) {
        appendDouble(result, value, false);
    }

    public void appendDouble(StringBuilder result, double value, boolean last) {
        result.append(nf.format(value));
        if (!last) {
            result.append(",");
        }
    }
}
