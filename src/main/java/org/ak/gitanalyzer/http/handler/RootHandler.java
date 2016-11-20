package org.ak.gitanalyzer.http.handler;

import com.sun.net.httpserver.HttpExchange;
import org.ak.gitanalyzer.http.ServiceFacade;
import org.ak.gitanalyzer.http.processor.ContentType;
import org.ak.gitanalyzer.step1.git.builder.IExploreBuilder;

import java.io.IOException;
import java.util.Map;

import static org.ak.gitanalyzer.http.ServiceFacade.PATH;

/**
 * Created by Andrew on 11.10.2016.
 */
public class RootHandler extends BaseHandler {

    private StaticFileHandler staticFileHandler = new StaticFileHandler();

    private TextHandler defaultHandler = new RedirectFunctionHandler(x -> null) {
        @Override
        void setHeaders(ContentType contentType, HttpExchange t, Map<String, String> parameters) {
            parameters.put(ServiceFacade.FORM_RETURN_PAGE, "/" + IExploreBuilder.START_PAGE);
            super.setHeaders(contentType, t, parameters);
        }
    };

    @Override
    protected void write(Map<String, String> parameters, HttpExchange t) throws IOException {
        if (parameters.get(PATH).equals("/")) {
            defaultHandler.write(parameters, t);
        } else {
            staticFileHandler.write(parameters, t);
        }
    }
}
