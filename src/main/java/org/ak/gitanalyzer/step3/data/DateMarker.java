package org.ak.gitanalyzer.step3.data;

import java.text.DateFormatSymbols;

/**
 * Created by Andrew on 19.10.2016.
 */
public class DateMarker implements Comparable<DateMarker> {
    private static String[] MONTHS = DateFormatSymbols.getInstance().getShortMonths();
    private int year;
    private int month;

    public DateMarker(int year, int month) {
        this.year = year;
        this.month = month;
    }

    public DateMarker previous() {
        return month == 0 ? new DateMarker(year - 1, 11) : new DateMarker(year, month - 1);
    }

    public DateMarker next() {
        return month == 11 ? new DateMarker(year + 1, 0) : new DateMarker(year, month + 1);
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    @Override
    public String toString() {
        return MONTHS[month] + " " + year;
    }

    @Override
    public int compareTo(DateMarker o) {
        return 12 * (year - o.year) + (month - o.month);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateMarker that = (DateMarker) o;

        if (year != that.year) return false;
        return month == that.month;

    }

    @Override
    public int hashCode() {
        int result = year;
        result = 31 * result + month;
        return result;
    }
}
