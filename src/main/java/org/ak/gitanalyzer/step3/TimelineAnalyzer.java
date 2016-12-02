package org.ak.gitanalyzer.step3;

import org.ak.gitanalyzer.step2.data.Author;
import org.ak.gitanalyzer.step2.data.DataRepository;
import org.ak.gitanalyzer.step2.data.File;
import org.ak.gitanalyzer.step2.data.Link;
import org.ak.gitanalyzer.step3.data.ActivitySummary;
import org.ak.gitanalyzer.step3.data.AgeStatistics;
import org.ak.gitanalyzer.step3.data.DateMarker;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by Andrew on 08.10.2016.
 */
public class TimelineAnalyzer extends Analyzer {

    public List<AgeStatistics> analyzeFilesAge(DataRepository dataRepository) {
        Map<File, List<Link>> files = dataRepository.getFiles();
        Long currentTime = new Date().getTime();
        return collectTimelineStatistics(files, currentTime);
    }

    public List<AgeStatistics> analyzeModulesAge(DataRepository dataRepository) {
        Map<File, List<Link>> linksByModules = new HashMap<>();
        Map<File, List<Link>> files = dataRepository.getFiles();
        files.entrySet().forEach(entry -> {
            File module = new File(entry.getKey().getModuleName());
            List<Link> moduleLinks = linksByModules.get(module);
            if (moduleLinks == null) {
                moduleLinks = new ArrayList<>();
                linksByModules.put(module, moduleLinks);
            }
            moduleLinks.addAll(entry.getValue());
        });
        Long currentTime = new Date().getTime();
        return collectTimelineStatistics(linksByModules, currentTime);
    }

    public List<ActivitySummary<Author>> analyzeAuthorsActivity(DataRepository dataRepository) {
        Supplier<Map<Author, List<Link>>> supplier = dataRepository::getAuthors;
        return analyzeActivity(supplier);
    }

    public List<ActivitySummary<String>> analyzeTeamsActivity(List<ActivitySummary<Author>> authorActivitySummary) {
        return groupActivity(authorActivitySummary, Author::getTeam);
    }

    public ActivitySummary<String> analyzeProjectActivity(List<ActivitySummary<Author>> authorActivitySummary) {
        return groupActivity(authorActivitySummary, author -> "").get(0);
    }

    private List<AgeStatistics> collectTimelineStatistics(Map<File, List<Link>> files, long currentTime) {
        return files.entrySet().stream().map(entry -> calculate(entry.getKey(), entry.getValue(), currentTime)).filter(ts -> ts != null).collect(Collectors.toList());
    }

    private <T> List<ActivitySummary<T>> analyzeActivity(Supplier<Map<T, List<Link>>> supplier) {
        List<ActivitySummary<T>> result = new ArrayList<>();

        supplier.get().entrySet().forEach(entry -> {
            T key = entry.getKey();
            ActivitySummary<T> activitySummary = new ActivitySummary<>(key);
            List<Link> links = entry.getValue().stream().filter(this::filter).collect(Collectors.toList());
            if (links.size() > 0) {
                links.sort(new AgeStatistics.DateComparator());
                links.forEach(link -> {
                    Date linkTime = link.getCommit().getDateTime();
                    int year = linkTime.getYear() + 1900;
                    int month = linkTime.getMonth();
                    DateMarker dateMarker = new DateMarker(year, month);
                    Double currentWeight = activitySummary.get(dateMarker);
                    if (currentWeight == null) {
                        currentWeight = 0.0;
                    }
                    activitySummary.put(dateMarker, currentWeight + link.getWeight());
                });
                result.add(activitySummary);
            }
        });
        return result;
    }

    private  <TNew, TOld> List<ActivitySummary<TNew>> groupActivity(List<ActivitySummary<TOld>> activitySummary, Function<TOld, TNew> transform) {
        Map<TNew, ActivitySummary<TNew>> newEntityMap = new HashMap<>();
        activitySummary.forEach(summary -> {
            TNew newEntity = transform.apply(summary.getEntity());
            if (newEntity != null) {
                ActivitySummary<TNew> value = ActivitySummary.transform(summary, newEntity);
                ActivitySummary<TNew> previousValue = newEntityMap.get(newEntity);
                if (previousValue == null) {
                    newEntityMap.put(newEntity, value);
                } else {
                    ActivitySummary.merge(previousValue, value);
                }
            }
        });
        return new ArrayList<>(newEntityMap.values());
    }

    private AgeStatistics calculate(File file, List<Link> _links, long currentTime) {
        List<Link> links = _links.stream().filter(this::filter).collect(Collectors.toList());
        if (links.size() == 0) {
            return null;
        }
        links.sort(new AgeStatistics.DateComparator());

        long firstCommitTime = links.get(0).getCommit().getDateTime().getTime();
        long lastCommitTime = links.get(links.size() - 1).getCommit().getDateTime().getTime();
        long nowMinusLast = currentTime - lastCommitTime;

        return new AgeStatistics(file, lastCommitTime - firstCommitTime, nowMinusLast);
    }


}
