package me.croabeast.beanslib.utilities;

import com.google.common.collect.Lists;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.terminals.ActionBar;
import me.croabeast.beanslib.terminals.TitleMngr;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class for static methods of the {@link BeansLib} class.
 *
 * @author CroaBeast
 * @since 1.0
 */
public class TextUtils {

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
     * @param player a player, can be null
     * @param message the input line
     * @return the parsed message
     */
    public static String parsePAPI(@Nullable Player player, String message) {
        return Exceptions.isPluginEnabled("PlaceholderAPI") ?
                PlaceholderAPI.setPlaceholders(player, message) : message;
    }

    /**
     * Removes the first spaces of a line.
     * @param string the input line
     * @return the line without the first spaces
     */
    public static String removeSpace(String string) {
        String startLine = string;
        try {
            while (string.charAt(0) == ' ') string = string.substring(1);
            return string;
        } catch (IndexOutOfBoundsException e) {
            return startLine;
        }
    }

    /**
     * Replace a {@link String} array of keys with another {@link String} array of values.
     * <p> It's case-insensitive and special characters are quoted to avoid errors.
     * @param string an input string
     * @param keys the array of keys
     * @param values the array of values
     * @return the parsed string with the respective values
     */
    public static String replaceInsensitiveEach(String string, String[] keys, String[] values) {
        if (keys == null || values == null) return string;
        if (keys.length > values.length) return string;

        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == null | values[i] == null) continue;
            String newKey = Pattern.quote(keys[i]);

            Matcher matcher = Pattern.compile("(?i)" + newKey).matcher(string);
            if (!matcher.find()) continue;

            string = string.replace(matcher.group(), values[i]);
        }
        return string;
    }

    /**
     * Replace a {@link String} array of keys with another {@link String} array of values.
     * <p> It's case-insensitive and special characters are quoted to avoid errors.
     * @param string an input string
     * @param key a key
     * @param value a value
     * @return the parsed string with the respective value
     */
    public static String replaceInsensitiveEach(String string, String key, String value) {
        return replaceInsensitiveEach(string, new String[] {key}, new String[] {value});
    }

    /**
     * Converts a {@link String} to a {@link List} from
     * a config file or section if it's not a list.
     * @param section a config file or section, can be null
     * @param path the path to locate the string or list
     * @return the converted string list or an empty list if section is null
     */
    public static List<String> toList(@Nullable ConfigurationSection section, String path) {
        if (section == null) return new ArrayList<>();
        if (section.isList(path)) return section.getStringList(path);
        return Lists.newArrayList(section.getString(path));
    }

    /**
     * Check if the line has a valid json format. Usage:
     * <pre> if (IS_JSON.apply(stringInput)) doSomethingIdk();</pre>
     */
    public static final Function<String, Boolean> IS_JSON = s -> TextKeys.JSON_PATTERN.matcher(s).find();

    /**
     * Strips the JSON format from a line.
     * @param line the line to strip.
     * @return the stripped line.
     */
    public static String stripJson(String line) {
        if (!IS_JSON.apply(line)) return line;
        Matcher m = TextKeys.JSON_PATTERN.matcher(line);
        return m.find() ? m.group(7) : line;
    }

    /**
     * Parse InteractiveChat placeholders for using it the Json message.
     * @param player the requested player
     * @param line the line to parse
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
     * @param player a player
     * @param message the message
     */
    public static void sendActionBar(Player player, String message) {
        ACTION_BAR.send(player, message);
    }

    /**
     * Sends a title message to a player
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
     * Sends a title message to a player
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
}
