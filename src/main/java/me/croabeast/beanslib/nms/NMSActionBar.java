package me.croabeast.beanslib.nms;

import me.croabeast.beanslib.utility.LibUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.md_5.bungee.api.ChatMessageType.ACTION_BAR;
import static net.md_5.bungee.api.chat.TextComponent.fromLegacyText;

/**
 * Represents the handler that manages how to send action bar
 * messages between different server versions.
 *
 * @author CroaBeast
 * @since 1.4
 */
public final class NMSActionBar extends Reflection {

    /**
     * The unique instance of the handler.
     */
    public static final NMSActionBar INSTANCE = new NMSActionBar();

    private final NMSHandler handler;

    private NMSActionBar() {
        handler = LibUtils.majorVersion() < 11 ?
                (p, s) -> {
                    try {
                        sendPacket(p, getPacketPlay("Chat").
                                getConstructor(getChatClass(), byte.class).
                                newInstance(invokeMethod(s), (byte) 2));
                    }
                    catch (Exception e) { e.printStackTrace(); }
                } :
                (p, s) -> p.spigot().sendMessage(ACTION_BAR, fromLegacyText(s));
    }

    private interface NMSHandler {
        void send(@NotNull Player player, String message);
    }

    /**
     * Sends an action bar message, the message should be formatted before send them.
     *
     * @param player a player
     * @param message a message, can not be empty
     */
    public void send(@NotNull Player player, String message) {
        if (StringUtils.isNotBlank(message)) handler.send(player, message);
    }
}
