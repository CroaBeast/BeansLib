package me.croabeast.beanslib.nms;

import me.croabeast.beanslib.utility.LibUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * Represents the handler that manages how to send titles between
 * different server versions.
 */
public final class NMSTitle extends Reflection {

    /**
     * The unique instance of the handler.
     */
    public static final NMSTitle INSTANCE = new NMSTitle();

    private static final Class<?> INT_C = int.class;

    private final NMSHandler handler;

    private NMSTitle() {
        handler = LibUtils.majorVersion() < 10 ?
                (p, t, s, i, st, o) -> {
                    createLegacyDisplay(p, t, i, st, o, true);
                    createLegacyDisplay(p, s, i, st, o, false);
                } :
                Player::sendTitle;
    }

    private int round(int i) {
        return Math.round((float) i / 20);
    }

    void createLegacyDisplay(Player player, String message, int in, int stay, int out, boolean isTitle) {
        try {
            Class<?> pClass =  getPacketPlay("Title"), pField = pClass.getDeclaredClasses()[0];

            Object pTimes = pField.getField("TIMES").get(null);
            Object chatMessage = invokeMethod(message);

            Constructor<?> subCons = pClass.getConstructor(pField, getChatClass(), INT_C, INT_C, INT_C);
            Object titlePacket = subCons.newInstance(pTimes, chatMessage, in, stay, out);

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

    @FunctionalInterface
    private interface NMSHandler {
        void send(@NotNull Player player, String title, String subtitle, int in, int stay, int out);
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
