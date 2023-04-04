package me.croabeast.beanslib.nms;

import lombok.var;
import me.croabeast.beanslib.utility.LibUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the handler that manages how to send titles between
 * different server versions.
 *
 * @author CroaBeast
 * @since 1.4
 */
public final class NMSTitle extends Reflection {

    /**
     * The unique instance of the handler.
     */
    public static final NMSTitle INSTANCE = new NMSTitle();

    private static final Class<?> INT_C = int.class;

    private final NMSHandler handler;

    private NMSTitle() {
        handler = LibUtils.getMainVersion() < 10 ?
                (p, t, sub, i, s, o) -> {
                    createLegacyDisplay(p, t, i, s, o, true);
                    createLegacyDisplay(p, sub, i, s, o, false);
                } :
                Player::sendTitle;
    }

    private interface NMSHandler {
        void send(@NotNull Player player, String title, String subtitle, int in, int stay, int out);
    }

    private int round(int i) {
        return Math.round((float) i / 20);
    }

    void createLegacyDisplay(Player player, String message, int in, int stay, int out, boolean isTitle) {
        try {
            var pClass =  getPacketPlay("Title");
            var pField = pClass.getDeclaredClasses()[0];

            var pTimes = pField.getField("TIMES").get(null);
            var chatMessage = invokeMethod(message);

            var subCons = pClass.getConstructor(pField, getChatClass(), INT_C, INT_C, INT_C);
            var titlePacket = subCons.newInstance(pTimes, chatMessage, in, stay, out);

            sendPacket(player, titlePacket);

            pTimes = pField.getField((isTitle ? "" : "SUB") + "TITLE").get(null);
            chatMessage = invokeMethod(message);

            subCons = !isTitle ?
                    pClass.getConstructor(pField, getChatClass(), INT_C, INT_C, INT_C) :
                    pClass.getConstructor(pField, getChatClass());

            titlePacket = !isTitle ?
                    subCons.newInstance(pTimes, chatMessage, round(in), round(stay), round(out)) :
                    subCons.newInstance(pTimes, chatMessage);

            sendPacket(player, titlePacket);
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Sends a title and subtitle to a player, both messages should be formatted before send them.
     *
     * @param player a player
     * @param title a title
     * @param subtitle a subtitle
     * @param in fade in ticks
     * @param stay stay ticks
     * @param out fade out ticks.
     */
    public void send(@NotNull Player player, String title, String subtitle, int in, int stay, int out) {
        handler.send(player, title, subtitle, in, stay, out);
    }
}
