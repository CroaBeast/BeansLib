package me.croabeast.beanslib.utility;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * The exceptions checker class.
 */
public final class Exceptions {

    private Exceptions() {}

    /**
     * Checks if a plugin is enabled.
     * Sure, you can just call the Bukkit method, but I like shorter methods.
     *
     * @param name the plugin's name
     * @return if the plugin is enabled
     */
    public static boolean isPluginEnabled(String name) {
        return Bukkit.getPluginManager().isPluginEnabled(name);
    }

    /**
     * Checks if a plugin names' list are enabled.
     *
     * @param isInclusive
     *      <p> if enabled, will return true if all plugins
     *      are enabled, otherwise false;
     *      <p> if false, will return true if at least one
     *      plugin is enabled, otherwise false
     * @param names the plugin names' list
     *
     * @return the respective value
     */
    public static boolean arePluginsEnabled(boolean isInclusive, Collection<? extends String> names) {
        if (names.size() == 0) return false;

        if (names.size() == 1) {
            String s = names.toArray(new String[0])[0];
            return isPluginEnabled(s);
        }

        for (String name : names) {
            boolean isEnabled = isPluginEnabled(name);

            if (!isInclusive) {
                if (isEnabled) return true;
            } else {
                if (!isEnabled) return false;
            }
        }

        return isInclusive;
    }

    /**
     * Checks if a plugin names' array are enabled.
     *
     * @param isInclusive
     *      <p> if enabled, will return true if all plugins
     *      are enabled, otherwise false;
     *      <p> if false, will return true if at least one
     *      plugin is enabled, otherwise false
     * @param names the plugin names' array
     *
     * @return the respective value
     */
    public static boolean arePluginsEnabled(boolean isInclusive, String... names) {
        return arePluginsEnabled(isInclusive, Lists.newArrayList(names));
    }

    /**
     * Checks if the player is null.
     *
     * @param player a player
     *
     * @return the player
     * @throws NullPointerException if player is null
     */
    @NotNull
    public static Player checkPlayer(Player player) {
        return Objects.requireNonNull(player, "Player can not be null");
    }
}
