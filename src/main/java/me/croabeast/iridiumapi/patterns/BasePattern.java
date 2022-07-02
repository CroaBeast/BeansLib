package me.croabeast.iridiumapi.patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class that handles the RGB format to parse.
 */
public abstract class BasePattern {

    BasePattern() {}

    /**
     * The hex basic format.
     */
    protected final String HEX = "[\\da-f]{6}";

    /**
     * Compiles the rainbow gradient pattern.
     * @return rainbow gradient pattern
     */
    protected Pattern rainbowPattern() {
        return Pattern.compile("(?i)<R:(\\d{1,3})>(.+?)</R>");
    }

    /**
     * Compiles the solid color pattern.
     * @return solid color pattern
     */
    protected Pattern solidPattern() {
        return Pattern.compile("(?i)[{&<]?#(" + HEX + ")[}>]?");
    }

    /**
     * Compiles the gradient pattern.
     * <p> Deprecated to use the new gradient format that has support for multiple gradients.
     * <p> See {@link #gradientPattern()} for more info.
     * @return legacy gradient pattern
     */
    @Deprecated
    final Pattern legacyGradient() {
        return Pattern.compile("(?i)<G:(" + HEX + ")>(.+?)</G:(" + HEX + ")>");
    }

    /**
     * Creates a gradient format from a string.
     * @param string an input string
     * @param isRegex if the format is a regex string
     * @param isEnding if is an ending format type
     * @return the gradient format
     */
    protected String gradient(String string, boolean isRegex, boolean isEnding) {
        return "<" + (isEnding ? "/" : "") + "#" +
                (isRegex ? "(" : "") + string + (isRegex ? ")" : "") + ">";
    }

    /**
     * Creates a gradient format from a string.
     * @param string an input string
     * @param isRegex if the format is a regex string
     * @return the gradient format
     */
    protected String gradient(String string, boolean isRegex) {
        return gradient(string, isRegex, false);
    }

    /**
     * Creates a gradient regex format using {@link #HEX} string.
     * @return the gradient format
     */
    protected String gradient() {
        return gradient(HEX, true);
    }

    /**
     * Converts the {@link #legacyGradient()} to the new {@link #gradientPattern()}.
     * @param string an input string
     * @return the converted string
     */
    protected String convertFromLegacyGradient(String string) {
        Matcher match = legacyGradient().matcher(string);
        if (!match.find()) return string;

        match.reset();
        while (match.find()) {
            String start = match.group(1), end = match.group(3);
            string = string.replace(match.group(),
                    gradient(start, false) + match.group(2) + gradient(end, false, true));
        }
        return string;
    }

    /**
     * Compiles the gradient pattern. Supports multiple gradients in one format.
     * @return gradient pattern
     */
    protected Pattern gradientPattern() {
        return Pattern.compile("(?i)" + gradient() + "(.+?)" + gradient(HEX, true, true));
    }

    /**
     * Process a string using the RGB patterns to apply colors.
     * @param string an input string
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     * @return the processed line
     */
    public abstract String process(String string, boolean useRGB);
}
