package me.croabeast.beanslib.message;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.var;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.builder.BossbarBuilder;
import me.croabeast.beanslib.builder.JsonBuilder;
import me.croabeast.beanslib.discord.Webhook;
import me.croabeast.iridiumapi.IridiumAPI;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import static me.croabeast.beanslib.utility.TextUtils.*;

/**
 * The {@code MessageKey} class manages how to identify a message type if it has
 * a respective registered prefix and an optional additional regex parameter to
 * check if more arguments will be needed to identify the message type.
 *
 * <p> Each default instance of this class have a setter for the main key and the
 * regex, if It's necessary to change those values.
 *
 * <p> The default key and regex of each  instance depends mostly on the
 * {@link BeansLib#getLoadedInstance()}.
 *
 * <p> This class can not have more instances or child classes to avoid errors with
 * the existing keys.
 *
 * <p> See {@link MessageSender}, it uses the default instances to parse, format, and send
 * messages to players.
 *
 * @author CroaBeast
 * @since 1.4
 */
@Accessors(chain = true)
@Setter
public abstract class MessageKey implements MessageAction, Cloneable {

    private static BeansLib getLib() {
        return BeansLib.getLoadedInstance();
    }

    private static final HashMap<Integer, MessageKey>
            MESSAGE_KEY_MAP = new HashMap<>(), DEFAULT_KEY_MAP = new HashMap<>();

    /**
     * The {@link MessageKey} instance to identify action-bar messages.
     */
    public static final MessageKey ACTION_BAR_KEY = new MessageKey("action-bar") {
        @Override
        public boolean execute(Player t, Player parser, String s) {
            try {
                sendActionBar(t, formatString(t, parser, s));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }.doColor();

    /**
     * The {@link MessageKey} instance to identify title and subtitle messages.
     *
     * <p> By default has a regex string to catch how many seconds the title will be displayed.
     */
    public static final MessageKey TITLE_KEY = new MessageKey("title", "(:\\d+)?") {
        @Override
        public boolean execute(Player t, Player parser, String s) {
            var m1 = getPattern().matcher(s);
            String tm = null;

            try {
                if (m1.find()) tm = m1.group(1).substring(1);
            } catch (Exception ignored) {}

            int[] a = getLib().getDefaultTitleTicks();
            int time = a[1];

            try {
                if (tm != null)
                    time = Integer.parseInt(tm) * 20;
            } catch (Exception ignored) {}

            var t1 = formatString(t, parser, s);

            try {
                sendTitle(t, getLib().splitLine(t1), a[0], time, a[2]);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }.doColor();

    /**
     * The {@link MessageKey} instance to identify discord webhook messages.
     *
     * <p> By default has a regex string to catch the configuration path of the webhook.
     */
    public static final MessageKey WEBHOOK_KEY = new MessageKey("webhook", "(:.+)?") {
        @Override
        public boolean execute(Player t, Player parser, String s) {
            var id = getLib().getWebhookSection();
            if (id == null) return false;

            var list = new ArrayList<>(id.getKeys(false));
            if (list.isEmpty()) return false;

            var m3 = getPattern().matcher(s);
            var line = formatString(t, parser, s);

            var path = list.get(0);

            if (m3.find()) {
                var split = m3.group().
                        replace(getLib().getKeysDelimiters()[0], "").
                        replace(getLib().getKeysDelimiters()[1], "").
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
     * The {@link MessageKey} instance to identify vanilla json messages.
     */
    public static final MessageKey JSON_KEY = new MessageKey("json") {
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
     * The {@link MessageKey} instance to identify minecraft bossbar messages.
     *
     * <p> By default has a regex string to catch the configuration path of the custom bossbar,
     * or to catch the arguments of the bossbar message.
     */
    public static final MessageKey BOSSBAR_KEY = new MessageKey("bossbar", "(:.+)?") {
        @Override
        public boolean execute(Player t, Player parser, String s) {
            var plugin = getLib().getPlugin();
            var m2 = getLib().getBossbarPattern().matcher(s);

            if (m2.find()) {
                var c = getLib().getBossbarSection();
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
     * The {@link MessageKey} instance to identify default chat messages.
     *
     * <p> It's not necessary to identify a string with this prefix; because
     * if a string doesn't have a prefix, it will by default a chat message.
     *
     * <p> The setters of this instance will throw an {@link UnsupportedOperationException}.
     */
    public static final MessageKey CHAT_KEY = new MessageKey("chat") {
        private static final String MSG_EX = "Setter is not supported on this instance";

        @Override
        public MessageKey setKey(String key) {
            throw new UnsupportedOperationException(MSG_EX);
        }

        @Override
        public MessageKey setRegex(String regex) {
            throw new UnsupportedOperationException(MSG_EX);
        }

        @Override
        public boolean execute(Player t, Player parser, String s) {
            try {
                return new JsonBuilder(t, parser, s).send();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }.doColor();

    private static int ordinal = 0;

    /**
     * The main key of this object to identify it.
     */
    @Getter
    private String key;
    private final int index;
    /**
     * The optional regex string to catch more arguments.
     */
    private String regex;

    @Setter(AccessLevel.NONE)
    private boolean color = false;

    private MessageKey(String key, String regex) {
        this.key = key;
        this.regex = regex;
        index = ordinal;

        MESSAGE_KEY_MAP.put(ordinal, this);
        DEFAULT_KEY_MAP.put(ordinal, clone());

        ordinal++;
    }

    private MessageKey(String key) {
        this(key, null);
    }

    MessageKey doColor() {
        this.color = true;

        DEFAULT_KEY_MAP.put(index, clone());
        return this;
    }

    /**
     * Creates and returns a copy of this key.
     *
     * @return a clone of this instance
     */
    @NotNull
    protected MessageKey clone() {
        try {
            return (MessageKey) super.clone();
        } catch (Exception e) {
            // this shouldn't happen, since keys are Cloneable
            return this;
        }
    }

    /**
     * The main key of this object to identify it, but in upper-case.
     *
     * @return the key but in upper-case
     */
    public String getUpperKey() {
        return getKey().toUpperCase(Locale.ENGLISH);
    }

    private String getRegex() {
        var s = key + (StringUtils.isBlank(regex) ? "" : regex);

        return "(?i)^" +
                Pattern.quote(getLib().getKeysDelimiters()[0]) + s +
                Pattern.quote(getLib().getKeysDelimiters()[1]);
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

    String formatString(Player target, Player parser, String s) {
        var m = getPattern().matcher(s);

        while (m.find()) s = s.replace(m.group(), "");

        s = STRIP_FIRST_SPACES.apply(STRIP_JSON.apply(s));
        s = getLib().parsePlayerKeys(parser, s, false);

        if (!color)
            return IridiumAPI.stripAll(
                    PARSE_PLACEHOLDERAPI.apply(
                    parser, getLib().parseChars(s))
            );

        return getLib().colorize(target, parser, s);
    }

    /**
     * Returns the key instance of an input string to check what message type
     * is the string. If there is no defined type, will return the {@link #CHAT_KEY}.
     *
     * @param s an input string
     * @return the requested message key
     */
    @NotNull
    public static MessageKey identifyKey(String s) {
        if (StringUtils.isBlank(s)) return CHAT_KEY;

        for (var key : MESSAGE_KEY_MAP.values())
            if (key.getPattern().matcher(s).find()) return key;

        if (getLib().getBossbarPattern().
                matcher(s).find()) return BOSSBAR_KEY;

        return CHAT_KEY;
    }

    /**
     * Returns the key instance of an input key that could match with any existing
     * defined key. If there is no match, will return the {@link #CHAT_KEY}.
     *
     * @param k an input string
     * @return the requested message key
     */
    @NotNull
    public static MessageKey matchKey(String k) {
        if (StringUtils.isBlank(k)) return CHAT_KEY;

        for (var key : MESSAGE_KEY_MAP.values())
            if (k.matches("(?i)" + key.getKey())) return key;

        return CHAT_KEY;
    }

    /**
     * Rollbacks any change in the default keys stored.
     * <p> Usefully for reload methods that depend on cache.
     */
    public static void setDefaults() {
        MESSAGE_KEY_MAP.clear();
        MESSAGE_KEY_MAP.putAll(DEFAULT_KEY_MAP);
    }
}
