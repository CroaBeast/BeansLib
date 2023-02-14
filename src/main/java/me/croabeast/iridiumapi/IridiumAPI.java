package me.croabeast.iridiumapi;

import com.google.common.collect.ImmutableMap;
import me.croabeast.beanslib.object.misc.ClientVersion;
import me.croabeast.beanslib.utility.LibUtils;
import me.croabeast.iridiumapi.pattern.*;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
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
     * Initializing this class is blocked.
     */
    private IridiumAPI() {}

    private static final boolean SUPPORTS_RGB = LibUtils.majorVersion() > 15;

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

    private static final List<RGBParser> BASE_PATTERNS =
            Arrays.asList(
                    (s, b) -> {
                        Matcher matcher = RGBParser.SOLID_PATTERN.matcher(s);

                        while (matcher.find()) {
                            s = s.replace(matcher.group(),
                                    IridiumAPI.getColor(matcher.group(1), b) + "");
                        }
                        return s;
                    },
                    (s, b) -> {
                        Matcher matcher = RGBParser.RAINBOW_PATTERN.matcher(s);

                        while (matcher.find()) {
                            String sat = matcher.group(1), c = matcher.group(2);
                            s = s.replace(matcher.group(),
                                    IridiumAPI.rainbow(c, Float.parseFloat(sat), b));
                        }
                        return s;
                    },
                    new Gradient()
            );

    /**
     * Process a string to apply the correct colors using the RGB format.
     *
     * @param s an input string
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     *
     * @return the processed string
     */
    public static String process(String s, boolean useRGB) {
        if (StringUtils.isBlank(s)) return s;

        for (RGBParser p : BASE_PATTERNS) s = p.process(s, useRGB);
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /**
     * Process a string to apply the correct colors using the RGB format.
     * Before processing, it checks if the player's client has RGB support.
     *
     * @param player a player
     * @param s an input string
     *
     * @return the processed string
     */
    public static String process(Player player, String s) {
        int i = ClientVersion.getClientVersion(player);
        return process(s, (i == 0 || i > 15) && SUPPORTS_RGB);
    }

    /**
     * Process a string to apply the correct colors using the RGB format.
     *
     * @param string an input string
     * @return the processed string
     */
    public static String process(String string) {
        return process(null, string);
    }

    /**
     * Applies a single color to a string.
     *
     * @param color the requested color to apply
     * @param string an input string
     *
     * @return the colored string
     */
    public static String color(@NotNull Color color, @NotNull String string) {
        return (SUPPORTS_RGB ? ChatColor.of(color) : getClosestColor(color)) + string;
    }

    /**
     * Applies a gradient color to an input string.
     *
     * @param string an input string
     * @param start the start color
     * @param end the end color
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     *
     * @return the string with the applied gradient
     */
    public static String color(@NotNull String string, @NotNull Color start, @NotNull Color end, boolean useRGB) {
        int step = stripSpecial(string).length();
        return step <= 1 ? string : apply(string, createGradient(start, end, step, useRGB));
    }

    /**
     * Applies a rainbow gradient with a specific saturation to an input string.
     *
     * @param string an input string
     * @param saturation the saturation for the rainbow gradient
     * @param useRGB if false, it will convert all RGB to its closest bukkit color
     *
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
     *
     * @return the requested chat color
     */
    @NotNull
    public static ChatColor getColor(@NotNull String string, boolean useRGB) {
        Color c = new Color(Integer.parseInt(string, 16));
        return useRGB ? ChatColor.of(c) : getClosestColor(c);
    }

    /**
     * Removes all the bukkit color format from a string.
     *
     * @param string an input string
     * @return the stripped string
     */
    @NotNull
    public static String stripBukkit(String string) {
        return StringUtils.isBlank(string) ? string : string.replaceAll("(?i)[&§][a-f\\d]", "");
    }

    /**
     * Removes all the bukkit special color format from a string.
     *
     * @param string an input string
     * @return the stripped string
     */
    @NotNull
    public static String stripSpecial(String string) {
        return StringUtils.isBlank(string) ? string : string.replaceAll("(?i)[&§][k-or]", "");
    }

    /**
     * Removes all the rgb color format from a string.
     *
     * @param string an input string
     * @return the stripped string
     */
    @NotNull
    public static String stripRGB(String string) {
        if (StringUtils.isBlank(string)) return string;

        string = Gradient.convertLegacy(string);

        Matcher gradient = Gradient.GRADIENT_PATTERN.matcher(string);
        while (gradient.find())
            string = string.replace(gradient.group(), gradient.group(2));

        Matcher rgb = RGBParser.SOLID_PATTERN.matcher(string);
        while (rgb.find()) string = string.replace(rgb.group(), "");

        string = string.replaceAll("(?i)(<R:\\d{1,3}>|</R>)", "");

        return string;
    }

    /**
     * Removes all the color format from a string.
     *
     * @param string an input string
     * @return the stripped string
     */
    @NotNull
    public static String stripAll(String string) {
        return stripRGB(stripSpecial(stripBukkit(string)));
    }

    /**
     * Gets the last used color (bukkit or RGB) in its string format in an unformatted input string.
     * <pre> {@code
     * getLastColor("&bmy name is &aJosh", null, false, false) = "&a";
     * getLastColor("&b&k&lThis is cool", null, true, false) = "&b&k&l";
     * getLastColor("&b&k&lThis is cool", null, false, false) = "&b";
     * getLastColor("{#FFFFFF}hi, &fChris", "Chris", false, false) = "{#FFFFFF}";
     * getLastColor("{#FFFFFF}hi, &fChris", "Chris", false, true) = "&f";
     * }</pre>
     *
     * @param string an input string, can not be null neither empty
     * @param key a key to search in the string, can be null
     * @param getSpecial if it gets the special color format
     * @param checkBefore checks a color before the key if true
     *
     * @return the last color used, returns an empty string if not found
     * @throws IndexOutOfBoundsException if string is empty
     */
    @NotNull
    public static String getLastColor(String string, String key, boolean getSpecial, boolean checkBefore) {
        if (string == null || string.length() < 1)
            throw new IndexOutOfBoundsException("String can not be empty");

        String lastColor = ""; // an empty string if not found

        boolean hasKey = key != null && key.length() >= 1;
        if (hasKey) key = Pattern.quote(key);

        String regex = "(?i)(([&§][a-f\\d]|[{&<]?#([\\da-f]{6})[}>]?)" +
                (getSpecial ? "([&§][k-or])*" : "") + ")";

        String[] inputs = !hasKey ? new String[] {string} :
                string.split((checkBefore ? "" : (regex + "?")) + key);

        Matcher match = Pattern.compile(regex).matcher(inputs[0]);

        while (match.find()) lastColor = match.group();
        return lastColor;
    }

    @NotNull
    private static String apply(@NotNull String source, @NotNull ChatColor[] colors) {
        StringBuilder specials = new StringBuilder(),
                builder = new StringBuilder();

        if (StringUtils.isBlank(source)) return source;

        String[] characters = source.split("");
        int outIndex = 0;

        for (int i = 0; i < characters.length; i++) {
            if (!characters[i].matches("[&§]") || i + 1 >= characters.length) {
                builder.append(colors[outIndex++]).
                        append(specials).
                        append(characters[i]);
                continue;
            }

            if (characters[i + 1].equals("r")) specials.setLength(0);
            else specials.append(characters[i]).append(characters[i + 1]);
            i++;
        }

        return builder.toString();
    }

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

    @NotNull
    private static ChatColor getClosestColor(Color color) {
        Color nearestColor = null;
        double nearestDistance = Integer.MAX_VALUE;

        for (Color c : COLORS.keySet()) {
            double distance =
                    Math.pow(color.getRed() - c.getRed(), 2) +
                    Math.pow(color.getGreen() - c.getGreen(), 2) +
                    Math.pow(color.getBlue() - c.getBlue(), 2);

            if (nearestDistance <= distance) continue;

            nearestColor = c;
            nearestDistance = distance;
        }

        return COLORS.get(nearestColor);
    }
}