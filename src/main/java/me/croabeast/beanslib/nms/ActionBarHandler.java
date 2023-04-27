package me.croabeast.beanslib.nms;

import lombok.experimental.UtilityClass;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Function;

import static me.croabeast.beanslib.nms.ReflectHandler.*;

/**
 * This utility class provides methods to send ActionBar messages to players
 * using the Spigot API.
 *
 * <p> It relies on reflection to interact with the net.minecraft and
 * net.minecraft.network packages.
 */
@UtilityClass
public class ActionBarHandler {

    final Class<?> CHAT_PACKET_CLASS = from(
            IS_LEGACY ? null : "network.protocol.game.",
            VERSION >= 19 ?
                    "ClientboundSystemChatPacket" :
                    "PacketPlayOutChat"
    );

    final Function<Class<?>, Object> MESSAGE_TYPE = clazz -> {
        if (clazz == null) return null;
        try {
            return clazz.
                    getDeclaredMethod("a", byte.class).
                    invoke(null, (byte) 2);
        } catch (Exception e) {
            return null;
        }
    };

    final Function<String, Object> PACKET_INSTANCE_FUNCTION = (message) -> {
        if (StringUtils.isBlank(message)) message = "";

        var c = from(IS_LEGACY ? null : "network.chat.", "ChatMessageType");
        var component = COMPONENT_SERIALIZER.apply(message);

        try {
            var holder = new InstanceHolder<>(CHAT_PACKET_CLASS).
                    add(BASE_COMP_CLASS.cast(component)).
                    add(VERSION >= 12 ?
                            (VERSION < 19.0 ? c.cast(MESSAGE_TYPE.apply(c)) : true) :
                            (byte) 2);

            var utils = from(null, "SystemUtils");
            if (utils != null) {
                String field = VERSION == 18.2 ? "c" : "b";

                holder.add(
                        (UUID) utils.getField(field).get(null),
                        VERSION >= 16.0 && VERSION < 19.0
                );
            }

            return holder.getInstance();
        }
        catch (Exception e) {
            return null;
        }
    };

    public boolean send(Player player, String string) {
        try {
            sendPacket(player, PACKET_INSTANCE_FUNCTION.apply(string));
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
