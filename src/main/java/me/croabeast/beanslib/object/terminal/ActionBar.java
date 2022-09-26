package me.croabeast.beanslib.object.terminal;

import me.croabeast.beanslib.utility.Exceptions;
import me.croabeast.beanslib.object.key.LibUtils;
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

    private final Handler actionBar;

    /**
     * Basic constructor.
     */
    public ActionBar() {
        actionBar = LibUtils.majorVersion() < 11 ? oldActionBar() : newActionBar();
    }

    private interface Handler {
        void send(Player player, String message);
    }

    private Handler oldActionBar() {
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

    private Handler newActionBar() {
        return (player, message) ->
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    /**
     * Send an action bar message.
     *
     * @param player a player
     * @param message a message
     *
     * @throws NullPointerException if player is null
     */
    public void send(Player player, String message) {
        actionBar.send(Exceptions.checkPlayer(player), message);
    }
}
