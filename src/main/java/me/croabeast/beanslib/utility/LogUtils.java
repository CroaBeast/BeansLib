package me.croabeast.beanslib.utility;

import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.misc.BeansLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

/**
 * The class that handles logger utilities.

 * @deprecated Use the {@link BeansLogger} object instead
 * or {@link BeansLib} inherited methods.
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
        BeansLib.getLoadedInstance().playerLog(player, lines);
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console.
     *
     * @param lines the information to send
     */
    public static void rawLog(String... lines) {
        BeansLib.getLoadedInstance().rawLog(lines);
    }

    /**
     * Sends requested information to the console.
     *
     * @param sender a valid sender, can be the console or a player
     * @param lines the information to send
     */
    public static void doLog(CommandSender sender, String... lines) {
        BeansLib.getLoadedInstance().doLog(sender, lines);
    }

    /**
     * Sends requested information to the console.
     *
     * @param lines the information to send
     */
    public static void doLog(String... lines) {
        BeansLib.getLoadedInstance().doLog(lines);
    }
}
