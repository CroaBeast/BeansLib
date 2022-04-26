package me.croabeast.beanslib;

import me.croabeast.beanslib.utilities.TextUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class BeansLib extends TextUtils {

    /**
     * The plugin instance.
     */
    private static JavaPlugin instance;

    /**
     * Only constructor to initialize the integration of BeansLib
     * @param plugin plugin's instance
     */
    public BeansLib(@NotNull JavaPlugin plugin) {
        instance = plugin;
    }

    /**
     * Gets your plugin's instance when initializing the class.
     * @return plugin's instance
     */
    @NotNull
    public static JavaPlugin getPlugin() {
        return instance;
    }
}
