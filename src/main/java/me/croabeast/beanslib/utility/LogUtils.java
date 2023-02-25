package me.croabeast.beanslib.utility;

import me.croabeast.beanslib.object.misc.BeansLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

/**
 * The class that handles logger utilities.

 * @deprecated Use the {@link BeansLogger} object instead.
 *
 * @author CroaBeast
 * @since 1.0
 */
@ApiStatus.ScheduledForRemoval(inVersion = "1.5")
@Deprecated
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
     * @param lines the information to send
     */
    public static void doLog(String... lines) {
        BeansLogger.DEFAULT_LOGGER.doLog(null, lines);
    }
}
