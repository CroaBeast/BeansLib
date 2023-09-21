package me.croabeast.beanslib.misc;

import lombok.RequiredArgsConstructor;
import lombok.var;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.message.MessageExecutor;
import me.croabeast.beanslib.message.MessageSender;
import me.croabeast.beanslib.utility.ArrayUtils;
import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.neoprismatic.NeoPrismaticAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * This class can manages how strings can be logged/sent to a player and/or console.
 *
 * @author CroaBeast
 * @since 1.4
 */
@RequiredArgsConstructor
public class BeansLogger {

    private final BeansLib lib;

    private List<String> toLogLines(Player p, boolean isLog, String... lines) {
        if (ArrayUtils.isArrayEmpty(lines))
            return new ArrayList<>();

        final String split = lib.getLineSeparator();
        final List<String> list = new ArrayList<>();

        for (String line : lines) {
            if (line == null) continue;

            line = lib.replacePrefixKey(line, isLog);
            line = line.replace(split, "&f" + split);

            isLog = isLog && lib.isStripPrefix();
            var key = MessageExecutor.identifyKey(line);

            if (isLog && key != MessageExecutor.CHAT_EXECUTOR) {
                Matcher match = key.getPattern().matcher(line);

                if (match.find())
                    line = line.replace(match.group(), "");
            }

            list.add(lib.createCenteredChatMessage(p, p, line));
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
        new MessageSender(player).setLogger(false).send(toLogLines(player, false, lines));
    }

    private String colorLogger(String string) {
        final String s = TextUtils.STRIP_JSON.apply(string);

        return lib.isColoredConsole() ?
                NeoPrismaticAPI.colorize(s) : NeoPrismaticAPI.stripAll(s);
    }

    private void raw(String line) {
        Bukkit.getLogger().info(colorLogger(line));
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console,
     * avoiding the plugin prefix.
     *
     * @param lines the information to send
     */
    public void rawLog(String... lines) {
        toLogLines(lines).forEach(this::raw);
    }

    private void log(String line) {
        lib.getPlugin().getLogger().info(colorLogger(line));
    }

    /**
     * Sends requested information to a {@link CommandSender} using the plugin's
     * logger and its prefix.
     *
     * <p> If the sender is a {@link Player} and it's not null, it will also send
     * the information to the player using {@link #playerLog(Player, String...)}.
     *
     * @param sender a valid sender, can be the console, a player or null
     * @param lines the information to send
     *
     * @throws NullPointerException if the plugin is null
     */
    public void doLog(CommandSender sender, String... lines) {
        if (sender instanceof Player) playerLog((Player) sender, lines);
        toLogLines(lines).forEach(this::log);
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
     * Sends information choosing which of the two main methods will be used in each line.
     * ({@link #rawLog(String...) rawLog}, {@link #doLog(CommandSender, String...) doLog})
     *
     * <p> If the line does not start with a boolean value or that value is false,
     * it will use the {@link #doLog(CommandSender, String...) doLog} method, otherwise
     * will use the {@link #rawLog(String...) rawLog} method.
     *
     * <pre> {@code
     * "My information for the console" >> // Uses "doLog"
     * "true::My basic log information" >> // Uses "rawLog"
     * "false::Some plugin's information" >> // Uses "doLog"
     * "" or null >> // Uses "doLog", 'cause is empty/null
     * } </pre>
     *
     * @param sender a valid sender, can be the console, a player or null
     * @param lines the information to send
     */
    public void mixLog(CommandSender sender, String... lines) {
        if (ArrayUtils.isArrayEmpty(lines))
            return;

        var mSender = new MessageSender(sender).setLogger(false);

        for (String line : lines) {
            if (StringUtils.isBlank(line)) {
                if (sender != null) mSender.singleSend(line);

                log(line);
                continue;
            }

            String[] array = line.split("::", 2);

            if (array.length != 2) {
                if (sender != null) mSender.singleSend(line);

                log(line);
                continue;
            }

            if (getBool(array[0])) {
                raw(array[1]);
                continue;
            }

            if (sender != null) mSender.singleSend(array[1]);
            log(array[1]);
        }
    }

    /**
     * Sends information choosing which of the two main methods will be used in each line.
     * ({@link #rawLog(String...) rawLog}, {@link #doLog(String...) doLog})
     *
     * <p> If the line does not start with a boolean value or that value is false,
     * it will use the {@link #doLog(String...) doLog} method, otherwise will use the
     * {@link #rawLog(String...) rawLog} method.
     *
     * <pre> {@code
     * "My information for the console" >> // Uses "doLog"
     * "true::My basic log information" >> // Uses "rawLog"
     * "false::Some plugin's information" >> // Uses "doLog"
     * "" or null >> // Uses "doLog", 'cause is empty/null
     * } </pre>
     *
     * @param lines the information to send
     */
    public void mixLog(String... lines) {
        mixLog(null, lines);
    }
}
