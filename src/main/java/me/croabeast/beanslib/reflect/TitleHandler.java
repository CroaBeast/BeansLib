package me.croabeast.beanslib.reflect;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

import java.util.function.BiFunction;

import static me.croabeast.beanslib.reflect.ReflectionUtils.*;

@UtilityClass
public class TitleHandler {

    int round(int i) {
        return Math.round((float) i / 20);
    }

    interface TimesInitialize {
        Object from(int in, int stay, int out);
    }

    private enum TitleType {
        TITLE, SUBTITLE
    }

    final TimesInitialize TIMES_PACKET_INSTANCE = (in, stay, out) -> {
        try {
            return from(null, "PacketPlayOutTitle")
                    .getDeclaredConstructor(int.class, int.class, int.class)
                    .newInstance(round(in), round(stay), round(out));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    };

    final BiFunction<Boolean, String, Object> LEGACY_PACKET_INSTANCE = (b, s) -> {
        TitleType type = b ? TitleType.TITLE : TitleType.SUBTITLE;
        Object component = COMPONENT_SERIALIZER.apply(s);

        try {
            Class<?> oldEnum = from(
                    VERSION < 8.3 ? "PacketPlayOutTitle$" : null,
                    "EnumTitleAction"
            );

            return from(null, "PacketPlayOutTitle")
                    .getDeclaredConstructor(oldEnum, BASE_COMP_CLASS)
                    .newInstance(
                            oldEnum.getField(type + "").get(null),
                            component
                    );
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    };

    public boolean send(Player player, String title, String subtitle, int in, int stay, int out) {
        if (VERSION >= 11.0) {
            player.sendTitle(title, subtitle, in, stay, out);
            return true;
        }

        try {
            sendPacket(player, TIMES_PACKET_INSTANCE.from(in, stay, out));
            sendPacket(player, LEGACY_PACKET_INSTANCE.apply(true, title));
            sendPacket(player, LEGACY_PACKET_INSTANCE.apply(false, subtitle));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean send(Player player, String title, int in, int stay, int out) {
        return send(player, title, "", in, stay, out);
    }
}
