package com.ethanzeigler.bukkit_plugin_utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.omg.IOP.ExceptionDetailMessage;

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

        for (ConfigValue value : ConfigValue.values()) {
            // this is a really complicated way of checking if the value is valid
            // if the default value is of the same class as the config's value

            if (!value.defaultVal.getClass().isInstance(config.get(value.toString()))) {
                config.set(value.toString(), value.defaultVal);
            }
        }
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

        saveConfig();
    }

    private void saveConfig() {
        plugin.saveConfig();
    }

    public Object get(ConfigValue path) {
        switch (path) {
            // tac block is stored as a string, but it is a Material
            case TAC_BLOCK:
                try {
                    return Material.valueOf((String) config.get(path.toString()));
                } catch (IllegalArgumentException | NullPointerException e) {
                    System.out.println("Error: cannot resolve material " + config.get(path.toString()) + ". " +
                            "Using default value: " + path.defaultVal.toString());
                    return Material.valueOf((String) path.defaultVal);
                }

            default:
                return config.get(path.toString(), path.defaultVal);
        }
    }

    public void set(ConfigValue path, Object value) {
        // set storage method
        if (path.isStoredDifferently) {
            // stored differently than the actual value
            switch (path) {
                case TAC_BLOCK:
                    if (!value.getClass().isInstance(path.defaultVal.getClass())) {
                        throw new IllegalArgumentException(
                                String.format("The value %s is not legal for the field %s", value, path));
                    }
                    break;
            }

            // is valid if it has gone this far

            switch (path) {
                case TAC_BLOCK:
                    config.set(path.toString(), value.toString());
            }
        } else {
            // normal data storage
            if (!value.getClass().isInstance(path.defaultVal)) {
                throw new IllegalArgumentException(
                        String.format("The value %s is not legal for the field %s", value, path));
            }

            // valid data
            config.set(path.toString(), value);
        }
    }
}
