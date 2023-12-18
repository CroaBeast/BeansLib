package me.croabeast.beanslib.utility;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.var;
import me.croabeast.beanslib.Beans;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

/**
 * A utility class for handling various exceptions and plugin-related checks.
 */
@UtilityClass
public class Exceptions {

    /**
     * Checks if a plugin with the given name is enabled or not.
     *
     * @param name the name of the plugin to check
     * @param checkRunning whether to check if the plugin is running or not
     *
     * @return true if the plugin is not null and is enabled, if
     *         checkRunning is true; false otherwise
     */
    public boolean isPluginEnabled(String name, boolean checkRunning) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        return plugin != null && (!checkRunning || plugin.isEnabled());
    }

    /**
     * Checks if a plugin with the given name is enabled or not, assuming checkRunning is true.
     *
     * @param name the name of the plugin to check
     * @return true if the plugin is not null and is enabled, false otherwise
     */
    public boolean isPluginEnabled(String name) {
        return isPluginEnabled(name, true);
    }

    /**
     * Checks if a collection of plugins are enabled or not, based on a boolean flag.
     *
     * @param isInclusive whether to check if all plugins are enabled (true) or any plugin is enabled (false)
     * @param names the collection of plugin names to check
     *
     * @return true if the collection is not empty and the plugins match the isInclusive flag, false otherwise
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
     * Checks if an array of plugins is enabled or not, based on a boolean flag.
     *
     * @param isInclusive whether to check if all plugins are enabled (true) or any plugin is enabled (false)
     * @param names the array of plugin names to check
     *
     * @return true if the array is not empty and the plugins match the isInclusive flag, false otherwise
     */
    public boolean arePluginsEnabled(boolean isInclusive, String... names) {
        return arePluginsEnabled(isInclusive, ArrayUtils.toList(names));
    }

    /**
     * Checks if a player is not null and returns it, otherwise throws a NullPointerException.
     *
     * @param player the player to check
     *
     * @return the player if it is not null
     * @throws NullPointerException if the player is null
     */
    @NotNull
    public Player checkPlayer(Player player) {
        return Objects.requireNonNull(player, "Player can not be null");
    }

    /**
     * Gets the caller class from the current thread's stack trace at a given index.
     *
     * @param index the index of the stack trace element to get the class from
     *
     * @return the class object of the caller class
     * @throws ClassNotFoundException if the class name is not found
     */
    @NotNull
    public Class<?> getCallerClass(int index) throws ClassNotFoundException {
        return Class.forName(Thread.currentThread().getStackTrace()[index].getClassName());
    }

    /**
     * Checks if a class has access to the plugin, otherwise throws a throwable.
     *
     * @param clazz the class to check
     * @param throwable the throwable to throw if the class does not have access
     *
     * @throws Throwable if the class does not have access to the plugin
     */
    @SneakyThrows
    public void hasPluginAccess(Class<?> clazz, Throwable throwable) {
        Objects.requireNonNull(clazz);
        Plugin plugin = null;

        try {
            plugin = JavaPlugin.getProvidingPlugin(clazz);
        } catch (Exception ignored) {}

        if (Objects.equals(plugin, Beans.getPlugin()))
            return;

        throw throwable;
    }

    /**
     * Checks if a class has access to the plugin, otherwise throws an IllegalAccessException with a given message.
     *
     * @param clazz the class to check
     * @param message the message to use for the exception
     *
     * @throws IllegalAccessException if the class does not have access to the plugin
     */
    public void hasPluginAccess(Class<?> clazz, String message) {
        hasPluginAccess(clazz, new IllegalAccessException(message));
    }

    /**
     * Checks if a class has access to the plugin, otherwise throws an IllegalAccessException with a default message.
     *
     * @param clazz the class to check
     * @throws IllegalAccessException if the class does not have access to the plugin
     */
    public void hasPluginAccess(Class<?> clazz) {
        hasPluginAccess(
                clazz,
                clazz.getSimpleName() +
                        " is only accessible using " +
                        Beans.getPlugin().getName()
        );
    }
}