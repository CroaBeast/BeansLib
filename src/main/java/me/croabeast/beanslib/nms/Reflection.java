package me.croabeast.beanslib.nms;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

class Reflection {

    Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().
                getName().split("\\.")[3];

        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (Exception e) {
            return null;
        }
    }

    Class<?> getChatClass() {
        return getNMSClass("IChatBaseComponent");
    }

    Class<?> getPacketPlay(String name) {
        return getNMSClass("PacketPlayOut" + name);
    }

    Object invokeMethod(String message) throws Exception {
        return getChatClass().getDeclaredClasses()[0].
                getMethod("a", String.class).
                invoke(null, "{\"text\":\"" + message + "\"}");
    }

    void sendPacket(Player player, Object packet) {
        try {
            Object handler  = player.getClass().getMethod("getHandle").invoke(player);
            Object connect = handler.getClass().getField("playerConnection").get(handler);

            connect.getClass().getMethod("sendPacket",
                    getNMSClass("Packet")).invoke(connect, packet);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
