package me.croabeast.iridiumapi.patterns;

import me.croabeast.iridiumapi.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The simple gradient class.
 *
 * @author CroaBeast
 * @since 1.0
 */
public final class Gradient extends BasePattern {

    private Color getColor(String line) {
        return new Color(Integer.parseInt(line, 16));
    }

    @Override
    public String process(String string, boolean useRGB) {
        string = convertFromLegacyGradient(string);
        Matcher match = gradientPattern().matcher(string);

        while (match.find()) {
            String x = match.group(1), text = match.group(2), z = match.group(3);

            Matcher insideMatch = Pattern.compile("(?i)" + gradient()).matcher(text);
            String[] array = text.split("(?i)" + gradient());

            List<String> ids = new ArrayList<>();
            while (insideMatch.find()) ids.add(insideMatch.group(1));

            StringBuilder result = new StringBuilder();

            for (int i = 0; i <= ids.size(); i++) {
                boolean canPass = i < ids.size();
                Color end = getColor(canPass ? ids.get(i) : z);

                result.append(IridiumAPI.color(
                        array[i], getColor(x), end, useRGB));
                if (canPass) x = ids.get(i);
            }

            string = string.replace(match.group(), result + "");
        }

        return string;
    }
}
