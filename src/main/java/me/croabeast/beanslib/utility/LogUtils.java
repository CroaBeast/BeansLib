package me.croabeast.beanslib.utility;

import me.croabeast.beanslib.BeansMethods;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static me.croabeast.iridiumapi.IridiumAPI.process;
import static me.croabeast.iridiumapi.IridiumAPI.stripAll;

/**
 * The class that handles logger utilities.
 */
public final class LogUtils {

    /**
     * Initializing this class is blocked.
     */
    private LogUtils() {}

    private static String colorLogger(String string) {
        if (StringUtils.isBlank(string)) return string;

        boolean doColor = System.console() != null &&
                System.getenv().get("TERM") != null;

        string = TextUtils.stripJson(string);
        return doColor ? process(string) : stripAll(string);
    }

    private static String[] toLogLines(BeansMethods m, Player player, boolean isLog, String... lines) {
        if (m == null) m = BeansMethods.DEFAULTS;

        List<String> list = new ArrayList<>();

        String value = isLog ? "" : m.langPrefix(),
                sp = m.lineSeparator();

        for (String line : lines) {
            if (line == null) continue;
            line = line.replace(m.langPrefixKey(), value);

            line = m.centerMessage(null, player, line);
            list.add(line.replace(sp, "&f" + sp));
        }

        return list.toArray(new String[0]);
    }

    private static String[] toLogLines(BeansMethods m, String... lines) {
        return toLogLines(m, null, true, lines);
    }

    /**
     * Sends requested information for a {@link Player}.
     *
     * @param m the {@link BeansMethods} instance
     * @param player a valid online player
     * @param lines the information to send
     */
    public static void playerLog(BeansMethods m, Player player, String... lines) {
        for (String s : toLogLines(m, player, false, lines))
            player.sendMessage(process(s));
    }

    /**
     * Sends requested information for a {@link Player}.
     *
     * @param player a valid online player
     * @param lines the information to send
     */
    public static void playerLog(Player player, String... lines) {
        playerLog(null, player, lines);
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console.
     *
     * @param m the {@link BeansMethods} instance
     * @param lines the information to send
     */
    public static void rawLog(BeansMethods m, String... lines) {
        for (String s : toLogLines(m, lines))
            Bukkit.getLogger().info(colorLogger(s));
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console.
     *
     * @param lines the information to send
     */
    public static void rawLog(String... lines) {
        rawLog(null, lines);
    }

    /**
     * Sends requested information to a {@link CommandSender}.
     *
     * @param m the {@link BeansMethods} instance
     * @param plugin the plugin's instance
     * @param sender a valid sender, can be the console or a player
     * @param lines the information to send
     */
    public static void doLog(BeansMethods m, JavaPlugin plugin, CommandSender sender, String... lines) {
        if (sender instanceof Player)
            playerLog(m, (Player) sender, lines);

        JavaPlugin p = m != null ? m.getPlugin() : plugin;

        for (String s : toLogLines(m, lines))
            p.getLogger().info(colorLogger(s));
    }

    /**
     * Sends requested information to the console.
     *
     * @param plugin the plugin's instance
     * @param lines the information to send
     */
    public static void doLog(@NotNull JavaPlugin plugin, String... lines) {
        doLog(null, plugin, null, lines);
    }

}
