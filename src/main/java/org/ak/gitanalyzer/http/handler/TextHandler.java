package org.ak.gitanalyzer.http.handler;

import com.sun.net.httpserver.HttpExchange;
import org.ak.gitanalyzer.http.ServiceFacade;
import org.ak.gitanalyzer.http.processor.ContentType;

import java.io.IOException;
import java.util.Map;

import static org.ak.gitanalyzer.http.ServiceFacade.PATH;

/**
 * Created by Andrew on 12.10.2016.
 */
abstract class TextHandler extends BaseHandler {

    protected void write(Map<String, String> parameters, HttpExchange t) throws IOException {
        String response = process(parameters);
        ContentType contentType = ServiceFacade.INSTANCE.getType(parameters.get(PATH));
        setHeaders(contentType, t, parameters);
        byte[] bytes = response == null ? new byte[0] : response.getBytes("UTF-8");
        t.sendResponseHeaders(getReturnCode(), bytes.length);
        out(bytes, t);
    }

    protected int getReturnCode() {
        return 200;
    }

    protected abstract String process(Map<String, String> parameters);
}
