package org.ak.step1;

import sun.security.action.GetPropertyAction;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.util.function.Supplier;

/**
 * Created by Andrew on 28.09.2016.
 */
public class Tempfile {

    public static File createTempFile(String name) throws IOException {
        File temp = getTempFile(name, Tempfile::tryGetTempDirectory);
        if (temp == null) {
            temp = getTempFile(name, Tempfile::tryGetLocalDirectory);
        }
        if (temp == null) {
            return null;
        }
        if(!temp.delete()) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }
        if(!temp.createNewFile()) {
            throw new IOException("Could not create temp file: " + temp.getAbsolutePath());
        }
        return temp;
    }

    public static File getTempFile(String name) throws IOException {
        File temp = getTempFile(name, Tempfile::tryGetTempDirectory);
        if (temp == null) {
            temp = getTempFile(name, Tempfile::tryGetLocalDirectory);
        }
        return temp;
    }

    private static File tryGetTempDirectory() {
        File tmpdir;
        try {
            tmpdir = new File(AccessController.doPrivileged(new GetPropertyAction("java.io.tmpdir")));
            if (!tmpdir.exists() || !tmpdir.isDirectory() || !tmpdir.canWrite()) {
                tmpdir = null;
            }
        } catch (Exception e) {
            tmpdir = null;
        }
        return tmpdir;
    }

    private static File tryGetLocalDirectory() {
        File localdir;
        try {
            localdir = new File(Tempfile.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            if (!localdir.exists() || !localdir.isDirectory() || !localdir.canWrite()) {
                localdir = null;
            }
        } catch (Exception e) {
            localdir = null;
        }
        return localdir;
    }

    private static File getTempFile(String name, Supplier<File> supplier) throws IOException {
        File dir = supplier.get();
        if (dir == null) {
            return null;
        }
        File f = new File(dir, name);
        if (!f.canRead() || !f.canWrite()) {
            return null;
        }
        return f;
    }

}
