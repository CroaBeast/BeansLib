package me.croabeast.beanslib.object.display;

import com.google.common.collect.Sets;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.object.discord.Webhook;
import me.croabeast.beanslib.utility.LogUtils;
import me.croabeast.beanslib.BeansMethods;
import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.beanslib.BeansVariables;
import me.croabeast.iridiumapi.IridiumAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.croabeast.beanslib.utility.TextUtils.*;

/**
 * The <code>Displayer</code> class represents the action to display a list of messages
 * to a player using {@link BeansVariables} keys for parse different message types.
 *
 * <p></p> This is a basic example of how to create a new <code>Displayer</code> object.
 * <pre>{@code
 * new Displayer(
 *       "A BeansLib instance to get methods and variables",
 *       "A collection of CommandSender targets to display",
 *       "A player to parse colors and placeholders, can be null",
 *       "A String list of messages to display"
 * );
 * }</pre>
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
    public static final String ACTION_BAR = "ACTION_BAR";

    private final BeansMethods m;

    private final Collection<? extends CommandSender> targets;
    private final Player parser;

    private final List<String> list;
    private final Set<String> flags;

    private String[] keys = null, values = null;
    private UnaryOperator<String>[] operators = null;

    private boolean isRegistered = false,
            isLogger = true,
            caseSensitive = true;

    public Displayer(
            BeansMethods m, Collection<? extends CommandSender> targets,
            Player parser, List<String> list, String... flags
    ) {
        this.m = m == null ? BeansMethods.DEFAULTS : m;

        this.targets = targets;
        this.parser = parser;

        this.list = list;
        this.flags = Sets.newHashSet(flags);
    }

    public Displayer(BeansMethods m, CommandSender target, Player parser, List<String> list, String... flags) {
        this(m, target == null ? null : Collections.singletonList(target), parser, list, flags);
    }

    public Displayer(BeansMethods m, Player parser, List<String> list, String... flags) {
        this(m, parser, parser, list, flags);
    }

    @SafeVarargs
    public final Displayer setOperators(UnaryOperator<String>... ops) {
        operators = ops;
        return this;
    }

    public Displayer setKeys(String... keys) {
        this.keys = keys;
        return this;
    }

    public Displayer setValues(String... values) {
        this.values = values;
        return this;
    }

    public Displayer setLogger(boolean b) {
        isLogger = b;
        return this;
    }

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

        string = m.parsePlayerKeys(parser, string, caseSensitive);
        return replaceEach(string, keys, values, caseSensitive);
    }

    private void registerValues() {
        if (isRegistered) return;
        if (list.isEmpty()) return;

        List<String> l = new ArrayList<>();

        for (String s : list) {
            if (s == null) continue;
            s = s.replace(m.langPrefixKey(), m.langPrefix());
            l.add(parseOperatorsAndValues(s));
        }

        list.clear();
        list.addAll(l);

        isRegistered = true;
    }

    private String parseFormat(Player t, String r, String s, boolean c) {
        Matcher m = Pattern.compile(r).matcher(s);

        while (m.find()) s = s.replace(m.group(), "");
        s = TextUtils.removeSpace(s);

        if (!c) {
            String s1 = parsePAPI(parser, this.m.parseChars(s));
            return IridiumAPI.stripAll(stripJson(s1));
        }

        return this.m.colorize(t, parser, s);
    }

    private boolean checkMatch(String s, String p) {
        Matcher m = Pattern.compile(p).matcher(s);
        return m.find();
    }

    public void display(boolean hardSpacing) {
        // register the values and operators in each line
        registerValues();

        // Checks if the messages list is empty or if
        // the first message is blank to not display.
        if (list.isEmpty()) return;
        if (list.size() == 1 && StringUtils.isBlank(list.get(0))) return;

        // if there is no target(s), it will display to the console.
        if (targets == null || targets.isEmpty()) {
            for (String s : list) LogUtils.rawLog(m, s);
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
            for (String s : list) LogUtils.rawLog(m, s);
            return;
        }

        // Gets all the message type regexes.
        String abp = m.actionBarRegex(true), tp = m.titleRegex(true),
                jp = m.jsonRegex(true), bp = m.bossbarRegex(true),
                wp = m.webhookRegex(true);

        // Displays the messages list to console if enabled.
        if (isLogger) for (String s : list) LogUtils.rawLog(m, s);

        // Iterates of every target to display.
        targets.forEach(t -> {
            for (String s : list) {
                // Checks if the message is an action bar type and if the type is allowed.
                if (checkMatch(s, abp)) {
                    if (!flags.isEmpty() && !flags.contains(ACTION_BAR)) continue;

                    sendActionBar(t, parseFormat(t, abp, s, true));
                    continue;
                }

                // Checks if the message is a title type and if the type is allowed.
                if (checkMatch(s, tp)) {
                    if (!flags.isEmpty() && !flags.contains(TITLE)) continue;
                    Matcher r = Pattern.compile(tp).matcher(s);

                    String tm = null;
                    try {
                        if (r.find()) tm = r.group(1).substring(1);
                    } catch (Exception ignored) {}

                    int[] ticks = this.m.defaultTitleTicks();
                    int time = ticks[1];

                    try {
                        if (tm != null) time = Integer.parseInt(tm) * 20;
                    } catch (Exception ignored) {}

                    String temp1 = parseFormat(t, tp, s, true);

                    sendTitle(t,
                            temp1.split(m.lineSeparator()),
                            ticks[0], time, ticks[2]
                    );
                    continue;
                }

                // Checks if the message is a json type and if the type is allowed.
                if (checkMatch(s, jp)) {
                    if (!flags.isEmpty() && !flags.contains(JSON)) continue;
                    String cmd = parseFormat(t, jp, s, false);

                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "minecraft:tellraw " +
                            t.getName() + " " + cmd
                    );
                    continue;
                }

                // Checks if the message is a bossbar type and if the type is allowed.
                if (checkMatch(s, bp)) {
                    if (!flags.isEmpty() && !flags.contains(BOSSBAR)) continue;
                    if (m.getPlugin() == null) continue;

                    new Bossbar(m.getPlugin(), t, s).display();
                    continue;
                }

                // Checks if the message is a webhook type and if the type is allowed.
                if (checkMatch(s, wp)) {
                    if (!flags.isEmpty() && !flags.contains(WEBHOOK)) continue;
                    ConfigurationSection id = m.getWebhookSection();

                    if (id == null) continue;

                    List<String> list = new ArrayList<>(id.getKeys(false));
                    if (list.isEmpty()) continue;

                    Matcher r1 = Pattern.compile(wp).matcher(s);
                    String n = r1.find() ? r1.group(1) : null;

                    String line = parseFormat(t, wp, s, false);

                    if (n == null) {
                        n = list.get(0);
                        new Webhook(id.getConfigurationSection(n), line).send();
                        continue;
                    }

                    for (String l : list) if (n.equals(l)) n = l;
                    new Webhook(id.getConfigurationSection(n), line).send();
                    continue;
                }

                // Checks if the message is a chat type and if the type is allowed.
                if (!flags.isEmpty() && !flags.contains(CHAT)) continue;
                new JsonMessage(m, t, parser, hardSpacing ? removeSpace(s) : s).send();
            }
        });
    }

    public void display() {
        display(false);
    }

    public static Displayer fromPlayer(Player player, List<String> list) {
        return new Displayer(null, (CommandSender) null, player, list);
    }
}
