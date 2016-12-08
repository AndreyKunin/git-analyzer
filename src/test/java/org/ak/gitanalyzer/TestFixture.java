package org.ak.gitanalyzer;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;

import static org.ak.gitanalyzer.util.TestHelper.deleteDir;

/**
 * Created by Andrew on 03.12.2016.
 */
public abstract class TestFixture {

    @BeforeClass
    public static void prepare() throws Exception {
        deleteDir(new java.io.File("./target/classes/conf"));
        deleteDir(new java.io.File("./target/classes/cache"));
        Main.main(new String[] {"-install"});
    }

    @AfterClass
    public static void clean() {
        deleteDir(new File("./target/classes/conf"));
        deleteDir(new File("./target/classes/cache"));
    }
}
