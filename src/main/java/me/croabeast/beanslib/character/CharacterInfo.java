package me.croabeast.beanslib.character;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The class to store a character's information.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
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
     * The char's length when is bold.
     * @return the length in bold
     */
    public int getBoldLength() {
        return getLength() + (getCharacter() == ' ' ? 0 : 1);
    }
}
