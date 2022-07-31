package me.croabeast.beanslib;

import com.google.common.collect.*;
import me.clip.placeholderapi.*;
import me.croabeast.beanslib.utilities.*;
import me.croabeast.iridiumapi.IridiumAPI;
import me.croabeast.beanslib.objects.*;
import me.croabeast.beanslib.utilities.*;
import me.croabeast.beanslib.utilities.chars.*;
import net.md_5.bungee.api.chat.*;
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

import static me.croabeast.beanslib.utilities.TextUtils.*;
import static me.croabeast.iridiumapi.IridiumAPI.*;
import static net.md_5.bungee.api.chat.ClickEvent.Action.*;

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
            line = centeredText(null, stripPrefix(line));
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
        if (StringUtils.isBlank(string)) return string;

        Pattern charPattern = Pattern.compile(charPattern());
        Matcher match = charPattern.matcher(string);

        while (match.find()) {
            char s = (char) Integer.parseInt(match.group(1), 16);
            string = string.replace(match.group(), s + "");
        }
        return string;
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
        return TextUtils.colorize(target, parser, parseChars(string));
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
        String initial = colorize(target, parser, stripJson(string));

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : initial.toCharArray()) {
            if (c == '§') previousCode = true;
            else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                CharacterInfo dFI = CharHandler.getInfo(c);
                messagePxSize += isBold ?
                        dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = chatBoxSize() - halvedMessageSize;
        int compensated = 0;

        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            // 4 is the SPACE char length + 1
            compensated += 4;
        }

        return sb + colorize(target, parser, string);
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
     * Defines a string if is centered or not.
     * @param target a target to parse colors depending on its client, can be null
     * @param sender a player to parse placeholders.
     * @param string the input line.
     * @return the result string.
     */
    public String centeredText(Player target, Player sender, String string) {
        return string.startsWith(centerPrefix()) ?
                centerMessage(target, sender, string.replace(centerPrefix(), "")) :
                colorize(target, sender, string);
    }

    /**
     * Defines a string if is centered or not.
     * @param player a player to parse placeholders.
     * @param string the input line.
     * @return the result string.
     */
    public String centeredText(Player player, String string) {
        return centeredText(null, player, string);
    }

    /**
     * Converts a string to a {@link TextComponent}.
     * @param string the string to convert.
     * @return the requested component.
     */
    private TextComponent toComponent(String string) {
        return new TextComponent(TextComponent.fromLegacyText(string));
    }

    /**
     * Gets a {@link ClickEvent.Action} from a string.
     * @param input an input string
     * @return the click action, can be null
     */
    private ClickEvent.Action parseAction(String input) {
        input = input.toUpperCase();

        if (input.matches("(?i)suggest")) return SUGGEST_COMMAND;
        if (input.matches("(?i)url")) return OPEN_URL;
        if (input.matches("(?i)run")) return RUN_COMMAND;

        for (ClickEvent.Action ac : values())
            if (input.equals(ac + "")) return ac;
        return null;
    }

    /**
     * Add a hover event in a component.
     * @param comp the component to add the event
     * @param hover the list to add as a hover
     */
    @SuppressWarnings("deprecation")
    private void addHover(Player player, TextComponent comp, List<String> hover) {
        BaseComponent[] array = new BaseComponent[hover.size()];
        for (int i = 0; i < hover.size(); i++) {
            String end = i == hover.size() - 1 ? "" : "\n";
            array[i] = toComponent(colorize(player, hover.get(i)) + end);
        }
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, array));
    }

    /**
     * Add the event found in the formatted line.
     * @param comp the component to add the event.
     * @param type the event's type.
     * @param string the input string for the event.
     */
    private void addEvent(Player player, TextComponent comp, String type, String string) {
        if (parseAction(type) != null)
            comp.setClickEvent(new ClickEvent(parseAction(type), string));
        else if (type.matches("(?i)hover"))
            addHover(player, comp, Arrays.asList(string.split(lineSeparator())));
    }

    /**
     * Removes the last char from a string using a start index.
     * @param string an input string
     * @param start a start index
     * @return the stripped string
     */
    private String removeLastChar(String string, int start) {
        return string.substring(start, string.length() - 1);
    }

    /**
     * Removes the last char from a string using the start of the string.
     * @param string an input string
     * @return the stripped string
     */
    private String removeLastChar(String string) {
        return removeLastChar(string, 0);
    }

    /**
     * Creates a list of {@link BaseComponent} from a string.
     * @param string an input string
     * @return the requested list of components
     */
    List<BaseComponent> compList(String string) {
        return Arrays.asList(TextComponent.fromLegacyText(string));
    }

    /**
     * Converts a line to a {@link BaseComponent} array and use it to send a json message.
     * Uses an input string to apply a click event and a string list to apply a hover event.
     * <p> Keep in mind that the click string has this format:
     * <pre> {@code Available Actions: RUN, SUGGEST, URL
     * "<ACTION>:<the click string>" -> "RUN:/me click to run"}</pre>
     * Send this {@link BaseComponent} array using:
     *     <pre>{@code player.spigot().sendMessage(stringToJson(...))}</pre>
     *
     * @param target a target to parse colors depending on its client, can be null
     * @param parser a player
     * @param string the input string
     * @param click the click string
     * @param hover a string list
     * @return the converted json object
     */
    public BaseComponent[] stringToJson(Player target, Player parser, String string, String click, List<String> hover) {
        string = parseInteractiveChat(parser, string);
        string = centeredText(target, parser, string);

        if (!hover.isEmpty() || click != null) {
            final TextComponent comp = toComponent(stripJson(string));
            if (!hover.isEmpty()) addHover(parser, comp, hover);

            if (click != null) {
                String[] input = click.split(":", 2);
                comp.setClickEvent(new ClickEvent(parseAction(input[0]), input[1]));
            }

            return Lists.newArrayList(comp).toArray(new BaseComponent[0]);
        }

        string = convertOldJson(string);

        Matcher match = JSON_PATTERN.matcher(string);
        int lastEnd = 0;
        List<BaseComponent> components = new ArrayList<>();

        while (match.find()) {
            final TextComponent comp = toComponent(match.group(7));
            final String[] arguments = match.group(1).split("[|]");

            components.addAll(compList(string.substring(lastEnd, match.start())));

            String[] event1 = arguments[0].split(":\"");
            addEvent(parser, comp, event1[0], removeLastChar(event1[1]));

            if (arguments.length == 2) {
                String[] event2 = arguments[0].split(":\"");
                addEvent(parser, comp, event2[0], removeLastChar(event2[1]));
            }

            components.add(comp);
            lastEnd = match.end();
        }

        if (lastEnd < (string.length() - 1))
            components.addAll(compList(string.substring(lastEnd)));

        return components.toArray(new BaseComponent[0]);
    }

    /**
     * Converts a line to a {@link BaseComponent} array and use it to send a json message.
     * Uses an input string to apply a click event and a string list to apply a hover event.
     * <p> Keep in mind that the click string has this format:
     * <pre> {@code Available Actions: RUN, SUGGEST, URL
     * "<ACTION>:<the click string>" -> "RUN:/me click to run"}</pre>
     * Send this {@link BaseComponent} array using:
     *     <pre>{@code player.spigot().sendMessage(stringToJson(...))}</pre>
     *
     * @param parser a player
     * @param string the input string
     * @param click the click string
     * @param hover a string list
     * @return the converted json object
     */
    public BaseComponent[] stringToJson(Player parser, String string, String click, List<String> hover) {
        return stringToJson(null, parser, string, click, hover);
    }

    /**
     * Converts a line to a {@link BaseComponent} array and use it to send a json message.
     * <p>It uses the {@link TextKeys#JSON_PATTERN} to applies click and hover events.
     * @param target a target to parse colors depending on its client, can be null
     * @param parser a player
     * @param line the input line
     * @return the converted json object
     */
    public BaseComponent[] stringToJson(Player target, Player parser, String line) {
        return stringToJson(target, parser, line, null, new ArrayList<>());
    }

    /**
     * Converts a line to a {@link BaseComponent} array and use it to send a json message.
     * <p>It uses the {@link TextKeys#JSON_PATTERN} to applies click and hover events.
     * @param parser a player
     * @param line the input line
     * @return the converted json object
     */
    public BaseComponent[] stringToJson(Player parser, String line) {
        return stringToJson(null, parser, line);
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

            line = colorize(target, parser, line);

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

                sendTitle(target, line.split(lineSeparator()),
                        defaultTitleTicks()[0], time, defaultTitleTicks()[2]);
            }
            else if (prefix.matches("(?i)" + jsonKey())) {
                String cmd = "minecraft:tellraw " + target.getName() + " " + line;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
            else if (prefix.matches("(?i)" + bossbarKey()))
                new Bossbar(getPlugin(), target, string).display();
            else if (prefix.matches("(?i)" + actionBarKey()))
                sendActionBar(target, line);
            else target.spigot().sendMessage(stringToJson(parser, line));
        }
        else target.spigot().sendMessage(stringToJson(parser, string));
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
     * @param sender a player to format and send the message
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

            if (sender != null && !(sender instanceof ConsoleCommandSender)) {
                Player player = (Player) sender;

                String[] k = {playerKey(), worldKey()},
                        v = {player.getName(), player.getWorld().getName()};

                sendMessage(player, replaceInsensitiveEach(line, k, v));
            }
            else rawLog(centeredText(null, line));
        }
    }

    /**
     * Sends a message list using {@link #sendMessage(Player, Player, String)}.
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String, String[], String[])}
     * @param sender a player to format and send the message
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
     * @param sender a player to format and send the message
     * @param list the message list
     */
    public void sendMessageList(CommandSender sender, List<String> list) {
        sendMessageList(sender, list, null, null);
    }

    /**
     * Sends a message list using {@link #sendMessage(Player, Player, String)}.
     * Parse keys and values using {@link TextUtils#replaceInsensitiveEach(String, String[], String[])}
     * @param sender a player to format and send the message
     * @param section the config file or section
     * @param path the path of the string or string list
     */
    public void sendMessageList(CommandSender sender, ConfigurationSection section, String path) {
        sendMessageList(sender, toList(section, path));
    }
}
