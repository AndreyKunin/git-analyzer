package org.ak.gitanalyzer.step3;

import org.ak.gitanalyzer.step2.data.DataRepository;
import org.ak.gitanalyzer.step2.data.File;
import org.ak.gitanalyzer.step2.data.Link;
import org.ak.gitanalyzer.step3.data.FileAuthors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Andrew on 09.10.2016.
 */
public class FileAuthorsAnalyzer extends Analyzer {

    private int minAuthorsCount;

    public FileAuthorsAnalyzer setMinAuthorsCount(int threshold) {
        this.minAuthorsCount = threshold;
        return this;
    }

    /**
     * Range files by count of authors.
     * @param dataRepository in-memory repository
     * @return List<FileAuthors>
     */
    public List<FileAuthors> getSharedFiles(DataRepository dataRepository) {
        List<FileAuthors> fileAuthorsList = new ArrayList<>();
        dataRepository.getFiles().entrySet().stream()
                .map(entry -> calculateFileAuthors(entry.getKey(), entry.getValue()))
                .forEach(fileAuthorsList::add);
        fileAuthorsList.sort(FileAuthors::compareTo);
        return filter(fileAuthorsList);
    }

    /**
     * Range modules by count of authors.
     * @param fileAuthorsList statistics given by getSharedFiles()
     * @return List<FileAuthors>
     */
    public List<FileAuthors> getSharedModules(List<FileAuthors> fileAuthorsList) {
        Map<String, FileAuthors> moduleAuthorsMap = new HashMap<>();
        fileAuthorsList.forEach(fa -> {
            String moduleName = fa.getFile().getModuleName();
            FileAuthors moduleAuthors = moduleAuthorsMap.get(moduleName);
            if (moduleAuthors == null) {
                moduleAuthors = new FileAuthors(new File(moduleName));
                moduleAuthorsMap.put(moduleName, moduleAuthors);
            }
            moduleAuthors.addAuthors(fa.getAuthors());
        });
        List<FileAuthors> moduleAuthors = new ArrayList<>(moduleAuthorsMap.values());
        moduleAuthors.sort(FileAuthors::compareTo);
        return filter(moduleAuthors);
    }

    private List<FileAuthors> filter(List<FileAuthors> initialList) {
        if (minAuthorsCount > 0) {
            return initialList.stream().filter(fa -> fa.getAuthorsCount() >= minAuthorsCount).collect(Collectors.toList());
        }
        return initialList;
    }

    private FileAuthors calculateFileAuthors(File file, List<Link> links) {
        FileAuthors fileAuthors = new FileAuthors(file);
        links.stream().filter(this::filter).forEach(link -> fileAuthors.addAuthor(link.getAuthor()));
        return fileAuthors;
    }
}
