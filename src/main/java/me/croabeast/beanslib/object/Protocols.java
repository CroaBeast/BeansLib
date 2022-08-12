package me.croabeast.beanslib.object;

import com.viaversion.viaversion.api.*;
import me.croabeast.beanslib.utility.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The enum class for checking the client's protocol version.
 *
 * @author CroaBeast
 * @since 1.0
 */
public enum Protocols {
    /**
     * For 1.7 client.
     */
    CLIENT_7("7", 0, 5),
    /**
     * For 1.8 client.
     */
    CLIENT_8("8", 6, 47),
    /**
     * For 1.9 client.
     */
    CLIENT_9("9", 48, 110),
    /**
     * For 1.10 client.
     */
    CLIENT_10("10", 201, 210, convertTo(206, 209)),
    /**
     * For 1.11 client.
     */
    CLIENT_11("11", 301, 316),
    /**
     * For 1.12 client.
     */
    CLIENT_12("12", 317, 340),
    /**
     * For 1.13 client.
     */
    CLIENT_13("13", 341, 404),
    /**
     * For 1.14 client.
     */
    CLIENT_14("14", 441, 500, convertTo(499)),
    /**
     * For 1.15 client.
     */
    CLIENT_15("15", 550, 578),
    /**
     * For 1.16 client.
     */
    CLIENT_16("16", 701, 754),
    /**
     * For 1.17 client.
     */
    CLIENT_17("17", 755, 756),
    /**
     * For 1.18 client.
     */
    CLIENT_18("18", 757, 758),
    /**
     * For 1.19 client.
     */
    CLIENT_19("19", 759, 800),
    /**
     * For unknown clients.
     */
    UNKNOWN_CLIENT;

    private final List<Integer> protocols;
    private final String majorVersion;

    Protocols(String version, int start, int end, List<Integer> ignore) {
        majorVersion = version;
        List<Integer> range = convertTo(start, end);

        if (ignore == null) {
            protocols = range;
            return;
        }

        List<Integer> list = new ArrayList<>(range);
        for (Integer i : range) {
            if (!ignore.contains(i)) continue;
            list.remove(i);
        }
        protocols = list;
    }

    Protocols(String version, int start, int end) {
        this(version, start, end, null);
    }

    Protocols() {
        this("0", 0, 0);
    }

    /**
     * Gets the protocols list of the major version.
     * @return protocols list
     */
    public List<Integer> protocols() {
        return protocols;
    }

    /**
     * Gets the major version.
     * @return major version
     */
    public int majorVersion() {
        return Integer.parseInt(majorVersion);
    }

    /**
     * Converts an array of numbers to a list.
     * If there are 2 numbers, it will get all the numbers between those numbers.
     * @param numbers numbers' array
     * @return the number list
     */
    private static List<Integer> convertTo(Integer... numbers) {
        if (numbers.length == 2) {
            int z = numbers[1], y = numbers[0];

            Integer[] array = new Integer[(z - y) + 1];
            int index = 0;

            for (int i = y; i <= z; i++) {
                array[index] = i;
                index++;
            }

            return Arrays.asList(array);
        }

        return Arrays.asList(numbers);
    }

    /**
     * Gets the major version from a protocol's number.
     * @param i protocol
     * @return the major version
     */
    public static int getClientVersion(int i) {
        Protocols init = UNKNOWN_CLIENT;

        for (Protocols p : values()) {
            if (p == UNKNOWN_CLIENT) continue;
            if (p.protocols().contains(i)) init = p;
        }

        return init.majorVersion();
    }

    /**
     * Exception for ViaVersion not being enabled.
     */
    private static final UnsupportedOperationException NOT_VIAVERSION_ENABLED =
            new UnsupportedOperationException("ViaVersion needs to be enabled to use this method.");

    /**
     * Gets the major version from a UUID.
     * @param uuid a uuid
     * @return the major version
     * @throws UnsupportedOperationException if ViaVersion is not enabled
     */
    public static int getClientVersion(@NotNull UUID uuid) {
        if (!Exceptions.isPluginEnabled("ViaVersion"))
            throw NOT_VIAVERSION_ENABLED;

        return getClientVersion(Via.getAPI().getPlayerVersion(uuid));
    }

    /**
     * Gets the major version from a player's client.
     * @param player a player
     * @return the major version
     * @throws UnsupportedOperationException if ViaVersion is not enabled
     * @throws NullPointerException if player is null
     */
    public static int getClientVersion(Player player) {
        return getClientVersion(Exceptions.checkPlayer(player).getUniqueId());
    }
}
