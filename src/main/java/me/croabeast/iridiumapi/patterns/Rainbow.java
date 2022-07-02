package me.croabeast.iridiumapi.patterns;

import me.croabeast.iridiumapi.*;

import java.util.regex.Matcher;

/**
 * The rainbow gradient class.
 *
 * @author CroaBeast
 * @since 1.0
 */
public final class Rainbow extends BasePattern {

    @Override
    public String process(String string, boolean useRGB) {
        Matcher matcher = rainbowPattern().matcher(string);
        while (matcher.find()) {
            String saturation = matcher.group(1), content = matcher.group(2);
            string = string.replace(matcher.group(),
                    IridiumAPI.rainbow(content, Float.parseFloat(saturation), useRGB));
        }
        return string;
    }
}