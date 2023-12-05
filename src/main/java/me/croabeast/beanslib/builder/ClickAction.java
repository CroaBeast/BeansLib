package me.croabeast.beanslib.builder;

import lombok.var;
import me.croabeast.beanslib.utility.ArrayUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * An enum that represents different types of click actions for chat components.
 *
 * <p> Each type has a corresponding {@link ClickEvent.Action} from the Bukkit API
 * and a list of names that can be used to identify it.
 */
public enum ClickAction {

    /**
     * A click action that runs a command as the player who clicked.
     */
    RUN_CMD(ClickEvent.Action.RUN_COMMAND, "click", "run"),

    /**
     * A click action that opens a URL in the player's browser.
     */
    OPEN_URL(ClickEvent.Action.OPEN_URL, "url"),

    /**
     * A click action that opens a file on the player's computer.
     */
    OPEN_FILE(ClickEvent.Action.OPEN_FILE, "file"),

    /**
     * A click action that suggests a command in the player's chat input.
     */
    SUGGEST_CMD(ClickEvent.Action.SUGGEST_COMMAND, "suggest"),

    /**
     * A click action that changes the page of a book.
     */
    CHANGE_PAGE(ClickEvent.Action.CHANGE_PAGE),

    /**
     * A click action that copies a text to the player's clipboard.
     */
    CLIPBOARD(ClickEvent.Action.COPY_TO_CLIPBOARD);

    private final ClickEvent.Action bukkit;
    private final List<String> names = new LinkedList<>();

    ClickAction(ClickEvent.Action bukkit, String... extras) {
        this.bukkit = bukkit;
        names.add(name().toLowerCase(Locale.ENGLISH));

        if (!ArrayUtils.isArrayEmpty(extras))
            names.addAll(ArrayUtils.toList(extras));
    }

    /**
     * Returns the Bukkit click action associated with this click type.
     *
     * @return the Bukkit click action
     */
    public ClickEvent.Action asBukkit() {
        return bukkit;
    }

    /**
     * Returns the first name associated with this click type.
     *
     * @return the first name
     */
    @Override
    public String toString() {
        return names.get(0);
    }

    /**
     * Returns the ClickAction that corresponds to the specified name, or {@link #SUGGEST_CMD}
     * if the name is blank or not recognized.
     *
     * @param name the name of the ClickAction to retrieve
     * @return the respective ClickAction instance
     */
    public static ClickAction fromString(String name) {
        if (StringUtils.isBlank(name)) return SUGGEST_CMD;

        for (var type : values()) {
            String temp = name.toLowerCase(Locale.ENGLISH);
            if (type.names.contains(temp)) return type;
        }

        return SUGGEST_CMD;
    }
}
