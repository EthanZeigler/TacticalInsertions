package com.ethanzeigler.bukkit_plugin_utils;

import com.ethanzeigler.tactical_insertions.TacStackFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Created by ethan on 6/27/16.
 */
public class PluginCore {
    private String pluginPrefix;
    private boolean displayVersionInfo;
    private boolean cachePlayerFiles;
    private String versionInformation;
    private LanguageManager languageManager;
    private ConfigManager configManager;
    private Map<UUID, FileConfiguration> playerFileCache;
    private String dirPath;

    public PluginCore(JavaPlugin plugin, boolean cachePlayerFiles, Language lang) {
        configManager = new ConfigManager(plugin);
        languageManager = new LanguageManager(plugin, Language.ENGLISH, (String) configManager.get(ConfigValue.PLUGIN_PREFIX));
        this.cachePlayerFiles = cachePlayerFiles;
        this.pluginPrefix = (String) configManager.get(ConfigValue.PLUGIN_PREFIX);
        dirPath = plugin.getDataFolder().getPath() + "/";

        // create data folder
        new File(dirPath).mkdirs();

        // load config data
        loadConfigData();
    }

    public FileConfiguration getPlayerFile(OfflinePlayer player) {
        if (cachePlayerFiles) {
            return null;
            //todo fill
        } else {
            try {
                File file = new File(String.format(dirPath + "Player Files/%s.txt", player.getUniqueId()));
                file.createNewFile();

                return YamlConfiguration.loadConfiguration(file);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(new IOException(
                        String.format("Could not load player file: %s/%s", player.getName(), player.getUniqueId())));
            }
        }
    }

    public FileConfiguration getFile(String path) {
        File file = null;
        try {
            file = new File(dirPath + path);
            file.createNewFile();

            return YamlConfiguration.loadConfiguration(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(new IOException(
                    "Could not load file:" + file.getPath()));
        }
    }

    public void logToConsole(String msg) {
        logToConsole(msg, null);
    }

    public void logToConsole(String msg, ChatColor startColor) {
        Bukkit.getConsoleSender().sendMessage(languageManager.getFormattedMessage(msg));
    }

    private void loadConfigData() {
        TacStackFactory.setSeedMaterial(((Material) configManager.get(ConfigValue.TAC_BLOCK)));
    }

    public String getPluginPrefix() {
        return pluginPrefix;
    }

    public void setPluginPrefix(String pluginPrefix) {
        this.pluginPrefix = pluginPrefix;
    }

    public void setDisplayVersionInfo(boolean displayVersionInfo) {
        this.displayVersionInfo = displayVersionInfo;
    }

    public boolean isDisplayVersionInfo() {
        return displayVersionInfo;
    }

    public boolean isCachePlayerFiles() {
        return cachePlayerFiles;
    }

    public String getVersionInformation() {
        return versionInformation;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public String getFileDirectory() {
        return dirPath;
    }
}
