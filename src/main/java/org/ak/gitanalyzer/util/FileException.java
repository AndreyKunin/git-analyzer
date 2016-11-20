package org.ak.gitanalyzer.util;

/**
 * Created by Andrew on 17.11.2016.
 */
public class FileException extends Exception {

    public FileException(String message) {
        super(message);
    }

    public FileException(String message, Throwable cause) {
        super(message, cause);
    }
}
