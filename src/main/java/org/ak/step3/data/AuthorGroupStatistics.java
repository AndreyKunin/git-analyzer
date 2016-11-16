package org.ak.step3.data;

import java.util.*;

/**
 * Created by Andrew on 17.10.2016.
 */
public class AuthorGroupStatistics {
    private Map<String, Double> contributionMap;
    private Map<String, Integer> countsMap;
    private double summaryContribution;

    public AuthorGroupStatistics(Map<String, Double> contributionMap, Map<String, Integer> countsMap, double summaryContribution) {
        this.contributionMap = contributionMap;
        this.countsMap = countsMap;
        this.summaryContribution = summaryContribution;
    }

    public Map<String, Double> getContributionMap() {
        return contributionMap;
    }

    public Map<String, Integer> getCountsMap() {
        return countsMap;
    }

    public double getSummaryContribution() {
        return summaryContribution;
    }

    public Collection<String> enumerate(Comparator<String> comparator) {
        List<String> list = new ArrayList<>(contributionMap.keySet());
        if (comparator != null) {
            list.sort(comparator);
        }
        return list;
    }

    public double getContribution(String id) {
        return contributionMap.get(id);
    }

    public double getContributionPercent(String id) {
        return contributionMap.get(id) / summaryContribution * 100.0;
    }

    public double getRelativeContribution(String id) {
        return contributionMap.get(id) / (double) countsMap.get(id);
    }
}
