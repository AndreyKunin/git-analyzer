package org.ak.gitanalyzer.step3.data;

import java.util.List;

/**
 * Created by Andrew on 08.10.2016.
 */
public class AuthorStatistics {
    private List<AuthorSummary> authorSummaries;
    private double summaryContribution;

    public AuthorStatistics(List<AuthorSummary> authorSummaries, double summaryContribution) {
        this.authorSummaries = authorSummaries;
        this.summaryContribution = summaryContribution;
    }

    public List<AuthorSummary> getAuthorSummaries() {
        return authorSummaries;
    }

    public double getSummaryContribution() {
        return summaryContribution;
    }

    public double getContributionPercent(AuthorSummary as) {
        return as.getContributionWeight() / summaryContribution * 100.0;
    }
}
