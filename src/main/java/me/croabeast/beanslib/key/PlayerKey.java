package me.croabeast.beanslib.key;

import me.croabeast.beanslib.applier.StringApplier;
import me.croabeast.beanslib.misc.CollectionBuilder;
import me.croabeast.beanslib.misc.Rounder;
import me.croabeast.beanslib.utility.Exceptions;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Function;

/**
 * A class that represents a key-function pair for a player, the function will
 * return a value of type T.
 *
 * <p> The class provides static methods to load, remove, edit, and replace
 * keys with their corresponding values.
 *
 * <p> The class also maintains a map of default keys that can be restored at
 * any time.
 */
public final class PlayerKey<T> {

    private static final Set<PlayerKey<?>> KEY_SET = new LinkedHashSet<>();
    private static final Set<PlayerKey<?>> DEFS = new LinkedHashSet<>();

    private final String key;
    private final Function<Player, T> function;

    private static <N extends Number> Function<Player, N> fromLoc(Function<Location, N> function) {
        return p -> Rounder.round(function.apply(p.getLocation()));
    }

    static {
        loadKey0("{player}", HumanEntity::getName);
        loadKey0("{playerDisplayName}", Player::getDisplayName);

        loadKey0("{playerUUID}", Entity::getUniqueId);
        loadKey0("{playerWorld}", p -> p.getWorld().getName());
        loadKey0("{playerGameMode}", HumanEntity::getGameMode);

        loadKey0("{playerX}", fromLoc(Location::getX));
        loadKey0("{playerY}", fromLoc(Location::getY));
        loadKey0("{playerZ}", fromLoc(Location::getZ));

        loadKey0("{playerYaw}", fromLoc(Location::getYaw));
        loadKey0("{playerPitch}", fromLoc(Location::getPitch));
    }

    private PlayerKey(String key, Function<Player, T> function) {
        Exceptions.hasPluginAccess(PlayerKey.class);

        if (StringUtils.isBlank(key))
            throw new NullPointerException("Key is empty/null");

        this.key = key;
        this.function = Objects.requireNonNull(function);
    }

    private PlayerKey(PlayerKey<T> key) {
        this.key = key.key;
        this.function = key.function;
    }

    private T apply(Player player) {
        return function.apply(player);
    }

    public boolean equals(Object o) {
        return o instanceof PlayerKey<?> && Objects.equals(key, ((PlayerKey<?>) o).key);
    }

    @Override
    public String toString() {
        return "PlayerKey{key='" + key + "', function=" + function + '}';
    }

    private static <T> boolean loadKey0(String key, Function<Player, T> function, boolean isDef) {
        PlayerKey<?> first = new PlayerKey<>(key, function);
        if (isDef) DEFS.add(new PlayerKey<>(first));
        return KEY_SET.add(first);
    }

    private static <T> boolean loadKey0(String key, Function<Player, T> function) {
        return loadKey0(key, function, true);
    }

    private static PlayerKey<?> removeKey0(String key) {
        PlayerKey<?> result = null;

        for (PlayerKey<?> k : KEY_SET) {
            if (!k.key.equals(key))
                continue;

            KEY_SET.remove(result = k);
            break;
        }

        return result;
    }

    /**
     * Removes a key-function pair from the loaded keys using its key.
     *
     * @param key The key string
     * @return True if the key was successfully removed, false otherwise
     */
    public static boolean removeKey(String key) {
        return removeKey0(key) != null;
    }

    /**
     * Loads a new key-function pair and stores it in the loaded keys.
     *
     * @param key The key string
     * @param function The function that returns a value of type T for a given player
     * @param <T> The result of the function.
     *
     * @return true if the key was successfully loaded, false otherwise
     */
    public static <T> boolean loadKey(String key, Function<Player, T> function) {
        return loadKey0(key, function, false);
    }

    /**
     * Edits a key-function pair by changing the key string.
     *
     * @param oldKey The old key string
     * @param newKey The new key string
     *
     * @return True if the key was successfully edited, false otherwise
     */
    public static boolean editKey(String oldKey, String newKey) {
        PlayerKey<?> key = removeKey0(oldKey);
        return key != null && loadKey0(newKey, key.function, false);
    }

    private static <T> List<T> map(Function<PlayerKey<?>, T> f) {
        return CollectionBuilder.of(KEY_SET).map(f).collect(new LinkedList<>());
    }

    /**
     * Replaces all the occurrences of the keys in a given string with their
     * corresponding value functions for a given player.
     *
     * @param player The player to apply the function to
     * @param string The string to replace the keys in
     * @param sensitive A boolean flag that indicates whether the replacement
     *                    is case-sensitive or not
     *
     * @return The modified string after replacing the keys with their values
     */
    public static String replaceKeys(Player player, String string, boolean sensitive) {
        if (player == null || StringUtils.isBlank(string))
            return string;

        StringApplier applier = StringApplier.simplified(string);

        for (PlayerKey<?> k : KEY_SET)
            applier.apply(s -> ValueReplacer.of(
                    k.key,
                    k.apply(player),
                    s, sensitive
            ));

        return applier.toString();
    }

    /**
     * Replaces all the occurrences of the keys in a given string with their
     * corresponding value functions for a given player. The replacement is
     * case-insensitive by default.
     *
     * @param player The player to apply the function to
     * @param string The string to replace the keys in
     *
     * @return The modified string after replacing the keys with their values
     */
    public static String replaceKeys(Player player, String string) {
        return replaceKeys(player, string, false);
    }

    /**
     * Restores the loaded keys to its default configuration.
     */
    public static void setDefaults() {
        KEY_SET.clear();
        KEY_SET.addAll(DEFS);
    }
}
