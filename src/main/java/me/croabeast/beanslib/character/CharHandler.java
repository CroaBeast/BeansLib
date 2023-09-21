package me.croabeast.beanslib.character;

import me.croabeast.beanslib.utility.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * The handler class for managing characters and determines its size in a centered chat message.
 */
public final class CharHandler {

    /**
     * The HashMap that stores all the characters.
     */
    private static final HashMap<Character, CharacterInfo> VALUES = new LinkedHashMap<>();

    /**
     * The default information if a char doesn't exist in the {@link #VALUES} map.
     */
    private static final CharacterInfo DEFAULT = new CharacterInfo('a', 5);

    /*
     * Stores all the default values in the map.
     */
    static {
        for (DefaultChars d : DefaultChars.values()) {
            char c = d.character;
            VALUES.put(c, new CharacterInfo(c, d.length));
        }
        for (SmallCaps s : SmallCaps.values()) {
            char c = s.character;
            VALUES.put(c, new CharacterInfo(c, s.length));
        }
    }

    /**
     * Gets the requested {@link CharacterInfo} instance of an input character.
     * <p> Returns the {@link #DEFAULT} value if the character isn't found in the {@link #VALUES} map.
     *
     * @param c an input character
     * @return the requested info
     */
    @NotNull
    public static CharacterInfo getInfo(char c) {
        CharacterInfo info = VALUES.getOrDefault(c, null);
        return info == null ? DEFAULT : info;
    }

    /**
     * Converts a string in a single character.
     * <p> Returns null if the string is null, empty or has more than 1 characters.
     *
     * @param input an input string
     * @return the converted character
     */
    @Nullable
    public static Character toChar(String input) {
        if (StringUtils.isBlank(input)) return null;

        char[] array = input.toCharArray();
        return array.length > 1 ? null : array[0];
    }

    /**
     * Gets the requested {@link CharacterInfo} instance of an input string.
     * <p> Returns the {@link #DEFAULT} value if the string is null, empty or has more than 1 characters.
     *
     * @param input an input string
     * @return the requested info
     */
    @NotNull
    public static CharacterInfo getInfo(String input) {
        Character c = toChar(input);
        return c == null ? DEFAULT : getInfo(c);
    }

    /**
     * Adds a new character in the {@link #VALUES} map.
     *
     * @param c a character
     * @param length the char's length
     */
    public static void addChar(char c, int length) {
        VALUES.put(c, new CharacterInfo(c, length));
    }

    /**
     * Removes a character array from the {@link #VALUES} map.
     *
     * @param array a character array
     */
    public static void removeChar(char... array) {
        if (!ArrayUtils.isArrayEmpty(array)) for (char c : array) VALUES.remove(c);
    }

    /**
     * Adds a new character from a string in the {@link #VALUES} map.
     *
     * @param input an input string
     * @param length the char's length
     */
    public static void addChar(String input, int length) {
        Character character = toChar(input);
        if (character != null) addChar(character, length);
    }

    /**
     * Removes characters in a string array from the {@link #VALUES} map.
     *
     * @param array an string array
     */
    public static void removeChar(String... array) {
        if (ArrayUtils.isArrayEmpty(array)) return;

        for (String input : array) {
            Character character = toChar(input);
            if (character != null) removeChar(character);
        }
    }
}
