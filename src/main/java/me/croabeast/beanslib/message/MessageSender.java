package me.croabeast.beanslib.message;

import com.google.common.collect.Lists;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.var;
import me.croabeast.beanslib.Beans;
import me.croabeast.beanslib.key.ValueReplacer;
import me.croabeast.beanslib.utility.ArrayUtils;
import me.croabeast.beanslib.utility.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * <p> The {@code MessageSender} class represents the action to display a
 * list of messages to a player using {@link MessageExecutor} instances for parse
 * different message types and format every message type respectively.
 *
 * <pre> {@code
 * // Creating an instance
 * MessageSender sender = new MessageSender(
 *       Bukkit.getOnlinePlayers(), // can be single or null
 *       Bukkit.getPlayer("Markiplier")
 * );
 *
 * // only chat and bossbar messages are allowed with
 * // these flags, this is optional
 * sender.setFlags(MessageFlag.CHAT, MessageFlag.BOSSBAR);
 *
 * // send multiple lists and arrays using the same instance
 * sender.send(false, plugin.getConfig().getStringList("path"));
 *
 * sender.send(false,
 *      "[C] <R:1>My message goes here, it's colorful</R>",
 *      "[BOSSBAR] Look, a bossbar message :o"
 * );
 * } </pre>
 *
 * Also, this objects implements the {@link Cloneable} interface to allow
 * cloning an instance, instead of creating new instances separately.
 *
 * <p> See {@link #send(List)} and/or {@link #send(String...)} for more info.
 *
 * @author CroaBeast
 * @since 1.3
 */
@Accessors(chain = true)
public final class MessageSender implements Cloneable {

    @NotNull
    private static MessageSender loaded = new MessageSender();

    private Collection<? extends CommandSender> targets = null;
    /**
     * The player object to parse all the internal and global placeholders, and
     * to format the message with the player client' color support.
     */
    @Setter
    private Player parser = null;

    private Set<MessageFlag> flags = new HashSet<>();

    private String[] keys = null, values = null;
    private final List<BiFunction<Player, String, String>> functions = new ArrayList<>();

    /**
     * Sets if messages can be sent into the console or not.
     */
    @Setter
    private boolean isLogger = true;

    /**
     * Sets if the input defined keys in this object are case-sensitive or not
     * if input keys were set.
     */
    @Setter
    private boolean caseSensitive = true;

    /**
     * Sets if all chat messages will remove all the first space characters.
     */
    @Setter
    private boolean noFirstSpaces = false;

    /**
     * Creates a new sender with a defined collection of targets and a player
     * that parses messages.
     *
     * @param targets a collection of targets
     * @param parser a player to parse messages
     */
    public MessageSender(Collection<? extends CommandSender> targets, Player parser) {
        this.targets = targets;
        this.parser = parser;
    }

    /**
     * Creates a new sender with a defined target, and if that sender is a
     * player, will act as the parser.
     *
     * @param sender a sender
     */
    public MessageSender(CommandSender sender) {
        this(
                sender != null ? Lists.newArrayList(sender) : null,
                sender instanceof Player ? (Player) sender : null
        );
    }

    /**
     * Creates a new sender without any argument defined. It's recommended
     * to define the targets, parser, keys, values and other parameters before
     * sending a list or array.
     */
    public MessageSender() {}

    public MessageSender(MessageSender sender) {
        Objects.requireNonNull(sender);

        this.parser = sender.parser;

        if (sender.targets != null)
            targets = new ArrayList<>(sender.targets);

        if (sender.flags != null)
            this.flags = new HashSet<>(sender.flags);

        final String[] k = sender.keys, v = sender.values;

        if (k != null)
            keys = Arrays.copyOf(k, k.length);
        if (v != null)
            values = Arrays.copyOf(v, v.length);

        this.caseSensitive = sender.caseSensitive;
        this.isLogger = sender.isLogger;
        this.noFirstSpaces = sender.noFirstSpaces;
    }

    /**
     * The collection or array of targets that messages will be sent.
     * If empty or null, the output will be on the console.
     *
     * @param targets a collection of targets
     * @return a reference of this object
     */
    public MessageSender setTargets(Collection<? extends CommandSender> targets) {
        this.targets = targets;
        return this;
    }

    /**
     * The collection or array of targets that messages will be sent. If null,
     * the output will be on the console.
     *
     * @param targets an array of targets
     * @return a reference of this object
     */
    public MessageSender setTargets(CommandSender... targets) {
        return setTargets(
                ArrayUtils.isArrayEmpty(targets) ?
                        null :
                        Lists.newArrayList(targets)
        );
    }

    /**
     * Adds new player-string functions to apply them in every string of the list
     * in {@link #send(List)}.
     *
     * @param functions an array of functions
     * @return a reference of this object
     */
    @SafeVarargs
    public final MessageSender addFunctions(BiFunction<Player, String, String>... functions) {
        this.functions.addAll(Lists.newArrayList(functions));
        return this;
    }

    /**
     * Adds new string operators to apply them in every string of the list
     * in {@link #send(List)}.
     *
     * @param ops an array of operators
     * @return a reference of this object
     */
    @SafeVarargs
    public final MessageSender addFunctions(UnaryOperator<String>... ops) {
        for (var u : Lists.newArrayList(ops))
            if (u != null) functions.add((p, s) -> u.apply(s));

        return this;
    }

    /**
     * Sets the flags of the displayer to allow certain type of messages.
     *
     * <p> Not invoking this method or invoking it without any arguments,
     * it will imply that all messages types are allowed.
     *
     * @param flags an array of flags
     * @return a reference of this object
     */
    public MessageSender setFlags(MessageFlag... flags) {
        this.flags = new HashSet<>(ArrayUtils.fromArray(flags));
        return this;
    }

    /**
     * Sets the string keys to be replaced for the input values.
     *
     * @param keys an array of keys
     * @return a reference of this object
     */
    public MessageSender setKeys(String... keys) {
        this.keys = keys;
        return this;
    }

    private static List<String> listFromObject(Object o) {
        if (o == null) return Lists.newArrayList("null");

        if (o instanceof CommandSender)
            return Lists.newArrayList(((CommandSender) o).getName());

        if (o.getClass().isArray()) {
            var result = new ArrayList<String>();

            for (var element : (Object[]) o)
                result.addAll(listFromObject(element));

            return result;
        }

        return Lists.newArrayList(String.valueOf(o));
    }

    /**
     * Sets the string values that replaces the input keys with any kind of object.
     * <p> Can detect players and entities to get its names.
     *
     * @param values an array of values to be replaced
     * @return a reference of this object
     *
     * @param <T> the clazz of any given value
     */
    @SafeVarargs
    public final <T> MessageSender setValues(T... values) {
        if (values == null || values.length == 0)
            return this;

        var list = new ArrayList<String>();
        for (T o : values) list.addAll(listFromObject(o));

        this.values = list.toArray(new String[0]);
        return this;
    }

    private String formatString(Player parser, String string) {
        final boolean c = caseSensitive;
        for (var f : functions) string = f.apply(parser, string);

        string = Beans.parsePlayerKeys(parser, string, c);
        return ValueReplacer.forEach(keys, values, string, c);
    }

    private boolean notFlag(MessageFlag flag) {
        return !flags.isEmpty() && !flags.contains(flag);
    }

    private boolean sendWebhook(String s, boolean output) {
        final var key = MessageExecutor.identifyKey(s);

        if (key == MessageExecutor.WEBHOOK_EXECUTOR &&
                !notFlag(MessageFlag.WEBHOOK)) key.execute(parser, s);

        Beans.rawLog(formatString(parser, s));
        return output;
    }

    private boolean sendWebhooks(List<String> list, boolean output) {
        list.forEach(s -> sendWebhook(s, output));
        return output;
    }

    /**
     * Sends an input string to the defined targets of the sender.
     *
     * @param string an input string to send
     * @return true if the string was sent, false otherwise
     */
    public boolean singleSend(String string) {
        if (string == null) return false;

        string = Beans.replacePrefixKey(string, false);

        if (targets == null || targets.isEmpty())
            return sendWebhook(string, true);

        var targets = new ArrayList<Player>();

        for (var t : this.targets)
            if (t instanceof Player) targets.add((Player) t);

        if (targets.isEmpty()) return sendWebhook(string, false);

        var m = Beans.getBlankPattern().matcher(string);

        var isMatching = m.find();
        var count = isMatching ? Integer.parseInt(m.group(1)) : 0;

        isMatching = isMatching && count > 0;

        var key = MessageExecutor.identifyKey(string);
        if (notFlag(key.getFlag())) return false;

        boolean notSend = true;

        for (var t : targets) {
            if (isMatching) {
                for (int i = 0; i < count; i++) t.sendMessage("");
                continue;
            }

            var temp = parser == null ? t : parser;
            var p = formatString(temp, string);

            boolean b = key.execute(t, temp,
                    noFirstSpaces && key == MessageExecutor.CHAT_EXECUTOR ?
                    TextUtils.STRIP_FIRST_SPACES.apply(p) : p
            );

            if (notSend && b) notSend = false;
        }

        if (notSend) return false;

        if (isLogger)
            Beans.rawLog(formatString(
                    parser == null && targets.size() == 1 ?
                    targets.get(0) : parser, string
            ));
        return true;
    }

    /**
     * Sends a string list to the defined targets of the sender.
     *
     * <p> If the list is empty, null or contains only one string and that string
     * is blank/empty, it will not be sent.
     *
     * @param stringList a string list to send
     * @return true if the list was sent, false otherwise
     */
    public boolean send(List<String> stringList) {
        final var list = new ArrayList<String>();

        for (var s : stringList)
            if (s != null) list.add(Beans.replacePrefixKey(s, false));

        if (list.isEmpty()) return false;
        if (list.size() == 1 &&
                StringUtils.isBlank(list.get(0))) return false;

        if (targets == null || targets.isEmpty())
            return sendWebhooks(list, true);

        var targets = new HashSet<Player>();

        for (var t : this.targets)
            if (t instanceof Player) targets.add((Player) t);

        if (targets.isEmpty()) return sendWebhooks(list, false);

        var logList = new ArrayList<String>();

        for (var s : list) {
            var m = Beans.getBlankPattern().matcher(s);

            var isMatching = m.find();
            int count = isMatching ?
                    Integer.parseInt(m.group(1)) : 0;

            isMatching = isMatching && count > 0;

            var key = MessageExecutor.identifyKey(s);
            if (notFlag(key.getFlag())) continue;

            List<Boolean> executed = new ArrayList<>();

            for (var t : targets) {
                if (isMatching) {
                    for (int i = 0; i < count; i++) t.sendMessage("");
                    continue;
                }

                var temp = parser == null ? t : parser;
                var p = formatString(temp, s);

                executed.add(key.execute(t, temp,
                        noFirstSpaces && key == MessageExecutor.CHAT_EXECUTOR ?
                        TextUtils.STRIP_FIRST_SPACES.apply(p) : p
                ));
            }

            if (executed.stream().noneMatch(b -> b)) continue;

            logList.add(formatString(parser == null &&
                    targets.size() == 1 ?
                    targets.toArray(new Player[0])[0] : parser, s
            ));
        }

        if (isLogger && logList.size() > 0)
            Beans.rawLog(logList.toArray(new String[0]));
        return true;
    }

    /**
     * Sends a string array to the defined targets of the sender.
     *
     * <p> If the array is empty, null or contains only one string and that string
     * is blank/empty, it will not be sent.
     *
     * @param strings a string array to send
     * @return true if the list was sent, false otherwise
     */
    public boolean send(String... strings) {
        return send(Lists.newArrayList(strings));
    }

    /**
     * Creates and returns a copy of this sender.
     *
     * @return a clone of this instance
     */
    @NotNull
    public MessageSender clone() {
        try {
            var sender = (MessageSender) super.clone();

            if (targets != null)
                sender.targets = new ArrayList<>(targets);

            sender.flags = new HashSet<>(flags);

            sender.functions.clear();
            sender.functions.addAll(new ArrayList<>(functions));

            final String[] k = keys, v = values;

            if (k != null)
                sender.keys = Arrays.copyOf(k, k.length);
            if (v != null)
                sender.values = Arrays.copyOf(v, v.length);

            return sender;
        }
        catch (Exception e) {
            // this shouldn't happen, since the sender is Cloneable
            return new MessageSender(this);
        }
    }

    /**
     * Sets the loaded static MessageSender instance with a new sender.
     *
     * @param sender a sender
     * @throws NullPointerException if sender is null
     */
    public static void setLoaded(MessageSender sender) {
        loaded = Objects.requireNonNull(sender);
    }

    /**
     * Returns a clone of the loaded static MessageSender instance.
     *
     * @return a clone of the static instance
     */
    @NotNull
    public static MessageSender fromLoaded() {
        return loaded.clone();
    }
}
