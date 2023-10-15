package me.croabeast.beanslib.key;

import lombok.experimental.UtilityClass;
import me.croabeast.beanslib.utility.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ValueReplacer {

    /**
     * Replaces the key with the value in the given input string.
     *
     * @param key the key to be replaced
     * @param value the value to replace the key with
     * @param string the input string to be replaced
     * @param b the flag indicating whether the replacement is case-sensitive or not
     *
     * @return the replaced string
     */
    public String of(String key, String value, String string, boolean b) {
        if (StringUtils.isBlank(string)) return string;
        if (StringUtils.isBlank(key)) return string;

        String temp = b ? "" : "(?i)";
        temp = temp + Pattern.quote(key);

        Matcher m = Pattern.compile(temp).matcher(string);

        if (m.find()) {
            String v = StringUtils.isBlank(value) ? "" : value;
            string = string.replace(m.group(), v);
        }

        return string;
    }

    /**
     * Replaces the key with the value in the given input string. The replacement is
     * case-insensitive by default.
     *
     * @param key the key to be replaced
     * @param value the value to replace the key with
     * @param string the input string to be replaced
     *
     * @return the replaced string
     */
    public String of(String key, String value, String string) {
        return of(key, value, string, false);
    }

    @SuppressWarnings("all")
    public <A, B> boolean isApplicable(A[] as, B[] bs) {
        return (!ArrayUtils.isArrayEmpty(as) && !ArrayUtils.isArrayEmpty(bs)) && (as.length <= bs.length);
    }

    /**
     * Replaces each key in the keys array with the corresponding value in the values array
     * in the given string. If the keys array or values array is null, or if their lengths are
     * not equal, returns the original string.
     *
     * @param keys an array of keys to be replaced
     * @param values an array of values to replace the keys with
     * @param string the input string to be replaced
     * @param b the flag indicating whether the replacement is case-sensitive or not
     *
     * @return the replaced string
     */
    public <T> String forEach(String[] keys, T[] values, String string, boolean b) {
        if (StringUtils.isBlank(string)) return string;
        if (!isApplicable(keys, values)) return string;

        for (int i = 0; i < keys.length; i++) {
            String v = String.valueOf(values[i]);
            if (v.equals("null")) continue;

            if (values[i] instanceof CommandSender)
                v = ((CommandSender) values[i]).getName();

            string = ValueReplacer.of(keys[i], v, string, b);
        }

        return string;
    }

    /**
     * Replaces each key in the keys array with the corresponding value in the values array
     * in the given string. If the keys array or values array is null, or if their lengths are
     * not equal, returns the original string. The replacement is case-insensitive by default.
     *
     * @param keys an array of keys to be replaced
     * @param values an array of values to replace the keys with
     * @param string the input string to be replaced
     *
     * @return the replaced string
     */
    public <T> String forEach(String[] keys, T[] values, String string) {
        return forEach(keys, values, string, false);
    }

    public <T> String forEach(List<String> keys, List<T> values, String string, boolean b) {
        return forEach(ArrayUtils.toArray(keys), ArrayUtils.toArray(values), string, b);
    }

    public <T> String forEach(List<String> keys, List<T> values, String string) {
        return forEach(ArrayUtils.toArray(keys), ArrayUtils.toArray(values), string);
    }

    public <T, R> String forEach(Map<String, ? extends T> map,
                                 Function<T, ? extends R> function,
                                 String string, boolean b)
    {
        if (StringUtils.isBlank(string)) return string;
        if (map.isEmpty()) return string;

        for (Map.Entry<String, ? extends T> entry : map.entrySet()) {
            T first = entry.getValue();

            R result = null;
            if (function != null) result = function.apply(first);

            string = ValueReplacer.of(
                    entry.getKey(),
                    (result != null ? result : first).toString(),
                    string, b
            );
        }

        return string;
    }

    public <T, R> String forEach(Map<String, ? extends T> map, Function<T, ? extends R> function, String string)
    {
        return forEach(map, function, string, false);
    }

    public <T> String forEach(Map<String, ? extends T> map, String string, boolean b) {
        return forEach(map, null, string, b);
    }

    public <T> String forEach(Map<String, ? extends T> map, String string) {
        return forEach(map, string, false);
    }
}
