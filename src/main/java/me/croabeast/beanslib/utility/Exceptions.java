package me.croabeast.beanslib.utility;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

/**
 * The exceptions checker class.
 *
 * @author CroaBeast
 * @since 1.2
 */
@UtilityClass
public class Exceptions {

    /**
     * Checks if a plugin is enabled.
     * Sure, you can just call the Bukkit method, but I like shorter methods.
     *
     * @param name the plugin's name
     * @param checkRunning if it needs to check if the plugin's is running
     *
     * @return if the plugin is enabled
     */
    public boolean isPluginEnabled(String name, boolean checkRunning) {
        var plugin = Bukkit.getPluginManager().getPlugin(name);
        return plugin != null && (!checkRunning || plugin.isEnabled());
    }

    /**
     * Checks if a plugin is enabled.
     * Sure, you can just call the Bukkit method, but I like shorter methods.
     *
     * @param name the plugin's name
     * @return if the plugin is enabled
     */
    public boolean isPluginEnabled(String name) {
        return isPluginEnabled(name, true);
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
    public boolean arePluginsEnabled(boolean isInclusive, Collection<String> names) {
        if (names.size() == 0) return false;

        if (names.size() == 1) {
            var s = names.toArray(new String[0])[0];
            return isPluginEnabled(s);
        }

        for (var name : names) {
            var isEnabled = isPluginEnabled(name);

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
    public boolean arePluginsEnabled(boolean isInclusive, String... names) {
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
    public Player checkPlayer(Player player) {
        return Objects.requireNonNull(player, "Player can not be null");
    }
}
