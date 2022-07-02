package me.croabeast.beanslib.utilities.chars;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * The handler class for managing characters and determines its size in a centered chat message.
 */
public final class CharHandler {

    /**
     * The HashMap that stores all the characters.
     */
    private static final HashMap<Character, CharacterInfo> VALUES = new HashMap<>();

    /**
     * The default information if a char doesn't exist in the {@link #getValues()} map.
     */
    private static final CharacterInfo DEFAULT = new CharacterInfo('a', 5);

    /*
     * Stores all the default values in the map.
     */
    static {
        for (DefaultChars d : DefaultChars.values()) {
            char c = d.character();
            VALUES.put(c, new CharacterInfo(c, d.length()));
        }
    }

    /**
     * Gets the values HashMap of all the stored characters.
     * This is private to avoid clearing the entire map.
     * @return the map
     */
    private static HashMap<Character, CharacterInfo> getValues() {
        return VALUES;
    }

    /**
     * Gets the requested {@link CharacterInfo} instance of an input character.
     * <p> Returns the {@link #DEFAULT} value if the character isn't found in the {@link #getValues()} map.
     * @param c an input character
     * @return the requested info
     */
    @NotNull
    public static CharacterInfo getInfo(char c) {
        CharacterInfo info = getValues().getOrDefault(c, null);
        return info == null ? DEFAULT : info;
    }

    /**
     * Converts a string in a single character.
     * <p> Returns null if the string is null, empty or has more than 1 characters.
     * @param input an input string
     * @return the converted character
     */
    @Nullable
    public static Character toChar(@Nullable String input) {
        if (input == null || input.length() <= 0) return null;
        char[] array = input.toCharArray();
        return array.length > 1 ? null : array[0];
    }

    /**
     * Gets the requested {@link CharacterInfo} instance of an input string.
     * <p> Returns the {@link #DEFAULT} value if the string is null, empty or has more than 1 characters.
     * @param input an input string
     * @return the requested info
     */
    @NotNull
    public static CharacterInfo getInfo(String input) {
        Character c = toChar(input);
        return c == null ? DEFAULT : getInfo(c);
    }

    /**
     * Adds a new character in the {@link #getValues()} map.
     * @param c a character
     * @param length the char's length
     */
    public static void addChar(char c, int length) {
        getValues().put(c, new CharacterInfo(c, length));
    }

    /**
     * Removes a character from the {@link #getValues()} map.
     * @param c a character
     */
    public static void removeChar(char c) {
        getValues().remove(c);
    }

    /**
     * Adds a new character from a string in the {@link #getValues()} map.
     * @param input an input string
     * @param length the char's length
     */
    public static void addChar(String input, int length) {
        Character character = toChar(input);
        if (character != null) addChar(character, length);
    }

    /**
     * Removes a character from a string from the {@link #getValues()} map.
     * @param input an input string
     */
    public static void removeChar(String input) {
        Character character = toChar(input);
        if (character != null) removeChar(character);
    }
}
