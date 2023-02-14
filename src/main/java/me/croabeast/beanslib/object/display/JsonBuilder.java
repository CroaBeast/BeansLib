package me.croabeast.beanslib.object.display;

import com.google.common.collect.Lists;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.utility.Exceptions;
import me.croabeast.beanslib.utility.LibUtils;
import me.croabeast.beanslib.utility.TextUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static net.md_5.bungee.api.chat.ClickEvent.Action.*;

/**
 * The object class to handle JSON messages to players in chat.
 *
 * <p> Uses an input string to apply a click event and a string list
 * to apply a hover event.
 *
 * <p> If click and hover values are null/empty, it will use the
 * {@link LibUtils#JSON_PATTERN} to parse the respective actions and
 * add then in the chat component.
 *
 * <p> After all the actions are set in the chat component, the
 * message is sent to the target player.
 * <pre>{@code
 * JsonConverter json = new JsonConverter(player, "my message");
 * // Sends the message to the target using a
 * // hover list and a string action.
 * json.send("RUN:/command", Arrays.asList("My hover message"));
 * // Send the message to the target using the defined pattern.
 * json.send();
 * }</pre>
 *
 * @author CroaBeast
 * @since 1.3
 */
public class JsonBuilder {

    private final BeansLib lib;

    private final Player target;
    private final Player parser;
    private final String string;

    /**
     * Converts a {@link String} line to a {@code JsonMessage} object.
     * <p> Parses all the required keys from {@link BeansLib} and formats colors.
     *
     * @param lib the {@link BeansLib} instance
     * @param target a target to send a message, can be null
     * @param parser a parser to parse placeholders
     * @param string an input string
     */
    public JsonBuilder(BeansLib lib, Player target, Player parser, String string) {
        this.lib = lib != null ? lib : BeansLib.DEFAULTS;
        this.target = target == null ? parser : target;

        this.parser = parser;
        this.string = string;
    }

    /**
     * Converts a {@link String} line to a {@code JsonMessage} object.
     * <p> Parses all the required keys and methods from {@link BeansLib#DEFAULTS}.
     *
     * @param target a target to send a message, can be null
     * @param parser a parser to parse placeholders
     * @param string an input string
     */
    public JsonBuilder(Player target, Player parser, String string) {
        this(null, target, parser, string);
    }

    String removeLastChar(String string) {
        return string.substring(0, string.length() - 1);
    }

    TextComponent toComponent(String string) {
        return new TextComponent(TextComponent.fromLegacyText(string));
    }

    ClickEvent.Action parseAction(String input) {
        if (input.matches("(?i)suggest")) return SUGGEST_COMMAND;
        if (input.matches("(?i)url")) return OPEN_URL;
        if (input.matches("(?i)run")) return RUN_COMMAND;

        for (ClickEvent.Action ac : values())
            if (input.matches("(?i)" + ac)) return ac;
        return null;
    }

    @SuppressWarnings("deprecation")
    void addHover(TextComponent comp, List<String> hover) {
        BaseComponent[] array = new BaseComponent[hover.size()];

        for (int i = 0; i < hover.size(); i++) {
            String end = i == hover.size() - 1 ? "" : "\n";
            array[i] = toComponent(lib.colorize(target, parser, hover.get(i)) + end);
        }

        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, array));
    }

    void addEvent(TextComponent comp, String type, String string) {
        if (type.matches("(?i)hover")) {
            String[] array = string.split(lib.getLineSeparator());
            addHover(comp, Lists.newArrayList(array));
        }
        else if (parseAction(type) != null)
            comp.setClickEvent(new ClickEvent(parseAction(type), string));
    }

    List<BaseComponent> compList(String string) {
        return Lists.newArrayList(TextComponent.fromLegacyText(string));
    }

    BaseComponent[] toJSON(String click, List<String> hover) {
        String line = TextUtils.parseInteractiveChat(parser, string);
        line = lib.centerMessage(target, parser, line);

        if (!hover.isEmpty() || StringUtils.isNotBlank(click)) {
            final TextComponent comp = toComponent(TextUtils.stripJson(line));
            if (!hover.isEmpty()) addHover(comp, hover);

            if (StringUtils.isNotBlank(click)) {
                try {
                    String[] input = click.split(":", 2);
                    comp.setClickEvent(new ClickEvent(parseAction(input[0]), input[1]));
                } catch (Exception ignored) {}
            }

            return Lists.newArrayList(comp).toArray(new BaseComponent[0]);
        }

        line = TextUtils.convertOldJson(line);
        Matcher match = LibUtils.JSON_PATTERN.matcher(line);

        int lastEnd = 0;
        List<BaseComponent> components = new ArrayList<>();

        while (match.find()) {
            final TextComponent comp = toComponent(match.group(7));
            final String[] arguments = match.group(1).split("[|]", 2);

            components.addAll(compList(
                    line.substring(lastEnd, match.start())));

            String[] event1 = arguments[0].split(":\"");
            addEvent(comp, event1[0], removeLastChar(event1[1]));

            if (arguments.length == 2) {
                String[] event2 = arguments[1].split(":\"");
                addEvent(comp, event2[0], removeLastChar(event2[1]));
            }

            components.add(comp);
            lastEnd = match.end();
        }

        if (lastEnd < (line.length() - 1))
            components.addAll(compList(line.substring(lastEnd)));

        return components.toArray(new BaseComponent[0]);
    }

    /**
     * Sends the {@code JsonMessage} object to the target player,
     * if not defined will be to the parser player.
     *
     * @param click the click string
     * @param hover a string list
     *
     * @throws NullPointerException if the target is null
     */
    public void send(String click, List<String> hover) {
        Exceptions.checkPlayer(target).spigot().sendMessage(toJSON(click, hover));
    }

    /**
     * Sends the {@code JsonMessage} object to the target player,
     * if not defined will be to the parser player.
     *
     * @throws NullPointerException if the target is null
     */
    public void send() {
        send(null, new ArrayList<>());
    }

    /**
     * Converts a line to a {@link BaseComponent} array using the {@link LibUtils#JSON_PATTERN}.
     *
     * <p> Example of how to use it:
     * <pre> {@code
     * String text = "<hover:\"a hover line\">text to apply</text>";
     * BaseComponent[] array = JsonMessage.fromText(player, text);
     * player.spigot().sendMessage(array);
     * }</pre>
     *
     * @param parser a player to parse placeholders
     * @param text an input string
     *
     * @return the requested chat component array
     */
    public static BaseComponent[] of(Player parser, String text) {
        return new JsonBuilder(null, parser, text).toJSON(null, new ArrayList<>());
    }

    /**
     * Converts a line to a {@link BaseComponent} array using the {@link LibUtils#JSON_PATTERN}.
     *
     * <p> Example of how to use it:
     * <pre> {@code
     * String text = "<hover:\"a hover line\">text to apply</text>";
     * BaseComponent[] array = JsonMessage.fromText(text);
     * player.spigot().sendMessage(array);
     * }</pre>
     *
     * @param text an input string
     * @return the requested chat component array
     */
    public static BaseComponent[] of(String text) {
        return of(null, text);
    }
}
