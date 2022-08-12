package me.croabeast.beanslib.terminal;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

abstract class Reflection {

    Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    void sendPacket(Player player, Object packet) {
        try {
            Object handler  = player.getClass().getMethod("getHandle").invoke(player),
                    connect = handler.getClass().getField("playerConnection").get(handler);
            connect.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connect, packet);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
