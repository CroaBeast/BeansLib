package me.croabeast.beanslib.message;

import org.apache.commons.lang.StringUtils;

import java.util.Locale;

/**
 * Represents a flag that can be allowed to be sent to a player.
 */
public enum MessageFlag {
    /**
     * This flag allows to display action bar messages.
     */
    ACTION_BAR,
    /**
     * This flag allows to send chat messages.
     */
    CHAT,
    /**
     * This flag allows to display bossbar messages.
     */
    BOSSBAR,
    /**
     * This flag allows to send vanilla JSON messages.
     */
    JSON,
    /**
     * This flag allows to send webhooks.
     */
    WEBHOOK,
    /**
     * This flag allows to display title messages.
     */
    TITLE;

    public String getName() {
        return name().toLowerCase(Locale.ENGLISH).replace('_', '-');
    }

    public static MessageFlag from(String string) {
        if (StringUtils.isBlank(string))
            return CHAT;

        for (MessageFlag flag : values())
            if (string.matches("(?i)" + flag.getName()))
                return flag;

        return CHAT;
    }
}
