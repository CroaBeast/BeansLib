package me.croabeast.neoprismatic.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

public class MapBuilder<A, B> {

    private final List<Entry<? extends A, ? extends B>> entries = new LinkedList<>();

    public MapBuilder() {}

    public MapBuilder(Map<? extends A, ? extends B> map) {
        if (map != null)
            map.forEach((k, v) -> entries.add(new Entry<>(k, v)));
    }

    public MapBuilder(Collection<Entry<? extends A,? extends B>> collection) {
        if (collection != null) entries.addAll(collection);
    }

    public MapBuilder<A, B> put(A key, B value) {
        entries.add(new Entry<>(key, value));
        return this;
    }

    public List<A> keys() {
        return entries.stream().map(e -> e.key).collect(Collectors.toList());
    }

    public List<B> values() {
        return entries.stream().map(e -> e.value).collect(Collectors.toList());
    }

    public List<Entry<A, B>> entries(){
        return entries.stream().
                map(e -> {
                    B v = e.value;
                    A k = e.key;

                    return new Entry<>(k, v);
                }).
                collect(Collectors.toList());
    }

    public Map<A, B> map() {
        Map<A, B> map = new LinkedHashMap<>();
        entries.forEach(e -> map.put(e.key, e.value));
        return map;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public String toString() {
        return entries.toString();
    }

    /**
     * Checks if a map builder is null or empty.
     *
     * @param builder a builder, can be null
     * @return true if builder is null or empty
     *
     * @param <A> key class
     * @param <B> value class
     */
    public static <A, B> boolean isEmpty(MapBuilder<A, B> builder) {
        return builder == null || builder.isEmpty();
    }

    /**
     * Represents a pair of key and value of a map builder.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static class Entry<A, B> {
        /**
         * The key corresponding of this entry.
         */
        private final A key;
        /**
         * The value corresponding of this entry.
         */
        private final B value;

        @Override
        public String toString() {
            return key + ":" + value;
        }
    }
}
