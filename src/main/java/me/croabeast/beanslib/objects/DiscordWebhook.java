package me.croabeast.beanslib.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

/**
 * The class for sending discord webhooks.
 *
 * @author Kihsomray
 * @since 1.1
 */
public class DiscordWebhook {

    private Webhook webhook;
    private final boolean enabled;
    private final ConfigurationSection sec;

    /**
     * Create a Discord webhook setup. Make sure
     * the section looks something like webhook.yml.
     *
     * @param section Section in question
     */
    public DiscordWebhook(@NotNull ConfigurationSection section) {

        enabled = section.getBoolean("enabled");
        this.sec = section;

    }

    /**
     * Send the webhook asynchronously.
     *
     * @param plugin Instance of plugin.
     * @param message Message to be replaced with {message}.
     */
    public void sendWebhookAsync(JavaPlugin plugin, String message) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            sendWebhook(message);
        });
    }

    /**
     * Send the webhook synchronously.
     *
     * @param message Message to be replaced with {message}.
     */
    public void sendWebhook(String message) throws IllegalArgumentException {
        if (!enabled) return;

        load(message);

        try {
            webhook.execute();
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }

    }

    private void load(String msg) {

        final String p = "{message}";

        webhook = new Webhook(sec.getString("url",
                "https://discord.com/api/webhooks/979987151099928606/ohq2MxJk3fBsAiddzLLcnBQeLDBJOowjsrhCkIfcRdukK_c2bQv7sHgt5YRzMGm2kent"));

        webhook.setContent(sec.getString("content", "").replace(p, msg));
        webhook.setUsername(sec.getString("username", "").replace(p, msg));
        webhook.setAvatarUrl(sec.getString("avatar-url", "").replace(p, msg));
        webhook.setTts(sec.getBoolean("tts", false));

        ConfigurationSection section = sec.getConfigurationSection("embeds");
        if (section == null) return;
        for (String s : section.getKeys(false)) {
            ConfigurationSection em = section.getConfigurationSection(s);
            if (em == null) continue;

            Webhook.EmbedObject embed = new Webhook.EmbedObject();
            embed.setTitle(em.getString("title", "").replace(p, msg));
            embed.setDescription(em.getString("description", "").replace(p, msg));
            embed.setUrl(em.getString("url", "").replace(p, msg));
            embed.setFooter(em.getString("footer.text", "").replace(p, msg),
                    em.getString("footer.icon-url", "").replace(p, msg));
            embed.setThumbnail(em.getString("thumbnail-url", "").replace(p, msg));
            embed.setImage(em.getString("image-url", "").replace(p, msg));
            embed.setAuthor(em.getString("author.name", "").replace(p, msg),
                    em.getString("author.url", "").replace(p, msg),
                    em.getString("author.icon-url", "").replace(p, msg));


            em = em.getConfigurationSection("fields");
            if (em == null) continue;
            for (String a : em.getKeys(false)) {
                ConfigurationSection fl = em.getConfigurationSection(a);
                if (fl == null) continue;

                embed.addField(fl.getString("name", "").replace(p, msg),
                        fl.getString("value", "").replace(p, msg),
                        fl.getBoolean("inline", false));

            }

            webhook.addEmbed(embed);
        }

    }


    @RequiredArgsConstructor
    @Data
    private static class Webhook {

        private final String url;
        private String content;
        private String username;
        private String avatarUrl;
        private boolean tts;
        private List<EmbedObject> embeds = new ArrayList<>();

        public void addEmbed(EmbedObject embed) {
            this.embeds.add(embed);
        }

        public void execute() throws IOException {
            if (content == null && embeds.isEmpty()) {
                throw new IllegalArgumentException("Set content or add at least one EmbedObject");
            }
            JSONObject json = new JSONObject();
            json.put("content", content);
            json.put("username", username);
            json.put("avatar_url", avatarUrl);
            json.put("tts", tts);
            if (!embeds.isEmpty()) {
                List<JSONObject> embedObjects = new ArrayList<>();
                for (EmbedObject embed : embeds) {
                    JSONObject jsonEmbed = new JSONObject();
                    jsonEmbed.put("title", embed.getTitle());
                    jsonEmbed.put("description", embed.getDescription());
                    jsonEmbed.put("url", embed.getUrl());

                    if (embed.getColor() != null) {
                        Color color = embed.getColor();
                        int rgb = color.getRed();
                        rgb = (rgb << 8) + color.getGreen();
                        rgb = (rgb << 8) + color.getBlue();

                        jsonEmbed.put("color", rgb);
                    }
                    EmbedObject.Footer footer = embed.getFooter();
                    EmbedObject.Image image = embed.getImage();
                    EmbedObject.Thumbnail thumbnail = embed.getThumbnail();
                    EmbedObject.Author author = embed.getAuthor();
                    List<EmbedObject.Field> fields = embed.getFields();
                    if (footer != null) {
                        JSONObject jsonFooter = new JSONObject();
                        jsonFooter.put("text", footer.getText());
                        jsonFooter.put("icon_url", footer.getIconUrl());
                        jsonEmbed.put("footer", jsonFooter);
                    }
                    if (image != null) {
                        JSONObject jsonImage = new JSONObject();
                        jsonImage.put("url", image.getUrl());
                        jsonEmbed.put("image", jsonImage);
                    }
                    if (thumbnail != null) {
                        JSONObject jsonThumbnail = new JSONObject();
                        jsonThumbnail.put("url", thumbnail.getUrl());
                        jsonEmbed.put("thumbnail", jsonThumbnail);
                    }
                    if (author != null) {
                        JSONObject jsonAuthor = new JSONObject();
                        jsonAuthor.put("name", author.getName());
                        jsonAuthor.put("url", author.getUrl());
                        jsonAuthor.put("icon_url", author.getIconUrl());
                        jsonEmbed.put("author", jsonAuthor);
                    }
                    List<JSONObject> jsonFields = new ArrayList<>();
                    for (EmbedObject.Field field : fields) {
                        JSONObject jsonField = new JSONObject();
                        jsonField.put("name", field.getName());
                        jsonField.put("value", field.getValue());
                        jsonField.put("inline", field.isInline());
                        jsonFields.add(jsonField);
                    }
                    jsonEmbed.put("fields", jsonFields.toArray());
                    embedObjects.add(jsonEmbed);
                }
                json.put("embeds", embedObjects.toArray());
            }
            URL url = new URL(this.url);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", "Java-DiscordWebhook-BY-Gelox_");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            OutputStream stream = connection.getOutputStream();
            stream.write(json.toString().getBytes(StandardCharsets.UTF_8));
            stream.flush();
            stream.close();
            connection.getInputStream().close();
            connection.disconnect();
        }

        @Data
        public static class EmbedObject {
            private final List<Field> fields = new ArrayList<>();
            private String title;
            private String description;
            private String url;
            private Color color;
            private Footer footer;
            private Thumbnail thumbnail;
            private Image image;
            private Author author;

            public EmbedObject setThumbnail(String url) {
                this.thumbnail = new Thumbnail(url);
                return this;
            }

            public EmbedObject setImage(String url) {
                this.image = new Image(url);
                return this;
            }

            public EmbedObject setFooter(String text, String icon) {
                this.footer = new Footer(text, icon);
                return this;
            }

            public EmbedObject setAuthor(String name, String url, String icon) {
                this.author = new Author(name, url, icon);
                return this;
            }

            public EmbedObject addField(String name, String value, boolean inline) {
                this.fields.add(new Field(name, value, inline));
                return this;
            }

            @AllArgsConstructor
            @Getter
            private static class Image {
                private final String url;
            }

            @AllArgsConstructor
            @Getter
            private static class Author {
                private final String name;
                private final String url;
                private final String iconUrl;
            }

            @AllArgsConstructor
            @Getter
            private static class Field {
                private final String name;
                private final String value;
                private final boolean inline;
            }

            @AllArgsConstructor
            @Getter
            private static class Footer {
                private final String text;
                private final String iconUrl;
            }

            @AllArgsConstructor
            @Getter
            private static class Thumbnail {
                private final String url;
            }
        }

        private static class JSONObject {

            private final HashMap<String, Object> map = new HashMap<>();

            void put(String key, Object value) {
                if (value != null) {
                    map.put(key, value);
                }
            }

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                Set<Map.Entry<String, Object>> entrySet = map.entrySet();
                builder.append("{");
                int i = 0;
                for (Map.Entry<String, Object> entry : entrySet) {
                    Object val = entry.getValue();
                    builder.append(quote(entry.getKey())).append(":");

                    if (val instanceof String) {
                        builder.append(quote(String.valueOf(val)));
                    } else if (val instanceof Integer) {
                        builder.append(Integer.valueOf(String.valueOf(val)));
                    } else if (val instanceof Boolean) {
                        builder.append(val);
                    } else if (val instanceof JSONObject) {
                        builder.append(val);
                    } else if (val.getClass().isArray()) {
                        builder.append("[");
                        int len = Array.getLength(val);
                        for (int j = 0; j < len; j++) {
                            builder.append(Array.get(val, j).toString()).append(j != len - 1 ? "," : "");
                        }
                        builder.append("]");
                    }

                    builder.append(++i == entrySet.size() ? "}" : ",");
                }
                return builder.toString();
            }

            private String quote(String string) {
                return "\"" + string + "\"";
            }
        }

    }

}
