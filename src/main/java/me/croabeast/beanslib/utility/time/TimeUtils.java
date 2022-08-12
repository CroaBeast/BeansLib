package me.croabeast.beanslib.utility.time;

/**
 * Basic time utilities for any server.
 * <p> Use {@link TimeParser} for better management.
 *
 * @author Kihsomray
 * @version 1.0
 */
@Deprecated
public final class TimeUtils {

    private TimeUtils() {}

    /**
     * Takes in a value in seconds and returns a very nicely formatted string
     * that can contain seconds, minutes, hours and days.
     *
     * @param seconds Amount of seconds to format
     * @return formatted string with seconds, minutes, hours and days
     */
    public static String formatTime(long seconds) {
        return new TimeParser(seconds).formatTime();
    }
}