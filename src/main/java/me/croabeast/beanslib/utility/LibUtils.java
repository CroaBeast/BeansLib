package me.croabeast.beanslib.utility;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;

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

    private static String serverVersion() {
        String temp = TextUtils.STRIP_FIRST_SPACES.apply(
                Bukkit.getVersion().substring(
                Bukkit.getVersion().indexOf("MC:") + 3)
        );
        return temp.substring(0, temp.length() - 1);
    }

    /**
     * Get the spigot-format server version and fork.
     *
     * @return server version and fork
     */
    @SuppressWarnings("deprecation")
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
    public static int majorJavaVersion() {
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
     * Checks if the server is Paper or a fork of it.
     *
     * @return if is Paper environment
     */
    public static boolean isPaper() {
        if (majorVersion() < 8) return false;

        String clazz = majorVersion() >= 12 ?
                "com.destroystokyo.paper.ParticleBuilder" :
                "io.papermc.paperclip.Paperclip";

        try {
            Class.forName(clazz);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the server is in a Windows' environment.
     * <p> Use {@link SystemUtils#IS_OS_WINDOWS} instead.
     *
     * @return if the server is in a Windows system
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.5")
    @Deprecated
    public static boolean isWindows() {
        return SystemUtils.OS_NAME.matches("(?i)Windows");
    }
}
