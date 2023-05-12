package me.croabeast.beanslib.builder;

import com.google.common.collect.Lists;
import lombok.var;
import net.md_5.bungee.api.chat.ClickEvent;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * An enumeration of supported click types for clickable text events
 * in Minecraft chat messages.
 *
 * @since 1.4
 * @author CroaBeast
 */
public enum ClickType {

    /**
     * A click event that runs a command when clicked.
     */
    RUN_CMD(ClickEvent.Action.RUN_COMMAND, "click", "run"),

    /**
     * A click event that opens a URL when clicked.
     */
    OPEN_URL(ClickEvent.Action.OPEN_URL, "url"),

    /**
     * A click event that opens a file when clicked.
     */
    OPEN_FILE(ClickEvent.Action.OPEN_FILE, "file"),

    /**
     * A click event that suggests a command when clicked.
     */
    SUGGEST_CMD(ClickEvent.Action.SUGGEST_COMMAND, "suggest"),

    /**
     * A click event that changes the page when clicked.
     */
    CHANGE_PAGE(ClickEvent.Action.CHANGE_PAGE),

    /**
     * A click event that copies text to the clipboard when clicked.
     */
    CLIPBOARD(ClickEvent.Action.COPY_TO_CLIPBOARD);

    private final ClickEvent.Action bukkit;
    private final List<String> names = new ArrayList<>();

    ClickType(ClickEvent.Action bukkit, String... extras) {
        this.bukkit = bukkit;
        names.add(name().toLowerCase(Locale.ENGLISH));

        if (extras != null && extras.length > 0)
            names.addAll(Lists.newArrayList(extras));
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
     * Returns the ClickType that corresponds to the specified name, or {@link #SUGGEST_CMD}
     * if the name is blank or not recognized.
     *
     * @param name the name of the ClickType to retrieve
     * @return the respective ClickType instance
     */
    public static ClickType fromString(String name) {
        if (StringUtils.isBlank(name)) return SUGGEST_CMD;

        for (var type : values()) {
            String temp = name.toLowerCase(Locale.ENGLISH);
            if (type.names.contains(temp)) return type;
        }

        return SUGGEST_CMD;
    }
}
