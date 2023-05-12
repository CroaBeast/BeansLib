package me.croabeast.beanslib.builder;

import lombok.var;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.utility.Exceptions;
import me.croabeast.beanslib.utility.TextUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * The object class to handle JSON messages to players in chat.
 *
 * <p> Uses an input string to apply a click event and a string list
 * to apply a hover event.
 *
 * <p> If click and hover values are null/empty, it will use the
 * {@link TextUtils#FORMATTED_CHAT_PATTERN} to parse the respective actions and
 * add then in the chat component.
 *
 * <p> After all the actions are set in the chat component, the
 * message is sent to the target player.
 *
 * @author CroaBeast
 * @since 1.3
 */
@Deprecated
public class JsonBuilder {

    private final ChatMessageBuilder builder;

    /**
     * Converts a {@link String} line to a {@code JsonBuilder} object.
     * <p> Parses all the required keys from {@link BeansLib} and formats colors.
     *
     * @param target a target to send a message, can be null
     * @param parser a parser to parse placeholders
     * @param string an input string
     */
    public JsonBuilder(Player target, Player parser, String string) {
        builder = new ChatMessageBuilder(target, parser, string);
    }

    /**
     * Sends the {@code JsonBuilder} object to the target player,
     * if not defined will be to the parser player.
     *
     * @return true if the builder was sent, false otherwise
     */
    public boolean send() {
        return builder.send();
    }

    /**
     * Converts a line to a {@link BaseComponent} array using the
     * {@link TextUtils#FORMATTED_CHAT_PATTERN}.
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
        return new JsonBuilder(null, parser, text).builder.build();
    }

    /**
     * Converts a line to a {@link BaseComponent} array using the
     * {@link TextUtils#FORMATTED_CHAT_PATTERN}.
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
