package org.ak.gitanalyzer.util;

import java.io.IOException;
import java.util.function.Function;

/**
 * Created by Andrew on 13.10.2016.
 */
@FunctionalInterface
public interface ThrowingFunction<T,R> extends Function<T,R> {

    @Override
    default R apply(T t) {
        try {
            return applyThrows(t);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    R applyThrows(T t) throws IOException;
}
