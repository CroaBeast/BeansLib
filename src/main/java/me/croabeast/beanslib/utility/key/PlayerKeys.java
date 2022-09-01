package me.croabeast.beanslib.utility.key;

import lombok.Setter;
import lombok.experimental.Accessors;
import me.croabeast.beanslib.utility.Exceptions;
import me.croabeast.beanslib.utility.TextUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * The object to handles the player's keys and values.
 */
@Setter
@Accessors(chain = true)
public class PlayerKeys {

    /**
     * The key of the player's name.
     *
     * @param playerKey a key
     */
    private String playerKey = "{player}";
    /**
     * The key of the player's display name.
     *
     * @param displayKey a key
     */
    private String displayKey = "{playerDisplayName}";
    /**
     * The key of the player's UUID.
     *
     * @param uuidKey a key
     */
    private String uuidKey = "{playerUUID}";
    /**
     * The key of the player's world.
     *
     * @param worldKey a key
     */
    private String worldKey = "{playerWorld}";
    /**
     * The key of the player's game mode.
     *
     * @param gamemodeKey a key
     */
    private String gamemodeKey = "{playerGameMode}";

    /**
     * The key of the player's "x" coordinate.
     *
     * @param xKey a key
     */
    private String xKey = "{playerX}";
    /**
     * The key of the player's "y" coordinate.
     *
     * @param yKey a key
     */
    private String yKey = "{playerY}";
    /**
     * The key of the player's "z" coordinate.
     *
     * @param zKey a key
     */
    private String zKey = "{playerZ}";
    /**
     * The key of the player's yaw vector.
     *
     * @param yawKey a key
     */
    private String yawKey = "{playerYaw}";
    /**
     * The key of the player's pitch vector.
     *
     * @param pitchKey a key
     */
    private String pitchKey = "{playerPitch}";

    private String[] getKeys() {
        return new String[] {
                playerKey, displayKey, uuidKey, worldKey,
                gamemodeKey, xKey, yKey, zKey, yawKey, pitchKey
        };
    }

    private String[] getValues(Player player) {
        Location location = Exceptions.checkPlayer(player).getLocation();

        return new String[] {
                player.getName(), player.getDisplayName(), player.getUniqueId() + "",
                player.getWorld().getName(), player.getGameMode() + "",
                location.getX() + "", location.getY() + "", location.getZ() + "",
                location.getYaw() + "", location.getPitch() + ""
        };
    }

    /**
     * Parses the players keys for its respective values.
     *
     * @param player a player, can not be null
     * @param string an input string
     * @param caseSensitive if the keys are case-sensitive
     *
     * @return the string with the respective values
     */
    public String parseKeys(Player player, String string, boolean caseSensitive) {
        String[] k = getKeys(), v;

        try {
            v = getValues(player);
        } catch (Exception e) {
            return string;
        }

        return TextUtils.replaceEach(string, k, v, caseSensitive);
    }
}
