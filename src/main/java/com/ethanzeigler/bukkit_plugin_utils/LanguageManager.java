package com.ethanzeigler.bukkit_plugin_utils;

import com.sun.istack.internal.Nullable;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * A language manager for Bukkit plugins
 */
public class LanguageManager {
    private FileConfiguration langMap;
    private Language language;
    public static final String REPLACE_SEPERATOR = "#$";

    public LanguageManager(Language language) {
        this.language = language;
        langMap = YamlConfiguration.loadConfiguration(new File(language.fileName));
    }

    /**
     * Gets the message template if it exists from the current language file.
     * If it does not exist, an {@link IllegalArgumentException} will be thrown.
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
     * @param template the String that needs placeholders replaced
     * @param args The replacing placeholder and replacement in the format "placeholder#$replacer"
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
     * @param player player to send the message to
     * @param startColor color to start the message with. If null, will be {@link ChatColor#RESET}.
     * @param message the message to send
     */
    public static void sendAndFormatMessage(Player player, @Nullable ChatColor startColor, String message) {
        player.sendMessage((startColor != null ? ChatColor.RESET : startColor) + message);
    }

    public Language getLanguage() {
        return language;
    }
}
