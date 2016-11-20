package org.ak.gitanalyzer.step3.data;

import org.ak.gitanalyzer.step2.data.Author;

/**
 * Created by Andrew on 08.10.2016.
 */
public class AuthorSummary implements Comparable<AuthorSummary> {
    private Author author;
    private double contributionWeight;

    public AuthorSummary(Author author, double contributionWeight) {
        this.author = author;
        this.contributionWeight = contributionWeight;
    }

    public Author getAuthor() {
        return author;
    }

    public double getContributionWeight() {
        return contributionWeight;
    }

    @Override
    public int compareTo(AuthorSummary o) {
        return contributionWeight < o.contributionWeight ? 1 : contributionWeight > o.contributionWeight ? -1 : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthorSummary that = (AuthorSummary) o;

        return author.equals(that.author);

    }

    @Override
    public int hashCode() {
        return author.hashCode();
    }
}
