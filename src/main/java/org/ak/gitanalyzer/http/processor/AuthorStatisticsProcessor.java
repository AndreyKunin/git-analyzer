package org.ak.gitanalyzer.http.processor;

import org.ak.gitanalyzer.step2.data.Author;
import org.ak.gitanalyzer.step3.data.AuthorGroupStatistics;
import org.ak.gitanalyzer.step3.data.AuthorStatistics;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;

/**
 * Created by Andrew on 16.10.2016.
 */
public class AuthorStatisticsProcessor extends BaseAnalysisProcessor {

    public AuthorStatisticsProcessor(NumberFormat nf) {
        super(nf);
    }

    public String getTopContributors(AuthorStatistics authorStatistics) {
        return getJSONForTable(authorStatistics.getAuthorSummaries(), (as, result) -> {
            String name = as.getAuthor().getName();
            if (name != null) {
                name = name.replace("\\", "\\\\");
            }
            htmlWriter.appendString(result, "name", name);
            htmlWriter.appendString(result, "email", as.getAuthor().getEmail());
            htmlWriter.appendString(result, "team", as.getAuthor().getTeam());
            htmlWriter.appendDouble(result, "weight", as.getContributionWeight());
            htmlWriter.appendDouble(result, "weight_p", authorStatistics.getContributionPercent(as));
            htmlWriter.appendLink(result, "files", "fsas", "showFilesModal", "files", "true");
            htmlWriter.appendLink(result, "modules", "msas", "showFilesModal", true, "modules", "true");
        });
    }

    public String getTopContributorsCSV(AuthorStatistics authorStatistics) {
        return getCSVForReport(new String[] {"Name", "E-mail", "Team", "Count of changes", "Summary contribution, %"},
                authorStatistics.getAuthorSummaries(), (as, result) -> {
            csvWriter.appendString(result, as.getAuthor().getName());
            csvWriter.appendString(result, as.getAuthor().getEmail());
            csvWriter.appendString(result, as.getAuthor().getTeam());
            csvWriter.appendDouble(result, as.getContributionWeight());
            csvWriter.appendDouble(result, authorStatistics.getContributionPercent(as), true);
        });
    }

    public String getTopAuthors(AuthorStatistics authorStatistics) {
        return getJSONForTable(authorStatistics.getAuthorSummaries(), (as, result) -> {
            String name = as.getAuthor().getName();
            if (name != null) {
                name = name.replace("\\", "\\\\");
            }
            htmlWriter.appendString(result, "name", name);
            htmlWriter.appendString(result, "email", as.getAuthor().getEmail());
            htmlWriter.appendString(result, "team", as.getAuthor().getTeam());
            htmlWriter.appendDouble(result, "weight", as.getContributionWeight());
            htmlWriter.appendDouble(result, "weight_p", authorStatistics.getContributionPercent(as), true);
        });
    }

    public String getTopTeams(AuthorGroupStatistics authorGroupStatistics) {
        return getJSONForTable(authorGroupStatistics.getContributionMap().entrySet(), (entry, result) -> {
            htmlWriter.appendString(result, "team", entry.getKey());
            htmlWriter.appendDouble(result, "weight", entry.getValue());
            htmlWriter.appendDouble(result, "weight_p", authorGroupStatistics.getContributionPercent(entry.getKey()), true);
        });
    }

    public String getTopGroups(AuthorGroupStatistics contributions) {
        Collection<String> collection = contributions.enumerate((o1, o2) -> Double.compare(contributions.getContribution(o1), contributions.getContribution((o2))));
        return getJSONForTable(collection, (name, result) -> {
            htmlWriter.appendString(result, "name", name.replace("\\", "\\\\"));
            htmlWriter.appendDouble(result, "weight", contributions.getContribution(name));
            htmlWriter.appendDouble(result, "weight_p", contributions.getContributionPercent(name));
            htmlWriter.appendDouble(result, "weight_eff", contributions.getRelativeContribution(name));
            htmlWriter.appendLink(result, "files", "fsts", "showTeamFilesModal", "team_files", "files", "true");
            htmlWriter.appendLink(result, "modules", "msts", "showTeamFilesModal", true, "team_modules", "modules", "true");
        });
    }

    public String getTopGroupsCSV(AuthorGroupStatistics contributions) {
        Collection<String> collection = contributions.enumerate((o1, o2) -> Double.compare(contributions.getContribution(o1), contributions.getContribution((o2))));
        return getCSVForReport(new String[] {"Name", "Count of changes", "Summary contribution, %", "Changes per member"},
                collection, (name, result) -> {
            csvWriter.appendString(result, name);
            csvWriter.appendDouble(result, contributions.getContribution(name));
            csvWriter.appendDouble(result, contributions.getContributionPercent(name));
            csvWriter.appendDouble(result, contributions.getRelativeContribution(name), true);
        });
    }

    public String getTeamsList(List<String> teams) {
        return getJSONForTable(teams, (name, result) -> htmlWriter.appendString(result, "name", name == null ? null : name.replace("\\", "\\\\"), true));
    }

    public String getAuthorsList(List<Author> authors) {
        return getJSONForTable(authors, (author, result) -> {
            String name = author.getName();
            if (name != null) {
                name = name.replace("\\", "\\\\");
            }
            htmlWriter.appendString(result, "name", name);
            htmlWriter.appendString(result, "email", author.getEmail(), true);
        });
    }

    public String getGroupWeightsMapAbs(AuthorGroupStatistics contributions) {
        return getJSONFor2D(
                contributions.enumerate(String::compareTo),
                (key, result) -> htmlWriter.appendMapEntry(result, key, contributions.getContribution(key))
        );
    }

    public String getGroupWeightsMapNorm(AuthorGroupStatistics contributions) {
        return getJSONFor2D(
                contributions.enumerate(String::compareTo),
                (key, result) -> htmlWriter.appendMapEntry(result, key, contributions.getContributionPercent(key))
        );
    }

    public String getGroupWeightsMapEff(AuthorGroupStatistics contributions) {
        return getJSONFor2D(
                contributions.enumerate(String::compareTo),
                (key, result) -> htmlWriter.appendMapEntry(result, key, contributions.getRelativeContribution(key))
        );
    }

    public String getGroupWeightsChartAbs(AuthorGroupStatistics contributions) {
        return getJSONFor2D(
                contributions.enumerate(String::compareTo),
                (key, result) -> htmlWriter.appendChartEntry(result, contributions.getContribution(key))
        );
    }

    public String getGroupWeightsChartNorm(AuthorGroupStatistics contributions) {
        return getJSONFor2D(
                contributions.enumerate(String::compareTo),
                (key, result) -> htmlWriter.appendChartEntry(result, contributions.getContributionPercent(key))
        );
    }

    public String getGroupWeightsChartEff(AuthorGroupStatistics contributions) {
        return getJSONFor2D(
                contributions.enumerate(String::compareTo),
                (key, result) -> htmlWriter.appendChartEntry(result, contributions.getRelativeContribution(key))
        );
    }

    public String getGroupWeightsChartLabels(AuthorGroupStatistics contributions) {
        return getJSONFor2D(
                contributions.enumerate(String::compareTo),
                (key, result) -> htmlWriter.appendChartLabel(result, key)
        );
    }
}
