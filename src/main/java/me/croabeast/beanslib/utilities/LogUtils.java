package me.croabeast.beanslib.utilities;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

import static me.croabeast.beanslib.utilities.TextUtils.*;
import static me.croabeast.iridiumapi.IridiumAPI.*;

public abstract class LogUtils {

    private static final boolean COLOR_SUPPORT =
            MC_VERSION >= 12 && !MC_FORK.split(" ")[0].matches("(?i)Spigot");

    private static String colorize(String line) {
        return COLOR_SUPPORT ? process(line) : stripAll(line);
    }

    public static void playerLog(Player player, String... lines) {
        for (String s : lines) if (s != null) player.sendMessage(process(s));
    }

    public static void rawLog(String... lines) {
        for (String s : lines) if (s != null) Bukkit.getServer().getLogger().info(colorize(s));
    }

    public static void doLog(CommandSender sender, String... lines) {
        if (sender instanceof Player) playerLog((Player) sender, lines);
        for (String s : lines) if (s != null) Bukkit.getLogger().info(colorize(s));
    }

    public static void doLog(String... lines) {
        doLog(null, lines);
    }
}
