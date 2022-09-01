package me.croabeast.beanslib.object;

import com.google.common.collect.*;
import me.croabeast.beanslib.utility.*;
import me.croabeast.beanslib.utility.key.LibKeys;
import me.croabeast.beanslib.utility.key.TextKeys;
import net.md_5.bungee.api.chat.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.*;

import java.util.*;
import java.util.regex.*;

import static net.md_5.bungee.api.chat.ClickEvent.Action.*;

/**
 * The object class to handle JSON messages.
 *
 * @author CroaBeast
 * @since 1.3
 */
public class JsonMessage {

    private final TextKeys keys;

    private final Player target;
    private final Player parser;
    private final String string;

    /**
     * Converts a {@link String} line to a {@code JsonMessage} object.
     * <p> Parses all the required keys from {@link TextKeys} and formats colors.
     *
     * @param keys the {@link TextKeys} instance
     * @param target a target to send a message, can be null
     * @param parser a parser to parse placeholders
     * @param string an input string
     */
    public JsonMessage(TextKeys keys, Player target, Player parser, String string) {
        this.keys = keys != null ? keys : LibKeys.DEFAULTS;
        this.target = target == null ? parser : target;

        this.parser = parser;
        this.string = string;
    }

    /**
     * Converts a {@link String} line to a {@code JsonMessage} object.
     * <p> Parses all the required keys from {@link LibKeys#DEFAULTS} and formats colors.
     *
     * @param target a target to send a message, can be null
     * @param parser a parser to parse placeholders
     * @param string an input string
     */
    public JsonMessage(Player target, Player parser, String string) {
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
            array[i] = toComponent(
                    TextUtils.colorize(keys, target, parser, hover.get(i)) + end);
        }
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, array));
    }

    void addEvent(TextComponent comp, String type, String string) {
        if (parseAction(type) != null)
            comp.setClickEvent(new ClickEvent(parseAction(type), string));
        else if (type.matches("(?i)hover")) {
            String[] array = string.split(keys.lineSeparator());
            addHover(comp, Lists.newArrayList(array));
        }
    }

    List<BaseComponent> compList(String string) {
        return Lists.newArrayList(TextComponent.fromLegacyText(string));
    }

    BaseComponent[] convertString(String click, List<String> hover) {
        String line = TextUtils.parseInteractiveChat(parser, string);
        line = TextUtils.centerMessage(keys, target, parser, line);

        if (!hover.isEmpty() || StringUtils.isNotBlank(click)) {
            final TextComponent comp = toComponent(TextUtils.stripJson(line));
            if (!hover.isEmpty()) addHover(comp, hover);

            if (StringUtils.isNotBlank(click)) {
                String[] input = click.split(":", 2);
                comp.setClickEvent(new ClickEvent(parseAction(input[0]), input[1]));
            }

            return Lists.newArrayList(comp).toArray(new BaseComponent[0]);
        }

        line = TextUtils.convertOldJson(line);
        Matcher match = LibKeys.JSON_PATTERN.matcher(line);

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
     * <p> Uses an input string to apply a click event and a
     * string list to apply a hover event.
     * <p> If click and hover values are null/empty, it will use
     * the {@link LibKeys#JSON_PATTERN} to parse the respective actions
     * and add then in the chat component.
     * <p> After all the actions are set in the chat component,
     * the message is sent to the target player.
     * <pre>{@code
     * JsonConverter json = new JsonConverter(player, "my message");
     * // Send the message to the target using a hover list and a string action.
     * json.send("RUN:/command", Arrays.asList("My hover message"));
     * }</pre>
     *
     * @param click the click string
     * @param hover a string list
     *
     * @throws NullPointerException if the target is null
     */
    public void send(String click, List<String> hover) {
        Exceptions.checkPlayer(target).spigot().sendMessage(convertString(click, hover));
    }

    /**
     * Sends the {@code JsonMessage} object to the target player,
     * if not defined will be to the parser player.
     * <p> Uses the {@link LibKeys#JSON_PATTERN} to parse the
     * respective actions and add then in the chat component.
     * <p> After all the actions are set in the chat component,
     * the message is sent to the target player.
     * <pre>{@code
     * JsonConverter json = new JsonConverter(target, player, "my message");
     * json.send(); // Send the message to the target using the defined pattern.
     * }</pre>
     *
     * @throws NullPointerException if the target is null
     */
    public void send() {
        send(null, new ArrayList<>());
    }

    /**
     * Converts a line to a {@link BaseComponent} array using the {@link LibKeys#JSON_PATTERN}.
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
    public static BaseComponent[] fromText(Player parser, String text) {
        return new JsonMessage(null, parser, text).convertString(null, new ArrayList<>());
    }

    /**
     * Converts a line to a {@link BaseComponent} array using the {@link LibKeys#JSON_PATTERN}.
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
    public static BaseComponent[] fromText(String text) {
        return fromText(null, text);
    }
}
