package me.croabeast.beanslib.object.misc;

import com.viaversion.viaversion.api.Via;
import lombok.Getter;
import me.croabeast.beanslib.utility.Exceptions;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * The class for checking the client's protocol version.
 *
 * @author CroaBeast
 * @since 1.0
 */
@Getter
public final class ClientVersion {

    private static final List<ClientVersion> PROTOCOL_LIST = new ArrayList<>();

    static final ClientVersion
            UNKNOWN_CLIENT = new ClientVersion(0, 0, 0),
            CLIENT_7 = new ClientVersion(7, 0, 5),
            CLIENT_8 = new ClientVersion(8, 6, 47),
            CLIENT_9 = new ClientVersion(9, 48, 110),
            CLIENT_10 = new ClientVersion(10, 201, 210, fromInts(206, 209)),
            CLIENT_11 = new ClientVersion(11, 301, 316),
            CLIENT_12 = new ClientVersion(12, 317, 340),
            CLIENT_13 = new ClientVersion(13, 341, 404),
            CLIENT_14 = new ClientVersion(14, 441, 500, fromInts(499)),
            CLIENT_15 = new ClientVersion(15, 550, 578),
            CLIENT_16 = new ClientVersion(16, 701, 754),
            CLIENT_17 = new ClientVersion(17, 755, 756),
            CLIENT_18 = new ClientVersion(18, 757, 758),
            CLIENT_19 = new ClientVersion(19, 759, 800);

    /**
     * The major version of the client.
     */
    private final int version;

    /**
     * The protocols list of the major version.
     */
    private final List<Integer> protocols;

    ClientVersion(int version, int start, int end, List<Integer> ignore) {
        this.version = version;

        List<Integer> range = fromInts(start, end);

        if (ignore == null || ignore.isEmpty()) {
            protocols = range;
            return;
        }

        range.removeIf(ignore::contains);

        protocols = range;
        PROTOCOL_LIST.add(this);
    }

    ClientVersion(int version, int start, int end) {
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
        return getVersion() == 0 ? "UNKNOWN_CLIENT:0" : ("CLIENT:" + getVersion());
    }

    /**
     * Converts an array of numbers to a list.
     * If there are 2 numbers, it will get all the numbers between those numbers.
     *
     * @param numbers numbers' array
     * @return the number list
     */
    private static List<Integer> fromInts(Integer... numbers) {
        if (numbers.length != 2)
            return Arrays.asList(numbers);

        int z = numbers[1], y = numbers[0];

        Integer[] array = new Integer[(z - y) + 1];
        int index = 0;

        for (int i = y; i <= z; i++) {
            array[index] = i;
            index++;
        }

        return Arrays.asList(array);
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
     * <p> Returns <strong><code>0</code></strong> if ViaVersion is not enabled or player is null.
     *
     * @param player a player
     * @return the major version
     */
    public static int getClientVersion(Player player) {
        int o = UNKNOWN_CLIENT.getVersion();

        if (!Exceptions.isPluginEnabled("ViaVersion"))
            return o;

        try {
            Exceptions.checkPlayer(player);
        } catch (Exception e) {
            return o;
        }

        UUID u = player.getUniqueId();
        int i = Via.getAPI().getPlayerVersion(u);

        for (ClientVersion p : values()) {
            if (p == UNKNOWN_CLIENT) continue;

            if (p.getProtocols().contains(i))
                return p.getVersion();
        }

        return o;
    }
}
