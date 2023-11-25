package me.croabeast.beanslib.map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Objects;

/**
 * A class that represents a pair of values with a key and a value.
 * The key and the value can be of any type, and are immutable.
 *
 * @param <A> the type of the key
 * @param <B> the type of the value
 */
@RequiredArgsConstructor
@Getter
public class Entry<A, B> {

    /**
     * The key corresponding to this entry.
     */
    protected final A key;
    /**
     * The value corresponding to this entry.
     */
    protected final B value;

    /**
     * Constructs a new Entry object from another Entry object.
     * @param entry the Entry object to copy from
     */
    public Entry(Entry<? extends A, ? extends B> entry) {
        this(entry.key, entry.value);
    }

    /**
     * Constructs a new Entry object from a Map.Entry object.
     * @param entry the Map.Entry object to copy from
     */
    public Entry(Map.Entry<? extends A, ? extends B> entry) {
        this(entry.getKey(), entry.getValue());
    }

    /**
     * Returns a string representation of this Entry object using
     * the key and value of this object.
     *
     * @return a string representation of the Entry object
     */
    @Override
    public String toString() {
        return key + ":" + value;
    }

    /**
     * Compares the specified object with this Entry object for equality.
     *
     * <p> Returns true if and only if the specified object is also an Entry object,
     * and both the keys and the values are equal.
     *
     * @param o the object to be compared for equality with this Entry object
     * @return true if the specified object is equal to this Entry object
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entry)) return false;

        Entry<?, ?> e = (Entry<?, ?>) o;

        return Objects.equals(key, e.key) &&
                Objects.equals(value, e.value);
    }

    /**
     * Returns the hash code value for this Entry object.
     * The hash code is computed based on the key and the value fields.
     *
     * @return the hash code value for this Entry object
     */
    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
