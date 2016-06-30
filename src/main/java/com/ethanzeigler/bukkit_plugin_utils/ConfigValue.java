package com.ethanzeigler.bukkit_plugin_utils;

/**
 * Created by ethan on 6/29/16.
 */
public enum ConfigValue {
    PLUGIN_PREFIX("plugin_prefix", "Tac Inserts"),
    DISTANCE_FROM_TAC("distance_from_other_tac_minimum", "3"),
    ALLOW_TORCH_DROP("allow_players_to_drop_torches", false);

    String path;
    Object defaultVal;

    ConfigValue(String path, Object defaultVal) {
        this.path = path;
        this.defaultVal = defaultVal;
    }

    public String toString() {
        return path;
    }
}
