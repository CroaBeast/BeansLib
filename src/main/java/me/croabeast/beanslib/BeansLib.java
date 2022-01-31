package me.croabeast.beanslib;

import org.bukkit.plugin.java.JavaPlugin;

public class BeansLib {

    private static JavaPlugin plugin;

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static void setPlugin(final JavaPlugin plugin) {
        BeansLib.plugin = plugin;
    }

}
