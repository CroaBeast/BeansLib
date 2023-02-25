package me.croabeast.beanslib.message;

import org.bukkit.entity.Player;

/**
 * Represents a defined action that will be executed using an input
 * string and a player to format that string.
 *
 * @author CroaBeast
 * @since 1.4
 */
interface MessageAction {

    /**
     * Executes the defined action of this representation.
     *
     * @param target a target player
     * @param parser a player to parse arguments
     * @param string an input string
     *
     * @return true if was executed, false otherwise
     */
    boolean execute(Player target, Player parser, String string);

    /**
     * Executes the defined action of this representation.
     *
     * @param parser a player to parse arguments
     * @param string an input string
     *
     * @return true if was executed, false otherwise
     */
    default boolean execute(Player parser, String string) {
        return execute(parser, parser, string);
    }
}
