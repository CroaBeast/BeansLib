package me.croabeast.iridiumapi.pattern;

import me.croabeast.iridiumapi.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The simple gradient class.
 *
 * @author CroaBeast
 * @since 1.0
 */
public final class Gradient extends BasePattern {

    /**
     * The hex basic format.
     */
    static final String HEX = "[\\da-f]{6}";

    /**
     * Compiles the gradient pattern.
     * <p> Deprecated to use the new gradient format that has support for multiple gradients.
     * <p> See {@link #GRADIENT_PATTERN} for more info.
     */
    static final Pattern LEGACY_GRADIENT =
            Pattern.compile("(?i)<G:(" + HEX + ")>(.+?)</G:(" + HEX + ")>");

    /**
     * Creates a gradient format from a string.
     * @param string an input string
     * @param isRegex if the format is a regex string
     * @param isEnding if is an ending format type
     * @return the gradient format
     */
    static String gradient(String string, boolean isRegex, boolean isEnding) {
        return "<" + (isEnding ? "/" : "") + "#" +
                (isRegex ? "(" : "") + string + (isRegex ? ")" : "") + ">";
    }

    /**
     * Creates a gradient format from a string.
     * @param string an input string
     * @param isRegex if the format is a regex string
     * @return the gradient format
     */
    static String gradient(String string, boolean isRegex) {
        return gradient(string, isRegex, false);
    }

    /**
     * Creates a gradient regex format using {@link #HEX} string.
     * @return the gradient format
     */
    static String gradient() {
        return gradient(HEX, true);
    }

    /**
     * Converts the {@link #LEGACY_GRADIENT} to the new {@link #GRADIENT_PATTERN}.
     * @param string an input string
     * @return the converted string
     */
    public static String convertLegacy(String string) {
        Matcher match = LEGACY_GRADIENT.matcher(string);

        while (match.find()) {
            String start = match.group(1), end = match.group(3);
            string = string.replace(match.group(),
                    gradient(start, false) +
                            match.group(2) +
                            gradient(end, false, true)
            );
        }

        return string;
    }

    /**
     * Compiles the gradient pattern. Supports multiple gradients in one format.
     */
    public static final Pattern GRADIENT_PATTERN =
            Pattern.compile("(?i)" + gradient() + "(.+?)" + gradient(HEX, true, true));

    private Color getColor(String line) {
        return new Color(Integer.parseInt(line, 16));
    }

    @Override
    public String process(String string, boolean useRGB) {
        string = convertLegacy(string);
        Matcher match = GRADIENT_PATTERN.matcher(string);

        while (match.find()) {
            String x = match.group(1), text = match.group(2), z = match.group(3);

            Matcher insideMatch = Pattern.compile("(?i)" + gradient()).matcher(text);
            String[] array = text.split("(?i)" + gradient());

            List<String> ids = new ArrayList<>();
            while (insideMatch.find()) ids.add(insideMatch.group(1));

            StringBuilder result = new StringBuilder();

            for (int i = 0; i <= ids.size(); i++) {
                boolean canPass = i < ids.size();
                Color end = getColor(canPass ? ids.get(i) : z);

                result.append(IridiumAPI.color(
                        array[i], getColor(x), end, useRGB));
                if (canPass) x = ids.get(i);
            }

            string = string.replace(match.group(), result + "");
        }

        return string;
    }
}
