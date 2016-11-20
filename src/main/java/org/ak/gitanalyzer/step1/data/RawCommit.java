package org.ak.gitanalyzer.step1.data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Andrew on 01.10.2016.
 */
public class RawCommit implements Serializable {

    private final String hash;
    private final Date commitDateTime;
    private final String authorName;
    private final String authorEmail;
    private final String comment;

    public RawCommit(String hash, Date commitDateTime, String authorName, String authorEmail, String comment) {
        this.hash = hash;
        this.commitDateTime = commitDateTime;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.comment = comment;
    }

    public String getHash() {
        return hash;
    }

    public Date getCommitDateTime() {
        return commitDateTime;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public String getComment() {
        return comment;
    }
}
