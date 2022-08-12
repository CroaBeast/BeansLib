package me.croabeast.beanslib.utility.chars;

/**
 * The class to store a character's information.
 */
public class CharacterInfo {

    private final char character;
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
     * Gets the character.
     * @return the char
     */
    public char getCharacter() {
        return character;
    }

    /**
     * Gets the char's length.
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the char's length when is bold.
     * @return the length in bold
     */
    public int getBoldLength() {
        return getLength() + (getCharacter() == ' ' ? 0 : 1);
    }
}
