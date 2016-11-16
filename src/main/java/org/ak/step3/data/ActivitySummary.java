package org.ak.step3.data;

import java.util.*;

/**
 * Created by Andrew on 19.10.2016.
 */
public class ActivitySummary<T> {
    private T entity;
    private Map<DateMarker, Double> commitsCount = new HashMap<>();

    public ActivitySummary(T entity) {
        this.entity = entity;
    }

    public T getEntity() {
        return entity;
    }

    public List<DateMarker> getDateMarkers() {
        List<DateMarker> result = new ArrayList<>(commitsCount.keySet());
        if (result.size() == 1) {
            result.add(result.get(0).next());
            result.add(0, result.get(0).previous());
        } else {
            result.sort(DateMarker::compareTo);
        }
        return result;
    }

    public Double get(DateMarker key) {
        return commitsCount.get(key);
    }

    public Double put(DateMarker key, Double value) {
        return commitsCount.put(key, value);
    }

    public static <TNew, TOld> ActivitySummary<TNew> transform(ActivitySummary<TOld> originalSummary, TNew entity) {
        ActivitySummary<TNew> result = new ActivitySummary<>(entity);
        result.commitsCount.putAll(originalSummary.commitsCount);
        return result;
    }

    public static <TNew> void merge(ActivitySummary<TNew> to, ActivitySummary<TNew> from) {
        from.commitsCount.entrySet().forEach(entry -> {
            DateMarker entity = entry.getKey();
            Double previousValue = to.get(entity);
            to.commitsCount.put(entity, previousValue == null ? entry.getValue() : previousValue + entry.getValue());
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActivitySummary<?> that = (ActivitySummary<?>) o;

        return entity.equals(that.entity);

    }

    @Override
    public int hashCode() {
        return entity.hashCode();
    }
}
