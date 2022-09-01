package me.croabeast.beanslib.utility;

import me.croabeast.beanslib.utility.key.LibKeys;
import me.croabeast.beanslib.utility.key.TextKeys;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static me.croabeast.beanslib.utility.TextUtils.centerMessage;
import static me.croabeast.iridiumapi.IridiumAPI.process;
import static me.croabeast.iridiumapi.IridiumAPI.stripAll;

/**
 * The class that handles logger utilities.
 */
public final class LogUtils {

    /**
     * Initializing this class is prohibited.
     */
    private LogUtils() {}

    private static String colorLogger(String string, boolean fixColor) {
        if (StringUtils.isBlank(string)) return string;

        boolean doColor = LibKeys.majorVersion() >= 12 && SystemUtils.IS_OS_WINDOWS &&
                !LibKeys.serverFork().split(" ")[0].matches("(?i)Bukkit|Spigot");

        string = TextUtils.stripJson(string);
        return doColor ? process(string, !fixColor) : stripAll(string);
    }

    private static String[] toLogLines(TextKeys keys, Player player, boolean isLog, String... lines) {
        if (keys == null) keys = LibKeys.DEFAULTS;

        List<String> list = new ArrayList<>();

        String value = isLog ? "" : keys.langPrefix(),
                sp = keys.lineSeparator();

        for (String line : lines) {
            if (line == null) continue;
            line = line.replace(keys.langPrefixKey(), value);

            line = centerMessage(keys, null, player, line);
            list.add(line.replace(sp, "&f" + sp));
        }

        return list.toArray(new String[0]);
    }

    private static String[] toLogLines(TextKeys keys, String... lines) {
        return toLogLines(keys, null, true, lines);
    }

    /**
     * Sends requested information for a {@link Player}.
     *
     * @param keys the {@link TextKeys} instance
     * @param player a valid online player
     * @param lines the information to send
     */
    public static void playerLog(TextKeys keys, Player player, String... lines) {
        for (String s : toLogLines(keys, player, false, lines))
            player.sendMessage(process(s));
    }

    /**
     * Sends requested information for a {@link Player}.
     *
     * @param player a valid online player
     * @param lines the information to send
     */
    public static void playerLog(Player player, String... lines) {
        playerLog(null, player, lines);
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console.
     *
     * @param keys the {@link TextKeys} instance
     * @param lines the information to send
     */
    public static void rawLog(TextKeys keys, String... lines) {
        boolean fix = keys != null && keys.fixColorLogger();
        for (String s : toLogLines(keys, lines))
            Bukkit.getLogger().info(colorLogger(s, fix));
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console.
     *
     * @param lines the information to send
     */
    public static void rawLog(String... lines) {
        rawLog(null, lines);
    }

    /**
     * Sends requested information to a {@link CommandSender}.
     *
     * @param keys the {@link TextKeys} instance
     * @param plugin the plugin's instance
     * @param sender a valid sender, can be the console or a player
     * @param lines the information to send
     */
    public static void doLog(TextKeys keys, @NotNull JavaPlugin plugin, CommandSender sender, String... lines) {
        if (sender instanceof Player)
            playerLog(keys, (Player) sender, lines);

        JavaPlugin p = keys != null ? keys.getPlugin() : plugin;
        boolean fix = keys != null && keys.fixColorLogger();

        for (String s : toLogLines(keys, lines))
            p.getLogger().info(colorLogger(s, fix));
    }

    /**
     * Sends requested information to the console.
     *
     * @param plugin the plugin's instance
     * @param lines the information to send
     */
    public static void doLog(@NotNull JavaPlugin plugin, String... lines) {
        doLog(null, plugin, null, lines);
    }

}
