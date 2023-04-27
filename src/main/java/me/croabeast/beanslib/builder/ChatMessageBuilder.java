package me.croabeast.beanslib.builder;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.var;
import me.croabeast.beanslib.BeansLib;
import me.croabeast.beanslib.utility.Exceptions;
import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.iridiumapi.IridiumAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A builder class for creating complex chat messages in Minecraft. Allows for
 * setting hover and click actions on individual parts of the message.
 *
 * <p> Every string that is appended can be colorized if Bukkit color codes,
 * {@link IridiumAPI} color formatting, and placeholders can be replaced if
 * the {@link Player} parser variable is defined.
 */
public class ChatMessageBuilder {

    private static BeansLib getLib() {
        return BeansLib.getLoadedInstance();
    }

    private Player target, parser;
    private boolean parseURLs = true;

    private final HashMap<Integer, ChatMessage> messageMap = new HashMap<>();
    private int index = -1;

    /**
     * Constructs a new <code>ChatMessageBuilder</code> object from a string message.
     *
     * @param string the string message to construct the builder from
     */
    public ChatMessageBuilder(String string) {
        updateMessageMapping(string, true);
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
        var m = TextUtils.URL_PATTERN.matcher(s);
        int end = 0;

        while (m.find()) {
            String t = s.substring(end, m.start());
            if (t.length() > 0) {
                messageMap.put(index, new ChatMessage(t));
                index++;
            }

            if (!parseURLs) continue;

            final String url = m.group();
            var message = new ChatMessage(url);

            message.handler.click =
                    new ClickAction(ClickType.OPEN_URL, url);

            messageMap.put(index, message);
            index++;
            end = m.end();
        }

        if (end >= (s.length() - 1)) return;

        messageMap.put(index, new ChatMessage(s.substring(end)));
        index++;
    }

    private void updateMessageMapping(String string, boolean initialize) {
        if (StringUtils.isBlank(string)) return;
        if (initialize) index++; // builder initialization

        var line = TextUtils.PARSE_INTERACTIVE_CHAT.apply(parser, string);

        line = getLib().createCenteredChatMessage(target, parser, line);
        line = TextUtils.CONVERT_OLD_JSON.apply(line);

        var match = TextUtils.FORMATTED_CHAT_PATTERN.matcher(line);
        int last = 0;

        while (match.find()) {
            String temp = line.substring(last, match.start());
            if (temp.length() > 0) toURL(temp);

            String[] args = match.group(1).split("[|]", 2);
            String hover = null, click = null;

            if (args.length == 1) {
                if (args[0].matches("(?i)^hover:\"")) hover = args[0];
                else click = args[0];
            }
            else if (args.length == 2) {
                boolean hoverNotSet = true;

                if (args[0].matches("(?i)^hover:\"")) {
                    hoverNotSet = false;
                    hover = args[0];
                }
                else click = args[0];

                if (args[1].matches("(?i)^hover:\"")
                        && hoverNotSet) hover = args[1];
                else click = args[1];
            }

            messageMap.put(index, new ChatMessage(
                    new ChatEventsHandler(hover, click),
                    match.group(7))
            );
            index++;
            last = match.end();
        }

        if (last < (line.length() - 1)) toURL(line.substring(last));
    }

    public ChatMessageBuilder setPlayers(Player target, Player parser) {
        this.parser = parser;
        this.target = target == null ? parser : target;

        return this;
    }

    public ChatMessageBuilder setPlayer(Player player) {
        return setPlayers(player, player);
    }

    public ChatMessageBuilder setParseURLs(boolean b) {
        parseURLs = b;
        return this;
    }

    public ChatMessageBuilder setHover(List<String> hover) {
        if (index == -1 || hover == null) return this;

        var message = messageMap.get(index);
        message.handler.hover = new HoverAction(hover.toArray(new String[0]));

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
        if (index == -1 || type == null || action == null) return this;

        var message = messageMap.get(index);
        message.handler.click = new ClickAction(type, action);

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
        updateMessageMapping(string, false);
        return this;
    }

    @NotNull
    public BaseComponent[] build() {
        if (index == -1)
            throw new IllegalStateException("The builder does not contain any message.");

        var components = new ArrayList<BaseComponent>();
        for (var message : messageMap.values())
            components.addAll(Lists.newArrayList(message.asComponent(target, parser)));

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

    public String toJsonPattern() {
        if (index == -1) return "";

        StringBuilder builder = new StringBuilder();
        for (var message : messageMap.values()) {
            var handler = message.handler;

            if (handler.isEmpty()) {
                builder.append(message.message);
                continue;
            }

            builder.append("<");

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

    @RequiredArgsConstructor(access = AccessLevel.MODULE)
    static class ClickAction {

        @NotNull
        private final ClickType type;
        @NotNull
        private final String action;

        ClickEvent createEvent(Player player) {
            String s = getLib().formatPlaceholders(player, action);
            return new ClickEvent(type.asBukkit(), s);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.MODULE)
    static class HoverAction {

        private final String[] hover;

        boolean isEmpty() {
            return hover == null || hover.length == 0;
        }

        @SuppressWarnings("deprecation")
        HoverEvent createEvent(Player target, Player parser) {
            var array = new BaseComponent[hover.length];

            for (int i = 0; i < hover.length; i++)
                array[i] = onlyComp(
                        getLib().colorize(target, parser, hover[i]) +
                        (i == hover.length - 1 ? "" : "\n")
                );

            var showText = HoverEvent.Action.SHOW_TEXT;
            return new HoverEvent(showText, array);
        }
    }

    static class ChatEventsHandler {

        static final ChatEventsHandler EMPTY = new ChatEventsHandler(null, null);

        private HoverAction hover = null;
        private ClickAction click = null;

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
            return this == EMPTY || (click == null && hover == null);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.MODULE)
    static class ChatMessage {

        @NotNull
        private final ChatEventsHandler handler;
        private final String message;

        ChatMessage(String message) {
            this(ChatEventsHandler.EMPTY, message);
        }

        BaseComponent[] asComponent(Player target, Player parser) {
            if (handler.isEmpty())
                return TextComponent.fromLegacyText(message);

            var c = onlyComp(message);

            var click = handler.click;
            var hover = handler.hover;

            if (click != null) c.setClickEvent(click.createEvent(parser));
            if (hover != null && !hover.isEmpty())
                c.setHoverEvent(hover.createEvent(target, parser));

            return new BaseComponent[] {c};
        }
    }
}
