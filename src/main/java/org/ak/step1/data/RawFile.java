package org.ak.step1.data;

import org.ak.step1.git.SubprocessException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 01.10.2016.
 */
public class RawFile implements Serializable {

    private final String path;
    private final List<RawCommit> commits = new ArrayList<>();
    private SubprocessException exception;

    public RawFile(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public List<RawCommit> getCommits() {
        return commits;
    }

    public SubprocessException getException() {
        return exception;
    }

    public void setException(SubprocessException exception) {
        this.exception = exception;
    }
}
