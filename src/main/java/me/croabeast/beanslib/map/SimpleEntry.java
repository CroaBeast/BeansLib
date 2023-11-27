package me.croabeast.beanslib.map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
class SimpleEntry<A, B> implements Entry<A, B> {

    protected final A key;
    protected final B value;

    public String toString() {
        return key + ":" + value;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entry)) return false;

        Entry<?, ?> e = (Entry<?, ?>) o;

        return Objects.equals(key, e.getKey()) &&
                Objects.equals(value, e.getValue());
    }

    public int hashCode() {
        return Objects.hash(key, value);
    }
}
