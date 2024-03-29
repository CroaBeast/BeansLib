package me.croabeast.beanslib.utility;

import com.google.common.collect.Lists;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.key.ValueReplacer;
import me.croabeast.beanslib.reflect.ActionBarHandler;
import me.croabeast.beanslib.reflect.TitleHandler;
import net.md_5.bungee.api.chat.ClickEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Predicate;
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
@UtilityClass
public class TextUtils {

    /**
     * Parse all the {@link PlaceholderAPI} placeholders of an input string if
     * {@link PlaceholderAPI} is enabled.
     *
     * <p> Use the <code>apply(Player, String)</code> method to apply it on a string.
     */
    public final BiFunction<Player, String, String> PARSE_PLACEHOLDERAPI = (p, s) -> {
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
    public final BiFunction<Player, String, String> PARSE_INTERACTIVE_CHAT = (p, s) -> {
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
     * <p> Use the <code>apply(String)</code> method to apply it on a string.
     */
    public final UnaryOperator<String> STRIP_FIRST_SPACES = s -> {
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
     * <p> Use the <code>apply(String)</code> method to apply it on a string.
     */
    public final UnaryOperator<String> CONVERT_OLD_JSON = s -> {
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
     * <p> Use the <code>apply(String)</code> method to check a string.
     */
    public final Predicate<String> IS_JSON = s ->
            TextUtils.FORMAT_CHAT_PATTERN.matcher(s).find();

    /**
     * Removes the in-built JSON pattern of a string, if there is any format.
     * <p> Use the <code>apply(String)</code> method to apply it on a string.
     */
    public final UnaryOperator<String> STRIP_JSON = s -> {
        if (StringUtils.isBlank(s)) return s;

        s = CONVERT_OLD_JSON.apply(s);
        if (!IS_JSON.test(s)) return s;

        Matcher m = TextUtils.FORMAT_CHAT_PATTERN.matcher(s);

        while (m.find())
            s = s.replace(m.group(), m.group(7));

        return s;
    };

    /**
     * A regular expression pattern for matching URLs in a case-insensitive manner.
     *
     * <p> The pattern matches URLs that start with an optional protocol (http or https),
     * followed by a domain name consisting of at least two alphanumeric characters, and
     * ending with an optional path.
     *
     * <p> Note: This pattern is a simplified version that matches basic URL formats and
     * does not handle all possible cases. It may not validate all edge cases or specific
     * scenarios. For more comprehensive URL matching, consider using specialized URL
     * parsing libraries or additional validation.
     */
    public final Pattern URL_PATTERN = Pattern.compile("(?i)^(?:(https?)://)?([-\\w_.]{2,}\\.[a-z]{2,4})(/\\S*)?$");

    final String FORMAT_PREFIX = "(.[^|]*?):\"(.[^|]*?)\"";

    /**
     * The main pattern to identify the custom chat message format in a string.
     *
     * <p> Keep in mind that every string can only have one {@link ClickEvent.Action};
     * a click action has this format:
     * <pre> {@code
     * Available Actions: RUN, SUGGEST, URL and all ClickAction values.
     * "<ACTION>:<the click string>" -> "RUN:/me click to run"
     * } </pre>
     *
     * <pre> {@code
     * // • Examples:
     * String hover = "<hover:\"a hover line\">text to apply</text>";
     * String click = "<run:\"/click me\">text to apply</text>";
     * String mixed = "<hover:\"a hover line<n>another line\"|run:\"/command\">text to apply</text>";
     * } </pre>
     */
    public final Pattern FORMAT_CHAT_PATTERN = Pattern.compile(
            "<(" + FORMAT_PREFIX + "([|]" + FORMAT_PREFIX + ")?)>(.+?)</text>"
    );

    /**
     * Parses the placeholders from {@link PlaceholderAPI} if is enabled.
     *
     * @param player a player, can be null
     * @param string the input line
     *
     * @return the parsed message
     */
    @Deprecated
    public String parsePAPI(Player player, String string) {
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
    public String parseInteractiveChat(Player player, String string) {
        return PARSE_INTERACTIVE_CHAT.apply(player, string);
    }

    /**
     * Removes the first spaces of a line.
     *
     * @param string the input line
     * @return the line without the first spaces
     */
    @Deprecated
    public String removeSpace(String string) {
        return STRIP_FIRST_SPACES.apply(string);
    }

    /**
     * Converts the old JSON format to the new one.
     *
     * @param string an input string
     * @return the converted string
     */
    @Deprecated
    public String convertOldJson(String string) {
        return CONVERT_OLD_JSON.apply(string);
    }

    /**
     * Strips the JSON format from an input string.
     *
     * @param string an input string.
     * @return the stripped line.
     */
    @Deprecated
    public String stripJson(String string) {
        return STRIP_JSON.apply(string);
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
    public List<String> toList(ConfigurationSection section, String path, List<String> def) {
        if (section == null) return def;

        if (section.isList(path)) {
            final List<?> raw = section.getList(path, def);
            if (raw == null || raw.isEmpty()) return def;

            List<String> list = new ArrayList<>();
            for (Object o : raw) list.add(o.toString());

            return list;
        }

        Object temp = section.get(path);
        return temp == null ? def : Lists.newArrayList(temp.toString());
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
    public List<String> toList(ConfigurationSection section, String path) {
        return toList(section, path, new ArrayList<>());
    }

    /**
     * Sends an action bar message to a player.
     * <p> Doesn't format the message. Use {@link BeansLib#colorize(Player, Player, String)}.
     *
     * @param player a player
     * @param message the message
     *
     * @return true is the message was sent, false otherwise
     */
    public boolean sendActionBar(Player player, String message) {
        return ActionBarHandler.send(player, message);
    }

    /**
     * Sends a title message to a player.
     * <p> Doesn't format the message. Use {@link BeansLib#colorize(Player, Player, String)}.
     *
     * @param player a player
     * @param title a title
     * @param subtitle a subtitle
     * @param in the fadeIn number in ticks
     * @param stay the stay number in ticks
     * @param out the fadeOut number in ticks
     *
     * @return true is the message was sent, false otherwise
     */
    public boolean sendTitle(Player player, String title, String subtitle, int in, int stay, int out) {
        return TitleHandler.send(player, title, subtitle, in, stay, out);
    }

    /**
     * Sends a title message to a player.
     * <p> Doesn't format the message. Use {@link BeansLib#colorize(Player, Player, String)}.
     *
     * @param player a player
     * @param title a title
     * @param in the fadeIn number in ticks
     * @param stay the stay number in ticks
     * @param out the fadeOut number in ticks
     *
     * @return true is the message was sent, false otherwise
     */
    public boolean sendTitle(Player player, String title, int in, int stay, int out) {
        return sendTitle(player, title, "", in, stay, out);
    }

    /**
     * Sends a title message to a player.
     * <p> Doesn't format the message. Use {@link BeansLib#colorize(Player, Player, String)}.
     *
     * @param player a player
     * @param message an array of title and subtitle
     * @param in the fadeIn number in ticks
     * @param stay the stay number in ticks
     * @param out the fadeOut number in ticks
     *
     * @return true is the message was sent, false otherwise
     */
    public boolean sendTitle(Player player, @NotNull String[] message, int in, int stay, int out) {
        if (message.length <= 0 || message.length > 2) return false;
        return sendTitle(player, message[0], message.length == 1 ? "" : message[1], in, stay, out);
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
     * @param b if keys are case-sensitive
     *
     * @deprecated See {@link ValueReplacer}.
     * @return the parsed string with the respective values
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.4")
    @Deprecated
    public String replaceEach(String[] keys, String[] values, String string, boolean b) {
        return ValueReplacer.forEach(keys, values, string, b);
    }

    /**
     * Replace a {@link String} key with a {@link String} value.
     * <p> Special characters are quoted to avoid errors.
     *
     * @param string an input string
     * @param key a key
     * @param value a value
     * @param b if key is case-sensitive
     *
     * @deprecated See {@link ValueReplacer}.
     * @return the parsed string with the respective value
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.4")
    @Deprecated
    public String replaceEach(String key, String value, String string, boolean b) {
        return ValueReplacer.of(key, value, string, b);
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
    @ApiStatus.ScheduledForRemoval(inVersion = "1.4")
    @Deprecated
    public String replaceInsensitiveEach(String[] keys, String[] values, String string) {
        return ValueReplacer.forEach(keys, values, string);
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
    @ApiStatus.ScheduledForRemoval(inVersion = "1.4")
    @Deprecated
    public String replaceInsensitiveEach(String key, String value, String string) {
        return ValueReplacer.of(key, value, string);
    }
}
