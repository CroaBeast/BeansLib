package example;

import me.croabeast.beanslib.BeansLib;
import org.jetbrains.annotations.NotNull;

public class MyTextClass extends BeansLib {

    /*
     * You need to initialize your BeansLib class with your plugin's instance.
     */

    private final MyPlugin myPlugin;

    public MyTextClass(MyPlugin plugin) {
        super(plugin);
        this.myPlugin = plugin;
    }

    /*
     * You can also override other methods if you want it.
     */

    @Override
    public @NotNull String langPrefixKey() {
        return myPlugin.getConfig().getString("lang.prefix-key", "<key>");
    }

    @Override
    public @NotNull String langPrefix() {
        return myPlugin.getConfig().getString("lang.main-prefix", " My Plugin owo");
    }

    @Override
    public boolean isHardSpacing() {
        return myPlugin.getConfig().getBoolean("options.hard-spacing");
    }

    @Override
    public boolean isStripPrefix() {
        return myPlugin.getConfig().getBoolean("options.strip-prefix");
    }
}
