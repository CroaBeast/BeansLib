package me.croabeast.beanslib.message;

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
}
