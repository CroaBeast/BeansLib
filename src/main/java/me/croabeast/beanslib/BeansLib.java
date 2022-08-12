package me.croabeast.beanslib;

import me.clip.placeholderapi.*;
import me.croabeast.beanslib.utility.*;
import me.croabeast.iridiumapi.IridiumAPI;
import me.croabeast.beanslib.object.*;
import org.apache.commons.lang.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

import static me.croabeast.beanslib.utility.TextUtils.*;
import static me.croabeast.iridiumapi.IridiumAPI.*;

/**
 * The main class of the Lib.
 * It has a lot of useful text-related methods.
 *
 * @author CroaBeast
 * @since 1.0
 */
public abstract class BeansLib extends TextKeys {

    /**
     * The {@link JavaPlugin} instance of your project.
     * @return plugin instance
     */
    @NotNull
    protected abstract JavaPlugin getPlugin();

    /**
     * Converts a string array to a List for the logger.
     * @param lines string array
     * @return the converted list
     */
    private List<String> toLogLines(String... lines) {
        List<String> list = new ArrayList<>();
        String sp = lineSeparator();

        for (String line : lines) {
            if (line == null) continue;
            line = centerMessage(null, stripPrefix(line));
            list.add(line.replace(sp, "&f" + sp));
        }
        return list;
    }

    /**
     * Sends requested information for a {@link Player}.
     * @param player a valid online player
     * @param lines the information to send
     */
    public void playerLog(@NotNull Player player, String... lines) {
        for (String s : toLogLines(lines))
            player.sendMessage(process(s.replace(langPrefixKey(), langPrefix())));
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console.
     * @param lines the information to send
     */
    public void rawLog(String... lines) {
        for (String s : toLogLines(lines))
            Bukkit.getLogger().info(LogUtils.colorLogger(s, fixColorLogger()));
    }

    /**
     * Sends requested information to a {@link CommandSender}.
     * @param sender a valid sender, can be the console or a player
     * @param lines the information to send
     */
    public void doLog(@Nullable CommandSender sender, String... lines) {
        if (sender instanceof Player) playerLog((Player) sender, lines);

        for (String s : toLogLines(lines)) {
            s = s.replace(langPrefixKey(), "");
            s = LogUtils.colorLogger(s, fixColorLogger());
            getPlugin().getLogger().info(s);
        }
    }

    /**
     * Sends requested information to the console.
     * @param lines the information to send
     */
    public void doLog(String... lines) {
        doLog(null, lines);
    }

    /**
     * Remove the text identifier prefix from the input line if {@link #isStripPrefix()} is true.
     * <p>See more in {@link TextKeys#textPattern()}
     * @param string the input line
     * @return the stripped message
     */
    public String stripPrefix(String string) {
        if (StringUtils.isBlank(string)) return string;

        Matcher matcher = textPattern().matcher(string);
        string = removeSpace(string);

        return (matcher.find() && isStripPrefix()) ?
                string.replace(matcher.group(1), "") : string;
    }

    /**
     * Removes the first spaces of a line if {@link #isHardSpacing()} is true.
     * @param string the input line
     * @return the line without the first spaces
     */
    public String removeSpace(String string) {
        return isHardSpacing() ? TextUtils.removeSpace(string) : string;
    }

    /**
     * Use the {@link #charPattern()} to find unicode values and
     * replace them with its respective characters.
     * @param string the input line
     * @return the parsed message with the new characters
     */
    public String parseChars(String string) {
        return TextUtils.parseChars(charPattern(), string);
    }

    /**
     * Parses the requested message using this sequence:
     * <blockquote><pre>
     * 1. First, parses characters using {@link #parseChars(String)}
     * 2. Then, parses {@link PlaceholderAPI} placeholders using {@link TextUtils#parsePAPI(Player, String)}
     * 3. Finally, applies color format using {@link IridiumAPI#process(Player, String)}.</pre></blockquote>
     * @param target a target to parse colors depending on its client, can be null
     * @param parser a player, can be null
     * @param string the input message
     * @return the formatted message
     */
    public String colorize(Player target, Player parser, String string) {
        return TextUtils.colorize(target, parser, charPattern(), string);
    }

    /**
     * Parses the requested message using:
     * <blockquote><pre>
     * 1. First, parses characters using {@link #parseChars(String)}
     * 2. Then, parses {@link PlaceholderAPI} placeholders using {@link TextUtils#parsePAPI(Player, String)}
     * 3. Finally, applies color format using {@link IridiumAPI#process(Player, String)}.</pre></blockquote>
     * @param player a player, can be null
     * @param string the input message
     * @return the formatted message
     */
    public String colorize(Player player, String string) {
        return colorize(null, player, string);
    }

    /**
     * Creates a centered chat message.
     * @param target a target to parse colors depending on its client, can be null
     * @param parser a player to parse placeholders.
     * @param string the input message.
     * @return the centered chat message.
     */
    public String centerMessage(Player target, Player parser, String string) {
        return TextUtils.centerMessage(target, parser, charPattern(), centerPrefix(), string);
    }

    /**
     * Creates a centered chat message.
     * @param player a player to parse placeholders.
     * @param string the input message.
     * @return the centered chat message.
     */
    public String centerMessage(Player player, String string) {
        return centerMessage(null, player, string);
    }

    /**
     * Sends a message depending on its prefix. See {@link TextKeys#textPattern()} for more info.
     * @param target a target player to send, can be null
     * @param parser a player to format the message
     * @param string the input string
     */
    public void sendMessage(Player target, Player parser, String string) {
        if (target == null) target = parser;
        Matcher matcher = textPattern().matcher(string);

        if (matcher.find()) {
            String prefix = removeLastChar(matcher.group(1), 1);

            String line = removeSpace(string.substring(prefix.length() + 2));
            String color = colorize(target, parser, line);

            if (prefix.matches("(?i)" + titleKey())) {
                Matcher m = Pattern.compile("(?i)" + titleKey()).matcher(prefix);

                String t = null;
                try {
                    if (m.find()) t = m.group(1).substring(1);
                } catch (Exception ignored) {}

                int time = defaultTitleTicks()[1];
                try {
                    if (t != null) time = Integer.parseInt(t) * 20;
                } catch (Exception ignored) {}

                sendTitle(target, color.split(lineSeparator()),
                        defaultTitleTicks()[0], time, defaultTitleTicks()[2]);
            }
            else if (prefix.matches("(?i)" + jsonKey())) {
                String cmd = "minecraft:tellraw " + target.getName() + " " + line;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
            else if (prefix.matches("(?i)" + bossbarKey())) {
                new Bossbar(getPlugin(), target, string).display();
            }
            else if (prefix.matches("(?i)" + actionBarKey())) {
                sendActionBar(target, color);
            }
            else new JsonMessage(this, parser, line).send(target);
        }
        else new JsonMessage(this, parser, string).send(target);
    }

    /**
     * Sends a message depending on its prefix. See {@link TextKeys#textPattern()} for more info.
     * @param parser a player to format the message
     * @param string the input string
     */
    public void sendMessage(Player parser, String string) {
        sendMessage(null, parser, string);
    }

    /**
     * Sends a message list using {@link #sendMessage(Player, Player, String)}.
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String, String[], String[])}
     * @param sender a sender to format and send the message
     * @param list the message list
     * @param keys a keys array
     * @param values a values array
     */
    public void sendMessageList(CommandSender sender, List<String> list, String[] keys, String[] values) {
        list = list.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (list.isEmpty()) return;

        for (String line : list) {
            line = line.replace(langPrefixKey(), langPrefix());
            line = replaceInsensitiveEach(line, keys, values);

            if (sender == null || sender instanceof ConsoleCommandSender) {
                rawLog(centerMessage(null, line));
                continue;
            }

            Player player = (Player) sender;
            String[] v = {player.getName(), player.getWorld().getName()},
                    k = {playerKey(), worldKey()};
            sendMessage(player, replaceInsensitiveEach(line, k, v));
        }
    }

    /**
     * Sends a message list using {@link #sendMessage(Player, Player, String)}.
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String, String[], String[])}
     * @param sender a sender to format and send the message
     * @param section the config file or section
     * @param path the path of the string or string list
     * @param keys a keys array
     * @param values a values array
     */
    public void sendMessageList(CommandSender sender, ConfigurationSection section, String path, String[] keys, String[] values) {
        sendMessageList(sender, toList(section, path), keys, values);
    }

    /**
     * Sends a message list using {@link #sendMessage(Player, Player, String)}.
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String, String[], String[])}
     * @param sender a sender to format and send the message
     * @param list the message list
     */
    public void sendMessageList(CommandSender sender, List<String> list) {
        sendMessageList(sender, list, null, null);
    }

    /**
     * Sends a message list using {@link #sendMessage(Player, Player, String)}.
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String, String[], String[])}
     * @param sender a sender to format and send the message
     * @param section the config file or section
     * @param path the path of the string or string list
     */
    public void sendMessageList(CommandSender sender, ConfigurationSection section, String path) {
        sendMessageList(sender, toList(section, path));
    }
}
