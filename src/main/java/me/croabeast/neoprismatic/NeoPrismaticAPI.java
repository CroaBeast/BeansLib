package me.croabeast.neoprismatic;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import me.croabeast.neoprismatic.rgb.CustomRGB;
import me.croabeast.neoprismatic.rgb.MultipleRGB;
import me.croabeast.neoprismatic.rgb.RGBParser;
import me.croabeast.neoprismatic.rgb.SingleRGB;
import me.croabeast.neoprismatic.util.ClientVersion;
import me.croabeast.beanslib.map.MapBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class NeoPrismaticAPI {

    private final Map<Color, ChatColor> COLOR_MAP = new MapBuilder<Color, ChatColor>()
            .put(new Color(0), ChatColor.getByChar('0'))
            .put(new Color(170), ChatColor.getByChar('1'))
            .put(new Color(43520), ChatColor.getByChar('2'))
            .put(new Color(43690), ChatColor.getByChar('3'))
            .put(new Color(11141120), ChatColor.getByChar('4'))
            .put(new Color(11141290), ChatColor.getByChar('5'))
            .put(new Color(16755200), ChatColor.getByChar('6'))
            .put(new Color(11184810), ChatColor.getByChar('7'))
            .put(new Color(5592405), ChatColor.getByChar('8'))
            .put(new Color(5592575), ChatColor.getByChar('9'))
            .put(new Color(5635925), ChatColor.getByChar('a'))
            .put(new Color(5636095), ChatColor.getByChar('b'))
            .put(new Color(16733525), ChatColor.getByChar('c'))
            .put(new Color(16733695), ChatColor.getByChar('d'))
            .put(new Color(16777045), ChatColor.getByChar('e'))
            .put(new Color(16777215), ChatColor.getByChar('f')).map();

    private final double MC_VERSION = ((Function<String, Double>) s -> {
        Matcher m = Pattern
                .compile("1\\.(\\d+(\\.\\d+)?)")
                .matcher(s);
        if (!m.find()) return 0.0;

        try {
            return Double.parseDouble(m.group(1));
        } catch (Exception e) {
            return 0.0;
        }
    }).apply(Bukkit.getVersion());

    private final List<RGBParser> PARSER_LIST =
            Lists.newArrayList(new CustomRGB(), new MultipleRGB(), new SingleRGB());

    private ChatColor getClosestColor(Color color) {
        Color nearestColor = null;
        double nearestDistance = Integer.MAX_VALUE;

        for (Color c : COLOR_MAP.keySet()) {
            double d = Math.pow(color.getRed() - c.getRed(), 2) +
                    Math.pow(color.getBlue() - c.getBlue(), 2) +
                    Math.pow(color.getGreen() - c.getGreen(), 2);

            if (nearestDistance <= d) continue;

            nearestColor = c;
            nearestDistance = d;
        }

        return COLOR_MAP.get(nearestColor);
    }

    private Object getBukkit(Color color, boolean isLegacy) {
        return isLegacy ? getClosestColor(color) : net.md_5.bungee.api.ChatColor.of(color);
    }

    public Object fromString(String string, boolean isLegacy) {
        return getBukkit(new Color(Integer.parseInt(string, 16)), isLegacy);
    }

    private Object[] createGradient(Color start, Color end, int step, boolean isLegacy) {
        Object[] colors = new Object[step];

        int stepR = Math.abs(start.getRed() - end.getRed()) / (step - 1),
                stepG = Math.abs(start.getGreen() - end.getGreen()) / (step - 1),
                stepB = Math.abs(start.getBlue() - end.getBlue()) / (step - 1);

        int[] direction = new int[] {
                start.getRed() < end.getRed() ? +1 : -1,
                start.getGreen() < end.getGreen() ? +1 : -1,
                start.getBlue() < end.getBlue() ? +1 : -1
        };

        for (int i = 0; i < step; i++) {
            Color color = new Color(start.getRed() + ((stepR * i) * direction[0]),
                    start.getGreen() + ((stepG * i) * direction[1]),
                    start.getBlue() + ((stepB * i) * direction[2]));

            colors[i] = getBukkit(color, isLegacy);
        }

        return colors;
    }

    private Object[] createRainbow(int step, float sat, boolean isLegacy) {
        Object[] colors = new Object[step];
        double colorStep = (1.00 / step);

        for (int i = 0; i < step; i++) {
            Color color = Color.getHSBColor((float) (colorStep * i), sat, sat);
            colors[i] = getBukkit(color, isLegacy);
        }

        return colors;
    }

    private Object[] reverseRainbow(int step, float sat, boolean isLegacy) {
        List<Object> r = Lists.newArrayList(createRainbow(step, sat, isLegacy));
        Collections.reverse(r);
        return r.toArray(new Object[0]);
    }

    public String applyColor(Color color, String string, boolean isLegacy) {
        return getBukkit(color, isLegacy) + string;
    }

    private String apply(String source, Object[] colors) {
        StringBuilder specials = new StringBuilder();
        StringBuilder builder = new StringBuilder();

        if (StringUtils.isBlank(source)) return source;

        String[] characters = source.split("");
        int outIndex = 0;

        for (int i = 0; i < characters.length; i++) {
            if (!characters[i].matches("[&§]") || i + 1 >= characters.length) {
                builder.append(colors[outIndex++]).
                        append(specials).
                        append(characters[i]);
                continue;
            }

            if (characters[i + 1].equals("r")) specials.setLength(0);
            else specials.append(characters[i]).append(characters[i + 1]);
            i++;
        }

        return builder.toString();
    }

    public String applyGradient(String string, Color start, Color end, boolean isLegacy) {
        int i = stripSpecial(string).length();
        return i <= 1 ? string : apply(string, createGradient(start, end, i, isLegacy));
    }

    public String applyRainbow(String string, float saturation, boolean isLegacy) {
        int i = stripSpecial(string).length();
        return i <= 0 ? string : apply(string, createRainbow(i, saturation, isLegacy));
    }

    public String colorize(Player player, String string) {
        for (RGBParser p : PARSER_LIST)
            string = p.parse(string, ClientVersion.isLegacy(player) && MC_VERSION < 16.0);

        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public String colorize(String string) {
        return colorize(null, string);
    }

    public String stripBukkit(String string) {
        if (StringUtils.isBlank(string)) return string;

        Matcher m = Pattern
                .compile("(?i)[&§][a-f\\dx]")
                .matcher(string);

        while (m.find())
            string = string.replace(m.group(), "");

        return string;
    }

    public String stripSpecial(String string) {
        if (StringUtils.isBlank(string)) return string;

        Matcher m = Pattern
                .compile("(?i)[&§][k-orx]")
                .matcher(string);

        while (m.find())
            string = string.replace(m.group(), "");

        return string;
    }

    public String stripRGB(String string) {
        for (RGBParser p : PARSER_LIST) string = p.strip(string);
        return string;
    }

    public String stripAll(String string) {
        return stripRGB(stripSpecial(stripBukkit(string)));
    }

    String singleToRegex() {
        return "[&§][a-fk-or\\d]|" + String.join("|", SingleRGB.PATTERNS);
    }

    public String getLastColor(String string, String key) {
        if (StringUtils.isEmpty(string))
            throw new IndexOutOfBoundsException("String is empty");

        String lastColor = ""; // an empty string if not found

        boolean has = StringUtils.isNotEmpty(key);
        if (has) key = Pattern.quote(key);

        String regex = "(?i)(" + singleToRegex() + ")([&§][k-or])*";
        String[] inputs = !has ?
                new String[] {string} : string.split(key);

        Matcher match = Pattern.compile(regex).matcher(inputs[0]);
        while (match.find()) lastColor = match.group();

        return lastColor;
    }
}
