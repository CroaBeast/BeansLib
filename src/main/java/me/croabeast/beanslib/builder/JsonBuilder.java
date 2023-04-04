package me.croabeast.beanslib.builder;

import com.google.common.collect.Lists;
import lombok.var;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.utility.Exceptions;
import me.croabeast.beanslib.utility.TextUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static net.md_5.bungee.api.chat.ClickEvent.Action.*;

/**
 * The object class to handle JSON messages to players in chat.
 *
 * <p> Uses an input string to apply a click event and a string list
 * to apply a hover event.
 *
 * <p> If click and hover values are null/empty, it will use the
 * {@link BeansLib#jsonRegex} to parse the respective actions and
 * add then in the chat component.
 *
 * <p> After all the actions are set in the chat component, the
 * message is sent to the target player.
 *
 * <pre> {@code
 * JsonConverter json = new JsonConverter(player, "my message");
 * // Sends the message to the target using a
 * // hover list and a string action.
 * json.send("RUN:/command", Arrays.asList("My hover message"));
 * // Send the message to the target using the defined pattern.
 * json.send();
 * } </pre>
 *
 * @author CroaBeast
 * @since 1.3
 */
public class JsonBuilder {

    private static BeansLib getLib() {
        return BeansLib.getLoadedInstance();
    }

    private final Player target;
    private final Player parser;
    private final String string;

    /**
     * Converts a {@link String} line to a {@code JsonBuilder} object.
     * <p> Parses all the required keys from {@link BeansLib} and formats colors.
     *
     * @param target a target to send a message, can be null
     * @param parser a parser to parse placeholders
     * @param string an input string
     */
    public JsonBuilder(Player target, Player parser, String string) {
        this.target = target == null ? parser : target;

        this.parser = parser;
        this.string = string;
    }

    static String removeLastChar(String string) {
        return string.substring(0, string.length() - 1);
    }

    static TextComponent toComponent(String string) {
        return new TextComponent(TextComponent.fromLegacyText(string));
    }

    static ClickEvent.Action parseAction(String input) {
        if (input.matches("(?i)suggest")) return SUGGEST_COMMAND;
        if (input.matches("(?i)url")) return OPEN_URL;
        if (input.matches("(?i)run")) return RUN_COMMAND;

        for (var ac : values()) if (input.matches("(?i)" + ac)) return ac;
        return null;
    }

    @SuppressWarnings("deprecation")
    void addHover(TextComponent comp, List<String> hover) {
        var array = new BaseComponent[hover.size()];

        for (int i = 0; i < hover.size(); i++) {
            var end = i == hover.size() - 1 ? "" : "\n";

            array[i] = toComponent(getLib().
                    colorize(target, parser, hover.get(i)) + end);
        }

        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, array));
    }

    void addEvent(TextComponent comp, String type, String string) {
        if (type.matches("(?i)hover")) {
            var array = getLib().splitLine(string);
            addHover(comp, Lists.newArrayList(array));
        }
        else if (parseAction(type) != null)
            comp.setClickEvent(new ClickEvent(parseAction(type), string));
    }

    static List<BaseComponent> compList(String string) {
        return Lists.newArrayList(TextComponent.fromLegacyText(string));
    }

    BaseComponent[] toJSON(String click, List<String> hover) {
        var line = TextUtils.PARSE_INTERACTIVE_CHAT.apply(parser, string);
        line = getLib().centerMessage(target, parser, line);

        var isListNotNull = hover != null && !hover.isEmpty();

        if (isListNotNull || StringUtils.isNotBlank(click)) {
            line = TextUtils.STRIP_JSON.apply(line);

            final var comp = toComponent(line);
            if (isListNotNull) addHover(comp, hover);

            if (StringUtils.isNotBlank(click)) {
                try {
                    var input = click.split(":", 2);
                    comp.setClickEvent(new ClickEvent(parseAction(input[0]), input[1]));
                } catch (Exception ignored) {}
            }

            return Lists.newArrayList(comp).toArray(new BaseComponent[0]);
        }

        line = TextUtils.CONVERT_OLD_JSON.apply(line);
        var match = getLib().getJsonPattern().matcher(line);

        int lastEnd = 0;
        var components = new ArrayList<BaseComponent>();

        while (match.find()) {
            final var comp = toComponent(match.group(7));
            final var arguments = match.group(1).split("[|]", 2);

            components.addAll(compList(
                    line.substring(lastEnd, match.start())));

            var event1 = arguments[0].split(":\"");
            addEvent(comp, event1[0], removeLastChar(event1[1]));

            if (arguments.length == 2) {
                var event2 = arguments[1].split(":\"");
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
     * Sends the {@code JsonBuilder} object to the target player,
     * if not defined will be to the parser player.
     *
     * @param click the click string
     * @param hover a string list
     *
     * @return true if the builder was sent, false otherwise
     */
    public boolean send(String click, List<String> hover) {
        try {
            Exceptions.checkPlayer(target);

            if (StringUtils.isBlank(string)) {
                target.sendMessage("");
                return true;
            }

            target.spigot().sendMessage(toJSON(click, hover));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends the {@code JsonBuilder} object to the target player,
     * if not defined will be to the parser player.
     *
     * @return true if the builder was sent, false otherwise
     */
    public boolean send() {
        return send(null, null);
    }

    /**
     * Converts a line to a {@link BaseComponent} array using the {@link BeansLib#jsonRegex}.
     *
     * <pre> {@code
     * // Example of how to use it:
     * String text = "<hover:\"a hover line\">text to apply</text>";
     * BaseComponent[] array = JsonBuilder.fromText(player, text);
     * player.spigot().sendMessage(array);
     * } </pre>
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
     * Converts a line to a {@link BaseComponent} array using the {@link BeansLib#jsonRegex}.
     *
     * <pre> {@code
     * // Example of how to use it:
     * String text = "<hover:\"a hover line\">text to apply</text>";
     * BaseComponent[] array = JsonBuilder.fromText(text);
     * player.spigot().sendMessage(array);
     * } </pre>
     *
     * @param text an input string
     * @return the requested chat component array
     */
    public static BaseComponent[] of(String text) {
        return of(null, text);
    }
}
