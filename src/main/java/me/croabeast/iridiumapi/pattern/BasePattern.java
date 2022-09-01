package me.croabeast.iridiumapi.pattern;

import java.util.regex.Pattern;

/**
 * The class that handles the RGB format to parse.
 */
public abstract class BasePattern {

    BasePattern() {}

    /**
     * Compiles the rainbow gradient pattern.
     */
    public static final Pattern RAINBOW_PATTERN = Pattern.compile("(?i)<R:(\\d{1,3})>(.+?)</R>");

    /**
     * Compiles the solid color pattern.
     */
    public static final Pattern SOLID_PATTERN = Pattern.compile("(?i)[{&<]?#([\\da-f]{6})[}>]?");

    /**
     * Process a string using the RGB patterns to apply colors.
     *
     * @param string an input string
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     * @return the processed line
     */
    public abstract String process(String string, boolean useRGB);
}
