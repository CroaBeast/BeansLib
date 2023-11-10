package me.croabeast.beanslib.utility;

import lombok.experimental.UtilityClass;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A utility class that provides some methods for working with arrays.
 * This class uses generics to handle different types of arrays.
 */
@UtilityClass
public class ArrayUtils {

    /**
     * Combines an array with one or more additional arrays into a new
     * array of the same type.
     *
     * @param array the first array to be combined
     * @param extraArrays the other arrays to be combined
     * @param <T> the type of the elements in the arrays
     *
     * @author Kihsomray
     * @since 1.3
     *
     * @return a new array that contains all the elements from the given arrays
     */
    @SafeVarargs
    public <T> T[] combineArrays(@NotNull T[] array, T[]... extraArrays) {
        if (isArrayEmpty(extraArrays)) return array;

        List<T> resultList = new ArrayList<>();
        Collections.addAll(resultList, array);

        for (T[] a : extraArrays)
            if (a != null) Collections.addAll(resultList, a);

        var clazz = array.getClass().getComponentType();
        T[] resultArray = (T[]) Array.newInstance(clazz, 0);

        return resultList.toArray(resultArray);
    }

    /**
     * Checks if an array is empty or null.
     *
     * @param array the array to be checked
     * @param <T> the type of the elements in the array
     *
     * @return true if the array is empty or null, false otherwise
     */
    @SafeVarargs
    public <T> boolean isArrayEmpty(T... array) {
        return array == null || array.length < 1;
    }

    /**
     * Checks if an array is declared and throws an exception if not.
     *
     * @param array the array to be checked
     * @param <T> the type of the elements in the array
     *
     * @return the same array if it is declared
     * @throws IllegalArgumentException if the array is null or has zero length
     */
    @SafeVarargs
    public <T> T[] checkArray(T... array) {
        if (isArrayEmpty(array))
            throw new IllegalArgumentException("Array should be declared");

        return array;
    }

    /**
     * Converts an array to a list and applies an optional operator to each element.
     *
     * @param operator the operator to be applied to each element, can be null
     * @param check if true will throw an exception when the array is null or empty
     * @param array the array to be converted
     * @param <T> the type of the elements in the array and the list
     *
     * @return a new list that contains the elements from the array after applying the operator
     * @throws IllegalArgumentException if the array is null or has zero length
     */
    @SafeVarargs
    @NotNull
    public <T> List<T> fromArray(UnaryOperator<T> operator, boolean check, T... array) {
        final List<T> list = new ArrayList<>();

        if (!check && isArrayEmpty(array))
            return list;

        for (T element : checkArray(array))
            list.add(operator != null ? operator.apply(element) : element);

        return list;
    }

    /**
     * Converts an array to a list and applies an optional operator to each element.
     *
     * @param operator the operator to be applied to each element, can be null
     * @param array the array to be converted
     * @param <T> the type of the elements in the array and the list
     *
     * @return a new list that contains the elements from the array after applying the operator
     */
    @SafeVarargs
    @NotNull
    public <T> List<T> fromArray(UnaryOperator<T> operator, T... array) {
        return fromArray(operator, false, array);
    }

    /**
     * Converts an array to a list.
     *
     * @param check if true will throw an exception when the array is null or empty
     * @param array the array to be converted
     * @param <T> the type of the elements in the array and the list
     *
     * @return a new list that contains the same elements as the array
     * @throws IllegalArgumentException if the array is null or has zero length
     */
    @SafeVarargs
    @NotNull
    public <T> List<T> fromArray(boolean check, T... array) {
        return fromArray(null, check, array);
    }

    /**
     * Converts an array to a list.
     *
     * @param array the array to be converted
     * @param <T> the type of the elements in the array and the list
     *
     * @return a new list that contains the same elements as the array
     */
    @SafeVarargs
    @NotNull
    public <T> List<T> fromArray(T... array) {
        return fromArray(null, array);
    }

    @SafeVarargs
    @NotNull
    public <T, R> List<R> fromArray(Function<T, ? extends R> function, T... array) {
        List<R> list = new ArrayList<>();

        Objects.requireNonNull(function);

        for (T element : checkArray(array))
            list.add(function.apply(element));

        return list;
    }

    /**
     * Converts an iterable to an array.
     *
     * @param iterable the iterable to be converted
     * @param <T> the type of the elements in the iterable and the array
     *
     * @return a new array that contains the elements from the iterable
     * @throws NullPointerException if the iterable is null
     */
    public <T> T[] toArray(Iterable<? extends T> iterable) {
        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list.toArray((T[]) new Object[0]);
    }
}
