package org.ak.http.processor;

/**
 * Created by Andrew on 14.10.2016.
 */
public enum ContentType {
    TEXT("text/plain"),
    HTML("text/html"),
    JS("text/js"),
    JSON("application/json"),
    CSS("text/css"),
    CSV("text/csv"),
    GIF("image/gif"),
    JPEG("image/jpeg"),
    PNG("image/png");

    private String parameter;

    ContentType(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }
}
