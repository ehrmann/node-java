package com.davidehrmann.nodejava.util;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CollectorsExtra {

    public static <T, K, U>
    Collector<T, ?, NavigableMap<K,U>> toNavigableMap(Function<? super T, ? extends K> keyMapper,
                                                      Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyMapper, valueMapper, throwingMerger(), TreeMap::new);
    }

    // From OpenJDK. GPLv2 with linking exception
    private static <T> BinaryOperator<T> throwingMerger() {
        return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }
}
