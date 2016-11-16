package org.ak.http.handler;

import com.sun.net.httpserver.HttpExchange;
import org.ak.http.ServiceFacade;
import org.ak.http.processor.ContentType;

import java.io.IOException;
import java.util.Map;

import static org.ak.http.ServiceFacade.PATH;

/**
 * Created by Andrew on 12.10.2016.
 */
class StaticFileHandler extends BaseHandler {

    @Override
    protected void write(Map<String, String> parameters, HttpExchange t) throws IOException {
        String path = parameters.get(PATH);
        ServiceFacade facade = ServiceFacade.INSTANCE;
        ContentType contentType = facade.getType(path);
        byte[] response = facade.getBinaryContent(path);
        if (response == null) {
            write404(path, t);
            return;
        }
        setHeaders(contentType, t, parameters);
        t.sendResponseHeaders(200, 0);
        out(response, t);
    }

    private void write404(String path, HttpExchange t) throws IOException {
        String message = ServiceFacade.INSTANCE.get404Content(path);
        byte[] bytes = message.getBytes();
        t.sendResponseHeaders(404, bytes.length);
        System.out.println("File " + path + " not found.");
        out(bytes, t);
    }
}
