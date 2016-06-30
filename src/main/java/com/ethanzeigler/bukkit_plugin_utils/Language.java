package com.ethanzeigler.bukkit_plugin_utils;

/**
 * Languages supported by the plugin
 */
public enum Language {
    ENGLISH("english.yml");

    /**
     * The path of the file for each language
     */
    String fileName;

    Language(String fileName) {
        this.fileName = "lang/" + fileName;
    }
}
