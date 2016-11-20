package org.ak.gitanalyzer.step1.data;

import org.ak.gitanalyzer.util.Configuration;
import org.ak.gitanalyzer.util.FileException;
import org.ak.gitanalyzer.util.FileHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    public void persist() throws IOException {
        try {
            File tempFile = FileHelper.asDirectory(Configuration.INSTANCE.getCacheDirectory())
                    .appendFile(TEMP_FILENAME).createFile(true, "Updating file " + TEMP_FILENAME)
                    .get();
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tempFile))) {
                out.writeObject(this);
                System.out.println("Raw Repository stored to file " + tempFile.getPath());
            }
        } catch (FileException e) {
            System.out.println(e.getMessage());
            System.out.println("Unable to persist GIT statistics.");
        }
    }

    public static RawRepository restore() {
        RawRepository rawRepository;
        System.out.println("Restoring GIT statistics from cache.");

        try {
            File tempFile = FileHelper.asDirectory(Configuration.INSTANCE.getCacheDirectory())
                    .appendFile(TEMP_FILENAME).exists().isNotDirectory().canReadWrite()
                    .get();
            if (tempFile.length() == 0) {
                throw new FileException("Invalid cache file.");
            } else {
                rawRepository = restoreFromFile(tempFile);
            }
        } catch (FileException e) {
            System.out.println(e.getMessage());
            rawRepository = null;
        }
        if (rawRepository == null) {
            System.out.println("GIT statistics not found in cache.");
        }
        return rawRepository;
    }


    private static RawRepository restoreFromFile(File tempFile) throws FileException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(tempFile))) {
            RawRepository rawRepository = (RawRepository) in.readObject();
            System.out.println("Raw Repository restored from file " + tempFile.getPath());
            return rawRepository;
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileException(e.getMessage(), e);
        }
    }
}
