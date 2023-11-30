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
 */
public final class CollectionOperator<T> {

    private final Collection<T> collection;

    private CollectionOperator(Collection<T> collection) {
        this.collection = collection;
    }

    /**
     * Filters the collection by a predicate and returns this operator.
     *
     * @param predicate the predicate to test each element
     *
     * @return a reference of this operator
     * @throws NullPointerException if predicate is null
     */
    public CollectionOperator<T> filter(Predicate<T> predicate) {
        collection.removeIf(Objects.requireNonNull(predicate).negate());
        return this;
    }

    /**
     * Adds all the elements from another collection to this collection.
     *
     * @param elements the collection of elements to add
     *
     * @return a reference of this operator
     * @throws NullPointerException if elements is null
     */
    public CollectionOperator<T> add(Collection<? extends T> elements) {
        collection.addAll(elements);
        return this;
    }

    /**
     * Adds all the elements from an iterator to this collection.
     *
     * @param elements the iterator of elements to add
     *
     * @return a reference of this operator
     * @throws NullPointerException if elements is null
     */
    public CollectionOperator<T> add(Iterator<? extends T> elements) {
        Objects.requireNonNull(elements);

        List<T> collection = new LinkedList<>();
        while (elements.hasNext())
            collection.add(elements.next());

        return add(collection);
    }

    /**
     * Adds all the elements from an iterable to this collection.
     *
     * @param elements the iterable of elements to add
     *
     * @return a reference of this operator
     * @throws NullPointerException if elements is null
     */
    public CollectionOperator<T> add(Iterable<? extends T> elements) {
        List<T> collection = new LinkedList<>();
        Objects.requireNonNull(elements).forEach(collection::add);
        return add(collection);
    }

    /**
     * Adds all the elements from an enumeration to this collection.
     *
     * @param elements the enumeration of elements to add
     *
     * @return a reference of this operator
     * @throws NullPointerException if elements is null
     */
    public CollectionOperator<T> add(Enumeration<? extends T> elements) {
        Objects.requireNonNull(elements);
        List<T> collection = new LinkedList<>();

        while (elements.hasMoreElements())
            collection.add(elements.nextElement());
        return add(collection);
    }

    /**
     * Adds all the elements from an array to this collection.
     *
     * @param elements the array of elements to add
     * @return a reference of this operator
     */
    @SafeVarargs
    public final CollectionOperator<T> add(T... elements) {
        return add(ArrayUtils.toList(elements));
    }

    /**
     * Applies a consumer to this collection.
     *
     * @param consumer the consumer to accept the collection
     *
     * @return a reference of this operator
     * @throws NullPointerException if consumer is null
     */
    public CollectionOperator<T> modify(Consumer<Collection<T>> consumer) {
        Objects.requireNonNull(consumer).accept(collection);
        return this;
    }

    /**
     * Sorts the collection by a comparator and returns a new operator with a sorted list.
     *
     * @param comparator the comparator to compare the elements
     *
     * @return a new operator with a sorted list
     * @throws NullPointerException if comparator is null
     */
    public CollectionOperator<T> sort(Comparator<? extends T> comparator) {
        Objects.requireNonNull(comparator);

        List<T> list = new ArrayList<>(collection);
        list.sort((Comparator<T>) comparator);

        return new CollectionOperator<>(new LinkedList<>(list));
    }

    /**
     * Applies a unary operator to each element of the collection and returns a new
     * operator with a modified list.
     *
     * @param operator the unary operator to apply to each element
     *
     * @return a new operator with a modified list
     * @throws NullPointerException if operator is null
     */
    public CollectionOperator<T> apply(UnaryOperator<T> operator) {
        List<T> list = new LinkedList<>(collection);
        list.replaceAll(operator);
        return new CollectionOperator<>(list);
    }

    /**
     * Maps each element of the collection to another type and returns a new
     * operator with a mapped list.
     *
     * @param function the function to map each element
     * @param <U> the type of the mapped elements
     *
     * @return a new operator with a mapped list
     * @throws NullPointerException if function is null
     */
    public <U> CollectionOperator<U> map(Function<T, U> function) {
        Objects.requireNonNull(function);

        List<U> list = new LinkedList<>();
        collection.forEach(t -> list.add(function.apply(t)));

        return new CollectionOperator<>(list);
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
     * Collects the elements of the collection into a new instance of a given
     * collection class.
     *
     * @param clazz the class of the collection to create
     * @param <C> the type of the collection to create
     *
     * @return a new instance of the collection class with the elements of this collection
     *
     * @throws NullPointerException if clazz is null
     * @throws IllegalStateException if the collection class cannot be instantiated
     */
    public <C extends Collection<T>> C collect(Class<C> clazz) {
        C collection = newInstance(clazz);
        collection.addAll(this.collection);
        return collection;
    }

    /**
     * Collects the elements of the collection into a given collection.
     *
     * @param collection the collection to add the elements to
     * @param <C> the type of the collection
     *
     * @return the given collection with the elements of this collection
     * @throws NullPointerException if collection is null
     */
    public <C extends Collection<T>> C collect(C collection) {
        Objects.requireNonNull(collection);

        collection.addAll(this.collection);
        return collection;
    }

    /**
     * Converts the collection to a list.
     * @return a list with the elements of the collection
     */
    public List<T> toList() {
        return new ArrayList<>(collection);
    }

    /**
     * Converts the collection to a set.
     * @return a set with the elements of the collection
     */
    public Set<T> toSet() {
        return new HashSet<>(collection);
    }

    /**
     * Converts the collection to a queue.
     * @return a queue with the elements of the collection
     */
    public Queue<T> toQueue() {
        return new LinkedList<>(collection);
    }

    /**
     * Converts the collection to an iterator.
     * @return an iterator with the elements of the collection
     */
    public Iterator<T> toIterator() {
        return collection.iterator();
    }

    /**
     * Converts the collection to an enumeration.
     * @return an enumeration with the elements of the collection
     */
    public Enumeration<T> toEnumeration() {
        return Collections.enumeration(collection);
    }

    public static <T> CollectionOperator<T> of(Collection<T> collection) {
        return new CollectionOperator<>(Objects.requireNonNull(collection));
    }

    public static <T> CollectionOperator<T> of(Iterator<T> iterator) {
        Objects.requireNonNull(iterator);

        List<T> collection = new LinkedList<>();
        while (iterator.hasNext())
            collection.add(iterator.next());

        return of(collection);
    }

    public static <T> CollectionOperator<T> of(Iterable<T> iterable) {
        Objects.requireNonNull(iterable);

        List<T> collection = new LinkedList<>();
        iterable.forEach(collection::add);

        return of(collection);
    }

    public static <T> CollectionOperator<T> of(Enumeration<T> enumeration) {
        Objects.requireNonNull(enumeration);

        List<T> collection = new LinkedList<>();
        while (enumeration.hasMoreElements())
            collection.add(enumeration.nextElement());

        return of(collection);
    }

    @SafeVarargs
    public static <T> CollectionOperator<T> of(T... elements) {
        return of(ArrayUtils.toList(elements));
    }
}
