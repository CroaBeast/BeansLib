package me.croabeast.beanslib;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.var;
import me.clip.placeholderapi.PlaceholderAPI;
import me.croabeast.beanslib.character.SmallCaps;
import me.croabeast.beanslib.key.KeyManager;
import me.croabeast.beanslib.message.CenteredMessage;
import me.croabeast.beanslib.message.MessageSender;
import me.croabeast.beanslib.logger.BeansLogger;
import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.neoprismatic.NeoPrismaticAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The main class of this lib. Most of the variables can be changed
 * using chain setters.
 *
 * @author CroaBeast
 * @since 1.0
 */
@Accessors(chain = true)
@Getter @Setter
public class BeansLib {

    @Getter(AccessLevel.NONE)
    @Nullable
    private final Plugin plugin;

    @Getter(AccessLevel.NONE)
    private final BeansLogger logger;

    /**
     * The {@link KeyManager} instance to parse the respective player
     * values in strings.
     */
    private KeyManager keyManager;

    /**
     * The plugin's prefix that will replace the prefix key: {@link #getLangPrefixKey()}.
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
     * The line splitter or separator to split multiple chat lines or split a title
     * message between title and subtitle.
     *
     * <p> If you want to override this string, the custom separator should be quoted
     * using {@link Pattern#quote(String)}.
     *
     * <pre> {@code
     * // Example of how setting a custom separator
     * lib.setLineSeparator(Pattern.quote("{split}"));
     * } </pre>
     */
    private String lineSeparator = Pattern.quote("<n>");

    /**
     * <p> The character regex pattern replace a 4-digit unicode code with a specific
     * char using the unique group in the pattern.
     *
     * <p> Note: some unicode chars doesn't work with minecraft chat itself.
     *
     * <pre> {@code
     * // Examples:
     * String heart = "<U:2764>"; //U+2764 = ❤️
     * String check = "<u:2714>"; //U+2714 = ✔️️
     * String airplane = "<U:2708>"; //U+2708 = ✈️
     * } </pre>
     */
    @Getter(AccessLevel.NONE)
    private String charRegex = "<[Uu]:([a-fA-F\\d]{4})>";

    /**
     * The regex pattern that identifies if an input line can be replaced with a
     * custom bossbar message stored in the {@link #bossbarSection}.
     *
     * <p> Ignores the spaces before and after the placeholder group.
     */
    @Getter(AccessLevel.NONE)
    private String bossbarRegex = "%bossbar:(.+)%";

    /**
     * The regex pattern that identifies if an input line should insert an
     * amount of blank spaces.
     *
     * <p> Ignores the spaces before and after the placeholder group.
     */
    @Getter(AccessLevel.NONE)
    private String blankSpaceRegex = "<add_space:(\\d+)>";

    /**
     * The regex pattern that identifies if an input line should convert all its
     * characters to {@link SmallCaps} characters.
     */
    @Getter(AccessLevel.NONE)
    private String smallCapsPattern = "<(small_caps|sc)>(.+?)</(small_caps|sc)>";

    /**
     * If the console can use colors or not. Some consoles don't have color support.
     */
    private boolean coloredConsole = true;
    /**
     * If you want to remove the message-type prefix from log lines. Ex: [title], [json]
     */
    private boolean stripPrefix = false;

    /**
     * The {@link ConfigurationSection} object to get all the available webhooks.
     */
    private ConfigurationSection webhookSection = null;
    /**
     * The {@link ConfigurationSection} object to get all the custom bossbars.
     */
    private ConfigurationSection bossbarSection = null;

    /**
     * In, stay and out ticks for a title message.
     *
     * <pre> {@code
     * int[] ticks = defaultTitleTicks();
     * // Respective values
     * int fadeIn = ticks[0];
     * int stay = ticks[1];
     * int fadeout = ticks[2];
     * } </pre>
     */
    @Setter(AccessLevel.NONE)
    private int @NotNull [] defaultTitleTicks = {8, 50, 8};

    /**
     * The delimiters of all the messages keys to identify what type of
     * message is a string to be displayed.
     *
     * <p> Example: default: [title] - custom: {{title}}
     */
    @Setter(AccessLevel.NONE)
    private String @NotNull [] keysDelimiters = {"[", "]"};

    BeansLib(@Nullable Plugin plugin, boolean load) {
        this.plugin = plugin;

        keyManager = new KeyManager();
        logger = new BeansLogger(this);

        langPrefix = "&e " + (this.plugin != null ?
                this.plugin.getName() :
                "JavaPlugin") + " &8»&7";

        if (Beans.lib == null && load)
            Beans.setLib(this);
    }

    /**
     * Creates a new instance of the lib using a {@link Plugin} implementation.
     *
     * <p> if the plugin instance is null, methods that use a plugin instance
     * like logging with a plugin prefix, sending bossbar messages and others
     * plugin-related will not work.
     *
     * @param plugin plugin's instance
     */
    public BeansLib(@Nullable Plugin plugin) {
        this(plugin, true);
    }

    public BeansLib() {
        this(((Function<Class<?>, Plugin>) c -> {
            try {
                return JavaPlugin.getProvidingPlugin(c);
            } catch (Exception e) {
                return null;
            }
        }).apply(BeansLib.class));
    }

    /**
     * The {@link Plugin} instance of your project.
     *
     * @throws NullPointerException if the plugin is null
     * @return plugin's instance
     */
    @NotNull
    public Plugin getPlugin() throws NullPointerException {
        return Objects.requireNonNull(plugin, "Plugin instance can not be null");
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
    public final BeansLib setTitleTicks(int in, int stay, int out) {
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
    public final BeansLib setKeysDelimiters(String start, String end) {
        keysDelimiters = new String[] {start, end};
        return this;
    }

    /**
     * Returns if it will fix an RGB issue in some servers that RGB
     * not working correctly.
     *
     * @deprecated check and/or set the "coloredConsole" variable
     * @return if this fix is enabled
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.4")
    @Deprecated
    public boolean fixColorLogger() {
        return !isColoredConsole();
    }

    /**
     * Replace the {@link #langPrefixKey} with the {@link #langPrefix}.
     *
     * @param string an input string
     * @param remove if the prefix will be removed
     *
     * @return the formatted string
     */
    public String replacePrefixKey(String string, boolean remove) {
        return StringUtils.isBlank(string) ? string :
                string.replace(getLangPrefixKey(), remove ? "" : getLangPrefix());
    }

    /**
     * Creates a new string array from an input string using the {@link #lineSeparator}
     * as a split for the array.
     *
     * <p> You can define the limit of the array.
     *
     * @param s an input string
     * @param limit a limit
     *
     * @return the requested array
     */
    public String[] splitLine(String s, int limit) {
        return s.split(getLineSeparator(), limit);
    }

    /**
     * Creates a new string array from an input string using the {@link #lineSeparator}
     * as a split for the array.
     *
     * @param s an input string
     * @return the requested array
     */
    public String[] splitLine(String s) {
        return splitLine(s, 0);
    }

    /**
     * Creates a new {@link Pattern} instance using the defined char regex string.
     *
     * @return the requested pattern
     */
    public Pattern getCharPattern() {
        return Pattern.compile(charRegex);
    }

    /**
     * Creates a new {@link Pattern} instance using the defined custom bossbar
     * internal placeholder.
     *
     * @return the requested pattern
     */
    public Pattern getBossbarPattern() {
        return Pattern.compile("(?i)^ *?" + bossbarRegex + " *?$");
    }

    /**
     * Creates a new {@link Pattern} instance using the defined blank-space
     * internal placeholder.
     *
     * @return the requested pattern
     */
    public Pattern getBlankPattern() {
        return Pattern.compile("(?i)^ *?" + blankSpaceRegex + " *?$");
    }

    /**
     * Creates a new {@link Pattern} instance using the defined SmallCaps
     * internal placeholder.
     *
     * @return the requested pattern
     */
    public Pattern getSmallCapsPattern() {
        return Pattern.compile("(?i)" + smallCapsPattern);
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

        Matcher m = getCharPattern().matcher(string);

        while (m.find()) {
            char c = (char) Integer.parseInt(m.group(1), 16);
            string = string.replace(m.group(), c + "");
        }

        return string;
    }

    public String convertToSmallCaps(String string) {
        if (StringUtils.isBlank(string))
            return string;

        var matcher = getSmallCapsPattern().matcher(string);

        while (matcher.find()) {
            String text = matcher.group(2);

            string = string.replace(
                    matcher.group(),
                    SmallCaps.toSmallCaps(text)
            );
        }

        return string;
    }

    public String formatPlaceholders(@Nullable Player parser, String string) {
        string = getKeyManager().parseKeys(parser, string, false);
        return TextUtils.PARSE_PLACEHOLDERAPI.apply(parser, parseChars(string));
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
        return NeoPrismaticAPI.colorize(target, formatPlaceholders(parser, string));
    }

    /**
     * Formats an input string parsing first {@link PlaceholderAPI} placeholders,
     * replaced chars and then applying the respective colors.
     *
     * @param player a player, can be null
     * @param string the input message
     *
     * @return the formatted message
     */
    public String colorize(Player player, String string) {
        return colorize(player, player, string);
    }

    /**
     * Formats an input string parsing first {@link PlaceholderAPI} placeholders,
     * replaced chars and then applying the respective colors.
     *
     * @param string the input message
     * @return the formatted message
     */
    public String colorize(String string) {
        return colorize(null, string);
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
    public String createCenteredChatMessage(Player target, Player parser, String string) {
        return CenteredMessage.toChat(parser, target, string);
    }

    /**
     * Formats a string to a centered string, that has a perfect amount of spaces
     * before the actual string to be display in the chat as a centered message.
     *
     * @param player a player to parse placeholders.
     * @param string the input message
     *
     * @return the centered chat message.
     */
    public String createCenteredChatMessage(Player player, String string) {
        return createCenteredChatMessage(player, player, string);
    }

    /**
     * Formats a string to a centered string, that has a perfect amount of spaces
     * before the actual string to be display in the chat as a centered message.
     *
     * @param string the input message
     * @return the centered chat message.
     */
    public String createCenteredChatMessage(String string) {
        return createCenteredChatMessage(null, string);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "1.4")
    @Deprecated
    public String centerMessage(Player target, Player parser, String string) {
        return createCenteredChatMessage(target, parser, string);
    }

    /**
     * Parses the players keys defined in your {@link #keyManager} object
     * to its respective player variables.
     *
     * @param parser a player
     * @param string an input string
     * @param c if keys are case-sensitive
     *
     * @return the requested string
     */
    public final String parsePlayerKeys(Player parser, String string, boolean c) {
        return getKeyManager().parseKeys(parser, string, c);
    }

    /**
     * Parses the players keys defined in your {@link #keyManager} object
     * to its respective player variables. Keys are case-insensitive.
     *
     * @param parser a player
     * @param string an input string
     *
     * @return the requested string
     */
    public final String parsePlayerKeys(Player parser, String string) {
        return parsePlayerKeys(parser, string, false);
    }

    /**
     * Sends information to a player using the {@link MessageSender} object.
     *
     * @param player a valid player
     * @param lines the information to send
     */
    public final void playerLog(@NotNull Player player, String... lines) {
        logger.playerLog(player, lines);
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console,
     * avoiding the plugin prefix.
     *
     * @param lines the information to send
     */
    public final void rawLog(String... lines) {
        logger.rawLog(lines);
    }

    /**
     * Sends requested information to a {@link CommandSender} using the plugin's
     * logger and its prefix.
     *
     * <p> If the sender is a {@link Player} and it's not null, it will log using
     * {@link #playerLog(Player, String...)}.
     *
     *
     * @param sender a valid sender, can be the console, a player or null
     * @param lines the information to send
     *
     * @throws NullPointerException if the plugin is null
     */
    public final void doLog(@Nullable CommandSender sender, String... lines) {
        logger.doLog(sender, lines);
    }

    /**
     * Sends requested information to a {@link CommandSender} using the plugin's
     * logger and its prefix.
     *
     * @param lines the information to send
     * @throws NullPointerException if the plugin is null
     */
    public final void doLog(String... lines) {
        logger.doLog(lines);
    }

    /**
     * Sends information choosing which of the two main methods will be used in each line.
     * ({@link #rawLog(String...) rawLog}, {@link #doLog(CommandSender, String...) doLog})
     *
     * <p> If the line does not start with a boolean value or that value is false,
     * it will use the {@link #doLog(CommandSender, String...) doLog} method, otherwise
     * will use the {@link #rawLog(String...) rawLog} method.
     *
     * <pre> {@code
     * "My information for the console" >> // Uses "doLog"
     * "true::My basic log information" >> // Uses "rawLog"
     * "false::Some plugin's information" >> // Uses "doLog"
     * "" or null >> // Uses "doLog", 'cause is empty/null
     * } </pre>
     *
     * @param sender a valid sender, can be the console, a player or null
     * @param lines the information to send
     */
    public final void mixLog(CommandSender sender, String... lines) {
        logger.mixLog(sender, lines);
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof BeansLib)) return false;

        BeansLib lib = (BeansLib) o;
        return Objects.equals(lib.plugin, plugin);
    }

    /**
     * Sends information choosing which of the two main methods will be used in each line.
     * ({@link #rawLog(String...) rawLog}, {@link #doLog(CommandSender, String...) doLog})
     *
     * <p> If the line does not start with a boolean value or that value is false,
     * it will use the {@link #doLog(CommandSender, String...) doLog} method, otherwise
     * will use the {@link #rawLog(String...) rawLog} method.
     *
     * <pre> {@code
     * "My information for the console" >> // Uses "doLog"
     * "true::My basic log information" >> // Uses "rawLog"
     * "false::Some plugin's information" >> // Uses "doLog"
     * "" or null >> // Uses "doLog", 'cause is empty/null
     * } </pre>
     *
     * @param lines the information to send
     */
    public final void mixLog(String... lines) {
        logger.mixLog(lines);
    }

    /**
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String[], String[], String)}
     *
     * @param sender a sender to format and send the message
     * @param list the message list
     * @param keys a keys array
     * @param values a values array
     *
     * @deprecated See {@link MessageSender} and its constructor.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.4")
    @Deprecated
    public void sendMessageList(CommandSender sender, List<String> list, String[] keys, String[] values) {
        new MessageSender().
                setTargets(sender).
                setKeys(keys).
                setValues(values).
                setLogger(false).
                setCaseSensitive(false).
                send(list);
    }

    /**
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String[], String[], String)}
     *
     * @param sender a sender to format and send the message
     * @param section the config file or section
     * @param path the path of the string or string list
     * @param keys a keys array
     * @param values a values array
     *
     * @deprecated See {@link MessageSender} and its constructor.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.4")
    @Deprecated
    public void sendMessageList(CommandSender sender, ConfigurationSection section, String path, String[] keys, String[] values) {
        sendMessageList(sender, TextUtils.toList(section, path), keys, values);
    }

    /**
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String[], String[], String)}
     *
     * @param sender a sender to format and send the message
     * @param list the message list
     *
     * @deprecated See {@link MessageSender} and its constructor.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.4")
    @Deprecated
    public void sendMessageList(CommandSender sender, List<String> list) {
        sendMessageList(sender, list, null, null);
    }

    /**
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String[], String[], String)}
     *
     * @param sender a sender to format and send the message
     * @param section the config file or section
     * @param path the path of the string or string list
     *
     * @deprecated See {@link MessageSender} and its constructor.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.4")
    @Deprecated
    public void sendMessageList(CommandSender sender, ConfigurationSection section, String path) {
        sendMessageList(sender, TextUtils.toList(section, path));
    }
}
