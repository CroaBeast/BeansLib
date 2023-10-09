package me.croabeast.beanslib.misc;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * The StringApplier class is a useful class that provides methods to apply
 * different operations on a single string.
 */
public final class StringApplier {

    private String string;

    private StringApplier(String string) {
        this.string = Objects.requireNonNull(string);
    }

    /**
     * Applies a given function to the current string. The function
     * should take a string as input and return a string as output.
     *
     * @param function the function to be applied to the current string
     * @return a reference of this object
     */
    public StringApplier apply(UnaryOperator<String> function) {
        string = function.apply(string);
        return this;
    }

    /**
     * Returns the current string. If there is any function that was
     * called using {@link #apply(UnaryOperator)}, will be applied
     * already to the current string.
     *
     * @return the current string
     */
    @Override
    public String toString() {
        return string;
    }

    public static StringApplier of(String string) {
        return new StringApplier(string);
    }

    public static StringApplier of(StringApplier applier) {
        return of(applier.toString());
    }
}
