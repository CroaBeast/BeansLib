package me.croabeast.beanslib.utility;

import lombok.experimental.UtilityClass;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    @NotNull
    public <T> List<T> fromArray(UnaryOperator<T> operator, T... array) {
        if (isArrayEmpty(array))
            throw new IllegalArgumentException("Array should be declared.");

        List<T> list = new ArrayList<>();

        for (T element : array)
            list.add(operator != null ? operator.apply(element) : element);

        return list;
    }

    @SafeVarargs
    @NotNull
    public <T> List<T> fromArray(T... array) {
        return fromArray(null, array);
    }
}
