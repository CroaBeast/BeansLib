package me.croabeast.beanslib.misc;

import lombok.RequiredArgsConstructor;
import lombok.var;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.message.MessageKey;
import me.croabeast.beanslib.message.MessageSender;
import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.iridiumapi.IridiumAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manages logger manages to player and/or console.
 * Have support for color codes.
 *
 * @author CroaBeast
 * @since 1.4
 */
@RequiredArgsConstructor
public class BeansLogger {

    /**
     * The static instance of the logger.
     */
    public static final BeansLogger DEFAULT_LOGGER = new BeansLogger(BeansLib.getLoadedInstance());

    private final BeansLib lib;

    private String colorLogger(String string) {
        final String s = TextUtils.STRIP_JSON.apply(string);

        return lib.isColoredConsole() ?
                IridiumAPI.process(s) : IridiumAPI.stripAll(s);
    }

    private List<String> toLogLines(Player p, boolean isLog, String... lines) {
        if (lines == null || lines.length == 0)
            return new ArrayList<>();

        final var split = lib.getLineSeparator();
        final var list = new ArrayList<String>();

        for (var line : lines) {
            if (line == null) continue;

            line = lib.replacePrefixKey(line, isLog);
            line = line.replace(split, "&f" + split);

            isLog = isLog && lib.isStripPrefix();
            var key = MessageKey.identifyKey(line);

            if (isLog && key != MessageKey.CHAT_KEY) {
                var match = key.getPattern().matcher(line);

                if (match.find())
                    line = line.replace(match.group(), "");
            }

            list.add(lib.centerMessage(p, p, line));
        }

        return list;
    }

    private List<String> toLogLines(String... lines) {
        return toLogLines(null, true, lines);
    }

    /**
     * Sends information to a player using the {@link MessageSender} object.
     *
     * @param player a valid player
     * @param lines the information to send
     */
    public void playerLog(Player player, String... lines) {
        new MessageSender().setTargets(player).send(false, toLogLines(player, false, lines));
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console,
     * avoiding the plugin prefix.
     *
     * @param lines the information to send
     */
    public void rawLog(String... lines) {
        for (var s : toLogLines(lines)) Bukkit.getLogger().info(colorLogger(s));
    }

    /**
     * Sends requested information to a {@link CommandSender} using the plugin's
     * logger and its prefix.
     *
     * <p> If the sender is a {@link Player} and it's not null, it will log using
     * {@link #playerLog(Player, String...)}.
     *
     *
     * @param sender a valid sender, can be the console, a player or null
     * @param lines the information to send
     *
     * @throws NullPointerException if the plugin is null
     */
    public void doLog(CommandSender sender, String... lines) {
        if (sender instanceof Player) playerLog((Player) sender, lines);

        for (var s : toLogLines(lines))
            lib.getPlugin().getLogger().info(colorLogger(s));
    }

    /**
     * Sends requested information to a {@link CommandSender} using the plugin's
     * logger and its prefix.
     *
     * @param lines the information to send
     * @throws NullPointerException if the plugin is null
     */
    public void doLog(String... lines) {
        doLog(null, lines);
    }

    private static boolean getBool(String string) {
        return string.matches("(?i)true|false") && string.matches("(?i)true");
    }

    /**
     * Sends information choosing which of the two main methods will be used
     * in each line. ({@link #rawLog(String...) rawLog}, {@link #doLog(CommandSender, String...) doLog})
     *
     * <p> If the line does not start with a boolean value or that value is false,
     * it will use the {@link #rawLog(String...)  rawLog} method, otherwise will
     * use the {@link #doLog(CommandSender, String...) doLog} method.
     *
     * <pre> {@code
     * "My information for the console" >> // Uses the "doLog" method.
     * "true::My basic log information" >> // Uses the "rawLog" method.
     * "false::Plugin's information" >> // Uses the "doLog" method.
     * } </pre>
     *
     * @param sender a valid sender, can be the console, a player or null
     * @param lines the information to send
     */
    public void mixLog(CommandSender sender, String... lines) {
        if (lines == null || lines.length == 0)
            return;

        for (var line : lines) {
            var array = line.split("::", 2);

            if (array.length != 2) {
                rawLog(line);
                continue;
            }

            if (getBool(array[0])) {
                doLog(sender, array[1]);
                continue;
            }

            rawLog(array[1]);
        }
    }
}
