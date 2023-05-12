package me.croabeast.iridiumapi.pattern;

import lombok.var;
import me.croabeast.iridiumapi.IridiumAPI;
import org.apache.commons.lang.StringUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * The simple gradient class.
 *
 * @author CroaBeast
 * @since 1.0
 */
public final class Gradient implements RGBParser {

    static final String HEX = "[\\da-f]{6}";

    static final Pattern LEGACY_GRADIENT =
            Pattern.compile("(?i)<G:(" + HEX + ")>(.+?)</G:(" + HEX + ")>");

    static String gradientFormat(String string, boolean isRegex, boolean isEnding) {
        return "<" + (isEnding ? "/" : "") + "#" +
                (isRegex ? "(" : "") + string + (isRegex ? ")" : "") + ">";
    }

    static String gradientFormat(String string, boolean isRegex) {
        return gradientFormat(string, isRegex, false);
    }

    static String gradientFormat() {
        return gradientFormat(HEX, true);
    }

    /**
     * Converts the {@link #LEGACY_GRADIENT} to the new {@link #GRADIENT_PATTERN}.
     * @param string an input string
     * @return the converted string
     */
    public static String convertLegacy(String string) {
        if (StringUtils.isBlank(string)) return string;

        var match = LEGACY_GRADIENT.matcher(string);

        while (match.find()) {
            String start = match.group(1), end = match.group(3);

            string = string.replace(match.group(),
                    gradientFormat(start, false) +
                            match.group(2) +
                            gradientFormat(end, false, true)
            );
        }

        return string;
    }

    /**
     * Compiles the gradient pattern. Supports multiple gradients in one format.
     */
    public static final Pattern GRADIENT_PATTERN =
            Pattern.compile("(?i)" + gradientFormat() + "(.+?)" + gradientFormat(HEX, true, true));

    private Color getColor(String line) {
        return new Color(Integer.parseInt(line, 16));
    }

    @Override
    public String process(String string, boolean useRGB) {
        string = convertLegacy(string);

        var match = GRADIENT_PATTERN.matcher(string);

        while (match.find()) {
            String x = match.group(1), text = match.group(2),
                    z = match.group(3), r = "(?i)" + gradientFormat();

            var insideMatch = Pattern.compile(r).matcher(text);
            var array = text.split(r);

            var ids = new ArrayList<String>();

            ids.add(x);
            while (insideMatch.find()) ids.add(insideMatch.group(1));

            ids.add(z);

            var result = new StringBuilder();
            int i = 0;

            while (i < ids.size() - 1) {
                result.append(IridiumAPI.color(
                        array[i],
                        getColor(ids.get(i)),
                        getColor(ids.get(i + 1)),
                        useRGB)
                );
                i++;
            }

            string = string.replace(match.group(), result + "");
        }

        return string;
    }
}
