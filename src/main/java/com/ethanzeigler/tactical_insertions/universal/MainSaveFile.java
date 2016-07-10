package com.ethanzeigler.tactical_insertions.universal;

import com.ethanzeigler.bukkit_plugin_utils.PluginCore;
import com.ethanzeigler.bukkit_plugin_utils.SaveFile;
import com.ethanzeigler.tactical_insertions.Insertion;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Holds the plugin's core save data
 */
public class MainSaveFile extends SaveFile {
    private static final String FILE_NAME = "general_save_data.yml";
    private static final String LAST_BLOCK_MAT = "historical_settings.last_block_material";
    private static final String HEADER = "DO NOT EDIT THE CONTENTS OF THIS FILE";

    public MainSaveFile(PluginCore pluginCore) {
        super(pluginCore, FILE_NAME);
    }

}
