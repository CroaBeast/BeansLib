package me.croabeast.beanslib.utility;

import com.google.common.collect.Lists;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.BeansMethods;
import me.croabeast.beanslib.object.terminal.ActionBar;
import me.croabeast.beanslib.object.terminal.TitleMngr;
import me.croabeast.beanslib.object.display.Displayer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class for static methods of the {@link BeansLib} class.
 *
 * @author CroaBeast
 * @since 1.0
 */
public final class TextUtils {

    /**
     * Initializing this class is blocked.
     */
    private TextUtils() {}

    /**
     * Action Bar sender handler.
     */
    private static final ActionBar ACTION_BAR = new ActionBar();
    /**
     * Title sender handler.
     */
    private static final TitleMngr TITLE_MNGR = new TitleMngr();

    /**
     * Parses the placeholders from {@link PlaceholderAPI} if is enabled.
     *
     * @param player a player, can be null
     * @param message the input line
     *
     * @return the parsed message
     */
    public static String parsePAPI(Player player, String message) {
        return Exceptions.isPluginEnabled("PlaceholderAPI") ?
                PlaceholderAPI.setPlaceholders(player, message) : message;
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
     * @param additionalArrays Any additional arrays
     * @param <T> Type of array (must be same)
     *
     * @return New array of combined values
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T> T[] combineArrays(@NotNull T[] array, @Nullable T[]... additionalArrays) {
        if (additionalArrays == null) return Arrays.copyOf(array, array.length);

        List<T> resultList = new ArrayList<>();
        Collections.addAll(resultList, array);

        for (T[] a : additionalArrays) {
            if (a == null) continue;
            Collections.addAll(resultList, a);
        }

        T[] resultArray = (T[]) Array.newInstance(array.getClass().getComponentType(), 0);
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
     * @return the parsed string with the respective values
     */
    @NotNull
    public static String replaceEach(String string, String[] keys, String[] values, boolean caseSensitive) {
        if (StringUtils.isBlank(string)) return string;

        if (keys == null || values == null) return string;
        if (keys.length > values.length) return string;

        String add = caseSensitive ? "" : "(?i)";

        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == null | values[i] == null) continue;

            String newKey = Pattern.quote(keys[i]);
            if (StringUtils.isBlank(newKey)) continue;

            Matcher matcher = Pattern.compile(add + newKey).matcher(string);
            if (!matcher.find()) continue;

            string = string.replace(matcher.group(), values[i]);
        }
        return string;
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
     * @return the parsed string with the respective value
     */
    public static String replaceEach(String string, String key, String value, boolean caseSensitive) {
        return replaceEach(string, new String[] {key}, new String[] {value}, caseSensitive);
    }

    /**
     * Replace a {@link String} array of keys with another {@link String} array of values.
     * <p> It's case-insensitive and special characters are quoted to avoid errors.
     *
     * @param string an input string
     * @param keys the array of keys
     * @param values the array of values
     *
     * @return the parsed string with the respective values
     */
    @NotNull
    public static String replaceInsensitiveEach(String string, String[] keys, String[] values) {
        return replaceEach(string, keys, values, false);
    }

    /**
     * Replace a {@link String} key with a {@link String} value.
     * <p> It's case-insensitive and special characters are quoted to avoid errors.
     *
     * @param string an input string
     * @param key a key
     * @param value a value
     *
     * @return the parsed string with the respective value
     */
    public static String replaceInsensitiveEach(String string, String key, String value) {
        return replaceInsensitiveEach(string, new String[] {key}, new String[] {value});
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
    public static List<String> toList(@Nullable ConfigurationSection section, String path) {
        if (section == null) return new ArrayList<>();
        if (section.isList(path)) return section.getStringList(path);
        return Lists.newArrayList(section.getString(path));
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
     * Check if the line has a valid json format. Usage:
     * <pre> {@code if (IS_JSON.apply("a string")) doSomethingIdk();}</pre>
     */
    public static final Function<String, Boolean> IS_JSON =
            s -> LibUtils.JSON_PATTERN.matcher(convertOldJson(s)).find();

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
        Exceptions.checkPlayer(player);

        try {
            return InteractiveChatAPI.markSender(line, player.getUniqueId());
        } catch (Exception e) {
            e.printStackTrace();
            return line;
        }
    }

    /**
     * Sends an action bar message to a player.
     *
     * @param player a player
     * @param message the message
     */
    public static void sendActionBar(Player player, String message) {
        ACTION_BAR.send(player, message);
    }

    /**
     * Sends a title message to a player.
     *
     * @param player a player
     * @param title a title
     * @param subtitle a subtitle
     * @param in the fadeIn number in ticks
     * @param stay the stay number in ticks
     * @param out the fadeOut number in ticks
     */
    public static void sendTitle(Player player, String title, String subtitle, int in, int stay, int out) {
        TITLE_MNGR.send(player, title, subtitle, in, stay, out);
    }

    /**
     * Sends a title message to a player.
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

        String quote = use ? "\"" : "";
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            if (i > 0) builder.append(split);
            builder.append(quote).append(args[i]).append(quote);
        }

        return name + "=[" + builder + "]";
    }
}
