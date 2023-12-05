package me.croabeast.beanslib.applier;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

class PriorityApplier implements StringApplier {

    final Map<Priority, Set<UnaryOperator<String>>> os = new HashMap<>();
    private final String string;

    PriorityApplier(String string) {
        this.string = Objects.requireNonNull(string);
    }

    @NotNull
    public PriorityApplier apply(Priority priority, UnaryOperator<String> operator) {
        priority = priority == null ? Priority.NORMAL : priority;
        Objects.requireNonNull(operator);

        Set<UnaryOperator<String>> set = os.getOrDefault(priority, new LinkedHashSet<>());
        set.add(operator);

        os.put(priority, set);
        return this;
    }

    @NotNull
    public PriorityApplier apply(UnaryOperator<String> operator) {
        return apply(null, operator);
    }

    @Override
    public String toString() {
        Comparator<Priority> sort = Comparator.reverseOrder();
        SimpleApplier applier = new SimpleApplier(string);

        Map<Priority, Set<UnaryOperator<String>>> result = new TreeMap<>(sort);
        result.putAll(os);

        result.forEach((p, s) -> s.forEach(applier::apply));
        return applier.toString();
    }
}
