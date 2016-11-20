package org.ak.gitanalyzer.step2.data;

/**
 * Created by Andrew on 01.10.2016.
 */
public class Link {
    private Author author;
    private Commit commit;
    private File file;

    //calculated values
    double weight = 1.0;  //If several commits were done for same JIRA by the same author this weight will be divided by the count of such commits.

    public Link(Author author, Commit commit, File file) {
        this.author = author;
        this.commit = commit;
        this.file = file;
    }

    public Author getAuthor() {
        return author;
    }

    public Commit getCommit() {
        return commit;
    }

    public File getFile() {
        return file;
    }

    void setAuthor(Author author) {
        this.author = author;
    }

    void setCommit(Commit commit) {
        this.commit = commit;
    }

    void setFile(File file) {
        this.file = file;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link = (Link) o;

        if (!author.equals(link.author)) return false;
        if (!commit.equals(link.commit)) return false;
        return file.equals(link.file);

    }

    @Override
    public int hashCode() {
        int result = author.hashCode();
        result = 31 * result + commit.hashCode();
        result = 31 * result + file.hashCode();
        return result;
    }
}
