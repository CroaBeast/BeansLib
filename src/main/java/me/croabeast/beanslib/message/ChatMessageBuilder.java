package me.croabeast.beanslib.message;

import me.croabeast.beanslib.Beans;
import me.croabeast.beanslib.applier.StringApplier;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A builder class for creating chat messages with interactive features such as hover
 * and click events.
 *
 * <p> The builder can parse URLs and format patterns from a string and convert them into
 * components, also append any object to the message and send it to a player.
 */
public class ChatMessageBuilder {

    private final Player target, parser;
    private boolean parseURLs = true;

    private final Map<Integer, ChatMessage> map = new LinkedHashMap<>();
    private int index = -1;

    private void toURL(String s) {
        Matcher urlMatcher = TextUtils.URL_PATTERN.matcher(s);
        int end = 0;

        while (urlMatcher.find()) {
            String t = s.substring(end, urlMatcher.start());

            if (t.length() > 0)
                map.put(++index, new ChatMessage(t).applyColor());

            if (parseURLs) {
                final String url = urlMatcher.group();

                ClickAction a = ClickAction.OPEN_URL;
                ClickEvent c = new ClickEvent(a, url);

                map.put(++index, new ChatMessage(url)
                        .setClick(c)
                        .applyColor());
            }

            end = urlMatcher.end();
        }

        if (end > (s.length() - 1)) return;

        String temp = s.substring(end);
        map.put(++index, new ChatMessage(temp).applyColor());
    }

    private void updateMessageMapping(String string) {
        if (string == null) return;

        if (string.length() < 1) {
            map.put(++index, new ChatMessage(string));
            return;
        }

        String line = StringApplier.simplified(string).
                apply(s -> TextUtils.PARSE_INTERACTIVE_CHAT.apply(parser, s)).
                apply(TextUtils.CONVERT_OLD_JSON).
                apply(Beans::convertToSmallCaps).
                apply(s -> Beans.createCenteredChatMessage(target, parser, s)).
                toString();

        Matcher match = TextUtils.FORMAT_CHAT_PATTERN.matcher(line);
        int last = 0;

        while (match.find()) {
            String temp = line.substring(last, match.start());
            if (temp.length() > 0) toURL(temp);

            String[] args = match.group(1).split("[|]", 2);
            String h = null, c = null;

            for (String s : args) {
                Matcher m = Pattern.compile("(?i)hover").matcher(s);
                if (m.find()) h = s; else c = s;
            }

            ChatMessage message = new ChatMessage(match.group(7));
            if (c != null || h != null) message.setHandler(c, h);

            map.put(++index, message);
            last = match.end();
        }

        if (last <= (line.length() - 1)) toURL(line.substring(last));
    }

    /**
     * Creates a new builder with a target player, a parser player, and an initial string.
     *
     * <p> The target player is the one who will receive the message, and the parser player is
     * the one who will provide the placeholders and centering.
     *
     * <p> The initial string is the first part of the message to be parsed and formatted.
     *
     * @param target the target player
     * @param parser the parser player
     * @param string the initial string
     */
    public ChatMessageBuilder(Player target, Player parser, String string) {
        this.target = target;
        this.parser = parser;
        updateMessageMapping(string);
    }

    /**
     * Creates a new builder with a player and an initial string.
     *
     * <p> The player will be both the target and the parser of the message.
     *
     * @param player the player
     * @param string the initial string
     */
    public ChatMessageBuilder(Player player, String string) {
        this(player, player, string);
    }

    /**
     * Creates a new builder with an initial string. The target and the parser will be null.
     * @param string the initial string
     */
    public ChatMessageBuilder(String string) {
        this(null, string);
    }

    /**
     * Creates a new builder with no initial string. The target and the parser will be null.
     *
     * <p> The {@link #append(String)} method should be called at least once before building it.
     */
    public ChatMessageBuilder() {
        this((String) null);
    }

    /**
     * Creates a new builder by copying another builder.
     *
     * <p> The target, parser, parseURLs, map, and index will be copied from the other
     * builder.
     *
     * @param builder the other builder
     * @throws NullPointerException if the other builder is null
     */
    public ChatMessageBuilder(ChatMessageBuilder builder) {
        Objects.requireNonNull(builder);

        target = builder.target;
        parser = builder.parser;

        parseURLs = builder.parseURLs;

        if (builder.map.isEmpty())
            return;

        map.putAll(builder.map);
        index = builder.index;
    }

    /**
     * Sets whether the builder should parse URLs from the string and create click
     * events for them.
     *
     * @param b true to parse URLs, false otherwise
     * @return the builder itself
     */
    public ChatMessageBuilder setParseURLs(boolean b) {
        parseURLs = b;
        return this;
    }

    /**
     * Sets the hover event for the last part of the message with a list of strings.
     *
     * <p> The list of strings will be shown as text when the mouse hovers over the
     * message part.
     *
     * @param hover the list of strings for the hover event
     * @return the builder itself
     */
    public ChatMessageBuilder setHover(List<String> hover) {
        if (index < 0) return this;

        try {
            Objects.requireNonNull(hover);
        } catch (Exception e) {
            return this;
        }

        if (hover.isEmpty()) return this;

        ChatMessage message = map.get(index);
        message.setHover(new HoverEvent(hover));

        map.put(index, message);
        return this;
    }

    /**
     * Sets the hover event for the last part of the message with an array of strings.
     *
     * <p> The array of strings will be shown as text when the mouse hovers over the
     * message part.
     *
     * @param hover the array of strings for the hover event
     * @return the builder itself
     */
    public ChatMessageBuilder setHover(String... hover) {
        return setHover(
                ArrayUtils.isArrayEmpty(hover) ?
                        null :
                        ArrayUtils.toList(hover)
        );
    }

    /**
     * Sets the hover event for all parts of the message with a list of strings.
     *
     * <p> The list of strings will be shown as text when the mouse hovers over any part
     * of the message.
     *
     * @param hover the list of strings for the hover event
     * @return the builder itself
     */
    public ChatMessageBuilder setHoverToAll(List<String> hover) {
        if (index < 0) return this;

        try {
            Objects.requireNonNull(hover);
        } catch (Exception e) {
            return this;
        }

        if (hover.isEmpty()) return this;

        for (ChatMessage m : map.values())
            m.setHover(new HoverEvent(hover));

        return this;
    }

    /**
     * Sets the click event for the last part of the message with a click action and
     * an action string.
     *
     * <p> The click action determines what will happen when the message part is clicked,
     * such as opening a URL, running a command, suggesting a command, etc.
     *
     * <p> The action string is the argument for the click action, such as the URL, the
     * command, the suggestion, the page number, or the text to copy.
     *
     * @param type the click action
     * @param action the action string
     *
     * @return the builder itself
     */
    public ChatMessageBuilder setClick(ClickAction type, String action) {
        if (index < 0) return this;

        try {
            Objects.requireNonNull(type);
            Objects.requireNonNull(action);
        } catch (Exception e) {
            return this;
        }

        ChatMessage message = map.get(index);
        message.setClick(new ClickEvent(type, action));

        map.put(index, message);
        return this;
    }

    /**
     * Sets the click event for the last part of the message with a string representation
     * of the click action and an action string.
     *
     * <p> The string representation of the click action can be one of the following:
     * <pre>{@code open_url, run_command, suggest_command, change_page, or copy_to_clipboard}</pre>
     *
     * <p> The action string is the argument for the click action, such as the URL, the
     * command, the suggestion, the page number, or the text to copy.
     *
     * @param type the string representation of the click action
     * @param action the action string
     *
     * @return the builder itself
     */
    public ChatMessageBuilder setClick(String type, String action) {
        return setClick(ClickAction.fromString(type), action);
    }

    /**
     * Sets the click event for the last part of the message with a single string input.
     *
     * <p> The input should be in the format of "click_action:\"action_string\"", such as
     * "open_url:\"https://www.bing.com\"", "run_command:\"/help\"", etc.
     *
     * @param input the single string input for the click event
     * @return the builder itself
     */
    public ChatMessageBuilder setClick(String input) {
        if (input == null) return this;

        String[] array = input.split(":\"", 2);
        String c = array.length == 1 ? null : array[1];

        return setClick(array[0], c != null ?
                c.substring(0, c.length() - 1) : null);
    }

    /**
     * Sets the click event for all parts of the message with a click action and an action
     * string.
     *
     * <p> The click action determines what will happen when any part of the message is clicked,
     * such as opening a URL, running a command, or suggesting a command.
     *
     * <p> The action string is the argument for the click action, such as the URL, the
     * command, the suggestion, the page number, or the text to copy.
     *
     * @param type the click action
     * @param action the action string
     * @return the builder itself
     */
    public ChatMessageBuilder setClickToAll(ClickAction type, String action) {
        if (index < 0) return this;

        try {
            Objects.requireNonNull(type);
            Objects.requireNonNull(action);
        } catch (Exception e) {
            return this;
        }

        for (ChatMessage m : map.values()) {
            if (parseURLs && m.handler.click.type == ClickAction.OPEN_URL)
                continue;

            m.setClick(new ClickEvent(type, action));
        }

        return this;
    }

    /**
     * Sets the click event for all parts of the message with a single string input.
     *
     * <p> The input should be in the format of "click_action:\"action_string\"", such as
     * "open_url:\"https://www.bing.com\"", "run_command:\"/help\"", etc.
     *
     * @param input the single string input for the click event
     * @return the builder itself
     */
    public ChatMessageBuilder setClickToAll(String input) {
        if (StringUtils.isEmpty(input)) return this;

        String[] array = input.split(":\"", 2);
        String c = array.length == 1 ? null : array[1];

        ClickAction click = ClickAction.fromString(array[0]);

        String action = StringUtils.isNotBlank(c) ?
                c.substring(0, c.length() - 1) : null;

        return setClickToAll(click, action);
    }

    /**
     * Appends a string to the message and parses and formats it.
     *
     * <p> The string can contain URLs and format patterns that will be converted into
     * components.
     *
     * @param string the string to append
     * @return the builder itself
     */
    public ChatMessageBuilder append(String string) {
        updateMessageMapping(string);
        return this;
    }

    /**
     * Appends an object to the message and parses and formats it.
     *
     * <p> The object will be converted to a string using its toString method or using
     * {@link String#valueOf(Object)}.
     *
     * <p> The string can contain URLs and format patterns that will be converted into components.
     *
     * @param object the object to append
     * @param <T> the type of the object
     *
     * @return the builder itself
     */
    public <T> ChatMessageBuilder append(T object) {
        String initial = object.toString();

        try {
            Class<?> clazz = object.getClass();
            Method method = String.class.getMethod("valueOf", clazz);

            initial = (String) method.invoke(null, object);
        } catch (Exception ignored) {}

        return append(initial);
    }

    /**
     * Builds the message as an array of base components.
     *
     * <p> The components will have the interactive features such as hover and click events
     * applied to them.
     *
     * @return the array of base components
     * @throws IllegalStateException if the builder does not contain any message
     */
    @NotNull
    public BaseComponent[] build() {
        if (index < 0) {
            String m = "The builder does not contain any message";
            throw new IllegalStateException(m);
        }

        List<BaseComponent> comps = new ArrayList<>();

        for (ChatMessage message : map.values())
            comps.addAll(message.asComponents());

        return ArrayUtils.toArray(comps);
    }

    /**
     * Sends the message to the target player.
     * <p> The message will be built as an array of base components and sent using the spigot method.
     *
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean send() {
        try {
            Exceptions.checkPlayer(target).spigot().sendMessage(build());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Converts the message to a pattern string that can be used to recreate the message with the
     * same format and interactive features.
     *
     * <p> The pattern string will use the format of {@code <click|hover>message</text>} for each part
     * of the message that has a click or hover event.
     *
     * <p> The click and hover arguments will be in the format of "click_action:\"action_string\""
     * and "hover:\"hover_text\"", respectively.
     *
     * @return the pattern string
     */
    public String toPatternString() {
        if (index == -1) return "";

        final StringBuilder builder = new StringBuilder();
        String split = Beans.getLineSeparator();

        for (ChatMessage message : map.values()) {
            ChatEventsHandler handler = message.handler;

            if (handler.isEmpty()) {
                builder.append(message.message);
                continue;
            }

            ClickEvent click = handler.click;
            HoverEvent hover = handler.hover;

            boolean hasHover = !ChatEvent.isEmpty(hover);
            boolean hasClick = !ChatEvent.isEmpty(click);

            if (hasHover || hasClick) {
                if (hasHover) {
                    StringJoiner joiner = new StringJoiner(split);
                    hover.hover.forEach(joiner::add);

                    String temp = (joiner + "")
                            .replaceAll("\\\\[QE]", "");

                    builder.append("<hover:\"")
                            .append(temp).append('"');
                }

                if (hasClick) {
                    builder.append(hasHover ? '|' : '<');

                    builder.append(click.type)
                            .append(":\"")
                            .append(click.input).append("\">");
                }
                else builder.append('>');
            }

            builder.append(message.message).append("</text>");
        }

        return builder.toString();
    }

    /**
     * Clones the builder and returns a new builder with the same properties.
     *
     * <p> The target, parser, parseURLs, map, and index will be copied from the original builder.
     *
     * @return the cloned builder
     */
    @SuppressWarnings("all")
    @Override
    public ChatMessageBuilder clone() {
        return new ChatMessageBuilder(this);
    }

    @Override
    public String toString() {
        return "ChatMessageBuilder{" + map.entrySet() + '}';
    }

    private static TextComponent onlyComp(String message) {
        return new TextComponent(TextComponent.fromLegacyText(message));
    }

    private interface ChatEvent {

        boolean isEmpty();

        String toString();

        static boolean isEmpty(ChatEvent event) {
            return event == null || event.isEmpty();
        }
    }

    private class ClickEvent implements ChatEvent {

        private final ClickAction type;
        private final String input;

        private ClickEvent(ClickAction type, String input) {
            this.type = type;
            this.input = NeoPrismaticAPI.stripAll(input);
        }

        public net.md_5.bungee.api.chat.ClickEvent createEvent() {
            String s = Beans.formatPlaceholders(parser, input);
            return new net.md_5.bungee.api.chat.ClickEvent(type.asBukkit(), s);
        }

        public boolean isEmpty() {
            return StringUtils.isBlank(input);
        }

        @Override
        public String toString() {
            return "{type=" + type + ", input='" + input + "'}";
        }
    }

    private class HoverEvent implements ChatEvent {

        private final List<String> hover;

        private HoverEvent(List<String> hover) {
            this.hover = hover;
        }

        private HoverEvent(String... hover) {
            this(ArrayUtils.toList(hover));
        }

        @SuppressWarnings("deprecation")
        public net.md_5.bungee.api.chat.HoverEvent createEvent() {
            int size = hover.size();
            BaseComponent[] array = new BaseComponent[size];

            for (int i = 0; i < size; i++)
                array[i] = onlyComp(
                        Beans.colorize(target, parser, hover.get(i)) +
                        (i == size - 1 ? "" : "\n")
                );

            return new net.md_5.bungee.api.chat.HoverEvent(
                    net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                    array
            );
        }

        public boolean isEmpty() {
            return ArrayUtils.isArrayEmpty(hover);
        }

        @Override
        public String toString() {
            if (this.isEmpty()) return "{}";

            int last = hover.size() - 1;
            hover.set(last, hover.get(last) + "§r");

            return '{' + hover.toString() + '}';
        }
    }

    private class ChatEventsHandler {

        private ClickEvent click = null;
        private HoverEvent hover = null;

        private ChatEventsHandler() {}

        private boolean isEmpty() {
            return ChatEvent.isEmpty(click) && ChatEvent.isEmpty(hover);
        }

        @Override
        public String toString() {
            return "{" + "hover=" + hover + ", click=" + click + '}';
        }
    }

    private class ChatMessage {

        private final ChatEventsHandler handler;
        private String message;
        private ChatColor color = null;

        private ChatMessage(String message) {
            this.message = message;
            handler = new ChatEventsHandler();
        }

        private ChatMessage setClick(ClickEvent event) {
            handler.click = event;
            return this;
        }

        private ChatMessage setHover(HoverEvent event) {
            handler.hover = event;
            return this;
        }

        private ChatMessage applyColor() {
            if (color == null) compile();

            message = color.toString() + message;
            return this;
        }

        private ChatMessage setHandler(String click, String hover) {
            if (click != null) {
                String[] array = click.split(":\"", 2);
                String c = array[1];

                try {
                    handler.click = new ClickEvent(
                            ClickAction.fromString(array[0]),
                            c.substring(0, c.length() - 1)
                    );
                } catch (Exception ignored) {}
            }

            if (hover != null) {
                String h = hover.split(":\"", 2)[1];
                h = h.substring(0, h.length() - 1);

                handler.hover = new HoverEvent(Beans.splitLine(h));
            }

            return this;
        }

        private BaseComponent[] compile() {
            Matcher urlMatch = TextUtils.URL_PATTERN.matcher(message);
            TextComponent comp = onlyComp(message);

            ClickEvent c = handler.click;
            HoverEvent h = handler.hover;

            if (parseURLs && urlMatch.find())
                setClick(new ClickEvent(ClickAction.OPEN_URL, message));

            if (!ChatEvent.isEmpty(c)) comp.setClickEvent(c.createEvent());
            if (!ChatEvent.isEmpty(h)) comp.setHoverEvent(h.createEvent());

            BaseComponent[] comps = new BaseComponent[] {comp};

            color = comps[comps.length - 1].getColor();
            return comps;
        }

        private List<BaseComponent> asComponents() {
            return ArrayUtils.toCollection(new LinkedList<>(), compile());
        }

        @Override
        public String toString() {
            return "{handler=" + handler + ", message='" + message + "§r'}";
        }
    }
}
