package me.croabeast.beanslib.message;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.var;
import me.croabeast.beanslib.Beans;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.builder.BossbarBuilder;
import me.croabeast.beanslib.builder.ChatMessageBuilder;
import me.croabeast.beanslib.discord.Webhook;
import me.croabeast.beanslib.misc.StringApplier;
import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.neoprismatic.NeoPrismaticAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.croabeast.beanslib.utility.TextUtils.*;

/**
 * The {@code MessageExecutor} class manages how to identify a message type if it has
 * a respective registered prefix and an optional additional regex parameter to check
 * if more arguments will be needed to identify the message type.
 *
 * <p> Each default instance of this class have a setter for the main key and the regex,
 * if It's necessary to change those values.
 * The default key and regex of each instance depends mostly on the {@link Beans#getLoaded()}.
 *
 * <p> This class can not have more instances or child classes to avoid errors with the
 * existing keys.
 *
 * <p> See {@link MessageSender}, it uses the default instances to parse, format, and
 * send messages to players.
 *
 * @author CroaBeast
 * @since 1.4
 */
@Accessors(chain = true)
public abstract class MessageExecutor implements Cloneable {

    private static final HashMap<Integer, MessageExecutor>
            MESSAGE_EXECUTOR_MAP = new HashMap<>(), DEFAULT_EXECUTOR_MAP = new HashMap<>();

    /**
     * The {@link MessageExecutor} instance to identify action-bar messages.
     */
    public static final MessageExecutor ACTION_BAR_EXECUTOR = new MessageExecutor(MessageFlag.ACTION_BAR) {
        @Override
        public boolean execute(Player t, Player p, String s) {
            try {
                sendActionBar(t, formatString(t, p, s));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }.doColor();

    /**
     * The {@link MessageExecutor} instance to identify title and subtitle messages.
     *
     * <p> By default has a regex string to catch how many seconds the title will be displayed.
     */
    public static final MessageExecutor TITLE_EXECUTOR = new MessageExecutor(MessageFlag.TITLE, "(:\\d+)?") {
        @Override
        public boolean execute(Player t, Player p, String s) {
            var m1 = getPattern().matcher(s);
            String tm = null;

            try {
                if (m1.find()) tm = m1.group(1).substring(1);
            } catch (Exception ignored) {}

            int[] a = Beans.getDefaultTitleTicks();
            int time = a[1];

            try {
                if (tm != null)
                    time = Integer.parseInt(tm) * 20;
            } catch (Exception ignored) {}

            var t1 = formatString(t, p, s);

            try {
                sendTitle(t, Beans.splitLine(t1), a[0], time, a[2]);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }.doColor();

    /**
     * The {@link MessageExecutor} instance to identify discord webhook messages.
     *
     * <p> By default has a regex string to catch the configuration path of the webhook.
     */
    public static final MessageExecutor WEBHOOK_EXECUTOR = new MessageExecutor(MessageFlag.WEBHOOK, "(:.+)?") {
        @Override
        public boolean execute(Player t, Player parser, String s) {
            var id = Beans.getWebhookSection();
            if (id == null) return false;

            var list = new ArrayList<>(id.getKeys(false));
            if (list.isEmpty()) return false;

            var m3 = getPattern().matcher(s);
            var line = formatString(t, parser, s);

            var path = list.get(0);

            if (m3.find()) {
                var split = m3.group().
                        replace(Beans.getKeysDelimiters()[0], "").
                        replace(Beans.getKeysDelimiters()[1], "").
                        split(":", 2);

                var temp = split.length == 2 ? split[1] : null;
                if (temp != null) path = temp;
            }

            id = id.getConfigurationSection(path);
            if (id == null) return false;

            new Webhook(id, line).send();
            return true;
        }
    };

    /**
     * The {@link MessageExecutor} instance to identify vanilla json messages.
     */
    public static final MessageExecutor JSON_EXECUTOR = new MessageExecutor(MessageFlag.JSON) {
        @Override
        public boolean execute(Player t, Player parser, String s) {
            try {
                Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(), "minecraft:tellraw " +
                        t.getName() + " " + formatString(t, parser, s)
                );
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    };

    /**
     * The {@link MessageExecutor} instance to identify minecraft bossbar messages.
     *
     * <p> By default has a regex string to catch the configuration path of the custom bossbar,
     * or to catch the arguments of the bossbar message.
     */
    public static final MessageExecutor BOSSBAR_EXECUTOR = new MessageExecutor(MessageFlag.BOSSBAR, "(:.+)?") {
        @Override
        public boolean execute(Player t, Player parser, String s) {
            var plugin = Beans.getPlugin();
            var m2 = Beans.getBossbarPattern().matcher(s);

            if (m2.find()) {
                var c = Beans.getBossbarSection();
                if (c == null) return false;

                c = c.getConfigurationSection(m2.group(1));
                if (c == null) return false;

                new BossbarBuilder(plugin, t, c).display();
                return true;
            }

            new BossbarBuilder(plugin, t, s).display();
            return true;
        }
    };

    /**
     * The {@link MessageExecutor} instance to identify default chat messages.
     *
     * <p> It's not necessary to identify a string with this prefix; because
     * if a string doesn't have a prefix, it will by default a chat message.
     *
     * <p> The setters of this instance will throw an {@link UnsupportedOperationException}.
     */
    public static final MessageExecutor CHAT_EXECUTOR = new MessageExecutor(MessageFlag.CHAT) {
        private static final String MSG_EX = "Setter is not supported on this instance";

        @Override
        public MessageExecutor setRegex(String regex) {
            throw new UnsupportedOperationException(MSG_EX);
        }

        @Override
        public boolean execute(Player t, Player parser, String s) {
            try {
                return new ChatMessageBuilder(t, parser, s).send();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }.doColor();

    private static int ordinal = 0;

    /**
     * The flag of this object to identify the message and check if it's allowed.
     */
    @Getter
    private final MessageFlag flag;
    private final int index;
    /**
     * The optional regex string to catch more arguments.
     */
    @Setter
    private String regex;

    private boolean color = false;

    private MessageExecutor(MessageFlag flag, String regex) {
        this.flag = flag;
        this.regex = regex;
        index = ordinal;

        MESSAGE_EXECUTOR_MAP.put(ordinal, this);
        DEFAULT_EXECUTOR_MAP.put(ordinal, clone());

        ordinal++;
    }

    private MessageExecutor(MessageFlag flag) {
        this(flag, null);
    }

    MessageExecutor doColor() {
        this.color = true;

        DEFAULT_EXECUTOR_MAP.put(index, clone());
        return this;
    }

    /**
     * Creates and returns a copy of this key.
     *
     * @return a clone of this instance
     */
    @NotNull
    protected MessageExecutor clone() {
        try {
            return (MessageExecutor) super.clone();
        } catch (Exception e) {
            // this shouldn't happen, since keys are Cloneable
            return this;
        }
    }

    /**
     * Executes the defined action of this representation.
     *
     * @param target a target player
     * @param parser a player to parse arguments
     * @param string an input string
     *
     * @return true if was executed, false otherwise
     */
    public abstract boolean execute(Player target, Player parser, String string);

    /**
     * Executes the defined action of this representation.
     *
     * @param parser a player to parse arguments
     * @param string an input string
     *
     * @return true if was executed, false otherwise
     */
    public boolean execute(Player parser, String string) {
        return execute(parser, parser, string);
    }

    private String getRegex() {
        String f = flag.getName() + (StringUtils.isBlank(regex) ? "" : regex);
        String[] del = Beans.getKeysDelimiters();
        return "(?i)^" + Pattern.quote(del[0]) + f + Pattern.quote(del[1]);
    }

    /**
     * Returns a {@link Pattern} instance using the defined key and the
     * optional defined regex.
     *
     * <p> It always checks the start of a string and adds the
     * {@link BeansLib#keysDelimiters} on the start and end of the regex.
     *
     * @return the requested pattern
     */
    public Pattern getPattern() {
        return Pattern.compile(getRegex());
    }

    String formatString(Player target, Player parser, String string) {
        StringApplier applier = StringApplier.of(string);
        Matcher matcher = getPattern().matcher(string);

        while (matcher.find())
            applier.apply(s -> s.replace(matcher.group(), ""));

        applier.apply(STRIP_JSON).apply(STRIP_FIRST_SPACES).
                apply(s -> Beans.parsePlayerKeys(parser, s)).
                apply(Beans::convertToSmallCaps);

        if (!color)
            applier.apply(s -> Beans.formatPlaceholders(parser, s)).
                    apply(NeoPrismaticAPI::stripAll);
        else
            applier.apply(s -> Beans.colorize(target, parser, s));

        return applier.toString();
    }

    /**
     * Returns the key instance of an input string to check what message type
     * is the string. If there is no defined type, will return the {@link #CHAT_EXECUTOR}.
     *
     * @param s an input string
     * @return the requested message key
     */
    @NotNull
    public static MessageExecutor identifyKey(String s) {
        if (StringUtils.isBlank(s)) return CHAT_EXECUTOR;

        for (var key : MESSAGE_EXECUTOR_MAP.values())
            if (key.getPattern().matcher(s).find()) return key;

        if (Beans.getBossbarPattern().
                matcher(s).find()) return BOSSBAR_EXECUTOR;

        return CHAT_EXECUTOR;
    }

    /**
     * Returns the key instance of an input key that could match with any existing
     * defined key. If there is no match, will return the {@link #CHAT_EXECUTOR}.
     *
     * @param k an input string
     * @return the requested message key
     */
    @NotNull
    public static MessageExecutor matchKey(String k) {
        if (StringUtils.isBlank(k)) return CHAT_EXECUTOR;

        for (var key : MESSAGE_EXECUTOR_MAP.values())
            if (k.matches("(?i)" + key.getFlag())) return key;

        return CHAT_EXECUTOR;
    }

    /**
     * Rollbacks any change in the default keys stored.
     * <p> Usefully for reload methods that depend on cache.
     */
    public static void setDefaults() {
        MESSAGE_EXECUTOR_MAP.clear();
        MESSAGE_EXECUTOR_MAP.putAll(DEFAULT_EXECUTOR_MAP);
    }
}