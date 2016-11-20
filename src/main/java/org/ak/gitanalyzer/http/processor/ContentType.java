package org.ak.gitanalyzer.http.processor;

/**
 * Created by Andrew on 14.10.2016.
 */
public enum ContentType {
    TEXT("text/plain; charset=utf-8"),
    HTML("text/html; charset=utf-8"),
    JS("text/js; charset=utf-8"),
    JSON("application/json; charset=utf-8"),
    CSS("text/css; charset=utf-8"),
    CSV("text/csv; charset=utf-8"),
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
