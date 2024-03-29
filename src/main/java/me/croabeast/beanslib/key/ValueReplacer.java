package me.croabeast.beanslib.key;

import lombok.experimental.UtilityClass;
import me.croabeast.beanslib.applier.StringApplier;
import me.croabeast.beanslib.utility.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class that provides methods for replacing placeholders with values in a string.
 * The values can be of any type, and some special cases are handled, such as CommandSender.
 */
@UtilityClass
public class ValueReplacer {

    /**
     * Replaces a single placeholder with a value in a string.
     *
     * @param key the placeholder to be replaced, must not be blank
     * @param value the value to replace the placeholder with
     * @param string the string to perform the replacement on, can be blank
     * @param sensitive a boolean flag indicating whether the placeholder
     *                 is case-sensitive or not
     *
     * @param <T> the type of the values
     * @return the modified string, or the original string if no replacement was done
     */
    public <T> String of(String key, T value, String string, boolean sensitive) {
        if (StringUtils.isBlank(string) ||
                StringUtils.isBlank(key) || value == null)
            return string;

        final String val;

        if (value instanceof CommandSender)
            val = ((CommandSender) value).getName();
        else
            val = value instanceof String ?
                    (String) value :
                    value.toString();

        String temp = Pattern.quote(key);
        if (!sensitive)
            temp = "(?i)" + temp;

        Matcher m = Pattern.compile(temp).matcher(string);

        StringApplier applier =
                StringApplier.simplified(string);

        while (m.find())
            applier.apply(s -> s.replace(m.group(), val));

        return applier.toString();
    }

    /**
     * Replaces a single placeholder with a value in a string, using case-insensitive mode.
     *
     * @param key the placeholder to be replaced, must not be blank
     * @param value the value to replace the placeholder with, can be empty
     * @param string the string to perform the replacement on, can be blank
     *
     * @param <T> the type of the values
     * @return the modified string, or the original string if no replacement was done
     */
    public <T> String of(String key, T value, String string) {
        return of(key, value, string, false);
    }

    /**
     * Checks if two arrays are applicable for replacement, i.e. they are not empty
     * and have matching lengths.
     *
     * @param as the array of placeholders
     * @param bs the array of values
     * @param <A> the type of the placeholders
     * @param <B> the type of the values
     *
     * @return true if the arrays are applicable, false otherwise
     */
    @SuppressWarnings("all")
    public <A, B> boolean isApplicable(A[] as, B[] bs) {
        return (!ArrayUtils.isArrayEmpty(as) && !ArrayUtils.isArrayEmpty(bs)) && (as.length <= bs.length);
    }

    /**
     * Replaces multiple placeholders with values in a string.
     *
     * @param keys the array of placeholders to be replaced, must not be empty
     * @param values the array of values to replace the placeholders with, must not be
     *               empty and have at least the same length as keys
     * @param string the string to perform the replacements on, can be blank
     * @param sensitive a boolean flag indicating whether the placeholders are case-sensitive
     *         or not
     *
     * @param <T> the type of the values
     * @return the modified string, or the original string if no replacements were done
     */
    public <T> String forEach(String[] keys, T[] values, String string, boolean sensitive) {
        if (StringUtils.isBlank(string) || !isApplicable(keys, values))
            return string;

        StringApplier applier = StringApplier.simplified(string);

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            final T temp = values[i];

            applier.apply(s ->
                    ValueReplacer.of(key, temp, s, sensitive));
        }

        return applier.toString();
    }

    /**
     * Replaces multiple placeholders with values in a string, using case-insensitive
     * mode.
     *
     * @param keys the array of placeholders to be replaced, must not be empty
     * @param values the array of values to replace the placeholders with, must not
     *               be empty and have at least the same length as keys
     * @param string the string to perform the replacements on, can be blank
     *
     * @param <T> the type of the values
     * @return the modified string, or the original string if no replacements were done
     */
    public <T> String forEach(String[] keys, T[] values, String string) {
        return forEach(keys, values, string, false);
    }

    /**
     * Replaces multiple placeholders with values in a string, using lists instead of
     * arrays.
     *
     * @param keys the list of placeholders to be replaced, must not be empty
     * @param values the list of values to replace the placeholders with, must not be
     *              empty and have at least the same size as keys
     * @param string the string to perform the replacements on, can be blank
     * @param sensitive a boolean flag indicating whether the placeholders are case-sensitive
     *          or not
     *
     * @param <T> the type of the values
     * @return the modified string, or the original string if no replacements were done
     */
    public <T> String forEach(Collection<String> keys, Collection<T> values, String string, boolean sensitive) {
        return forEach(ArrayUtils.toArray(keys), ArrayUtils.toArray(values), string, sensitive);
    }

    /**
     * Replaces multiple placeholders with values in a string, using lists instead of
     * arrays and case-insensitive mode.
     *
     * @param keys the list of placeholders to be replaced, must not be empty
     * @param values the list of values to replace the placeholders with, must not be
     *               empty and have at least the same size as keys
     * @param string the string to perform the replacements on, can be blank
     *
     * @param <T> the type of the values
     * @return the modified string, or the original string if no replacements were done
     */
    public <T> String forEach(Collection<String> keys, Collection<T> values, String string) {
        return forEach(ArrayUtils.toArray(keys), ArrayUtils.toArray(values), string);
    }

    /**
     * Replaces multiple placeholders with values in a string, using a map and an optional
     * function to transform the values.
     *
     * @param map the map of placeholders and values to be replaced, must not be empty
     * @param function an optional function to apply on the values before replacing them,
     *                 can be null
     * @param string the string to perform the replacements on, can be blank
     * @param sensitive a boolean flag indicating whether the placeholders are case-sensitive
     *                  or not
     *
     * @param <T> the type of the values in the map
     * @param <R> the type of the transformed values
     *
     * @return the modified string, or the original string if no replacements were done
     */
    public <T, R> String forEach(Map<String, T> map, Function<T, R> function, String string, boolean sensitive) {
        if (StringUtils.isBlank(string)) return string;
        if (map.isEmpty()) return string;

        StringApplier applier = StringApplier.simplified(string);

        for (Map.Entry<String, T> entry : map.entrySet()) {
            T first = entry.getValue();

            R value = null;
            if (function != null) value = function.apply(first);

            R result = value;

            applier.apply(s -> ValueReplacer.of(
                    entry.getKey(),
                    (result != null ? result : first).toString(),
                    s, sensitive
            ));
        }

        return applier.toString();
    }

    /**
     * Replaces multiple placeholders with values in a string, using a map and an optional
     * function to transform the values and case-insensitive mode.
     *
     * @param map the map of placeholders and values to be replaced, must not be empty
     * @param function an optional function to apply on the values before replacing them,
     *                can be null
     * @param string the string to perform the replacements on, can be blank
     *
     * @param <T> the type of the values in the map
     * @param <R> the type of the transformed values
     *
     * @return the modified string, or the original string if no replacements were done
     */
    public <T, R> String forEach(Map<String, T> map, Function<T, R> function, String string) {
        return forEach(map, function, string, false);
    }

    /**
     * Replaces multiple placeholders with values in a string, using a map and no function.
     *
     * @param map the map of placeholders and values to be replaced, must not be empty
     * @param string the string to perform the replacements on, can be blank
     * @param sensitive a boolean flag indicating whether the placeholders are case-sensitive or not
     *
     * @param <T> the type of the values in the map
     * @return the modified string, or the original string if no replacements were done
     */
    public <T> String forEach(Map<String, ? extends T> map, String string, boolean sensitive) {
        return forEach(map, null, string, sensitive);
    }

    /**
     * Replaces multiple placeholders with values in a string, using a map and no function
     * and case-insensitive mode.
     *
     * @param map the map of placeholders and values to be replaced, must not be empty
     * @param string the string to perform the replacements on, can be blank
     *
     * @param <T> the type of the values in the map
     * @return the modified string, or the original string if no replacements were done
     */
    public <T> String forEach(Map<String, ? extends T> map, String string) {
        return forEach(map, string, false);
    }
}
