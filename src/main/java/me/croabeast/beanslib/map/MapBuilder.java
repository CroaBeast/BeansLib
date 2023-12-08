package me.croabeast.beanslib.map;

import me.croabeast.beanslib.misc.CollectionBuilder;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class MapBuilder<K, V> {

    private final List<Entry<K, V>> entries = new LinkedList<>();

    public MapBuilder() {}

    public MapBuilder(Map<? extends K, ? extends V> map) {
        if (map != null)
            map.entrySet().forEach(e -> entries.add(Entry.of(e)));
    }

    public MapBuilder(Collection<Entry<? extends K, ? extends V>> collection) {
        if (collection != null)
            collection.forEach(e -> entries.add(Entry.of(e)));
    }

    public MapBuilder<K, V> put(K key, V value) {
        entries.add(Entry.of(key, value));
        return this;
    }

    public MapBuilder<K, V> put(Entry<? extends K, ? extends V> entry) {
        entries.add(Entry.of(entry));
        return this;
    }

    public MapBuilder<K, V> put(Map.Entry<? extends K, ? extends V> entry) {
        entries.add(Entry.of(entry));
        return this;
    }

    public MapBuilder<K, V> putAll(Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
        return this;
    }

    public MapBuilder<K, V> removeByKey(K key) {
        for (Entry<K, V> entry : entries) {
            if (!Objects.equals(entry.getKey(), key))
                continue;

            entries.remove(entry);
            break;
        }
        return this;
    }

    public MapBuilder<K, V> removeByValue(V value) {
        for (Entry<K, V> entry : entries) {
            if (!Objects.equals(entry.getValue(), value))
                continue;

            entries.remove(entry);
            break;
        }
        return this;
    }

    public MapBuilder<K, V> filterByKey(Predicate<K> predicate) {
        Objects.requireNonNull(predicate);

        entries.removeIf(e -> predicate.negate().test(e.getKey()));
        return this;
    }

    public MapBuilder<K, V> filterByValue(Predicate<V> predicate) {
        Objects.requireNonNull(predicate);

        entries.removeIf(e -> predicate.negate().test(e.getValue()));
        return this;
    }

    public <A> MapBuilder<A, V> applyByKey(Function<K, A> function) {
        Objects.requireNonNull(function);
        List<Entry<A, V>> entries = new LinkedList<>();

        for (Entry<K, V> entry : this.entries) {
            A key = function.apply(entry.getKey());
            V value = entry.getValue();

            entries.add(Entry.of(key, value));
        }

        return new MapBuilder<>(entries);
    }

    public <B> MapBuilder<K, B> applyByValue(Function<V, B> function) {
        Objects.requireNonNull(function);
        List<Entry<K, B>> entries = new LinkedList<>();

        for (Entry<K, V> entry : this.entries) {
            K key = entry.getKey();
            B value = function.apply(entry.getValue());

            entries.add(Entry.of(key, value));
        }

        return new MapBuilder<>(entries);
    }

    public <A, B> MapBuilder<A, B> map(Function<K, A> keyFunction, Function<V, B> valueFunction) {
        Objects.requireNonNull(valueFunction);
        Objects.requireNonNull(keyFunction);

        List<Entry<A, B>> entries = new LinkedList<>();

        for (Entry<K, V> entry : this.entries)
            entries.add(Entry.of(
                    keyFunction.apply(entry.getKey()),
                    valueFunction.apply(entry.getValue())
            ));

        return new MapBuilder<>(entries);
    }

    public boolean containsKey(K key) {
        for (Entry<K, V> entry : entries)
            if (Objects.equals(entry.getKey(), key))
                return true;

        return false;
    }

    public boolean containsValue(V value) {
        for (Entry<K, V> entry : entries)
            if (Objects.equals(entry.getValue(), value))
                return true;

        return false;
    }

    public V get(K key, V def) {
        V value = null;

        for (Entry<K, V> entry : entries) {
            if (!Objects.equals(entry.getKey(), key))
                continue;

            value = entry.getValue();
            break;
        }

        return value == null ? def : value;
    }

    public V get(K key) {
        return get(key, null);
    }

    public void clear() {
        entries.clear();
    }

    public int size() {
        return entries.size();
    }

    public CollectionBuilder<K> keyCollector() {
        return CollectionBuilder.of(entries).map(Entry::getKey);
    }

    public List<K> keys() {
        return keyCollector().toList();
    }

    public CollectionBuilder<V> valueCollector() {
        return CollectionBuilder.of(entries).map(Entry::getValue);
    }

    public List<V> values() {
        return valueCollector().toList();
    }

    public CollectionBuilder<Entry<K, V>> entryCollector() {
        return CollectionBuilder.of(entries);
    }

    public List<Entry<K, V>> entries() {
        return new LinkedList<>(entries);
    }

    public Map<K, V> toMap() {
        Map<K, V> map = new LinkedHashMap<>();

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

    public static <A, B> boolean isEmpty(MapBuilder<A, B> builder) {
        return builder == null || builder.isEmpty();
    }
}
