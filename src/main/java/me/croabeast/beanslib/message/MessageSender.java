package me.croabeast.beanslib.message;

import lombok.Setter;
import lombok.experimental.Accessors;
import me.croabeast.beanslib.Beans;
import me.croabeast.beanslib.key.PlayerKey;
import me.croabeast.beanslib.key.ValueReplacer;
import me.croabeast.beanslib.applier.StringApplier;
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
 * Also, this objects implements the {@link Cloneable} interface to allow
 * cloning an instance, instead of creating new instances separately.
 *
 * <p> See {@link #send(List)} and/or {@link #send(String...)} for more info.
 *
 * @author CroaBeast
 * @since 1.3
 */
@Accessors(chain = true)
public class MessageSender implements Cloneable {

    @NotNull
    private static MessageSender loaded = new MessageSender();

    private Collection<? extends CommandSender> targets = null;
    /**
     * The player object to parse all the internal and global placeholders, and
     * to format the message with the player client' color support.
     */
    @Setter
    private Player parser = null;

    private Set<MessageFlag> flags = new LinkedHashSet<>();

    private final List<KeyValue<?>> keyValues = new LinkedList<>();
    private final List<BiFunction<Player, String, String>> functions = new LinkedList<>();

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
                sender != null ? ArrayUtils.fromArray(sender) : null,
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
            flags = new HashSet<>(sender.flags);

        keyValues.clear();
        keyValues.addAll(sender.keyValues);

        functions.clear();
        functions.addAll(sender.functions);

        caseSensitive = sender.caseSensitive;
        isLogger = sender.isLogger;
        noFirstSpaces = sender.noFirstSpaces;
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
                        ArrayUtils.fromArray(targets)
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
        this.functions.addAll(ArrayUtils.fromArray(functions));
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
            ArrayUtils.fromArray(ops).forEach(u -> {
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
        try {
            this.flags = new HashSet<>(ArrayUtils.fromArray(flags));
        } catch (Exception ignored) {}
        return this;
    }

    public <T> MessageSender addKeyValue(Entry<String, T> entry) {
        keyValues.add(new KeyValue<>(entry.getKey(), entry.getValue()));
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

        final int size = keyValues.size();

        if (size <= 0 || size != keys.length)
            return this;

        for (int i = 0; i < size; i++) {
            KeyValue<?> o = keyValues.get(i);
            String k = keys[i];

            keyValues.set(i, new KeyValue<>(k, o.getValue()));
        }
        return this;
    }

    private static List<String> listFromObject(Object o) {
        if (o == null) return ArrayUtils.fromArray("null");

        if (o instanceof CommandSender)
            return ArrayUtils.fromArray(((CommandSender) o).getName());

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

        return ArrayUtils.fromArray(String.valueOf(o));
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

        int size = keyValues.size();

        if (size <= 0 || size != result.length)
            return this;

        for (int i = 0; i < size; i++) {
            KeyValue<?> o = keyValues.get(i);
            Object v = result[i];

            keyValues.set(i, new KeyValue<>(o.getKey(), v));
        }
        return this;
    }

    private String formatString(Player p, String string) {
        final StringApplier applier = StringApplier.simplified(string);
        final boolean c = caseSensitive;

        for (BiFunction<Player, String, String> f : functions)
            applier.apply(s -> f.apply(p, s));

        applier.apply(s -> PlayerKey.replaceKeys(p, s, c));
        for (KeyValue<?> k : keyValues)
            applier.apply(s -> k.replace(s, caseSensitive));

        return applier.toString();
    }

    private boolean isFlag(MessageFlag flag) {
        return flags.isEmpty() || flags.contains(flag);
    }

    private boolean sendWebhook(String s, boolean output) {
        MessageExecutor key = MessageExecutor.identifyKey(s);

        if (key == MessageExecutor.WEBHOOK_EXECUTOR &&
                isFlag(MessageFlag.WEBHOOK)) key.execute(parser, s);

        Beans.rawLog(formatString(parser, s));
        return output;
    }

    private boolean sendWebhooks(List<String> list, boolean output) {
        list.forEach(s -> sendWebhook(s, true));
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

        StringApplier applier = StringApplier.simplified(string);
        applier.apply(s -> Beans.replacePrefixKey(s, false));

        if (targets == null || targets.isEmpty())
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

            Player parser = this.parser == null ? t : this.parser;

            StringApplier temp = StringApplier.simplified(applier);
            temp.apply(s -> formatString(parser, s));

            if (noFirstSpaces && ex == MessageExecutor.CHAT_EXECUTOR)
                temp.apply(TextUtils.STRIP_FIRST_SPACES);

            boolean b = ex.execute(t, parser, temp.toString());
            if (notSend && b) notSend = false;
        }

        if (notSend) return false;

        if (isLogger) {
            applier.apply(s -> {
                boolean is = parser == null && targets.size() == 1;
                return formatString(is ? targets.get(0) : parser, s);
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
        if (stringList == null) return false;

        final List<String> list = new ArrayList<>();

        for (String s : stringList)
            if (s != null) list.add(Beans.replacePrefixKey(s, false));

        if (list.isEmpty()) return false;
        if (list.size() == 1 &&
                StringUtils.isBlank(list.get(0))) return false;

        if (targets == null || targets.isEmpty())
            return sendWebhooks(list, true);

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

            MessageExecutor key = MessageExecutor.identifyKey(s);
            if (!isFlag(key.getFlag())) continue;

            List<Boolean> executed = new ArrayList<>();

            for (Player t : targets) {
                if (isMatching) {
                    for (int i = 0; i < count; i++) t.sendMessage("");
                    continue;
                }

                Player temp = parser == null ? t : parser;
                String  p = formatString(temp, s);

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
        return send(ArrayUtils.fromArray(strings));
    }

    /**
     * Creates and returns a copy of this sender.
     *
     * @return a clone of this instance
     */
    @NotNull
    public MessageSender clone() {
        try {
            MessageSender sender = (MessageSender) super.clone();

            if (targets != null)
                sender.targets = new ArrayList<>(targets);

            sender.flags = new HashSet<>(flags);

            sender.keyValues.clear();
            sender.keyValues.addAll(new LinkedList<>(keyValues));

            sender.functions.clear();
            sender.functions.addAll(new LinkedList<>(functions));

            sender.caseSensitive = caseSensitive;
            sender.isLogger = isLogger;
            sender.noFirstSpaces = noFirstSpaces;

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

    public static <T> KeyValue<T> newKeyValue(String key, T value) {
        return new KeyValue<>(key, value);
    }

    static class KeyValue<T> extends Entry<String, T> {

        private KeyValue(String key, T value) {
            super(key, value);
        }

        private String replace(String string, boolean caseSensitive) {
            return ValueReplacer.of(key, String.valueOf(value), string, caseSensitive);
        }
    }
}
