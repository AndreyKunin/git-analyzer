package org.ak.gitanalyzer.http.processor;

import org.ak.gitanalyzer.step3.data.ActivitySummary;
import org.ak.gitanalyzer.step3.data.AgeStatistics;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static org.ak.gitanalyzer.step3.data.AgeStatistics.MS_IN_DAY;

/**
 * Created by Andrew on 20.10.2016.
 */
public class TimelineProcessor extends BaseAnalysisProcessor {

    public TimelineProcessor(NumberFormat nf) {
        super(nf);
    }

    public String getAgeSeparator(List<AgeStatistics> ages) {
        long maxY = 0, maxX = 0;
        for (AgeStatistics age : ages) {
            if (age.getLastMinusFirst() > maxX) {
                maxX = age.getLastMinusFirst();
            }
            if (age.getNowMinusLast() > maxY) {
                maxY = age.getNowMinusLast();
            }
        }
        maxX /= MS_IN_DAY; maxY /= MS_IN_DAY;
        long secondPoint = Math.min(maxX, maxY);
        List<long[]> points = new ArrayList<>();
        points.add(new long[] {0, 0});
        points.add(new long[] {secondPoint, secondPoint});
        if (maxX > secondPoint) {
            points.add(new long[] {maxX, secondPoint});
        }
        return getJSONFor2D(points, (point, result) -> htmlWriter.appendScatterPoint(result, point[0], point[1]));
    }

    public String getAgeComparison(List<AgeStatistics> _ages) {
        List<AgeStatistics> ages = new ArrayList<>(_ages);
        ages.sort(new AgeStatistics.LastMinusFirstComparator());
        return getJSONFor2D(ages, (age, result) -> htmlWriter.appendScatterPoint(result, age.getLastMinusFirst() / MS_IN_DAY, age.getNowMinusLast() / MS_IN_DAY));
    }

    public String getAges(List<AgeStatistics> _ages) {
        List<AgeStatistics> ages = new ArrayList<>(_ages);
        ages.sort(new AgeStatistics.StabilityComparator());
        return getJSONForTable(ages, (age, result) -> {
            htmlWriter.appendString(result, "age", age.intervalToString(age.getAge()));
            htmlWriter.appendString(result, "interval", age.intervalToString(age.getNowMinusLast()));
            htmlWriter.appendDouble(result, "stability", age.getStabilityPercent());
            htmlWriter.appendString(result, "path", age.getFile().getPath(), true);
        });
    }

    public String getAgesCSV(List<AgeStatistics> _ages) {
        List<AgeStatistics> ages = new ArrayList<>(_ages);
        ages.sort(new AgeStatistics.StabilityComparator());
        return getCSVForReport(new String[] {"Age", "Time from last change", "Stability, %", "Path", }, ages, (age, result) -> {
            csvWriter.appendString(result, age.intervalToString(age.getAge()));
            csvWriter.appendString(result, age.intervalToString(age.getNowMinusLast()));
            csvWriter.appendDouble(result, age.getStabilityPercent());
            csvWriter.appendString(result, age.getFile().getPath(), true);
        });
    }

    public String getActivity(ActivitySummary<?> activity) {
        return "[[" + getActivityLabels(activity) + "],[" + getActivityValues(activity) + "]]";
    }

    public String getActivityLabels(ActivitySummary<?> activity) {
        return getJSONFor2D(activity.getDateMarkers(), (date, result) -> htmlWriter.appendChartLabel(result, date.toString()));
    }

    public String getActivityValues(ActivitySummary<?> activity) {
        return getJSONFor2D(activity.getDateMarkers(), (date, result) -> htmlWriter.appendChartEntry(result, activity.get(date) == null ? 0.0 : activity.get(date)));
    }
}
