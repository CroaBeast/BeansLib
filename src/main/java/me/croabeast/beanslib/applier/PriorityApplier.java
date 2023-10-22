package me.croabeast.beanslib.applier;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

class PriorityApplier implements StringApplier {

    final Map<Priority, Set<UnaryOperator<String>>> os = new LinkedHashMap<>();
    private final String string;

    PriorityApplier(String string) {
        this.string = Objects.requireNonNull(string);

        os.put(Priority.HIGHEST, new LinkedHashSet<>());
        os.put(Priority.HIGH, new LinkedHashSet<>());
        os.put(Priority.NORMAL, new LinkedHashSet<>());
        os.put(Priority.LOW, new LinkedHashSet<>());
        os.put(Priority.LOWEST, new LinkedHashSet<>());
    }

    @NotNull
    public PriorityApplier apply(Priority priority, UnaryOperator<String> operator) {
        Objects.requireNonNull(priority);
        Objects.requireNonNull(operator);

        Set<UnaryOperator<String>> set = os.get(priority);
        set.add(operator);

        os.put(priority, set);
        return this;
    }

    @NotNull
    public PriorityApplier apply(UnaryOperator<String> operator) {
        return apply(Priority.NORMAL, operator);
    }

    @Override
    public String toString() {
        SimpleApplier applier = new SimpleApplier(string);

        os.values().forEach(s -> {
            if (!s.isEmpty()) s.forEach(applier::apply);
        });

        return applier.toString();
    }
}
