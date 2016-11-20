package org.ak.gitanalyzer.step1.git;

/**
 * Created by Andrew on 28.09.2016.
 */
public class SubprocessException extends Exception {

    public SubprocessException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubprocessException(String message) {
        super(message);
    }
}
