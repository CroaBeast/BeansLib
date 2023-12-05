package me.croabeast.beanslib.utility;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
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

        Class<?> clazz = array.getClass().getComponentType();
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
     * Converts an array of elements into a collection.
     *
     * @param collection the collection to which the elements will be added.
     *                  Must not be null.
     * @param function the function to apply to each element of the array.
     *                  If null, the identity function is used.
     * @param array the array of elements to be converted. If empty, the collection
     *                  is returned unchanged.
     *
     * @param <T> the type of the elements
     * @param <I> the type of the collection
     *
     * @return the collection with the elements of the array added after applying the function.
     * @throws NullPointerException if the collection is null.
     */
    @SafeVarargs
    public <T, I extends Collection<T>> I toCollection(I collection, UnaryOperator<T> function, T... array) {
        Objects.requireNonNull(collection);
        if (function == null) function = o -> o;

        if (isArrayEmpty(array)) return collection;

        for (T o : array)
            collection.add(function.apply(o));

        return collection;
    }

    /**
     * Converts an array of elements into a collection.
     *
     * @param collection the collection to which the elements will be added.
     *                  Must not be null.
     * @param array the array of elements to be converted. If empty, the collection
     *                  is returned unchanged.
     *
     * @param <T> the type of the elements
     * @param <I> the type of the collection
     *
     * @return the collection with the elements of the array added.
     * @throws NullPointerException if the collection is null.
     */
    @SafeVarargs
    public <T, I extends Collection<T>> I toCollection(I collection, T... array) {
        return toCollection(collection, null, array);
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
    public <T> List<T> toList(UnaryOperator<T> operator, T... array) {
        return toCollection(new ArrayList<>(), operator, array);
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
    public <T> List<T> toList(T... array) {
        return toCollection(new ArrayList<>(), array);
    }

    /**
     * Converts an iterable to an array.
     *
     * @param iterable the iterable to be converted
     * @param <T> the type of the elements in the iterable and the array
     *
     * @return a new array that contains the elements from the iterable
     * @throws NullPointerException if the iterable is null or empty
     */
    public <T> T[] toArray(Iterable<? extends T> iterable) {
        if (iterable == null || !iterable.iterator().hasNext())
            throw new NullPointerException();

        final List<T> list = new ArrayList<>();
        iterable.forEach(list::add);

        Class<?> clazz = list.get(0).getClass();
        T[] array = (T[]) Array.newInstance(clazz, list.size());

        return list.toArray(array);
    }
}
