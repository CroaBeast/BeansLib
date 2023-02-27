package me.croabeast.beanslib.message;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.key.ValueReplacer;
import me.croabeast.beanslib.utility.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * <p> The {@code MessageSender} class represents the action to display a
 * list of messages to a player using {@link MessageKey} instances for parse
 * different message types and format every message type respectively.
 *
 * <pre> {@code
 * // Creating an instance
 * MessageSender sender = new MessageSender(
 *       Bukkit.getOnlinePlayers(), // can be single or null
 *       Bukkit.getPlayer("Markiplier"),
 * );
 *
 * // only chat and bossbar messages are allowed with
 * // these flags, this is optional
 * sender.setFlags(MessageSender.CHAT, MessageSender.BOSSBAR);
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
 * See {@link #send(boolean, List)} and/or {@link #send(boolean, String...)} for more info.
 *
 * @author CroaBeast
 * @since 1.3
 */
@Accessors(chain = true)
public class MessageSender implements Cloneable {

    private static final BeansLib B_LIB = BeansLib.getLoadedInstance();

    /**
     * This flag allows to display action bar messages.
     */
    public static final String ACTION_BAR = "ACTION-BAR";
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

    private Collection<? extends CommandSender> targets = null;
    /**
     * The player object to parse all the internal and global placeholders, and
     * to format the message with the player client' color support.
     */
    @Setter
    private Player parser = null;

    private Set<String> flags = new HashSet<>();

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
     * Creates a new sender without any argument defined. It's recommended
     * to define the targets, parser, keys, values and other parameters before
     * sending a list or array.
     */
    public MessageSender() {}

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
     * The collection or array of targets that messages will be sent.
     * If empty or null, the output will be on the console.
     *
     * @param targets an array of targets
     * @param <T> a CommandSender child class
     *
     * @return a reference of this object
     */
    @SafeVarargs
    public final <T extends CommandSender> MessageSender setTargets(T... targets) {
        return setTargets(Lists.newArrayList(targets));
    }

    /**
     * Adds new player-string functions to apply them in every string of the list
     * in {@link #send(boolean, List)}.
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
     * in {@link #send(boolean, List)}.
     *
     * @param ops an array of operators
     * @return a reference of this object
     */
    @SafeVarargs
    public final MessageSender addFunctions(UnaryOperator<String>... ops) {
        for (UnaryOperator<String> u : Lists.newArrayList(ops))
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
    public MessageSender setFlags(String... flags) {
        this.flags = Sets.newHashSet(flags);
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

    /**
     * Sets the string values that replaces the input keys.
     *
     * @param values an array of values
     * @return a reference of this object
     */
    public MessageSender setValues(String... values) {
        this.values = values;
        return this;
    }

    private String parseOperatorsAndValues(Player parser, String string) {
        final boolean c = caseSensitive;

        for (BiFunction<Player, String, String> function : functions)
            string = function.apply(parser, string);

        string = B_LIB.parsePlayerKeys(parser, string, c);
        return ValueReplacer.forEach(string, keys, values, c);
    }

    private boolean notAllowed(String flag) {
        return !flags.isEmpty() && !flags.contains(flag);
    }

    private boolean sendWebhooks(List<String> list, boolean output) {
        for (String s : list) {
            final MessageKey key = MessageKey.identifyKey(s);

            if (key == MessageKey.WEBHOOK_KEY &&
                    !notAllowed(WEBHOOK)) key.execute(parser, s);

            B_LIB.rawLog(s);
        }
        return output;
    }

    /**
     * Sends a string list to the defined targets of the sender.
     *
     * @param noFirstSpaces if chat messages will remove all the first spaces
     * @param stringList a string list to display
     *
     * @return true if the list was sent, false otherwise
     */
    public boolean send(boolean noFirstSpaces, List<String> stringList) {
        final List<String> list = new ArrayList<>();

        for (String s : stringList)
            if (s != null) list.add(B_LIB.replacePrefixKey(s, false));

        // Checks if the messages list is empty or if the first message
        // is blank to not display.
        if (list.isEmpty()) return false;
        if (list.size() == 1 &&
                StringUtils.isBlank(list.get(0))) return false;

        // if there is no target(s), it will display to the console.
        if (targets == null || targets.isEmpty())
            return sendWebhooks(list, true);

        // Only gets the players from the collection of targets.
        List<Player> targets = new ArrayList<>();

        for (CommandSender t : this.targets) {
            if (t == null) continue;
            if (t instanceof Player) targets.add((Player) t);
        }

        // if there is no player targets, it will display to the console.
        if (targets.isEmpty()) return sendWebhooks(list, false);

        List<String> logList = new ArrayList<>();
        boolean onlyOne = targets.size() == 1;

        // Iterates of every target to display.
        for (Player t : targets)
            for (String s : list) {
                final MessageKey k = MessageKey.identifyKey(s);
                if (notAllowed(k.getUpperKey())) continue;

                Player temp = parser == null ? t : parser;
                s = parseOperatorsAndValues(temp, s);

                boolean b = noFirstSpaces && k == MessageKey.CHAT_KEY;
                k.execute(t, temp, b ? TextUtils.removeSpace(s) : s);

                if (onlyOne) logList.add(s);
            }

        // Displays the messages list to console if enabled.
        if (isLogger) (onlyOne ? logList : list).forEach(B_LIB::rawLog);
        return true;
    }

    /**
     * Sends a string array to the defined targets of the sender.
     *
     * @param noFirstSpaces if chat messages will remove all the first spaces
     * @param strings a string array to display
     *
     * @return true if the list was sent, false otherwise
     */
    public boolean send(boolean noFirstSpaces, String... strings) {
        return send(noFirstSpaces, Lists.newArrayList(strings));
    }

    /**
     * Creates and returns a copy of this sender. It will return null
     * if an error occurs.
     *
     * @return a clone of this instance
     */
    @Nullable
    public MessageSender clone() {
        try {
            return (MessageSender) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
