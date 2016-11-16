package org.ak.http.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.ak.http.ServiceFacade;
import org.ak.http.processor.ContentType;

import java.util.Map;
import java.util.function.Function;

/**
 * Created by Andrew on 12.11.2016.
 */
public class DownloadFunctionHandler extends FunctionHandler {

    public DownloadFunctionHandler(Function<Map<String, String>, String> function) {
        super(function);
    }

    @Override
    void setHeaders(ContentType contentType, HttpExchange t, Map<String, String> parameters) {
        super.setHeaders(contentType, t, parameters);
        Headers h = t.getResponseHeaders();
        h.set("Content-Disposition", "attachment;filename=" + getFileName(parameters.get(ServiceFacade.PATH)));
    }

    private String getFileName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
