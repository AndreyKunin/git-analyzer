package org.ak.gitanalyzer.step3.data;

import org.ak.gitanalyzer.step2.data.File;
import org.ak.gitanalyzer.step2.data.Link;

import java.util.Comparator;

/**
 * Created by Andrew on 09.10.2016.
 */
public class AgeStatistics {
    private File file;
    private long lastMinusFirst;
    private long nowMinusLast;

    public static final long MS_IN_DAY = 24 * 60 * 60 * 1000L;

    public AgeStatistics(File file, long lastMinusFirst, long nowMinusLast) {
        this.file = file;
        this.lastMinusFirst = lastMinusFirst;
        this.nowMinusLast = nowMinusLast;
    }

    public File getFile() {
        return file;
    }

    public long getLastMinusFirst() {
        return lastMinusFirst;
    }

    public long getNowMinusLast() {
        return nowMinusLast;
    }

    public long getAge() {
        return lastMinusFirst + nowMinusLast;
    }

    public double getStabilityPercent() {
        return (double) nowMinusLast / (double) (lastMinusFirst + nowMinusLast) * 100.0;
    }

    public String intervalToString(long interval) {
        StringBuilder result = new StringBuilder();
        Interval[] intervals = Interval.values();
        long[] parts = new long[intervals.length];
        for (int i = 0; i < intervals.length && interval > 0; ++i) {
            parts[i] = interval / intervals[i].k;
            interval %= intervals[i].k;
        }
        for (int i = 0; i < intervals.length; ++i) {
            if (parts[i] > 0) {
                result.append(parts[i]).append(" ").append(intervals[i].getName(parts[i]));
                if (i < intervals.length - 1 && parts[i + 1] != 0) {
                    result.append(", ").append(parts[i + 1]).append(" ").append(intervals[i + 1].getName(parts[i + 1]));
                }
                return result.toString();
            }
        }
        return "Less than a minute";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgeStatistics that = (AgeStatistics) o;

        return file.equals(that.file);

    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    public static class LastMinusFirstComparator implements Comparator<AgeStatistics> {
        @Override
        public int compare(AgeStatistics o1, AgeStatistics o2) {
            return Long.compare(o1.lastMinusFirst, o2.lastMinusFirst);
        }
    }

    public static class StabilityComparator implements Comparator<AgeStatistics> {
        @Override
        public int compare(AgeStatistics o1, AgeStatistics o2) {
            return Double.compare(o1.getStabilityPercent(), o2.getStabilityPercent());
        }
    }

    public static class DateComparator implements Comparator<Link> {
        @Override
        public int compare(Link o1, Link o2) {
            return o1.getCommit().getDateTime().compareTo(o2.getCommit().getDateTime());
        }
    }

    private enum Interval {
        years(365 * MS_IN_DAY), months(30 * MS_IN_DAY), days(MS_IN_DAY), hours(MS_IN_DAY / 24), minutes(MS_IN_DAY / 24 / 60);

        long k;

        Interval(long k) {
            this.k = k;
        }

        String getName(long value) {
            return value == 1 ? name().substring(0, name().length() - 1) : name();
        }
    }

}
