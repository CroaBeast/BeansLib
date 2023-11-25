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
     * Converts an array of elements into a collection.
     *
     * @param collection the collection to which the elements will be added.
     *                  Must not be null.
     * @param function the function to apply to each element of the array.
     *                  If null, the identity function is used.
     * @param array the array of elements to be converted. If empty, the collection
     *                  is returned unchanged.
     *
     * @param <T> the type of the elements in the array and the collection.
     * @param <I> the type of the collection that extends Collection<T>.
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
     * @param array the array of elements to be converted. If empty, the
     *                  collection is returned unchanged.
     *
     * @param <T> the type of the elements in the array and the collection.
     * @param <I> the type of the collection that extends Collection<T>.
     *
     * @return the collection with the elements of the array.
     * @throws NullPointerException if the collection is null.
     */
    @SafeVarargs
    public <T, I extends Collection<T>> I toCollection(I collection, T... array) {
        return toCollection(collection, null, array);
    }

    @SafeVarargs
    public <T, U, I extends Collection<U>> I mapToCollection(I collection, Function<? extends T, U> function, T... array) {
        Objects.requireNonNull(collection);
        Objects.requireNonNull(function);

        if (isArrayEmpty(array)) return collection;

        Function<T, U> f = (Function<T, U>) function;
        for (T o : array) collection.add(f.apply(o));

        return collection;
    }

    /**
     * Applies a given function to each element of a given collection and returns a new
     * collection of the same type with the results.
     *
     * @param <T> the type of the elements in the input collection
     * @param <U> the type of the elements in the output collection
     * @param <C> the type of the input collection
     * @param <D> the type of the output collection
     *
     * @param collection the input collection to be mapped
     * @param function the function to be applied to each element
     *
     * @return a new collection of the same type as the input collection with the mapped elements
     *
     * @throws NullPointerException if the collection or the function is null
     * @throws IllegalStateException if the collection type cannot be instantiated
     */
    public static <T, U, C extends Collection<T>, D extends Collection<U>> D mapCollection(C collection, Function<T, U> function) {
        Objects.requireNonNull(collection);
        Objects.requireNonNull(function);

        Class<?> clazz = collection.getClass();

        if (clazz.getName().equals("java.util.Arrays$ArrayList"))
            clazz = ArrayList.class;

        final D result;

        try {
            result = (D) clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        for (T element : collection) result.add(function.apply(element));
        return result;
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
        return toList(null, array);
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
        Objects.requireNonNull(iterable);

        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list.toArray((T[]) new Object[0]);
    }
}
