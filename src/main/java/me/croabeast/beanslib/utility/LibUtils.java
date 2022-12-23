package me.croabeast.beanslib.utility;

import net.md_5.bungee.api.chat.ClickEvent;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;

import java.util.regex.Pattern;

/**
 * The class that stores static keys for easy access and management.
 *
 * @author CroaBeast
 * @since 1.0
 */
public final class LibUtils {

    /**
     * Initializing this class is blocked.
     */
    private LibUtils() {}

    /**
     * A prefix used in the main pattern to identify the event.
     * This can't be overridden.
     */
    private static final String JSON_PREFIX = "(.[^|]*?):\"(.[^|]*?)\"";

    /**
     * The main pattern to identify the JSON message in a string.
     * <p> Keep in mind that every string can only have one {@link ClickEvent.Action};
     * a click action has this format:
     * <pre> {@code
     * Available Actions: RUN, SUGGEST, URL and all ClickAction values.
     * "<ACTION>:<the click string>" -> "RUN:/me click to run"
     * }</pre>
     * <p> Examples:
     * <pre> {@code
     * String hover = "<hover:\"a hover line\">text to apply</text>";
     * String click = "<run:\"/click me\">text to apply</text>";
     * String mixed = "<hover:\"a hover line<n>another line\"|run:\"/command\">text to apply</text>";
     * }</pre>
     */
    public static final Pattern JSON_PATTERN =
            Pattern.compile("(?i)<(" + JSON_PREFIX + "([|]" + JSON_PREFIX + ")?)>(.+?)</text>");

    private static String serverVersion() {
        String temp = Bukkit.getVersion();

        temp = TextUtils.removeSpace(temp.substring(temp.indexOf("MC:") + 3));
        return temp.substring(0, temp.length() - 1);
    }

    /**
     * Get the spigot-format server version and fork.
     *
     * @return server version and fork
     */
    public static String serverFork() {
        return WordUtils.capitalize(Bukkit.getName()) + " " + serverVersion();
    }

    /**
     * Gets the major version of the server.
     * <p> Example: if version is <strong>1.16.5</strong>, will return <strong>16</strong>
     *
     * @return server's major version
     */
    public static int majorVersion() {
        return Integer.parseInt(serverVersion().split("\\.", 3)[1]);
    }

    /**
     * Gets the Java Major version of the server.
     * <p> Example: if version is <strong>1.8.0.302</strong>, will return <strong>8</strong>
     *
     * @return server's java version
     */
    public static int javaVersion() {
        String version = SystemUtils.JAVA_VERSION;

        if (!version.startsWith("1.")) {
            int dot = version.indexOf(".");
            if (dot != -1)
                version = version.substring(0, dot);
        }
        else version = version.substring(2, 3);

        return Integer.parseInt(version);
    }

    /**
     * Checks if the server is in a Windows' environment.
     * <p> Use {@link SystemUtils#IS_OS_WINDOWS} instead.
     *
     * @return if the server is in a Windows system
     */
    @Deprecated
    public static boolean isWindows() {
        return SystemUtils.OS_NAME.matches("(?i)Windows");
    }
}
