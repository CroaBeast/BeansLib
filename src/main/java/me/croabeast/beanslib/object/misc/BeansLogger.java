package me.croabeast.beanslib.object.misc;

import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.iridiumapi.IridiumAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manages logger manages to player and/or console.
 * Have support for color codes.
 *
 * @author CroaBeast
 * @since 1.4
 */
public class BeansLogger {

    /**
     * A static instance of a logger without any lib or plugin implementation.
     *
     * <p> The methods {@link BeansLogger#doLog(String...)} and
     * {@link BeansLogger#doLog(CommandSender, String...)} does not work
     * with this logger.
     */
    public static final BeansLogger DEFAULT_LOGGER = new BeansLogger();

    private final BeansLib lib;

    /**
     * Creates a new logger using a {@link BeansLib} instance.
     *
     * @param lib {@link BeansLib} instance
     */
    public BeansLogger(@NotNull BeansLib lib) {
        this.lib = lib;
    }

    /**
     * Creates a new logger using a {@link Plugin} instance.
     *
     * @param plugin a {@link Plugin} instance
     */
    public BeansLogger(Plugin plugin) {
        this(new BeansLib(plugin));
    }

    /**
     * Creates a new logger without any plugin implementation. Useful to log
     * information without the plugin's prefix.
     *
     * <p> The methods {@link BeansLogger#doLog(CommandSender, String...)} and
     * {@link BeansLogger#doLog(String...)} does not work with this logger.
     */
    public BeansLogger() {
        this((Plugin) null);
    }

    private String colorLogger(String string) {
        if (StringUtils.isBlank(string)) return string;
        string = TextUtils.stripJson(string);

        return lib.isColoredConsole() ?
                IridiumAPI.process(string) :
                IridiumAPI.stripAll(string);
    }

    private String[] toLogLines(Player player, boolean isLog, String... lines) {
        List<String> list = new ArrayList<>();

        String value = isLog ? "" : lib.getLangPrefix(),
                sp = lib.getLineSeparator();

        for (String line : lines) {
            if (line == null) continue;
            line = line.replace(lib.getLangPrefixKey(), value);

            line = lib.centerMessage(player, player, line);
            list.add(line.replace(sp, "&f" + sp));
        }

        return list.toArray(new String[0]);
    }

    private String[] toLogLines(String... lines) {
        return toLogLines(null, true, lines);
    }

    /**
     * Sends requested information for a {@link Player}.
     *
     * @param player a valid online player
     * @param lines the information to send
     */
    public void playerLog(Player player, String... lines) {
        for (String s : toLogLines(player, false, lines))
            player.sendMessage(IridiumAPI.process(s));
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console.
     *
     * @param lines the information to send
     */
    public void rawLog(String... lines) {
        for (String s : toLogLines(lines)) Bukkit.getLogger().info(colorLogger(s));
    }

    /**
     * Sends requested information to a {@link CommandSender}.
     *
     * @param sender a valid sender, can be the console or a player
     * @param lines the information to send
     */
    public void doLog(CommandSender sender, String... lines) {
        if (sender instanceof Player)
            playerLog((Player) sender, lines);

        Plugin plugin = lib.getPlugin();
        if (plugin == null) return;

        for (String s : toLogLines(lines))
            plugin.getLogger().info(colorLogger(s));
    }

    /**
     * Sends requested information to the console.
     *
     * @param lines the information to send
     */
    public void doLog(String... lines) {
        doLog(null, lines);
    }
}
