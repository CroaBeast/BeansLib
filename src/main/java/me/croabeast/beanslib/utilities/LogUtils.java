package me.croabeast.beanslib.utilities;

import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.terminals.JsonMsg;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.croabeast.beanslib.utilities.TextUtils.*;
import static me.croabeast.iridiumapi.IridiumAPI.*;

public abstract class LogUtils {

    private static final boolean COLOR_SUPPORT =
            MAJOR_VERSION >= 12 && !MC_FORK.split(" ")[0].matches("(?i)Spigot");

    private static String colorize(@NotNull String line) {
        return JsonMsg.stripJson(COLOR_SUPPORT ? process(line) : stripAll(line));
    }

    public static void playerLog(@NotNull Player player, String... lines) {
        for (String s : lines) if (s != null)
            player.sendMessage(process(s.replace(getLangPrefixKey(), getLangPrefix())));
    }

    public static void rawLog(String... lines) {
        for (String s : lines) if (s != null) Bukkit.getServer().getLogger().info(colorize(s));
    }

    public static void doLog(@Nullable CommandSender sender, String... lines) {
        if (sender instanceof Player) playerLog((Player) sender, lines);
        for (String s : lines) if (s != null)
            BeansLib.getPlugin().getLogger().info(colorize(s.replace(getLangPrefixKey(), "")));
    }

    public static void doLog(String... lines) {
        doLog(null, lines);
    }
}
