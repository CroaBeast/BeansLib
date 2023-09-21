package me.croabeast.beanslib.character;

import org.apache.commons.lang.StringUtils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 *
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
        return "SmallCaps{char=" + character + ", length=" + length + '}';
    }

    public static String stripAccents(String string) {
        if (StringUtils.isBlank(string)) return string;

        Normalizer.Form form = Normalizer.Form.NFKD;
        return Normalizer.normalize(string, form).replaceAll("\\p{M}", "");
    }

    public static char stripAccent(char character) {
        return stripAccents(String.valueOf(character)).toCharArray()[0];
    }

    public static SmallCaps valueOf(char character) {
        char c = stripAccent(character);

        for (SmallCaps caps : values())
            if (caps.equalsIgnoreCase(c)) return caps;

        return null;
    }

    public static boolean isSmallCaps(char character) {
        return valueOf(character) != null;
    }

    public static boolean hasSmallCaps(String string) {
        if (StringUtils.isBlank(string))
            return false;

        for (char c : string.toCharArray())
            if (isSmallCaps(c)) return true;

        return false;
    }

    public static String toSmallCaps(String string) {
        if (StringUtils.isBlank(string))
            return string;

        char[] first = string.toCharArray();
        int length = first.length;

        char[] result = new char[length];

        for (int i = 0; i < length; i++) {
            SmallCaps caps = valueOf(first[i]);

            result[i] = caps != null ?
                    caps.character : first[i];
        }

        return new String(result);
    }

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
