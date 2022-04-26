package me.croabeast.iridiumapi.patterns;

import me.croabeast.iridiumapi.IridiumAPI;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolidColor extends BasePattern {

    public String process(String string) {
        Matcher matcher = solidPattern().matcher(string);
        while (matcher.find()) {
            String color = matcher.group(1);
            if (color == null) color = matcher.group(2);
            if (color == null) color = matcher.group(3);
            if (color == null) color = matcher.group(4);
            string = string.replace(matcher.group(), IridiumAPI.getColor(color) + "");
        }
        return string;
    }
}
