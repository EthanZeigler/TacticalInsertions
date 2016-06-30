package com.ethanzeigler.bukkit_plugin_utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import static com.ethanzeigler.bukkit_plugin_utils.ConfigValue.*;

/**
 * Created by ethan on 6/29/16.
 */
public class ConfigManager {

    private JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        config = plugin.getConfig();
        // use plugin prefix as the indicator of a valid config
        if (config.get(PLUGIN_PREFIX.toString()) == null) {
            // file is not set up
            for (ConfigValue val:
                 ConfigValue.values()) {
                config.set(val.toString(), val.defaultVal);
            }
        } else {
            // other operations
        }
    }

    private void saveConfig() {
        plugin.saveConfig();
    }

    public Object get(ConfigValue path) {
        return config.get(path.toString(), path.defaultVal);
    }
}
