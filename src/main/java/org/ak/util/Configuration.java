package org.ak.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Andrew on 26.10.2016.
 */
public enum Configuration {
    INSTANCE;

    private static ThreadLocal<SimpleDateFormat> df = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        }
    };
    private static ThreadLocal<SimpleDateFormat> dtf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        }
    };

    public final Set<String> SERVICE_AUTHOR_MARKERS;
    public final Set<String> BUILD_FILE_MARKERS;
    public final Set<String> SERVICE_COMMIT_MARKERS;

    Properties teams;
    Properties locations;
    Properties authorNameMapping;
    Properties authorEmailMapping;
    Properties commonProperties;

    Configuration() {
        teams = readProperties("teams.properties");
        locations = readProperties("locations.properties");
        commonProperties = readProperties("application.properties");
        authorNameMapping = readProperties("author.names.mapping.properties");
        authorEmailMapping = readProperties("author.emails.mapping.properties");


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

    public boolean getBoolean(String propertyName, boolean defaultValue) {
        String s = getString(propertyName);
        return s == null ? defaultValue : Boolean.parseBoolean(s);
    }

    private Properties readProperties(String fileName) {
        Properties prop = new Properties();
        try(BufferedInputStream is = new BufferedInputStream(this.getClass().getClassLoader().getResourceAsStream("data/" + fileName))) {
            prop.load(is);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
        return prop;
    }
}
