package me.croabeast.beanslib.terminals;

import me.croabeast.beanslib.BeansLib;
import me.croabeast.iridiumapi.IridiumAPI;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.croabeast.beanslib.utilities.TextUtils.*;

public class Bossbar {

    private final JavaPlugin plugin = BeansLib.getPlugin();

    private final Player player;
    private String line;

    private BossBar bar = null;
    private BarColor color = null;
    private BarStyle style = null;
    private Integer time = null;
    private Boolean progress = null;

    private final Pattern PATTERN =
            Pattern.compile("(?i)\\[BOSSBAR(:(.+?)(:(.+?)(:(\\d+)(:(true|false))?)?)?)?](.+)");

    protected static Map<Player, BossBar> bossbarMap = new HashMap<>();

    /**
     * Bossbar message constructor if using the PATTERN
     * to recognize a valid bossbar message.
     * @param player the player that will see the bossbar
     * @param line the bossbar message to validate
     */
    public Bossbar(Player player, String line) {
        this.player = player;
        this.line = line;
        register();
    }

    /**
     * Bossbar message constructor if you are setting every
     * parameter for the bossbar to be created.
     * @param player the player that will see the bossbar
     * @param line the message that will be displayed
     * @param color the color of the bossbar
     * @param style the style of the bossbar
     * @param seconds the seconds that the bossbar will be visible
     * @param progress if the bossbar will decrease overtime
     */
    public Bossbar(Player player, @Nullable String line, @Nullable String color, @Nullable String style, int seconds, boolean progress) {
        this.player = player;
        this.line = line;

        try {
            if (color == null) color = "";
            this.color = BarColor.valueOf(color.toUpperCase());
        } catch (Exception e) {
            this.color = null;
        }

        try {
            if (style == null) style = "";
            this.style = BarStyle.valueOf(style.toUpperCase());
        } catch (Exception e) {
            this.style = null;
        }

        this.time = (seconds <= 0 ? 1 : seconds) * 20;
        this.progress = progress;

        if (this.color == null) this.color = BarColor.WHITE;
        if (this.style == null) this.style = BarStyle.SOLID;

        if (line == null) line = "";
        line = replaceInsensitiveEach(line, new String[] {"player", "world"},
                new String[] {player.getName(), player.getWorld().getName()});
        this.line = IridiumAPI.process(parsePAPI(player, line));
    }

    /**
     * Registers the variables in the default constructor.
     */
    private void register() {
        Matcher matcher = PATTERN.matcher(line);
        if (matcher.find()) {
            try {
                color = BarColor.valueOf(matcher.group(2).toUpperCase());
            } catch (Exception e) {
                color = null;
            }

            try {
                style = BarStyle.valueOf(matcher.group(4).toUpperCase());
            } catch (Exception e) {
                style = null;
            }

            try {
                time = Integer.parseInt(matcher.group(6)) * 20;
            } catch (Exception e) {
                style = null;
            }

            try {
                progress = Boolean.valueOf(matcher.group(8));
            } catch (Exception e) {
                progress = null;
            }

            this.line = matcher.group(9);
        }

        if (color == null) color = BarColor.WHITE;
        if (style == null) style = BarStyle.SOLID;
        if (time == null) time = 3 * 20;
        if (progress == null) progress = false;

        if (line == null) line = "";
        line = replaceInsensitiveEach(line, new String[] {"player", "world"},
                new String[] {player.getName(), player.getWorld().getName()});
        line = IridiumAPI.process(parsePAPI(player, line));
    }

    /**
     * Unregisters the bossbar from the player.
     */
    private void unregister() {
        bar.removePlayer(player);
        bossbarMap.remove(player);
        bar = null;
    }

    /**
     * Animates the bossbar when the progress is enabled.
     */
    private void animate() {
        double time = 1.0D / this.time;
        double[] percentage = {1.0D};

        new BukkitRunnable() {
            @Override
            public void run() {
                bar.setProgress(percentage[0]);
                if (percentage[0] > 0.0) percentage[0] -= time;
                if (percentage[0] <= 0.0) {
                    unregister();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 0);
    }

    /**
     * Displays the bossbar message to the player.
     */
    public void display() {
        bar = Bukkit.createBossBar(line, color, style);
        bar.setProgress(1.0D);

        bar.addPlayer(player);
        bar.setVisible(true);
        bossbarMap.put(player, bar);

        if (progress && time > 0) animate();
        else Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::unregister, time);
    }

    /**
     * Gets the bukkit bossbar object from the bossbar map.
     * @param player the player that has the bossbar.
     * @return the bossbar, if the player exists or has a bossbar displayed; null otherwise
     */
    @Nullable
    public static BossBar getBossbar(@Nullable Player player) {
        if (player == null) return null;
        return bossbarMap.getOrDefault(player, null);
    }

    /**
     * Gets the bossbar map stored in cache.
     * @return the bossbar map
     */
    public static Map<Player, BossBar> getBossbarMap() {
        return bossbarMap;
    }
}
