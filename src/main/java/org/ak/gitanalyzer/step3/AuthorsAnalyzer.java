package org.ak.gitanalyzer.step3;

import org.ak.gitanalyzer.step2.data.Author;
import org.ak.gitanalyzer.step2.data.DataRepository;
import org.ak.gitanalyzer.step2.data.Link;
import org.ak.gitanalyzer.step3.data.AuthorGroupStatistics;
import org.ak.gitanalyzer.step3.data.AuthorStatistics;
import org.ak.gitanalyzer.step3.data.AuthorSummary;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Andrew on 05.10.2016.
 */
public class AuthorsAnalyzer extends Analyzer {

    private String path;
    private String moduleName;
    private Pattern fileMask;
    private int sampleSize = 0;

    public AuthorsAnalyzer setPath(String path) {
        this.path = path;
        return this;
    }

    public AuthorsAnalyzer setModuleName(String path) {
        this.moduleName = path;
        return this;
    }

    public AuthorsAnalyzer setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
        return this;
    }

    public AuthorsAnalyzer setFileMask(Pattern fileMask) {
        this.fileMask = fileMask;
        return this;
    }

    /**
     * Range authors by contribution.
     * @param dataRepository in-memory repository
     * @return AuthorStatistics
     */
    public AuthorStatistics getAllContributors(DataRepository dataRepository) {
        List<AuthorSummary> authorSummaries = dataRepository.getAuthors().entrySet().stream()
                .map(entry -> calculate(entry.getKey(), entry.getValue()))
                .filter(as -> as.getContributionWeight() > 0.0)
                .collect(Collectors.toList());
        if (authorSummaries.size() == 0) {
            return new AuthorStatistics(authorSummaries, 0.0);
        }

        authorSummaries.sort(AuthorSummary::compareTo);
        double summaryContribution = authorSummaries.stream().map(AuthorSummary::getContributionWeight).reduce((a1, a2) -> a1 + a2).orElse(0.0);
        if (sampleSize > 0 && sampleSize < authorSummaries.size()) {
            authorSummaries = authorSummaries.subList(0, sampleSize);
        }
        return new AuthorStatistics(authorSummaries, summaryContribution);
    }

    @Override
    protected boolean filter(Link link) {
        return super.filter(link) &&
                !(path != null && !link.getFile().getPath().equals(path)) &&
                !(fileMask != null && !fileMask.matcher(link.getFile().getPath()).matches()) &&
                !(moduleName != null && !link.getFile().getModuleName().equals(moduleName));
    }

    private AuthorSummary calculate(Author author, List<Link> links) {
        double summaryWeight = links.stream().filter(this::filter).map(Link::getWeight).reduce((x, y) -> x + y).orElse(0.0);
        return new AuthorSummary(author, summaryWeight);
    }

    public List<String> getTeams(Collection<Author> authors) {
        List<String> result = new ArrayList<>();
        authors.stream().map(Author::getTeam).filter(x -> x != null).distinct().forEach(result::add);
        result.sort(String::compareTo);
        return result;
    }

    public List<Author> getAuthors(DataRepository dataRepository) {
        List<Author> result = new ArrayList<>(dataRepository.getAuthors().keySet());
        return result.stream()
                .sorted(new Author.NameEmailComparator())
                .collect(Collectors.toList());
    }

    public AuthorGroupStatistics getContributions(AuthorStatistics authorStatistics, Function<AuthorSummary, String> valueGetter) {
        Map<String, Double> result = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>();
        authorStatistics.getAuthorSummaries().stream().filter(as -> valueGetter.apply(as) != null).forEach(as -> {
            String groupName = valueGetter.apply(as);
            if (groupName == null) {
                return;
            }
            Double value = result.get(groupName);
            Integer count = counts.get(groupName);
            if (value == null) {
                value = 0.0;
            }
            value += as.getContributionWeight();
            if (count == null) {
                count = 0;
            }
            count++;
            result.put(groupName, value);
            counts.put(groupName, count);
        });
        return new AuthorGroupStatistics(result, counts, result.values().stream().reduce((x1, x2) -> x1 + x2).orElse(0.0));
    }

}
