package me.croabeast.beanslib.nms;

import lombok.experimental.UtilityClass;
import lombok.var;
import me.croabeast.beanslib.utility.LibUtils;
import org.bukkit.entity.Player;

import java.util.function.Function;

@UtilityClass
class ReflectionUtils {

    final double VERSION = LibUtils.getMainVersion();
    final boolean IS_LEGACY = VERSION < 17.0;

    final Class<?> BASE_COMP_CLASS = from(
            IS_LEGACY ? null : "network.chat.", "IChatBaseComponent");

    Class<?> from(String prefix, String name) {
        var builder = new StringBuilder("net.minecraft.");

        if (IS_LEGACY)
            builder.append("server.").
                    append(LibUtils.getBukkitVersion()).
                    append(".");

        if (prefix != null) builder.append(prefix);

        try {
            return Class.forName(builder.append(name) + "");
        } catch (Exception e) {
            return null;
        }
    }

    final Function<String, Object> COMPONENT_SERIALIZER = message -> {
        try {
            var serializer = from(
                    IS_LEGACY ? null : ("network.chat.IChatBaseComponent$"),
                    "ChatSerializer");

            return serializer.getDeclaredMethod("a", String.class).
                    invoke(null, "{\"text\":\"" + message + "\"}");
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    };

    void sendPacket(Player p, Object o) throws Exception {
        var clazz = from(
                IS_LEGACY ? null : "network.protocol.",
                "Packet"
        );

        var handler = p.getClass().getMethod("getHandle").invoke(p);
        var co = VERSION >= 20.0 ? "c" : "b";

        var connect = handler.getClass().
                getField(IS_LEGACY ? "playerConnection" : co).
                get(handler);

        connect.getClass().
                getMethod(VERSION < 18.0 ? "sendPacket" : "a", clazz).
                invoke(connect, o);
    }
}
