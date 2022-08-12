package me.croabeast.beanslib.utility.time;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class TimeKeys {

    /**
     * A static variable using the default keys without any changes.
     */
    public static final TimeKeys DEFAULT_KEYS = new TimeKeys();

    /**
     * A static variable using the correct spanish translation of the formats.
     */
    public static final TimeKeys SPANISH_KEYS = new TimeKeys().
            setSecondFormat("Segundo(s)").
            setMinuteFormat("Minuto(s)").
            setHourFormat("Hora(s)").
            setDayFormat("Día(s)").
            setWeekFormat("Semana(s)").
            setMonthFormat("Mes(es)").
            setYearFormat("Año(s)");

    private String splitter = ", ";
    private String pluralRegex = "\\s*\\([^)]*\\)\\s*";
    private String startDelimiter = "(";
    private String endDelimiter = ")";

    private String secondFormat = "Second(s)";
    private String minuteFormat = "Minute(s)";
    private String hourFormat = "Hour(s)";
    private String dayFormat = "Day(s)";
    private String weekFormat = "Week(s)";
    private String monthFormat = "Month(s)";
    private String yearFormat = "Year(s)";
}
