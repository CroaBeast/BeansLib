package me.croabeast.beanslib.utilities;

import com.google.common.collect.*;
import me.clip.placeholderapi.*;
import me.croabeast.beanslib.terminals.*;
import me.croabeast.iridiumapi.*;
import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.*;
import org.bukkit.scheduler.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.regex.Pattern;

public class TextUtils {

    private static JavaPlugin main;

    private static ActionBar actionBar;
    private static TitleMngr titleMngr;

    public static String
            CENTER_PREFIX, LINE_SPLITTER,
            LANG_PREFIX, LANG_PREFIX_KEY,
            ACTION_BAR_KEY, TITLE_KEY, JSON_KEY,
            PLAYER_KEY, PLAYER_WORLD_KEY;

    public static String MC_FORK;
    public static int MC_VERSION;

    public static boolean isHardSpacing;

    public TextUtils(JavaPlugin main) {
        TextUtils.main = main;
        actionBar = new ActionBar();
        titleMngr = new TitleMngr();
        loadTextValues();
    }

    public static void setCenterPrefix(String centerPrefix) {
        CENTER_PREFIX = centerPrefix;
    }
    public static void setLineSplitter(String lineSplitter) {
        LINE_SPLITTER = lineSplitter;
    }
    public static void setLangPrefix(String langPrefix) {
        LANG_PREFIX = langPrefix;
    }
    public static void setLangPrefixKey(String langPrefixKey) {
        LANG_PREFIX_KEY = langPrefixKey;
    }
    public static void setIsHardSpacing(boolean isHardSpacing) {
        TextUtils.isHardSpacing = isHardSpacing;
    }
    public static void setActionBarKey(String actionBarKey) {
        ACTION_BAR_KEY = actionBarKey;
    }
    public static void setTitleKey(String titleKey) {
        TITLE_KEY = titleKey;
    }
    public static void setJsonKey(String jsonKey) {
        JSON_KEY = jsonKey;
    }
    public static void setPlayerKey(String playerKey) {
        PLAYER_KEY = playerKey;
    }
    public static void setPlayerWorldKey(String playerWorldKey) {
        PLAYER_WORLD_KEY = playerWorldKey;
    }

    // Use this method in your reload command.
    public void loadTextValues() {
        String version = Bukkit.getBukkitVersion().split("-")[0];
        MC_VERSION = Integer.parseInt(version.split("\\.")[1]);
        MC_FORK = Bukkit.getVersion().split("-")[1] + " " + version;

        CENTER_PREFIX = "<C>";
        LINE_SPLITTER = Pattern.quote("<n>");
        LANG_PREFIX = "&e&l MY-PLUGIN &8> ";
        LANG_PREFIX_KEY = "<P>";

        ACTION_BAR_KEY = "[ACTION-BAR]";
        TITLE_KEY = "[TITLE]";
        JSON_KEY = "[JSON]";
        PLAYER_KEY = "{player}";
        PLAYER_WORLD_KEY = "{world}";

        isHardSpacing = true;
    }

    public static String parsePAPI(@Nullable Player player, String message) {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")
                ? PlaceholderAPI.setPlaceholders(player, message) : message;
    }

    public static String colorize(Player player, String message) {
        return IridiumAPI.process(parsePAPI(player, message));
    }

    public static String centerMessage(Player player, String message) {
        message = colorize(player, message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '§') previousCode = true;
            else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                FontInfo dFI = FontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ?
                        dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = 154 - halvedMessageSize;
        int spaceLength = FontInfo.SPACE.getLength() + 1;
        int compensated = 0;

        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }

        return sb + message;
    }

    public static TextComponent toComponent(String line) {
        return new TextComponent(TextComponent.fromLegacyText(line));
    }

    public static Object mixedChat(Player player, String line) {
        line = JsonMsg.isValid(line) ? new JsonMsg(player, line).getText() : line;
        line = line.startsWith(CENTER_PREFIX) ?
                centerMessage(player, line.replace(CENTER_PREFIX, "")) :
                colorize(player, line);
        return JsonMsg.isValid(line) ? toComponent(line) : line;
    }

    public static List<String> fileList(FileConfiguration file, String path) {
        return  !file.isList(path) ?
                Lists.newArrayList(file.getString(path)) :
                file.getStringList(path);
    }

    public static String removeSpace(String line) {
        if (isHardSpacing) {
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

    public static void sendFileMsg(CommandSender sender, List<String> list, String[] keys, String[] values) {
        for (String line : list) {
            if (line == null || line.equals("")) continue;
            line = line.startsWith(LANG_PREFIX_KEY) ?
                    line.replace(LANG_PREFIX_KEY, LANG_PREFIX) : line;

            if (keys != null && values != null) {
                for (int i = 0; i < keys.length; i ++)
                    line = line.replaceAll("(?i)\\{" + keys[i] + "}", values[i]);
            }

            if (sender != null && !(sender instanceof ConsoleCommandSender)) {
                Player player = (Player) sender;
                line = line.replaceAll("(?i)" + PLAYER_KEY, player.getName());
                line = line.replaceAll(
                        "(?i)" + PLAYER_WORLD_KEY, player.getWorld().getName());

                Object chatText = mixedChat(player, line);
                if (chatText instanceof String) player.sendMessage((String) chatText);
                else player.spigot().sendMessage((TextComponent) chatText);
            }
            else LogUtils.rawLog(line.replace(CENTER_PREFIX, ""));
        }
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

    private static int[] intArray(String[] array) {
        int[] ints = new int[array.length];
        for (int i = 0; i < array.length; i++)
            ints[i] = Integer.parseInt(array[i]);
        return ints;
    }

    public static void sendTitle(Player player, String[] message, String[] times) {
        if (message.length == 0 || message.length > 2) return;
        String subtitle = message.length == 1 ? "" : message[1];
        int[] i = checkInts(times) && times.length == 3 ?
                intArray(times) : new int[]{10, 50, 10};
        titleMngr.getMethod().send(player, message[0], subtitle, i[0], i[1], i[2]);
    }

    public static String parsePrefix(String type, String message) {
        message = message.substring(type.length());
        return removeSpace(message);
    }

    public static boolean isStarting(String prefix, String line) {
        return line.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    public static void selectMsgType(Player player, String line) {
        if (isStarting(ACTION_BAR_KEY, line)) {
            sendActionBar(player, parsePrefix(ACTION_BAR_KEY, line));
        }
        else if (isStarting(TITLE_KEY, line)) {
            sendTitle(player, parsePrefix(TITLE_KEY, line).split(LINE_SPLITTER), null);
        }
        else if (isStarting(JSON_KEY, line) && line.contains("{\"text\":")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    String cmd = "minecraft:tellraw " + player.getName() + " "
                            + parsePrefix(JSON_KEY, line);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }.runTask(main);
        }
        else {
            Object text = mixedChat(player, line);
            if (text instanceof String) player.sendMessage((String) text);
            else player.spigot().sendMessage((TextComponent) text);
        }
    }

    public enum FontInfo {
        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),

        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),

        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),

        LEFT_PARENTHESIS('(', 4),
        RIGHT_PARENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),

        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 4);

        private final char character;
        private final int length;

        FontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        private char getCharacter() {
            return character;
        }

        public int getLength() {
            return length;
        }

        public int getBoldLength() {
            if (this == SPACE) return getLength();
            return this.length + 1;
        }

        public static FontInfo getDefaultFontInfo(char c) {
            for (FontInfo dFI : values())
                if (dFI.getCharacter() == c) return dFI;
            return DEFAULT;
        }
    }
}
