package me.croabeast.beanslib.object.display;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.object.discord.Webhook;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;

import static me.croabeast.beanslib.utility.TextUtils.*;

/**
 * <p>The <code>Displayer</code> class represents the action to display a list
 * of messages to a player using {@link BeansLib} keys for parse different message
 * types and format every message type respectively.</p>
 *
 * <p> This is a basic example of how to create a new <code>Displayer</code> object.
 * <pre>{@code
 * new Displayer(
 *       "A BeansLib instance to get methods and variables",
 *       "A collection of CommandSender targets to display",
 *       "A player to parse colors and placeholders, can be null",
 *       "A String list of messages to display"
 *       "An array of flags to allow certain message types"
 *       // if there is no flag array, it will allow all types
 * );
 * }</pre>
 *
 * Example:
 * <pre>{@code
 * Displayer displayer = new Displayer(
 *       plugin.getBeansLibExtendedClass(),
 *       Bukkit.getOnlinePlayers(), // can be single or null
 *       Bukkit.getPlayer("Markiplier"),
 *       plugin.getConfig().getStringList("path-here"),
 *       Displayer.CHAT, Displayer.BOSSBAR
 *       // only chat and bossbar messages are allowed
 *       // with these flags
 * );
 *
 * displayer.display(); // displays the object
 * }</pre>
 *
 * See {@link #display(boolean)} or {@link #display()} for more info.
 */
public class Displayer {

    /**
     * This flag allows to send chat messages.
     */
    public static final String CHAT = "CHAT";
    /**
     * This flag allows to display bossbar messages.
     */
    public static final String BOSSBAR = "BOSSBAR";
    /**
     * This flag allows to send vanilla JSON messages.
     */
    public static final String JSON = "JSON";
    /**
     * This flag allows to send webhooks.
     */
    public static final String WEBHOOK = "WEBHOOK";
    /**
     * This flag allows to display title messages.
     */
    public static final String TITLE = "TITLE";
    /**
     * This flag allows to display action bar messages.
     */
    public static final String ACTION_BAR = "ACTION-BAR";

    private final BeansLib lib;

    private final Collection<? extends CommandSender> targets;
    private final Player parser;

    private final List<String> list;
    private final Set<String> flags;

    private String[] keys = null, values = null;
    private UnaryOperator<String>[] operators = null;

    private boolean isRegistered = false,
            isLogger = true,
            caseSensitive = true;

    /**
     * See {@link Displayer} for more info.
     *
     * @param lib       the BeansLib instance
     * @param targets a CommandSender targets, can be null
     * @param parser  a player to parse values, can be null
     * @param list    a string list
     * @param flags   an array of flags to allow certain message types
     */
    public Displayer(
            BeansLib lib, Collection<? extends CommandSender> targets,
            Player parser, List<String> list, String... flags
    ) {
        this.lib = lib == null ? BeansLib.getLoadedInstance() : lib;

        this.targets = targets;
        this.parser = parser;

        this.list = list;
        this.flags = Sets.newHashSet(flags);
    }

    /**
     * See {@link Displayer} for more info.
     *
     * @param lib      the BeansLib instance
     * @param target a CommandSender target, can be null
     * @param parser a player to parse values, can be null
     * @param list   a string list
     * @param flags  an array of flags to allow certain message types
     */
    public Displayer(BeansLib lib, CommandSender target, Player parser, List<String> list, String... flags) {
        this(lib, target == null ? null : Lists.newArrayList(target), parser, list, flags);
    }

    /**
     * See {@link Displayer} for more info.
     *
     * @param lib      the BeansLib instance
     * @param parser a player to parse values, can be null
     * @param list   a string list
     * @param flags  an array of flags to allow certain message types
     */
    public Displayer(BeansLib lib, Player parser, List<String> list, String... flags) {
        this(lib, parser, parser, list, flags);
    }

    /**
     * Sets strings operators to apply them in
     * every string of the string list.
     *
     * @param ops an array of operator
     * @return a reference of this object
     */
    @SafeVarargs
    public final Displayer setOperators(UnaryOperator<String>... ops) {
        operators = ops;
        return this;
    }

    /**
     * Sets the string keys to be replaced for
     * the input values in {@link #setValues(String...)}.
     *
     * @param keys an array of keys
     * @return a reference of this object
     */
    public Displayer setKeys(String... keys) {
        this.keys = keys;
        return this;
    }

    /**
     * Sets the string values that replaces the
     * input keys in {@link #setValues(String...)}.
     *
     * @param values an array of values
     * @return a reference of this object
     */
    public Displayer setValues(String... values) {
        this.values = values;
        return this;
    }

    /**
     * Sets if messages can be sent into the console or not.
     *
     * @param b the input value
     * @return a reference of this object
     */
    public Displayer setLogger(boolean b) {
        isLogger = b;
        return this;
    }

    /**
     * Sets if the input keys in {@link #setValues(String...)}
     * are case-sensitive or not if input keys were set.
     *
     * @param b the input value
     * @return a reference of this object
     */
    public Displayer setCaseSensitive(boolean b) {
        caseSensitive = b;
        return this;
    }

    private String parseOperatorsAndValues(String string) {
        if (operators != null && operators.length > 0) {
            for (UnaryOperator<String> o : operators) {
                if (o == null) continue;
                string = o.apply(string);
            }
        }

        string = lib.parsePlayerKeys(parser, string, caseSensitive);
        return replaceEach(string, keys, values, caseSensitive);
    }

    private void registerValues() {
        if (isRegistered) return;
        if (list.isEmpty()) return;

        List<String> l = new ArrayList<>();

        for (String s : list) {
            if (s == null) continue;
            s = s.replace(lib.getLangPrefixKey(), lib.getLangPrefix());
            l.add(parseOperatorsAndValues(s));
        }

        list.clear();
        list.addAll(l);

        isRegistered = true;
    }

    private boolean isNotAllowed(String flag) {
        return !flags.isEmpty() && !flags.contains(flag);
    }

    private void sendWebhooks() {
        for (String s : list) {
            final BeansLib.MessageKey key = lib.identifyMessageType(s);

            if (key != null && key.getUpperKey().equals(WEBHOOK)) {
                if (isNotAllowed(WEBHOOK)) continue;

                ConfigurationSection id = lib.getWebhookSection();
                if (id == null) continue;

                List<String> list = new ArrayList<>(id.getKeys(false));
                if (list.isEmpty()) continue;

                Matcher m2 = key.getPattern().matcher(s);
                String line = key.formatString(null, parser, s);

                String path = list.get(0);

                if (m2.find()) {
                    String[] a = m2.group().replace("[", "").
                            replace("]", "").split(":", 2);

                    String temp = a.length == 2 ? a[1] : null;
                    if (temp != null) path = temp;
                }

                id = id.getConfigurationSection(path);
                if (id != null) new Webhook(id, line).send();
            }
            lib.rawLog(s);
        }
    }

    /**
     * Displays all the messages in the string list to the defined targets.
     * If there is no targets, will be display in console as log messages.
     *
     * @param hardSpacing if the first spaces of a chat message will be removed.
     */
    public void display(boolean hardSpacing) {
        // register the values and operators in each line
        registerValues();

        // Checks if the messages list is empty or if
        // the first message is blank to not display.
        if (list.isEmpty()) return;
        if (list.size() == 1 && StringUtils.isBlank(list.get(0))) return;

        // if there is no target(s), it will display to the console.
        if (targets == null || targets.isEmpty()) {
            sendWebhooks();
            return;
        }

        // Only gets the players from the collection of targets.
        List<Player> targets = new ArrayList<>();

        for (CommandSender t : this.targets) {
            if (t == null) continue;
            if (!(t instanceof Player)) continue;
            targets.add(((Player) t));
        }

        // if there is no player targets, it will display to the console.
        if (targets.isEmpty()) {
            sendWebhooks();
            return;
        }

        // Displays the messages list to console if enabled.
        if (isLogger) list.forEach(lib::rawLog);

        // Iterates of every target to display.
        for (Player t : targets)
            for (String s : list) {
                final BeansLib.MessageKey key = lib.identifyMessageType(s);

                if (key == null) {
                    if (isNotAllowed(CHAT)) continue;

                    String temp = hardSpacing ? removeSpace(s) : s;

                    new JsonBuilder(lib, t, parser, temp).send();
                    continue;
                }

                switch (key.getUpperKey()) {

                    case ACTION_BAR:
                        if (isNotAllowed(ACTION_BAR)) continue;
                        sendActionBar(t, key.formatString(t, parser, s));
                        continue;

                    case TITLE:
                        if (isNotAllowed(TITLE)) continue;

                        Matcher m1 = key.getPattern().matcher(s);

                        String tm = null;
                        try {
                            if (m1.find()) tm = m1.group(1).substring(1);
                        } catch (Exception ignored) {}

                        int[] a = lib.getDefaultTitleTicks();
                        int time = a[1];

                        try {
                            if (tm != null) time = Integer.parseInt(tm) * 20;
                        } catch (Exception ignored) {}

                        String t1 = key.formatString(t, parser, s);

                        sendTitle(t, t1.split(lib.getLineSeparator()), a[0], time, a[2]);
                        continue;

                    case BOSSBAR:
                        if (isNotAllowed(BOSSBAR)) continue;

                        Plugin plugin = lib.getPlugin();
                        if (plugin == null) continue;

                        new BossbarBuilder(plugin, t, s).display();
                        continue;

                    case JSON:
                        if (isNotAllowed(JSON)) continue;

                        Bukkit.dispatchCommand(
                                Bukkit.getConsoleSender(), "minecraft:tellraw " +
                                t.getName() + " " + key.formatString(t, parser, s)
                        );
                        continue;

                    case WEBHOOK:
                        if (isNotAllowed(WEBHOOK)) continue;

                        ConfigurationSection id = lib.getWebhookSection();
                        if (id == null) continue;

                        List<String> list = new ArrayList<>(id.getKeys(false));
                        if (list.isEmpty()) continue;

                        Matcher m2 = key.getPattern().matcher(s);
                        String line = key.formatString(null, parser, s);

                        String path = list.get(0);

                        if (m2.find()) {
                            String[] split = m2.group().replace("[", "").
                                    replace("]", "").split(":", 2);

                            String temp = split.length == 2 ? split[1] : null;
                            if (temp != null) path = temp;
                        }

                        id = id.getConfigurationSection(path);
                        if (id != null) new Webhook(id, line).send();
                        continue;

                    default:
                }
            }
    }

    /**
     * Displays all the messages in the string list to the defined targets.
     * If there is no targets, will be display in console as log messages.
     * <p>The <code>hardSpacing</code> value of {@link #display(boolean)} is false using this method.
     */
    public void display() {
        display(false);
    }

    /**
     * Creates a single Displayer object with default values and a string list.
     * Bossbar messages will not work with this integration.
     *
     * @param player a target player that can parse values, can not be null
     * @param list an input string list
     *
     * @return a new instance of the {@link Displayer} object
     */
    public static Displayer of(@NotNull Player player, List<String> list) {
        return new Displayer(null, player, list);
    }
}
