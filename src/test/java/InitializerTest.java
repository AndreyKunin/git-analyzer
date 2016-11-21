import org.ak.gitanalyzer.Main;
import org.ak.gitanalyzer.util.Configuration;
import org.ak.gitanalyzer.util.FileException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.ak.gitanalyzer.util.Configuration.StartMode.INSTALL;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;

/**
 * Created by Andrew on 20.11.2016.
 */
public class InitializerTest {

    @Before
    public void init() {
        Configuration.INSTANCE.clean();
        deleteTempDirs();
    }

    @AfterClass
    public static void clean() {
        deleteTempDirs();
        Configuration.INSTANCE.clean();
    }

    @Test
    public void testInstallationSpecifiedDirectories() throws Exception {
        Main.main(new String[] {"-install", "configfolder=./test/config", "cacheFolder=./test/cache"});
        assertFile("./test/config", true, 5, 0, true);
        assertFile("./test/cache", true, 0, 0, true);
    }

    @Test
    public void testInstallationLocalDirectories() throws Exception {
        Main.main(new String[] {"-install"});
        assertFile("./target/classes/conf", true, 5, 0, true);
        assertFile("./target/classes/cache", true, 0, 0, true);
    }

    @Test
    public void testInstallationExistingDirectories() throws Exception {
        new File("./test2/config").mkdirs();
        new File("./test2/cache").mkdirs();
        Main.main(new String[] {"-install", "configfolder=./test2/config", "cacheFolder=./test2/cache"});
        assertFile("./test2/config", true, 5, 0, true);
        assertFile("./test2/cache", true, 0, 0, true);
    }

    @Test
    public void testInstallationAlreadyInstalled() throws Exception {
        Main.main(new String[] {"-install", "configfolder=./test/config", "cacheFolder=./test/cache"});
        File propertiesFile = new File("./test/config/application.properties");
        propertiesFile.delete();
        propertiesFile.createNewFile();
        assertEquals(0, propertiesFile.length());
        Main.main(new String[] {"-install", "configfolder=./test/config", "cacheFolder=./test/cache"});
        assertFile("./test/config", true, 5, 0, false);
        assertFile("./test/cache", true, 0, 0, false);
        assertEquals(0, new File("./test/config/application.properties").length());
    }

    @Test
    public void testInstallationPartiallyInstalled() throws Exception {
        Main.main(new String[] {"-install"});
        assertEquals(5, new File("./target/classes/conf").list().length);
        new File("./target/classes/conf/teams.properties").delete();
        assertEquals(4, new File("./target/classes/conf").list().length);
        Main.main(new String[] {"-install"});
        assertFile("./target/classes/conf", true, 4, 0, false);
        Configuration.INSTANCE.clean();
    }

    @Test
    public void testConfiguration() throws Exception {
        //pre-installed
        Main.main(new String[] {"-install"});
        Configuration conf = Configuration.INSTANCE;
        conf.initConfiguration();
        assertEquals(3, conf.getLocations().size());
        assertEquals(3, conf.getTeams().size());
        assertEquals("author1@email.org", conf.getAuthorEmail("author1.1@email.org"));
        assertEquals("author1.3@email.org", conf.getAuthorEmail("author1.3@email.org"));
        assertEquals("Author 1", conf.getAuthorName("author1"));
        assertEquals("author4", conf.getAuthorName("author4"));
        assertNull(conf.getString("install"));
        assertTrue(conf.hasString("install"));
        assertEquals("./test-repo", conf.getString("GIT.repository.paths"));
        assertEquals(3, conf.getStringArray("GIT.build.file.markers").length);
        assertEquals(500, conf.getInt("GIT.refactoring.commits.min.size", 0));
        assertEquals("Mon Nov 21 14:20:21 EST 2016", conf.getDate("GIT.log.max.date", new Date()).toString());
        assertEquals("Mon Nov 21 00:00:00 EST 2011", conf.getDate("GIT.log.min.date", new Date()).toString());
        assertEquals(INSTALL, conf.getStartMode());
        assertEquals(new File("./target/classes/conf").getCanonicalPath(), conf.getConfigDirectory().getCanonicalPath());
        assertEquals(new File("./target/classes/cache").getCanonicalPath(), conf.getCacheDirectory().getCanonicalPath());

        //not installed
        deleteDir(new File("./target/classes/conf"));
        try {
            conf.initConfiguration();
        } catch (FileException e) {
            assertThat(e.getMessage(), containsString(File.separatorChar + "conf"));
        }
        Configuration.INSTANCE.clean();
    }

    private void assertFile(String path, boolean isDirectory, int childrenCount, long size, boolean initialized) throws FileException, IOException {
        File file = new File(path);
        assertTrue(file.exists());
        assertEquals(isDirectory, file.isDirectory());
        if (isDirectory) {
            assertEquals(childrenCount, file.list().length);
            if (initialized) {
                if (path.contains("conf")) {
                    assertEquals(file.getCanonicalPath(), Configuration.INSTANCE.getConfigDirectory().getCanonicalPath());
                } else if (path.contains("cache")) {
                    assertEquals(file.getCanonicalPath(), Configuration.INSTANCE.getCacheDirectory().getCanonicalPath());
                }
            }
        } else {
            assertEquals(size, file.length());
        }
    }

    private static void deleteTempDirs() {
        deleteDir(new File("./target/classes/conf"));
        deleteDir(new File("./target/classes/cache"));
        deleteDir(new File("./test/config"));
        deleteDir(new File("./test/cache"));
        deleteDir(new File("./test2/config"));
        deleteDir(new File("./test2/cache"));
        deleteDir(new File("./test"));
        deleteDir(new File("./test2"));
    }

    private static void deleteDir(File dir) {
        String[] entries = dir.list();
        if (entries != null) {
            for (String name : entries) {
                File file = new File(dir.getPath(), name);
                file.delete();
            }
        }
        dir.delete();
    }
}
