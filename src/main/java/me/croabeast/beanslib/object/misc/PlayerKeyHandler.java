package me.croabeast.beanslib.object.misc;

import lombok.Setter;
import lombok.experimental.Accessors;
import me.croabeast.beanslib.utility.TextUtils;
import org.apache.commons.lang.StringUtils;
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
 * <p> • Example: <pre>{@code
 * PlayerKeyHandler handler = new PlayerKeyHandler();
 * // or using the BeansLib#getPlayerKeys() to get its instance of the lib
 *
 * handler.setKey(0, "{playerName}"). // sets a key of an existing key
 *      createKey("{prefix}", p -> player.getPrefix()); // create a new key
 * }</pre>
 *
 * Then use the method {@link #parsePlayerKeys(Player, String, boolean)} to parse
 * all the stored keys in an input string with a player as a reference.
 *
 * <p> • Example: <pre>{@code
 * String s = handler.parsePlayerKeys(player, "hello {player} :D", true);
 * }</pre>
 */
public final class PlayerKeyHandler {

    private static final Map<Integer, Key> KEY_MAP = new HashMap<>();

    /**
     * Creates a new instance of the handler.
     */
    public PlayerKeyHandler() {
        new Key("{player}", HumanEntity::getName);
        new Key("{playerDisplayName}", Player::getDisplayName);
        new Key("{playerUUID}", p -> p.getUniqueId() + "");
        new Key("{playerWorld}", p -> p.getWorld().getName());
        new Key("{playerGameMode}", p -> p.getGameMode() + "");

        new Key("{playerX}", p -> round(p.getLocation().getX()) + "");
        new Key("{playerY}", p -> round(p.getLocation().getY()) + "");
        new Key("{playerZ}", p -> round(p.getLocation().getZ()) + "");

        new Key("{playerYaw}", p -> round(p.getLocation().getYaw()) + "");
        new Key("{playerPitch}", p -> round(p.getLocation().getPitch()) + "");
    }

    /**
     * Creates a new key instance that will be stored with the
     * default player keys.
     *
     * @param key a key for the object
     * @param function a function to get the respective player value
     *
     * @return a reference of this object
     */
    public PlayerKeyHandler createKey(String key, Function<Player, String> function) {
        if (StringUtils.isBlank(key)) return this;
        if (function == null) return this;

        new Key(key, (PlayerFunction) function);
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

        Key k = KEY_MAP.getOrDefault(index, null);
        if (k != null) k.setKey(key);

        return this;
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
    public String parsePlayerKeys(Player player, String string, boolean c) {
        if (StringUtils.isBlank(string)) return string;
        if (player == null) return string;

        for (Key key : KEY_MAP.values())
            string = key.parseKey(player, string, c);
        return string;
    }

    interface PlayerFunction extends Function<Player, String> {}

    static class Key {

        private static int ordinal = 0;

        @Accessors(chain = true)
        @Setter
        private String key;

        private final int index;
        private final PlayerFunction function;

        private Key(String key, PlayerFunction function) {
            this.key = key;
            this.index = ordinal;
            this.function = function;

            KEY_MAP.put(ordinal, this);
            ordinal++;
        }

        @Override
        public String toString() {
            return TextUtils.classFormat(this, ":", false, index, key);
        }

        public String parseKey(Player player, String string, boolean c) {
            return TextUtils.replaceEach(string, key, function.apply(player), c);
        }
    }
}
