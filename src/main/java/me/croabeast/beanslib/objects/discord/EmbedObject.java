package me.croabeast.beanslib.objects.discord;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * An object that represents embed messages.
 * Only accessible inside the package.
 * @author Kihsomray
 * @forkBy CroaBeast
 * @since 1.1
 */
@Getter
class EmbedObject {

    /**
     * The stored fields of the embed message.
     */
    private final List<Field> fields = new ArrayList<>();

    private final String token;
    private final String message;

    /**
     * Value necessary to use.
     */
    private String
            title, description, url,
            image, thumbnail,
            footerText, footerIcon;

    /**
     * The color of the embed message.
     */
    private Color color;
    /**
     * The author of the embed message.
     */
    private Author author;

    /**
     * Constructs an object with a message token and a message.
     * @param message a message
     */
    public EmbedObject(String token, String message) {
        this.token = token;
        this.message = message;
    }

    /**
     * Gets the message token, can be null.
     * @return the message
     */
    private String getToken() {
        return token;
    }

    /**
     * Gets the message, can be null.
     * @return the message
     */
    private String getMessage() {
        return message;
    }

    /**
     * Replace the {@link #getToken()} with the {@link #getMessage()}.
     * @param string an input string to replace
     * @return the replaced string
     */
    @NotNull
    private String replace(String string) {
        if (string == null) return "";
        if (getMessage() == null) return string;
        if (getToken() == null) return string;
        return string.replace(getToken(), getMessage());
    }

    /**
     * Set the title for the embed object.
     * @param text a text
     * @return the object instance
     */
    public EmbedObject setTitle(String text) {
        title = replace(text);
        return this;
    }

    /**
     * Set the description for the embed object.
     * @param text a text
     * @return the object instance
     */
    public EmbedObject setDescription(String text) {
        description = replace(text);
        return this;
    }

    /**
     * Set the URL for the embed object.
     * @param text a text
     * @return the object instance
     */
    public EmbedObject setUrl(String text) {
        url = replace(text);
        return this;
    }

    /**
     * Set the thumbnail for the embed object.
     * @param url an url
     * @return the object instance
     */
    public EmbedObject setThumbnail(String url) {
        thumbnail = replace(url);
        return this;
    }

    /**
     * Set the image URL for the embed object.
     * @param url an url
     * @return the object instance
     */
    public EmbedObject setImage(String url) {
        image = replace(url);
        return this;
    }

    /**
     * Set the footer for the embed object.
     * @param text a text
     * @param icon an icon url
     * @return the object instance
     */
    public EmbedObject setFooter(String text, String icon) {
        footerText = replace(text);
        footerIcon = replace(icon);
        return this;
    }

    /**
     * Set the color for the embed object.
     * @param color a color
     * @return the object instance
     */
    public EmbedObject setColor(String color) {
        Color c = DefaultColor.to(color);
        this.color = c != null ? c : Color.getColor(color);
        return this;
    }

    /**
     * Set the author for the embed object.
     * @param name a text
     * @param url an url
     * @param icon an url icon
     * @return the object instance
     */
    public EmbedObject setAuthor(String name, String url, String icon) {
        this.author = new Author(replace(name), replace(url), replace(icon));
        return this;
    }

    /**
     * Add a field to the embed object.
     * @param name   a text
     * @param value  a value
     * @param inLine if is in line
     */
    public void addField(String name, String value, boolean inLine) {
        this.fields.add(new Field(replace(name), replace(value), inLine));
    }

    /**
     * A field for text in the discord message.
     */
    @AllArgsConstructor
    @Getter
    static class Field {
        private final String name;
        private final String value;
        private final boolean inLine;
    }

    /**
     * An author for the discord message.
     */
    @AllArgsConstructor
    @Getter
    static class Author {
        private final String name;
        private final String url;
        private final String iconUrl;
    }

    /**
     * An enum to get the default color variables.
     */
    enum DefaultColor {
        WHITE(Color.WHITE),
        LIGHT_GRAY(Color.LIGHT_GRAY),
        GRAY(Color.GRAY),
        DARK_GRAY(Color.DARK_GRAY),
        BLACk(Color.BLACK),
        RED(Color.RED),
        PINK(Color.PINK),
        ORANGE(Color.ORANGE),
        YELLOW(Color.YELLOW),
        GREEN(Color.GREEN),
        MAGENTA(Color.MAGENTA),
        CYAN(Color.CYAN),
        BLUE(Color.BLUE);

        private final Color color;

        DefaultColor(Color color) {
            this.color = color;
        }

        /**
         * Gets the default by its name.
         * @param name a string
         * @return the color, can be null
         */
        static Color to(String name) {
            if (StringUtils.isBlank(name)) return null;
            for (DefaultColor d : values()) {
                if (!name.matches("(?i)" + d)) continue;
                return d.color;
            }
            return null;
        }
    }
}
