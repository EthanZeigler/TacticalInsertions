package com.ethanzeigler.bukkit_plugin_utils;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.util.Map;

/**
 * A wrapper framework for a Bukkit FileConfiguration file.
 */
public class SaveFile {
    private String fileName;
    private PluginCore pluginCore;
    private FileConfiguration file;
    private Map<String, Object> defaultValues;

    public SaveFile(PluginCore pluginCore, String fileName) {
        this.pluginCore = pluginCore;
        this.file = pluginCore.getFile(fileName);
        this.fileName = fileName;
    }

    public void reload() {
        this.file = pluginCore.getFile(fileName);
    }

    public void save() {
        try {
            file.save(pluginCore.getFileDirectory() + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getFile() {
        return file;
    }

    protected PluginCore getPluginCore() {
        return pluginCore;
    }

    public String getFileName() {
        return fileName;
    }
}
