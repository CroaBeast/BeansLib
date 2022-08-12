package me.croabeast.beanslib.utility;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The exceptions checker class.
 */
public final class Exceptions {

    private Exceptions() {}

    /**
     * Checks if a plugin is enabled.
     * Sure, you can just call the Bukkit method, but I like shorter methods.
     * @param name the plugin's name
     * @return if the plugin is enabled
     */
    public static boolean isPluginEnabled(String name) {
        return Bukkit.getPluginManager().isPluginEnabled(name);
    }

    /**
     * Checks if the player is null.
     * @param player a player
     * @return the player
     * @throws NullPointerException if player is null
     */
    @NotNull
    public static Player checkPlayer(Player player) {
        if (player != null) return player;
        throw new NullPointerException("Player can not be null");
    }
}
