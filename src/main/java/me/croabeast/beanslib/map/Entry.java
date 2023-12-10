package me.croabeast.beanslib.map;

import java.util.Map;
import java.util.Objects;

/**
 * A class that represents a pair of values with a key and a value.
 * The key and the value can be of any type, and are immutable.
 *
 * @param <A> the type of the key
 * @param <B> the type of the value
 */
public interface Entry<A, B> {

    /**
     * Returns the key corresponding to this entry.
     * @return the key
     */
    A getKey();

    /**
     * Returns the value corresponding to this entry.
     * @return the value
     */
    B getValue();

    /**
     * Returns a string representation of this Entry object using
     * the key and value of this object.
     *
     * @return a string representation of the Entry object
     */
    String toString();

    /**
     * Returns the hash code value for this Entry object.
     * The hash code is computed based on the key and the value fields.
     *
     * @return the hash code value for this Entry object
     */
    int hashCode();

    /**
     * Compares the specified object with this Entry object for equality.
     *
     * <p> Returns true if and only if the specified object is also an Entry object,
     * and both the keys and the values are equal.
     *
     * @param o the object to be compared for equality with this Entry object
     * @return true if the specified object is equal to this Entry object
     */
    boolean equals(Object o);

    static <A, B> Entry<A, B> of(A key, B value) {
        return new SimpleEntry<>(key, value);
    }

    static <A, B> Entry<A, B> of(Entry<? extends A, ? extends B> entry) {
        Objects.requireNonNull(entry);
        return of(entry.getKey(), entry.getValue());
    }

    static <A, B> Entry<A, B> of(Map.Entry<? extends A, ? extends B> entry) {
        Objects.requireNonNull(entry);
        return of(entry.getKey(), entry.getValue());
    }
}
