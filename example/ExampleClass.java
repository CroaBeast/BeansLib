package example;

import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.beanslib.message.MessageSender;
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
        new MessageSender(Bukkit.getPlayer("Mark")).send(
                "[C] <#D1FF0A>This chat message will be centered and colored</#>",
                "[TITLE:3] A good title message<n>Send by: {player}",
                "[JSON] {\"text\":\"Click Me!\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/ah\"}}"
                "%bossbar:custom-bossbar%"
                "[ACTION-BAR] &7Action bar message in &e{playerX}, {playerY}, {playerZ}"
        );
    }

    /*
     * If you want to use static method to get BeansLib methods.
     */

    public void doStatic() {
        MyPlugin.getStaticMyTextClass().doLog("this is some console logs", "a lot of multiple lines");

        Player player = Bukkit.getPlayer("Mark");
        TextUtils.sendActionBar(player, "<R:1>my rainbow action bar message</R>");
    }
}
