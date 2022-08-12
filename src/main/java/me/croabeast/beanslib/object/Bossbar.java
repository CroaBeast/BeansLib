package me.croabeast.beanslib.object;

import lombok.Getter;
import me.croabeast.beanslib.utility.Exceptions;
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

import static me.croabeast.beanslib.utility.TextUtils.*;

/**
 * The object to handle Bossbar messages.
 *
 * @author CroaBeast
 * @since 1.0
 */
public class Bossbar {

    private final JavaPlugin plugin;

    private final Player player;
    private String line;

    @Getter @Nullable
    /*
     * The bukkit bossbar object, can be null.
     */
    private BossBar bar = null;

    private BarColor color = null;
    private BarStyle style = null;
    private Integer time = null;
    private Boolean progress = null;

    /**
     * The Bossbar pattern to check bossbar arguments.
     */
    protected final Pattern PATTERN = Pattern.compile("(?i)(\\[bossbar(:.+)?])(.+)");

    /**
     * The map to get all the players that have a bossbar message displayed.
     */
    private static final Map<Player, Bossbar> BOSSBAR_MAP = new HashMap<>();

    /**
     * Bossbar message constructor if using the PATTERN
     * to recognize a valid bossbar message.
     * @param plugin the plugin's instance
     * @param player the player that will see the bossbar
     * @param line the bossbar message to validate
     * @throws NullPointerException if player is null
     */
    public Bossbar(JavaPlugin plugin, Player player, String line) {
        this.plugin = plugin;
        this.player = Exceptions.checkPlayer(player);

        registerValues(line == null ? "" : line);
        setDefaultsIfValuesNull();
    }

    /**
     * Bossbar message constructor if you are setting every
     * parameter for the bossbar to be created.
     * @param plugin the plugin's instance
     * @param player the player that will see the bossbar
     * @param line the message that will be displayed
     * @param color the color of the bossbar
     * @param style the style of the bossbar
     * @param s the seconds that the bossbar will be visible
     * @param p if the bossbar will decrease overtime
     * @throws NullPointerException if player is null
     */
    public Bossbar(JavaPlugin plugin, Player player, String line, String color, String style, int s, boolean p) {
        this.plugin = plugin;
        this.player = Exceptions.checkPlayer(player);
        this.line = line == null ? "" : line;

        this.color = ifValidColor(color) ? BarColor.valueOf(color) : BarColor.WHITE;
        this.style = ifValidStyle(style) ? BarStyle.valueOf(style) : BarStyle.SOLID;

        if (s > 0) this.time = s * 20;
        this.progress = p;

        setDefaultsIfValuesNull();
    }

    /**
     * Checks if an input string is a valid {@link BarColor} enum.
     * @param input an input string
     * @return if the input string is valid
     */
    private boolean ifValidColor(String input) {
        if (input == null) return false;
        input = input.toUpperCase();

        for (BarColor c : BarColor.values())
            if (input.matches("(?i)" + c)) return true;

        return false;
    }

    /**
     * Checks if an input string is a valid {@link BarStyle} enum.
     * @param input an input string
     * @return if the input string is valid
     */
    private boolean ifValidStyle(String input) {
        if (input == null) return false;
        input = input.toUpperCase();

        for (BarStyle s : BarStyle.values())
            if (input.matches("(?i)" + s)) return true;

        return false;
    }

    /**
     * Registers all the values depending on an input string line.
     * @param input an input string
     */
    private void registerValues(String input) {
        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find()) {
            line = input;
            return;
        }

        line = matcher.group(3);

        String arguments = matcher.group(2);
        if (arguments == null) return;

        String[] array = arguments.substring(1).split(":");

        int length = array.length;
        if (length <= 0 || length > 4) return;

        String c = null, st = null, t = null, p = null;

        for (String s : array) {
            if (s.matches("(?i)true|false")) p = s;
            else if (ifValidColor(s)) c = s;
            else if (ifValidStyle(s)) st = s;
            else if (s.matches("\\d+")) t = s;
        }

        color = c == null ? null : BarColor.valueOf(c);
        style = st == null ? null : BarStyle.valueOf(st);

        time = t == null ? null : Integer.parseInt(t) * 20;
        progress = p == null ? null : Boolean.parseBoolean(p);
    }

    /**
     * Sets the values to default ones if the input values are null or empty.
     */
    private void setDefaultsIfValuesNull() {
        if (color == null) color = BarColor.WHITE;
        if (style == null) style = BarStyle.SOLID;

        if (time == null) time = 3 * 20;
        if (progress == null) progress = false;

        if (line == null) line = "";
        line = removeSpace(parsePAPI(player, line));
        line = IridiumAPI.process(player, line);
    }

    /**
     * Unregisters the bossbar from the player.
     */
    public void unregister() {
        if (bar == null) return;

        bar.removePlayer(player);
        bar = null;
        BOSSBAR_MAP.remove(player);
    }

    /**
     * Animates the bossbar when the progress is enabled.
     */
    public void animate() {
        if (bar == null) return;

        double time = 1.0D / this.time;
        double[] percentage = {1.0D};

        new BukkitRunnable() {
            @Override
            public void run() {
                bar.setProgress(percentage[0]);
                if (percentage[0] > 0.0)
                    percentage[0] -= time;

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
        BOSSBAR_MAP.put(player, this);

        if (progress && time > 0) animate();
        else Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::unregister, time);
    }

    /**
     * Gets the BeansLib bossbar object from the bossbar map.
     * @param player the player that has the bossbar.
     * @return the bossbar, if the player exists or has a bossbar displayed; null otherwise
     */
    @Nullable
    public static Bossbar getBossbar(Player player) {
        return player == null ? null : BOSSBAR_MAP.getOrDefault(player, null);
    }
}
