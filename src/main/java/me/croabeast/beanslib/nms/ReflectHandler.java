package me.croabeast.beanslib.nms;

import lombok.experimental.UtilityClass;
import lombok.var;
import me.croabeast.beanslib.utility.LibUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@UtilityClass
class ReflectHandler {

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
            return null;
        }
    };

    void sendPacket(Player p, Object o) throws Exception {
        var clazz = from(
                IS_LEGACY ? null : "network.protocol.",
                "Packet"
        );

        var handler = p.getClass().getMethod("getHandle").invoke(p);
        var connect = handler.getClass().
                getField(IS_LEGACY ? "playerConnection" : "b").
                get(handler);

        connect.getClass().
                getMethod(VERSION < 18.0 ? "sendPacket" : "a", clazz).
                invoke(connect, clazz.cast(o));
    }

    static class InstanceHolder<I> {

        private final Class<I> clazz;
        private final List<Object> objects = new ArrayList<>();

        InstanceHolder(Class<I> clazz) {
            this.clazz = clazz;
        }

        <T> InstanceHolder<I> add(T t, boolean isValid) {
            if (isValid && t != null) objects.add(t);
            return this;
        }

        <T> InstanceHolder<I> add(T t) {
            return add(t, true);
        }

         I getInstance() throws Exception {
            if (objects.size() == 0)
                return clazz.getDeclaredConstructor().newInstance();

            Class<?>[] cs = new Class<?>[objects.size()];
            Object[] objs = new Object[objects.size()];

            for (int i = 0; i < objects.size(); i++) {
                objs[i] = objects.get(i);
                cs[i] = objects.get(i).getClass();
            }

            return clazz.getDeclaredConstructor(cs).newInstance(objs);
        }
    }
}
