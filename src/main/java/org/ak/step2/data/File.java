package org.ak.step2.data;

import org.ak.util.Configuration;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Andrew on 01.10.2016.
 */
public class File {

    public static final String ROOT_MODULE = "/";

    private final String path;
    private String moduleName;
    private String fileName;

    private boolean isVCSFile;

    public File(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        if (fileName == null) {
            int startName = path.lastIndexOf("/");
            if (startName == -1 || startName == path.length() - 1) {
                fileName = path;
            } else {
                fileName = path.substring(startName + 1);
            }
        }
        return fileName;
    }

    public String getModuleName() {
        return moduleName;
    }

    String generateModuleName() {
        if (moduleName == null) {
            int startSource = path.indexOf("/src/");
            if (startSource < 0) {
                Optional<String> buildFile = Configuration.INSTANCE.BUILD_FILE_MARKERS.stream().filter(path::endsWith).findFirst();
                if (buildFile.isPresent()) {
                    startSource = path.lastIndexOf("/", path.length() - buildFile.get().length() - 1);
                }
            }
            if (startSource < 0) {
                moduleName = ROOT_MODULE;
            } else {
                moduleName = path.substring(0, startSource);
            }
        }
        return moduleName;
    }

    void updateModuleName(Set<String> modules) {
        if (moduleName.equals(ROOT_MODULE)) {
            Set<String> matchedModules = modules.stream().filter(path::startsWith).collect(Collectors.toSet());
            moduleName = matchedModules.stream().max((s1, s2) -> s1.length() - s2.length()).orElse(ROOT_MODULE);
        }
    }

    public boolean isVCSFile() {
        return isVCSFile;
    }

    public void setVCSFile(boolean VCSFile) {
        isVCSFile = VCSFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        File file = (File) o;

        return path.equals(file.path);

    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
