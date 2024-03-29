package me.croabeast.beanslib.message;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.croabeast.beanslib.Beans;
import me.croabeast.beanslib.applier.StringApplier;
import me.croabeast.beanslib.key.PlayerKey;
import me.croabeast.beanslib.key.ValueReplacer;
import me.croabeast.beanslib.map.Entry;
import me.croabeast.beanslib.utility.ArrayUtils;
import me.croabeast.beanslib.utility.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;

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
 * <p> See {@link #send(List)} and/or {@link #send(String...)} for more info.
 *
 * @author CroaBeast
 * @since 1.3
 */
@Accessors(chain = true)
@Getter @Setter
public class MessageSender {

    @NotNull
    private static MessageSender loaded = new MessageSender();

    @Getter(value = AccessLevel.NONE)
    private final Set<CommandSender> targets = new HashSet<>();

    @Getter(value = AccessLevel.NONE)
    private final List<KeyValue<?>> entries = new LinkedList<>();
    @Getter(value = AccessLevel.NONE)
    private final List<PlayerFunction> functions = new LinkedList<>();

    @Getter(value = AccessLevel.NONE)
    private final Set<MessageFlag> flags = new HashSet<>();

    /**
     * The player object to parse all the internal and global placeholders, and
     * to format the message with the player client' color support.
     */
    private Player parser;

     /**
     * If messages can be sent into the console or not.
     */
    private boolean logger = true;

    /**
     * If the input defined keys in this object are case-sensitive or not
     * if input keys were set.
     */
    private boolean sensitive = true;

    @Getter(value = AccessLevel.NONE)
    private boolean trimFirstSpaces = false;

    /**
     * Creates a new sender with a defined collection of targets and a player
     * that parses messages.
     *
     * @param targets a collection of targets
     * @param parser a player to parse messages
     */
    public MessageSender(Collection<? extends CommandSender> targets, Player parser) {
        if (!targets.isEmpty()) this.targets.addAll(targets);
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
                sender != null ? ArrayUtils.toList(sender) : new HashSet<>(),
                sender instanceof Player ? (Player) sender : null
        );
    }

    /**
     * Creates a new sender without any argument defined. It's recommended
     * to define the targets, parser, keys, values and other parameters before
     * sending a list or array.
     */
    public MessageSender() {
        this(new HashSet<>(), null);
    }

    public MessageSender(MessageSender sender) {
        Objects.requireNonNull(sender);

        this.parser = sender.parser;

        targets.addAll(sender.targets);
        flags.addAll(sender.flags);
        entries.addAll(sender.entries);
        functions.addAll(sender.functions);

        sensitive = sender.sensitive;
        logger = sender.logger;
        trimFirstSpaces = sender.trimFirstSpaces;
    }

    /**
     * The collection or array of targets that messages will be sent.
     * If empty or null, the output will be on the console.
     *
     * @param targets a collection of targets
     * @return a reference of this object
     */
    public MessageSender setTargets(Collection<? extends CommandSender> targets) {
        if (targets == null || targets.isEmpty())
            return this;

        this.targets.clear();
        this.targets.addAll(targets);
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
                        ArrayUtils.toList(targets)
        );
    }

    /**
     * Adds new player-string functions to apply them in every string of the list
     * in {@link #send(List)}.
     *
     * @param functions an array of functions
     * @return a reference of this object
     */
    public final MessageSender addFunctions(PlayerFunction... functions) {
        this.functions.addAll(ArrayUtils.toList(functions));
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
        try {
            ArrayUtils.toList(ops).forEach(u -> {
                if (u != null)
                    functions.add((p, s) -> u.apply(s));
            });
        } catch (Exception ignored) {}
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
        if (ArrayUtils.isArrayEmpty(flags))
            return this;

        this.flags.clear();
        this.flags.addAll(ArrayUtils.toList(flags));

        return this;
    }

    public <T> MessageSender addKeyValue(Entry<String, T> entry) {
        entries.add(new KeyValue<>(entry.getKey(), entry.getValue()));
        return this;
    }

    public <T> MessageSender addKeyValue(Supplier<Entry<String, T>> supplier) {
        return addKeyValue(supplier.get());
    }

    public <T> MessageSender addKeyValue(String key, T value) {
        if (StringUtils.isBlank(key))
            throw new NullPointerException();

        Objects.requireNonNull(value);
        return addKeyValue(new KeyValue<>(key, value));
    }

    @SafeVarargs
    public final <T> MessageSender addKeysValues(String[] keys, T... values) {
        if (!ValueReplacer.isApplicable(keys, values))
            throw new NullPointerException("Keys/Values are not applicable for replacements.");

        for (int i = 0; i < keys.length; i++)
            try {
                addKeyValue(keys[i], values[i]);
            } catch (Exception ignored) {}

        return this;
    }

    private void checkOrIncreaseCapacity(int length) {
        if (!entries.isEmpty() && entries.size() >= length)
            return;

        List<KeyValue<?>> list = entries;
        List<KeyValue<?>> result = new ArrayList<>(length);

        for (int i = 0; i < list.size(); i++)
            result.set(i, list.get(i));

        entries.clear();
        entries.addAll(new LinkedList<>(result));
    }

    /**
     * Sets the string keys to be replaced for the input values.
     *
     * @param keys an array of keys
     * @return a reference of this object
     */
    @Deprecated
    public MessageSender setKeys(String... keys) {
        if (ArrayUtils.isArrayEmpty(keys))
            return this;

        checkOrIncreaseCapacity(keys.length);
        final int size = entries.size();

        for (int i = 0; i < size; i++) {
            KeyValue<?> o = entries.get(i);
            String k = keys[i];

            entries.set(i, new KeyValue<>(k, o.getValue()));
        }
        return this;
    }

    private static List<String> listFromObject(Object o) {
        if (o == null) return ArrayUtils.toList("null");

        if (o instanceof CommandSender)
            return ArrayUtils.toList(((CommandSender) o).getName());

        if (o.getClass().isArray()) {
            List<String> result = new ArrayList<>();
            for (Object element : (Object[]) o)
                result.addAll(listFromObject(element));
            return result;
        }

        if (Iterable.class.isAssignableFrom(o.getClass())) {
            List<String> result = new ArrayList<>();
            for (Object value : (Iterable<?>) o)
                result.addAll(listFromObject(value));
            return result;
        }

        return ArrayUtils.toList(String.valueOf(o));
    }

    @SafeVarargs
    private static <T> String[] createValueArray(T... array) {
        List<String> list = new ArrayList<>();

        for (T o : array) list.addAll(listFromObject(o));
        return list.toArray(new String[0]);
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
    @Deprecated
    public final <T> MessageSender setValues(T... values) {
        String[] result = createValueArray(values);

        if (ArrayUtils.isArrayEmpty(result))
            return this;

        checkOrIncreaseCapacity(result.length);
        int size = entries.size();

        for (int i = 0; i < size; i++) {
            KeyValue<?> o = entries.get(i);
            Object v = result[i];

            entries.set(i, new KeyValue<>(o.getKey(), v));
        }
        return this;
    }

    private String formatString(Player p, String string) {
        final StringApplier applier = StringApplier.simplified(string);
        final boolean c = isSensitive();

        for (BiFunction<Player, String, String> f : functions)
            applier.apply(s -> f.apply(p, s));

        applier.apply(s -> PlayerKey.replaceKeys(p, s, c));
        for (KeyValue<?> k : entries)
            applier.apply(s -> k.replace(s, c));

        return applier.toString();
    }

    private boolean isFlag(MessageFlag flag) {
        return flags.isEmpty() || flags.contains(flag);
    }

    private boolean sendWebhook(String s, boolean output) {
        MessageExecutor key = MessageExecutor.identifyKey(s);

        if (key == MessageExecutor.WEBHOOK &&
                isFlag(MessageFlag.WEBHOOK)) key.execute(getParser(), s);

        Beans.rawLog(formatString(getParser(), s));
        return output;
    }

    private boolean sendWebhooks(List<String> list, boolean output) {
        list.forEach(s -> sendWebhook(s, true));
        return output;
    }

    /**
     * Returns ff all chat messages will remove all the first space characters.
     * @return if chat messages will remove all the first spaces
     */
    public boolean shouldTrimSpaces() {
        return trimFirstSpaces;
    }

    /**
     * Sends an input string to the defined targets of the sender.
     *
     * @param string an input string to send
     * @return true if the string was sent, false otherwise
     */
    public boolean singleSend(String string) {
        if (string == null) return false;

        StringApplier applier = StringApplier.simplified(string);
        applier.apply(s -> Beans.replacePrefixKey(s, false));

        if (targets.isEmpty())
            return sendWebhook(applier.toString(), true);

        List<Player> targets = new ArrayList<>();

        for (CommandSender t : this.targets)
            if (t instanceof Player) targets.add((Player) t);

        if (targets.isEmpty()) return sendWebhook(string, false);

        Matcher m = Beans.getBlankPattern().matcher(string);
        boolean isMatching = m.find();
        int count = isMatching ? Integer.parseInt(m.group(1)) : 0;

        isMatching = isMatching && count > 0;

        MessageExecutor ex = MessageExecutor.identifyKey(string);
        if (!isFlag(ex.getFlag())) return false;

        boolean notSend = true;

        for (Player t : targets) {
            if (isMatching) {
                for (int i = 0; i < count; i++) t.sendMessage("");
                continue;
            }

            Player parser = getParser() == null ? t : getParser();

            StringApplier temp = StringApplier.simplified(applier);
            temp.apply(s -> formatString(getParser(), s));

            if (shouldTrimSpaces() && ex == MessageExecutor.CHAT)
                temp.apply(TextUtils.STRIP_FIRST_SPACES);

            boolean b = ex.execute(t, parser, temp.toString());
            if (notSend && b) notSend = false;
        }

        if (notSend) return false;

        if (isLogger()) {
            applier.apply(s -> {
                boolean is = getParser() == null && targets.size() == 1;
                return formatString(is ? targets.get(0) : getParser(), s);
            });

            Beans.rawLog(applier.toString());
        }
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
        if (stringList == null || stringList.isEmpty()) return false;

        final List<String> list = new ArrayList<>();

        for (String s : stringList)
            if (s != null) list.add(Beans.replacePrefixKey(s, false));

        if (list.size() == 1) {
            final String temp = list.get(0);
            return !StringUtils.isBlank(temp) && singleSend(temp);
        }

        if (targets.isEmpty()) return sendWebhooks(list, true);

        Set<Player> targets = new HashSet<>();

        for (CommandSender t : this.targets)
            if (t instanceof Player) targets.add((Player) t);

        if (targets.isEmpty()) return sendWebhooks(list, false);

        List<String> logList = new ArrayList<>();

        for (String s : list) {
            Matcher m = Beans.getBlankPattern().matcher(s);

            boolean isMatching = m.find();
            int count = isMatching ?
                    Integer.parseInt(m.group(1)) : 0;

            isMatching = isMatching && count > 0;

            MessageExecutor e = MessageExecutor.identifyKey(s);
            if (!isFlag(e.getFlag())) continue;

            List<Boolean> executed = new ArrayList<>();

            for (Player t : targets) {
                if (isMatching) {
                    for (int i = 0; i < count; i++) t.sendMessage("");
                    continue;
                }

                Player temp = getParser() == null ? t : getParser();
                String  p = formatString(temp, s);

                executed.add(e.execute(t, temp,
                        shouldTrimSpaces() && e == MessageExecutor.CHAT ?
                        TextUtils.STRIP_FIRST_SPACES.apply(p) : p
                ));
            }

            if (executed.stream().noneMatch(b -> b)) continue;

            logList.add(formatString(getParser() == null &&
                    targets.size() == 1 ?
                    targets.toArray(new Player[0])[0] : getParser(), s
            ));
        }

        if (isLogger() && logList.size() > 0)
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
        return send(ArrayUtils.toList(strings));
    }

    /**
     * Creates and returns a copy of this sender.
     *
     * @return a clone of this instance
     */
    @SuppressWarnings("all")
    @NotNull
    public MessageSender clone() {
        return new MessageSender(this);
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

    @RequiredArgsConstructor
    @Getter
    static class KeyValue<T> implements Entry<String, T> {

        private final String key;
        private final T value;

        private String replace(String string, boolean sensitive) {
            return ValueReplacer.of(key, String.valueOf(value), string, sensitive);
        }
    }

    public interface PlayerFunction extends BiFunction<Player, String, String> {}
}
