package example;

import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {

    /*
     * If you want to initialize the class in a non-static way.
     */
    private MyTextClass myTextClass;

    /*
     * If you want to use static to initialize the class.
     */
    private static MyTextClass staticMyTextClass;

    /*
     * Initializing your BeansLib class in your onEnable method.
     */
    @Override
    public void onEnable() {
        myTextClass = new MyTextClass(this);
        staticMyTextClass = new MyTextClass(this);
        myTextClass.doLog("hello world");
    }

    @Override
    public void onDisable() {}

    /*
     * Create a getter for your BeansLib class.
     */

    public MyTextClass getMyTextClass() {
        return myTextClass;
    }

    public static MyTextClass getStaticMyTextClass() {
        return staticMyTextClass;
    }
}
