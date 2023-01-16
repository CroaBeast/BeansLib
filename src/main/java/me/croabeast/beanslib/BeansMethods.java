package me.croabeast.beanslib;

import me.clip.placeholderapi.PlaceholderAPI;
import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.beanslib.utility.chars.CharHandler;
import me.croabeast.beanslib.utility.chars.CharacterInfo;
import me.croabeast.iridiumapi.IridiumAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The methods for the {@link BeansLib} class.
 *
 * @author CroaBeast
 * @since 1.3
 */
public class BeansMethods extends BeansVariables {

    /**
     * A default {@link BeansMethods} instance for static methods.
     */
    public static final BeansMethods DEFAULTS = new BeansMethods();

    /**
     * Use a char pattern to find unicode values and
     * replace them with its respective characters.
     *
     * @param string the input line
     *
     * @return the parsed message with the new characters
     */
    public String parseChars(String string) {
        if (StringUtils.isBlank(charRegex()) ||
                StringUtils.isBlank(string)) return string;

        Pattern charPattern = Pattern.compile(charRegex());
        Matcher m = charPattern.matcher(string);

        while (m.find()) {
            char s = (char) Integer.parseInt(m.group(1), 16);
            string = string.replace(m.group(), s + "");
        }

        return string;
    }

    /**
     * Formats an input string parsing first {@link PlaceholderAPI} placeholders,
     * replaced chars and then applying the respective colors.
     *
     * @param target a target to parse colors depending on its client, can be null
     * @param parser a player, can be null
     * @param string the input message
     *
     * @return the formatted message
     */
    public String colorize(Player target, Player parser, String string) {
        if (target == null) target = parser;
        string = TextUtils.parsePAPI(parser, parseChars(string));
        return IridiumAPI.process(target, string);
    }

    /**
     * Creates a centered chat message.
     *
     * @param target a target to parse colors depending on its client, can be null
     * @param parser a player to parse placeholders.
     * @param string the input message
     *
     * @return the centered chat message.
     */
    public String centerMessage(Player target, Player parser, String string) {
        String prefix = centerPrefix();

        final String output = colorize(target, parser, string);
        if (!string.startsWith(prefix)) return output;

        string = string.substring(prefix.length());

        String initial = parseChars(TextUtils.stripJson(string));
        initial = colorize(target, parser, initial);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : initial.toCharArray()) {
            if (c == '§') {
                previousCode = true;
                continue;
            }

            else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
                continue;
            }

            CharacterInfo dFI = CharHandler.getInfo(c);
            messagePxSize += isBold ?
                    dFI.getBoldLength() : dFI.getLength();
            messagePxSize++;
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = 154 - halvedMessageSize;
        int compensated = 0;

        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += 4; // 4 is the SPACE char length (3) + 1
        }

        return sb + output.substring(prefix.length());
    }

    public String parsePlayerKeys(Player parser, String string, boolean c) {
        return playerKeys().parseKeys(parser, string, c);
    }
}
