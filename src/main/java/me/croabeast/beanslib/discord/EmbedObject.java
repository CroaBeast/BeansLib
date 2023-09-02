package me.croabeast.beanslib.discord;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.var;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * An object that represents embed messages.
 *
 * @author Kihsomray (forked by CroaBeast)
 * @since 1.1
 */
@Getter
public class EmbedObject {

    /**
     * The stored fields of the embed message.
     */
    private final List<Field> fields = new ArrayList<>();

    @Getter(AccessLevel.PRIVATE)
    private final String token, message;

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
     * @param token a token
     * @param message a message
     */
    public EmbedObject(String token, String message) {
        this.token = token;
        this.message = message;
    }

    /**
     * Replace the {@link #getToken()} with the {@link #getMessage()}.
     * @param string an input string to replace
     * @return the replaced string
     */
    @NotNull
    private String replace(String string) {
        if (StringUtils.isBlank(string)) return "";

        if (getMessage() == null) return string;
        if (getToken() == null) return string;

        return string.replace(getToken(), getMessage());
    }

    /**
     * Set the title for the embed object.
     * @param text a text
     * @return a reference of this object
     */
    public EmbedObject setTitle(String text) {
        title = replace(text);
        return this;
    }

    /**
     * Set the description for the embed object.
     * @param text a text
     * @return a reference of this object
     */
    public EmbedObject setDescription(String text) {
        description = replace(text);
        return this;
    }

    /**
     * Set the URL for the embed object.
     * @param text a text
     * @return a reference of this object
     */
    public EmbedObject setUrl(String text) {
        url = replace(text);
        return this;
    }

    /**
     * Set the thumbnail for the embed object.
     * @param url an url
     * @return a reference of this object
     */
    public EmbedObject setThumbnail(String url) {
        thumbnail = replace(url);
        return this;
    }

    /**
     * Set the image URL for the embed object.
     * @param url an url
     * @return a reference of this object
     */
    public EmbedObject setImage(String url) {
        image = replace(url);
        return this;
    }

    /**
     * Set the footer for the embed object.
     * @param text a text
     * @param icon an icon url
     * @return a reference of this object
     */
    public EmbedObject setFooter(String text, String icon) {
        footerText = replace(text);
        footerIcon = replace(icon);
        return this;
    }

    /**
     * Set the color for the embed object.
     * @param color a color
     * @return a reference of this object
     */
    public EmbedObject setColor(String color) {
        Color c = null;
        try {
            try {
                c = Color.decode(color);
            } catch (Exception e) {
                var clazz = Class.forName("java.awt.Color");
                c = ((Color) clazz.getField(color).get(null));
            }
        } catch (Exception ignored) {}

        this.color = c != null ? c : Color.getColor(color);
        return this;
    }

    /**
     * Set the author for the embed object.
     * @param name a text
     * @param url an url
     * @param icon an url icon
     * @return a reference of this object
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
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    static class Field {
        private final String name, value;
        private final boolean inLine;
    }

    /**
     * An author for the discord message.
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    static class Author {
        private final String name, url, iconUrl;
    }
}
