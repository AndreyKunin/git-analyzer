package org.ak.gitanalyzer.step3.data;

import org.ak.gitanalyzer.step2.data.File;

/**
 * Created by Andrew on 08.10.2016.
 */
public class FileSummary implements Comparable<FileSummary> {
    private File file;
    private double fileWeight;

    public FileSummary(File file, double fileWeight) {
        this.file = file;
        this.fileWeight = fileWeight;
    }

    public File getFile() {
        return file;
    }

    public double getFileWeight() {
        return fileWeight;
    }

    @Override
    public int compareTo(FileSummary o) {
        return fileWeight < o.fileWeight ? 1 : fileWeight > o.fileWeight ? -1 : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileSummary that = (FileSummary) o;

        return file.equals(that.file);

    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
