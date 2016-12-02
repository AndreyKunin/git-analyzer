package org.ak.gitanalyzer.util;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Created by Andrew on 28.09.2016.
 */
public class FileHelper {

    private File file;
    private boolean isDirectory = true;

    private FileHelper(File file, boolean isDirectory) {
        this.file = file;
        this.isDirectory = isDirectory;
    }

    public File getFile(String name, Supplier<File> supplier) throws IOException {
        return getFile(name, supplier, null);
    }

    public File getFile(String name, Supplier<File> supplier, String exceptionString) throws IOException {
        File dir = supplier.get();
        if (dir == null) {
            if (exceptionString == null) {
                return null;
            } else {
                throw new IOException(exceptionString);
            }
        }
        File f = new File(dir, name);
        if (!f.canRead() || !f.canWrite()) {
            if (exceptionString == null) {
                return null;
            } else {
                throw new IOException(exceptionString);
            }
        }
        return f;
    }

    Properties readPropertiesFromFile() {
        return readProperties(() -> new FileInputStream(file));
    }

    public void copyPropertiesFromJar() {
        byte[] buffer = new byte[1024];
        int length;
        try(BufferedInputStream is = new BufferedInputStream(FileHelper.class.getClassLoader().getResourceAsStream("data/" + file.getName()));
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    public FileHelper createFile(boolean overwrite) throws FileException {
        return create(false, overwrite, null);
    }

    public FileHelper createDirectory(boolean overwrite, String messageIfExists) throws FileException {
        return create(true, overwrite, messageIfExists);
    }

    public FileHelper createFile(boolean overwrite, String messageIfExists) throws FileException {
        return create(false, overwrite, messageIfExists);
    }

    public File get() throws FileException {
        return file;
    }

    public FileHelper appendFile(String name) throws FileException {
        return new FileHelper(new File(file, name), false);
    }

    public FileHelper appendDir(String name) throws FileException {
        return new FileHelper(new File(file, name), true);
    }

    public FileHelper exists() throws FileException {
        return validate(File::exists, "does not exist.");
    }

    public FileHelper notExists() throws FileException {
        return validate(f -> !f.exists(), "already exists.");
    }

    public FileHelper isDirectory() throws FileException {
        return validate(File::isDirectory, "is not a directory.");
    }

    public FileHelper isNotDirectory() throws FileException {
        return validate(f -> !f.isDirectory(), "is a directory.");
    }

    public FileHelper canReadWrite() throws FileException {
        return validate(f -> f.canRead() && f.canWrite(), "does not have read and write access.");
    }

    public static FileHelper asLocalDirectory() throws FileException {
        return asDirectory(() -> new File(FileHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
    }

    public static FileHelper asDirectory(String path) throws FileException {
        return asDirectory(() -> new File(path));
    }

    public static FileHelper asDirectory(File file) throws FileException {
        return asDirectory(() -> file);
    }

    public static String quoteFileMask(String fileMask) {
        String fileMaskQuoted = null;
        if (fileMask != null) {
            fileMask = fileMask.trim();
            if (fileMask.length() != 0) {
                fileMaskQuoted = Arrays.stream(fileMask.split("\\*"))
                        .map(Pattern::quote)
                        .reduce((p1, p2) -> p1 + ".*" + p2)
                        .orElse(fileMask.contains("*") ? "" : Pattern.quote(fileMask));
                fileMaskQuoted = fileMaskQuoted.replace("\\Q\\E", "");
                if (!fileMaskQuoted.startsWith(".*")) {
                    fileMaskQuoted = ".*" + fileMaskQuoted;
                }
                if (!fileMaskQuoted.endsWith(".*")) {
                    fileMaskQuoted = fileMaskQuoted + ".*";
                }
            }
        }
        return fileMaskQuoted;
    }

    private String getDescription() {
        return (isDirectory ? "Directory " : "File ") + file.getAbsolutePath();
    }

    private static FileHelper asDirectory(ThrowingSupplier<File> resolver) throws FileException {
        try {
            return new FileHelper(resolver.getThrows(), true);
        } catch (Exception e) {
            throw new FileException(e.getMessage(), e);
        }
    }

    private FileHelper validate(Function<File, Boolean> check, String message) throws FileException {
        if (file == null || !check.apply(file)) {
            throw new FileException(getDescription() + " " + message);
        }
        return this;
    }

    private FileHelper create(boolean asDirectory, boolean overwrite, String existsMessage) throws FileException {
        if(overwrite && file.exists() && !file.delete()) {
            throw new FileException("Could not delete " + getDescription());
        }
        try {
            boolean result = asDirectory ? file.mkdirs() : file.createNewFile();
            if (overwrite && !result) {
                throw new FileException("Could not create " + getDescription());
            }
            if (!result && existsMessage != null) {
                System.out.println(existsMessage);
            }
        } catch (IOException e) {
            throw new FileException(e.getMessage(), e);
        }
        return this;
    }

    private Properties readProperties(ThrowingSupplier<InputStream> isSupplier) {
        Properties prop = new Properties();
        try(BufferedInputStream is = new BufferedInputStream(isSupplier.getThrows())) {
            prop.load(is);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
        return prop;
    }
}
