package me.croabeast.beanslib.builder;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.var;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.utility.Exceptions;
import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.iridiumapi.IridiumAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A builder class for creating complex chat messages in Minecraft. Allows for
 * setting hover and click actions on individual parts of the message.
 *
 * <p> Every string that is appended can be colorized if Bukkit color codes,
 * {@link IridiumAPI} color formatting, and placeholders can be replaced if
 * the {@link Player} parser variable is defined.
 */
public class ChatMessageBuilder implements Cloneable {

    private static BeansLib getLib() {
        return BeansLib.getLoadedInstance();
    }

    private final Player target, parser;
    private boolean parseURLs = true;

    private final HashMap<Integer, ChatMessage> messageMap = new HashMap<>();
    private int index = -1;

    /**
     * Constructs a new <code>ChatMessageBuilder</code> object from a string message.
     *
     * @param target the player that will receive the chat builder
     * @param parser the player to parse placeholders and colors
     * @param string the string message to construct the builder from
     */
    public ChatMessageBuilder(Player target, Player parser, String string) {
        this.target = target;
        this.parser = parser;
        updateMessageMapping(string);
    }

    /**
     * Constructs a new <code>ChatMessageBuilder</code> object from a string message.
     *
     * @param player the player to parse placeholders and colors
     * @param string the string message to construct the builder from
     */
    public ChatMessageBuilder(Player player, String string) {
        this(player, player, string);
    }

    /**
     * Constructs a new <code>ChatMessageBuilder</code> object from a string message.
     *
     * @param string the string message to construct the builder from
     */
    public ChatMessageBuilder(String string) {
        this(null, string);
    }

    /**
     * Constructs a new <code>ChatMessageBuilder</code> object without any initial string.
     *
     * <p> The {@link #append(String)} method should be called at least once before building it.
     */
    public ChatMessageBuilder() {
        this(null);
    }

    private void toURL(String s) {
        var matcher = TextUtils.URL_PATTERN.matcher(s);
        int end = 0;

        while (matcher.find()) {
            String t = s.substring(end, matcher.start());
            if (t.length() > 0)
                messageMap.put(++index, new ChatMessage(t));


            if (parseURLs) {
                final String url = matcher.group();

                var message = new ChatMessage(url);
                message.handler.setClick(
                        new ClickAction(ClickType.OPEN_URL, url));

                messageMap.put(++index, message);
            }

            end = matcher.end();
        }

        if (end <= (s.length() - 1))
            messageMap.put(++index, new ChatMessage(s.substring(end)));
    }

    @SuppressWarnings("deprecation")
    private void updateMessageMapping(String string) {
        if (string == null || string.length() < 1) return;

        var line = TextUtils.PARSE_INTERACTIVE_CHAT.apply(parser, string);

        line = TextUtils.CONVERT_OLD_JSON.apply(line);
        line = getLib().centerMessage(target, parser, line);

        var match = TextUtils.FORMATTED_CHAT_PATTERN.matcher(line);
        int last = 0;

        while (match.find()) {
            String temp = line.substring(last, match.start());
            if (temp.length() > 0) toURL(temp);

            String[] args = match.group(1).split("[|]", 2);
            String hover = null, click = null;

            for (String s : args) {
                var m = Pattern.compile("(?i)hover").matcher(s);

                if (m.find()) {
                    hover = s;
                    continue;
                }
                click = s;
            }

            var message = new ChatMessage(match.group(7));

            if (click != null || hover != null)
                message.setHandler(click, hover);

            messageMap.put(++index, message);
            last = match.end();
        }

        if (last <= (line.length() - 1)) toURL(line.substring(last));
    }

    public ChatMessageBuilder setParseURLs(boolean b) {
        parseURLs = b;
        return this;
    }

    public ChatMessageBuilder setHover(List<String> hover) {
        if (index == -1 || hover == null || hover.isEmpty())
            return this;

        var message = messageMap.get(index);
        message.handler.setHover(new HoverAction(hover));

        messageMap.put(index, message);
        return this;
    }

    public ChatMessageBuilder setHover(String... hover) {
        return setHover(
                (hover == null || hover.length == 0) ?
                null : Lists.newArrayList(hover)
        );
    }

    public ChatMessageBuilder setClick(ClickType type, String action) {
        if (index == -1 || type == null || action == null)
            return this;

        var message = messageMap.get(index);
        message.handler.setClick(new ClickAction(type, action));

        messageMap.put(index, message);
        return this;
    }

    public ChatMessageBuilder setClick(String type, String action) {
        return setClick(ClickType.fromString(type), action);
    }

    public ChatMessageBuilder setClick(String input) {
        if (input == null) return this;

        String[] array = input.split(":\"", 2);
        String c = array.length == 1 ? null : array[1];

        return setClick(array[0], c != null ?
                c.substring(0, c.length() - 1) : null);
    }

    public ChatMessageBuilder append(String string) {
        updateMessageMapping(string);
        return this;
    }

    @NotNull
    public BaseComponent[] build() {
        if (index == -1)
            throw new IllegalStateException("The builder does not contain any message.");

        var components = new ArrayList<BaseComponent>();
        for (var message : messageMap.values())
            components.addAll(Lists.newArrayList(message.asComponent()));

        return components.toArray(new BaseComponent[0]);
    }

    public boolean send() {
        try {
            Exceptions.checkPlayer(target).spigot().sendMessage(build());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String toString() {
        if (index == -1) return "";

        StringBuilder builder = new StringBuilder();
        for (var message : messageMap.values()) {
            var handler = message.handler;

            if (handler.isEmpty()) {
                builder.append(message.message);
                continue;
            }

            var click = handler.click;
            var hover = handler.hover;

            boolean clickNotSet = true;

            if (click != null) {
                builder.append(click.type.asBukkit()).
                        append(":\"").
                        append(click.action).
                        append("\"");

                clickNotSet = false;
            }

            if (hover != null) {
                if (clickNotSet) builder.append('|');

                String s = getLib().getLineSeparator();
                builder.append("hover:\"").
                        append(String.join(s, hover.hover)).
                        append("\"");
            }

            builder.append(">").
                    append(message.message).
                    append("</text>");
        }

        return builder.toString();
    }

    static TextComponent onlyComp(String message) {
        return new TextComponent(TextComponent.fromLegacyText(message));
    }

    @Override
    public ChatMessageBuilder clone() {
        try {
            return (ChatMessageBuilder) super.clone();
        } catch (Exception e) {
            return this;
        }
    }

    @SuppressWarnings("all")
    class ClickAction {

        final ClickType type;
        final String action;

        ClickAction(ClickType type, String action) {
            this.type = type;
            this.action = IridiumAPI.stripAll(action);
        }

        ClickEvent createEvent() {
            String s = getLib().formatPlaceholders(parser, action);
            return new ClickEvent(type.asBukkit(), s);
        }

        @Override
        public String toString() {
            return "{" + "type=" + type + ", action='" + action + '\'' + '}';
        }
    }

    @SuppressWarnings("all")
    class HoverAction {

        final String[] hover;

        HoverAction(String[] hover) {
            this.hover = hover;
        }

        HoverAction(List<String> hover) {
            this(hover.toArray(new String[0]));
        }

        boolean isEmpty() {
            return hover == null || hover.length == 0;
        }

        @SuppressWarnings("deprecation")
        HoverEvent createEvent() {
            var array = new BaseComponent[hover.length];

            for (int i = 0; i < hover.length; i++)
                array[i] = onlyComp(
                        getLib().colorize(target, parser, hover[i]) +
                        (i == hover.length - 1 ? "" : "\n")
                );

            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, array);
        }

        @Override
        public String toString() {
            if (hover == null || hover.length == 0) return "{}";

            var array = Arrays.copyOf(hover, hover.length);
            array[array.length - 1] = array[array.length - 1] + "§r";

            return '{' + Arrays.toString(array) + '}';
        }
    }

    @Setter
    class ChatEventsHandler {

        ClickAction click = null;
        HoverAction hover = null;

        ChatEventsHandler() {}

        ChatEventsHandler(String click, String hover) {
            if (click != null) {
                String[] array = click.split(":\"", 2);
                String c = array[1];

                try {
                    this.click = new ClickAction(
                            ClickType.fromString(array[0]),
                            c.substring(0, c.length() - 1)
                    );
                } catch (Exception ignored) {}
            }

            if (hover == null) return;

            String h = hover.split(":\"", 2)[1];
            h = h.substring(0, h.length() - 1);

            this.hover = new HoverAction(getLib().splitLine(h));
        }

        boolean isEmpty() {
            return click == null && hover == null;
        }

        @Override
        public String toString() {
            return "{" + "hover=" + hover + ", click=" + click + '}';
        }
    }

    @RequiredArgsConstructor
    class ChatMessage {

        @Setter @NotNull
        ChatEventsHandler handler = new ChatEventsHandler();
        final String message;

        void setHandler(String click, String hover) {
            handler = new ChatEventsHandler(click, hover);
        }

        BaseComponent[] asComponent() {
            if (handler.isEmpty())
                return TextComponent.fromLegacyText(message);

            var c = onlyComp(message);

            var click = handler.click;
            var hover = handler.hover;

            if (click != null) c.setClickEvent(click.createEvent());

            if (hover != null && !hover.isEmpty())
                c.setHoverEvent(hover.createEvent());

            return new TextComponent[] {c};
        }

        @Override
        public String toString() {
            return "{" + "handler=" + handler + ", message='" + message + "§r'" + '}';
        }
    }
}
