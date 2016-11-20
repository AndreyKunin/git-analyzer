package org.ak.gitanalyzer.http.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.ak.gitanalyzer.http.ServiceFacade;
import org.ak.gitanalyzer.http.processor.ContentType;

import java.util.Map;
import java.util.function.Function;

/**
 * Created by Andrew on 03.11.2016.
 */
public class RedirectFunctionHandler extends FunctionHandler {

    public RedirectFunctionHandler(Function<Map<String, String>, String> function) {
        super(function);
    }

    @Override
    void setHeaders(ContentType contentType, HttpExchange t, Map<String, String> parameters) {
        super.setHeaders(contentType, t, parameters);
        Headers h = t.getResponseHeaders();
        String redirectPage = parameters.get(ServiceFacade.FORM_RETURN_PAGE);
        if (redirectPage == null) {
            redirectPage = "";
        }
        if (!redirectPage.startsWith("/")) {
            redirectPage = "/" + redirectPage;
        }
        h.set("Location", redirectPage);
    }

    @Override
    protected int getReturnCode() {
        return 303;
    }
}
