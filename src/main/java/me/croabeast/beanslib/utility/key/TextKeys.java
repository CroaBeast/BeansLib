package me.croabeast.beanslib.utility.key;

import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.object.Bossbar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * The keys for the {@link BeansLib} class.
 *
 * @author CroaBeast
 * @since 1.0
 */
public class TextKeys {

    /**
     * The {@link JavaPlugin} instance of your project.
     *
     * @return plugin instance
     */
    public JavaPlugin getPlugin() {
        return null;
    }

    /**
     * The prefix of the plugin that will replace the prefix key: {@link #langPrefixKey()}.
     *
     * @return plugin prefix
     */
    @NotNull
    public String langPrefix() {
        return "";
    }

    /**
     * It will fix an RGB issue is some servers that RGB not working correctly.
     *
     * @return if this fix is enabled
     */
    public boolean fixColorLogger() {
        return false;
    }

    /**
     * If you want to remove the message-type prefix. Ex: [title], [json]
     *
     * @return if strip prefix is enabled
     */
    public boolean isStripPrefix() {
        return false;
    }

    /**
     * The {@link ConfigurationSection} object to get all the available webhooks.
     *
     * @return the requested section
     */
    public ConfigurationSection getWebhookSection() {
        return null;
    }

    /**
     * A key that will be replaced by the main plugin prefix: {@link #langPrefix()}.
     *
     * @return the prefix key
     */
    @NotNull
    public String langPrefixKey() {
        return "<P>";
    }

    /**
     * The center prefix to define a center chat message.
     *
     * @return center prefix
     */
    @NotNull
    public String centerPrefix() {
        return "[C]";
    }

    /**
     * The line splitter or separator to split multiple chat lines
     * or split a title message between title and subtitle.
     * <p> If you want to override this string, you need to quote
     * your own one using {@link Pattern#quote(String)}.
     * <p> Example: <pre>{@code Pattern.quote("my own string");}</pre>
     *
     * @return line splitter
     */
    @NotNull
    public String lineSeparator() {
        return Pattern.quote("<n>");
    }

    /**
     * The {@link PlayerKeys} instance to parse the respective player
     * values in strings.
     *
     * @return the {@link PlayerKeys} instance
     */
    @NotNull
    public PlayerKeys playerKeys() {
        return new PlayerKeys();
    }

    /**
     * The character regex pattern that replace the specified char.
     * This regex should have only 1 group to catch.
     *
      * @return char pattern
     */
    @NotNull
    public String charRegex() {
        return "<U:([a-fA-F0-9]{4})>";
    }

    /*
     * Message type regex keys below. All those keys are case-insensitive.
     * Chat messages doesn't need a key.
     */

    /**
     * <strong>[MESSAGE TYPE REGEX KEY]</strong>
     * <p> The title key to search if you want to send a title message. If the
     * regex doesn't have a group to catch the duration in seconds, it will
     * use the default one in {@link #defaultTitleTicks defaultTitleTicks}.
     *
     * @return the title regex key
     */
    @NotNull
    public String titleRegex(boolean startsIgnoreCase) {
        String temp = Pattern.quote("[") + "title(:\\d+)?" + Pattern.quote("]");
        return (startsIgnoreCase ? "(?i)^" : "") + temp;
    }

    /**
     * <strong>[MESSAGE TYPE REGEX KEY]</strong>
     * <p> The title key to search if you want to send a title message. If the
     * regex doesn't have a group to catch the duration in seconds, it will
     * use the default one in {@link #defaultTitleTicks defaultTitleTicks}.
     *
     * @return the title regex key
     */
    @NotNull
    public String titleRegex() {
        return titleRegex(false);
    }

    /**
     * In, stay and out ticks for a title message.
     * <pre> {@code
     * int[] ticks = defaultTitleTicks();
     * // Respective values
     * int fadeIn = ticks[0];
     * int stay = ticks[1];
     * int fadeout = ticks[2];
     * }</pre>
     *
     * @return title ticks
     */
    public int @NotNull [] defaultTitleTicks() {
        return new int[] {10, 50, 10};
    }

    /**
     * <strong>[MESSAGE TYPE REGEX KEY]</strong>
     * <p> The json key to search if you want to send a json message.
     *
     * @return json key
     */
    @NotNull
    public String jsonRegex(boolean startsIgnoreCase) {
        String temp = Pattern.quote("[") + "json" + Pattern.quote("]");
        return (startsIgnoreCase ? "(?i)^" : "") + temp;
    }

    /**
     * <strong>[MESSAGE TYPE REGEX KEY]</strong>
     * <p> The json key to search if you want to send a json message.
     *
     * @return json key
     */
    @NotNull
    public String jsonRegex() {
        return jsonRegex(false);
    }

    /**
     * <strong>[MESSAGE TYPE REGEX KEY]</strong>
     * <p> The action bar to search if you want to send a action-bar message.
     *
     * @return action bar key
     */
    @NotNull
    public String actionBarRegex(boolean startsIgnoreCase) {
        String temp = Pattern.quote("[") + "action-bar" + Pattern.quote("]");
        return (startsIgnoreCase ? "(?i)^" : "") + temp;
    }

    /**
     * <strong>[MESSAGE TYPE REGEX KEY]</strong>
     * <p> The action bar to search if you want to send a action-bar message.
     *
     * @return action bar key
     */
    @NotNull
    public String actionBarRegex() {
        return actionBarRegex(false);
    }

    /**
     * <strong>[MESSAGE TYPE REGEX KEY]</strong>
     * <p> The bossbar key to search if you want to send a bossbar message.
     * <p> This is mainly a regex string to not have conflicts with {@link Bossbar#PATTERN}
     *
     * @return bossbar key
     */
    @NotNull
    public String bossbarRegex(boolean startsIgnoreCase) {
        String temp = Pattern.quote("[") + "bossbar(.+)?" + Pattern.quote("]");
        return (startsIgnoreCase ? "(?i)^" : "") + temp;
    }

    /**
     * <strong>[MESSAGE TYPE REGEX KEY]</strong>
     * <p> The bossbar key to search if you want to send a bossbar message.
     * <p> This is mainly a regex string to not have conflicts with {@link Bossbar#PATTERN}
     *
     * @return bossbar key
     */
    @NotNull
    public String bossbarRegex() {
        return bossbarRegex(false);
    }

    public String webhookRegex(boolean startsIgnoreCase) {
        String temp = Pattern.quote("[") + "webhook(:.+)?" + Pattern.quote("]");
        return (startsIgnoreCase ? "(?i)^" : "") + temp;
    }

    public String webhookRegex() {
        return webhookRegex(false);
    }

    /**
     * The main pattern to check the message type in the input line.
     * You can also override this pattern to use your own one.
     * <p>WARNING: If you want to override this, you <strong>NEED</strong>
     * to have exactly 3 groups to catch.
     * <blockquote><pre>
     * 1. The prefix with the delimiters.
     * 2. The prefix without the delimiters.
     * 3. The message without the prefix.</pre></blockquote>
     * Examples that works in {@link BeansLib#sendMessage(Player, String)}:
     * <pre> {@code
     * "[action-bar] my action bar message" // Sends an action bar message
     * "[title] a simple title" // Sends a cool title message
     * "[title:10] a 10 sec title" // Sends a title message that last 10 seconds
     * "[json] {\"text\":\"json message\"}" // Sends a vanilla json message
     * "[bossbar] a bossbar message" // Sends a bossbar message
     * "My cool and awesome chat message" // Sends a chat message
     * } </pre>
     *
     * @return the main text pattern
     */
    private String a() { return ""; }
}
