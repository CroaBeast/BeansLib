package me.croabeast.beanslib.nms;

import lombok.experimental.UtilityClass;
import lombok.var;
import org.bukkit.entity.Player;

import java.util.function.BiFunction;

import static me.croabeast.beanslib.nms.ReflectionUtils.*;

@UtilityClass
public class TitleHandler {

    private enum TitleType {
        TITLE, SUBTITLE, ANIMATE
    }

    int round(int i) {
        return Math.round((float) i / 20);
    }

    interface TimesInitialize {
        Object from(int in, int stay, int out);
    }

    final TimesInitialize TIMES_PACKET_INSTANCE = (in, stay, out) -> {
        try {
            return from(null, "PacketPlayOutTitle").
                    getDeclaredConstructor(int.class, int.class, int.class).
                    newInstance(round(in), round(stay), round(out));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    };

    final BiFunction<Boolean, String, Object> LEGACY_PACKET_INSTANCE = (b, s) -> {
        var type = b ? TitleType.TITLE : TitleType.SUBTITLE;
        var component = COMPONENT_SERIALIZER.apply(s);

        try {
            var oldEnum = from(
                    VERSION < 8.3 ? "PacketPlayOutTitle$" : null,
                    "EnumTitleAction"
            );

            return from(null, "PacketPlayOutTitle").
                    getDeclaredConstructor(oldEnum, BASE_COMP_CLASS).
                    newInstance(
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
}
