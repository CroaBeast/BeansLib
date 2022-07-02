package me.croabeast.beanslib.terminals;

import me.croabeast.beanslib.utilities.Exceptions;
import me.croabeast.beanslib.utilities.TextKeys;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

/**
 * The class to handle action bar NMS.
 *
 * @author CroaBeast
 * @since 1.0
 */
public final class ActionBar extends Reflection {

    private final GetActionBar actionBar;

    /**
     * Basic constructor.
     */
    public ActionBar() {
        actionBar = TextKeys.majorVersion() < 11 ? oldActionBar() : newActionBar();
    }

    private interface GetActionBar {
        void send(Player player, String message);
    }

    private GetActionBar oldActionBar() {
        return (player, message) -> {
            try {
                Class<?> chat = getNMSClass("IChatBaseComponent");
                Constructor<?> constructor = getNMSClass("PacketPlayOutChat").getConstructor(chat, byte.class);
                message = "{\"text\":\"" + message + "\"}";

                Object icbc = chat.getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, message),
                        packet = constructor.newInstance(icbc, (byte) 2);
                sendPacket(player, packet);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    private GetActionBar newActionBar() {
        return (player, message) ->
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    /**
     * Send an action bar message.
     * @param player a player
     * @param message a message
     * @throws NullPointerException if player is null
     */
    public void send(Player player, String message) {
        actionBar.send(Exceptions.checkPlayer(player), message);
    }
}
