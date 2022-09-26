package me.croabeast.beanslib.object.discord;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * The class for sending discord webhooks.
 *
 * @deprecated See {@link Webhook}.
 *
 * @author Kihsomray
 * @since 1.1
 */
@Deprecated
public class DiscordWebhook {

    private final Webhook webhook;

    /**
     * Create a Discord webhook setup using a default configuration section.
     * <p> Make sure the section looks something like this:
     * <a href="https://paste.helpch.at/yojidanamu.bash">webhook.yml</a>
     *
     * @param section Section in question
     */
    public DiscordWebhook(@NotNull ConfigurationSection section) {
        webhook = new Webhook(section);
    }

    /**
     * Sends the webhook. If the webhook is null or isn't enabled, won't send anything.
     * <p> If any error happens when connecting to its url, will print an error in the console.
     *
     * @param message a message if no message was declared in the constructor
     */
    public void sendWebhook(String message)  {
        webhook.send(message);
    }

    /**
     * Sends the webhook asynchronously.
     * <p> See {@link #sendWebhook(String)} for more info.
     *
     * @param plugin a javaPlugin's instance
     * @param message a message if no message was declared in the constructor
     */
    public void sendWebhookAsync(JavaPlugin plugin, String message) {
        webhook.sendAsync(plugin, message);
    }
}
