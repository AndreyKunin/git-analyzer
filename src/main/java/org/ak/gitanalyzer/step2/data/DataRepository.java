package org.ak.gitanalyzer.step2.data;

import org.ak.gitanalyzer.util.Configuration;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by Andrew on 01.10.2016.
 */
public class DataRepository {

    private Map<Author, List<Link>> authors = new HashMap<>();
    private Map<Commit, List<Link>> commits = new HashMap<>();
    private Map<File, List<Link>> files = new HashMap<>();

    private Map<Author, Author> authorsTmp = new HashMap<>();
    private Map<Commit, Commit> commitsTmp = new HashMap<>();
    private Map<File, File> filesTmp = new HashMap<>();

    //Order for links: filename, commit date. This fact can be used for optimizations.
    private List<Link> links = new ArrayList<>();

    private Date buildDate;

    public DataRepository(Date buildDate) {
        this.buildDate = buildDate;
    }

    public Date getBuildDate() {
        return buildDate;
    }

    public Map<Author, List<Link>> getAuthors() {
        return authors;
    }

    public Map<Commit, List<Link>> getCommits() {
        return commits;
    }

    public Map<File, List<Link>> getFiles() {
        return files;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void addLink(Link link) {
        links.add(link);
        addEntity(link.getAuthor(), link, authors, authorsTmp);
        addEntity(link.getCommit(), link, commits, commitsTmp);
        addEntity(link.getFile(), link, files, filesTmp);
    }

    public synchronized DataRepository mergeFrom(DataRepository other) {
        mergeEntity(authors, other.authors, authorsTmp);
        mergeEntity(commits, other.commits, commitsTmp);
        mergeEntity(files, other.files, filesTmp);
        links.addAll(other.links);
        return this;
    }

    public void updateFiles() {
        Set<String> moduleNames = files.keySet().stream().map(File::generateModuleName).collect(Collectors.toSet());
        moduleNames.remove(File.ROOT_MODULE);
        files.keySet().forEach(f -> {
            f.setVCSFile(Configuration.INSTANCE.BUILD_FILE_MARKERS.stream().filter(f.getPath()::endsWith).findFirst().isPresent());
            f.updateModuleName(moduleNames);
        });
    }

    public void updateAuthors(Properties teams, Properties locations) {
        authors.keySet().forEach(a -> {
            if (a.getEmail() != null) {
                a.setTeam((String) teams.get(a.getEmail()));
                a.setLocation((String) locations.get(a.getEmail()));
            }
        });
    }

    public void arrangeRepository() {
        links.forEach(link -> {
            link.setAuthor(authorsTmp.get(link.getAuthor()));
            link.setCommit(commitsTmp.get(link.getCommit()));
            link.setFile(filesTmp.get(link.getFile()));
        });
        authorsTmp.clear();
        commitsTmp.clear();
        filesTmp.clear();
    }

    public Author findAuthor(String author) {
        author = author.trim();
        int firstBracketIndex = author.indexOf('(');
        int lastBracketIndex = author.lastIndexOf(')');
        String email = firstBracketIndex + 1 == lastBracketIndex ? null : author.substring(firstBracketIndex + 1, lastBracketIndex).trim();
        String name = firstBracketIndex == 0 ? null : author.substring(0, firstBracketIndex).trim();
        return authors.keySet().stream().filter(a -> compare(a::getEmail, email) && compare(a::getName, name)).findFirst().orElse(null);
    }

    private boolean compare(Supplier<String> supplier, String valueToCompare) {
        String value = supplier.get();
        if (!isEmpty(valueToCompare)) {
            return !isEmpty(value) && value.equals(valueToCompare);
        } else {
            return isEmpty(value);
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }

    private static <E> void addEntity(E entity, Link link, Map<E, List<Link>> map, Map<E, E> tmpMap) {
        List<Link> links = map.get(entity);
        if (links == null) {
            links = new ArrayList<>();
            map.put(entity, links);
            tmpMap.put(entity, entity);
        }
        links.add(link);
    }

    private static <E> void mergeEntity(Map<E, List<Link>> map1, Map<E, List<Link>> map2, Map<E, E> tmpMap) {
        map2.entrySet().forEach(entry -> {
            List<Link> originalList = map1.get(entry.getKey());
            if (originalList == null) {
                map1.put(entry.getKey(), entry.getValue());
                tmpMap.put(entry.getKey(), entry.getKey());
            } else {
                originalList.addAll(entry.getValue());
            }
        });
    }
}
