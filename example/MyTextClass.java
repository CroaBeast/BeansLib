package example;

import me.croabeast.beanslib.BeansLib;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class MyTextClass extends BeansLib {

    public MyTextClass(MyPlugin plugin) {
        super(plugin);
    }

    /*
     * You can also override other methods if you want it.
     */

    @Override
    public @NotNull String getLangPrefixKey() {
        return getPlugin().getConfig().getString("lang.prefix-key", "<key>");
    }

    @Override
    public @NotNull String getLangPrefix() {
        return getPlugin().getConfig().getString("lang.main-prefix", " My Plugin owo");
    }
}
