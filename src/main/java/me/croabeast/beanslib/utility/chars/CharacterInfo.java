package me.croabeast.beanslib.utility.chars;

import lombok.Getter;

/**
 * The class to store a character's information.
 */
@Getter
public class CharacterInfo {

    /**
     * The character.
     * @return the char
     */
    private final char character;
    /**
     * The char's length.
     * @return the length
     */
    private final int length;

    /**
     * Creates a character info class.
     * <p> It's not possible to create an instance of this class.
     * <p> Use {@link CharHandler#addChar(char, int)} instead.
     * @param character the character
     * @param length the char's length
     */
    CharacterInfo(char character, int length) {
        this.character = character;
        this.length = length;
    }

    /**
     * The char's length when is bold.
     * @return the length in bold
     */
    public int getBoldLength() {
        return getLength() + (getCharacter() == ' ' ? 0 : 1);
    }
}
