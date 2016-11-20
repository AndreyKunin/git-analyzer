package org.ak.gitanalyzer.http.handler;

import org.ak.gitanalyzer.http.ServiceFacade;

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
