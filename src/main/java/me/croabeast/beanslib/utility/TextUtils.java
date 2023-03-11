package me.croabeast.beanslib.utility;

import com.google.common.collect.Lists;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.key.ValueReplacer;
import me.croabeast.beanslib.nms.NMSActionBar;
import me.croabeast.beanslib.nms.NMSTitle;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that stores static methods for text, string, configurations,
 * and more.
 *
 * @author CroaBeast
 * @since 1.0
 */
public final class TextUtils {

    /**
     * Parse all the {@link PlaceholderAPI} placeholders of an input string if
     * {@link PlaceholderAPI} is enabled.
     *
     * <p> Use the <code>apply(Player, String)</code> method to apply it on a string.
     */
    public static final BiFunction<Player, String, String> PARSE_PLACEHOLDERAPI = (p, s) -> {
        if (StringUtils.isBlank(s)) return s;
        if (!Exceptions.isPluginEnabled("PlaceholderAPI")) return s;

        return PlaceholderAPI.setPlaceholders(p, s);
    };

    /**
     * Parse all the {@link InteractiveChat} values of an input string if
     * {@link InteractiveChat} is enabled.
     *
     * <p> Use the <code>apply(Player, String)</code> method to apply it on a string.
     */
    public static final BiFunction<Player, String, String> PARSE_INTERACTIVE_CHAT = (p, s) -> {
        if (!Exceptions.isPluginEnabled("InteractiveChat"))
            return s;

        try {
            UUID u = Exceptions.checkPlayer(p).getUniqueId();
            return InteractiveChatAPI.markSender(s, u);
        } catch (Exception e) {
            return s;
        }
    };

    /**
     * Strips the first spaces of an input string.
     *
     * <p> Use the <code>apply(String)</code> method to apply it on a string.
     */
    public static final UnaryOperator<String> STRIP_FIRST_SPACES = s -> {
        if (StringUtils.isBlank(s)) return s;
        String startLine = s;

        try {
            while (s.charAt(0) == ' ') s = s.substring(1);
            return s;
        } catch (IndexOutOfBoundsException e) {
            return startLine;
        }
    };

    /**
     * Coverts the old Json format to the new in-built one.
     *
     * <p> Use the <code>apply(String)</code> method to apply it on a string.
     */
    public static final UnaryOperator<String> CONVERT_OLD_JSON = s -> {
        if (StringUtils.isBlank(s)) return s;

        String p = "(?i)(hover|run|suggest|url)=\\[(.[^|\\[\\]]*)]";
        Matcher old = Pattern.compile(p).matcher(s);

        while (old.find()) {
            String temp = old.group(1) + ":\"" + old.group(2) + "\"";
            s = s.replace(old.group(), temp);
        }
        return s;
    };

    /**
     * Check if the line uses a valid json format on any part of the string.
     *
     * <p> Use the <code>apply(String)</code> method to check a string.
     */
    public static final Function<String, Boolean> IS_JSON = s ->
            BeansLib.getLoadedInstance().getJsonPattern().matcher(s).find();

    /**
     * Removes the in-built JSON pattern of a string, if there is any format.
     *
     * <p> Use the <code>apply(String)</code> method to apply it on a string.
     */
    public static final UnaryOperator<String> STRIP_JSON = s -> {
        if (StringUtils.isBlank(s)) return s;

        s = CONVERT_OLD_JSON.apply(s);
        if (!IS_JSON.apply(s)) return s;

        Matcher m = BeansLib.getLoadedInstance().
                getJsonPattern().matcher(s);

        while (m.find())
            s = s.replace(m.group(), m.group(7));

        return s;
    };

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
    @Deprecated
    public static String parsePAPI(Player player, String string) {
        return PARSE_PLACEHOLDERAPI.apply(player, string);
    }

    /**
     * Parse InteractiveChat placeholders for using it a JSON message.
     *
     * @param player the requested player
     * @param string the line to parse
     *
     * @return the line with the parsed placeholders.
     */
    @Deprecated
    public static String parseInteractiveChat(Player player, String string) {
        return PARSE_INTERACTIVE_CHAT.apply(player, string);
    }

    /**
     * Removes the first spaces of a line.
     *
     * @param string the input line
     * @return the line without the first spaces
     */
    @Deprecated
    public static String removeSpace(String string) {
        return STRIP_FIRST_SPACES.apply(string);
    }

    /**
     * Converts the old JSON format to the new one.
     *
     * @param string an input string
     * @return the converted string
     */
    @Deprecated
    public static String convertOldJson(String string) {
        return CONVERT_OLD_JSON.apply(string);
    }

    /**
     * Strips the JSON format from an input string.
     *
     * @param string an input string.
     * @return the stripped line.
     */
    @Deprecated
    public static String stripJson(String string) {
        return STRIP_JSON.apply(string);
    }

    /**
     * Combines an array with one or more additional arrays into a new
     * array of the same type.
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
     * Converts a {@link String} to a {@link List} from a configuration section if it's not a list.
     *
     * @param section a config file or section, can be null
     * @param path the path to locate the string or list
     * @param def a default string list if value is not found
     *
     * @return the converted string list or default value if section is null
     */
    @SuppressWarnings("unchecked")
    public static List<String> toList(ConfigurationSection section, String path, List<String> def) {
        if (section == null) return def;

        if (section.isList(path))
            return (List<String>) section.getList(path, def);

        String temp = section.getString(path);
        return temp == null ? def : Lists.newArrayList(temp);
    }

    /**
     * Converts a {@link String} to a {@link List} from a configuration section if it's not a list.
     *
     * @param section a config file or section, can be null
     * @param path the path to locate the string or list
     *
     * @return the converted string list or an empty list if section is null
     */
    @NotNull
    public static List<String> toList(ConfigurationSection section, String path) {
        return toList(section, path, new ArrayList<>());
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

        sendTitle(player, message[0], message.length == 1 ? "" : message[1], in, stay, out);
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

    /**
     * Replace a {@link String} array of keys with another {@link String}
     * array of values.
     *
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
     * Replace a {@link String} array of keys with another {@link String}
     * array of values.
     *
     * <p> It's case-insensitive and special characters are quoted
     * to avoid errors.
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
     *
     * <p> It's case-insensitive and special characters are quoted
     * to avoid errors.
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
}
