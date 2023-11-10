package me.croabeast.neoprismatic.util;

import com.google.common.collect.Lists;
import com.viaversion.viaversion.api.Via;
import me.croabeast.beanslib.utility.Exceptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * The class for checking the client's protocol version.
 *
 * @author CroaBeast
 * @since 1.0
 */
public final class ClientVersion {

    private static final List<ClientVersion> PROTOCOL_LIST = new ArrayList<>();

    private static final ClientVersion UNKNOWN = new ClientVersion(0, 0, 0);

    static {
        new ClientVersion(7, 0, 5);
        new ClientVersion(8, 6, 47);
        new ClientVersion(9, 48, 110);
        new ClientVersion(10, 201, 210, fromInts(206, 209));
        new ClientVersion(11, 301, 316);
        new ClientVersion(12, 317, 340);
        new ClientVersion(13, 341, 404);
        new ClientVersion(14, 441, 500, fromInts(499));
        new ClientVersion(15, 550, 578);
        new ClientVersion(16, 701, 754);
        new ClientVersion(17, 755, 756);
        new ClientVersion(18, 757, 758);
        new ClientVersion(19, 759, 762);
        new ClientVersion(20, 763, 800);
    }

    /**
     * The major version of the client.
     */
    private final int version;

    /**
     * The protocols list of the major version.
     */
    private final List<Integer> protocols;

    private static List<Integer> fromInts(Integer... numbers) {
        if (numbers.length != 2)
            return Lists.newArrayList(numbers);

        int z = numbers[1], y = numbers[0];

        Integer[] array = new Integer[(z - y) + 1];
        int index = 0;

        for (int i = y; i <= z; i++) {
            array[index] = i;
            index++;
        }

        return new ArrayList<>(Arrays.asList(array));
    }

    private ClientVersion(int version, int start, int end, List<Integer> ignore) {
        this.version = version;

        List<Integer> range = fromInts(start, end);

        if (ignore == null || ignore.isEmpty()) {
            protocols = range;
            PROTOCOL_LIST.add(this);
            return;
        }

        range.removeIf(ignore::contains);

        protocols = range;
        PROTOCOL_LIST.add(this);
    }

    private ClientVersion(int version, int start, int end) {
        this(version, start, end, null);
    }

    /**
     * Returns a string representation of the object. The result should be a
     * concise but informative representation that is easy for a person to read.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return version == 0 ? "UNKNOWN_CLIENT:0" : ("CLIENT:" + version);
    }

    /**
     * Returns an array containing the constants of this class, in the
     * order they're declared. This method may be used to iterate over the
     * constants as follows:
     * <pre> {@code
     *      for (Protocol protocol : Protocol.values())
     *          System.out.println(protocol);
     * } </pre>
     *
     * @return an array containing the constants of this class
     */
    public static ClientVersion[] values() {
        return PROTOCOL_LIST.toArray(new ClientVersion[0]);
    }

    /**
     * Gets the major version from a player's client.
     *
     * <p> Returns {@code 0} if ViaVersion is not enabled or player is null.
     *
     * @param player a player
     * @return the major version
     */
    public static int getClientVersion(Player player) {
        int o = UNKNOWN.version;

        if (!Exceptions.isPluginEnabled("ViaVersion"))
            return o;

        if (player == null) return o;

        int i = Via.getAPI().getPlayerVersion(player.getUniqueId());

        for (ClientVersion p : values()) {
            if (p == UNKNOWN) continue;

            if (p.protocols.contains(i)) return p.version;
        }

        return o;
    }

    public static boolean isLegacy(Player player) {
        return getClientVersion(player) <= 15;
    }
}
