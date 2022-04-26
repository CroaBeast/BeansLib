package me.croabeast.beanslib.utilities;

import me.croabeast.iridiumapi.IridiumAPI;

/**
 * Basic time utilities for any server.
 *
 * @author Kihsomray
 * @version 1.0
 */
public class TimeUtils {

    // formatting strings
    private static String secondFormat = "{time} Second(s)";
    private static String minuteFormat = "{time} Minute(s)";
    private static String hourFormat = "{time} Hour(s)";
    private static String dayFormat = "{time} Day(s)";
    private static String splitterFormat = ", ";

    // fields needed for time formatter
    private static String pluralRegex = "\\s*\\([^)]*\\)\\s*";
    private static char startDelimiter = '(';
    private static char endDelimiter = ')';

    private static String colorize(String input) {
        return IridiumAPI.process(input);
    }

    /**
     * Takes in seconds and returns a very nicely formatted string
     * that can contain seconds, minutes, hours and days.
     *
     * @param seconds Amount of seconds to format
     * @return Formatted string with seconds, minutes, hours and days
     */
    public static String formatTime(long seconds) {

        // if time 0, return right away
        if (seconds <= 0) return colorize(checkPluralFormat(0, secondFormat));

        String formattedTime = "";
        long daysTotal, hoursTotal, minutesTotal;

        // gets day time
        daysTotal = getFixedTime(seconds, 86400);
        seconds = seconds - (daysTotal * 86400);
        if (daysTotal > 0) formattedTime += (checkPluralFormat(daysTotal, dayFormat) + splitterFormat);

        // gets hour time
        hoursTotal = getFixedTime(seconds, 3600);
        seconds = seconds - (hoursTotal * 3600);
        if (hoursTotal > 0) formattedTime += checkPluralFormat(hoursTotal, hourFormat) + splitterFormat;

        // gets minute time
        minutesTotal = getFixedTime(seconds, 60);
        seconds = seconds - (minutesTotal * 60);
        if (minutesTotal > 0) formattedTime += checkPluralFormat(minutesTotal, minuteFormat) + splitterFormat;

        // gets second time
        if (seconds > 0) formattedTime += checkPluralFormat(seconds, secondFormat + splitterFormat);

        // returns final string
        return colorize(formattedTime.substring(0, formattedTime.length() - splitterFormat.length()));

    }

    // gets proper time for a time format
    private static long getFixedTime(long seconds, long formatter) {
        long tempSeconds = seconds % formatter;
        return (seconds - tempSeconds) / formatter;
    }

    // checks plural formatting and applies it
    private static String checkPluralFormat(long value, String string) {
        string = string.replace("{time}", value + "");
        if (value == 1) return string.replaceAll(pluralRegex, "");
        else return string.replace(startDelimiter + "", "").replace(endDelimiter + "", "");
    }

    /**
     * Change the days in time formatter, placeholders: {time}.
     *
     * @param format string to use as days
     */
    public static void setDayFormat(String format) {
        dayFormat = format;
    }

    /**
     * Change the hours in time formatter, placeholders: {time}.
     *
     * @param format string to use as hours
     */
    public static void setHourFormat(String format) {
        hourFormat = format;
    }

    /**
     * Change the minutes in time formatter, placeholders: {time}.
     *
     * @param format string to use as minutes
     */
    public static void setMinuteFormat(String format) {
        minuteFormat = format;
    }

    /**
     * Change the seconds in time formatter, placeholders: {time}.
     *
     * @param format string to use as seconds
     */
    public static void setSecondFormat(String format) {
        secondFormat = format;
    }

    /**
     * Change the splitter in time formatter.
     *
     * @param format string to use as splitter
     */
    public static void setSplitterFormat(String format) {
        splitterFormat = format;
    }

    /**
     * When there is only one item, what should be replaced?
     */
    public static void pluralRegexFormat(String regex) {
        pluralRegex = regex;
    }

    /**
     * Start delimiter for time formatter.
     *
     * @param delimiter Delimiter of your choice
     */
    public static void setStartDelimiter(char delimiter) {
        startDelimiter = delimiter;
    }

    /**
     * End delimiter for time formatter.
     *
     * @param delimiter Delimiter of your choice
     */
    public static void setEndDelimiter(char delimiter) {
        endDelimiter = delimiter;
    }

}