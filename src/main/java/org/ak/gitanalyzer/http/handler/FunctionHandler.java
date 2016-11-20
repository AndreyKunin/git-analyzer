package org.ak.gitanalyzer.http.handler;

import java.util.Map;
import java.util.function.Function;

/**
 * Created by Andrew on 15.10.2016.
 */
public class FunctionHandler extends TextHandler {

    private Function<Map<String, String>, String> function;

    public FunctionHandler(Function<Map<String, String>, String> function) {
        this.function = function;
    }

    @Override
    protected String process(Map<String, String> parameters) {
        return function.apply(parameters);
    }
}
