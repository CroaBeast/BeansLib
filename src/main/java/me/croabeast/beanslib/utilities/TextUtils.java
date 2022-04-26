package me.croabeast.beanslib.utilities;

import com.google.common.collect.*;
import me.clip.placeholderapi.*;
import me.croabeast.beanslib.*;
import me.croabeast.beanslib.terminals.*;
import me.croabeast.iridiumapi.*;
import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;

import static me.croabeast.iridiumapi.IridiumAPI.*;
import static net.md_5.bungee.api.chat.ClickEvent.Action.*;

public abstract class TextUtils extends TextKeys {

    private final ActionBar actionBar;
    private final TitleMngr titleMngr;

    public TextUtils() {
        actionBar = new ActionBar();
        titleMngr = new TitleMngr();
    }

    private String colorLogger(@NotNull String line) {
        return stripJson(COLOR_SUPPORT ? process(line) : stripAll(line));
    }

    public void playerLog(@NotNull Player player, String... lines) {
        for (String s : lines) if (s != null)
            player.sendMessage(process(s.replace(langPrefixKey(), langPrefix())));
    }

    public void rawLog(String... lines) {
        for (String s : lines) if (s != null) Bukkit.getServer().getLogger().info(colorLogger(s));
    }

    public void doLog(@Nullable CommandSender sender, String... lines) {
        if (sender instanceof Player) playerLog((Player) sender, lines);
        for (String s : lines) if (s != null)
            BeansLib.getPlugin().getLogger().info(colorLogger(s.replace(langPrefixKey(), "")));
    }

    public void doLog(String... lines) {
        doLog(null, lines);
    }

    public String stripPrefix(String line) {
        Matcher matcher = getTextPattern().matcher(line);
        line = removeSpace(line);

        return (matcher.find() && isStripPrefix()) ?
                line.replace(matcher.group(1), "") : line;
    }

    public String removeSpace(String line) {
        if (isHardSpacing()) {
            String startLine = line;
            try {
                while (line.charAt(0) == ' ') line = line.substring(1);
                return line;
            } catch (IndexOutOfBoundsException e) {
                return startLine;
            }
        }
        else return line;
    }

    public static String parsePAPI(@Nullable Player player, String message) {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ?
                PlaceholderAPI.setPlaceholders(player, message) : message;
    }

    public static String replaceInsensitiveEach(String line, String[] keys, String[] values) {
        if (keys == null || values == null) return line;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == null | values[i] == null) continue;
            keys[i] = Pattern.quote(keys[i]);
            line = line.replaceAll("(?i)" + keys[i], values[i]);
        }
        return line;
    }

    public String parseChars(String line) {
        Pattern charPattern = Pattern.compile(charPattern());
        Matcher match = charPattern.matcher(line);

        while (match.find()) {
            char s = (char) Integer.parseInt(match.group(1), 16);
            line = line.replace(match.group(), s + "");
        }
        return line;
    }

    public String colorize(@Nullable Player player, String message) {
        return IridiumAPI.process(parsePAPI(player, parseChars(message)));
    }

    public List<String> toList(ConfigurationSection file, String path) {
        return  !file.isList(path) ?
                Lists.newArrayList(file.getString(path)) :
                file.getStringList(path);
    }

    /**
     * Creates a centered chat message.
     * @param player a player to parse placeholders.
     * @param message the input message.
     * @return the centered chat message.
     */
    public String centerMessage(Player player, String message) {
        String initial = colorize(player, stripJson(message));

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : initial.toCharArray()) {
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

        return sb + colorize(player, message);
    }

    /**
     * Defines a string if is centered or not.
     * @param player a player to parse placeholders.
     * @param line the input line.
     * @return the result string.
     */
    public String centeredText(Player player, String line) {
        return line.startsWith(centerPrefix()) ?
                centerMessage(player, line.replace(centerPrefix(), "")) :
                colorize(player, line);
    }

    /**
     * Check if the line has a valid json format. Usage:
     * <p> if (IS_JSON.apply(stringInput)) doSomething;
     */
    public final Function<String, Boolean> IS_JSON = s -> JSON_PATTERN.matcher(s).find();

    /**
     * Strips the JSON format from a line.
     * @param line the line to strip.
     * @return the stripped line.
     */
    public String stripJson(String line) {
        return line.replaceAll("(?i)</?(text|hover|run|suggest|url)" +
                "(=\\[(.+?)](\\|(hover|run|suggest|url)=\\[(.+?)])?)?>", "");
    }

    /**
     * Converts a string to a TextComponent.
     * @param line the line to convert.
     * @return the requested component.
     */
    private TextComponent toComponent(String line) {
        return new TextComponent(TextComponent.fromLegacyText(line));
    }

    /**
     * Add a click event to a component.
     * @param comp the component to add the event
     * @param type the click event type
     * @param input the input line for the click event
     */
    private void addClick(TextComponent comp, String type, String input) {
        ClickEvent.Action action = null;
        if (type.matches("(?i)run")) action = RUN_COMMAND;
        else if (type.matches("(?i)suggest")) action = SUGGEST_COMMAND;
        else if (type.matches("(?i)url")) action = OPEN_URL;
        if (action != null) comp.setClickEvent(new ClickEvent(action, input));
    }

    /**
     *
     * @param comp the component to add the event
     * @param hover the list to add as a hover
     */
    @SuppressWarnings("deprecation")
    private void addHover(Player player, TextComponent comp, List<String> hover) {
        BaseComponent[] array = new BaseComponent[hover.size()];
        for (int i = 0; i < hover.size(); i++) {
            String end = i == hover.size() - 1 ? "" : "\n";
            array[i] = toComponent(colorize(player, hover.get(i)) + end);
        }
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, array));
    }

    /**
     * Add the event found in the formatted line.
     * @param comp the component to add the event.
     * @param type the event's type.
     * @param input the input line for the event.
     */
    private void addEvent(Player player, TextComponent comp, String type, String input) {
        if (type.matches("(?i)run|suggest|url")) addClick(comp, type, input);
        else if (type.matches("(?i)hover"))
            addHover(player, comp, Arrays.asList(input.split(lineSeparator())));
    }

    public BaseComponent[] stringToJson(Player player, String line) {
        line = centeredText(player, line);
        List<BaseComponent> components = new ArrayList<>();
        final Matcher match = JSON_PATTERN.matcher(line);
        int lastEnd = 0;

        while (match.find()) {
            final String type = match.group(1);
            final String input = match.group(2);

            final String extra = match.group(3);
            final String type2 = match.group(4);
            final String input2 = match.group(5);

            boolean isExtra = extra != null && extra.matches("(?i)\\|" + JSON_PREFIX);

            final String text = match.group(6);
            final String before = line.substring(lastEnd, match.start());

            components.addAll(Arrays.asList(TextComponent.fromLegacyText(before)));
            final TextComponent comp = toComponent(text);

            addEvent(player, comp, type, input);
            if (isExtra) addEvent(player, comp, type2, input2);

            components.add(comp);
            lastEnd = match.end();
        }

        if (lastEnd < (line.length() - 1)) {
            final String after = line.substring(lastEnd);
            components.addAll(Arrays.asList(TextComponent.fromLegacyText(after)));
        }

        return components.toArray(new BaseComponent[0]);
    }

    public BaseComponent[] stringToJson(Player player, String line, @Nullable String click, List<String> hover) {
        if (IS_JSON.apply(line)) line = stripJson(line);
        line = centeredText(player, line);

        List<BaseComponent> components = new ArrayList<>();
        final TextComponent comp = toComponent(line);

        if (!hover.isEmpty()) addHover(player, comp, hover);
        if (click != null) {
            String[] input = click.split(":", 2);
            addClick(comp, input[0], input[1]);
        }

        components.add(comp);
        return components.toArray(new BaseComponent[0]);
    }

    public void sendActionBar(Player player, String message) {
        actionBar.getMethod().send(player, message);
    }

    public void sendTitle(Player player, @NotNull String[] message, int in, int stay, int out) {
        if (message.length == 0 || message.length > 2) return;
        String subtitle = message.length == 1 ? "" : message[1];
        titleMngr.getMethod().send(player, message[0], subtitle, in, stay, out);
    }

    public void sendMessage(@Nullable Player target, @NotNull Player sender, String input) {
        if (target == null) target = sender;
        Matcher matcher = getTextPattern().matcher(input);

        if (matcher.find()) {
            String line = colorize(sender, removeSpace(matcher.group(3)));
            String prefix = removeSpace(matcher.group(2));

            if (prefix.matches("(?i)" + titleKey())) {
                Matcher timeMatch = Pattern.compile("(?i)" + titleKey()).matcher(prefix);
                String timeString;

                try {
                    timeString = timeMatch.find() ? timeMatch.group(1) : null;
                } catch (Exception e) {
                    timeString = null;
                }

                int time = timeString == null ? defaultTitleTicks()[1] :
                        Integer.parseInt(timeString) * 20;

                sendTitle(target, line.split(lineSeparator()),
                        defaultTitleTicks()[0], time, defaultTitleTicks()[2]);
            }
            else if (prefix.matches("(?i)" + getJsonKey())) {
                String cmd = "minecraft:tellraw " + target.getName() + " " + line;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
            else if (prefix.matches("(?i)" + getActionBarKey())) sendActionBar(target, line);
            else if (prefix.matches("(?i)" + getBossbarKey())) new Bossbar(target, line).display();
            else target.spigot().sendMessage(stringToJson(sender, line));
        }
        else target.spigot().sendMessage(stringToJson(sender, input));
    }

    public void sendMessageList(CommandSender sender, List<String> list, @Nullable String[] keys, @Nullable String[] values) {
        for (String line : list) {
            if (line == null || line.equals("")) continue;

            line = line.startsWith(langPrefixKey()) ?
                    line.replace(langPrefixKey(), langPrefix()) : line;

            line = replaceInsensitiveEach(line, keys, values);

            if (sender != null && !(sender instanceof ConsoleCommandSender)) {
                Player player = (Player) sender;

                line = replaceInsensitiveEach(line, new String[] {playerKey(), playerWorldKey()},
                        new String[] {player.getName(), player.getWorld().getName()});

                sendMessage(null, player, line);
            }
            else rawLog(centeredText(null, line));
        }
    }

    public void sendMessageList(CommandSender sender, ConfigurationSection section, String path, @Nullable String[] keys, @Nullable String[] values) {
        sendMessageList(sender, toList(section, path), keys, values);
    }

    public void sendMessageList(CommandSender sender, List<String> list) {
        sendMessageList(sender, list, null, null);
    }

    public void sendMessageList(CommandSender sender, ConfigurationSection section, String path) {
        sendMessageList(sender, toList(section, path));
    }

    /**
     * The enum class to manage the length of every char.
     */
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
