package me.croabeast.beanslib.discord;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.var;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A class that represents a raw webhook for sending messages to a Discord channel.
 *
 * <p> This class can be used to create and send simple messages or messages with
 * embedded content, such as images and links.
 *
 * <p> To use this class, create an instance of it with the URL of the webhook and
 * the message to be sent.
 *
 * <p> The message can then be further customized with methods to add embedded
 * content and set additional parameters such as the username, avatar URL, and
 * text-to-speech (TTS) settings.
 *
 * <p> Finally, call the {@link #execute()} method to send the message to the Discord
 * channel.
 *
 * <p> Note: This class requires the Apache Commons Lang and JSON libraries, as well as Java 11 or higher.
 *
 * @author Kihsomray
 * @fork CroaBeast
 * @since 1.1
 */
public class RawWebhook {

    /**
     * List of embed objects to display.
     */
    private final List<EmbedObject> embeds = new ArrayList<>();

    @Getter(AccessLevel.PRIVATE)
    private final String url, token, message;

    private String content, username, avatarUrl;

    @Accessors(chain = true)
    @Setter
    private boolean tts;

    /**
     * Constructs a new RawWebhook instance with the specified URL, token and message.
     *
     * @param url the URL of the webhook.
     * @param token the token of the webhook.
     * @param message the message of the webhook.
     *
     * @throws NullPointerException if the URL is blank.
     */
    public RawWebhook(String url, String token, String message) {
        if (StringUtils.isBlank(url))
            throw new NullPointerException("URL can not be null");

        this.url = url;
        this.token = token;
        this.message = message;
    }

    /**
     * Adds an embed object to the {@link #embeds} list.
     * @param embed an embed object
     */
    public void addEmbed(EmbedObject embed) {
        embeds.add(embed);
    }

    @NotNull
    private String replace(String string) {
        if (StringUtils.isBlank(string)) return "";

        if (getMessage() == null) return string;
        if (getToken() == null) return string;

        return string.replace(getToken(), getMessage());
    }

    /**
     * Sets the content for the webhook.
     * @param text a text
     * @return the object's instance
     */
    public RawWebhook setContent(String text) {
        content = replace(text);
        return this;
    }

    /**
     * Sets the username for the webhook.
     * @param text a text
     * @return the object's instance
     */
    public RawWebhook setUsername(String text) {
        username = replace(text);
        return this;
    }

    /**
     * Sets the avatar's url for the webhook.
     * @param url an url
     * @return the object's instance
     */
    public RawWebhook setAvatarUrl(String url) {
        avatarUrl = replace(url);
        return this;
    }

    private JSONObject registerContent(JSONObject json) {
        if (embeds.isEmpty()) return json;
        var embedObjects = new ArrayList<JSONObject>();

        for (var embed : embeds) {
            var jsonEmbed = new JSONObject().
                    put("title", embed.getTitle()).
                    put("description", embed.getDescription()).
                    put("url", embed.getUrl());

            if (embed.getColor() != null) {
                var color = embed.getColor();
                int rgb = color.getRed();
                rgb = (rgb << 8) + color.getGreen();
                rgb = (rgb << 8) + color.getBlue();

                jsonEmbed.put("color", rgb);
            }

            String footerText = embed.getFooterText(),
                    footerIcon = embed.getFooterIcon(),
                    image = embed.getImage(),
                    thumbnail = embed.getThumbnail();

            var author = embed.getAuthor();
            var fields = embed.getFields();

            if (footerText != null || footerIcon != null) {
                jsonEmbed.put("footer", new JSONObject().
                        put("text", footerText).
                        put("icon_url", footerIcon)
                );
            }

            if (image != null)
                jsonEmbed.put("image", new JSONObject().put("url", image));

            if (thumbnail != null)
                jsonEmbed.put("thumbnail", new JSONObject().put("url", thumbnail));

            if (author != null) {
                jsonEmbed.put("author", new JSONObject().
                        put("name", author.getName()).
                        put("url", author.getUrl()).
                        put("icon_url", author.getIconUrl())
                );
            }

            if (!fields.isEmpty()) {
                jsonEmbed.put("fields", fields.stream().map(f ->
                        new JSONObject().
                                put("name", f.getName()).
                                put("value", f.getValue()).
                                put("inline", f.isInLine())
                ).toArray());
            }

            embedObjects.add(jsonEmbed);
        }

        return json.put("embeds", embedObjects.toArray());
    }

    /**
     * Executes the webhook by sending the message to the channel.
     *
     * @throws IOException if it has an error connecting to the url or if the url is invalid
     * @throws NullPointerException if there is no content or embeds to display
     */
    public void execute() throws IOException {
        if (content == null)
            throw new NullPointerException("Set a content in the embed");

        var json = new JSONObject().
                put("avatar_url", avatarUrl).
                put("content", content).
                put("username", username).
                put("tts", tts);

        json = registerContent(json);

        var url = new URL(getUrl());
        var c = (HttpsURLConnection) url.openConnection();

        c.addRequestProperty("Content-Type", "application/json");

        c.addRequestProperty("User-Agent",
                "Java-DiscordWebhook-BY-Gelox_");

        c.setDoOutput(true);
        c.setRequestMethod("POST");

        var stream = c.getOutputStream();

        stream.write((json + "").getBytes(StandardCharsets.UTF_8));
        stream.flush();
        stream.close();

        c.getInputStream().close();
        c.disconnect();
    }

    static class JSONObject {

        private final HashMap<String, Object> map = new HashMap<>();

        JSONObject put(String key, Object value) {
            if (value != null) map.put(key, value);
            return this;
        }

        private String quote(String string) {
            return "\"" + string + "\"";
        }

        @Override
        public String toString() {
            var builder = new StringBuilder();
            var entrySet = map.entrySet();

            builder.append("{");
            int i = 0;

            for (var entry : entrySet) {
                var val = entry.getValue();
                builder.append(quote(entry.getKey())).append(":");

                if (val instanceof Boolean || val instanceof JSONObject)
                    builder.append(val);
                else if (val instanceof Integer)
                    builder.append(Integer.valueOf(String.valueOf(val)));
                else if (val instanceof String)
                    builder.append(quote(String.valueOf(val)));
                else if (val.getClass().isArray()) {
                    builder.append("[");
                    int len = Array.getLength(val);

                    for (int j = 0; j < len; j++) {
                        var temp = Array.get(val, j).toString();
                        builder.append(temp).append(j != len - 1 ? "," : "");
                    }
                    builder.append("]");
                }

                builder.append(++i == entrySet.size() ? "}" : ",");
            }

            return builder + "";
        }
    }
}
