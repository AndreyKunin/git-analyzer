package org.ak.gitanalyzer.util;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Andrew on 26.10.2016.
 */
public enum Configuration {
    INSTANCE;

    private static final ThreadLocal<SimpleDateFormat> df = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        }
    };
    private static final ThreadLocal<SimpleDateFormat> dtf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        }
    };
    public static final ThreadLocal<NumberFormat> nf = new ThreadLocal<NumberFormat>() {
        @Override
        protected NumberFormat initialValue() {
            NumberFormat nf = DecimalFormat.getInstance(Locale.US);
            nf.setMaximumFractionDigits(2);
            nf.setRoundingMode(RoundingMode.HALF_UP);
            nf.setGroupingUsed(false);
            return nf;
        }
    };

    public static final String CONF = "conf";
    public static final String CACHE = "cache";
    public static final String CONFIG_FOLDER = "configfolder";
    public static final String CACHE_FOLDER = "cachefolder";
    public static final String SERVER = "server";
    public static final String INSTALL = "install";

    public Set<String> SERVICE_AUTHOR_MARKERS;
    public Set<String> BUILD_FILE_MARKERS;
    public Set<String> SERVICE_COMMIT_MARKERS;

    private Properties teams;
    private Properties locations;
    private Properties authorNameMapping;
    private Properties authorEmailMapping;
    private Properties commonProperties;

    private StartMode startMode;

    private File configDirectory;
    private File cacheDirectory;

    Configuration() {
        clean();
    }

    public void clean() {
        teams = new Properties();
        locations = new Properties();
        authorNameMapping = new Properties();
        authorEmailMapping = new Properties();
        commonProperties = new Properties();
        configDirectory = null;
        cacheDirectory = null;
    }

    public void initConfiguration() throws FileException {
        FileHelper fileHelper = FileHelper.asDirectory(getConfigDirectory());
        teams.putAll(fileHelper.appendFile("teams.properties").exists().isNotDirectory().readPropertiesFromFile());
        locations.putAll(fileHelper.appendFile("locations.properties").exists().isNotDirectory().readPropertiesFromFile());
        commonProperties.putAll(fileHelper.appendFile("application.properties").exists().isNotDirectory().readPropertiesFromFile());
        authorNameMapping.putAll(fileHelper.appendFile("author.names.mapping.properties").exists().isNotDirectory().readPropertiesFromFile());
        authorEmailMapping.putAll(fileHelper.appendFile("author.emails.mapping.properties").exists().isNotDirectory().readPropertiesFromFile());


        SERVICE_AUTHOR_MARKERS = Arrays.stream(getStringArray("GIT.service.author.markers")).collect(Collectors.toSet());
        BUILD_FILE_MARKERS = Arrays.stream(getStringArray("GIT.build.file.markers")).collect(Collectors.toSet());
        SERVICE_COMMIT_MARKERS = Arrays.stream(getStringArray("GIT.service.commit.markers")).collect(Collectors.toSet());
    }

    public Properties getTeams() {
        return teams;
    }

    public Properties getLocations() {
        return locations;
    }

    public String getAuthorName(String name) {
        if (name == null) {
            return null;
        }
        if (authorNameMapping.containsKey(name)) {
            return (String) authorNameMapping.get(name);
        }
        return name;
    }

    public String getAuthorEmail(String email) {
        if (email == null) {
            return null;
        }
        if (authorEmailMapping.containsKey(email)) {
            return (String) authorEmailMapping.get(email);
        }
        return email;
    }

    public String getString(String propertyName) {
        String s = (String) commonProperties.get(propertyName);
        return s != null && s.length() == 0 ? null : s;
    }

    public boolean hasString(String propertyName) {
        return commonProperties.containsKey(propertyName);
    }

    public void setString(String propertyName, String propertyValue) {
        commonProperties.put(propertyName, propertyValue);
    }

    public int getInt(String propertyName, int defaultValue) {
        String s = getString(propertyName);
        return s == null ? defaultValue : Integer.parseInt(s);
    }

    public String[] getStringArray(String propertyName) {
        String string = getString(propertyName);
        if (string == null) {
            return new String[0];
        }
        return string.split(",");
    }

    public Date getDate(String propertyName, Date defaultValue) throws IllegalArgumentException {
        String date = getString(propertyName);
        if (date == null || date.length() == 0) {
            return defaultValue;
        }
        try {
            return dtf.get().parse(date);
        } catch (ParseException ignore) {
            try {
                return df.get().parse(date);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Value \"" + date + "\" corresponds to nether \"yyyy-MM-dd HH:mm:ss\" nor \"yyyy-MM-dd\" format.");
            }
        }
    }

    public StartMode getStartMode() {
        if (startMode == null) {
            startMode = getMode();
        }
        return startMode;
    }

    public File getConfigDirectory() throws FileException {
        if (configDirectory == null) {
            String folderPath = getString(CONFIG_FOLDER);
            FileHelper fileHelper = folderPath == null ?
                    FileHelper.asLocalDirectory().exists().isDirectory().appendDir(CONF) :
                    FileHelper.asDirectory(folderPath);
            configDirectory = fileHelper.exists().isDirectory().canReadWrite().get();
        }
        return configDirectory;
    }

    public File getCacheDirectory() throws FileException {
        if (cacheDirectory == null) {
            String folderPath = getString(CACHE_FOLDER);
            FileHelper fileHelper = folderPath == null ?
                    FileHelper.asLocalDirectory().exists().isDirectory().appendDir(CACHE) :
                    FileHelper.asDirectory(folderPath);
            cacheDirectory = fileHelper.exists().isDirectory().canReadWrite().get();
        }
        return cacheDirectory;
    }

    private StartMode getMode() {
        if (hasString(SERVER)) {
            return StartMode.SERVER;
        }
        if (hasString(INSTALL)) {
            return StartMode.INSTALL;
        }
        return StartMode.CLIENT;
    }

    public enum StartMode {
        CLIENT, SERVER, INSTALL
    }
}
