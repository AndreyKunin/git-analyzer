package org.ak.http.processor;

import org.ak.step3.data.FileStatistics;
import org.ak.step3.data.FileSummary;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 14.10.2016.
 */
public class FileStatisticsProcessor extends BaseAnalysisProcessor {

    public FileStatisticsProcessor(NumberFormat nf) {
        super(nf);
    }

    public String getXAxe(FileStatistics fileStatistics, int labelsCount) {
        List<FileSummary> sample = getSample(fileStatistics.getFileSummaries(), labelsCount);
        return getJSONFor2D(sample, (summary, result) -> htmlWriter.appendChartLabel(result, summary.getFile().getFileName()));
    }

    public String getYAxe(FileStatistics fileStatistics, int labelsCount) {
        List<FileSummary> sample = getSample(fileStatistics.getFileSummaries(), labelsCount);
        return getJSONFor2D(sample, (summary, result) -> htmlWriter.appendChartEntry(result, summary.getFileWeight()));
    }

    public String getIdealAxe(FileStatistics fileStatistics, int labelsCount) {
        List<FileSummary> sample = getSample(fileStatistics.getFileSummaries(), labelsCount);
        double idealY = fileStatistics.getSummaryWeight() / fileStatistics.getFileSummaries().size();
        return getJSONFor2D(sample, (summary, result) -> htmlWriter.appendChartEntry(result, idealY));
    }

    public String getHotFiles(FileStatistics fileStatistics, String param) {
        return getJSONForTable(fileStatistics.getFileSummaries(), (fs, result) -> {
            htmlWriter.appendDouble(result, "weight", fs.getFileWeight());
            htmlWriter.appendDouble(result, "weight_p", fs.getFileWeight() / fileStatistics.getSummaryWeight() * 100.0);
            htmlWriter.appendString(result, "path", fs.getFile().getPath());
            htmlWriter.appendLink(result, "authors", "affs", "showAuthorsModal", param);
            htmlWriter.appendLink(result, "teams", "tffs", "showTeamsModal", true, param);
        });
    }

    public String getHotFilesCSV(FileStatistics fileStatistics) {
        return getCSVForReport(new String[] {"Count of changes", "Proportion of changes, %", "Path"}, fileStatistics.getFileSummaries(), (fs, result) -> {
            csvWriter.appendDouble(result, fs.getFileWeight());
            csvWriter.appendDouble(result, fs.getFileWeight() / fileStatistics.getSummaryWeight() * 100.0);
            csvWriter.appendString(result, fs.getFile().getPath());
        });
    }

    public String getTopFiles(FileStatistics fileStatistics) {
        return getJSONForTable(fileStatistics.getFileSummaries(), (fs, result) -> {
            htmlWriter.appendDouble(result, "weight", fs.getFileWeight());
            htmlWriter.appendDouble(result, "weight_p", fs.getFileWeight() / fileStatistics.getSummaryWeight() * 100.0);
            htmlWriter.appendString(result, "path", fs.getFile().getPath(), true);
        });
    }

    private   <T> List<T> getSample(List<T> list, int labelsCount) {
        int size = list.size();
        int granularity = size / (labelsCount - 1);
        if (granularity == 0) {
            return list;
        }
        List<T> sample = new ArrayList<>();
        if (list instanceof ArrayList) {
            for (int i = 0; i < size; i += granularity) {
                if (i + granularity >= size) {
                    sample.add(list.get(size - 1));
                } else {
                    sample.add(list.get(i));
                }
            }
        } else {
            int i = 0;
            for (T element : list) {
                if (i % granularity == 0 && i + granularity < size || i == size - 1) {
                    sample.add(element);
                }
                i++;
            }
        }
        return sample;
    }
}
