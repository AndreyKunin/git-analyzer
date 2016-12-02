package org.ak.gitanalyzer.step2;

import org.ak.gitanalyzer.step1.data.RawCommit;
import org.ak.gitanalyzer.step1.data.RawCommitComparator;
import org.ak.gitanalyzer.step1.data.RawFile;
import org.ak.gitanalyzer.step1.data.RawRepository;
import org.ak.gitanalyzer.step2.data.*;
import org.ak.gitanalyzer.util.Configuration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Andrew on 01.10.2016.
 */
public class RepositoryBuilder {

    private final static RawCommitComparator RC_COMPARATOR = new RawCommitComparator();

    public DataRepository build(RawRepository rawRepository) {
        final String jiraPrefix = Configuration.INSTANCE.getString("GIT.jira.prefix");
        final int batchSize = Configuration.INSTANCE.getInt("GIT.read.batch.size", 1024);
        Date buildDate = rawRepository.getBuildDate();
        List<List<RawFile>> splittedList = partition(rawRepository.getRawFiles(), batchSize);
        Optional<DataRepository> result = splittedList.parallelStream()
                .map(fileList -> new SublistBuilder(buildDate).setJiraPrefix(jiraPrefix).setFileList(fileList).build())
                .reduce(DataRepository::mergeFrom);
        if (result.isPresent()) {
            result.get().updateFiles();
            result.get().updateAuthors(Configuration.INSTANCE.getTeams(), Configuration.INSTANCE.getLocations());
            result.get().arrangeRepository();
            return result.get();
        }
        return null;
    }

    private List<List<RawFile>> partition(ArrayList<RawFile> list, int batchSize) {
        List<List<RawFile>> splittedList = new ArrayList<>();
        int actualSize = list.size();
        for (int i = 0, j = batchSize; i < actualSize; i += batchSize, j += batchSize) {
            splittedList.add(list.subList(Math.min(i, actualSize), Math.min(j, actualSize)));
        }
        return splittedList;
    }

    private class SublistBuilder {
        private List<RawFile> fileList;
        private String jiraPrefix;
        private Date buildDate;

        private SublistBuilder(Date buildDate) {
            this.buildDate = buildDate;
        }

        private SublistBuilder setFileList(List<RawFile> fileList) {
            this.fileList = fileList;
            return this;
        }

        private SublistBuilder setJiraPrefix(String jiraPrefix) {
            this.jiraPrefix = jiraPrefix;
            return this;
        }

        private DataRepository build() {
            DataRepository dataRepository = new DataRepository(buildDate);
            FileBuilder builder = new FileBuilder(this)
                    .setDataRepository(dataRepository);
            fileList.forEach(file -> builder.setRawFile(file).build());
            return dataRepository;
        }
    }

    private class FileBuilder {
        private DataRepository dataRepository;
        private RawFile rawFile;

        private SublistBuilder parent;

        private FileBuilder(SublistBuilder parent) {
            this.parent = parent;
        }

        private FileBuilder setDataRepository(DataRepository dataRepository) {
            this.dataRepository = dataRepository;
            return this;
        }

        private FileBuilder setRawFile(RawFile rawFile) {
            this.rawFile = rawFile;
            return this;
        }

        private void build() {
            Map<Integer, List<Link>> weightedLinks = new HashMap<>();
            File file = new File(rawFile.getPath());
            CommitBuilder builder = new CommitBuilder(this)
                    .setWeightedLinks(weightedLinks)
                    .setFile(file);
            if (rawFile.getCommits() != null) {
                filter(rawFile.getCommits()).forEach(commit -> builder.setRawCommit(commit).build());
            }

            weightedLinks.values().forEach(linkList ->
                    linkList.forEach(link -> link.setWeight(0.0))
            );

            weightedLinks.values().forEach(linkList -> {
                double addedValue = 1.0 / linkList.size();
                linkList.forEach(link -> link.setWeight(link.getWeight() + addedValue));
            });
        }

        private List<RawCommit> filter(List<RawCommit> commits) {
            Map<Long, RawCommit> groups = new HashMap<>();
            for (RawCommit commit : commits) {
                long noiseHashCode = noiseHashCode(commit);
                RawCommit existingCommit = groups.get(noiseHashCode);
                if (existingCommit == null || RC_COMPARATOR.compare(commit, existingCommit) < 0) {
                    groups.put(noiseHashCode, commit);
                }
            }
            Collection<RawCommit> values = groups.values();
            return values.size() != commits.size() ? new ArrayList<>(values) : commits;
        }

        private long noiseHashCode(RawCommit commit) {
            return commit.getCommitDateTime().getTime();
        }

    }

    private class CommitBuilder {
        private Map<Integer, List<Link>> weightedLinks;
        private File file;
        private RawCommit rawCommit;

        private FileBuilder parent;

        private CommitBuilder(FileBuilder parent) {
            this.parent = parent;
        }

        private CommitBuilder setWeightedLinks(Map<Integer, List<Link>> weightedLinks) {
            this.weightedLinks = weightedLinks;
            return this;
        }

        private CommitBuilder setFile(File file) {
            this.file = file;
            return this;
        }

        private CommitBuilder setRawCommit(RawCommit rawCommit) {
            this.rawCommit = rawCommit;
            return this;
        }

        private void build() {
            String jiraPrefix = parent.parent.jiraPrefix;
            DataRepository dataRepository = parent.dataRepository;

            Author author = new Author(
                    Configuration.INSTANCE.getAuthorEmail(rawCommit.getAuthorEmail()),
                    Configuration.INSTANCE.getAuthorName(rawCommit.getAuthorName())
            );
            Commit commit = new Commit(rawCommit.getHash(), rawCommit.getCommitDateTime(), rawCommit.getComment());
            if (isServiceAuthor(author) || isServiceCommit(commit)) {
                return;
            }

            Link link = new Link(author, commit, file);
            groupLink(weightedLinks, jiraPrefix, link);
            dataRepository.addLink(link);
        }

        private boolean isServiceCommit(Commit commit) {
            String comment = commit.getComment();
            return comment != null && Configuration.INSTANCE.SERVICE_COMMIT_MARKERS.stream().filter(comment::contains).findFirst().isPresent();
        }

        private boolean isServiceAuthor(Author author) {
            String name = author.getName();
            return name != null && Configuration.INSTANCE.SERVICE_AUTHOR_MARKERS.stream().filter(name::contains).findFirst().isPresent();
        }

        private void groupLink(Map<Integer, List<Link>> weightedLinks, String jiraPrefix, Link link) {
            List<Integer> weightHashCodes = weightHashCodes(link, jiraPrefix);
            if (weightHashCodes.size() == 0) {
                return;
            }
            weightHashCodes.forEach(weightHashCode -> {
                List<Link> sameLinks = weightedLinks.get(weightHashCode);
                if (sameLinks == null) {
                    sameLinks = new ArrayList<>();
                    weightedLinks.put(weightHashCode, sameLinks);
                }
                sameLinks.add(link);
            });
        }

        private List<Integer> weightHashCodes(Link link, String jiraPrefix) {
            if (jiraPrefix == null) {
                return Collections.emptyList();
            }
            Collection<String> jiraRefs = null;
            if (link.getCommit().getComment() != null) {
                jiraRefs = getJiraReferences(link.getCommit().getComment(), jiraPrefix);
            }
            if (jiraRefs != null && jiraRefs.size() > 0) {
                return jiraRefs.stream()
                        .map(jiraRef -> (jiraRef + '|' + link.getAuthor().getName() + '|' + link.getAuthor().getEmail()).hashCode())
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        private Collection<String> getJiraReferences(String value, String jiraPrefix) {
            if (value == null) {
                return Collections.emptySet();
            }
            Set<String> result = new HashSet<>();
            int jiraRefIndex = 0;
            while (jiraRefIndex != -1) {
                jiraRefIndex = value.indexOf(jiraPrefix, jiraRefIndex);
                StringBuilder sb = new StringBuilder();
                if (jiraRefIndex != -1) {
                    for (int i = jiraRefIndex + jiraPrefix.length(); i < value.length(); ++i) {
                        char c = value.charAt(i);
                        if (c >= '0' && c <= '9') {
                            sb.append(c);
                        } else {
                            break;
                        }
                    }
                    jiraRefIndex += jiraPrefix.length();
                    if (sb.length() > 0) {
                        jiraRefIndex += sb.length();
                        result.add(sb.insert(0, jiraPrefix).toString());
                    }
                }
            }
            return result;
        }
    }

}
