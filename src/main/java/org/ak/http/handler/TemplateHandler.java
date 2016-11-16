package org.ak.http.handler;

import org.ak.http.ServiceFacade;

import java.util.Map;

/**
 * Created by Andrew on 13.10.2016.
 */
public class TemplateHandler extends TextHandler {

    @Override
    protected String process(Map<String, String> parameters) {
        return ServiceFacade.INSTANCE.processTemplate(parameters);
    }
}
