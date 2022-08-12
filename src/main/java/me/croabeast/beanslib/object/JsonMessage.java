package me.croabeast.beanslib.object;

import com.google.common.collect.*;
import me.croabeast.beanslib.*;
import me.croabeast.beanslib.utility.*;
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

    private final BeansLib lib;
    private final Player parser;
    private final String string;

    /**
     * Basic JSON constructor.
     * @param lib the {@link BeansLib} instance
     * @param parser a parser
     * @param string an input string
     */
    public JsonMessage(BeansLib lib, Player parser, String string) {
        this.lib = lib;
        this.parser = parser;
        this.string = string;
    }

    /**
     * Basic JSON constructor.
     * @param parser a parser
     * @param string an input string
     */
    public JsonMessage(Player parser, String string) {
        this(null, parser, string);
    }

    String getSplit() {
        return lib == null ? TextUtils.DEFAULT_KEYS.lineSeparator() : lib.lineSeparator();
    }

    String colorize(Player target, String string) {
        if (lib != null) return lib.colorize(target, parser, string);
        return TextUtils.colorize(target, parser, string);
    }

    String centerMessage(Player target, String string) {
        if (lib != null) return lib.centerMessage(target, parser, string);
        return TextUtils.centerMessage(target, parser, string);
    }

    TextComponent toComponent(String string) {
        return new TextComponent(TextComponent.fromLegacyText(string));
    }

    ClickEvent.Action parseAction(String input) {
        input = input.toUpperCase();

        if (input.matches("(?i)suggest")) return SUGGEST_COMMAND;
        if (input.matches("(?i)url")) return OPEN_URL;
        if (input.matches("(?i)run")) return RUN_COMMAND;

        for (ClickEvent.Action ac : values())
            if (input.equals(ac + "")) return ac;
        return null;
    }

    @SuppressWarnings("deprecation")
    void addHover(Player target, TextComponent comp, List<String> hover) {
        BaseComponent[] array = new BaseComponent[hover.size()];
        for (int i = 0; i < hover.size(); i++) {
            String end = i == hover.size() - 1 ? "" : "\n";
            array[i] = toComponent(colorize(target, hover.get(i)) + end);
        }
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, array));
    }

    void addEvent(Player target, TextComponent comp, String type, String string) {
        if (parseAction(type) != null)
            comp.setClickEvent(new ClickEvent(parseAction(type), string));
        else if (type.matches("(?i)hover")) {
            String[] array = string.split(getSplit());
            addHover(target, comp, Lists.newArrayList(array));
        }
    }

    List<BaseComponent> compList(String string) {
        return Arrays.asList(TextComponent.fromLegacyText(string));
    }

    BaseComponent[] stringToJson(Player target, String click, List<String> hover) {
        String line = TextUtils.parseInteractiveChat(parser, string);
        line = centerMessage(target, line);

        if (!hover.isEmpty() || StringUtils.isNotBlank(click)) {
            final TextComponent comp = toComponent(TextUtils.stripJson(line));
            if (!hover.isEmpty()) addHover(target, comp, hover);

            if (StringUtils.isNotBlank(click)) {
                String[] input = click.split(":", 2);
                comp.setClickEvent(new ClickEvent(parseAction(input[0]), input[1]));
            }

            return Lists.newArrayList(comp).toArray(new BaseComponent[0]);
        }

        line = TextUtils.convertOldJson(line);

        Matcher match = TextKeys.JSON_PATTERN.matcher(line);
        int lastEnd = 0;
        List<BaseComponent> components = new ArrayList<>();

        while (match.find()) {
            final TextComponent comp = toComponent(match.group(7));
            final String[] arguments = match.group(1).split("[|]", 2);

            components.addAll(compList(
                    line.substring(lastEnd, match.start())));

            String[] event1 = arguments[0].split(":\"");
            addEvent(target, comp, event1[0],
                    TextUtils.removeLastChar(event1[1]));

            if (arguments.length == 2) {
                String[] event2 = arguments[1].split(":\"");
                addEvent(target, comp, event2[0],
                        TextUtils.removeLastChar(event2[1]));
            }

            components.add(comp);
            lastEnd = match.end();
        }

        if (lastEnd < (line.length() - 1))
            components.addAll(compList(line.substring(lastEnd)));

        return components.toArray(new BaseComponent[0]);
    }

    /**
     * Converts a line to a {@link BaseComponent} array and use it to send a json message.
     * Uses an input string to apply a click event and a string list to apply a hover event.
     * <p> If click and hover values are null/empty, it will use the {@link TextKeys#JSON_PATTERN}
     * to parse the respective actions and add then in the chat component.
     * <p> After all the actions are set in the chat component, the message is sent to the target player.
     * <pre>{@code
     * // player is the player to parse all respective placeholders and colors
     * JsonConverter json = new JsonConverter(player, "my message");
     * // Send the message to the target using the defined pattern.
     * json.send(target);
     * // Send the message to the target using a hover list and a string action.
     * json.send(target, "RUN:/command", Arrays.asList("My hover message"));
     * }</pre>
     *
     * @param target a target player, can be null
     * @param click the click string
     * @param hover a string list
     */
    public void send(Player target, String click, List<String> hover) {
        if (target == null) target = parser;
        target.spigot().sendMessage(stringToJson(target, click, hover));
    }

    /**
     * Converts a line to a {@link BaseComponent} array and use it to send a json message.
     * Uses an input string to apply a click event and a string list to apply a hover event.
     * <p> Keep in mind that the click string has this format:
     * <pre> {@code
     * Available Actions: RUN, SUGGEST, URL and all ClickAction values.
     * "<ACTION>:<the click string>" -> "RUN:/me click to run"
     * }</pre>
     * <p> If click and hover values are null/empty, it will use the {@link TextKeys#JSON_PATTERN}
     * to parse the respective actions and add then in the chat component.
     * <p></p>
     * <p> After all the actions are set in the chat component, the message is sent to the target player.
     * <pre>{@code
     * // player is the player to parse all respective placeholders and colors
     * JsonConverter json = new JsonConverter(player, "my message");
     * // Send the message to the target using the defined pattern.
     * json.send(target);
     * // Send the message to the target using a hover list and a string action.
     * json.send(target, "RUN:/command", Arrays.asList("My hover message"));
     * }</pre>
     *
     * @param target a target player, can be null
     */
    public void send(Player target) {
        send(target, null, new ArrayList<>());
    }
}
