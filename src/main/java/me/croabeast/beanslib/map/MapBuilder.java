package me.croabeast.beanslib.map;

import java.util.*;
import java.util.stream.Collectors;

public class MapBuilder<A, B> {

    private final List<Entry<A, B>> entries = new LinkedList<>();

    public MapBuilder() {}

    public MapBuilder(Map<? extends A, ? extends B> map) {
        if (map != null)
            map.forEach((k, v) -> entries.add(new Entry<>(k, v)));
    }

    public MapBuilder(Collection<Entry<? extends A,? extends B>> collection) {
        if (collection != null)
            collection.forEach(e -> entries.add(new Entry<>(e)));
    }

    public MapBuilder<A, B> put(A key, B value) {
        entries.add(new Entry<>(key, value));
        return this;
    }

    public List<A> keys() {
        return entries.stream().map(Entry::getKey).collect(Collectors.toList());
    }

    public List<B> values() {
        return entries.stream().map(Entry::getValue).collect(Collectors.toList());
    }

    public List<Entry<A, B>> entries(){
        return entries.stream().
                map(e -> {
                    B v = e.getValue();
                    A k = e.getKey();

                    return new Entry<>(k, v);
                }).
                collect(Collectors.toList());
    }

    public Map<A, B> map() {
        Map<A, B> map = new LinkedHashMap<>();

        entries.forEach(e ->
                map.put(e.getKey(), e.getValue()));

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
}
