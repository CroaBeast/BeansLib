package me.croabeast.beanslib.logger;

import lombok.SneakyThrows;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.message.MessageExecutor;
import me.croabeast.beanslib.message.MessageSender;
import me.croabeast.beanslib.misc.StringApplier;
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
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * This class can manages how strings can be logged/sent to a player and/or console.
 *
 * @author CroaBeast
 * @since 1.4
 */

public class BeansLogger {

    private RawLogger rawLogger, pluginLogger;
    private final BeansLib lib;

    public BeansLogger(BeansLib lib) {
        this.lib = Objects.requireNonNull(lib);

        Plugin plugin = null;

        try {
            plugin = lib.getPlugin();
        } catch (Exception ignored) {}

        pluginLogger = new BukkitLogger(plugin);
        rawLogger = new BukkitLogger(null);

        if (!LibUtils.isPaper() || LibUtils.getMainVersion() < 18.2)
            return;

        try {
            pluginLogger = new PaperLogger(plugin);
            rawLogger = new PaperLogger(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> toLoggerStrings(Player player, boolean useLogger, String... strings) {
        if (ArrayUtils.isArrayEmpty(strings)) return new ArrayList<>();

        final String sp = lib.getLineSeparator();
        List<String> list = new ArrayList<>();

        boolean isLog = useLogger && lib.isStripPrefix();

        for (String string : strings) {
            if (string == null) continue;

            StringApplier applier = StringApplier.of(string)
                    .apply(s -> lib.replacePrefixKey(s, isLog))
                    .apply(s -> s.replace(sp, "&f" + sp));

            String temp = applier.toString();

            StringApplier result = StringApplier.of(temp);
            MessageExecutor e = MessageExecutor.identifyKey(temp);

            if (isLog && e != MessageExecutor.CHAT_EXECUTOR) {
                Matcher m = e.getPattern().matcher(temp);

                if (m.find())
                    result.apply(s -> s.replace(m.group(), ""));
            }

            result.apply(s -> lib
                    .createCenteredChatMessage(player, s));
            list.add(result.toString());
        }

        return list;
    }

    private List<String> toLoggerStrings(String... lines) {
        return toLoggerStrings(null, true, lines);
    }

    /**
     * Sends information to a player using the {@link MessageSender} object.
     *
     * @param player a valid player
     * @param lines the information to send
     */
    public void playerLog(Player player, String... lines) {
        new MessageSender(player).setLogger(false).send(toLoggerStrings(player, false, lines));
    }

    private String colorLogger(String string) {
        final String s = TextUtils.STRIP_JSON.apply(string);

        return lib.isColoredConsole() ?
                NeoPrismaticAPI.colorize(s) : NeoPrismaticAPI.stripAll(s);
    }

    private void raw(String line) {
        rawLogger.info(colorLogger(line));
    }

    /**
     * Sends requested information using {@link Bukkit#getLogger()} to the console,
     * avoiding the plugin prefix.
     *
     * @param lines the information to send
     */
    public void rawLog(String... lines) {
        toLoggerStrings(lines).forEach(this::raw);
    }

    private void log(String line) {
        pluginLogger.info(colorLogger(line));
    }

    /**
     * Sends requested information to a {@link CommandSender} using the plugin's
     * logger and its prefix.
     *
     * <p> If the sender is a {@link Player} and it's not null, it will also send
     * the information to the player using {@link #playerLog(Player, String...)}.
     *
     * @param sender a valid sender, can be the console, a player or null
     * @param lines the information to send
     *
     * @throws NullPointerException if the plugin is null
     */
    public void doLog(CommandSender sender, String... lines) {
        if (sender instanceof Player) playerLog((Player) sender, lines);
        toLoggerStrings(lines).forEach(this::log);
    }

    /**
     * Sends requested information to a {@link CommandSender} using the plugin's
     * logger and its prefix.
     *
     * @param lines the information to send
     * @throws NullPointerException if the plugin is null
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
        private PaperLogger(Plugin plugin) {
            if (LibUtils.isPaper()) {
                String name = plugin != null ? plugin.getName() : "";

                logger = Class
                        .forName(KYORI_PREFIX + "logger.slf4j.ComponentLogger")
                        .getMethod("logger", String.class)
                        .invoke(null, name);

                clazz = logger.getClass();
                return;
            }

            throw new IllegalAccessException("Paper is not being used");
        }

        @SneakyThrows
        static Class<?> from(String name) {
            return Class.forName(KYORI_PREFIX + name);
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
