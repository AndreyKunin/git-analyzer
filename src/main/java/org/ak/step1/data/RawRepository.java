package org.ak.step1.data;

import org.ak.step1.Tempfile;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Andrew on 01.10.2016.
 */
public class RawRepository implements Serializable {

    private static final String TEMP_FILENAME = "git-analyzer.tmp";

    private ArrayList<RawFile> rawFiles;

    private Date buildDate;

    public RawRepository(List<RawFile> rawFiles, Date buildDate) {
        this.rawFiles = rawFiles instanceof ArrayList ? (ArrayList<RawFile>) rawFiles : new ArrayList<>(rawFiles);
        this.buildDate = buildDate;
    }

    public Date getBuildDate() {
        return buildDate;
    }

    public ArrayList<RawFile> getRawFiles() {
        return rawFiles;
    }

    public void persist() throws IOException {
        File tempFile = Tempfile.createTempFile(TEMP_FILENAME);
        if (tempFile == null) {
            return;
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tempFile))) {
            out.writeObject(this);
            System.out.println("Raw Repository stored to file " + tempFile.getPath());
        }
    }

    public void merge(RawRepository other) throws IOException {
        Map<String, RawFile> matchMap = rawFiles.stream().collect(Collectors.toMap(RawFile::getPath, rf -> rf));
        other.rawFiles.forEach(rf -> {
            if (matchMap.containsKey(rf.getPath())) {
                matchMap.get(rf.getPath()).getCommits().addAll(rf.getCommits());
            } else {
                matchMap.put(rf.getPath(), rf);
            }
        });
        rawFiles = new ArrayList<>(matchMap.values());
        buildDate = other.buildDate;
        System.out.println("Raw Repository merged as of " + buildDate);
    }

    public static RawRepository restore() throws IOException {
        RawRepository rawRepository;
        System.out.println("Restoring GIT statistics from cache.");

        File tempFile = Tempfile.getTempFile(TEMP_FILENAME);
        if (tempFile == null || !tempFile.exists() || tempFile.length() == 0) {
            rawRepository = null;
        } else {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(tempFile))) {
                rawRepository = (RawRepository) in.readObject();
                System.out.println("Raw Repository restored from file " + tempFile.getPath());
            } catch (Exception e) {
                e.printStackTrace();
                rawRepository = null;
            }
        }

        if (rawRepository == null) {
            System.out.println("GIT statistics not found in cache.");
        }
        return rawRepository;
    }
}
