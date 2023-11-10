package me.croabeast.beanslib.utility;

import lombok.experimental.UtilityClass;
import lombok.var;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;

import java.util.regex.Pattern;

/**
 * The class that stores static keys for easy access and management.
 *
 * @author CroaBeast
 * @since 1.0
 */
@UtilityClass
public class LibUtils {

    /**
     * Get the spigot-format server version and fork.
     *
     * @return server version and fork
     */
    public String serverFork() {
        return WordUtils.capitalize(Bukkit.getName()) + " 1." + getBukkitVersion();
    }

    /**
     * Retrieves the version number of the Bukkit package that is currently running.
     *
     * @return the Bukkit package version
     */
    public String getBukkitVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    /**
     * Returns the main version of the server in a double/decimal format.
     * If version is <code>1.16.5</code>, will return <code>16.5</code>.
     *
     * @return server's main version, <code>0.0</code> if an error occurs.
     */
    public double getMainVersion() {
        var m = Pattern.
                compile("1\\.(\\d+(\\.\\d+)?)").
                matcher(Bukkit.getVersion());

        if (!m.find()) return 0.0;

        try {
            return Double.parseDouble(m.group(1));
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Returns the major version of the active server.
     * If version is <code>1.16.5</code>, will return <code>16</code>.
     *
     * @return server's major version, <code>0</code> if an error occurs.
     * @deprecated See {@link #getMainVersion()} to get the double value using the minor
     *             and patch version of the active server.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.4")
    @Deprecated
    public int majorVersion() {
        return (int) getMainVersion();
    }

    /**
     * Returns the Java major version of the server.
     * <p> Example: if version is <code>1.8.0.302</code>, will return <code>8</code>.
     *
     * @return server's java version
     */
    public int majorJavaVersion() {
        var version = SystemUtils.JAVA_VERSION;

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
    public boolean isPaper() {
        if (getMainVersion() < 8) return false;

        var clazz = getMainVersion() >= 12.0 ?
                "com.destroystokyo.paper.ParticleBuilder" :
                "io.papermc.paperclip.Paperclip";

        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks if the server is in a Windows' environment.
     *
     * @return if the server is in a Windows system
     * @deprecated Use {@link SystemUtils#IS_OS_WINDOWS} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.4")
    @Deprecated
    public boolean isWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }
}
