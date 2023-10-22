package me.croabeast.beanslib.applier;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.UnaryOperator;

class SimpleApplier implements StringApplier {

    private String string;

    SimpleApplier(String string) {
        this.string = Objects.requireNonNull(string);
    }

    @NotNull
    public SimpleApplier apply(Priority priority, UnaryOperator<String> operator) {
        return apply(operator);
    }

    @NotNull
    public SimpleApplier apply(UnaryOperator<String> operator) {
        string = Objects.requireNonNull(operator).apply(string);
        return this;
    }

    @Override
    public String toString() {
        return string;
    }
}
