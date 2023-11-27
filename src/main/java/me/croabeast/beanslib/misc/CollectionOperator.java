package me.croabeast.beanslib.misc;

import me.croabeast.beanslib.utility.ArrayUtils;

import java.util.*;
import java.util.function.*;

/**
 * A utility class that provides methods to operate on collections in a fluent and
 * functional way.
 *
 * <p> It allows to create, filter, modify, map, and collect collections of any type and
 * implementation. It also supports creating collections from iterables and enumerations.
 *
 * @param <T> the type of elements in the collection
 * @param <C> the type of collection
 */
public final class CollectionOperator<T, C extends Collection<T>> {

    private final C collection;

    private CollectionOperator(C collection) {
        this.collection = Objects.requireNonNull(collection);
    }

    private static <C extends Collection> C newInstance(Class<? extends Collection> clazz) {
        Objects.requireNonNull(clazz);

        if (clazz.getName().equals("java.util.Arrays$ArrayList"))
            clazz = ArrayList.class;

        try {
            return (C) clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Filters the elements of the collection that match the given predicate.
     *
     * @param predicate the predicate to apply to each element
     *
     * @return this collection operator
     * @throws NullPointerException if the predicate is null
     */
    public CollectionOperator<T, C> filter(Predicate<T> predicate) {
        collection.removeIf(Objects.requireNonNull(predicate).negate());
        return this;
    }

    /**
     * Adds all the elements of the given array to this collection.
     *
     * @param elements the array of elements to add
     *
     * @return this collection operator
     * @throws NullPointerException if the array of elements is null
     */
    public CollectionOperator<T, C> add(Collection<? extends T> elements) {
        collection.addAll(elements);
        return this;
    }

    /**
     * Adds all the elements of the given array to this collection.
     *
     * @param elements the array of elements to add
     *
     * @return this collection operator
     * @throws NullPointerException if the array of elements is null
     */
    public CollectionOperator<T, C> add(Iterable<? extends T> elements) {
        List<T> collection = new LinkedList<>();
        elements.forEach(collection::add);
        return add(collection);
    }

    /**
     * Adds all the elements of the given array to this collection.
     *
     * @param elements the array of elements to add
     *
     * @return this collection operator
     * @throws NullPointerException if the array of elements is null
     */
    public CollectionOperator<T, C> add(Enumeration<? extends T> elements) {
        List<T> collection = new LinkedList<>();
        while (elements.hasMoreElements())
            collection.add(elements.nextElement());
        return add(collection);
    }

    /**
     * Adds all the elements of the given array to this collection.
     *
     * @param elements the array of elements to add
     *
     * @return this collection operator
     * @throws NullPointerException if the array of elements is null
     */
    @SafeVarargs
    public final CollectionOperator<T, C> add(T... elements) {
        return add(ArrayUtils.toList(elements));
    }

    /**
     * Modifies the collection using the given consumer function.
     *
     * @param consumer the consumer function to apply to the collection
     *
     * @return this collection operator
     * @throws NullPointerException if the consumer function is null
     */
    public CollectionOperator<T, C> modify(Consumer<C> consumer) {
        Objects.requireNonNull(consumer).accept(collection);
        return this;
    }

    /**
     * Maps each element of the collection to a new element using the given unary operator.
     *
     * @param operator the unary operator to apply to each element
     *
     * @return a new collection operator with the mapped elements
     * @throws NullPointerException if the unary operator is null
     */
    public CollectionOperator<T, C> map(UnaryOperator<T> operator) {
        Objects.requireNonNull(operator);

        C result = newInstance(collection.getClass());

        for (T obj : collection)
            result.add(operator.apply(obj));

        return new CollectionOperator<>(result);
    }

    /**
     * Maps each element of the collection to a new element of a different type using
     * the given function.
     *
     * @param <U> the type of the new elements
     * @param <D> the type of the new collection
     *
     * @param function the function to apply to each element
     *
     * @return a new collection operator with the mapped elements
     *
     * @throws NullPointerException if the function is null
     * @throws IllegalStateException if the instantiation of the new collection fails
     */
    public <U, D extends Collection<U>> CollectionOperator<U, D> map(Function<T, U> function) {
        Objects.requireNonNull(function);

        D result = newInstance(collection.getClass());

        for (T obj : collection)
            result.add(function.apply(obj));

        return new CollectionOperator<>(result);
    }

    /**
     * Returns the underlying collection of this operator.
     * @return the collection
     */
    public C collect() {
        return collection;
    }

    /**
     * Returns a new collection of the given type with the same elements as this operator.
     *
     * @param <D> the type of the new collection
     * @param clazz the class of the new collection
     *
     * @return the new collection
     *
     * @throws NullPointerException if the class is null
     * @throws IllegalStateException if the instantiation of the new collection fails
     */
    public <D extends Collection<T>> D collect(Class<D> clazz) {
        D result = newInstance(clazz);
        result.addAll(collection);
        return result;
    }

    /**
     * Creates a new collection operator from the given collection.
     *
     * @param <T> the type of elements in the collection
     * @param <C> the type of collection
     *
     * @param collection the collection to wrap
     *
     * @return the collection operator
     * @throws NullPointerException if the collection is null
     */
    public static <T, C extends Collection<T>> CollectionOperator<T, C> of(C collection) {
        return new CollectionOperator<>(collection);
    }

    /**
     * Creates a new collection operator from the given iterable and the given collection type.
     *
     * @param <T> the type of elements in the iterable
     * @param <C> the type of collection
     *
     * @param iterable the iterable to wrap
     * @param clazz the class of the collection
     *
     * @return the collection operator
     * @throws NullPointerException if the iterable or the class is null
     */
    public static <T, C extends Collection<T>> CollectionOperator<T, C> of(Iterable<T> iterable, Class<C> clazz) {
        Objects.requireNonNull(iterable);

        C collection = newInstance(clazz);
        iterable.forEach(collection::add);

        return of(collection);
    }

    /**
     * Creates a new collection operator from the given iterable and a default list type.
     *
     * @param <T> the type of elements in the iterable
     * @param iterable the iterable to wrap
     *
     * @return the collection operator
     * @throws NullPointerException if the iterable is null
     */
    public static <T> CollectionOperator<T, List<T>> of(Iterable<T> iterable) {
        Objects.requireNonNull(iterable);

        List<T> collection = new LinkedList<>();
        iterable.forEach(collection::add);

        return of(collection);
    }

    /**
     * Creates a new collection operator from the given enumeration and the given collection type.
     *
     * @param <T> the type of elements in the enumeration
     * @param <C> the type of collection
     *
     * @param enumeration the enumeration to wrap
     * @param clazz the class of the collection
     *
     * @return the collection operator
     * @throws NullPointerException if the enumeration or the class is null
     */
    public static <T, C extends Collection<T>> CollectionOperator<T, C> of(Enumeration<T> enumeration, Class<C> clazz) {
        Objects.requireNonNull(enumeration);

        final C collection = newInstance(clazz);
        while (enumeration.hasMoreElements())
            collection.add(enumeration.nextElement());

        return of(collection);
    }

    /**
     * Creates a new collection operator from the given enumeration and a default list type.
     *
     * @param <T> the type of elements in the enumeration
     * @param enumeration the enumeration to wrap
     *
     * @return the collection operator
     * @throws NullPointerException if the enumeration is null
     */
    public static <T> CollectionOperator<T, List<T>> of(Enumeration<T> enumeration) {
        Objects.requireNonNull(enumeration);

        List<T> collection = new LinkedList<>();
        while (enumeration.hasMoreElements())
            collection.add(enumeration.nextElement());

        return of(collection);
    }

    /**
     * Creates a new collection operator from the given array and the given collection type.
     *
     * @param <T> the type of elements in the enumeration
     * @param <C> the type of collection
     *
     * @param clazz the class of the collection
     * @param elements the array to wrap
     *
     * @return the collection operator
     * @throws NullPointerException if the class is null
     */
    @SafeVarargs
    public static <T, C extends Collection<T>> CollectionOperator<T, C> of(Class<C> clazz, T... elements) {
        final C collection = newInstance(clazz);
        collection.addAll(ArrayUtils.toList(elements));
        return of(collection);
    }

    /**
     * Creates a new collection operator from the given array and a default list type.
     *
     * @param <T> the type of elements in the array
     * @param elements the array to wrap
     *
     * @return the collection operator
     * @throws NullPointerException if the array is null
     */
    @SafeVarargs
    public static <T> CollectionOperator<T, List<T>> of(T... elements) {
        return of(ArrayUtils.toList(elements));
    }
}
