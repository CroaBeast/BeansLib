package me.croabeast.beanslib;

import me.croabeast.beanslib.object.display.Bossbar;
import me.croabeast.beanslib.object.display.Displayer;
import me.croabeast.beanslib.object.key.PlayerKeys;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * The variables for the {@link BeansLib} class.
 * The name before BeansVariables was TextKeys.
 *
 * @author CroaBeast
 * @since 1.0
 */
public class BeansVariables {

    /**
     * A default {@link BeansVariables} instance for static methods.
     */
    public static final BeansVariables DEFAULTS = new BeansVariables();

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
     * Message type prefixes below. All those keys are case-insensitive.
     * Chat messages doesn't need a key.
     */

    /**
     * <strong>[MESSAGE TYPE PREFIX]</strong>
     * <p> The title key to search if you want to send a title message. If the
     * regex doesn't have a group to catch the duration in seconds, it will
     * use the default one in {@link #defaultTitleTicks defaultTitleTicks}.
     *
     * @param isRegex is the key is in regex format
     * @return the title prefix key
     */
    @NotNull
    public String titleRegex(boolean isRegex) {
        final String prefix = "title(:\\d+)?";

        if (isRegex)
            return "(?i)^" + Pattern.quote("[") +
                    prefix + Pattern.quote("]");

        return "[" + prefix.split("[(]")[0] + "]";
    }

    /**
     * <strong>[MESSAGE TYPE PREFIX]</strong>
     * <p> The title key to search if you want to send a title message. If the
     * regex doesn't have a group to catch the duration in seconds, it will
     * use the default one in {@link #defaultTitleTicks}.
     *
     * @return the title prefix key
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
     * <strong>[MESSAGE TYPE PREFIX]</strong>
     * <p> The json key to search if you want to send a json message.
     *
     * @param isRegex is the key is in regex format
     * @return the json prefix key
     */
    @NotNull
    public String jsonRegex(boolean isRegex) {
        final String prefix = "json";

        if (isRegex)
            return "(?i)^" + Pattern.quote("[") +
                    prefix + Pattern.quote("]");

        return "[" + prefix + "]";
    }

    /**
     * <strong>[MESSAGE TYPE PREFIX]</strong>
     * <p> The json key to search if you want to send a json message.
     *
     * @return json key
     */
    @NotNull
    public String jsonRegex() {
        return jsonRegex(false);
    }

    /**
     * <strong>[MESSAGE TYPE PREFIX]</strong>
     * <p> The action bar to search if you want to send a action-bar message.
     *
     * @param isRegex is the key is in regex format
     * @return the action bar prefix key
     */
    @NotNull
    public String actionBarRegex(boolean isRegex) {
        final String prefix = "action-bar";

        if (isRegex)
            return "(?i)^" + Pattern.quote("[") +
                    prefix + Pattern.quote("]");

        return "[" + prefix + "]";
    }

    /**
     * <strong>[MESSAGE TYPE PREFIX]</strong>
     * <p> The action bar to search if you want to send a action-bar message.
     *
     * @return the action bar prefix key
     */
    @NotNull
    public String actionBarRegex() {
        return actionBarRegex(false);
    }

    /**
     * <strong>[MESSAGE TYPE PREFIX]</strong>
     * <p> The bossbar key to search if you want to send a bossbar message.
     * <p> This is mainly a regex string to not have conflicts with {@link Bossbar#PATTERN}
     *
     * @param isRegex is the key is in regex format
     * @return the bossbar prefix key
     */
    @NotNull
    public String bossbarRegex(boolean isRegex) {
        final String prefix = "bossbar(.+)?";

        if (isRegex)
            return "(?i)^" + Pattern.quote("[") +
                    prefix + Pattern.quote("]");

        return "[" + prefix.split("[(]")[0] + "]";
    }

    /**
     * <strong>[MESSAGE TYPE PREFIX]</strong>
     * <p> The bossbar key to search if you want to send a bossbar message.
     * <p> This is mainly a regex string to not have conflicts with {@link Bossbar#PATTERN}
     *
     * @return the bossbar prefix key
     */
    @NotNull
    public String bossbarRegex() {
        return bossbarRegex(false);
    }

    /**
     * <strong>[MESSAGE TYPE PREFIX]</strong>
     * <p> The bossbar key to search if you want to send a webhook to Discord.
     *
     * @param isRegex is the key is in regex format
     * @return the webhook prefix key
     */
    public String webhookRegex(boolean isRegex) {
        final String prefix = "webhook(:.+)?";

        if (isRegex)
            return "(?i)^" + Pattern.quote("[") +
                    prefix + Pattern.quote("]");

        return "[" + prefix.split("[(]")[0] + "]";
    }

    /**
     * <strong>[MESSAGE TYPE PREFIX]</strong>
     * <p> The bossbar key to search if you want to send a webhook to Discord.
     *
     * @return the webhook prefix key
     */
    public String webhookRegex() {
        return webhookRegex(false);
    }

    /**
     * The main pattern to check the message type in the input line.
     * You can also override this pattern to use your own one.
     * <p> <i>WARNING:</i> If you want to override this, you should
     * have exactly 3 groups to catch.
     * <ul>
     *     <li> The prefix with the delimiters.
     *     <li> The prefix without the delimiters.
     *     <li> The message without the prefix.
     * </ul>
     * Examples that works in {@link Displayer}:
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
