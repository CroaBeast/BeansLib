package me.croabeast.beanslib.message;

import me.croabeast.beanslib.Beans;
import me.croabeast.beanslib.character.CharHandler;
import me.croabeast.beanslib.character.CharacterInfo;
import me.croabeast.beanslib.utility.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

public final class CenteredMessage {

    public static final int DEFAULT_CHAT_BOX_LIMIT = 154;
    public static final int MOTD_LIMIT = 140;

    private final Player parser, target;
    private int limit = DEFAULT_CHAT_BOX_LIMIT;

    public CenteredMessage(Player parser, Player target) {
        this.parser = parser;
        this.target = target;
    }

    public CenteredMessage(Player player) {
        this(player, player);
    }

    public CenteredMessage() {
        this(null, null);
    }

    public CenteredMessage setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public String center(String string) {
        if (StringUtils.isBlank(string)) return string;

        final String prefix = Beans.getCenterPrefix(),
                output = Beans.colorize(target, parser, string);

        if (!string.startsWith(prefix)) return output;

        string = string.substring(prefix.length());
        String initial = TextUtils.STRIP_JSON.apply(string);

        initial = Beans.parseChars(initial);
        initial = Beans.colorize(target, parser, initial);

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

            CharacterInfo info = CharHandler.getInfo(c);
            messagePxSize += isBold ?
                    info.getBoldLength() : info.getLength();
            messagePxSize++;
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = limit - halvedMessageSize;
        int compensated = 0;

        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += 4; // 4 is the SPACE char length (3) + 1
        }

        return sb + output.substring(prefix.length());
    }

    public static String toChat(Player parser, Player target, String string) {
        return new CenteredMessage(parser, target).center(string);
    }

    public static String toMOTD(Player player, String string) {
        return new CenteredMessage(player).setLimit(MOTD_LIMIT).center(string);
    }
}
