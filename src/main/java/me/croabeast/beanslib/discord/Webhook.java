package me.croabeast.beanslib.discord;

import lombok.var;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * A class for sending a webhook to a Discord channel. The class uses a configuration section
 * to store the webhook settings, and can send both text and embed messages.
 *
 * <p> Text messages can be sent directly through the {@link #send()} methods, while embed
 * messages can be created and added using the register() method.
 *
 * <p> To send a message, the {@link #send()} methods can be used. The message can be either
 * provided as a parameter or set in the configuration section. If the message is not
 * set in the configuration section and is not provided as a parameter, an empty message
 * will be sent.
 *
 * <p> {@link #sendAsync()} can be used to send asynchronously using {@link CompletableFuture}.
 *
 * @author Kihsomray (forked by CroaBeast)
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
     *
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
     *
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
     *
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
            hook = new RawWebhook(sec.getString("url"), token, message);
        } catch (NullPointerException e) {
            return null;
        }

        hook.setContent(sec.getString("content")).
                setTts(sec.getBoolean("tts")).
                setAvatarUrl(sec.getString("avatar-url")).
                setUsername(sec.getString("username"));

        var s = sec.getConfigurationSection("embeds");
        if (s == null) {
            notRegistered = false;
            return hook;
        }

        var keys = new ArrayList<>(s.getKeys(false));
        if (keys.isEmpty()) {
            notRegistered = false;
            return hook;
        }

        for (var key : keys) {
            var em = s.getConfigurationSection(key);
            if (em == null) continue;

            var embed = new EmbedObject(token, message).
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

            var fields = new ArrayList<>(em.getKeys(false));
            if (fields.isEmpty()) {
                hook.addEmbed(embed);
                continue;
            }

            for (var f : fields) {
                var fl = em.getConfigurationSection(f);
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
     *
     * @return true if was correctly sent, false otherwise
     */
    public boolean send(String token, String message) {
        if (!enabled) return false;

        if (message != null && notRegistered)
            webhook = register(token, message);

        if (webhook == null) return false;

        try {
            webhook.execute();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends the webhook. If the webhook is null or isn't enabled, won't send anything.
     * <p> If any error happens when connecting to its url, will print an error in the console.
     *
     * @param message a message if no message was declared in the constructor
     * @return true if was correctly sent, false otherwise
     */
    public boolean send(String message) {
        return send("{message}", message);
    }

    /**
     * Sends the webhook. If the webhook is null or isn't enabled, won't send anything.
     * <p> If any error happens when connecting to its url, will print an error in the console.
     *
     * @return true if was correctly sent, false otherwise
     */
    public boolean send() {
        return send(null);
    }

    /**
     * Sends the webhook asynchronously. See {@link #send(String, String)} for more info.
     *
     * @param token a token
     * @param message a message if no message was declared in the constructor
     */
    public void sendAsync(String token, String message) {
        CompletableFuture.runAsync(() -> send(token, message));
    }

    /**
     * Sends the webhook asynchronously. See {@link #send(String)} for more info.
     *
     * @param message a message if no message was declared in the constructor
     */
    public void sendAsync(String message) {
        CompletableFuture.runAsync(() -> send(message));
    }

    /**
     * Sends the webhook asynchronously. See {@link #send()} for more info.
     */
    public void sendAsync() {
        CompletableFuture.runAsync(this::send);
    }
}
