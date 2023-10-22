package me.croabeast.beanslib.applier;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Represents an object that can apply multiple string operators to a single string.
 *
 * <p> Each operator is applied in the order that they were called, if there is no
 * defined priority to those operators.
 */
public interface StringApplier {

    /**
     * Applies the operator with the defined priority if the applier allows
     * prioritized operators.
     *
     * <p> If prioritized operators are not allowed, the operator will apply
     * directly to the current string.
     *
     * @param priority the priority
     * @param operator the operator
     *
     * @throws NullPointerException if the priority or the operator is null
     * @return a reference of this applier
     */
    @NotNull
    StringApplier apply(Priority priority, UnaryOperator<String> operator);

    /**
     * Applies the operator directly to the current string if the applier does
     * not allow prioritized operators.
     *
     * <p> If prioritized operators are allowed, it will use {@link Priority#NORMAL}.
     *
     * @param operator the operator
     *
     * @throws NullPointerException if the operator is null
     * @return a reference of this applier
     */
    @NotNull
    StringApplier apply(UnaryOperator<String> operator);

    /**
     * Returns the string after all the operators were applied.
     *
     * @return the applied string
     */
    String toString();

    /**
     * Creates a new applier without prioritization in all its operators.
     *
     * <p> Each call of the {@link #apply(Priority, UnaryOperator)} and {@link #apply(UnaryOperator)}
     * methods, the operators are applied  directly to the string without any priority.
     *
     * @param string a string, can be empty/blank
     *
     * @throws NullPointerException if the string is null
     * @return a new non-prioritized applier
     */
    static StringApplier simplified(String string) {
        return new SimpleApplier(string);
    }

    /**
     * Creates a new applier without prioritization in all its operators.
     *
     * <p> Each call of the {@link #apply(Priority, UnaryOperator)} and {@link #apply(UnaryOperator)}
     * methods, the operators are applied  directly to the string without any priority.
     *
     * @param applier an applier
     *
     * @throws NullPointerException if the applier is null
     * @return a new non-prioritized applier
     */
    static StringApplier simplified(StringApplier applier) {
        return simplified(Objects.requireNonNull(applier).toString());
    }

    /**
     * Creates a new applier with prioritization for each operator that is being
     * added to the applier.
     *
     * <p> The {@link #apply(Priority, UnaryOperator)} method add the operator with the
     * defined priority, and the {@link #apply(UnaryOperator)} method add the operator
     * with the {@link Priority#NORMAL} priority.
     *
     * @param string a string, can be empty/blank
     *
     * @throws NullPointerException if the string is null
     * @return a new prioritized applier
     */
    static StringApplier prioritized(String string) {
        return new PriorityApplier(string);
    }

    /**
     * Creates a new applier with prioritization for each operator that is being
     * added to the applier.
     *
     * <p> The {@link #apply(Priority, UnaryOperator)} method add the operator with the
     * defined priority, and the {@link #apply(UnaryOperator)} method add the operator
     * with the {@link Priority#NORMAL} priority.
     *
     * @param applier an applier
     *
     * @throws NullPointerException if the applier is null
     * @return a new prioritized applier
     */
    static StringApplier prioritized(StringApplier applier) {
        return prioritized(Objects.requireNonNull(applier).toString());
    }

    /**
     * An enum that represents the priority of an operator used in {@link StringApplier}.
     */
    enum Priority {
        /**
         * Operators with this priority will be called last.
         */
        LOWEST,
        /**
         * Operators have a low importance to be applied.
         */
        LOW,
        /**
         * Operators are neither important nor unimportant.
         */
        NORMAL,
        /**
         * Operators have a high importance to be applied.
         */
        HIGH,
        /**
         * Operators with this priority will be called first.
         */
        HIGHEST
    }
}
