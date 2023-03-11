package me.croabeast.beanslib.object.misc;

import lombok.RequiredArgsConstructor;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.message.MessageKey;
import me.croabeast.beanslib.message.MessageSender;
import me.croabeast.beanslib.utility.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import static me.croabeast.iridiumapi.IridiumAPI.process;
import static me.croabeast.iridiumapi.IridiumAPI.stripAll;

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
     * A static instance of the logger.
     */
    public static final BeansLogger DEFAULT_LOGGER = new BeansLogger(BeansLib.getLoadedInstance());

    private final BeansLib lib;

    private String colorLogger(String string) {
        final String temp = TextUtils.STRIP_JSON.apply(string);
        return lib.isColoredConsole() ? process(temp) : stripAll(temp);
    }

    private String[] toLogLines(Player player, boolean isLog, String... lines) {
        List<String> list = new ArrayList<>();

        String sp = lib.getLineSeparator();

        for (String line : lines) {
            if (line == null) continue;

            line = lib.replacePrefixKey(line, isLog);
            line = line.replace(sp, "&f" + sp);

            MessageKey k = MessageKey.identifyKey(line);

            if (isLog && lib.isStripPrefix() && k != MessageKey.CHAT_KEY) {
                Matcher match = k.getPattern().matcher(line);
                if (match.find()) line = line.replace(match.group(), "");
            }

            list.add(lib.centerMessage(player, player, line));
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
        new MessageSender().setTargets(player).
                send(false, Arrays.asList(toLogLines(player, false, lines)));
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
     *
     * @throws NullPointerException if the plugin is null
     */
    public void doLog(CommandSender sender, String... lines) {
        if (sender instanceof Player)
            playerLog((Player) sender, lines);

        for (String s : toLogLines(lines))
            lib.getPlugin().getLogger().info(colorLogger(s));
    }

    /**
     * Sends requested information to the console.
     *
     * @param lines the information to send
     * @throws NullPointerException if the plugin is null
     */
    public void doLog(String... lines) {
        doLog(null, lines);
    }
}
