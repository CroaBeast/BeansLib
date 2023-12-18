package me.croabeast.beanslib.message;

import org.apache.commons.lang.StringUtils;

import java.util.Locale;

/**
 * An enum that represents different types of messages that can be sent to players.
 */
public enum MessageFlag {
    /**
     * A message that is displayed in the action bar, which is the area above the hot bar.
     */
    ACTION_BAR,
    /**
     * A message that is displayed in the chat window, which is the default type of message.
     */
    CHAT,
    /**
     * A message that is displayed in the boss bar, which is the area at the top of the
     * screen that shows the health of a boss entity.
     */
    BOSSBAR,
    /**
     * A message that is formatted as a JSON object, which allows for advanced customization
     * of the text, such as color, style, and click events.
     */
    JSON,
    /**
     * A message that is sent to a webhook, which is a URL that can receive and process data
     * from other sources.
     */
    WEBHOOK,
    /**
     * A message that is displayed as a title, which is a large text that appears in the center
     * of the screen for a short duration.
     */
    TITLE;

    /**
     * Returns the name of the message flag in lowercase and with dashes instead of underscores.
     * For example, ACTION_BAR becomes action-bar.
     *
     * @return the name of the message flag
     */
    public String getName() {
        return name().toLowerCase(Locale.ENGLISH).replace('_', '-');
    }

    /**
     * Returns the message flag that corresponds to the given string, or CHAT if the string is blank
     * or does not match any flag.
     *
     * <p> The string is case-insensitive and can have dashes or underscores.
     * For example, action-bar, ACTION_BAR, and action_bar all return ACTION_BAR.
     *
     * @param string the string to convert to a message flag
     * @return the message flag that matches the string, or CHAT if none
     */
    public static MessageFlag from(String string) {
        if (StringUtils.isBlank(string))
            return CHAT;

        for (MessageFlag flag : values())
            if (string.matches("(?i)" + flag.getName()))
                return flag;

        return CHAT;
    }
}
