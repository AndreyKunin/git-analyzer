package org.ak.gitanalyzer.http.processor;

import org.ak.gitanalyzer.step3.data.FileAuthors;

import java.text.NumberFormat;
import java.util.List;

/**
 * Created by Andrew on 16.10.2016.
 */
public class SharedFilesStatisticsProcessor extends BaseAnalysisProcessor {

    public SharedFilesStatisticsProcessor(NumberFormat nf) {
        super(nf);
    }

    public String getAxeX(List<FileAuthors> fileAuthorsList, int labelsCount) {
        List<FileAuthors> sample = fileAuthorsList.subList(0, labelsCount);
        sample.sort((f1, f2) -> f1.getFile().getPath().compareTo(f2.getFile().getPath()));
        return getJSONFor2D(sample, (fa, result) -> htmlWriter.appendChartLabel(result, fa.getFile().getFileName()));
    }

    public String getAxeY(List<FileAuthors> fileAuthorsList, int labelsCount) {
        List<FileAuthors> sample = fileAuthorsList.subList(0, labelsCount);
        sample.sort((f1, f2) -> f1.getFile().getPath().compareTo(f2.getFile().getPath()));
        return getJSONFor2D(sample, (fa, result) -> htmlWriter.appendChartEntry(result, fa.getAuthorsCount()));
    }

    public String getSharedFiles(List<FileAuthors> fileAuthorsList, String param) {
        return getJSONForTable(fileAuthorsList, (fa, result) -> {
            htmlWriter.appendString(result, "path", fa.getFile().getPath());
            htmlWriter.appendInteger(result, "c_count", fa.getAuthorsCount());
            htmlWriter.appendLink(result, "authors", "afsfs", "showAuthorsModal", param);
            htmlWriter.appendLink(result, "teams", "tfsfs", "showTeamsModal", true, param);
        });
    }

    public String getSharedFilesCSV(List<FileAuthors> fileAuthorsList) {
        return getCSVForReport(new String[] {"Path", "Count of authors"}, fileAuthorsList, (fa, result) -> {
            csvWriter.appendString(result, fa.getFile().getPath());
            csvWriter.appendInteger(result, fa.getAuthorsCount());
        });
    }
}
