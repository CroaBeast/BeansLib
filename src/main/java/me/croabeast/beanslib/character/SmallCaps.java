package me.croabeast.beanslib.character;

import org.apache.commons.lang.StringUtils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * This enum class provides methods for converting strings to and from small caps.
 *
 * <p> Small caps are a typographic style where lowercase letters are replaced by
 * smaller versions of uppercase letters. For example, "Hello" becomes "ʜᴇʟʟᴏ".
 *
 * <p> This class uses the Unicode characters for small caps, which are not
 * supported by all fonts and platforms. It also strips accents from the input
 * characters before converting them to small caps, as there are no Unicode
 * characters for accented small caps.
 */
public enum SmallCaps {
    A('ᴀ'),
    B('ʙ'),
    C('ᴄ'),
    D('ᴅ'),
    E('ᴇ'),
    F('ғ'),
    G('ɢ'),
    H('ʜ'),
    I('ɪ', 3),
    J('ᴊ'),
    K('ᴋ'),
    L('ʟ'),
    M('ᴍ'),
    N('ɴ'),
    O('ᴏ'),
    P('ᴘ'),
    Q('ǫ'),
    R('ʀ'),
    S('s'),
    T('ᴛ'),
    U('ᴜ'),
    V('ᴠ'),
    W('ᴡ'),
    X('x'),
    Y('ʏ'),
    Z('ᴢ');

    final char character;
    final char def;
    int length = 5;

    SmallCaps(char character) {
        def = name().toLowerCase(Locale.ENGLISH).toCharArray()[0];
        this.character = character;
    }

    SmallCaps(char character, int i) {
        this(character);
        length = i;
    }

    private boolean equalsIgnoreCase(char c) {
        return (def + "").matches("(?i)" + Pattern.quote(String.valueOf(c)));
    }

    @Override
    public String toString() {
        return String.valueOf(character);
    }

    /**
     * Strips accents from the given string.
     *
     * @param string The string to strip accents from
     * @return A string with no accents
     */
    public static String stripAccents(String string) {
        if (StringUtils.isBlank(string)) return string;

        Normalizer.Form form = Normalizer.Form.NFKD;
        return Normalizer.normalize(string, form).replaceAll("\\p{M}", "");
    }

    /**
     * Strips accents from the given character.
     *
     * @param character The character to strip accents from
     * @return A character with no accents
     */
    public static char stripAccent(char character) {
        return stripAccents(String.valueOf(character)).toCharArray()[0];
    }

    private static SmallCaps valueOf(char character, boolean strip) {
        char c = strip ? stripAccent(character) : character;

        for (SmallCaps caps : values())
            if (caps.equalsIgnoreCase(c)) return caps;

        return null;
    }

    /**
     * Returns the SmallCaps enum constant corresponding to the given character,
     * or null if none exists.
     *
     * <p> This method strips accents from the input character before looking
     * for a match.
     *
     * @param character The character to look for a SmallCaps enum constant
     * @return The respective SmallCaps enum constant, or null if none exists
     */
    public static SmallCaps valueOf(char character) {
        return valueOf(character, true);
    }

    /**
     * Checks if the given character is a small caps character.
     *
     * @param character The character to check
     * @return true if the given character is small caps; false otherwise
     */
    public static boolean isSmallCaps(char character) {
        return valueOf(character) != null;
    }

    /**
     * Checks if the given string contains any small caps characters.
     *
     * @param string The string to check
     * @return true if the given string contains any small caps
     *              characters; false otherwise
     */
    public static boolean hasSmallCaps(String string) {
        if (StringUtils.isBlank(string))
            return false;

        for (char c : string.toCharArray())
            if (isSmallCaps(c)) return true;

        return false;
    }

    /**
     * Converts the given string to small caps, replacing lowercase letters with
     * their small caps equivalents.
     *
     * <p> This method strips accents from the input characters before converting
     * them to small caps.
     *
     * @param string The string to convert to small caps
     * @return A string in small caps
     */
    public static String toSmallCaps(String string) {
        if (StringUtils.isBlank(string))
            return string;

        char[] first = stripAccents(string).toCharArray();
        int length = first.length;

        char[] result = new char[length];

        for (int i = 0; i < length; i++) {
            SmallCaps caps = valueOf(first[i], false);

            result[i] = caps != null ?
                    caps.character : first[i];
        }

        return new String(result);
    }

    /**
     * Converts the given string from small caps to normal, replacing small caps
     * characters with their lowercase equivalents.
     *
     * @param string The string to convert from small caps
     * @return A string in normal characters
     */
    public static String toNormal(String string) {
        if (StringUtils.isBlank(string))
            return string;

        char[] first = string.toCharArray();
        int length = first.length;

        char[] result = new char[length];

        for (int i = 0; i < length; i++) {
            char c = first[i];

            if (isSmallCaps(c)) {
                SmallCaps sc = valueOf(c);
                if (sc != null) c = sc.def;
            }

            result[i] = c;
        }

        return new String(result);
    }
}
