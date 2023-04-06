package me.croabeast.beanslib.time;

import org.jetbrains.annotations.ApiStatus;

/**
 * Basic time utilities for any server.
 *
 * @deprecated See {@link TimeParser}.
 *
 * @author Kihsomray
 * @version 1.0
 */
@ApiStatus.ScheduledForRemoval(inVersion = "1.4")
@Deprecated
public final class TimeUtils {

    /**
     * Initializing this class is blocked.
     */
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