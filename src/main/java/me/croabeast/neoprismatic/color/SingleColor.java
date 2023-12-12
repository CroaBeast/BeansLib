package me.croabeast.neoprismatic.color;

import me.croabeast.beanslib.misc.Regex;
import me.croabeast.neoprismatic.NeoPrismaticAPI;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SingleColor implements ColorPattern {

    private final Set<ColorPattern> colors = new LinkedHashSet<>();

    SingleColor() {
        new Color("[{]#([a-f\\d]{6})[}]");
        new Color("%#([a-f\\d]{6})%");
        new Color("\\[#([a-f\\d]{6})]");
        new Color("<#([a-f\\d]{6})>");
        new Color("&x([a-f\\d]{6})");
        new Color("&?#([a-f\\d]{6})");
    }

    @Override
    public @NotNull String apply(String string, boolean isLegacy) {
        for (ColorPattern color : colors)
            string = color.apply(string, isLegacy);

        return string;
    }

    @Override
    public @NotNull String strip(String string) {
        for (ColorPattern color : colors) string = color.strip(string);
        return string;
    }

    private class Color implements ColorPattern {

        private final Pattern pattern;

        Color(@Regex String pattern) {
            this.pattern = Pattern.compile("(?i)" + pattern);
            colors.add(this);
        }

        @Override
        public @NotNull String apply(String string, boolean isLegacy) {
            Matcher m = pattern.matcher(string);

            while (m.find()) {
                ChatColor c = NeoPrismaticAPI.fromString(m.group(1), isLegacy);
                string = string.replace(m.group(), c.toString());
            }

            return string;
        }

        @Override
        public @NotNull String strip(String string) {
            Matcher m = pattern.matcher(string);
            while (m.find())
                string = string.replace(m.group(), "");
            return string;
        }
    }
}
