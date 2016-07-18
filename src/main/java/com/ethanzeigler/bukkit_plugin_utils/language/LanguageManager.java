package com.ethanzeigler.bukkit_plugin_utils.language;

import com.ethanzeigler.bukkit_plugin_utils.FileClassLoader;
import com.ethanzeigler.bukkit_plugin_utils.PluginCore;
import org.apache.commons.codec.language.bm.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R2.boss.CraftBossBar;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ResourceBundle;

/**
 * A language manager for Bukkit plugins
 */
public class LanguageManager {
    private JavaPlugin plugin;
    private Language language;
    private String pluginPrefix;
    private MessageProvider messageProvider;

    public LanguageManager(JavaPlugin plugin, Language language, String pluginPrefix) {
        this.plugin = plugin;
        this.language = language;
        this.pluginPrefix = pluginPrefix;

        // copy language files to the plugin directory so they can be edited.
        for (Language toCopy: Language.values()) {
            plugin.saveResource(Language.getResourceBundleDir() + toCopy.getFileName(), false);
        }

        messageProvider = new I18N(new FileClassLoader(
                new File(plugin.getDataFolder().toPath() + File.separator + Language.getResourceBundleDir()).toPath()), language);

        System.out.println("Printing keys:...");
        ((I18N) messageProvider).printAllKeys();
    }


    /**
     * Gets the message template if it exists from the current language file.
     *
     * @param msg the message template to get
     * @return the message of that template
     */
    public String getMessage(String msg) {
        return (String) messageProvider.get(msg);
    }

    /**
     * Sends a formatted message to the player
     *
     * @param player     player to send the message to
     * @param startColor color to start the message with. If null, will be {@link ChatColor#RESET}.
     * @param message    the message to send
     */
    public void sendMessage(CommandSender player, ChatColor startColor, String message) {
        player.sendMessage(getFormattedMessage(startColor, message));
    }

    public void sendMessage(CommandSender player, String message) {
        sendMessage(player, null, message);
    }

    /**
     * Sends a message to the player on the next tick synchronously for when a message needs to be sent during an
     * async action.
     *
     * @param player     player to send the message to
     * @param startColor color to start the message with. If null, will be {@link ChatColor#RESET}
     * @param message    the message to send
     */
    public void sendSyncMessage(CommandSender player, ChatColor startColor, String message) {
        plugin.getServer().getScheduler().runTask(plugin, () -> sendMessage(player, startColor, message));
    }

    public void sendSyncMessgae(CommandSender player, String message) {
        sendSyncMessage(player, null, message);
    }

    public String getFormattedMessage(String msg) {
        return getFormattedMessage(null, msg);
    }

    public String getFormattedMessage(ChatColor startColor, String msg) {
        return String.format("%s[%s] %s", startColor == null ? ChatColor.RESET : startColor, pluginPrefix, msg);
    }

    public Language getLanguage() {
        return language;
    }
}
