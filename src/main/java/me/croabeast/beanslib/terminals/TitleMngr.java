package me.croabeast.beanslib.terminals;

import me.croabeast.beanslib.utilities.TextKeys;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

public class TitleMngr implements Reflection {

    private final GetTitle title;

    private int in;
    private int stay;
    private int out;

    public TitleMngr() {
        title = TextKeys.majorVersion() < 10 ? oldTitle() : newTitle();
    }

    public interface GetTitle {
        void send(Player player, String title, String subtitle, int in, int stay, int out);
    }

    public GetTitle getMethod() {
        return title;
    }

    private void legacyMethod(Player player, String message, boolean isTitle) {
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

    private GetTitle oldTitle() {
        return (player, title, subtitle, in, stay, out) -> {
            this.in = in;
            this.stay = stay;
            this.out = out;
            legacyMethod(player, title, true);
            legacyMethod(player, subtitle, false);
        };
    }

    public GetTitle newTitle() {
        return Player::sendTitle;
    }
}
