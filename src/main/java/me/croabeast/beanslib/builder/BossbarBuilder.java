package me.croabeast.beanslib.builder;

import com.google.common.collect.Lists;
import lombok.*;
import lombok.experimental.Accessors;
import me.croabeast.beanslib.Beans;
import me.croabeast.beanslib.message.MessageKey;
import me.croabeast.beanslib.utility.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static me.croabeast.beanslib.message.MessageKey.BOSSBAR_KEY;
import static me.croabeast.beanslib.misc.Rounder.round;

/**
 * This class manages how to create a {@link BossBar} instance for
 * a player to be displayed, to set messages, color and styles.
 * It can be animated to change messages, colors, styles and change
 * the progress as the display time runs out.
 *
 * <pre> {@code
 * // Creating an instance of the builder
 * BossbarBuilder builder = new BossbarBuilder(
 *      yourPlugin, player,
 *      Arrays.asList("My bossbar message", "It changes :D"),
 *      0, // if interval is 0, will be default interval
 *      Arrays.asList("SOLID:BLUE", "YELLOW", "SEGMENTED_10"),
 *      0, // if interval is 0, will be default interval
 *      5, // 5 is the display time in seconds
 *      true // if bossbar progress will decrease over time
 * );
 *
 * builder.display(); // Displays the bossbar to the player.
 * } </pre>
 */
@Accessors(chain = true)
@Setter
public class BossbarBuilder {

    /**
     * The map to get all the players that have a bossbar message displayed.
     */
    private static final Map<Player, BossbarBuilder> BOSSBAR_MAP = new HashMap<>();

    private static final Random RANDOM = new Random();

    private static final DoubleValue
            DEFAULT_FORMAT = new DoubleValue(BarColor.WHITE, BarStyle.SOLID);

    private final Plugin plugin;
    private final Player player;

    @Setter(AccessLevel.NONE)
    private BossBar bar = null;

    private List<String> messages;

    @Setter(AccessLevel.NONE)
    private List<DoubleValue> formats = Lists.newArrayList(DEFAULT_FORMAT);

    private int time = 3 * 20;
    private boolean progressDecrease = false;

    private boolean useRandomMessages = false,
            useRandomFormats = false;

    @Setter(AccessLevel.NONE)
    private BukkitRunnable run = null;

    /**
     *
     * Creates a new builder with all the specified configuration.
     *
     * @param plugin plugin's instance of your project
     * @param player a player, can not be null
     * @param messages a list of the messages to be displayed
     * @param messageInterval custom interval of messages
     *                        if 0, will use the default interval
     * @param formats a list of {@link BarColor} and/or {@link BarStyle} of the bossbar
     * @param formatInterval custom interval of formats
     *                       if 0, will use the default interval
     * @param time the display time of the bossbar in seconds
     * @param progressDecrease if bossbar progress will decrease over time
     */
    public BossbarBuilder(
            @NotNull Plugin plugin, @NotNull Player player,
            @NotNull List<String> messages, int messageInterval,
            @Nullable List<String> formats, int formatInterval,
            int time, boolean progressDecrease
    ) {
        this.plugin = plugin;
        this.player = player;

        this.time = time * 20;

        this.messages = toList(messages, messageInterval);

        if (formats == null) formats = new ArrayList<>();
        this.formats = toFormats(formats, formatInterval);

        this.progressDecrease = progressDecrease;
    }

    private static String checkPathList(ConfigurationSection section, String path) {
        final var s = path + ".lines";
        return section.isSet(s) ? s : path;
    }

    /**
     * Creates a new builder using a {@link ConfigurationSection} of a file.
     * Basic example: <a href="https://paste.helpch.at/yojidanamu.bash">bossbars.yml</a>
     *
     * @param plugin plugin's instance of your project
     * @param player a player, can not be null
     * @param section a {@link ConfigurationSection} to format the bossbar
     */
    public BossbarBuilder(
            @NotNull Plugin plugin, @NotNull Player player,
            @NotNull ConfigurationSection section
    ) {
        this(
                plugin, player,
                TextUtils.toList(section, checkPathList(section, "messages")),
                section.getInt("messages.interval"),
                TextUtils.toList(section, checkPathList(section, "formats")),
                section.getInt("formats.interval"),
                section.getInt("time", 3),
                section.getBoolean("progress-decrease")
        );

        setUseRandomFormats(section.getBoolean("messages.random")).
                setUseRandomFormats(section.getBoolean("formats.random"));
    }

    /**
     * Creates a new builder using a single line to parse all the necessary arguments.
     * The line should be using the {@link MessageKey#BOSSBAR_KEY} pattern.
     *
     * @param plugin plugin's instance of your project
     * @param player a player, can not be null
     * @param string an input string
     */
    public BossbarBuilder(@NotNull Plugin plugin, @NotNull Player player, String string) {
        this.plugin = plugin;
        this.player = player;

        if (StringUtils.isBlank(string)) {
            messages = Lists.newArrayList("");
            return;
        }

        var matcher = BOSSBAR_KEY.getPattern().matcher(string);
        if (!matcher.find()) {
            messages = toList(Lists.newArrayList(string), 0);
            return;
        }

        @Nullable var args = matcher.group(2);

        var c = BarColor.WHITE;
        var st = BarStyle.SOLID;

        if (args != null) {
            var a = args.substring(1).split(":", 4);

            for (var s : a) {
                try {
                    time = Integer.parseInt(s) * 20;
                    continue;
                } catch (Exception ignored) {}

                if (s.matches("(?i)true")) {
                    progressDecrease = true;
                    continue;
                }
                if (s.matches("(?i)false")) {
                    progressDecrease = false;
                    continue;
                }

                try {
                    c = BarColor.valueOf(s);
                    continue;
                } catch (Exception ignored) {}

                try {
                    st = BarStyle.valueOf(s);
                } catch (Exception ignored) {}
            }
        }

        formats = Lists.newArrayList(new DoubleValue(c, st));

        messages = toList(
                Lists.newArrayList(matcher.group(3)), 0);
    }

    <E> List<E> ifList(List<E> list, int interval, List<E> def) {
        if (interval == 0) return def == null ? list : def;

        var tempList = new ArrayList<E>();
        int temp = 0;

        for (int i = 0; i <= time / interval; i++) {
            tempList.add(list.get(temp));
            temp++;

            if (temp >= list.size()) temp = 0;
        }

        return tempList;
    }

    List<String> toList(List<String> list, int interval) {
        list.replaceAll(s -> {
            if (StringUtils.isBlank(s)) return "";

            return Beans.colorize(player,
                    TextUtils.STRIP_FIRST_SPACES.apply(s));
        });

        return ifList(list, interval, null);
    }

    List<DoubleValue> toFormats(List<String> formats, int interval) {
        var f = new ArrayList<DoubleValue>();

        for (var s : formats) {
            if (StringUtils.isBlank(s)) continue;

            var a = s.split(":", 2);

            BarColor c = null;
            BarStyle t = null;

            boolean cRegister = false, tRegister = false;

            try {
                c = BarColor.valueOf(a[0]);
                cRegister = true;
            } catch (Exception e) {
                try {
                    t = BarStyle.valueOf(a[0]);
                    tRegister = true;
                } catch (Exception ignored) {}
            }

            if (a.length == 2)
                try {
                    if (!tRegister)
                        t = BarStyle.valueOf(a[1]);
                } catch (Exception e) {
                    try {
                        if (!cRegister)
                            c = BarColor.valueOf(a[1]);
                    } catch (Exception ignored) {}
                }

            f.add(new DoubleValue(c, t));
        }

        return ifList(f, interval, f.isEmpty() ?
                Lists.newArrayList(DEFAULT_FORMAT) : f);
    }

    /**
     * Displays the bossbar message to the player.
     */
    public void display() {
        var value = formats.get(0);

        bar = Bukkit.createBossBar(
                messages.get(0),
                value.getColor(), value.getStyle()
        );
        bar.setProgress(1.0D);

        bar.addPlayer(player);
        bar.setVisible(true);
        BOSSBAR_MAP.put(player, this);

        createAnimation();
    }

    void createAnimation() {
        if (bar == null) return;

        final double total = 1.0D;
        double[] initial = {total};

        int[] c1 = {1}, c2 = {1};
        int mSize = messages.size(), fSize = formats.size();

        run = new BukkitRunnable() {
            @Override
            public void run() {
                if (initial[0] <= 0.0) { unregister(); cancel(); return; }

                final double interval = round(4, total / time);
                initial[0] = round(4, initial[0] - interval);

                if (progressDecrease)
                    bar.setProgress(Math.max(initial[0], 0.0));

                double msgInt = round((mSize - c1[0]) * (total / mSize)),
                        forInt = round((fSize - c2[0]) * (total / fSize));

                final double init = round(initial[0]);

                if (mSize > 1 && (Objects.equals(init, msgInt) ||
                        round(init - msgInt) <= 0.01) && c1[0] < mSize)
                {
                    int i = useRandomMessages ?
                            RANDOM.nextInt(mSize) : c1[0];

                    bar.setTitle(messages.get(i));
                    if (!useRandomMessages) c1[0]++;
                }

                if (!(fSize > 1 && (Objects.equals(init, forInt) ||
                        round(init - forInt) <= 0.01) && c2[0] < fSize))
                    return;

                int i = useRandomFormats ? RANDOM.nextInt(fSize) : c2[0];

                var c = formats.get(i).getColor();
                var st = formats.get(i).getStyle();

                if (c != null) bar.setColor(c);
                if (st != null) bar.setStyle(st);

                if (!useRandomFormats) c2[0]++;
            }
        };

        run.runTaskTimer(plugin, 0, 0);
    }

    /**
     * Unregisters the bossbar from the player.
     */
    public void unregister() {
        if (bar == null) return;

        if (run != null && !run.isCancelled())
            run.cancel();

        bar.removePlayer(player);
        bar.setVisible(false);
        bar = null;

        BOSSBAR_MAP.remove(player);
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    @Getter
    static class DoubleValue {
        private final BarColor color;
        private final BarStyle style;

        @Override
        public String toString() {
            return "{" + color + ", " + style + "}";
        }
    }

    /**
     * Gets the BeansLib bossbar object from the bossbar map.
     *
     * @param player the player that has the bossbar.
     * @return the bossbar, if the player exists or has a bossbar displayed; null otherwise
     */
    @Nullable
    public static BossbarBuilder getBuilder(Player player) {
        return player == null ? null : BOSSBAR_MAP.getOrDefault(player, null);
    }
}
