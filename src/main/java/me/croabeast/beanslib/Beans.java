package me.croabeast.beanslib;

import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import me.croabeast.beanslib.key.KeyManager;
import me.croabeast.beanslib.message.MessageSender;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents the lib core, for the lib singleton handling.
 */
@UtilityClass
public class Beans {

    /**
     * The static instance of the lib that doesn't have a plugin's implementation.
     *
     * <p> Methods that use a plugin instance like logging with a plugin prefix,
     * sending bossbar messages and others plugin-related will throw a {@link NullPointerException}.
     */
    private final BeansLib NO_PLUGIN_INSTANCE = new BeansLib(null, false);

    BeansLib lib = null;

    public void setLib(BeansLib lib) {
        Beans.lib = Objects.requireNonNull(lib);
    }

    /**
     * The {@link KeyManager} instance to parse the respective player
     * values in strings.
     *
     * @return the key manager instance
     */
    public KeyManager getKeyManager() {
        return lib.getKeyManager();
    }

    /**
     * The plugin's prefix that will replace the prefix key: {@link #getLangPrefixKey()}.
     *
     * @return the lang prefix
     */
    public String getLangPrefix() {
        return lib.getLangPrefix();
    }

    /**
     * A key that will be replaced by the main plugin prefix: {@link #getLangPrefix()}.
     *
     * @return the lang prefix key
     */
    public String getLangPrefixKey() {
        return lib.getLangPrefixKey();
    }

    /**
     * The center prefix to define a center chat message.
     *
     * @return the center prefix
     */
    public String getCenterPrefix() {
        return lib.getCenterPrefix();
    }

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
     *
     * @return the line separator
     */
    public String getLineSeparator() {
        return lib.getLineSeparator();
    }

    /**
     * The {@link ConfigurationSection} object to get all the available webhooks.
     *
     * @return the webhook section
     */
    public ConfigurationSection getWebhookSection() {
        return lib.getWebhookSection();
    }

    /**
     * The {@link ConfigurationSection} object to get all the custom bossbars.
     *
     * @return the bossbar section
     */
    public ConfigurationSection getBossbarSection() {
        return lib.getBossbarSection();
    }

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
     *
     * @return the default title ticks array
     */
    public int @NotNull [] getDefaultTitleTicks() {
        return lib.getDefaultTitleTicks();
    }

    /**
     * The delimiters of all the messages keys to identify what type of
     * message is a string to be displayed.
     *
     * <p> Example: default: [title] - custom: {{title}}
     *
     * @return tje keys delimiters array
     */
    public String @NotNull [] getKeysDelimiters() {
        return lib.getKeysDelimiters();
    }

    /**
     * Creates a new {@link Pattern} instance using the defined char regex string.
     *
     * @return the requested pattern
     */
    public Pattern getCharPattern() {
        return lib.getCharPattern();
    }

    /**
     * Creates a new {@link Pattern} instance using the defined custom bossbar
     * internal placeholder.
     *
     * @return the requested pattern
     */
    public Pattern getBossbarPattern() {
        return lib.getBossbarPattern();
    }

    /**
     * Creates a new {@link Pattern} instance using the defined blank-space
     * internal placeholder.
     *
     * @return the requested pattern
     */
    public Pattern getBlankPattern() {
        return lib.getBlankPattern();
    }

    /**
     * The {@link Plugin} instance of your project.
     *
     * @throws NullPointerException if the plugin is null
     * @return plugin's instance
     */
    @NotNull
    public Plugin getPlugin() throws NullPointerException {
        return lib.getPlugin();
    }

    /**
     * Replace the {@link BeansLib#langPrefixKey} with the {@link BeansLib#langPrefix}.
     *
     * @param string an input string
     * @param remove if the prefix will be removed
     *
     * @return the formatted string
     */
    public String replacePrefixKey(String string, boolean remove) {
        return lib.replacePrefixKey(string, remove);
    }

    /**
     * Creates a new string array from an input string using the {@link BeansLib#getLineSeparator}
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
        return lib.splitLine(s, limit);
    }

    /**
     * Creates a new string array from an input string using the {@link BeansLib#getLineSeparator}
     * as a split for the array.
     *
     * @param s an input string
     * @return the requested array
     */
    public String[] splitLine(String s) {
        return lib.splitLine(s);
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
        return lib.parseChars(string);
    }

    public String formatPlaceholders(@Nullable Player parser, String string) {
        return lib.formatPlaceholders(parser, string);
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
        return lib.colorize(target, parser, string);
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
        return lib.colorize(player, string);
    }

    /**
     * Formats an input string parsing first {@link PlaceholderAPI} placeholders,
     * replaced chars and then applying the respective colors.
     *
     * @param string the input message
     * @return the formatted message
     */
    public String colorize(String string) {
        return lib.colorize(string);
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
        return lib.createCenteredChatMessage(target, parser, string);
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
        return lib.createCenteredChatMessage(player, string);
    }

    /**
     * Formats a string to a centered string, that has a perfect amount of spaces
     * before the actual string to be display in the chat as a centered message.
     *
     * @param string the input message
     * @return the centered chat message.
     */
    public String createCenteredChatMessage(String string) {
        return lib.createCenteredChatMessage(string);
    }

    /**
     * Parses the players keys defined in your {@link BeansLib#getKeyManager} object
     * to its respective player variables.
     *
     * @param parser a player
     * @param string an input string
     * @param c if keys are case-sensitive
     *
     * @return the requested string
     */
    public String parsePlayerKeys(Player parser, String string, boolean c) {
        return lib.parsePlayerKeys(parser, string, c);
    }

    /**
     * Parses the players keys defined in your {@link BeansLib#getKeyManager} object
     * to its respective player variables. Keys are case-insensitive.
     *
     * @param parser a player
     * @param string an input string
     *
     * @return the requested string
     */
    public String parsePlayerKeys(Player parser, String string) {
        return lib.parsePlayerKeys(parser, string);
    }

    /**
     * Sends information to a player using the {@link MessageSender} object.
     *
     * @param player a valid player
     * @param lines the information to send
     */
    public void playerLog(@NotNull Player player, String... lines) {
        lib.playerLog(player, lines);
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console,
     * avoiding the plugin prefix.
     *
     * @param lines the information to send
     */
    public void rawLog(String... lines) {
        lib.rawLog(lines);
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
    public void doLog(@Nullable CommandSender sender, String... lines) {
        lib.doLog(sender, lines);
    }

    /**
     * Sends requested information to a {@link CommandSender} using the plugin's
     * logger and its prefix.
     *
     * @param lines the information to send
     * @throws NullPointerException if the plugin is null
     */
    public void doLog(String... lines) {
        lib.doLog(lines);
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
    public void mixLog(CommandSender sender, String... lines) {
        lib.mixLog(sender, lines);
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
    public void mixLog(String... lines) {
        lib.mixLog(lines);
    }

    /**
     * Returns the static instance of the lib that is loaded by a plugin using this lib.
     * If there is no loaded instance, will return the {@link #NO_PLUGIN_INSTANCE}.
     *
     * <p> To avoid the no-plugin result, a lib instance should be initialized in the
     * main class of the project, on {@link JavaPlugin#onLoad()} or {@link JavaPlugin#onEnable()}.
     *
     * <pre> {@code
     * "Initialization example of the lib in the main plugin class"
     * @Override
     * public void onEnable() {
     *     BeansLib lib = new BeansLib(this);
     *     // or this
     *     BeansLib lib = new BeansLib();
     * }} </pre>
     *
     * Methods that depends on the plugin's instance, such as {@link BeansLib#doLog(String...)},
     * will throw an {@link NullPointerException} if the {@link #NO_PLUGIN_INSTANCE} is
     * returned or if the instance has no plugin implementation.
     *
     * @return loaded instance
     */
    @NotNull
    public BeansLib getLoaded() {
        return lib == null ? NO_PLUGIN_INSTANCE : lib;
    }
}
