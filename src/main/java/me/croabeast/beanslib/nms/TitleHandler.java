package me.croabeast.beanslib.nms;

import lombok.experimental.UtilityClass;
import lombok.var;
import org.bukkit.entity.Player;

import java.util.function.BiFunction;

import static me.croabeast.beanslib.nms.ReflectHandler.*;

@UtilityClass
public class TitleHandler {

    Class<?> getTitlePacket(TitleType type) {
        if (IS_LEGACY) return from(null, "PacketPlayOutTitle");

        String name = "ClientboundSet";
        switch (type) {
            case SUBTITLE:
                name += "SubtitleTextPacket";
                break;
            case TIMES:
                name += "TitlesAnimationPacket";
                break;
            case TITLE: default:
                name += "TitleTextPacket";
                break;
        }

        return from("network.protocol.game.", name);
    }

    private enum TitleType {
        TITLE, SUBTITLE, TIMES
    }

    int round(int i) {
        return Math.round((float) i / 20);
    }

    interface TimesInitialize {
        Object from(int in, int stay, int out);
    }

    final TimesInitialize TIMES_PACKET_INSTANCE = (in, stay, out) -> {
        var clazz = getTitlePacket(TitleType.TIMES);
        if (clazz == null) return null;

        try {
            return clazz.
                    getDeclaredConstructor(int.class, int.class, int.class).
                    newInstance(round(in), round(stay), round(out));
        } catch (Exception e) {
            return null;
        }
    };

    @SuppressWarnings("all")
    final BiFunction<Boolean, String, Object> TEXT_PACKET_INSTANCE = (isTitle, message) -> {
        var type = isTitle ? TitleType.TIMES : TitleType.SUBTITLE;
        var component = COMPONENT_SERIALIZER.apply(message);

        try {
            var holder = new InstanceHolder<>(getTitlePacket(type));
            var oldEnum = from(
                    VERSION < 8.3 ? "PacketPlayOutTitle$" : null,
                    "EnumTitleAction"
            );

            return holder.add(IS_LEGACY ?
                            Enum.valueOf((Class<Enum>) oldEnum, type + "") :
                            BASE_COMP_CLASS.cast(component)
                    ).
                    add(BASE_COMP_CLASS.cast(component), IS_LEGACY).
                    getInstance();
        } catch (Exception e) {
            return null;
        }
    };

    public boolean send(Player player, String title, String subtitle, int in, int stay, int out) {
        try {
            sendPacket(player, TIMES_PACKET_INSTANCE.from(in, stay, out));
            sendPacket(player, TEXT_PACKET_INSTANCE.apply(true, title));
            sendPacket(player, TEXT_PACKET_INSTANCE.apply(false, subtitle));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
