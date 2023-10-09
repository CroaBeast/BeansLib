package me.croabeast.beanslib.key;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.croabeast.beanslib.utility.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class that replaces a given key with a given value in a given string.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValueReplacer {

    private final String key, value;
    private boolean sensitive = false;

    private String replace(String string) {
        if (StringUtils.isBlank(string)) return string;
        if (StringUtils.isBlank(key)) return string;

        String temp = sensitive ? "" : "(?i)";
        temp = temp + Pattern.quote(key);

        Matcher m = Pattern.compile(temp).matcher(string);

        if (m.find()) {
            String v = StringUtils.isBlank(value) ? "" : value;
            string = string.replace(m.group(), v);
        }

        return string;
    }

    /**
     * Replaces the key with the value in the given input string.
     *
     * @param key the key to be replaced
     * @param value the value to replace the key with
     * @param input the input string to be replaced
     * @param b the flag indicating whether the replacement is case-sensitive or not
     *
     * @return the replaced string
     */
    public static String of(String key, String value, String input, boolean b) {
        ValueReplacer replacer = new ValueReplacer(key, value);
        if (b) replacer.sensitive = true;

        return replacer.replace(input);
    }

    /**
     * Replaces the key with the value in the given input string. The replacement is
     * case-insensitive by default.
     *
     * @param key the key to be replaced
     * @param value the value to replace the key with
     * @param input the input string to be replaced
     *
     * @return the replaced string
     */
    public static String of(String key, String value, String input) {
        return new ValueReplacer(key, value).replace(input);
    }

    @SuppressWarnings("all")
    public static <A, B> boolean isApplicable(A[] as, B[] bs) {
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
    public static String forEach(String[] keys, String[] values, String string, boolean b) {
        if (StringUtils.isBlank(string)) return string;
        if (!isApplicable(keys, values)) return string;

        for (int i = 0; i < keys.length; i++)
            string = ValueReplacer.of(keys[i], values[i], string, b);

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
    public static String forEach(String[] keys, String[] values, String string) {
        return forEach(keys, values, string, false);
    }
}
