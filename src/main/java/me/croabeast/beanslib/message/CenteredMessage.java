package me.croabeast.beanslib.message;

import lombok.Setter;
import lombok.experimental.Accessors;
import me.croabeast.beanslib.Beans;
import me.croabeast.beanslib.character.CharHandler;
import me.croabeast.beanslib.character.CharacterInfo;
import me.croabeast.beanslib.applier.StringApplier;
import me.croabeast.beanslib.utility.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

@Accessors(chain = true)
@Setter
public final class CenteredMessage {

    public static final int CHAT_BOX_LIMIT = 154;
    public static final int MOTD_LIMIT = 140;

    private final Player parser, target;

    private int limit = CHAT_BOX_LIMIT;
    private boolean colored = true;

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

    public String center(String string) {
        if (StringUtils.isBlank(string)) return string;

        String prefix = Beans.getCenterPrefix();
        int i = prefix.length();

        String output = colored ?
                Beans.colorize(target, parser, string) :
                string;
        if (!string.startsWith(prefix)) return output;

        String initial = StringApplier.simplified(string.substring(i))
                .apply(TextUtils.STRIP_JSON)
                .apply(Beans::parseChars)
                .apply(s -> Beans.colorize(target, parser, s))
                .toString();

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
}
