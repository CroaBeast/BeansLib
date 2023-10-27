package me.croabeast.neoprismatic.rgb;

import lombok.var;
import me.croabeast.beanslib.map.MapBuilder;
import org.apache.commons.lang.StringUtils;

import java.awt.*;
import java.util.regex.Pattern;

public abstract class RGBParser {

    protected final RGBMapBuilder parserMap = new RGBMapBuilder(), stripMap = new RGBMapBuilder();

    protected static Color getColor(String line) {
        return new Color(Integer.parseInt(line, 16));
    }

    protected static String[] splitString(String text, int parts) {
        if (parts < 2) return new String[] {text};

        String[] list = new String[parts];
        double length = text.length();

        int start = 0;

        for (int i = 0; i < parts; i++) {
            int chars = (int) Math.ceil((length - start) / (parts - i));
            int end = start + chars;

            list[i] = text.substring(start, end);
            start = end;
        }

        return list;
    }

    public String parse(String string, boolean isLegacy) {
        if (StringUtils.isEmpty(string)) return string;
        if (MapBuilder.isEmpty(parserMap)) return string;

        for (var e : parserMap.entries())
            string = e.getValue().apply(e.getKey(), string, isLegacy);

        return string;
    }

    public String strip(String string) {
        if (StringUtils.isEmpty(string)) return string;
        if (MapBuilder.isEmpty(parserMap)) return string;

        for (var e : stripMap.entries())
            string = e.getValue().apply(e.getKey(), string);

        return string;
    }

    @FunctionalInterface
    protected interface RGBAction {
        String apply(Pattern p, String s, boolean isLegacy);

        default String apply(Pattern p, String s) {
            return apply(p, s, false);
        }
    }

    protected static class RGBMapBuilder extends MapBuilder<Pattern, RGBAction> {

        public RGBMapBuilder put(String s, RGBAction a) {
            super.put(Pattern.compile("(?i)" + s), a);
            return this;
        }

        public void putAll(RGBAction a, String... strings) {
            for (String s : strings) put(s, a);
        }
    }
}
