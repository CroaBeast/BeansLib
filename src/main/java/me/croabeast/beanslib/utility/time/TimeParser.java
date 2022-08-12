package me.croabeast.beanslib.utility.time;

import me.croabeast.beanslib.utility.TextUtils;
import org.bukkit.entity.Player;

/**
 * An object that handles a time in seconds and converts it in a formatted string.
 * <p> You can see a very basic example here:
 * <pre>{@code player.sendMessage(new TimeParser(56981).formatTime())}</pre>
 * You can also have custom keys creating a new instance of the {@link TimeKeys} object.
 * <pre>{@code
 * TimeKeys keys = new TimeKeys();
 * // Here I'm changing the splitter.
 * keys = keys.setSplitter("; ");
 * // Then I set it on an instance of TimeParser.
 * TimeParser parser = new TimeParser(keys, 2132);
 * }</pre>
 * @author Kihsomray
 * @fork CroaBeast
 * @version 1.3
 */
public class TimeParser {

    /**
     * The seconds that are stored in a minute.
     */
    public static final int MINUTE = 60;
    /**
     * The seconds that are stored in an hour.
     */
    public static final int HOUR = 60 * MINUTE;
    /**
     * The seconds that are stored in a day.
     */
    public static final int DAY = 24 * HOUR;
    /**
     * The seconds that are stored in a week.
     */
    public static final int WEEK = 7 * DAY;
    /**
     * The seconds that are stored in a month.
     */
    public static final int MONTH = 30 * DAY;
    /**
     * The seconds that are stored in a year.
     */
    public static final int YEAR = 365 * DAY;

    private final TimeKeys keys;
    private final long seconds;

    public TimeParser(TimeKeys keys, long seconds) {
        this.keys = keys == null ? TimeKeys.DEFAULT_KEYS : keys;
        this.seconds = seconds;
    }

    public TimeParser(long seconds) {
        this(null, seconds);
    }

    /**
     * Gets a proper time for a time format.
     * @param seconds the time in seconds
     * @param formatter a seconds format
     * @return the fixed time
     */
    private long getFixedTime(long seconds, long formatter) {
        return (seconds - (seconds % formatter)) / formatter;
    }

    /**
     * Checks if a string is in its plural format.
     * @param value a value in seconds
     * @param string the string to check
     * @return the converted string
     */
    private String checkPlural(long value, String string) {
        string = value + " " + string;
        if (value == 1)
            return string.replaceAll(keys.getPluralRegex(), "");

        return string.
                replace(keys.getStartDelimiter(), "").
                replace(keys.getEndDelimiter(), "");
    }

    /**
     * Takes in a value in seconds and returns a very nicely formatted string
     * that can contain seconds, minutes, hours and days.
     * @param parser a player to parse colors and placeholders
     * @return formatted string with seconds, minutes, hours and days
     */
    public String formatTime(Player parser) {
        final String split = keys.getSplitter();
        long result = this.seconds;

        if (result <= 0)
            return TextUtils.colorize(null, parser, checkPlural(0, keys.getSecondFormat()));

        StringBuilder formattedTime = new StringBuilder();
        long years, months, weeks, days, hours, mins;

        years = getFixedTime(result, YEAR);
        result = result - (years * YEAR);

        if (years > 0) formattedTime.
                append(checkPlural(years, keys.getYearFormat())).
                append(split);

        months = getFixedTime(result, MONTH);
        result = result - (months * MONTH);

        if (months > 0) formattedTime.
                append(checkPlural(months, keys.getMonthFormat())).
                append(split);

        weeks = getFixedTime(result, WEEK);
        result = result - (weeks * WEEK);

        if (weeks > 0) formattedTime.
                append(checkPlural(weeks, keys.getWeekFormat())).
                append(split);

        days = getFixedTime(result, DAY);
        result = result - (days * DAY);

        if (days > 0) formattedTime.
                append(checkPlural(days, keys.getDayFormat())).
                append(split);

        hours = getFixedTime(result, HOUR);
        result = result - (hours * HOUR);

        if (hours > 0) formattedTime.
                append(checkPlural(hours, keys.getHourFormat())).
                append(split);

        mins = getFixedTime(result, MINUTE);
        result = result - (mins * MINUTE);

        if (mins > 0) formattedTime.
                    append(checkPlural(mins, keys.getMinuteFormat())).
                    append(split);

        if (result > 0) formattedTime.
                append(checkPlural(result, keys.getSecondFormat() + split));

        return TextUtils.colorize(null, parser,
                formattedTime.substring(0,
                formattedTime.length() - split.length())
        );
    }

    /**
     * Takes in a value in seconds and returns a very nicely formatted string
     * that can contain seconds, minutes, hours and days.
     * @return formatted string with seconds, minutes, hours and days
     */
    public String formatTime() {
        return formatTime(null);
    }
}
