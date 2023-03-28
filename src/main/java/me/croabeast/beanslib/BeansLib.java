package me.croabeast.beanslib;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.var;
import me.clip.placeholderapi.PlaceholderAPI;
import me.croabeast.beanslib.character.CharHandler;
import me.croabeast.beanslib.key.KeyManager;
import me.croabeast.beanslib.message.MessageSender;
import me.croabeast.beanslib.misc.BeansLogger;
import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.iridiumapi.IridiumAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
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

    /**
     * The static instance of the lib that doesn't have a plugin's implementation.
     *
     * <p> Methods that use a plugin instance like logging with a plugin prefix,
     * sending bossbar messages and others plugin-related will throw a {@link NullPointerException}.
     */
    private static final BeansLib NO_PLUGIN_INSTANCE = new BeansLib(null);

    private static BeansLib loadedInstance = null;

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
    private String blankSpaceRegex = "<ADD_SPACE:(\\d+)>";

    /**
     * A prefix used in the main pattern to identify the json event.
     */
    @Getter(AccessLevel.NONE)
    private String jsonPrefix = "(.[^|]*?):\"(.[^|]*?)\"";

    /**
     * The main pattern to identify the JSON message in a string.
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
    @Getter(AccessLevel.NONE)
    private String jsonRegex = "<(" + jsonPrefix + "([|]" + jsonPrefix + ")?)>(.+?)</text>";

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
        this.plugin = plugin;

        keyManager = new KeyManager();
        logger = new BeansLogger(this);

        langPrefix = "&e " + (this.plugin == null ?
                "JavaPlugin" : this.plugin.getName()) + " &8»&7";

        if (loadedInstance == null) loadedInstance = this;
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
    @ApiStatus.ScheduledForRemoval(inVersion = "1.5")
    @Deprecated
    public boolean fixColorLogger() {
        return !coloredConsole;
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
        return string.replace(getLangPrefixKey(), remove ? "" : getLangPrefix());
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
        return s.split(lineSeparator, limit);
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
     * Creates a new {@link Pattern} instance using the defined in-built
     * json pattern.
     *
     * @return the requested pattern
     */
    public Pattern getJsonPattern() {
        return Pattern.compile("(?i)" + jsonRegex);
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

        var m = getCharPattern().matcher(string);

        while (m.find()) {
            var s = (char) Integer.parseInt(m.group(1), 16);
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

        string = TextUtils.PARSE_PLACEHOLDERAPI.apply(parser, parseChars(string));
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
        final var prefix = getCenterPrefix();

        final var output = colorize(target, parser, string);
        if (!string.startsWith(prefix)) return output;

        string = string.substring(prefix.length());

        var initial = parseChars(TextUtils.STRIP_JSON.apply(string));
        initial = colorize(target, parser, initial);

        var messagePxSize = 0;
        var previousCode = false;
        var isBold = false;

        for (var c : initial.toCharArray()) {
            if (c == '§') {
                previousCode = true;
                continue;
            }

            else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
                continue;
            }

            var dFI = CharHandler.getInfo(c);
            messagePxSize += isBold ?
                    dFI.getBoldLength() : dFI.getLength();
            messagePxSize++;
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = 154 - halvedMessageSize;
        int compensated = 0;

        var sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += 4; // 4 is the SPACE char length (3) + 1
        }

        return sb + output.substring(prefix.length());
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
        return keyManager.parseKeys(parser, string, c);
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
     * Sends requested information for a {@link Player}.
     *
     * @param player a valid online player
     * @param lines the information to send
     */
    public final void playerLog(@NotNull Player player, String... lines) {
        logger.playerLog(player, lines);
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console.
     *
     * @param lines the information to send
     */
    public final void rawLog(String... lines) {
        logger.rawLog(lines);
    }

    /**
     * Sends requested information to a {@link CommandSender}.
     *
     * @param sender a valid sender, can be the console or a player
     * @param lines the information to send
     */
    public final void doLog(@Nullable CommandSender sender, String... lines) {
        logger.doLog(sender, lines);
    }

    /**
     * Sends requested information to the console.
     *
     * @param lines the information to send
     */
    public final void doLog(String... lines) {
        doLog(null, lines);
    }

    /**
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String, String[], String[])}
     *
     * @param sender a sender to format and send the message
     * @param list the message list
     * @param keys a keys array
     * @param values a values array
     *
     * @deprecated See {@link MessageSender} and its constructor.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.5")
    @Deprecated
    public void sendMessageList(CommandSender sender, List<String> list, String[] keys, String[] values) {
        new MessageSender().
                setTargets(sender).
                setKeys(keys).
                setValues(values).
                setLogger(false).
                setCaseSensitive(false).
                send(false, list);
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
     * @deprecated See {@link MessageSender} and its constructor.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.5")
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
     * @deprecated See {@link MessageSender} and its constructor.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.5")
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
     * @deprecated See {@link MessageSender} and its constructor.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.5")
    @Deprecated
    public void sendMessageList(CommandSender sender, ConfigurationSection section, String path) {
        sendMessageList(sender, TextUtils.toList(section, path));
    }

    /**
     * Returns the static instance of the lib that is loaded by a plugin using this lib.
     * If there is no loaded instance, will return the {@link #NO_PLUGIN_INSTANCE}.
     *
     * <p> To avoid the no-plugin result, a lib instance should be initialized in the
     * main class of the project, on {@link JavaPlugin#onLoad()} or {@link JavaPlugin#onEnable()}.
     *
     * <pre> {@code
     * "Initialization example of the lib"
     * public void onEnable() {
     *     lib = new BeansLib(this);
     * }} </pre>
     *
     * Methods that depends on the plugin's instance, such as {@link #doLog(String...)},
     * will throw an {@link NullPointerException} if the {@link #NO_PLUGIN_INSTANCE} is
     * returned or if the instance has no plugin implementation.
     *
     * @return loaded instance
     */
    @NotNull
    public static BeansLib getLoadedInstance() {
        return loadedInstance == null ? NO_PLUGIN_INSTANCE : loadedInstance;
    }
}
