package me.croabeast.beanslib.map;

import java.util.Map;

public interface UnaryEntry<T> extends Entry<T, T> {

    static <T> UnaryEntry<T> of(T key, T value) {
        return (UnaryEntry<T>) Entry.of(key, value);
    }

    static <T> UnaryEntry<T> of(Entry<? extends T, ? extends T> entry) {
        return of(entry.getKey(), entry.getValue());
    }

    static <T> UnaryEntry<T> of(Map.Entry<? extends T, ? extends T> entry) {
        return of(entry.getKey(), entry.getValue());
    }
}
