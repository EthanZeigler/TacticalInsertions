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

        config.options().header("This is the TacticalInsertions configuration. Please know there is a setting " +
                "that is not listed here. /n There are 2 modes to this plugin: warp and respawn. Warp mode allows your " +
                "players to set warps that can be warped to at any time with their names.\n" +
                "Respawn mode allows a player to only set one warp that they will respawn at when they die. It cannot " +
                "be warped to on demand. This setting is controlled using the //ti chngemode command.\n" +
                "===========================================\n" +
                "\n" +
                "The following are descriptions of each setting besides the mode.\n" +
                "plugin_prefix = the prefix the plugin uses in chat\n" +
                "distance_from_other_tac_minimum = The minimum distance insertions much be apart. Changing will " +
                "not affect existing insertions\n" +
                "allow_players_to_drop_insertions = whether or not players are allowed to drop the insertion\n" +
                "tac_block_material = the material the tactical insertion is made of. It MUST be placeable or the " +
                "plugin will behave oddly. These are case sensitive. Find the full material list @ " +
                "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html. ==PLEASE READ!!! CHANGING THIS WILL " +
                "DELETE ALL WARPS IN THE WORLD. BE CAREFUL!!! PLEASE READ==");

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
