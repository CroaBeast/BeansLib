package me.croabeast.beanslib.utility;

import lombok.experimental.UtilityClass;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.UnaryOperator;

@UtilityClass
public class ArrayUtils {

    /**
     * Combines an array with one or more additional arrays into a new
     * array of the same type.
     *
     * @author Kihsomray
     * @since 1.3
     *
     * @param array First array
     * @param extraArrays Any additional arrays
     * @param <T> Type of array (must be same)
     *
     * @return New array of combined values
     */
    @SafeVarargs
    public <T> T[] combineArrays(@NotNull T[] array, T[]... extraArrays) {
        if (extraArrays == null || extraArrays.length < 1)
            return array;

        List<T> resultList = new ArrayList<>();
        Collections.addAll(resultList, array);

        for (T[] a : extraArrays)
            if (a != null) Collections.addAll(resultList, a);

        var clazz = array.getClass().getComponentType();
        T[] resultArray = (T[]) Array.newInstance(clazz, 0);

        return resultList.toArray(resultArray);
    }

    @SafeVarargs
    public <T> boolean isArrayEmpty(T... array) {
        return array == null || array.length < 1;
    }

    @SafeVarargs
    public <T> T[] checkArray(T... array) {
        if (isArrayEmpty(array))
            throw new IllegalArgumentException("Array should be declared.");

        return array;
    }

    @SafeVarargs
    @NotNull
    public <T> List<T> fromArray(UnaryOperator<T> operator, T... array) {
        List<T> list = new ArrayList<>();

        for (T element : checkArray(array))
            list.add(operator != null ? operator.apply(element) : element);

        return list;
    }

    @SafeVarargs
    @NotNull
    public <T> List<T> fromArray(T... array) {
        return fromArray(null, array);
    }

    public <T> T[] toArray(Iterable<T> iterable) {
        Objects.requireNonNull(iterable);

        Iterator<T> iterator = iterable.iterator();
        List<T> list = new ArrayList<>();

        while (iterator.hasNext())
            list.add(iterator.next());

        return list.toArray((T[]) new Object[0]);
    }
}
