package me.croabeast.beanslib.object.misc;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * The <code>Rounder</code> object rounds a number using a mutable
 * amount of fixed decimals.
 *
 * @param <T> the number class type
 */
public final class Rounder<T extends Number> {

    private final T number;
    private final Class<? extends Number> clazz;

    private int decimalAmount = 2;

    private Rounder(T t) {
        number = t;
        clazz = number.getClass();
    }

    private Rounder<T> setAmount(int i) {
        this.decimalAmount = i;
        return this;
    }

    private String getNumberIdentifier() {
        if (clazz == Long.class) return "L";
        if (clazz == Double.class) return "D";
        if (clazz == Float.class) return "F";

        return "";
    }

    private String getRoundString() {
        String s = "#." + StringUtils.repeat("#", decimalAmount);
        if (decimalAmount == 0) s = "#";

        return new DecimalFormat(s,
                DecimalFormatSymbols.getInstance(Locale.ENGLISH)).
                format(number) + getNumberIdentifier();
    }

    @SuppressWarnings("unchecked")
    private T result() {
        Number n = NumberUtils.createNumber(getRoundString());

        if (clazz != BigDecimal.class &&
                n.getClass() == BigDecimal.class)
            n = clazz == Double.class ?
                    Double.parseDouble(getRoundString()) :
                    Float.parseFloat(getRoundString());

        return (T) n;
    }

    private T result(int i) {
        return setAmount(i).result();
    }

    public static <T extends Number> T round(T t) {
        return new Rounder<>(t).result();
    }

    public static <T extends Number> T round(int decimalAmount, T t) {
        return new Rounder<>(t).result(decimalAmount);
    }
}
