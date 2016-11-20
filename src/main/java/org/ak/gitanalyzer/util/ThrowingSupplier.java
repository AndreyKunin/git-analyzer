package org.ak.gitanalyzer.util;

import java.util.function.Supplier;

/**
 * Created by Andrew on 17.11.2016.
 */
@FunctionalInterface
public interface ThrowingSupplier<T> extends Supplier<T> {

    @Override
    default T get() {
        try {
            return getThrows();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    T getThrows() throws Exception;
}
