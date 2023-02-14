package example;

import me.croabeast.beanslib.BeansLib;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class MyTextClass extends BeansLib {

    /*
     * You need to initialize your BeansLib class with your plugin's instance.
     */

    private final MyPlugin myPlugin;

    public MyTextClass(MyPlugin plugin) {
        this.myPlugin = plugin;
    }

    /*
     * You can also override other methods if you want it.
     */

    @Override
    public @NotNull Plugin getPlugin() {
        return myPlugin;
    }

    @Override
    public @NotNull String langPrefixKey() {
        return myPlugin.getConfig().getString("lang.prefix-key", "<key>");
    }

    @Override
    public @NotNull String langPrefix() {
        return myPlugin.getConfig().getString("lang.main-prefix", " My Plugin owo");
    }

    @Override
    public boolean fixColorLogger() {
        return myPlugin.getConfig().getBoolean("options.fix-color-logger");
    }
}
