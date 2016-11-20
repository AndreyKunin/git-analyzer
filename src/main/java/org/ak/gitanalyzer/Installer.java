package org.ak.gitanalyzer;

import org.ak.gitanalyzer.util.Configuration;
import org.ak.gitanalyzer.util.FileException;
import org.ak.gitanalyzer.util.FileHelper;

import java.io.File;

import static org.ak.gitanalyzer.util.Configuration.*;

/**
 * Created by Andrew on 17.11.2016.
 */
class Installer {

    static void installApplication() throws FileException {
        System.out.println("Configuring application.");

        File configDirectory = initDestinationFolder(CONFIG_FOLDER, CONF, "Configuration directory");
        initDestinationFolder(CACHE_FOLDER, CACHE, "Cache directory");

        FileHelper.asDirectory(configDirectory).appendFile("application.properties").notExists().createFile(false).copyPropertiesFromJar();
        FileHelper.asDirectory(configDirectory).appendFile("author.emails.mapping.properties").notExists().createFile(false).copyPropertiesFromJar();
        FileHelper.asDirectory(configDirectory).appendFile("author.names.mapping.properties").notExists().createFile(false).copyPropertiesFromJar();
        FileHelper.asDirectory(configDirectory).appendFile("locations.properties").notExists().createFile(false).copyPropertiesFromJar();
        FileHelper.asDirectory(configDirectory).appendFile("teams.properties").notExists().createFile(false).copyPropertiesFromJar();

        System.out.println("Application configured.");
    }

    private static File initDestinationFolder(String parameterName, String defaultFolderName, String logPrefix) throws FileException {
        String folderPath = Configuration.INSTANCE.getString(parameterName);
        if (folderPath == null) {
            System.out.println(logPrefix + " parameter -" + parameterName + " is not specified. New directory \"" + defaultFolderName + "\" will be created.");
            return FileHelper.asLocalDirectory()
                    .exists().isDirectory().canReadWrite()
                    .appendDir(defaultFolderName)
                    .createDirectory(false, logPrefix + " \"" + defaultFolderName + "\" already exists. Directory will not be created.")
                    .exists().isDirectory().canReadWrite().get();
        } else {
            System.out.println(logPrefix + " is \"" + folderPath + "\".");
            return FileHelper.asDirectory(folderPath)
                    .createDirectory(false, logPrefix + " \"" + defaultFolderName + "\" already exists. Directory will not be created.")
                    .exists().isDirectory().canReadWrite().get();
        }
    }
}
