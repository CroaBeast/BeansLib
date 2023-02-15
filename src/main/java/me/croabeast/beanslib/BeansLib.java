package me.croabeast.beanslib;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.clip.placeholderapi.PlaceholderAPI;
import me.croabeast.beanslib.object.misc.PlayerKeyHandler;
import me.croabeast.beanslib.object.display.Displayer;
import me.croabeast.beanslib.object.misc.BeansLogger;
import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.beanslib.utility.chars.CharHandler;
import me.croabeast.beanslib.utility.chars.CharacterInfo;
import me.croabeast.iridiumapi.IridiumAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.croabeast.beanslib.utility.TextUtils.parsePAPI;
import static me.croabeast.beanslib.utility.TextUtils.stripJson;

/**
 * The main class of the Lib.
 * It has a lot of useful text-related methods.
 *
 * @author CroaBeast
 * @since 1.0
 */
@Accessors(chain = true)
@Getter @Setter
public class BeansLib {

    private static final Map<Integer, MessageKey> MESSAGE_KEY_MAP = new HashMap<>();

    /**
     * The static instance of the lib that doesn't have a plugin's implementation.
     *
     * <p> Methods that use a plugin instance like {@link #doLog(CommandSender, String...)},
     * sending bossbar messages and other related will not work.
     */
    public static final BeansLib DEFAULTS = new BeansLib(null);

    /**
     * The {@link Plugin} instance of your project.
     */
    private final Plugin plugin;

    @Getter(AccessLevel.NONE)
    private final BeansLogger logger;

    /**
     * The {@link PlayerKeyHandler} instance to parse the respective player
     * values in strings.
     */
    private PlayerKeyHandler playerKeyHandler;

    /**
     * The prefix of the plugin that will replace the prefix key: {@link #getLangPrefixKey()}.
     */
    private String langPrefix;

    /**
     * A key that will be replaced by the main plugin prefix: {@link #getLangPrefix()}.
     */
    private String langPrefixKey = "<P>";
    /**
     * The center prefix to define a center chat message.
     */
    private String centerPrefix = "[C]";
    /**
     * The line splitter or separator to split multiple chat lines
     * or split a title message between title and subtitle.
     *
     * <p> If you want to override this string, you need to quote
     * your own one using {@link Pattern#quote(String)}.
     *
     * <p> Example: {@code Pattern.quote("my own string");}
     */
    private String lineSeparator = Pattern.quote("<n>");

    /**
     * The character regex pattern that replace the specified char.
     * This regex should have only 1 group to catch.
     */
    private String charRegex = "<U:([a-fA-F0-9]{4})>";

    /**
     * If the console can use colors or not. Some consoles have color support disabled.
     */
    private boolean coloredConsole = false;
    /**
     * If you want to remove the message-type prefix. Ex: [title], [json]
     */
    private boolean stripPrefix = false;

    /**
     * The {@link ConfigurationSection} object to get all the available webhooks.
     */
    private ConfigurationSection webhookSection = null;
    /**
     * The {@link ConfigurationSection} object to get all the custom boss-bars.
     */
    private ConfigurationSection bossbarSection = null;

    /**
     * In, stay and out ticks for a title message.
     * <pre> {@code
     * int[] ticks = defaultTitleTicks();
     * // Respective values
     * int fadeIn = ticks[0];
     * int stay = ticks[1];
     * int fadeout = ticks[2];
     * }</pre>
     */
    @Setter(AccessLevel.NONE)
    private int[] defaultTitleTicks = {8, 50, 8};

    @Setter(AccessLevel.NONE)
    private String[] keysDelimiters = {"[", "]"};

    @Getter(AccessLevel.NONE)
    private final MessageKey titleKey, jsonKey, aBarKey, bossbarKey, webhookKey;

    /**
     * Creates a new instance of the lib using a {@link Plugin} implementation.
     *
     * <p> Methods that use a plugin instance like {@link #doLog(CommandSender, String...)},
     * sending bossbar messages and other related will not work if plugin is null.
     *
     * @param plugin plugin's instance
     */
    public BeansLib(@Nullable Plugin plugin) {
        this.plugin = plugin;

        logger = new BeansLogger(this);
        playerKeyHandler = new PlayerKeyHandler();

        langPrefix = "&e " + (plugin == null ? "JavaPlugin" : plugin.getName()) + " &8»&7";

        aBarKey = new MessageKey(this, "action-bar", null).doColor();
        titleKey = new MessageKey(this, "title", "(:\\d+)?").doColor();
        jsonKey = new MessageKey(this, "json", null);
        webhookKey = new MessageKey(this, "webhook", "(:.+)?");
        bossbarKey = new MessageKey(this, "bossbar", "(:.+)?");
    }

    /**
     * Sets the default title ticks values.
     *
     * @param in in server ticks
     * @param stay stay server ticks
     * @param out out server ticks
     *
     * @return a reference of this object
     */
    public BeansLib setTitleTicks(int in, int stay, int out) {
        defaultTitleTicks = new int[] {in, stay, out};
        return this;
    }

    /**
     * Sets the delimiters for the messages keys to identify what type of
     * message is a string to be displayed.
     *
     * <p> Example: default: [title] - custom: {{title}}
     *
     * @param start start delimiter string
     * @param end end delimiter string
     *
     * @return a reference of this object
     */
    public BeansLib setKeysDelimiters(String start, String end) {
        keysDelimiters = new String[] {start, end};
        return this;
    }

    /**
     * Sets the values of a specific {@link MessageKey} object stored.
     *
     * <ul>
     *   <li>0: action bar key - "[action-bar]"</li>
     *   <li>1: title key - "[title]" - "(:\d+)?"</li>
     *   <li>2: json key - "[json]"</li>
     *   <li>3: webhook key - "[webhook]" - "(:.+)?"</li>
     * </ul>
     *
     * @param index the respective index of the key
     * @param key a prefix key
     * @param regex regex pattern, can be null if no extra pattern will be applicable
     *
     * @return a reference of this object
     */
    public BeansLib setMessageKeyValues(int index, @NotNull String key, @Nullable String regex) {
        if (StringUtils.isBlank(key)) return this;
        if (index > 3) return this;

        MessageKey k = MESSAGE_KEY_MAP.getOrDefault(index, null);
        if (k != null) k.setKey(key).setRegex(regex);

        return this;
    }

    /**
     * Returns if it will fix an RGB issue in some servers that RGB
     * not working correctly.
     *
     * @deprecated check and/or override the "coloredConsole" variable
     * @return if this fix is enabled
     */
    @Deprecated
    public boolean fixColorLogger() {
        return !coloredConsole;
    }

    /**
     * Returns the key instance of an input string to check what message
     * type is the string. If there is no type or a chat message, will return null.
     *
     * @param s an input string
     * @return the requested message key
     */
    @Nullable
    public MessageKey identifyMessageType(String s) {
        if (StringUtils.isBlank(s)) return null;

        for (MessageKey key : MESSAGE_KEY_MAP.values())
            if (key.getPattern().matcher(s).find()) return key;

        return null;
    }

    /**
     * Use a char pattern to find unicode values and replace them with
     * its respective characters.
     *
     * @param string the input line
     *
     * @return the parsed message with the new characters
     */
    public String parseChars(String string) {
        if (StringUtils.isBlank(string)) return string;

        Matcher m = Pattern.compile(getCharRegex()).matcher(string);

        while (m.find()) {
            char s = (char) Integer.parseInt(m.group(1), 16);
            string = string.replace(m.group(), s + "");
        }

        return string;
    }

    /**
     * Formats an input string parsing first {@link PlaceholderAPI} placeholders,
     * replaced chars and then applying the respective colors.
     *
     * @param target a target to parse colors depending on its client, can be null
     * @param parser a player, can be null
     * @param string the input message
     *
     * @return the formatted message
     */
    public String colorize(Player target, Player parser, String string) {
        if (target == null) target = parser;
        string = TextUtils.parsePAPI(parser, parseChars(string));
        return IridiumAPI.process(target, string);
    }

    /**
     * Formats a string to a centered string, that has a perfect amount of spaces
     * before the actual string to be display in the chat as a centered message.
     *
     * @param target a target to parse colors depending on its client, can be null
     * @param parser a player to parse placeholders.
     * @param string the input message
     *
     * @return the centered chat message.
     */
    public String centerMessage(Player target, Player parser, String string) {
        String prefix = getCenterPrefix();

        final String output = colorize(target, parser, string);
        if (!string.startsWith(prefix)) return output;

        string = string.substring(prefix.length());

        String initial = parseChars(TextUtils.stripJson(string));
        initial = colorize(target, parser, initial);

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
            compensated += 4; // 4 is the SPACE char length (3) + 1
        }

        return sb + output.substring(prefix.length());
    }

    /**
     * Parses the players keys defined in your {@link #playerKeyHandler} object
     * to its respective player variables.
     *
     * @param parser a player
     * @param string an input string
     * @param c if keys are case-sensitive
     *
     * @return the requested string
     */
    public String parsePlayerKeys(Player parser, String string, boolean c) {
        return playerKeyHandler.parseKeys(parser, string, c);
    }

    /**
     * Sends requested information for a {@link Player}.
     *
     * @param player a valid online player
     * @param lines the information to send
     */
    public void playerLog(@NotNull Player player, String... lines) {
        logger.playerLog(player, lines);
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console.
     *
     * @param lines the information to send
     */
    public void rawLog(String... lines) {
        logger.rawLog(lines);
    }

    /**
     * Sends requested information to a {@link CommandSender}.
     *
     * @param sender a valid sender, can be the console or a player
     * @param lines the information to send
     */
    public void doLog(@Nullable CommandSender sender, String... lines) {
        logger.doLog(sender, lines);
    }

    /**
     * Sends requested information to the console.
     *
     * @param lines the information to send
     */
    public void doLog(String... lines) {
        doLog(null, lines);
    }

    /**
     * Creates a {@link Displayer} instance using the lib as a reference.
     *
     * @param targets a CommandSender targets, can be null
     * @param parser  a player to parse values, can be null
     * @param list    a string list
     * @param flags   an array of flags to allow certain message types
     *
     * @return a {@link Displayer} instance
     */
    public Displayer create(Collection<? extends CommandSender> targets,
                            Player parser, List<String> list, String... flags) {
        return new Displayer(this, targets, parser, list, flags);
    }

    /**
     * Creates a {@link Displayer} instance using the lib as a reference.
     *
     * @param target a CommandSender target, can be null
     * @param parser  a player to parse values, can be null
     * @param list    a string list
     * @param flags   an array of flags to allow certain message types
     *
     * @return a {@link Displayer} instance
     */
    public Displayer create(CommandSender target, Player parser, List<String> list, String... flags) {
        return new Displayer(this, target, parser, list, flags);
    }

    /**
     * Creates a {@link Displayer} instance using the lib as a reference.
     *
     * @param parser  a player to parse values, can be null
     * @param list    a string list
     * @param flags   an array of flags to allow certain message types
     *
     * @return a {@link Displayer} instance
     */
    public Displayer create(Player parser, List<String> list, String... flags) {
        return new Displayer(this, parser, list, flags);
    }

    /**
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String, String[], String[])}
     *
     * @param sender a sender to format and send the message
     * @param list the message list
     * @param keys a keys array
     * @param values a values array
     *
     * @deprecated See {@link Displayer} and its constructor.
     */
    @Deprecated
    public void sendMessageList(CommandSender sender, List<String> list, String[] keys, String[] values) {
        create(sender, null, list).
                setKeys(keys).setValues(values).setLogger(false).
                setCaseSensitive(false).display();
    }

    /**
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String, String[], String[])}
     *
     * @param sender a sender to format and send the message
     * @param section the config file or section
     * @param path the path of the string or string list
     * @param keys a keys array
     * @param values a values array
     *
     * @deprecated See {@link Displayer} and its constructor.
     */
    @Deprecated
    public void sendMessageList(CommandSender sender, ConfigurationSection section, String path, String[] keys, String[] values) {
        sendMessageList(sender, TextUtils.toList(section, path), keys, values);
    }

    /**
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String, String[], String[])}
     *
     * @param sender a sender to format and send the message
     * @param list the message list
     *
     * @deprecated See {@link Displayer} and its constructor.
     */
    @Deprecated
    public void sendMessageList(CommandSender sender, List<String> list) {
        sendMessageList(sender, list, null, null);
    }

    /**
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String, String[], String[])}
     *
     * @param sender a sender to format and send the message
     * @param section the config file or section
     * @param path the path of the string or string list
     *
     * @deprecated See {@link Displayer} and its constructor.
     */
    @Deprecated
    public void sendMessageList(CommandSender sender, ConfigurationSection section, String path) {
        sendMessageList(sender, TextUtils.toList(section, path));
    }

    /**
     * The MessageKey class manages how to identify a message type if it has
     * a respective registered prefix and an optional additional regex parameter
     * to check if more arguments will be needed to identify the message type.
     *
     * <p> This class can not have more instances to avoid errors
     * with the existing keys.
     */
    @Accessors(chain = true)
    @Setter(AccessLevel.PRIVATE)
    public static class MessageKey {

        private static int ordinal = 0;

        private final BeansLib lib;

        /**
         * The main key of this object to identify it.
         */
        @NotNull @Getter
        private String key;
        @Nullable
        private String regex;

        @Setter(AccessLevel.NONE)
        private boolean useColor = false;

        private MessageKey(BeansLib lib, @NotNull String key, @Nullable String regex) {
            this.lib = lib;
            this.key = key;
            this.regex = regex;

            MESSAGE_KEY_MAP.put(ordinal, this);
            ordinal++;
        }

        private String start() {
            return lib.getKeysDelimiters()[0];
        }

        private String end() {
            return lib.getKeysDelimiters()[1];
        }

        /**
         * The main key of this object to identify it in upper-case.
         *
         * @return the requested key
         */
        public String getUpperKey() {
            return getKey().toUpperCase(Locale.ENGLISH);
        }

        /**
         * Creates a regex pattern in staring format using the key and the
         * optional regex parameter.
         *
         * <p> It will be case-insensitive and always check at the start of a string.
         *
         * @return the requested regex string
         */
        private String getRegex() {
            String s = StringUtils.isBlank(regex) ? key : (key + regex);
            return "(?i)^" + Pattern.quote(start()) + s + Pattern.quote(end());
        }

        private MessageKey doColor() {
            this.useColor = true;
            return this;
        }

        /**
         * Creates a {@link Pattern} instance using the created {@link #getRegex()}.
         *
         * @return the requested regex
         */
        public Pattern getPattern() {
            return Pattern.compile(getRegex());
        }

        /**
         * Formats an input string, colorizing it, parsing placeholders
         * and removing the prefix.
         *
         * @param target a target to send
         * @param parser a player to parse
         * @param s an input string
         *
         * @return the requested string
         */
        public String formatString(Player target, Player parser, String s) {
            Matcher m = getPattern().matcher(s);

            while (m.find()) s = s.replace(m.group(), "");
            s = TextUtils.removeSpace(s);

            if (!useColor) {
                String s1 = parsePAPI(parser, lib.parseChars(s));
                return IridiumAPI.stripAll(stripJson(s1));
            }

            return lib.colorize(target, parser, s);
        }
    }
}
