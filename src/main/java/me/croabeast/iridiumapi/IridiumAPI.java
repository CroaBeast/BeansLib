package me.croabeast.iridiumapi;

import com.google.common.collect.ImmutableMap;
import me.croabeast.iridiumapi.patterns.*;
import me.croabeast.beanslib.*;
import me.croabeast.beanslib.objects.*;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class that handles all the RGB support.
 *
 * @author PeachesMLB
 * @fork CroaBeast
 */
public final class IridiumAPI {

    /**
     * Checks if the server can use RGB colors.
     */
    private static final boolean SUPPORTS_RGB = BeansLib.majorVersion() > 15;

    /**
     * Color regex strings to check.
     */
    private static final String
            BUKKIT_REGEX = "[&§][a-f\\dk-or]", GRADIENT_REGEX = "</?[gr](:\\d{1,3})?>",
            RGB_REGEX = "\\{#[\\dA-F]{6}}|<#[\\dA-F]{6}>|&#[\\dA-F]{6}|#[\\dA-F]{6}",
            COLOR_REGEX = "(?i)" + BUKKIT_REGEX + "|" + GRADIENT_REGEX + "|" + RGB_REGEX;

    /**
     * A map that handles all the Bukkit colors by its hex value.
     */
    private static final Map<Color, ChatColor> COLORS = ImmutableMap.<Color, ChatColor>builder()
            .put(new Color(0), ChatColor.getByChar('0'))
            .put(new Color(170), ChatColor.getByChar('1'))
            .put(new Color(43520), ChatColor.getByChar('2'))
            .put(new Color(43690), ChatColor.getByChar('3'))
            .put(new Color(11141120), ChatColor.getByChar('4'))
            .put(new Color(11141290), ChatColor.getByChar('5'))
            .put(new Color(16755200), ChatColor.getByChar('6'))
            .put(new Color(11184810), ChatColor.getByChar('7'))
            .put(new Color(5592405), ChatColor.getByChar('8'))
            .put(new Color(5592575), ChatColor.getByChar('9'))
            .put(new Color(5635925), ChatColor.getByChar('a'))
            .put(new Color(5636095), ChatColor.getByChar('b'))
            .put(new Color(16733525), ChatColor.getByChar('c'))
            .put(new Color(16733695), ChatColor.getByChar('d'))
            .put(new Color(16777045), ChatColor.getByChar('e'))
            .put(new Color(16777215), ChatColor.getByChar('f')).build();

    /**
     * A list with all the {@link BasePattern} classes.
     */
    private static final List<BasePattern> PATTERNS =
            Arrays.asList(new Gradient(), new Rainbow(), new SolidColor());

    /**
     * Process a string to apply the correct colors using the RGB format.
     *
     * @param s an input string
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     * @return the processed string
     */
    @NotNull
    public static String process(@NotNull String s, boolean useRGB) {
        for (BasePattern pattern : PATTERNS) s = pattern.process(s, useRGB);
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /**
     * Process a string to apply the correct colors using the RGB format.
     *
     * @param s an input string
     * @return the processed string
     */
    @NotNull
    public static String process(@NotNull String s) {
        return process(s, SUPPORTS_RGB);
    }

    /**
     * Process a string to apply the correct colors using the RGB format.
     * Before processing, it checks if the player's client has RGB support.
     *
     * @param player a player
     * @param s an input string
     * @return the processed string
     */
    @NotNull
    public static String process(Player player, String s) {
        int i = 0;
        try {
            i = Protocols.getClientVersion(player);
        }
        catch (Exception ignored) {}
        return i == 0 ? process(s) : process(s, i > 15 && SUPPORTS_RGB);
    }

    /**
     * Applies a single color to a string.
     *
     * @param color  the requested color to apply
     * @param string an input string
     * @return the colored string
     */
    @NotNull
    public static String color(@NotNull Color color, @NotNull String string) {
        return (SUPPORTS_RGB ? ChatColor.of(color) : getClosestColor(color)) + string;
    }

    /**
     * Applies a gradient color to an input string.
     *
     * @param string an input string
     * @param start  the start color
     * @param end    the end color
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     * @return the string with the applied gradient
     */
    @NotNull
    public static String color(@NotNull String string, @NotNull Color start, @NotNull Color end, boolean useRGB) {
        int step = stripSpecial(string).length();
        return step <= 1 ? string : apply(string, createGradient(start, end, step, useRGB));
    }

    /**
     * Applies a rainbow gradient with a specific saturation to an input string.
     *
     * @param string     an input string
     * @param saturation the saturation for the rainbow gradient
     * @param useRGB     if false, it will convert all RGB to its closest bukkit color
     * @return the string with the applied rainbow gradient
     */
    @NotNull
    public static String rainbow(@NotNull String string, float saturation, boolean useRGB) {
        int step = stripSpecial(string).length();
        return step <= 0 ? string : apply(string, createRainbow(step, saturation, useRGB));
    }

    /**
     * Gets the color from an input string.
     *
     * @param string an input string
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     * @return the requested chat color
     */
    @NotNull
    public static ChatColor getColor(@NotNull String string, boolean useRGB) {
        return useRGB ? ChatColor.of(new Color(Integer.parseInt(string, 16)))
                : getClosestColor(new Color(Integer.parseInt(string, 16)));
    }

    /**
     * Removes all the bukkit color format from a string.
     *
     * @param string an input string
     * @return the stripped string
     */
    @NotNull
    public static String stripBukkit(@NotNull String string) {
        return string.replaceAll("(?i)[&§][a-f\\d]", "");
    }

    /**
     * Removes all the bukkit special color format from a string.
     *
     * @param string an input string
     * @return the stripped string
     */
    @NotNull
    public static String stripSpecial(@NotNull String string) {
        return string.replaceAll("(?i)[&§][k-o]", "");
    }

    /**
     * Removes all the rgb color format from a string.
     *
     * @param string an input string
     * @return the stripped string
     */
    @NotNull
    public static String stripRGB(@NotNull String string) {
        return string.replaceAll("(?i)" + RGB_REGEX + "|" + GRADIENT_REGEX, "");
    }

    /**
     * Removes all the color format from a string.
     *
     * @param string an input string
     * @return the stripped string
     */
    @NotNull
    public static String stripAll(@NotNull String string) {
        return string.replaceAll(COLOR_REGEX, "");
    }

    /**
     * Gets the last used color (bukkit or RGB) in its string format in an unformatted input string.
     * <pre>  {@code getLastColor("&bmy name is &aJosh", null, false) = "&a";
     * getLastColor("&b&k&lThis is cool", null, true) = "&b&k&l";
     * getLastColor("&b&k&lThis is cool", null, false) = "&b";
     * getLastColor("{#FFFFFF}hi, &fChris", "Chris", false) = "{#FFFFFF}";
     * getLastColor("{#FFFFFF}hi, &fChris", "Not found", false) = "&f";}</pre>
     *
     * @param string an input string, can not be null
     * @param key a key to search in the string
     * @param getSpecial if you want to get special format
     * @throws IndexOutOfBoundsException if string is empty
     * @return the last color used, returns an empty string if not found
     */
    @NotNull
    public static String getLastColor(String string, String key, boolean getSpecial) {
        if (string == null || string.length() < 1)
            throw new IndexOutOfBoundsException("String can not be empty");

        String lastColor = ""; // an empty string if not found

        boolean hasKey = key != null && key.length() >= 1;
        if (hasKey) key = Pattern.quote(key);

        String special = getSpecial ? "([&§][k-or])*" : "",
                regex = "(?i)(([&§][a-f\\d]|" + RGB_REGEX + ")" + special + ")",
                input = hasKey ? string.split(regex + "?" + key)[0] : string;

        Matcher match = Pattern.compile(regex).matcher(input);

        while (match.find()) lastColor = match.group();
        return lastColor;
    }

    /**
     * Applies every color in an array to a source string.
     *
     * @param source a string
     * @param colors the requested colors array
     * @return the formatted string
     */
    @NotNull
    private static String apply(@NotNull String source, @NotNull ChatColor[] colors) {
        StringBuilder specials = new StringBuilder(),
                builder = new StringBuilder();

        if (StringUtils.isBlank(source)) return source;

        String[] characters = source.split("");
        int outIndex = 0;

        for (int i = 0; i < characters.length; i++) {
            if (!characters[i].matches("[&§]") || i + 1 >= characters.length)
                builder.append(colors[outIndex++])
                        .append(specials).append(characters[i]);
            else {
                if (!characters[i + 1].equals("r")) {
                    specials.append(characters[i]);
                    specials.append(characters[i + 1]);
                }
                else specials.setLength(0);
                i++;
            }
        }

        return builder.toString();
    }

    /**
     * Creates an array of colors for the rainbow gradient.
     *
     * @param step the string's length
     * @param saturation the saturation for the rainbow
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     * @return the rainbow color array
     */
    @NotNull
    private static ChatColor[] createRainbow(int step, float saturation, boolean useRGB) {
        ChatColor[] colors = new ChatColor[step];
        double colorStep = (1.00 / step);

        for (int i = 0; i < step; i++) {
            Color color = Color.getHSBColor((float) (colorStep * i), saturation, saturation);
            colors[i] = useRGB ? ChatColor.of(color) : getClosestColor(color);
        }
        return colors;
    }

    /**
     * Creates an array of colors for the gradient.
     *
     * @param start a start color
     * @param end an end color
     * @param step the string's length
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     * @return the rainbow color array
     */
    @NotNull
    private static ChatColor[] createGradient(@NotNull Color start, @NotNull Color end, int step, boolean useRGB) {
        ChatColor[] colors = new ChatColor[step];

        int stepR = Math.abs(start.getRed() - end.getRed()) / (step - 1),
                stepG = Math.abs(start.getGreen() - end.getGreen()) / (step - 1),
                stepB = Math.abs(start.getBlue() - end.getBlue()) / (step - 1);

        int[] direction = new int[] {
                start.getRed() < end.getRed() ? +1 : -1,
                start.getGreen() < end.getGreen() ? +1 : -1,
                start.getBlue() < end.getBlue() ? +1 : -1
        };

        for (int i = 0; i < step; i++) {
            Color color = new Color(start.getRed() + ((stepR * i) * direction[0]),
                    start.getGreen() + ((stepG * i) * direction[1]),
                    start.getBlue() + ((stepB * i) * direction[2]));
            colors[i] = useRGB ? ChatColor.of(color) : getClosestColor(color);
        }

        return colors;
    }

    /**
     * Gets the closest bukkit color from a normal color.
     *
     * @param color an input color
     * @return the closest bukkit color
     */
    @NotNull
    private static ChatColor getClosestColor(Color color) {
        Color nearestColor = null;
        double nearestDistance = Integer.MAX_VALUE;

        for (Color color1 : COLORS.keySet()) {
            double distance = Math.pow(color.getRed() - color1.getRed(), 2)
                    + Math.pow(color.getGreen() - color1.getGreen(), 2)
                    + Math.pow(color.getBlue() - color1.getBlue(), 2);
            if (nearestDistance > distance) {
                nearestColor = color1;
                nearestDistance = distance;
            }
        }
        return COLORS.get(nearestColor);
    }
}