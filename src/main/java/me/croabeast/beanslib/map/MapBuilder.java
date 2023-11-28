package me.croabeast.beanslib.map;

import me.croabeast.beanslib.misc.CollectionOperator;

import java.util.*;

public class MapBuilder<X, Y> {

    private final List<Entry<X, Y>> entries = new LinkedList<>();

    public MapBuilder() {}

    public MapBuilder(Map<? extends X, ? extends Y> map) {
        if (map != null)
            map.entrySet().forEach(e -> entries.add(Entry.of(e)));
    }

    public MapBuilder(Collection<Entry<? extends X, ? extends Y>> collection) {
        if (collection != null)
            collection.forEach(e -> entries.add(Entry.of(e)));
    }

    public MapBuilder<X, Y> put(X key, Y value) {
        entries.add(Entry.of(key, value));
        return this;
    }

    public MapBuilder<X, Y> put(Entry<? extends X, ? extends Y> entry) {
        entries.add(Entry.of(entry));
        return this;
    }

    public MapBuilder<X, Y> put(Map.Entry<? extends X, ? extends Y> entry) {
        entries.add(Entry.of(entry));
        return this;
    }

    public List<X> keys() {
        return CollectionOperator.of(entries).map(Entry::getKey).toList();
    }

    public List<Y> values() {
        return CollectionOperator.of(entries).map(Entry::getValue).toList();
    }

    public List<Entry<X, Y>> entries() {
        return new LinkedList<>(entries);
    }

    public Map<X, Y> map() {
        Map<X, Y> map = new LinkedHashMap<>();

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
