package me.croabeast.beanslib.map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
@Getter
public class Entry<A, B> {

    protected final A key;
    protected final B value;

    public Entry(Entry<? extends A, ? extends B> entry) {
        key = entry.key;
        value = entry.value;
    }

    @Override
    public String toString() {
        return key + ":" + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entry)) return false;

        Entry<?, ?> e = (Entry<?, ?>) o;

        return Objects.equals(key, e.key) &&
                Objects.equals(value, e.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
