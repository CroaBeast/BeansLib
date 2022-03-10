package me.croabeast.beanslib.utilities;

import com.google.common.collect.*;
import me.clip.placeholderapi.*;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.terminals.*;
import me.croabeast.iridiumapi.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.*;
import org.bukkit.scheduler.*;
import org.jetbrains.annotations.*;

import java.sql.Time;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

    private static final JavaPlugin main = BeansLib.getPlugin();

    private static ActionBar actionBar;
    private static TitleMngr titleMngr;

    private static final String MC_VERSION = Bukkit.getBukkitVersion().split("-")[0];

    public static final String MC_FORK = Bukkit.getVersion().split("-")[1] + " " + MC_VERSION;
    public static final int MAJOR_VERSION = Integer.parseInt(MC_VERSION.split("\\.")[1]);

    private static String centerPrefix, lineSplitter, langPrefix, langPrefixKey,
            actionBarKey, titleKey, jsonKey, playerKey, playerWorldKey, charPattern;

    private static boolean isHardSpacing;

    public TextUtils() {
        actionBar = new ActionBar();
        titleMngr = new TitleMngr();
        loadTextValues();
    }

    public static void setCenterPrefix(String centerPrefix) {
        TextUtils.centerPrefix = centerPrefix;
    }
    public static void setLineSplitter(String lineSplitter) {
        TextUtils.lineSplitter = lineSplitter;
    }
    public static void setLangPrefix(String langPrefix) {
        TextUtils.langPrefix = langPrefix;
    }
    public static void setLangPrefixKey(String langPrefixKey) {
        TextUtils.langPrefixKey = langPrefixKey;
    }
    public static void setActionBarKey(String actionBarKey) {
        TextUtils.actionBarKey = actionBarKey;
    }
    public static void setTitleKey(String titleKey) {
        TextUtils.titleKey = titleKey;
    }
    public static void setJsonKey(String jsonKey) {
        TextUtils.jsonKey = jsonKey;
    }
    public static void setPlayerKey(String playerKey) {
        TextUtils.playerKey = playerKey;
    }
    public static void setPlayerWorldKey(String playerWorldKey) {
        TextUtils.playerWorldKey = playerWorldKey;
    }
    public static void setCharPattern(String charPattern) {
        TextUtils.charPattern = charPattern;
    }
    public static void setHardSpacing(boolean isHardSpacing) {
        TextUtils.isHardSpacing = isHardSpacing;
    }

    public void loadTextValues() {
        centerPrefix = "<C>";
        lineSplitter = Pattern.quote("<n>");
        langPrefix = "&e&l MY-PLUGIN &8> ";
        langPrefixKey = "<P>";

        actionBarKey = "[ACTION-BAR]";
        titleKey = "[TITLE]";
        jsonKey = "[JSON]";
        playerKey = "{player}";
        playerWorldKey = "{world}";

        isHardSpacing = true;
        charPattern = "<U:([a-fA-F0-9]{4})>";
    }

    public static String parsePAPI(@Nullable Player player, String message) {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")
                ? PlaceholderAPI.setPlaceholders(player, message) : message;
    }

    public static String parseChars(String line) {
        Pattern charPattern = Pattern.compile(getCharPattern());
        Matcher match = charPattern.matcher(line);

        while (match.find()) {
            char s = (char) Integer.parseInt(match.group(1), 16);
            line = line.replace(match.group(), s + "");
        }
        return line;
    }

    public static String colorize(Player player, String message) {
        return IridiumAPI.process(parsePAPI(player, parseChars(message)));
    }

    public static List<String> fileList(ConfigurationSection file, String path) {
        return  !file.isList(path) ?
                Lists.newArrayList(file.getString(path)) :
                file.getStringList(path);
    }

    public static String removeSpace(String line) {
        if (isHardSpacing()) {
            String startLine = line;
            try {
                while (line.charAt(0) == ' ') line = line.substring(1);
                return line;
            } catch (IndexOutOfBoundsException e) {
                return startLine;
            }
        }
        else return line.startsWith(" ") ? line.substring(1) : line;
    }

    public static String replaceInsensitiveEach(String line, String[] keys, String[] values) {
        if (keys == null || values == null) return line;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == null || values[i] == null) continue;
            line = line.replaceAll("(?i)" + keys[i], values[i]);
        }
        return line;
    }

    public static void sendFileMsg(CommandSender sender, List<String> list, String[] keys, String[] values) {
        for (String line : list) {
            if (line == null || line.equals("")) continue;

            line = line.startsWith(getLangPrefixKey()) ?
                    line.replace(getLangPrefixKey(), getLangPrefix()) : line;

            line = replaceInsensitiveEach(line, keys, values);

            if (sender != null && !(sender instanceof ConsoleCommandSender)) {
                Player player = (Player) sender;
                line = replaceInsensitiveEach(line, new String[] {getPlayerKey(), getPlayerWorldKey()},
                        new String[] {player.getName(), player.getWorld().getName()});

                selectMsgType(player, line);
            }
            else LogUtils.rawLog(JsonMsg.centeredText(null, line));
        }
    }

    public static void sendFileMsg(CommandSender sender, ConfigurationSection section, String path, String[] keys, String[] values) {
        sendFileMsg(sender, fileList(section, path), keys, values);
    }

    public static void sendFileMsg(CommandSender sender, List<String> list) {
        sendFileMsg(sender, list, null, null);
    }

    public static void sendFileMsg(CommandSender sender, ConfigurationSection section, String path) {
        sendFileMsg(sender, fileList(section, path));
    }

    public static void sendActionBar(Player player, String message) {
        actionBar.getMethod().send(player, message);
    }

    private static boolean checkInts(String[] array) {
        if (array == null) return false;
        for (String integer : array)
            if (!integer.matches("-?\\d+")) return false;
        return true;
    }

    public static void sendTitle(Player player, String[] message, String[] times) {
        if (message.length == 0 || message.length > 2) return;
        String subtitle = message.length == 1 ? "" : message[1];

        int[] i;
        if (checkInts(times) && times.length == 3) {
            i = new int[times.length];
            for (int x = 0; x < times.length; x++)
                i[x] = Integer.parseInt(times[x]);
        }
        else i = new int[] {10, 50, 10};

        titleMngr.getMethod().send(player, message[0], subtitle, i[0], i[1], i[2]);
    }

    private static String parsePrefix(String type, String message) {
        message = message.substring(type.length());
        return removeSpace(message);
    }

    public static boolean startsIgnoreCase(String prefix, String line) {
        return line.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    public static void selectMsgType(Player player, String line) {
        if (startsIgnoreCase(getActionBarKey(), line)) {
            sendActionBar(player, parsePrefix(getActionBarKey(), line));
        }
        else if (startsIgnoreCase(getTitleKey(), line)) {
            sendTitle(player, parsePrefix(getTitleKey(), line).split(getLineSplitter()), null);
        }
        else if (startsIgnoreCase(getJsonKey(), line) && line.contains("{\"text\":")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    String cmd = "minecraft:tellraw " + player.getName() + " "
                            + parsePrefix(getJsonKey(), line);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }.runTask(main);
        }
        else player.spigot().sendMessage(new JsonMsg(player, line).build());
    }

    public static String getCenterPrefix() {
        return centerPrefix;
    }
    public static String getLineSplitter() {
        return lineSplitter;
    }
    public static String getLangPrefix() {
        return langPrefix;
    }
    public static String getLangPrefixKey() {
        return langPrefixKey;
    }
    public static String getActionBarKey() {
        return actionBarKey;
    }
    public static String getTitleKey() {
        return titleKey;
    }
    public static String getJsonKey() {
        return jsonKey;
    }
    public static String getPlayerKey() {
        return playerKey;
    }
    public static String getPlayerWorldKey() {
        return playerWorldKey;
    }
    public static String getCharPattern() {
        return charPattern;
    }
    public static boolean isHardSpacing() {
        return isHardSpacing;
    }
}
