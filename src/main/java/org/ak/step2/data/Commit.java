package org.ak.step2.data;

import java.util.Date;

/**
 * Created by Andrew on 01.10.2016.
 */
public class Commit {

    final String hash;
    final Date dateTime;
    final String comment;

    public Commit(String hash, Date dateTime, String comment) {
        this.hash = hash;
        this.dateTime = dateTime;
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public String getHash() {
        return hash;
    }

    public Date getDateTime() {
        return dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Commit commit = (Commit) o;

        return hash.equals(commit.hash);

    }

    @Override
    public int hashCode() {
        return hash.hashCode();
    }
}
