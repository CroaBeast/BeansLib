package me.croabeast.neoprismatic.color;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public interface ColorPattern {

    List<ColorPattern> COLOR_PATTERNS = Arrays.asList(new MultiColor(), new SingleColor());

    @NotNull String apply(String string, boolean isLegacy);

    @NotNull String strip(String string);
}
