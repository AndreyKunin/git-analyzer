package org.ak.step1.git.builder;

import org.ak.util.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 11.10.2016.
 */
public class IExploreBuilder extends CommandBuilder {

    public static final String START_PAGE = "cost_analysis.html";

    private int port;
    private String host;

    public IExploreBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public IExploreBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    @Override
    public String[] buildCommand() {
        List<String> parameters = new ArrayList<>();
        parameters.add(Configuration.INSTANCE.getString("HTTP.browser.path"));
        parameters.add("http://" + host + ":" + port + "/" + START_PAGE);
        return parameters.toArray(new String[parameters.size()]);
    }
}
