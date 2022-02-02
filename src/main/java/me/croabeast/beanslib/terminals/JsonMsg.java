package me.croabeast.beanslib.terminals;

import me.croabeast.beanslib.utilities.TextUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.md_5.bungee.api.chat.ClickEvent.Action.*;
import static net.md_5.bungee.api.chat.HoverEvent.Action.*;

@SuppressWarnings("deprecation")
public class JsonMsg {

    private final Player player;
    private String line;

    final static Pattern
            hover = Pattern.compile("(?i)<hover:(.*?)>(.*?)</hover>"),
            click = Pattern.compile("(?i)<click:(execute|suggest|openurl):(.*?)>(.*?)</click>");

    public JsonMsg(Player player, String line) {
        this.player = player;
        this.line = line;
        registerComps();
    }

    public static boolean isValid(String line) {
        return hover.matcher(line).find() || click.matcher(line).find();
    }

    private void registerComps() {
        Matcher hoverMatch = hover.matcher(line);
        Matcher clickMatch = click.matcher(line);

        while (hoverMatch.find()) {
            String[] textArray = hoverMatch.group(1).split(TextUtils.getLineSplitter());
            String text = hoverMatch.group(2);
            BaseComponent[] baseComps = new BaseComponent[textArray.length];

            for (int i = 0; i < textArray.length; i++) {
                baseComps[i] = TextUtils.toComponent(
                        TextUtils.colorize(player, textArray[i]) +
                        (i == textArray.length - 1 ? "" : "\n")
                );
            }

            TextComponent component = TextUtils.toComponent(text);
            component.setHoverEvent(new HoverEvent(SHOW_TEXT, baseComps));
            line = line.replace(hoverMatch.group(), component.getText());
        }

        while (clickMatch.find()) {
            String type = clickMatch.group(1);
            String click = clickMatch.group(2);
            String text = clickMatch.group(3);

            ClickEvent.Action action = null;
            if (type.matches("(?i)execute")) action = RUN_COMMAND;
            else if (type.matches("(?i)suggest")) action = SUGGEST_COMMAND;
            else if (type.matches("(?i)openurl")) action = OPEN_URL;

            if (action != null) {
                TextComponent component = TextUtils.toComponent(text);
                component.setClickEvent(new ClickEvent(action, click));
                line = line.replace(clickMatch.group(), component.getText());
            }
        }
    }

    public TextComponent build() {
        return TextUtils.toComponent(TextUtils.centeredText(player, line));
    }
}
