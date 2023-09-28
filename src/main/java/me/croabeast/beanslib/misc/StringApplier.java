package me.croabeast.beanslib.misc;

import org.apache.commons.lang.StringUtils;

import java.util.function.UnaryOperator;

public final class StringApplier {

    private String string;

    private StringApplier(String string) {
        if (string == null)
            throw new NullPointerException();

        this.string = string;
    }

    public StringApplier apply(UnaryOperator<String> function) {
        string = function.apply(string);
        return this;
    }

    @Override
    public String toString() {
        return string;
    }

    public static StringApplier of(String string) {
        return new StringApplier(string);
    }

    public static StringApplier of(StringApplier applier) {
        return new StringApplier(applier.toString());
    }
}
