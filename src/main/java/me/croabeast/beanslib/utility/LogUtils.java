package me.croabeast.beanslib.utility;

import me.croabeast.beanslib.object.misc.BeansLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * The class that handles logger utilities.
 *
 * @deprecated Use the {@link BeansLogger} object instead.
 */
@Deprecated @ApiStatus.ScheduledForRemoval(inVersion = "1.5")
public final class LogUtils {

    /**
     * Initializing this class is blocked.
     */
    private LogUtils() {}

    /**
     * Sends requested information for a {@link Player}.
     *
     * @param player a valid online player
     * @param lines the information to send
     */
    public static void playerLog(Player player, String... lines) {
        BeansLogger.DEFAULT_LOGGER.playerLog(player, lines);
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console.
     *
     * @param lines the information to send
     */
    public static void rawLog(String... lines) {
        BeansLogger.DEFAULT_LOGGER.rawLog(lines);
    }

    /**
     * Sends requested information to the console.
     *
     * @param plugin the plugin's instance
     * @param lines the information to send
     */
    public static void doLog(@NotNull JavaPlugin plugin, String... lines) {
        new BeansLogger(plugin).doLog(null, lines);
    }
}
