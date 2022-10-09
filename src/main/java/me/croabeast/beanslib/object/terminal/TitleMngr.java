package me.croabeast.beanslib.object.terminal;

import me.croabeast.beanslib.utility.Exceptions;
import me.croabeast.beanslib.utility.LibUtils;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

/**
 * The class to handle title NMS.
 *
 * @author CroaBeast
 * @since 1.0
 */
public final class TitleMngr extends Reflection {

    private final Handler title;

    /**
     * Basic constructor.
     */
    public TitleMngr() {
        title = LibUtils.majorVersion() < 10 ? oldTitle() : newTitle();
    }

    private interface Handler {
        void send(Player player, String title, String subtitle, int in, int stay, int out);
    }

    private void legacyMethod(Player player, String message, int in, int stay, int out, boolean isTitle) {
        try {
            Class<?> packetClass = getNMSClass("PacketPlayOutTitle"), chatClass = getNMSClass("IChatBaseComponent"),
                    packetField = packetClass.getDeclaredClasses()[0], chatField = chatClass.getDeclaredClasses()[0];
            
            message = "{\"text\":\"" + message + "\"}";
            
            Object packetTimes = packetField.getField("TIMES").get(null);
            Object chatMessage = chatField.getMethod("a", String.class).invoke(null, message);
            
            Constructor<?> subtitleConstructor = packetClass.getConstructor(packetField, chatClass, int.class, int.class, int.class);
            Object titlePacket = subtitleConstructor.newInstance(packetTimes, chatMessage, in, stay, out);

            sendPacket(player, titlePacket);

            packetTimes = packetField.getField((isTitle ? "" : "SUB") + "TITLE").get(null);
            chatMessage = chatField.getMethod("a", String.class).invoke(null, message);
            
            subtitleConstructor = isTitle ?
                    packetClass.getConstructor(packetField, chatClass) :
                    packetClass.getConstructor(packetField, chatClass, int.class, int.class, int.class);
            
            titlePacket = isTitle ? subtitleConstructor.newInstance(packetTimes, chatMessage) :
                    subtitleConstructor.newInstance(packetTimes, chatMessage, Math.round((float) in / 20),
                            Math.round((float) stay / 20), Math.round((float) out / 20));

            sendPacket(player, titlePacket);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler oldTitle() {
        return (player, title, subtitle, in, stay, out) -> {
            legacyMethod(player, title, in, stay, out, true);
            legacyMethod(player, subtitle, in, stay, out, false);
        };
    }

    private Handler newTitle() {
        return Player::sendTitle;
    }

    /**
     * Send a title message.
     *
     * @param player a player
     * @param title a title
     * @param subtitle a subtitle
     * @param in fade in time in ticks
     * @param stay stay time in ticks
     * @param out fade out time in ticks
     *
     * @throws NullPointerException if player is null
     */
    public void send(Player player, String title, String subtitle, int in, int stay, int out) {
        this.title.send(Exceptions.checkPlayer(player), title, subtitle, in, stay, out);
    }
}
