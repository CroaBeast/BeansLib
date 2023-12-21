package me.croabeast.beanslib.misc;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.var;
import me.croabeast.beanslib.Beans;
import me.croabeast.beanslib.map.Entry;
import me.croabeast.beanslib.utility.ArrayUtils;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;

@Accessors(chain = true)
@Setter
public final class BossbarBuilder {

    private static final Map<Player, Set<BossbarBuilder>> BOSSBAR_MAP = new HashMap<>();

    private static final Set<Entry<BarColor, BarStyle>> DEFS =
            Collections.singleton(Entry.of(BarColor.WHITE, BarStyle.SOLID));

    private static final Random RANDOM = new Random();

    private final Plugin plugin;
    private final Player player;

    @Setter(AccessLevel.NONE)
    private BossBar bar;

    private final List<String> messages;
    private final List<Entry<BarColor, BarStyle>> formats;

    @Setter(AccessLevel.NONE)
    private int messageInterval = 0,
            formatInterval = 0, time = 3 * 20;

    private boolean decrease = false;
    private boolean randomMessages = false;
    private boolean randomFormats = false;

    @Setter(AccessLevel.NONE)
    private BukkitRunnable run = null;

    public BossbarBuilder(Plugin plugin, Player player) {
        this.plugin = Objects.requireNonNull(plugin);
        this.player = Objects.requireNonNull(player);

        messages = new LinkedList<>();
        formats = new LinkedList<>();
    }

    private static List<String> fromSection(ConfigurationSection section, String path) {
        String s = path + ".lines";
        s = section.isSet(s) ? s : path;

        return TextUtils.toList(section, s);
    }

    private static <N extends Number> N round(N number) {
        return Rounder.round(number);
    }

    public BossbarBuilder(Plugin plugin, Player player, ConfigurationSection section) {
        this(plugin, player);

        setMessageInterval(section.getInt("messages.interval"))
                .setMessages(fromSection(section, "messages"))
                .setFormatInterval(section.getInt("formats.interval"))
                .setFormats(fromSection(section, "formats"))
                .setDecrease(section.getBoolean("progress-decrease"))
                .setTime(section.getInt("time", 3))
                .setRandomMessages(section.getBoolean("messages.random"))
                .setRandomFormats(section.getBoolean("formats.random"));
    }

    public BossbarBuilder(Plugin plugin, Player player, String string) {
        this(plugin, player);

        if (StringUtils.isBlank(string)) {
            messages.add("");
            return;
        }

        Matcher matcher = Beans.getBossbarPattern().matcher(string);
        if (!matcher.find()) {
            setMessages(string);
            return;
        }

        @Nullable String args = matcher.group(2);

        BarColor c = BarColor.WHITE;
        BarStyle st = BarStyle.SOLID;

        if (args != null) {
            String[] a = args.substring(1).split(":", 4);

            for (String s : a) {
                try {
                    time = Integer.parseInt(s) * 20;
                    continue;
                } catch (Exception ignored) {}

                if (s.matches("(?i)true")) {
                    decrease = true;
                    continue;
                }
                if (s.matches("(?i)false")) {
                    decrease = false;
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

        formats.add(Entry.of(c, st));
        setMessages(matcher.group(3));
    }

    public BossbarBuilder setTime(int time) {
        this.time = time * 20;
        return this;
    }

    public BossbarBuilder setMessages(List<String> list) {
        int interval = messageInterval;

        list.replaceAll(s -> {
            if (StringUtils.isBlank(s)) return "";

            return Beans.colorize(player,
                    TextUtils.STRIP_FIRST_SPACES.apply(s));
        });

        if (interval == 0) {
            messages.addAll(list);
            return this;
        }

        List<String> tempList = new ArrayList<>();
        int temp = 0;

        for (int i = 0; i <= time / interval; i++) {
            tempList.add(list.get(temp));
            temp++;

            if (temp >= list.size()) temp = 0;
        }

        messages.addAll(tempList);
        return this;
    }

    public BossbarBuilder setMessages(String... list) {
        return setMessages(ArrayUtils.toList(list));
    }

    public BossbarBuilder setMessageInterval(int interval) {
        messageInterval = interval;
        return setMessages(messages);
    }

    public BossbarBuilder setFormats(List<String> list) {
        int interval = formatInterval;

        List<Entry<BarColor, BarStyle>> temp = new LinkedList<>();

        for (String s : list) {
            if (StringUtils.isBlank(s)) continue;

            String[] a = s.split(":", 2);

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

            temp.add(Entry.of(c, t));
        }

        if (interval == 0) {
            formats.addAll(temp.isEmpty() ? DEFS : temp);
            return this;
        }

        List<Entry<BarColor, BarStyle>> tempList = new LinkedList<>();
        int count = 0;

        for (int i = 0; i <= time / formatInterval; i++) {
            var e = formats.get(count++);
            tempList.add(Entry.of(e));

            if (count >= formats.size()) count = 0;
        }

        formats.clear();
        formats.addAll(tempList);

        return this;
    }

    public BossbarBuilder setFormats(String... list) {
        return setFormats(ArrayUtils.toList(list));
    }

    public BossbarBuilder setFormatInterval(int interval) {
        formatInterval = interval;
        if (formatInterval < 1) return this;

        List<Entry<BarColor, BarStyle>> temp = new LinkedList<>();
        int count = 0;

        for (int i = 0; i <= time / formatInterval; i++) {
            var e = formats.get(count++);
            temp.add(Entry.of(e));

            if (count >= formats.size()) count = 0;
        }

        formats.clear();
        formats.addAll(temp);

        return this;
    }

    public boolean display() {
        try {
            Entry<BarColor, BarStyle> value = formats.get(0);

            bar = Bukkit.createBossBar(
                    messages.get(0),
                    value.getKey(), value.getValue()
            );
            bar.setProgress(1.0D);

            bar.addPlayer(player);
            bar.setVisible(true);

            Set<BossbarBuilder> builders = BOSSBAR_MAP.get(player);
            if (builders == null) builders = new HashSet<>();

            builders.add(this);
            BOSSBAR_MAP.put(player, builders);

            return createAnimation();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean createAnimation() {
        if (bar == null) return false;

        final double total = 1.0D;
        double[] initial = {total};

        int[] c1 = {1}, c2 = {1};
        int mSize = messages.size(), fSize = formats.size();

        run = new BukkitRunnable() {
            @Override
            public void run() {
                if (initial[0] <= 0.0) {
                    unregister();
                    cancel();
                    return;
                }

                final double interval = Rounder.round(4, total / time);
                initial[0] = Rounder.round(4, initial[0] - interval);

                if (decrease)
                    bar.setProgress(Math.max(initial[0], 0.0));

                double msgInt = round((mSize - c1[0]) * (total / mSize)),
                        forInt = round((fSize - c2[0]) * (total / fSize));

                final double init = round(initial[0]);

                if (mSize > 1 && (Objects.equals(init, msgInt) ||
                        round(init - msgInt) <= 0.01) && c1[0] < mSize)
                {
                    int i = randomMessages ?
                            RANDOM.nextInt(mSize) : c1[0];

                    bar.setTitle(messages.get(i));
                    if (!randomMessages) c1[0]++;
                }

                if (!(fSize > 1 && (Objects.equals(init, forInt) ||
                        round(init - forInt) <= 0.01) && c2[0] < fSize))
                    return;

                int i = randomFormats ? RANDOM.nextInt(fSize) : c2[0];

                BarColor c = formats.get(i).getKey();
                BarStyle st = formats.get(i).getValue();

                if (c != null) bar.setColor(c);
                if (st != null) bar.setStyle(st);

                if (!randomFormats) c2[0]++;
            }
        };

        run.runTaskTimer(plugin, 0, 0);
        return true;
    }

    public boolean unregister() {
        if (bar == null) return true;

        try {
            if (run != null && !run.isCancelled())
                run.cancel();

            bar.removePlayer(player);
            bar.setVisible(false);
            bar = null;

            BOSSBAR_MAP.remove(player);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Set<BossbarBuilder> getBuilders(Player player) {
        return BOSSBAR_MAP.getOrDefault(player, new HashSet<>());
    }
}
