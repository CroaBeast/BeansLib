package me.croabeast.beanslib.utilities;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import static me.croabeast.iridiumapi.IridiumAPI.process;
import static me.croabeast.iridiumapi.IridiumAPI.stripAll;

public class LogUtils {

    private LogUtils() {}

    /**
     * Check if a logger line can be colored or not.
     * @return if this fix is enabled.
     */
    private static boolean loggerColorSupport() {
        String fork = TextKeys.serverFork().split(" ")[0];
        return TextKeys.majorVersion() >= 12 && (fork.matches("(?i)Paper") &&
                TextKeys.isWindows() || !fork.matches("(?i)Spigot|Paper"));
    }

    /**
     * It will color the console message if the server supports it.
     * @param string the input line
     * @param fixColor if it's necessary to fix a visual color bug
     * @return the formatted console message
     */
    public static String colorLogger(@NotNull String string, boolean fixColor) {
        string = TextUtils.stripJson(string);
        return loggerColorSupport() ? process(string, !fixColor) : stripAll(string);
    }

    /**
     * Sends requested information for a {@link Player}.
     * @param player a valid online player
     * @param lines the information to send
     */
    public static void playerLog(@NotNull Player player, String... lines) {
        for (String s : lines) player.sendMessage(process(s));
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console.
     * @param lines the information to send
     */
    public static void rawLog(String... lines) {
        for (String s : lines) Bukkit.getLogger().info(colorLogger(s, false));
    }

    /**
     * Sends requested information to a {@link CommandSender}.
     * @param plugin the plugin's instance
     * @param sender a valid sender, can be the console or a player
     * @param lines the information to send
     */
    public static void doLog(JavaPlugin plugin, CommandSender sender, String... lines) {
        if (sender instanceof Player) playerLog((Player) sender, lines);
        for (String s : lines) plugin.getLogger().info(colorLogger(s, false));
    }

    /**
     * Sends requested information to the console.
     * @param plugin the plugin's instance
     * @param lines the information to send
     */
    public static void doLog(JavaPlugin plugin, String... lines) {
        doLog(plugin, null, lines);
    }

}
