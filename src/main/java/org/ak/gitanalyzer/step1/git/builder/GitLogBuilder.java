package org.ak.gitanalyzer.step1.git.builder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Andrew on 30.09.2016.
 */
public class GitLogBuilder extends CommandBuilder {

    private static ThreadLocal<SimpleDateFormat> df = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        }
    };

    private Date sinceDate;
    private Date untilDate;
    private String fileName;


    public GitLogBuilder setSinceDate(Date sinceDate) {
        this.sinceDate = sinceDate;
        return this;
    }

    public GitLogBuilder setUntilDate(Date untilDate) {
        this.untilDate = untilDate;
        return this;
    }

    public GitLogBuilder setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Override
    public String[] buildCommand() {
        List<String> parameters = new ArrayList<>();
        parameters.add("git");
        parameters.add("log");
        parameters.add("--all");
        parameters.add("--no-merges");
        parameters.add("--pretty=format:\"%H]|[%an]|[%ae]|[%ad]|[%s\"");
        if (sinceDate != null) {
            parameters.add("--since=\"" + df.get().format(sinceDate) + "\"");
        }
        if (untilDate != null) {
            parameters.add("--before=\"" + df.get().format(untilDate) + "\"");
        }
        parameters.add("\"" + fileName + "\"");
        return parameters.toArray(new String[parameters.size()]);
    }
}
