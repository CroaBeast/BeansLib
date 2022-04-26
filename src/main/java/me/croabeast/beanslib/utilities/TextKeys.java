package me.croabeast.beanslib.utilities;

import me.croabeast.beanslib.terminals.Bossbar;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public abstract class TextKeys {

    /**
     * A prefix used in the main pattern to identify the event.
     * This can't be overridden.
     */
    protected final String JSON_PREFIX = "(hover|run|suggest|url)=\\[(.+?)]";

    /**
     * The main pattern to identify the JSON message.
     * This can't be overridden.
     */
    protected final Pattern JSON_PATTERN = Pattern.compile("(?i)<"
            + JSON_PREFIX + "(\\|" + JSON_PREFIX + ")?>(.+?)</text>");

    /**
     * Check if a logger line can be colored or not.
     */
    protected final boolean COLOR_SUPPORT =
            majorVersion() >= 12 && !serverFork().split(" ")[0].matches("(?i)Spigot");

    /**
     * Get the spigot-format server version and fork.
     * @return server version and fork
     */
    private static String serverFormat() {
        return Bukkit.getBukkitVersion().split("-")[0];
    }

    /**
     * Gets the server's fork. Example: Spigot, Paper, Purpur.
     * @return server's fork
     */
    public static String serverFork() {
        return Bukkit.getVersion().split("-")[1] + " " + serverFormat();
    }

    /**
     * Gets the major version of the server.
     * <p> Example: if version is <strong>1.16.5</strong>, will return <strong>16</strong>
     * @return server's major version
     */
    public static int majorVersion() {
        return Integer.parseInt(serverFormat().split("\\.")[1]);
    }

    /**
     * The key that need to be replaced by the main plugin prefix: {@link #getLangPrefix()}.
     * It's recommended to use a string from a .yml of your plugin.
     * <p>Example: <strong>JavaPlugin.getConfig().getString("the key path")</strong>
     * @return the prefix key
     */
    @NotNull
    public abstract String getLangPrefixKey();

    /**
     * The prefix of the plugin that will replace the prefix key: {@link #getLangPrefixKey()}.
     * It's recommended to use a string from a .yml of your plugin.
     * <p>Example: <strong>JavaPlugin.getConfig().getString("main prefix path")</strong>
     * @return plugin prefix
     */
    @NotNull
    public abstract String getLangPrefix();

    /**
     * If you want to remove spaces at the start of a message.
     * It's recommended to use a boolean from a .yml of your plugin.
     * <p>Example: <strong>JavaPlugin.getConfig().getBoolean("path here")</strong>
     * @return if hard spacing is enabled
     */
    public abstract boolean isHardSpacing();

    /**
     * If you want to remove the message-type prefix. Ex: [title], [json]
     * It's recommended to use a boolean from a .yml of your plugin.
     * <p>Example: <strong>JavaPlugin.getConfig().getBoolean("path here")</strong>
     * @return if strip prefix is enabled
     */
    public abstract boolean isStripPrefix();

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
     * <p> Example: <strong>Pattern.quote("my own string")</strong>
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
    public String playerWorldKey() {
        return "{world}";
    }

    /**
     * The character regex pattern that replace the specified char.
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
     * <p> The json key to search if you want to json a title message.
     * @return json key
     */
    @NotNull
    public String getJsonKey() {
        return "json";
    }

    /**
     * <strong>[MESSAGE TYPE KEY]</strong>
     * <p> The action bar to search if you want to json a action-bar message.
     * @return action bar key
     */
    @NotNull
    public String getActionBarKey() {
        return "action-bar";
    }

    /**
     * <strong>[MESSAGE TYPE KEY]</strong>
     * <p> The bossbar key to search if you want to send a bossbar message.
     * <p> This is mainly a regex string to not have conflicts with {@link Bossbar#PATTERN}
     * @return bossbar key
     */
    @NotNull
    public String getBossbarKey() {
        return "^bossbar";
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
    public Pattern getTextPattern() {
        String first = Pattern.quote(startDelimiter()), second = Pattern.quote(endDelimiter());
        return Pattern.compile("(" + first + "(.[^" + first + "][^" + second + "]+)" + second + ")(.+)");
    }
}
