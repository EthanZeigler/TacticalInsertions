package com.ethanzeigler.bukkit_plugin_utils;

import org.bukkit.Material;

/**
 * Created by ethan on 6/29/16.
 */
public enum ConfigValue {
    PLUGIN_PREFIX("plugin_prefix", "Tac Inserts", false),
    DISTANCE_FROM_TAC("distance_from_other_tac_minimum", 3, false),
    ALLOW_TORCH_DROP("allow_players_to_drop_insertions", false, false),
    TAC_BLOCK("tac_block_material", Material.DIAMOND_BLOCK.toString(), true);

    String path;
    Object defaultVal;
    boolean isStoredDifferently;

    ConfigValue(String path, Object defaultVal, boolean isStoredDifferently) {
        this.path = path;
        this.defaultVal = defaultVal;
        this.isStoredDifferently = isStoredDifferently;
    }

    public String toString() {
        return path;
    }
}
