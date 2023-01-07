package me.croabeast.beanslib.object.discord;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * An object for sending discord webhooks.
 * @author Kihsomray
 * @fork CroaBeast
 * @since 1.1
 */
public class Webhook {

    private final ConfigurationSection sec;
    private final boolean enabled;

    /**
     * The webhook items handler to display.
     * <p> See {@link RawWebhook} for more info.
     */
    private RawWebhook webhook;
    private boolean notRegistered = true;

    /**
     * Create a Discord webhook setup using a default configuration section.
     * <p> Make sure the section looks something like this:
     * <a href="https://paste.helpch.at/yojidanamu.bash">webhook.yml</a>
     *
     * @param sec a valid configuration section
     * @param token a token to replace
     * @param message a basic message to show
     */
    public Webhook(@NotNull ConfigurationSection sec, String token, String message) {
        this.sec = sec;
        enabled = sec.getBoolean("enabled");

        if (message != null)
            webhook = register(token, message);
    }

    /**
     * Create a Discord webhook setup using a default configuration section and a default token "{message}".
     * <p> Make sure the section looks something like this:
     * <a href="https://paste.helpch.at/yojidanamu.bash">webhook.yml</a>
     *
     * @param sec a valid configuration section
     * @param message a basic message to show
     */
    public Webhook(@NotNull ConfigurationSection sec, String message) {
        this(sec, "{message}", message);
    }

    /**
     * Create a Discord webhook setup using a default configuration section with no message to display.
     * <p> Make sure the section looks something like this:
     * <a href="https://paste.helpch.at/yojidanamu.bash">webhook.yml</a>
     *
     * @param sec a valid configuration section
     */
    public Webhook(@NotNull ConfigurationSection sec) {
        this(sec, null);
    }

    /**
     * Registers the {@link #webhook} with all the necessary items to display.
     *
     * @param token a token
     * @param message a message
     * @return the requested webhook
     */
    private RawWebhook register(String token, String message) {
        RawWebhook hook;
        try {
            hook = new RawWebhook(sec.getString("url"), token, message).
                    setContent(sec.getString("content")).
                    setAvatarUrl(sec.getString("avatar-url")).
                    setUsername(sec.getString("username")).
                    setTTS(sec.getBoolean("tts"));
        } catch (NullPointerException e) {
            return null;
        }

        ConfigurationSection s = sec.getConfigurationSection("embeds");
        if (s == null) return null;

        List<String> keys = new ArrayList<>(s.getKeys(false));
        if (keys.isEmpty()) return null;

        for (String key : keys) {
            ConfigurationSection em = s.getConfigurationSection(key);
            if (em == null) continue;

            EmbedObject embed = new EmbedObject(token, message).
                    setTitle(em.getString("title")).
                    setDescription(em.getString("description")).
                    setUrl(em.getString("url")).
                    setFooter(
                            em.getString("footer.text"),
                            em.getString("footer.icon-url")
                    ).
                    setThumbnail(em.getString("thumbnail-url")).
                    setImage(em.getString("image-url")).
                    setAuthor(
                            em.getString("author.name"),
                            em.getString("author.url"),
                            em.getString("author.icon-url")
                    ).
                    setColor(em.getString("color"));

            em = em.getConfigurationSection("fields");
            if (em == null) {
                hook.addEmbed(embed);
                continue;
            }

            List<String> fields = new ArrayList<>(em.getKeys(false));
            if (fields.isEmpty()) {
                hook.addEmbed(embed);
                continue;
            }

            for (String f : fields) {
                ConfigurationSection fl = em.getConfigurationSection(f);
                if (fl != null)
                    embed.addField(
                            fl.getString("name"),
                            fl.getString("value"),
                            fl.getBoolean("inline")
                    );
            }

            hook.addEmbed(embed);
        }

        notRegistered = false;
        return hook;
    }

    /**
     * Sends the webhook. If the webhook is null or isn't enabled, won't send anything.
     * <p> If any error happens when connecting to its url, will print an error in the console.
     *
     * @param token a token
     * @param message a message if no message was declared in the constructor
     */
    public void send(String token, String message) {
        if (!enabled) return;

        if (message != null && notRegistered)
            webhook = register(token, message);

        if (webhook == null) return;

        try {
            webhook.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the webhook. If the webhook is null or isn't enabled, won't send anything.
     * <p> If any error happens when connecting to its url, will print an error in the console.
     *
     * @param message a message if no message was declared in the constructor
     */
    public void send(String message) {
        send("{message}", message);
    }

    /**
     * Sends the webhook. If the webhook is null or isn't enabled, won't send anything.
     * <p> If any error happens when connecting to its url, will print an error in the console.
     */
    public void send() {
        send(null);
    }

    /**
     * Sends the webhook asynchronously.
     * <p> See {@link #send(String, String)} for more info.
     * @param plugin a javaPlugin's instance
     * @param token a token
     * @param message a message if no message was declared in the constructor
     */
    public void sendAsync(JavaPlugin plugin, String token, String message) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> send(token, message));
    }

    /**
     * Sends the webhook asynchronously.
     * <p> See {@link #send(String)} for more info.
     * @param plugin a javaPlugin's instance
     * @param message a message if no message was declared in the constructor
     */
    public void sendAsync(JavaPlugin plugin, String message) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> send(message));
    }

    /**
     * Sends the webhook asynchronously.
     * <p> See {@link #send()} for more info.
     * @param plugin a javaPlugin's instance
     */
    public void sendAsync(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> send());
    }
}
