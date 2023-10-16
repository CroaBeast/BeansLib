package me.croabeast.beanslib.nms;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * A utility class that handles sending action bar messages to players.
 * It uses reflection to support different versions of Minecraft.
 */
@UtilityClass
public class ActionBarHandler {

    final Class<?> LEGACY_CHAT_CLASS = ReflectionUtils.from(null, "PacketPlayOutChat");

    final Function<String, Object> STRING_FUNCTION = (s) -> {
        Class<?> legacyClass = ReflectionUtils.from(null, "IChatBaseComponent");
        legacyClass = legacyClass.getDeclaredClasses()[0];

        try {
            Method method = legacyClass.getMethod("a", String.class);
            return method.invoke(null, "{\"text\":\"" + s + "\"}");
        } catch (Exception e) {
            return null;
        }
    };

    /**
     * Sends an action bar message to a player.
     *
     * @param player The player to send the message to.
     * @param string The message to send.
     *
     * @return True if the message was sent successfully, false otherwise.
     */
    public boolean send(Player player, String string) {
        if (ReflectionUtils.VERSION < 11.0) {
            try {
                Method method = player.getClass().getMethod("getHandle");
                Object handle = method.invoke(player);

                method = handle.getClass().getMethod("playerConnection");
                handle = method.invoke(handle);

                method = handle.getClass().getMethod("sendPacket", LEGACY_CHAT_CLASS);
                method.invoke(handle, STRING_FUNCTION.apply(string));
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(string)
        );
        return true;
    }
}
