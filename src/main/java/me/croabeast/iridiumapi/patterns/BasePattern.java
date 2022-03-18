package me.croabeast.iridiumapi.patterns;

public abstract class BasePattern {

    private static String
            gradientPattern = "(?i)<G:([0-9a-f]{6})>(.+?)</G:([0-9a-f]{6})>",
            rainbowPattern = "<R:([0-9]{1,3})>(.+?)</R>",
            solidPattern = "(?i)\\{#([0-9a-f]{6})}|<#([0-9a-f]{6})>|&#([0-9a-f]{6})|#([0-9a-f]{6})";

    public static String getGradientPattern() {
        return gradientPattern;
    }
    public static String getRainbowPattern() {
        return rainbowPattern;
    }
    public static String getSolidPattern() {
        return solidPattern;
    }

    public static void setGradientPattern(String gradientPattern) {
        BasePattern.gradientPattern = gradientPattern;
    }
    public static void setRainbowPattern(String rainbowPattern) {
        BasePattern.rainbowPattern = rainbowPattern;
    }
    public static void setSolidPattern(String solidPattern) {
        BasePattern.solidPattern = solidPattern;
    }

    public abstract String process(String string);
}
