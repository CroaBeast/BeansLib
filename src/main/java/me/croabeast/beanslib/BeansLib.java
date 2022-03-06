package me.croabeast.beanslib;

import me.croabeast.beanslib.utilities.TextUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class BeansLib {

    private static JavaPlugin plugin;

    @NotNull
    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static void init(final @NotNull JavaPlugin plugin) {
        BeansLib.plugin = plugin;
        new TextUtils();
    }
}
