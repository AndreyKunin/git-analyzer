package org.ak.step1.git.parser;

import org.ak.step1.data.RawCommit;
import org.ak.step1.data.RawFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Andrew on 01.10.2016.
 */
public class LogParser {

    private static final int HASH_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int EMAIL_INDEX = 2;
    private static final int DATE_INDEX = 3;
    private static final int COMMENT_INDEX = 4;

    private static final ThreadLocal<SimpleDateFormat> df = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("E MMM d HH:mm:ss yyyy Z", Locale.US);
        }
    };

    public void parseLog(RawFile rawFile, String output) {
        if (output == null) {
            return;
        }
        List<RawCommit> commits = rawFile.getCommits();
        String[] parts = output.split("\\]\\|\\[");

        Date date;
        try {
            date = df.get().parse(parts[DATE_INDEX]);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
            date = new Date();
        }

        RawCommit commit = new RawCommit(parts[HASH_INDEX], date, parts[NAME_INDEX], parts[EMAIL_INDEX], parts[COMMENT_INDEX]);
        commits.add(commit);
    }
}
