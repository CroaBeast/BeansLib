package me.croabeast.beanslib.builder;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.var;
import me.croabeast.beanslib.Beans;
import me.croabeast.beanslib.misc.StringApplier;
import me.croabeast.beanslib.utility.ArrayUtils;
import me.croabeast.beanslib.utility.Exceptions;
import me.croabeast.beanslib.utility.TextUtils;
import me.croabeast.neoprismatic.NeoPrismaticAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A builder class for creating complex chat messages in Minecraft. Allows for
 * setting hover and click actions on individual parts of the message.
 *
 * <p> Every string that is appended is being colorized using {@link NeoPrismaticAPI}
 * color formatting, and placeholders are being replaced if a {@link Player} parser
 * variable is defined.
 */
public class ChatMessageBuilder implements Cloneable {

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

    private String putColorToURL() {
        return ++index > 0 ? messageMap.get(index - 1).getLastColor() + "" : "";
    }

    private void toURL(String s) {
        var urlMatcher = TextUtils.URL_PATTERN.matcher(s);
        int end = 0;

        while (urlMatcher.find()) {
            String t = s.substring(end, urlMatcher.start());
            if (t.length() > 0) {
                var m = new ChatMessage(putColorToURL() + t);
                messageMap.put(index, m);
            }

            if (parseURLs) {
                final String url = urlMatcher.group();

                var c = new ClickEvent(ClickAction.OPEN_URL, url);
                var m = new ChatMessage(putColorToURL() + url);

                m.handler.setClick(c);
                messageMap.put(index, m);
            }

            end = urlMatcher.end();
        }

        if (end > (s.length() - 1)) return;

        var st = putColorToURL() + s.substring(end);
        messageMap.put(index, new ChatMessage(st));
    }

    private void updateMessageMapping(String string) {
        if (string == null) return;

        if (string.length() < 1) {
            messageMap.put(++index, new ChatMessage(string));
            return;
        }

        String line = StringApplier.of(string).
                apply(s -> TextUtils.PARSE_INTERACTIVE_CHAT.apply(parser, s)).
                apply(TextUtils.CONVERT_OLD_JSON).
                apply(Beans::convertToSmallCaps).
                apply(s -> Beans.createCenteredChatMessage(target, parser, s)).
                toString();

        var match = TextUtils.FORMAT_CHAT_PATTERN.matcher(line);
        int last = 0;

        while (match.find()) {
            String temp = line.substring(last, match.start());
            if (temp.length() > 0) toURL(temp);

            String[] args = match.group(1).split("[|]", 2);
            String h = null, c = null;

            for (String s : args) {
                var m = Pattern.compile("(?i)hover").matcher(s);
                if (m.find()) h = s; else c = s;
            }

            var message = new ChatMessage(match.group(7));
            if (c != null || h != null) message.setHandler(c, h);

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
        message.handler.setHover(new HoverEvent(hover));

        messageMap.put(index, message);
        return this;
    }

    public ChatMessageBuilder setHover(String... hover) {
        return setHover(
                ArrayUtils.isArrayEmpty(hover) ?
                        null :
                        Lists.newArrayList(hover)
        );
    }

    public ChatMessageBuilder setHoverToAll(List<String> hover) {
        if (index == -1 || hover == null || hover.isEmpty())
            return this;

        for (var m : messageMap.values())
            m.handler.setHover(new HoverEvent(hover));

        return this;
    }

    public ChatMessageBuilder setClick(ClickAction type, String action) {
        if (index == -1 || type == null || action == null)
            return this;

        var message = messageMap.get(index);
        message.handler.setClick(new ClickEvent(type, action));

        messageMap.put(index, message);
        return this;
    }

    public ChatMessageBuilder setClick(String type, String action) {
        return setClick(ClickAction.fromString(type), action);
    }

    public ChatMessageBuilder setClick(String input) {
        if (input == null) return this;

        String[] array = input.split(":\"", 2);
        String c = array.length == 1 ? null : array[1];

        return setClick(array[0], c != null ?
                c.substring(0, c.length() - 1) : null);
    }

    public ChatMessageBuilder setClickToAll(ClickAction type, String action) {
        if (index == -1 || type == null || action == null)
            return this;

        for (var m : messageMap.values()) {
            if (parseURLs && m.handler.click.type == ClickAction.OPEN_URL)
                continue;

            m.handler.setClick(new ClickEvent(type, action));
        }

        return this;
    }

    public ChatMessageBuilder setClickToAll(String input) {
        if (StringUtils.isEmpty(input)) return this;

        String[] array = input.split(":\"", 2);
        String c = array.length == 1 ? null : array[1];

        ClickAction click = ClickAction.fromString(array[0]);

        String action = StringUtils.isNotBlank(c) ?
                c.substring(0, c.length() - 1) : null;

        return setClickToAll(click, action);
    }

    public ChatMessageBuilder append(String string) {
        updateMessageMapping(string);
        return this;
    }

    public <T> ChatMessageBuilder append(T object) {
        String initial = object.toString();

        try {
            Class<?> clazz = object.getClass();
            Method method = String.class.getMethod("valueOf", clazz);

            initial = (String) method.invoke(null, object);
        } catch (Exception ignored) {}

        return append(initial);
    }

    @NotNull
    public BaseComponent[] build() {
        if (index < 0) {
            var m = "The builder does not contain any message";
            throw new IllegalStateException(m);
        }

        List<BaseComponent> comps = new ArrayList<>();

        for (ChatMessage message : messageMap.values())
            comps.addAll(message.asComponents());

        return comps.toArray(new BaseComponent[0]);
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

    public String toPatternString() {
        if (index == -1) return "";

        StringBuilder builder = new StringBuilder();

        for (var message : messageMap.values()) {
            var handler = message.handler;

            if (handler.isEmpty()) {
                builder.append(message.message);
                continue;
            }

            builder.append('<');

            var click = handler.click;
            var hover = handler.hover;

            boolean clickSet = false;

            if (click != null) {
                builder.append(click.type.asBukkit()).
                        append(":\"").
                        append(click.input).
                        append('"');

                clickSet = true;
            }

            if (hover != null) {
                if (clickSet) builder.append('|');

                String s = Beans.getLineSeparator();
                builder.append("hover:\"").
                        append(String.join(s, hover.hover)).
                        append('"');
            }

            builder.append('>').
                    append(message.message).
                    append("</text>");
        }

        return builder.toString();
    }

    @Override
    public String toString() {
        try {
            return TextComponent.toPlainText(build());
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public ChatMessageBuilder clone() {
        try {
            return (ChatMessageBuilder) super.clone();
        } catch (Exception e) {
            return this;
        }
    }

    static TextComponent onlyComp(String message) {
        return new TextComponent(TextComponent.fromLegacyText(message));
    }

    class ClickEvent {

        final me.croabeast.beanslib.builder.ClickAction type;
        final String input;

        ClickEvent(me.croabeast.beanslib.builder.ClickAction type, String input) {
            this.type = type;
            this.input = NeoPrismaticAPI.stripAll(input);
        }

        net.md_5.bungee.api.chat.ClickEvent createEvent() {
            String s = Beans.formatPlaceholders(parser, input);
            return new net.md_5.bungee.api.chat.ClickEvent(type.asBukkit(), s);
        }

        @Override
        public String toString() {
            return "{type=" + type + ", input='" + input + "'}";
        }
    }

    class HoverEvent {

        final String[] hover;

        HoverEvent(String[] hover) {
            this.hover = hover;
        }

        HoverEvent(List<String> hover) {
            this(hover.toArray(new String[0]));
        }

        boolean isEmpty() {
            return ArrayUtils.isArrayEmpty(hover);
        }

        @SuppressWarnings("deprecation")
        net.md_5.bungee.api.chat.HoverEvent createEvent() {
            var array = new BaseComponent[hover.length];

            for (int i = 0; i < hover.length; i++)
                array[i] = onlyComp(
                        Beans.colorize(target, parser, hover[i]) +
                        (i == hover.length - 1 ? "" : "\n")
                );

            return new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, array);
        }

        @Override
        public String toString() {
            if (isEmpty()) return "{}";

            String[] array = Arrays.copyOf(hover, hover.length);
            array[array.length - 1] =
                    array[array.length - 1] + "§r";

            return '{' + Arrays.toString(array) + '}';
        }
    }

    @Setter
    class ChatEventsHandler {

        ClickEvent click = null;
        HoverEvent hover = null;

        ChatEventsHandler() {}

        ChatEventsHandler(String click, String hover) {
            if (click != null) {
                String[] array = click.split(":\"", 2);
                String c = array[1];

                try {
                    this.click = new ClickEvent(
                            me.croabeast.beanslib.builder.ClickAction.fromString(array[0]),
                            c.substring(0, c.length() - 1)
                    );
                } catch (Exception ignored) {}
            }

            if (hover == null) return;

            String h = hover.split(":\"", 2)[1];
            h = h.substring(0, h.length() - 1);

            this.hover = new HoverEvent(Beans.splitLine(h));
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

        ChatColor color = null;

        void setHandler(String click, String hover) {
            handler = new ChatEventsHandler(click, hover);
        }

        BaseComponent[] compile() {
            var urlMatch = TextUtils.URL_PATTERN.matcher(message);
            var comp = onlyComp(message);

            var c = handler.click;
            var h = handler.hover;

            if (parseURLs && urlMatch.find()) {
                var cl = new ClickEvent(me.croabeast.beanslib.builder.ClickAction.OPEN_URL, message);
                handler.setClick(cl);
            }

            if (c != null) comp.setClickEvent(c.createEvent());

            if (h != null && !h.isEmpty())
                comp.setHoverEvent(h.createEvent());

            var comps = new BaseComponent[] {comp};

            color = comps[comps.length - 1].getColor();
            return comps;
        }

        List<BaseComponent> asComponents() {
            return Lists.newArrayList(compile());
        }

        ChatColor getLastColor() {
            if (color == null) compile();
            return color;
        }

        @Override
        public String toString() {
            return "{handler=" + handler + ", message='" + message + "§r'}";
        }
    }
}
