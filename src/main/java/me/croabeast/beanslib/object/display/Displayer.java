package me.croabeast.beanslib.object.display;

import com.google.common.collect.Sets;
import me.croabeast.beanslib.object.discord.Webhook;
import me.croabeast.beanslib.utility.LogUtils;
import me.croabeast.beanslib.BeansMethods;
import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.beanslib.BeansVariables;
import me.croabeast.iridiumapi.IridiumAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.croabeast.beanslib.utility.TextUtils.*;

/**
 * The <code>Displayer</code> class represents the action to display a list of messages
 * to a player using {@link BeansVariables} keys for parse different message types.
 *
 * <p> It can set flags to allow certain message types to be sent.
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
    /**
     * This flag allows to display every message type.
     */
    public static final String ALL = "ALL";

    private final BeansMethods m;

    private final CommandSender target;
    private final Player parser;

    private final AccessChecker checker;
    private List<String> list;

    private String[] keys = null, values = null;
    private UnaryOperator<String>[] operators = null;

    private boolean isRegistered = false,
            caseSensitive = true,
            stripPrefix = false;

    public Displayer(BeansMethods m, CommandSender target, Player parser, List<String> list, String... flags) {
        this.m = m == null ? BeansMethods.DEFAULTS : m;
        this.target = target == null ?
                Bukkit.getConsoleSender() : target;

        Player t = target instanceof Player ? (Player) target : null;
        this.parser = (parser == null && t != null) ? t : parser;

        this.list = list;
        checker = new AccessChecker(this.m, flags);
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

    public Displayer setCaseSensitive(boolean b) {
        caseSensitive = b;
        return this;
    }

    public Displayer setStripPrefix(boolean b) {
        stripPrefix = b;
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

        List<String> list = new ArrayList<>();
        if (this.list.isEmpty()) return;

        for (String s : this.list) {
            if (s == null) continue;

            s = s.replace(m.langPrefixKey(), m.langPrefix());
            list.add(parseOperatorsAndValues(s));
        }

        this.list = list;
        isRegistered = true;
    }

    private String parseFormat(String regex, String string, boolean color) {
        Matcher m = Pattern.compile(regex).matcher(string);
        if (m.find()) string = string.replace(m.group(), "");

        string = TextUtils.removeSpace(string);

        if (!color) {
            String s = parsePAPI(parser, this.m.parseChars(string));
            return IridiumAPI.stripAll(stripJson(s));
        }

        return this.m.colorize(target instanceof Player ?
                (Player) target : null, parser, string);
    }

    public void display() {
        registerValues(); // register the values and operators
        if (list.isEmpty()) return;

        Player t = target instanceof Player ? (Player) target : null;

        if (t == null) {
            for (String s : list) LogUtils.rawLog(m, s);
            return;
        }

        for (String s : list) {
            if (checker.isTitle(s)) {
                Matcher m = Pattern.compile(this.m.titlePrefix(true)).matcher(s);

                String tm = null;
                try {
                    if (m.find()) tm = m.group(1).substring(1);
                } catch (Exception ignored) {}

                int[] ticks = this.m.defaultTitleTicks();
                int time = ticks[1];

                try {
                    if (tm != null) time = Integer.parseInt(tm) * 20;
                } catch (Exception ignored) {}

                String temp = parseFormat(this.m.titlePrefix(true), s, true);

                sendTitle(t,
                        temp.split(this.m.lineSeparator()),
                        ticks[0], time, ticks[2]
                );
            }
            else if (checker.isActionBar(s)) {
                String temp = parseFormat(m.actionBarRegex(true), s, true);
                sendActionBar(t, temp);
            }
            else if (checker.isJson(s)) {
                String cmd = parseFormat(m.jsonRegex(true), s, false);

                Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        "minecraft:tellraw " +
                        target.getName() + " " + cmd
                );
            }
            else if (checker.isBossbar(s)) {
                new Bossbar(m.getPlugin(), t, s).display();
            }
            else if (checker.isWebhook(s)) {
                ConfigurationSection id = m.getWebhookSection();

                List<String> list = new ArrayList<>(id.getKeys(false));
                if (list.isEmpty()) continue;

                Matcher m = Pattern.compile(this.m.webhookRegex(true)).matcher(s);
                String n = m.find() ? m.group(1) : null;

                String line = parseFormat(this.m.webhookRegex(true), s, false);

                if (n == null) {
                    n = list.get(0);
                    new Webhook(id.getConfigurationSection(n), line).send();
                    continue;
                }

                for (String l : list) if (n.equals(l)) n = l;
                new Webhook(id.getConfigurationSection(n), line).send();
            }
            else if (checker.isChat())
                new JsonMessage(m, t, parser, removeSpace(s)).send();
        }
    }

    public static void display(Player target, Player parser, List<String> list) {
        new Displayer(null, target, parser, list).display();
    }

    static class AccessChecker {

        private final BeansMethods methods;
        private final Set<String> flags;

        AccessChecker(BeansMethods methods, String... flags) {
            this.methods = methods;
            this.flags = flags == null || flags.length == 0 ?
                    Sets.newHashSet(ALL) :
                    Sets.newHashSet(flags);
        }

        private boolean isAll() {
            return flags.contains(ALL);
        }

        public boolean isChat() {
            return isAll() || flags.contains(CHAT);
        }

        public boolean isActionBar(String s) {
            boolean allow = isAll() || flags.contains(ACTION_BAR);
            return (allow) && s.matches(methods.actionBarRegex(true));
        }

        public boolean isTitle(String s) {
            boolean allow = isAll() || flags.contains(TITLE);
            return (allow) && s.matches(methods.titlePrefix(true));
        }

        public boolean isBossbar(String s) {
            boolean allow = isAll() || flags.contains(BOSSBAR);
            allow = (allow) && s.matches(methods.bossbarRegex(true));
            return allow && methods.getPlugin() != null;
        }

        public boolean isJson(String s) {
            boolean allow = isAll() || flags.contains(JSON);
            return (allow) && s.matches(methods.jsonRegex(true));
        }

        public boolean isWebhook(String s) {
            boolean allow = isAll() || flags.contains(WEBHOOK);
            allow = (allow) && s.matches(methods.webhookRegex(true));
            return allow && methods.getWebhookSection() != null;
        }
    }
}
