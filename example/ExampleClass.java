package example;

import me.croabeast.beanslib.utilities.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ExampleClass {

    /*
     * If you want to use non-static method to get BeansLib methods.
     */

    private final MyPlugin plugin;

    public ExampleClass(MyPlugin plugin) {
        this.plugin = plugin;
    }

    public void doSomething() {
        Player player = Bukkit.getPlayer("Mark");
        plugin.getMyTextClass().sendMessage(player, "<#F0F12A>a cool <#12AAE1>chat message</#07DD01>");
        TextUtils.sendActionBar(player, "<R:1>my rainbow action bar message</R>");
    }

    /*
     * If you want to use static method to get BeansLib methods.
     */

    public void doStatic() {
        MyPlugin.getStaticMyTextClass().doLog(
                "this is some console logs",
                "a lot of multiple lines"
        );
    }
}
