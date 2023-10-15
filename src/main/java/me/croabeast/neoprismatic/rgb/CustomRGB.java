package me.croabeast.neoprismatic.rgb;

import me.croabeast.neoprismatic.NeoPrismaticAPI;

import java.util.regex.Matcher;

public class CustomRGB extends RGBParser {

    private static final String PATTERN =
            "<(#([a-f\\d]{6})(:#([a-f\\d]{6})){1,})>(.+?)</g(radient)?>";

    public CustomRGB() {
        parserMap.put(PATTERN, (p, s, isLegacy) -> {
            Matcher m = p.matcher(s);

            while (m.find()) {
                String[] colors = m.group(1).split(":");
                int count = colors.length - 1;

                String[] text = splitString(m.group(5), count);

                StringBuilder result = new StringBuilder();
                int i = 0;

                while (i < count) {
                    String textPart = text[i];

                    if (i > 0) {
                        final String prev = text[i - 1];
                        int l = prev.length() - 1;

                        char last = prev.toCharArray()[l];
                        textPart = last + textPart;
                    }

                    textPart = NeoPrismaticAPI.applyGradient(
                            textPart,
                            getColor(colors[i]),
                            getColor(colors[i + 1]),
                            isLegacy
                    );

                    result.append(i > 0 ?
                            textPart.substring(15) :
                            textPart
                    );
                    i++;
                }

                s = s.replace(m.group(), result);
            }

            return s;
        });

        stripMap.put(PATTERN, (p, s, isLegacy) -> {
            Matcher m = p.matcher(s);
            while (m.find())
                s = s.replace(m.group(), m.group(5));

            return s;
        });
    }
}
