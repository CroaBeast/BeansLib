package me.croabeast.beanslib.utility.sender;

import com.google.common.collect.Sets;
import me.croabeast.beanslib.object.Bossbar;
import me.croabeast.beanslib.object.JsonMessage;
import me.croabeast.beanslib.object.discord.Webhook;
import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.beanslib.utility.key.LibKeys;
import me.croabeast.beanslib.utility.key.TextKeys;
import me.croabeast.iridiumapi.IridiumAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.croabeast.beanslib.utility.TextUtils.*;

/**
 * The {@code Displayer} class
 */
public class Displayer {

    public static final String CHAT = "CHAT";
    public static final String BOSSBAR = "BOSSBAR";
    public static final String JSON = "JSON";
    public static final String WEBHOOK = "WEBHOOK";
    public static final String TITLE = "TITLE";
    public static final String ACTION_BAR = "ACTION_BAR";
    public static final String ALL = "ALL";

    private final AccessChecker checker;

    private final TextKeys text;

    private final Player target;
    private final Player parser;

    private List<String> list;

    private String[] keys = null, values = null;
    private UnaryOperator<String>[] operators;

    private boolean isRegistered = false;

    public Displayer(TextKeys textKeys, Player target, Player parser, List<String> list, String... flags) {
        this.text = textKeys == null ? LibKeys.DEFAULTS : textKeys;

        this.target = target == null ? parser : target;
        this.parser = parser;
        this.list = list;

        checker = new AccessChecker(this.text, flags);
    }

    private String parseOperatorsAndValues(String string) {
        if (operators == null || operators.length == 0)
            return string;

        for (UnaryOperator<String> o : operators) {
            if (o == null) continue;
            string = o.apply(string);
        }

        string = replacePlayerValues(text, parser, string, true);
        return replaceInsensitiveEach(string, keys, values);
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

    private void registerValues() {
        if (isRegistered) return;

        List<String> list = new ArrayList<>();
        if (this.list.isEmpty()) return;

        for (String s : this.list) {
            if (s == null) continue;
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
            String s = parsePAPI(parser, parseChars(text, string));
            return IridiumAPI.stripAll(stripJson(s));
        }

        return colorize(text, target, parser, string);
    }

    public void display() {
        registerValues(); // register the values and operators
        if (list.isEmpty()) return;

        for (String s : list) {
            if (checker.isTitle(s)) {
                Matcher m = Pattern.compile(text.titleRegex(true)).matcher(s);

                String t = null;
                try {
                    if (m.find()) t = m.group(1).substring(1);
                } catch (Exception ignored) {}

                int[] ticks = text.defaultTitleTicks();
                int time = ticks[1];

                try {
                    if (t != null) time = Integer.parseInt(t) * 20;
                } catch (Exception ignored) {}

                String temp = parseFormat(text.titleRegex(true), s, true);

                sendTitle(target,
                        temp.split(text.lineSeparator()),
                        ticks[0], time, ticks[2]
                );
            }
            else if (checker.isActionBar(s)) {
                String temp = parseFormat(text.actionBarRegex(true), s, true);
                sendActionBar(target, temp);
            }
            else if (checker.isJson(s)) {
                try {
                    String cmd = parseFormat(text.jsonRegex(true), s, false);
                    new JSONObject(cmd);
                    // checks if is a valid JSON format

                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "minecraft:tellraw " +
                            target.getName() + " " + cmd
                    );
                } catch (Exception ignored) {}
            }
            else if (checker.isBossbar(s)) {
                new Bossbar(text.getPlugin(), target, s).display();
            }
            else if (checker.isWebhook(s)) {
                ConfigurationSection id = text.getWebhookSection();

                List<String> list = new ArrayList<>(id.getKeys(false));
                if (list.isEmpty()) continue;

                Matcher m = Pattern.compile(text.webhookRegex(true)).matcher(s);
                String n = m.find() ? m.group(1) : null;

                String line = parseFormat(text.webhookRegex(true), s, false);

                if (n == null) {
                    n = list.get(0);
                    new Webhook(id.getConfigurationSection(n), line).send();
                    continue;
                }

                for (String l : list) if (n.equals(l)) n = l;
                new Webhook(id.getConfigurationSection(n), line).send();
            }
            else if (checker.isChat())
                new JsonMessage(text, target, parser, removeSpace(s)).send();
        }
    }

    public static void display(Player target, Player parser, List<String> list, String[] keys, String[] values) {
        new Displayer(null, target, parser, list).setKeys(keys).setValues(values).display();
    }

    public static void display(Player target, Player parser, List<String> list) {
        new Displayer(null, target, parser, list).display();
    }

    private static class AccessChecker {

        private final TextKeys keys;
        private final Set<String> flags;

        AccessChecker(TextKeys keys, String... flags) {
            this.keys = keys;
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
            return (allow) && s.matches(keys.actionBarRegex(true));
        }

        public boolean isTitle(String s) {
            boolean allow = isAll() || flags.contains(TITLE);
            return (allow) && s.matches(keys.titleRegex(true));
        }

        public boolean isBossbar(String s) {
            boolean allow = isAll() || flags.contains(BOSSBAR);
            allow = (allow) && s.matches(keys.bossbarRegex(true));
            return allow && keys.getPlugin() != null;
        }

        public boolean isJson(String s) {
            boolean allow = isAll() || flags.contains(JSON);
            return (allow) && s.matches(keys.jsonRegex(true));
        }

        public boolean isWebhook(String s) {
            boolean allow = isAll() || flags.contains(WEBHOOK);
            allow = (allow) && s.matches(keys.webhookRegex(true));
            return allow && keys.getWebhookSection() != null;
        }
    }
}
