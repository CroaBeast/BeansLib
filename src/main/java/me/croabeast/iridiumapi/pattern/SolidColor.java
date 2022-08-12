package me.croabeast.iridiumapi.pattern;

import me.croabeast.iridiumapi.*;

import java.util.regex.Matcher;

/**
 * The rgb solid color class.
 *
 * @author CroaBeast
 * @since 1.0
 */
public final class SolidColor extends BasePattern {

    @Override
    public String process(String string, boolean useRGB) {
        Matcher matcher = BasePattern.SOLID_PATTERN.matcher(string);
        while (matcher.find()) {
            string = string.replace(matcher.group(),
                    IridiumAPI.getColor(matcher.group(1), useRGB) + "");
        }
        return string;
    }
}
