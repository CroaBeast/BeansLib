package me.croabeast.beanslib.nms;

import lombok.experimental.UtilityClass;
import lombok.var;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
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
            e.printStackTrace();
            return null;
        }
    };

    final Function<String, Object> PACKET_INSTANCE_FUNCTION = (message) -> {
        if (StringUtils.isBlank(message)) message = "";

        var c = from(IS_LEGACY ? null : "network.chat.", "ChatMessageType");
        var comps = TextComponent.fromLegacyText(message);

        try {
            List<Class<?>> classes = new ArrayList<>();
            List<Object> objects = new ArrayList<>();

            classes.add(VERSION < 19.0 ? BASE_COMP_CLASS : BaseComponent[].class);
            objects.add(VERSION < 19.0 ? null : comps);

            classes.add(VERSION >= 12 ?
                    (VERSION < 19.0 ? c :
                            (VERSION < 19.3 ? int.class : boolean.class)) :
                    byte.class);
            objects.add(VERSION >= 12 ?
                    (VERSION < 19.0 ? MESSAGE_TYPE.apply(c) :
                            (VERSION < 19.3 ? 2 : true)) :
                    (byte) 2);

            if (VERSION >= 16.0 && VERSION < 19.0) {
                classes.add(UUID.class);

                var utils = from(null, "SystemUtils");
                if (utils != null) {
                    String field = VERSION == 18.2 ? "c" : "b";
                    objects.add(utils.getField(field).get(null));
                }
            }

            var packet = CHAT_PACKET_CLASS.
                    getDeclaredConstructor(classes.toArray(new Class<?>[0])).
                    newInstance(objects.toArray(new Object[0]));

            if (VERSION < 19.0) {
                var field = packet.getClass().getDeclaredField("components");
                field.set(packet, comps);
            }

            return packet;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    };

    public boolean send(Player player, String string) {
        try {
            sendPacket(player, PACKET_INSTANCE_FUNCTION.apply(string));
            // player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(string));
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
