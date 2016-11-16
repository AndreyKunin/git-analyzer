package org.ak.http.processor;

import org.ak.http.ThrowingFunction;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.ak.http.processor.ContentType.HTML;
import static org.ak.http.processor.ContentType.TEXT;

/**
 * Created by Andrew on 13.10.2016.
 */
public enum ContentProcessor {
    INSTANCE;

    private static boolean DO_CACHE = true;

    private Map<String, byte[]> contentByteCache = new ConcurrentHashMap<>();
    private Map<String, String> contentStringCache = new ConcurrentHashMap<>();
    private Map<String, ContentType> typeCache = new ConcurrentHashMap<>();

    public String get500Content(Exception e) {
        return "<span clolr=red>Internal server error: " + e.getMessage() + ". Look server log for details.</span><br>" + Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).reduce((r1, r2) -> r1 + "<br>" + r2).get();
    }

    public String get404Content(String path) {
        return "<span clolr=red>File " + path + " not found.</span>";
    }

    public String getStringContent(String path) throws IOException {
        return getContent(path, this::toStringArray, contentStringCache);
    }

    public byte[] getBinaryContent(String path) throws IOException {
        return getContent(path, this::toByteArray, contentByteCache);
    }

    public ContentType getType(String path) {
        ContentType result = typeCache.get(path);
        if (result == null) {
            result = getContentType(path);
            if (DO_CACHE) {
                typeCache.put(path, result);
            }
        }
        return result;
    }

    private ContentType getContentType(String path) {
        if (path.equals("/")) {
            return HTML;
        }
        int extDot = path.lastIndexOf('.');
        if (extDot == -1) {
            return TEXT;
        }
        String ext = path.substring(extDot + 1);
        if (ext.length() == 0) {
            return TEXT;
        }
        try {
            return ContentType.valueOf(ext.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TEXT;
        }
    }

    private <T> T getContent(String path, ThrowingFunction<InputStream, T> read, Map<String, T> cache) throws IOException {
        T result = cache.get(path);
        if (result == null) {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("html/" + path);
            if (is == null) {
                return null;
            }
            result = read.applyThrows(is);
            if (DO_CACHE) {
                cache.put(path, result);
            }
        }
        return result;
    }

    private byte[] toByteArray(InputStream is) throws IOException {
        try (BufferedInputStream bs = new BufferedInputStream(is); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];

            int read;
            while ((read = bs.read(buffer)) != -1 ) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }

    private String toStringArray(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is)); CharArrayWriter out = new CharArrayWriter()) {
            char[] buffer = new char[4096];

            int read;
            while ((read = br.read(buffer)) != -1 ) {
                out.write(buffer, 0, read);
            }
            return out.toString();
        }
    }

}
