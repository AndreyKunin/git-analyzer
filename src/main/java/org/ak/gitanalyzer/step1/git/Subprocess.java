package org.ak.gitanalyzer.step1.git;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Andrew on 27.09.2016.
 */
public class Subprocess {

    public static void start(Consumer<Integer> exitCallback, String ... command) throws SubprocessException {
        Process process;
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            process = builder.start();
        } catch (IOException e) {
            throw new SubprocessException(e.getMessage(), e);
        }
        try {
            int exitCode = process.waitFor();
            exitCallback.accept(exitCode);
        } catch (InterruptedException e) {
            System.out.println("Unable to kill process " + command[0] + ". Please do it manually.");
        }

    }

    public static SubprocessResult execute(String workingDirectory, boolean redirectStderr, String ... command) throws SubprocessException {
        File directory = new File(workingDirectory);
        if (!directory.exists()) {
            throw new SubprocessException("Working directory " + workingDirectory + " not found.");
        }

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(redirectStderr);
        builder.directory(directory);
        Process process;
        SubprocessResult result = new SubprocessResult(command);

        try {
            process = builder.start();
        } catch (IOException e) {
            throw new SubprocessException(e.getMessage(), e);
        }

        try (BufferedReader inOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader inErr = new BufferedReader(new InputStreamReader(process.getInputStream()));) {
            while(process.isAlive()) {
                readOutput(result, inOut, inErr);
            }
            readOutput(result, inOut, inErr);
            result.exitCode = process.exitValue();
            return result;
        } catch (IOException e) {
            throw new SubprocessException(e.getMessage(), e);
        }
    }

    private static void readOutput(SubprocessResult result, BufferedReader inOut, BufferedReader inErr) throws IOException {
        String line;
        while ((line = inOut.readLine()) != null) {
            result.out.add(line);
        }
        while ((line = inErr.readLine()) != null) {
            result.errorOut.add(line);
        }
    }


    public static class SubprocessResult {

        private List<String> out = new ArrayList<>();
        private List<String> errorOut = new ArrayList<>();
        private String[] commandWithParameters;
        private int exitCode;

        private SubprocessResult(String ... command) {
            this.commandWithParameters = command;
        }

        public List<String> getErrorOut() {
            return errorOut;
        }

        public List<String> getOut() {
            return out;
        }

        private String getCommand() {
            return String.join(" ", Arrays.asList(commandWithParameters));
        }

        public SubprocessResult assertExitCode() throws SubprocessException {
            if (exitCode != 0) {
                throw new SubprocessException("Command [" + getCommand() + "] processed with exit code " + exitCode);
            }
            return this;
        }

        public SubprocessResult assertErrors() throws SubprocessException {
            if (errorOut.size() != 0) {
                throw new SubprocessException("Command [" + getCommand() + "] processed with error: " + String.join(System.lineSeparator(), errorOut));
            }
            return this;
        }

        public SubprocessResult assertOutputPresent() throws SubprocessException {
            if (out.size() == 0) {
                throw new SubprocessException("Command [" + getCommand() + "] returned nothing.");
            }
            return this;
        }
    }
}
