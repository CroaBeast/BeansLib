package me.croabeast.beanslib.utility;

import com.google.common.collect.Lists;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.terminal.ActionBar;
import me.croabeast.beanslib.terminal.TitleMngr;
import me.croabeast.beanslib.utility.chars.CharHandler;
import me.croabeast.beanslib.utility.chars.CharacterInfo;
import me.croabeast.beanslib.utility.time.TimeUtils;
import me.croabeast.iridiumapi.IridiumAPI;
import org.apache.commons.lang.StringUtils;
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
public final class TextUtils {

    /**
     * A default {@link TextKeys} instance for static methods.
     */
    public static final TextKeys DEFAULT_KEYS = new TextKeys() {
        @Override
        public @NotNull String langPrefix() {
            return "";
        }

        @Override
        public boolean fixColorLogger() {
            return false;
        }

        @Override
        public boolean isHardSpacing() {
            return false;
        }

        @Override
        public boolean isStripPrefix() {
            return false;
        }
    };

    /**
     * Avoid initializing this class.
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
     * @param player a player, can be null
     * @param message the input line
     * @return the parsed message
     */
    public static String parsePAPI(Player player, String message) {
        return Exceptions.isPluginEnabled("PlaceholderAPI") ?
                PlaceholderAPI.setPlaceholders(player, message) : message;
    }

    /**
     * Removes the first spaces of a line.
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
     * Use a char pattern to find unicode values and
     * replace them with its respective characters.
     * @param regex a char pattern
     * @param string the input line
     * @return the parsed message with the new characters
     */
    public static String parseChars(String regex, String string) {
        if (StringUtils.isBlank(regex) ||
                StringUtils.isBlank(string)) return string;

        Pattern charPattern = Pattern.compile(regex);
        Matcher match = charPattern.matcher(string);

        while (match.find()) {
            char s = (char) Integer.parseInt(match.group(1), 16);
            string = string.replace(match.group(), s + "");
        }
        return string;
    }

    /**
     * Use a default {@link TextKeys#charPattern()} to find unicode values and
     * replace them with its respective characters.
     * @param string the input line
     * @return the parsed message with the new characters
     */
    public static String parseChars(String string) {
        return parseChars(DEFAULT_KEYS.charPattern(), string);
    }

    /**
     * Removes the last char from a string using a start index.
     * @param string an input string
     * @param start a start index
     * @return the stripped string
     */
    public static String removeLastChar(String string, int start) {
        return string.substring(start, string.length() - 1);
    }

    /**
     * Removes the last char from a string using the start of the string.
     * @param string an input string
     * @return the stripped string
     */
    public static String removeLastChar(String string) {
        return removeLastChar(string, 0);
    }

    /**
     * Formats an input string parsing first {@link PlaceholderAPI} placeholders,
     * replaced chars and then applying the respective colors.
     * @param target a target to parse colors depending on its client, can be null
     * @param parser a player, can be null
     * @param regex a char pattern
     * @param string the input message
     * @return the formatted message
     */
    public static String colorize(Player target, Player parser, String regex, String string) {
        if (target == null) target = parser;
        string = parsePAPI(parser, parseChars(regex, string));
        return IridiumAPI.process(target, string);
    }

    /**
     * Formats an input string parsing first {@link PlaceholderAPI}
     * placeholders, replaced chars using {@link TextKeys#charPattern()}
     * and then applying the respective colors.
     * @param target a target to parse colors depending on its client, can be null
     * @param parser a player, can be null
     * @param string the input message
     * @return the formatted message
     */
    public static String colorize(Player target, Player parser, String string) {
        return colorize(target, parser, DEFAULT_KEYS.charPattern(), string);
    }

    /**
     * Creates a centered chat message.
     * @param target a target to parse colors depending on its client, can be null
     * @param parser a player to parse placeholders.
     * @param regex a char pattern
     * @param prefix the center message prefix
     * @param string the input message
     * @return the centered chat message.
     */
    public static String centerMessage(Player target, Player parser, String regex, String prefix, String string) {
        final String output = colorize(target, parser, string);
        if (!string.startsWith(prefix)) return output;

        String initial = parseChars(regex, stripJson(string));
        initial = TextUtils.colorize(target, parser, initial);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : initial.toCharArray()) {
            if (c == '§') {
                previousCode = true;
                continue;
            }

            else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
                continue;
            }

            CharacterInfo dFI = CharHandler.getInfo(c);
            messagePxSize += isBold ?
                    dFI.getBoldLength() : dFI.getLength();
            messagePxSize++;
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = 154 - halvedMessageSize;
        int compensated = 0;

        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += 4; // 4 is the SPACE char length + 1
        }

        return sb + output;
    }

    /**
     * Creates a centered chat message.
     * @param target a target to parse colors depending on its client, can be null
     * @param parser a player to parse placeholders.
     * @param string the input message
     * @return the centered chat message.
     */
    public static String centerMessage(Player target, Player parser, String string) {
        return centerMessage(target, parser,
                DEFAULT_KEYS.charPattern(), DEFAULT_KEYS.centerPrefix(), string);
    }

    /**
     * Replace a {@link String} array of keys with another {@link String} array of values.
     * <p> It's case-insensitive and special characters are quoted to avoid errors.
     * @param string an input string
     * @param keys the array of keys
     * @param values the array of values
     * @return the parsed string with the respective values
     */
    @NotNull
    public static String replaceInsensitiveEach(String string, String[] keys, String[] values) {
        if (StringUtils.isBlank(string)) return string;

        if (keys == null || values == null) return string;
        if (keys.length > values.length) return string;

        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == null | values[i] == null) continue;

            String newKey = Pattern.quote(keys[i]);
            if (StringUtils.isBlank(newKey)) continue;

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
     * Converts the old JSON format to the new one.
     * @param string an input string
     * @return the converted string
     */
    public static String convertOldJson(String string) {
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
     * <pre> if (IS_JSON.apply(stringInput)) doSomethingIdk();</pre>
     */
    public static final Function<String, Boolean> IS_JSON =
            s -> TextKeys.JSON_PATTERN.matcher(convertOldJson(s)).find();

    /**
     * Strips the JSON format from a line.
     * @param string an input string.
     * @return the stripped line.
     */
    public static String stripJson(String string) {
        string = convertOldJson(string);
        if (!IS_JSON.apply(string)) return string;

        Matcher matcher = TextKeys.JSON_PATTERN.matcher(string);
        return matcher.find() ? matcher.group(7) : string;
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

    /**
     * Formats a class to a string defined format.
     * @param obj the object to format
     * @param split the splitter
     * @param use use quotes in every object
     * @param args values for the object
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

    /**
     * Formats a class to a string defined format.
     * @param obj the object to format
     * @param args values for the object
     * @return the formatted class
     */
    public static String classFormat(Object obj, Object... args) {
        return classFormat(obj, ":", true, args);
    }
}
