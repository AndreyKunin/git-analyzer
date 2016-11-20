package org.ak.gitanalyzer.step3.data;

import java.util.List;

/**
 * Created by Andrew on 08.10.2016.
 */
public class FileStatistics {
    private List<FileSummary> fileSummaries;
    private double summaryWeight;

    public FileStatistics(List<FileSummary> fileSummaries, double summaryWeight) {
        this.fileSummaries = fileSummaries;
        this.summaryWeight = summaryWeight;
    }

    public List<FileSummary> getFileSummaries() {
        return fileSummaries;
    }

    public double getSummaryWeight() {
        return summaryWeight;
    }
}
