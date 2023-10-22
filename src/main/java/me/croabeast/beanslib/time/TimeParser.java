package me.croabeast.beanslib.time;

import lombok.var;
import me.croabeast.beanslib.Beans;
import org.bukkit.entity.Player;

/**
 * An object that handles a time in seconds and converts it in a formatted string.
 * You can see a very basic example here:
 * <pre> {@code player.sendMessage(new TimeParser(56981).formatTime())} </pre>
 * You can also have custom keys creating a new instance of the {@link TimeKeys} object.
 * <pre> {@code
 * TimeKeys keys = new TimeKeys();
 * // Here I'm changing the splitter.
 * keys = keys.setSplitter("; ");
 * // Then I set it on an instance of TimeParser.
 * TimeParser parser = new TimeParser(keys, 2132);
 * } </pre>
 *
 * @author Kihsomray (forked by CroaBeast)
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

    /**
     * Creates a new parser using a {@link TimeKeys} instance and an amount of seconds.
     *
     * @param keys a {@link TimeKeys} instance
     * @param seconds time in seconds
     */
    public TimeParser(TimeKeys keys, long seconds) {
        this.keys = keys == null ? TimeKeys.DEFAULT_KEYS : keys;
        this.seconds = seconds;
    }

    /**
     * Creates a new parser using an amount of seconds.
     *
     * @param seconds time in seconds
     */
    public TimeParser(long seconds) {
        this(null, seconds);
    }

    private long getFixedTime(long seconds, long formatter) {
        return (seconds - (seconds % formatter)) / formatter;
    }

    private String isPlural(long value, String string) {
        string = value + " " + string;
        if (value == 1)
            return string.replaceAll(TimeKeys.PLURAL_REGEX, "");

        return string.replace("(", "").replace(")", "");
    }

    /**
     * Takes in a value in seconds and returns a very nicely formatted string
     * that can contain seconds, minutes, hours, days, weeks, months and years.
     *
     * @param parser a player to parse colors and placeholders
     * @return formatted string with seconds, minutes, hours and days
     */
    public String formatTime(Player parser) {
        final String split = keys.getSplitter();
        long result = this.seconds;

        if (result <= 0)
            return Beans.colorize(parser, isPlural(0, keys.getSecondFormat()));

        var formattedTime = new StringBuilder();
        long years, months, weeks, days, hours, mins;

        years = getFixedTime(result, YEAR);
        result = result - (years * YEAR);

        if (years > 0) formattedTime
                .append(isPlural(years, keys.getYearFormat()))
                .append(split);

        months = getFixedTime(result, MONTH);
        result = result - (months * MONTH);

        if (months > 0) formattedTime
                .append(isPlural(months, keys.getMonthFormat()))
                .append(split);

        weeks = getFixedTime(result, WEEK);
        result = result - (weeks * WEEK);

        if (weeks > 0) formattedTime
                .append(isPlural(weeks, keys.getWeekFormat()))
                .append(split);

        days = getFixedTime(result, DAY);
        result = result - (days * DAY);

        if (days > 0) formattedTime
                .append(isPlural(days, keys.getDayFormat()))
                .append(split);

        hours = getFixedTime(result, HOUR);
        result = result - (hours * HOUR);

        if (hours > 0) formattedTime
                .append(isPlural(hours, keys.getHourFormat()))
                .append(split);

        mins = getFixedTime(result, MINUTE);
        result = result - (mins * MINUTE);

        if (mins > 0) formattedTime
                .append(isPlural(mins, keys.getMinuteFormat()))
                .append(split);

        if (result > 0) formattedTime
                .append(isPlural(result, keys.getSecondFormat() + split));

        int time = formattedTime.length(), s = split.length();
        return Beans.colorize(parser, formattedTime.substring(0, time - s));
    }

    /**
     * Takes in a value in seconds and returns a very nicely formatted string
     * that can contain seconds, minutes, hours, days, weeks, months and years.
     *
     * @return formatted string with seconds, minutes, hours and days
     */
    public String formatTime() {
        return formatTime(null);
    }
}
