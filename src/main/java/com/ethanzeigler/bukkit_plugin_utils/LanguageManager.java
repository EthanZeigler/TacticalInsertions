package com.ethanzeigler.bukkit_plugin_utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * A language manager for Bukkit plugins
 */
public class LanguageManager {
    private FileConfiguration langMap;
    private JavaPlugin plugin;
    private Language language;
    private String pluginPrefix;
    public static final String REPLACE_SEPERATOR = "#$";

    public LanguageManager(JavaPlugin plugin, Language language, String pluginPrefix) {
        this.plugin = plugin;
        this.language = language;
        this.pluginPrefix = pluginPrefix;
        langMap = YamlConfiguration.loadConfiguration(new File(language.fileName));
    }

    /**
     * Gets the message template if it exists from the current language file.
     * If it does not exist, an {@link IllegalArgumentException} will be thrown.
     *
     * @param msg the message template to get
     * @return the message of that template
     */
    public String getMessage(String msg) {
        String message = (String) langMap.get(msg);
        if (message == null) throw new RuntimeException(new IllegalArgumentException(
                String.format("Message template \"%s\" could not be found in the \"%s\" file", msg, language)));

        return message;
    }

    /**
     * Replaces placeholders in messages separated by {@link LanguageManager#REPLACE_SEPERATOR} (#$)
     *
     * @param template the String that needs placeholders replaced
     * @param args     The replacing placeholder and replacement in the format "placeholder#$replacer"
     * @return the template with the replaced values
     */
    public static String replaceValues(String template, String... args) {
        String output = new String(template);
        for (int i = 0; i < args.length; i++) {
            String[] sides = args[i].split(REPLACE_SEPERATOR);
            assert sides.length == 2;
            output = output.replace(sides[0], sides[1]);
        }
        return output;
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
