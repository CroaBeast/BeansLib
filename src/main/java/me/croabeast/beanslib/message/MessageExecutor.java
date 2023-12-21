package me.croabeast.beanslib.message;

import lombok.Getter;
import me.croabeast.beanslib.Beans;
import me.croabeast.beanslib.applier.StringApplier;
import me.croabeast.beanslib.discord.Webhook;
import me.croabeast.beanslib.key.PlayerKey;
import me.croabeast.beanslib.misc.BossbarBuilder;
import me.croabeast.beanslib.misc.Regex;
import me.croabeast.beanslib.reflect.ActionBarHandler;
import me.croabeast.beanslib.reflect.TitleHandler;
import me.croabeast.beanslib.utility.Exceptions;
import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.neoprismatic.NeoPrismaticAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An abstract class that represents a message executor for different types of messages.
 *
 * <p> A message executor can execute a message for a target player and a parser player,
 * based on the message flag and the input string, can also format the input string by
 * replacing keys, placeholders, and colors.
 *
 * <p> This class can not have more instances or child classes to avoid errors with the
 * existing keys.
 *
 * @see MessageFlag
 * @see MessageSender
 */
public abstract class MessageExecutor {

    private static final Map<MessageFlag, MessageExecutor> MAP, DEFS;

    private static String[] delimiters = {"[", "]"};
    private static int[] titleTicks = {8, 50, 8};

    static {
        MAP = new LinkedHashMap<>();
        DEFS = new LinkedHashMap<>();
    }

    /**
     * The {@link MessageFlag} object of this executor.
     */
    @Getter
    private final MessageFlag flag;
    @Regex private String regex;

    private boolean color = false;

    /**
     * A message executor for the action bar message flag.
     *
     * <p> It sends a message to the target player's action bar, which is the area above the hot bar.
     * It also colorizes the input string.
     */
    public static final MessageExecutor ACTION_BAR = new MessageExecutor(MessageFlag.ACTION_BAR) {
        @Override
        public boolean execute(Player target, Player parser, String input) {
            try {
                return ActionBarHandler.send(target, formatString(target, parser, input));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }.doColor();

    /**
     * A message executor for the title message flag.
     *
     * <p> It sends a message to the target player's title, which is a large text that appears
     * in the center of the screen for a short duration. It also colorizes the input string.
     *
     * <p> The input string can have an optional number after the flag, which specifies the
     * duration of the title in seconds.
     *
     * <p> The input string can also have two lines, separated by a newline character, which
     * represent the main title and the subtitle.
     */
    public static final MessageExecutor TITLE = new MessageExecutor(MessageFlag.TITLE, "(:\\d+)?") {
        @Override
        public boolean execute(Player target, Player parser, String input) {
            Matcher m1 = getPattern().matcher(input);
            String tm = null;

            try {
                if (m1.find()) tm = m1.group(1).substring(1);
            } catch (Exception ignored) {}

            int[] a = titleTicks;
            int time = a[1];

            try {
                if (tm != null)
                    time = Integer.parseInt(tm) * 20;
            } catch (Exception ignored) {}

            String[] temp = Beans.splitLine(formatString(target, parser, input));
            String sub = temp.length > 1 ? temp[1] : "";

            try {
                return TitleHandler.send(target, temp[0], sub, a[0], time, a[2]);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }.doColor();

    /**
     * A message executor for the webhook message flag.
     *
     * <p> It sends a message to a webhook, which is a URL that can receive and process data
     * from other sources.
     *
     * <p> The input string can have an optional path after the flag, which specifies the
     * configuration section in the BeansLib instance ({@link Beans#getWebhookSection()}).
     *
     * <p> If no path is specified, the first webhook section in the config file is used.
     */
    public static final MessageExecutor WEBHOOK = new MessageExecutor(MessageFlag.WEBHOOK, "(:.+)?") {
        @Override
        public boolean execute(Player target, Player parser, String input) {
            ConfigurationSection id = Beans.getWebhookSection();
            if (id == null) return false;

            List<String> list = new ArrayList<>(id.getKeys(false));
            if (list.isEmpty()) return false;

            Matcher m3 = getPattern().matcher(input);
            String line = formatString(target, parser, input);

            String path = list.get(0);

            if (m3.find()) {
                String[] split = m3.group().replace(delimiters[0], "")
                        .replace(delimiters[1], "").split(":", 2);

                String temp = split.length == 2 ? split[1] : null;
                if (temp != null) path = temp;
            }

            return (id = id.getConfigurationSection(path)) != null
                    && new Webhook(id, line).send();
        }
    };

    /**
     * A message executor for the JSON message flag.
     *
     * <p> It sends a message to the target player that is formatted as a JSON object, which allows
     * for advanced customization of the text, such as color, style, and click events.
     *
     * <p> The input string must be a valid JSON object that follows the Minecraft tellraw format.
     *
     * @see <a href="https://minecraft.wiki/w/Raw_JSON_text_format">Minecraft tellraw format</a>
     */
    public static final MessageExecutor JSON = new MessageExecutor(MessageFlag.JSON) {
        @Override
        public boolean execute(Player target, Player parser, String input) {
            if (StringUtils.isBlank(input)) return false;

            try {
                Exceptions.checkPlayer(target);

                Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(), "minecraft:tellraw " +
                                target.getName() + " " +
                                formatString(target, parser, input)
                );
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    };

    /**
     * A message executor for the bossbar message flag.
     *
     * <p> It sends a message to the target player's boss bar, which is the area at the
     * top of the screen that shows the health of a boss entity.
     *
     * <p> The input string can have an optional path after the flag, which specifies the
     * configuration section in the BeansLib instance ({@link Beans#getBossbarSection()}).
     *
     * <p> If no path is specified, the input string is used as the text of the boss bar.
     */
    public static final MessageExecutor BOSSBAR = new MessageExecutor(MessageFlag.BOSSBAR, "(:.+)?") {
        @Override
        public boolean execute(Player target, Player parser, String input) {
            Plugin plugin = Beans.getPlugin();
            Matcher m2 = Beans.getBossbarPattern().matcher(input);

            try {
                if (m2.find()) {
                    ConfigurationSection c = Beans.getBossbarSection();
                    if (c == null) return false;

                    c = c.getConfigurationSection(m2.group(1));
                    if (c == null) return false;

                    return new BossbarBuilder(plugin, target, c).display();
                }

                return new BossbarBuilder(plugin, target, input).display();
            } catch (Exception e) {
                return false;
            }
        }
    };

    /**
     * A message executor for the chat message flag.
     *
     * <p> It sends a message to the target player's chat window, which is the
     * default type of message. It also colorizes the input string.
     */
    public static final MessageExecutor CHAT = new MessageExecutor(MessageFlag.CHAT) {
        @Override
        public MessageExecutor setRegex(@Regex String regex) {
            throw new UnsupportedOperationException("Setter is not supported on this instance");
        }

        @Override
        public boolean execute(Player target, Player parser, String input) {
            try {
                return new ChatMessageBuilder(target, parser, input).send();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }.doColor();

    private MessageExecutor(MessageFlag flag, @Regex String regex) {
        this.flag = flag;
        this.regex = regex;

        MAP.put(flag, this);
        DEFS.put(flag, copy());
    }

    private MessageExecutor(MessageFlag flag) {
        this(flag, null);
    }

    private MessageExecutor copy() {
        final MessageExecutor parent = this;

        return new MessageExecutor(parent.flag, parent.regex) {
            @Override
            public boolean execute(Player target, Player parser, String input) {
                return parent.execute(target, parser, input);
            }
        };
    }

    MessageExecutor doColor() {
        this.color = true;

        DEFS.put(flag, copy());
        return this;
    }

    /**
     * Sets the regex for this message executor.
     *
     * <p> The regex is used to match the input string after the message flag and
     * the delimiters.
     *
     * <p> The regex can be used to specify optional parameters or values for the
     * message executor.
     *
     * @param regex the regex for this message executor
     * @return this message executor with the new regex
     */
    public MessageExecutor setRegex(@Regex String regex) {
        this.regex = regex;
        return this;
    }

    /**
     * Executes the message for the target player and the parser player, based on the
     * message flag and the input string.
     *
     * @param target the player who receives the message
     * @param parser the player who parses the message
     * @param input the input string that contains the message
     *
     * @return true if the message was executed successfully, false otherwise
     */
    public abstract boolean execute(Player target, Player parser, String input);

    /**
     * Executes the message for the same player as both the target and the parser, based
     * on the message flag and the input string.
     *
     * <p> This method is a convenience method that calls the execute method with the same
     * player as both parameters.
     *
     * @param player the player who receives and parses the message
     * @param input the input string that contains the message
     *
     * @return true if the message was executed successfully, false otherwise
     */
    public boolean execute(Player player, String input) {
        return execute(player, player, input);
    }

    @Regex
    private String getRegex() {
        @Regex String f = flag.getName();

        if (StringUtils.isNotBlank(regex))
            f += regex;

        @Regex
        String d1 = Pattern.quote(delimiters[0]),
                d2 = Pattern.quote(delimiters[1]);

        return "(?i)^" + d1 + f + d2;
    }

    /**
     * Returns the pattern for this message executor, based on the message flag,
     * the regex, and the delimiters.
     *
     * <p> The pattern is used to match the input string and extract the relevant parts
     * for the message executor.
     *
     * @return the pattern for this message executor
     */
    public Pattern getPattern() {
        return Pattern.compile(getRegex());
    }

    String formatString(Player target, Player parser, String string) {
        final StringApplier applier = StringApplier.simplified(string);
        Matcher matcher = getPattern().matcher(string);

        while (matcher.find())
            applier.apply(s -> s.replace(matcher.group(), ""));

        applier.apply(TextUtils.STRIP_JSON);

        applier.apply(TextUtils.STRIP_FIRST_SPACES).
                apply(s -> PlayerKey.replaceKeys(parser, s)).
                apply(Beans::convertToSmallCaps);

        if (!color)
            applier.apply(s -> Beans.formatPlaceholders(parser, s)).
                    apply(NeoPrismaticAPI::stripAll);
        else
            applier.apply(s -> Beans.colorize(target, parser, s));

        return applier.toString();
    }

    /**
     * Returns the message executor that corresponds to the given message flag.
     *
     * @param flag the message flag to look for
     * @return the message executor that matches the flag
     */
    public static MessageExecutor fromFlag(MessageFlag flag) {
        return MAP.get(flag);
    }

    /**
     * Sets the delimiters for the message executors.
     *
     * <p> The delimiters are used to enclose the message flag and the optional
     * regex in the input string. The delimiters must not be blank.
     *
     * @param start the start delimiter
     * @param end the end delimiter
     */
    public static void setDelimiters(String start, String end) {
        delimiters = new String[] {
                Exceptions.validate(StringUtils::isNotBlank, start),
                Exceptions.validate(StringUtils::isNotBlank, end)
        };
    }

    /**
     * Sets the title ticks for the title message executor.
     *
     * <p> The title ticks are used to specify the duration of the title in ticks
     * (1 tick = 0.05 seconds).
     *
     * <p> Must have three elements: fade-in time, stay time, and fade-out time.
     * The stay time must be positive.
     *
     * @param in the fade-in time in ticks
     * @param stay the stay time in ticks
     * @param out the fade-out time in ticks
     */
    public static void setTitleTicks(int in, int stay, int out) {
        titleTicks = new int[] {in, Exceptions.validate(i -> i > 0, stay), out};
    }

    /**
     * Returns the message executor that matches the given string, based on the message
     * flag name.
     *
     * <p> This method is used to convert a string to a message flag and then to a message
     * executor.
     *
     * @param string the string to match
     * @return the message executor that matches the string, or {@link #CHAT} if none
     */
    @NotNull
    public static MessageExecutor matchKey(String string) {
        return fromFlag(MessageFlag.from(string));
    }

    /**
     * Returns the message executor that identifies the given string, based on the message
     * flag pattern.
     *
     * <p> This method is used to find the message executor that has the pattern that matches
     * the input string.
     *
     * @param string the string to identify
     * @return the message executor that identifies the string, or {@link #CHAT} if none
     */
    @NotNull
    public static MessageExecutor identifyKey(String string) {
        if (StringUtils.isBlank(string)) return CHAT;

        for (MessageExecutor e : MAP.values())
            if (e.getPattern().matcher(string).find())
                return e;

        if (Beans.getBossbarPattern()
                .matcher(string).find()) return BOSSBAR;

        return CHAT;
    }

    /**
     * Sets the default values for the message executors. This method is used to restore
     * the original values of the message executors.
     */
    public static void setDefaults() {
        MAP.clear();
        MAP.putAll(DEFS);
    }
}
