package me.croabeast.beanslib.misc;

import lombok.RequiredArgsConstructor;
import lombok.var;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.message.MessageKey;
import me.croabeast.beanslib.message.MessageSender;
import me.croabeast.beanslib.utility.TextUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

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
     * The static instance of the logger.
     */
    public static final BeansLogger DEFAULT_LOGGER = new BeansLogger(BeansLib.getLoadedInstance());

    private final BeansLib lib;

    private String colorLogger(String string) {
        final String s = TextUtils.STRIP_JSON.apply(string);
        return lib.isColoredConsole() ? process(s) : stripAll(s);
    }

    private String[] toLogLines(Player p, boolean isLog, String... lines) {
        final var splitter = lib.getLineSeparator();
        final var list = new ArrayList<String>();

        for (var line : lines) {
            if (StringUtils.isBlank(line)) continue;

            line = lib.replacePrefixKey(line, isLog);
            line = line.replace(splitter, "&f" + splitter);

            isLog = isLog && lib.isStripPrefix();
            var key = MessageKey.identifyKey(line);

            if (isLog && key != MessageKey.CHAT_KEY) {
                var match = key.getPattern().matcher(line);
                if (!match.find()) continue;

                line = line.replace(match.group(), "");
            }

            list.add(lib.centerMessage(p, p, line));
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
        for (var s : toLogLines(lines)) Bukkit.getLogger().info(colorLogger(s));
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
        if (sender instanceof Player) playerLog((Player) sender, lines);

        for (var s : toLogLines(lines))
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
