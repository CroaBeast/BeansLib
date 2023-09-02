package me.croabeast.beanslib.key;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code ValueReplacer} class manages the replacement of a key with
 * a given value in an input string, using the {@link #replace(String)} method.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ValueReplacer {

    private final String key, value;

    /**
     * If the key is case-sensitive or not, {@code false} by default.
     */
    @Accessors(chain = true)
    @Setter
    private boolean sensitive = false;

    /**
     * Replaces the key with the defined value on an input string.
     *
     * @param string an input string
     * @return the formatted string
     */
    String replace(String string) {
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

    public static String of(String key, String value, String input, boolean b) {
        return new ValueReplacer(key, value).setSensitive(b).replace(input);
    }

    public static String of(String key, String value, String input) {
        return new ValueReplacer(key, value).replace(input);
    }

    /**
     * Replace an array of string keys with an array of string values in a string.
     *
     * <p> All the keys are quoted to avoid replacing errors in the string.
     *
     * @param string an input string
     * @param keys an array of keys
     * @param values an array of values
     * @param b if keys are case-sensitive
     *
     * @return the string with the parsed values
     */
    public static String forEach(String[] keys, String[] values, String string, boolean b) {
        if (StringUtils.isBlank(string)) return string;

        if (keys == null || values == null) return string;
        if (keys.length > values.length) return string;

        for (int i = 0; i < keys.length; i++)
            string = ValueReplacer.of(keys[i], values[i], string, b);

        return string;
    }

    /**
     * Replace an array of string keys that are case-insensitive with an
     * array of string values in a string.
     *
     * <p> All the keys are quoted to avoid replacing errors in the string.
     *
     * @param keys an array of keys
     * @param values an array of values
     * @param string an input string
     *
     * @return the string with the parsed values
     */
    public static String forEach(String[] keys, String[] values, String string) {
        return forEach(keys, values, string, false);
    }
}
