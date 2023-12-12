package me.croabeast.neoprismatic.color;

import me.croabeast.beanslib.misc.Regex;
import me.croabeast.neoprismatic.NeoPrismaticAPI;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MultiColor implements ColorPattern {

    private final Set<ColorPattern> colors = new LinkedHashSet<>();

    @Regex
    static String gradientPattern(@Regex String prefix) {
        @Regex String hex = prefix + "([\\da-f]{6})";
        return "<" + hex + ">(.+?)</" + hex + ">";
    }

    @Regex
    static String rainbowPattern(@Regex String prefix) {
        return "<" + prefix + ":(\\d{1,3})>(.+?)</" + prefix + ">";
    }

    static String[] splitString(String text, int parts) {
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

    static Color getColor(String line) {
        return new Color(Integer.parseInt(line, 16));
    }

    MultiColor() {
        Pattern custom = Pattern.compile("<(#([a-f\\d]{6})(:#([a-f\\d]{6}))+)>(.+?)</g(radient)?>");

        colors.add(new ColorPattern() {
            @Override
            public @NotNull String apply(String string, boolean isLegacy) {
                Matcher m = custom.matcher(string);

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

                    string = string.replace(m.group(), result);
                }

                return string;
            }

            @Override
            public @NotNull String strip(String string) {
                Matcher m = custom.matcher(string);

                while (m.find())
                    string = string.replace(m.group(), m.group(5));

                return string;
            }
        });

        new Gradient("g:");
        new Gradient("#");

        new Rainbow("rainbow");
        new Rainbow("r");
    }

    @Override
    public @NotNull String apply(String string, boolean isLegacy) {
        for (ColorPattern color : colors)
            string = color.apply(string, isLegacy);

        return string;
    }

    @Override
    public @NotNull String strip(String string) {
        for (ColorPattern color : colors)
            string = color.strip(string);

        return string;
    }

    private class Gradient implements ColorPattern {

        private final Pattern pattern;

        private final BiFunction<String, Boolean, String> applier;
        private final UnaryOperator<String> stripOperator;

        Gradient(@Regex String prefix) {
            pattern = Pattern.compile("(?i)" + gradientPattern(prefix));

            applier = (string, isLegacy) -> {
                Matcher matcher = Gradient.this.pattern.matcher(string);

                while (matcher.find()) {
                    String x = matcher.group(1), text = matcher.group(2),
                            z = matcher.group(3),
                            r = "(?i)<" + prefix + "([\\da-f]{6})>";

                    Matcher inside = Pattern.compile(r).matcher(text);
                    String[] array = text.split(r);

                    List<String> ids = new ArrayList<>();

                    ids.add(x);
                    while (inside.find()) ids.add(inside.group(1));

                    ids.add(z);

                    StringBuilder result = new StringBuilder();
                    int i = 0;

                    while (i < ids.size() - 1) {
                        result.append(NeoPrismaticAPI.applyGradient(
                                array[i],
                                getColor(ids.get(i)),
                                getColor(ids.get(i + 1)),
                                isLegacy
                        ));
                        i++;
                    }

                    string = string.replace(matcher.group(), result);
                }

                return string;
            };

            stripOperator = (string) -> {
                Matcher matcher = Gradient.this.pattern.matcher(string);

                while (matcher.find()) {
                    String[] array = matcher.group(2).split("(?i)<" + prefix + "([\\da-f]{6})>");
                    string = string.replace(matcher.group(), String.join("", array));
                }

                return string;
            };

            colors.add(this);
        }

        @Override
        public @NotNull String apply(String string, boolean isLegacy) {
            return applier.apply(string, isLegacy);
        }

        @Override
        public @NotNull String strip(String string) {
            return stripOperator.apply(string);
        }
    }

    private class Rainbow implements ColorPattern {

        private final Pattern pattern;

        private final BiFunction<String, Boolean, String> applier;
        private final UnaryOperator<String> stripOperator;

        Rainbow(@Regex String prefix) {
            pattern = Pattern.compile("(?i)" + rainbowPattern(prefix));

            applier = (string, isLegacy) -> {
                Matcher matcher = Rainbow.this.pattern.matcher(string);

                while (matcher.find()) {
                    String g = matcher.group(), c = matcher.group(2);
                    float f = Float.parseFloat(matcher.group(1));

                    String temp = NeoPrismaticAPI.applyRainbow(c, f, isLegacy);
                    string = string.replace(g, temp);
                }

                return string;
            };

            stripOperator = (string) -> {
                Matcher matcher = Rainbow.this.pattern.matcher(string);

                while (matcher.find())
                    string = string.replace(matcher.group(), matcher.group(2));

                return string;
            };

            colors.add(this);
        }

        @Override
        public @NotNull String apply(String string, boolean isLegacy) {
            return applier.apply(string, isLegacy);
        }

        @Override
        public @NotNull String strip(String string) {
            return stripOperator.apply(string);
        }
    }
}
