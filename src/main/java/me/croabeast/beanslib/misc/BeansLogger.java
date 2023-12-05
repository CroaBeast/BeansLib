package me.croabeast.beanslib.misc;

import lombok.SneakyThrows;
import me.croabeast.beanslib.Beans;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.message.CenteredMessage;
import me.croabeast.beanslib.message.MessageExecutor;
import me.croabeast.beanslib.message.MessageSender;
import me.croabeast.beanslib.applier.StringApplier;
import me.croabeast.beanslib.utility.ArrayUtils;
import me.croabeast.beanslib.utility.LibUtils;
import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.neoprismatic.NeoPrismaticAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * A class that provides methods for logging messages to the console or to a player.
 *
 * <p> It uses the {@link BeansLib} and {@link NeoPrismaticAPI} libraries for colorizing and
 * formatting the messages.
 *
 * <p> It also supports different types of loggers depending on the server
 * platform (Bukkit or Paper).
 *
 * @author CroaBeast
 * @version 1.0
 */
public class BeansLogger {

    private RawLogger rawLogger, pluginLogger;
    private final BeansLib lib;

    /**
     * Creates a BeansLogger instance with a given BeansLib instance.
     *
     * <p> It initializes the raw logger and the plugin logger according to the
     * server platform (Bukkit or Paper).
     *
     * @param lib the BeansLib instance to use for logging
     * @throws NullPointerException if the lib is null
     */
    public BeansLogger(BeansLib lib) {
        this.lib = Objects.requireNonNull(lib);

        Plugin plugin = null;

        try {
            plugin = lib.getPlugin();
        } catch (Exception ignored) {}

        pluginLogger = new BukkitLogger(plugin);
        rawLogger = new BukkitLogger(null);

        if (!LibUtils.IS_PAPER || LibUtils.MAIN_VERSION < 18.2)
            return;

        try {
            pluginLogger = new PaperLogger(plugin);
            rawLogger = new PaperLogger(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a BeansLogger instance with the default BeansLib instance.
     *
     * <p> It initializes the raw logger and the plugin logger according to the
     * server platform (Bukkit or Paper).
     */
    public BeansLogger() {
        this(Beans.getLoaded());
    }

    private List<String> toLoggerStrings(Player player, boolean useLogger, String... strings) {
        if (ArrayUtils.isArrayEmpty(strings))
            return new ArrayList<>();

        String split = lib.getLineSeparator();
        String resultSplit = "&f" + split.replaceAll("\\\\[QE]", "");

        List<String> list = new LinkedList<>();
        boolean isLog = useLogger && lib.isStripPrefix();

        CenteredMessage c = new CenteredMessage(player).setColored(false);

        for (String string : strings) {
            if (string == null) continue;

            StringApplier applier = StringApplier.simplified(string)
                    .apply(s -> lib.replacePrefixKey(s, isLog))
                    .apply(s -> s.replaceAll(split, resultSplit));

            String temp = applier.toString();
            StringApplier result = StringApplier.simplified(temp);

            MessageExecutor e = MessageExecutor.identifyKey(temp);

            if (isLog && e != MessageExecutor.CHAT_EXECUTOR) {
                Matcher m = e.getPattern().matcher(temp);
                if (m.find()) result.apply(s -> s.replace(m.group(), ""));
            }

            list.add(result.apply(c::center).toString());
        }

        return list;
    }

    private List<String> toLoggerStrings(String... lines) {
        return toLoggerStrings(null, true, lines);
    }

    /**
     * Logs a list of messages to a player only, if not null.
     * <p> The messages are formatted and colorized according to the BeansLib settings.
     *
     * @param player the player to send the messages to, or null if none
     * @param lines the array of messages to log
     */
    public void playerLog(Player player, String... lines) {
        new MessageSender(player).setLogger(false).send(toLoggerStrings(player, false, lines));
    }

    private String colorLogger(String string) {
        return StringApplier.simplified(string).apply(TextUtils.STRIP_JSON)
                .apply(
                        lib.isColoredConsole() ?
                                NeoPrismaticAPI::colorize :
                                NeoPrismaticAPI::stripAll
                )
                .toString();
    }

    private void raw(String line) {
        rawLogger.info(colorLogger(line));
    }

    /**
     * Logs a list of messages to the raw logger only.
     * <p> The messages are formatted and colorized according to the BeansLib settings.
     *
     * @param lines the array of messages to log
     */
    public void rawLog(String... lines) {
        toLoggerStrings(lines).forEach(this::raw);
    }

    private void log(String line) {
        pluginLogger.info(colorLogger(line));
    }

    /**
     * Logs a list of messages to a player, if not null, and to the console.
     * <p> The messages are formatted and colorized according to the BeansLib settings.
     *
     * @param sender the sender to send the messages to, or null if none
     * @param lines the array of messages to log
     */
    public void doLog(CommandSender sender, String... lines) {
        if (sender instanceof Player) playerLog((Player) sender, lines);
        toLoggerStrings(lines).forEach(this::log);
    }

    /**
     * Logs a list of messages to the console only.
     * <p> The messages are formatted and colorized according to the BeansLib settings.
     *
     * @param lines the array of messages to log
     */
    public void doLog(String... lines) {
        doLog(null, lines);
    }

    private static boolean getBool(String string) {
        return string.matches("(?i)true|false") && string.matches("(?i)true");
    }

    /**
     * Sends information choosing which of the two main methods will be used in each line.
     * ({@link #rawLog(String...) rawLog}, {@link #doLog(CommandSender, String...) doLog})
     *
     * <p> If the line does not start with a boolean value or that value is false,
     * it will use the {@link #doLog(CommandSender, String...) doLog} method, otherwise
     * will use the {@link #rawLog(String...) rawLog} method.
     *
     * <pre> {@code
     * "My information for the console" >> // Uses "doLog"
     * "true::My basic log information" >> // Uses "rawLog"
     * "false::Some plugin's information" >> // Uses "doLog"
     * "" or null >> // Uses "doLog", 'cause is empty/null
     * } </pre>
     *
     * @param sender a valid sender, can be the console, a player or null
     * @param lines the information to send
     */
    public void mixLog(CommandSender sender, String... lines) {
        if (ArrayUtils.isArrayEmpty(lines))
            return;

        MessageSender mSender = new MessageSender(sender)
                .setLogger(false);

        for (String line : lines) {
            if (StringUtils.isBlank(line)) {
                if (sender != null) mSender.singleSend(line);

                log(line);
                continue;
            }

            String[] array = line.split("::", 2);

            if (array.length != 2) {
                if (sender != null) mSender.singleSend(line);

                log(line);
                continue;
            }

            if (getBool(array[0])) {
                raw(array[1]);
                continue;
            }

            if (sender != null) mSender.singleSend(array[1]);
            log(array[1]);
        }
    }

    /**
     * Sends information choosing which of the two main methods will be used in each line.
     * ({@link #rawLog(String...) rawLog}, {@link #doLog(String...) doLog})
     *
     * <p> If the line does not start with a boolean value or that value is false,
     * it will use the {@link #doLog(String...) doLog} method, otherwise will use the
     * {@link #rawLog(String...) rawLog} method.
     *
     * <pre> {@code
     * "My information for the console" >> // Uses "doLog"
     * "true::My basic log information" >> // Uses "rawLog"
     * "false::Some plugin's information" >> // Uses "doLog"
     * "" or null >> // Uses "doLog", 'cause is empty/null
     * } </pre>
     *
     * @param lines the information to send
     */
    public void mixLog(String... lines) {
        mixLog(null, lines);
    }

    interface RawLogger {
        void info(String string);
    }

    static class BukkitLogger implements RawLogger {

        final Plugin plugin;

        BukkitLogger(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void info(String string) {
            if (plugin == null) {
                Bukkit.getLogger().info(string);
                return;
            }

            plugin.getLogger().info(string);
        }
    }

    static class PaperLogger implements RawLogger {

        static final String KYORI_PREFIX = "net.kyori.adventure.text.";

        private final Class<?> clazz;
        private final Object logger;

        @SneakyThrows
        static Class<?> from(String name) {
            return Class.forName(KYORI_PREFIX + name);
        }

        @SneakyThrows
        private PaperLogger(Plugin plugin) {
            if (LibUtils.IS_PAPER) {
                String name = plugin != null ? plugin.getName() : "";

                logger = from("logger.slf4j.ComponentLogger")
                        .getMethod("logger", String.class).invoke(null, name);

                clazz = logger.getClass();
                return;
            }

            throw new IllegalAccessException("Paper is not being used");
        }

        @Override
        public void info(String string) {
            try {
                Class<?> legacy = from("serializer.legacy.LegacyComponentSerializer");

                Method method = clazz.getMethod("info", from("Component"));
                method.setAccessible(true);

                Method section = legacy.getMethod("legacySection");
                section.setAccessible(true);

                method.invoke(logger,
                        legacy
                                .getMethod("deserialize", String.class)
                                .invoke(
                                        section.invoke(null),
                                        string
                                ));

                section.setAccessible(false); method.setAccessible(false);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
