package me.croabeast.iridiumapi.patterns;

import java.util.regex.Pattern;

public abstract class BasePattern {

    private final String HEX = "[\\da-f]{6}";

    protected Pattern gradientPattern() {
        return Pattern.compile("(?i)<G:(" + HEX + ")>(.+?)</G:(" + HEX + ")>");
    }
    protected Pattern rainbowPattern() {
        return Pattern.compile("(?i)<R:(\\d{1,3})>(.+?)</R>");
    }
    protected Pattern solidPattern() {
        return Pattern.compile("(?i)\\{#(" + HEX + ")}|<#(" + HEX + ")>|&#(" + HEX + ")|#(" + HEX + ")");
    }

    public abstract String process(String string);
}
