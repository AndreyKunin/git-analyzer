package org.ak.step3;

import org.ak.step2.data.Author;
import org.ak.step2.data.DataRepository;
import org.ak.step2.data.File;
import org.ak.step2.data.Link;
import org.ak.step3.data.FileStatistics;
import org.ak.step3.data.FileSummary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Andrew on 03.10.2016.
 */
public class FileCommitsAnalyzer extends Analyzer {

    private Author author;
    private String team;

    public FileCommitsAnalyzer setAuthor(Author author) {
        this.author = author;
        return this;
    }

    public FileCommitsAnalyzer setTeam(String team) {
        this.team = team;
        return this;
    }

    /**
     * Range files by amount of changes.
     * @param dataRepository in-memory repository
     * @return FileStatistics
     */
    public FileStatistics getFileStatistics(DataRepository dataRepository) {
        List<FileSummary> fileSummaries = dataRepository.getFiles().entrySet().stream()
                .map(entry -> calculateFileSummary(entry.getKey(), entry.getValue()))
                .filter(fs -> fs.getFileWeight() > 0.0)
                .collect(Collectors.toList());
        return getFileStatistics(fileSummaries);
    }

    /**
     * Range modules by amount of changes.
     * @param fileStatistics statistics given by getFileStatistics()
     * @return new FileStatistics by modules
     */
    public FileStatistics getModuleStatistics(FileStatistics fileStatistics) {
        Map<String, Double> modules = new HashMap<>();
        fileStatistics.getFileSummaries().forEach(fileSummary -> {
            String name = fileSummary.getFile().getModuleName();
            Double value = modules.get(name);
            modules.put(name, value == null ? fileSummary.getFileWeight() : value + fileSummary.getFileWeight());
        });

        List<FileSummary> fileSummaries = modules.entrySet().stream()
                .map(entry -> new FileSummary(new File(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());

        return getFileStatistics(fileSummaries);
    }

    protected boolean filter(Link link) {
        return super.filter(link) &&
                !(author != null && !link.getAuthor().equals(author)) &&
                !(team != null && (link.getAuthor().getTeam() == null || !link.getAuthor().getTeam().equals(team)));
    }

    private FileStatistics getFileStatistics(List<FileSummary> fileSummaries) {
        //sort descending
        fileSummaries.sort(FileSummary::compareTo);
        double leftCost = fileSummaries.stream().map(FileSummary::getFileWeight).reduce((x1, x2) -> x1 + x2).orElse(0.0);
        return new FileStatistics(fileSummaries, leftCost);
    }

    private FileSummary calculateFileSummary(File file, List<Link> links) {
        double summaryWeight = links.stream()
                .filter(this::filter)
                .map(Link::getWeight)
                .reduce((x, y) -> x + y).orElse(0.0);
        return new FileSummary(file, summaryWeight);
    }

}
