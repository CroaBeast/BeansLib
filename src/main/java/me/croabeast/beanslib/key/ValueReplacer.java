package me.croabeast.beanslib.key;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.var;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * The {@code ValueReplacer} class manages the replacement of a key with
 * a given value in an input string, using the {@link #replace(String)} method.
 */
@RequiredArgsConstructor(staticName = "of")
public class ValueReplacer {

    private final String key;
    private final String value;

    /**
     * If the key is case-sensitive or not, {@code true} by default.
     */
    @Accessors(chain = true)
    @Setter
    private boolean caseSensitive = true;

    /**
     * Replaces the key with the defined value on an input string.
     *
     * @param string an input string
     * @return the formatted string
     */
    public String replace(String string) {
        if (StringUtils.isBlank(string)) return string;
        if (StringUtils.isBlank(key)) return string;

        var temp = caseSensitive ? "" : "(?i)";
        temp = temp + Pattern.quote(key);

        var m = Pattern.compile(temp).matcher(string);

        if (m.find()) {
            var v = StringUtils.isBlank(value) ? "" : value;
            string = string.replace(m.group(), v);
        }

        return string;
    }

    /**
     * Replace an array of string keys with an array of string values in a string.
     *
     * <p> All the keys are quoted to avoid replacing errors in the string.
     *
     * @param string an input string
     * @param keys an array of keys
     * @param values an array of values
     * @param isSensitive if keys are case-sensitive
     *
     * @return the string with the parsed values
     */
    public static String forEach(String string, String[] keys, String[] values, boolean isSensitive) {
        if (StringUtils.isBlank(string)) return string;

        if (keys == null || values == null) return string;
        if (keys.length > values.length) return string;

        for (int i = 0; i < keys.length; i++) {
            var v = ValueReplacer.of(keys[i], values[i]);
            string = v.setCaseSensitive(isSensitive).replace(string);
        }

        return string;
    }

    /**
     * Replace an array of string keys that are case-insensitive with an
     * array of string values in a string.
     *
     * <p> All the keys are quoted to avoid replacing errors in the string.
     *
     * @param string an input string
     * @param keys an array of keys
     * @param values an array of values
     *
     * @return the string with the parsed values
     */
    public static String forEach(String string, String[] keys, String[] values) {
        return forEach(string, keys, values, false);
    }
}
