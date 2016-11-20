package org.ak.gitanalyzer;

import org.ak.gitanalyzer.util.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {


    public static void main(String[] args) throws Exception {
        try {
            displayHeader();

            configure(parseArgs(args));

            new Initializer().configure();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        displayFooter();
    }

    private static void displayHeader() {
        System.out.println("==============================================================================");
        System.out.println("==   Git Analyzer v 0.2                                                     ==");
        System.out.println("==============================================================================");
    }

    private static void displayFooter() {
        System.out.println("==============================================================================");
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> result = new HashMap<>();
        Arrays.stream(args).forEach(arg -> {
            int delimiterPos = arg.indexOf('=');
            if (delimiterPos == -1) {
                result.put(getKey(arg), "");
                return;
            }
            result.put(getKey(arg.substring(0, delimiterPos)), arg.substring(delimiterPos + 1).trim());
        });
        return result;
    }

    private static void configure(Map<String, String> parameters) {
        parameters.forEach(Configuration.INSTANCE::setString);
    }

    private static String getKey(String arg) {
        return arg.trim().replace("-", "").toLowerCase();
    }
}
