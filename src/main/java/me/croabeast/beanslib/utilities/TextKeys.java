package me.croabeast.beanslib.utilities;

import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.objects.Bossbar;
import org.apache.commons.lang.SystemUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * The keys for the {@link BeansLib} class.
 *
 * @author CroaBeast
 * @since 1.0
 */
public abstract class TextKeys {

    /**
     * A prefix used in the main pattern to identify the event.
     * This can't be overridden.
     */
    protected static final String JSON_PREFIX = "(.[^|]+):\"(.[^|]+)\"";

    /**
     * The main pattern to identify the JSON message.
     * This can't be overridden.
     */
    public static final Pattern JSON_PATTERN =
            Pattern.compile("(?i)<(" + JSON_PREFIX + "([|]" + JSON_PREFIX + ")?)>(.+)</text>");

    /**
     * Gets the server's version. Example: 1.8.8, 1.16.5
     * @return server's version
     */
    private static String serverVersion() {
        return Bukkit.getBukkitVersion().split("-")[0];
    }

    /**
     * Get the spigot-format server version and fork.
     * @return server version and fork
     */
    public static String serverFork() {
        return Bukkit.getVersion().split("-")[1] + " " + serverVersion();
    }

    /**
     * Gets the major version of the server.
     * <p> Example: if version is <strong>1.16.5</strong>, will return <strong>16</strong>
     * @return server's major version
     */
    public static int majorVersion() {
        return Integer.parseInt(serverVersion().split("\\.")[1]);
    }

    /**
     * Checks if the server is in a Windows' environment.
     * @return if the server is in a Windows system
     */
    public static boolean isWindows() {
        return SystemUtils.OS_NAME.matches("(?i)Windows");
    }

    /**
     * The key that need to be replaced by the main plugin prefix: {@link #langPrefix()}.
     * It's recommended to use a string from a .yml of your plugin.
     * <pre>Example: {@code JavaPlugin.getConfig().getString("path here")}</pre>
     * @return the prefix key
     */
    @NotNull
    public abstract String langPrefixKey();

    /**
     * The prefix of the plugin that will replace the prefix key: {@link #langPrefixKey()}.
     * It's recommended to use a string from a .yml of your plugin.
     * <pre>Example: {@code JavaPlugin.getConfig().getString("path here")}</pre>
     * @return plugin prefix
     */
    @NotNull
    public abstract String langPrefix();

    /**
     * It will fix an RGB issue is some servers that RGB not working correctly.
     * It's recommended to use a boolean from a .yml of your plugin.
     * <pre>Example:{@code JavaPlugin.getConfig().getBoolean("path here")}</pre>
     * @return if this fix is enabled
     */
    public abstract boolean fixColorLogger();

    /**
     * If you want to remove spaces at the start of a message.
     * It's recommended to use a boolean from a .yml of your plugin.
     * <pre>Example:{@code JavaPlugin.getConfig().getBoolean("path here")}</pre>
     * @return if hard spacing is enabled
     */
    public abstract boolean isHardSpacing();

    /**
     * If you want to remove the message-type prefix. Ex: [title], [json]
     * It's recommended to use a boolean from a .yml of your plugin.
     * <pre>Example:{@code JavaPlugin.getConfig().getBoolean("path here")}</pre>
     * @return if strip prefix is enabled
     */
    public abstract boolean isStripPrefix();

    /**
     * The size of the chat box of the player's client for centered chat messages.
     * <p> This can be overridden, if you want to set a custom value.
     * @return chat box's size
     */
    public int chatBoxSize() {
        return 154;
    }

    /**
     * The center prefix to define a center chat message.
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
     * your own one using this: {@link Pattern#quote(String)}
     * <pre>Example:{@code Pattern.quote("my own string")}</pre>
     * @return line splitter
     */
    @NotNull
    public String lineSeparator() {
        return Pattern.quote("<n>");
    }

    /**
     * The player key that will be replaced for the player's name.
     * @return player key
     */
    @NotNull
    public String playerKey() {
        return "{player}";
    }

    /**
     * The world key that will be replaced for the player's world.
     * @return player world key
     */
    @NotNull
    public String worldKey() {
        return "{world}";
    }

    /**
     * The character regex pattern that replace the specified char.
     * This regex should have only 1 group to catch.
      * @return char pattern
     */
    @NotNull
    public String charPattern() {
        return "<U:([a-fA-F0-9]{4})>";
    }

    /*
     * Message type keys below. All those keys are case-insensitive.
     * Chat messages doesn't need a key.
     */

    /**
     * The start delimiter of every message-type keys.
     * No need to quote this string if you want to override it.
     * <p> This will apply to all message-type keys.
     * @return start delimiter
     */
    @NotNull
    public String startDelimiter() {
        return "[";
    }

    /**
     * The end delimiter of every message-type keys.
     * No need to quote this string if you want to override it.
     * <p> This will apply to all message-type keys.
     * @return end delimiter
     */
    @NotNull
    public String endDelimiter() {
        return "]";
    }

    /**
     * <strong>[MESSAGE TYPE KEY]</strong>
     * <p> The title key to search if you want to send a title message, this is mainly a regex string.
     * <p> If the string doesn't have a group to catch the duration
     * in seconds, it will use the default one in {@link #defaultTitleTicks defaultTitleTicks}.
     * To be exact, {@link #defaultTitleTicks defaultTitleTicks}<strong>()[1]</strong>.
     * @return the title regex key
     */
    @NotNull
    public String titleKey() {
        return "title(:\\d+)?";
    }

    /**
     * In, stay and out ticks for a title message.
     * @return title ticks
     */
    public int @NotNull [] defaultTitleTicks() {
        return new int[] {10, 50, 10};
    }

    /**
     * <strong>[MESSAGE TYPE KEY]</strong>
     * <p> The json key to search if you want to send a title message.
     * @return json key
     */
    @NotNull
    public String jsonKey() {
        return "json";
    }

    /**
     * <strong>[MESSAGE TYPE KEY]</strong>
     * <p> The action bar to search if you want to send a action-bar message.
     * @return action bar key
     */
    @NotNull
    public String actionBarKey() {
        return "action-bar";
    }

    /**
     * <strong>[MESSAGE TYPE KEY]</strong>
     * <p> The bossbar key to search if you want to send a bossbar message.
     * <p> This is mainly a regex string to not have conflicts with {@link Bossbar#PATTERN}
     * @return bossbar key
     */
    @NotNull
    public String bossbarKey() {
        return "bossbar(.+)?";
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
     * <p> See the default pattern to understand how the groups are
     * caught if you want to override this method.
     * @return the main pattern
     */
    @NotNull
    public Pattern textPattern() {
        String start = Pattern.quote(startDelimiter()), end = Pattern.quote(endDelimiter());
        return Pattern.compile("(?i)(" + start + "(" + titleKey() + "|" +
                jsonKey() + "|" + bossbarKey() + "|" + actionBarKey() + ")" + end + ")(.+)");
    }
}
