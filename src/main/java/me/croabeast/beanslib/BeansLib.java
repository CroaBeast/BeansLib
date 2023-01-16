package me.croabeast.beanslib;

import me.croabeast.beanslib.utility.*;
import me.croabeast.beanslib.object.display.Displayer;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * The main class of the Lib.
 * It has a lot of useful text-related methods.
 *
 * @author CroaBeast
 * @since 1.0
 */
public abstract class BeansLib extends BeansMethods {

    /**
     * The {@link JavaPlugin} instance of your project.
     *
     * @return plugin instance
     */
    @NotNull
    public abstract JavaPlugin getPlugin();

    /**
     * The prefix of the plugin that will replace the prefix key: {@link #langPrefixKey()}.
     * It's recommended to use a string from a .yml of your plugin.
     * <p> Example:
     * <pre> {@code return JavaPlugin.getConfig().getString("path here")}</pre>
     *
     * @return plugin prefix
     */
    @NotNull
    public abstract String langPrefix();

    /**
     * The {@link ConfigurationSection} object to get all the available webhooks.
     *
     * @return the requested section
     */
    public abstract ConfigurationSection getWebhookSection();

    /**
     * Sends requested information for a {@link Player}.
     *
     * @param player a valid online player
     * @param lines the information to send
     */
    public void playerLog(@NotNull Player player, String... lines) {
        LogUtils.playerLog(this, player, lines);
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console.
     *
     * @param lines the information to send
     */
    public void rawLog(String... lines) {
        LogUtils.rawLog(this, lines);
    }

    /**
     * Sends requested information to a {@link CommandSender}.
     *
     * @param sender a valid sender, can be the console or a player
     * @param lines the information to send
     */
    public void doLog(@Nullable CommandSender sender, String... lines) {
        LogUtils.doLog(this, getPlugin(), sender, lines);
    }

    /**
     * Sends requested information to the console.
     *
     * @param lines the information to send
     */
    public void doLog(String... lines) {
        doLog(null, lines);
    }

    /**
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String, String[], String[])}
     *
     * @param sender a sender to format and send the message
     * @param list the message list
     * @param keys a keys array
     * @param values a values array
     *
     * @deprecated See {@link Displayer} and its constructor.
     */
    @Deprecated
    public void sendMessageList(CommandSender sender, List<String> list, String[] keys, String[] values) {
        new Displayer(this, sender, null, list).
                setKeys(keys).setValues(values).
                setCaseSensitive(false).display();
    }

    /**
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String, String[], String[])}
     *
     * @param sender a sender to format and send the message
     * @param section the config file or section
     * @param path the path of the string or string list
     * @param keys a keys array
     * @param values a values array
     *
     * @deprecated See {@link Displayer} and its constructor.
     */
    @Deprecated
    public void sendMessageList(CommandSender sender, ConfigurationSection section, String path, String[] keys, String[] values) {
        sendMessageList(sender, TextUtils.toList(section, path), keys, values);
    }

    /**
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String, String[], String[])}
     *
     * @param sender a sender to format and send the message
     * @param list the message list
     *
     * @deprecated See {@link Displayer} and its constructor.
     */
    @Deprecated
    public void sendMessageList(CommandSender sender, List<String> list) {
        sendMessageList(sender, list, null, null);
    }

    /**
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String, String[], String[])}
     *
     * @param sender a sender to format and send the message
     * @param section the config file or section
     * @param path the path of the string or string list
     *
     * @deprecated See {@link Displayer} and its constructor.
     */
    @Deprecated
    public void sendMessageList(CommandSender sender, ConfigurationSection section, String path) {
        sendMessageList(sender, TextUtils.toList(section, path));
    }
}
