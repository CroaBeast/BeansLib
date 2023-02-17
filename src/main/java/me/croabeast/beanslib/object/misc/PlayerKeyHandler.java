package me.croabeast.beanslib.object.misc;

import lombok.Setter;
import lombok.experimental.Accessors;
import me.croabeast.beanslib.utility.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static me.croabeast.beanslib.object.misc.Rounder.round;

/**
 * The {@code PlayerKeyHandler} class manages all the player variables that
 * can be replaced to its respective defined values.
 *
 * <p> It's possible to add more keys using {@link #createKey(String, Function)}, and
 * replace existing keys using {@link #setKey(int, String)}. Both methods can be
 * cast as a chain in the same object instance or as void methods.
 *
 * <p> • Example: <pre> {@code
 * PlayerKeyHandler handler = new PlayerKeyHandler();
 * // or using the BeansLib#getPlayerKeys() to get its instance of the lib
 *
 * handler.setKey(0, "{playerName}"). // sets a key of an existing key
 *      createKey("{prefix}", p -> player.getPrefix()); // create a new key
 * } </pre>
 *
 * Then use the method {@link #parseKeys(Player, String, boolean)} to parse
 * all the stored keys in an input string with a player as a reference.
 *
 * <p> • Example: <pre> {@code
 * String s = handler.parsePlayerKeys(player, "hello {player} :D", true);
 * } </pre>
 */
public final class PlayerKeyHandler {

    private static final Map<Integer, PlayerKey> KEY_MAP = new HashMap<>();
    private static final Map<Integer, PlayerKey> DEFAULTS = new HashMap<>();

    private static Location loc(Player p) { return p.getLocation(); }

    /**
     * Creates a new instance of the handler.
     */
    public PlayerKeyHandler() {
        new PlayerKey("{player}", HumanEntity::getName);
        new PlayerKey("{playerDisplayName}", Player::getDisplayName);

        new PlayerKey("{playerUUID}", Entity::getUniqueId);
        new PlayerKey("{playerWorld}", p -> p.getWorld().getName());
        new PlayerKey("{playerGameMode}", HumanEntity::getGameMode);

        new PlayerKey("{playerX}", p -> round(loc(p).getX()));
        new PlayerKey("{playerY}", p -> round(loc(p).getY()));
        new PlayerKey("{playerZ}", p -> round(loc(p).getZ()));

        new PlayerKey("{playerYaw}", p -> round(loc(p).getYaw()));
        new PlayerKey("{playerPitch}", p -> round(loc(p).getPitch()));

        DEFAULTS.putAll(KEY_MAP);
    }

    /**
     * Creates a new key instance that will be stored with the default player keys.
     *
     * @param key a key for the object
     * @param function a function to get the respective player value
     *
     * @return a reference of this object
     */
    public PlayerKeyHandler createKey(String key, Function<Player, Object> function) {
        if (StringUtils.isBlank(key)) return this;
        if (function == null) return this;

        new PlayerKey(key, (PlayerFunction<Object>) function);
        return this;
    }

    /**
     * Sets a key of an existing key instance having an index as
     * a reference in which order was created.
     *
     * <ul>
     *   <li>0: player's name - "{player}"</li>
     *   <li>1: player's display name - "{displayName}"</li>
     *   <li>2: player's UUID - "{playerUUID}"</li>
     *   <li>3: player's world - "{playerWorld}"</li>
     *   <li>4: player's gamemode - "{playerGameMode}"</li>
     *   <li>5: player's X location - "{playerX}"</li>
     *   <li>6: player's Y location - "{playerY}"</li>
     *   <li>7: player's Z location - "{playerZ}"</li>
     *   <li>8: player's yaw vector - "{playerYaw}"</li>
     *   <li>9: player's pitch vector - "{playerPitch}"</li>
     *   <li>10 or more: custom keys that can be created</li>
     * </ul>
     *
     * @param index the index of the key
     * @param key a key that will replace the old one
     *
     * @return a reference of this object
     */
    public PlayerKeyHandler setKey(int index, String key) {
        if (StringUtils.isBlank(key)) return this;

        PlayerKey k = KEY_MAP.getOrDefault(index, null);
        if (k != null) k.setKey(key);

        return this;
    }

    /**
     * Rollbacks any change in the default keys and removes any custom key stored.
     * <p> Usefully for reload methods that depend on cache.
     */
    public void setDefaults() {
        KEY_MAP.clear();
        KEY_MAP.putAll(DEFAULTS);
    }

    /**
     * Parses the players keys for its respective values.
     *
     * @param player a player, can not be null
     * @param string an input string
     * @param c if the keys are case-sensitive
     *
     * @return the string with the respective values
     */
    public String parseKeys(Player player, String string, boolean c) {
        if (StringUtils.isBlank(string)) return string;
        if (player == null) return string;

        for (PlayerKey playerKey : KEY_MAP.values())
            string = playerKey.parseKey(player, string, c);
        return string;
    }

    public interface PlayerFunction<O> extends Function<Player, O> {}

    static class PlayerKey {

        private static int ordinal = 0;

        @Accessors(chain = true)
        @Setter
        private String key;

        private final int index;
        private final PlayerFunction<String> function;

        private PlayerKey(String key, PlayerFunction<Object> function) {
            this.key = key;
            this.index = ordinal;

            this.function = p -> String.valueOf(function.apply(p));

            KEY_MAP.put(ordinal, this);
            ordinal++;
        }

        @Override
        public String toString() {
            return TextUtils.classFormat(this, ":", false, index, key);
        }

        String parseKey(Player player, String string, boolean c) {
            return TextUtils.replaceEach(string, key, function.apply(player), c);
        }
    }
}
