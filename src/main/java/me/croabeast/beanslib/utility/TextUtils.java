package me.croabeast.beanslib.utility;

import com.google.common.collect.Lists;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.key.ValueReplacer;
import me.croabeast.beanslib.nms.NMSActionBar;
import me.croabeast.beanslib.nms.NMSTitle;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that stores static methods for text, string, configurations, and more.
 *
 * @author CroaBeast
 * @since 1.0
 */
public final class TextUtils {

    /**
     * Check if the line uses a valid json format on any part of the string.
     *
     * <pre> {@code
     * // • Usage example:
     * if (IS_JSON.apply("a string")) doSomething();
     * } </pre>
     */
    public static final Function<String, Boolean> IS_JSON =
            s -> LibUtils.JSON_PATTERN.matcher(convertOldJson(s)).find();

    /**
     * Initializing this class is blocked.
     */
    private TextUtils() {}

    /**
     * Parses the placeholders from {@link PlaceholderAPI} if is enabled.
     *
     * @param player a player, can be null
     * @param string the input line
     *
     * @return the parsed message
     */
    public static String parsePAPI(Player player, String string) {
        if (StringUtils.isBlank(string)) return string;

        return Exceptions.isPluginEnabled("PlaceholderAPI") ?
                PlaceholderAPI.setPlaceholders(player, string) : string;
    }

    /**
     * Removes the first spaces of a line.
     *
     * @param string the input line
     * @return the line without the first spaces
     */
    public static String removeSpace(String string) {
        if (StringUtils.isBlank(string)) return string;
        String startLine = string;

        try {
            while (string.charAt(0) == ' ') string = string.substring(1);
            return string;
        } catch (IndexOutOfBoundsException e) {
            return startLine;
        }
    }

    /**
     * Combines an array with one or more additional arrays
     * into a new array of the same type.
     *
     * @author Kihsomray
     * @since 1.3
     *
     * @param array First array
     * @param extraArrays Any additional arrays
     * @param <T> Type of array (must be same)
     *
     * @return New array of combined values
     */
    @SuppressWarnings("unchecked") @SafeVarargs
    public static <T> T[] combineArrays(@NotNull T[] array, T[]... extraArrays) {
        if (extraArrays == null || extraArrays.length < 1)
            return array;

        List<T> resultList = new ArrayList<>();
        Collections.addAll(resultList, array);

        for (T[] a : extraArrays)
            if (a != null) Collections.addAll(resultList, a);

        Class<?> clazz = array.getClass().getComponentType();
        T[] resultArray = (T[]) Array.newInstance(clazz, 0);

        return resultList.toArray(resultArray);
    }

    /**
     * Replace a {@link String} array of keys with another {@link String} array of values.
     * <p> Special characters are quoted to avoid errors.
     *
     * @param string an input string
     * @param keys the array of keys
     * @param values the array of values
     * @param caseSensitive if keys are case-sensitive
     *
     * @deprecated See {@link ValueReplacer}.
     * @return the parsed string with the respective values
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.5")
    @Deprecated
    public static String replaceEach(String string, String[] keys, String[] values, boolean caseSensitive) {
        return ValueReplacer.forEach(string, keys, values, caseSensitive);
    }

    /**
     * Replace a {@link String} key with a {@link String} value.
     * <p> Special characters are quoted to avoid errors.
     *
     * @param string an input string
     * @param key a key
     * @param value a value
     * @param caseSensitive if key is case-sensitive
     *
     * @deprecated See {@link ValueReplacer}.
     * @return the parsed string with the respective value
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.5")
    @Deprecated
    public static String replaceEach(String string, String key, String value, boolean caseSensitive) {
        return new ValueReplacer(key, value).setCaseSensitive(caseSensitive).replace(string);
    }

    /**
     * Replace a {@link String} array of keys with another {@link String} array of values.
     * <p> It's case-insensitive and special characters are quoted to avoid errors.
     *
     * @param string an input string
     * @param keys the array of keys
     * @param values the array of values
     *
     * @deprecated See {@link ValueReplacer}.
     * @return the parsed string with the respective values
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.5")
    @Deprecated
    public static String replaceInsensitiveEach(String string, String[] keys, String[] values) {
        return ValueReplacer.forEach(string, keys, values);
    }

    /**
     * Replace a {@link String} key with a {@link String} value.
     * <p> It's case-insensitive and special characters are quoted to avoid errors.
     *
     * @param string an input string
     * @param key a key
     * @param value a value
     *
     * @deprecated See {@link ValueReplacer}.
     * @return the parsed string with the respective value
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.5")
    @Deprecated
    public static String replaceInsensitiveEach(String string, String key, String value) {
        return new ValueReplacer(key, value).setCaseSensitive(false).replace(string);
    }

    /**
     * Converts a {@link String} to a {@link List} from
     * a config file or section if it's not a list.
     *
     * @param section a config file or section, can be null
     * @param path the path to locate the string or list
     *
     * @return the converted string list or an empty list if section is null
     */
    @NotNull
    public static List<String> toList(@Nullable ConfigurationSection section, String path) {
        if (section == null) return new ArrayList<>();

        final String temp = section.getString(path);
        if (temp == null) return new ArrayList<>();

        if (section.isList(path)) return section.getStringList(path);
        return Lists.newArrayList(temp);
    }

    /**
     * Converts the old JSON format to the new one.
     *
     * @param string an input string
     * @return the converted string
     */
    public static String convertOldJson(String string) {
        if (StringUtils.isBlank(string)) return string;

        String s = "(?i)(hover|run|suggest|url)=\\[(.[^|\\[\\]]*)]";
        Matcher old = Pattern.compile(s).matcher(string);

        while (old.find()) {
            String temp = old.group(1) + ":\"" + old.group(2) + "\"";
            string = string.replace(old.group(), temp);
        }
        return string;
    }

    /**
     * Strips the JSON format from an input string.
     *
     * @param string an input string.
     * @return the stripped line.
     */
    public static String stripJson(String string) {
        if (StringUtils.isBlank(string)) return string;

        string = convertOldJson(string);
        if (!IS_JSON.apply(string)) return string;

        Matcher m = LibUtils.JSON_PATTERN.matcher(string);
        while (m.find())
            string = string.replace(m.group(), m.group(7));

        return string;
    }

    /**
     * Parse InteractiveChat placeholders for using it a JSON message.
     *
     * @param player the requested player
     * @param line the line to parse
     *
     * @return the line with the parsed placeholders.
     */
    public static String parseInteractiveChat(Player player, String line) {
        if (!Exceptions.isPluginEnabled("InteractiveChat")) return line;

        try {
            Exceptions.checkPlayer(player);
            return InteractiveChatAPI.markSender(line, player.getUniqueId());
        } catch (Exception e) {
            return line;
        }
    }

    /**
     * Sends an action bar message to a player.
     *
     * <p> Doesn't format the message. Use {@link BeansLib#colorize(Player, Player, String)}.
     *
     * @param player a player
     * @param message the message
     */
    public static void sendActionBar(Player player, String message) {
        NMSActionBar.INSTANCE.send(player, message);
    }

    /**
     * Sends a title message to a player.
     *
     * <p> Doesn't format the message. Use {@link BeansLib#colorize(Player, Player, String)}.
     *
     * @param player a player
     * @param title a title
     * @param subtitle a subtitle
     * @param in the fadeIn number in ticks
     * @param stay the stay number in ticks
     * @param out the fadeOut number in ticks
     */
    public static void sendTitle(Player player, String title, String subtitle, int in, int stay, int out) {
        NMSTitle.INSTANCE.send(player, title, subtitle, in, stay, out);
    }

    /**
     * Sends a title message to a player.
     *
     * <p> Doesn't format the message. Use {@link BeansLib#colorize(Player, Player, String)}.
     *
     * @param player a player
     * @param message an array of title and subtitle
     * @param in the fadeIn number in ticks
     * @param stay the stay number in ticks
     * @param out the fadeOut number in ticks
     */
    public static void sendTitle(Player player, @NotNull String[] message, int in, int stay, int out) {
        if (message.length <= 0 || message.length > 2) return;
        String subtitle = message.length == 1 ? "" : message[1];
        sendTitle(player, message[0], subtitle, in, stay, out);
    }

    /**
     * Formats a class to a string defined format.
     *
     * @param obj the object to format
     * @param split the splitter
     * @param use use quotes in every object
     * @param args values for the object
     *
     * @return the formatted class
     */
    public static String classFormat(Object obj, String split, boolean use, Object... args) {
        final String name = obj.getClass().getSimpleName();

        if (args == null || args.length == 0) return name + "=[]";
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            if (i > 0) builder.append(split);

            if (use) builder.append("\"");
            builder.append(args[i]);
            if (use) builder.append("\"");
        }

        return name + "=[" + builder + "]";
    }
}
