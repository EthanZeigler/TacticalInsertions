package com.ethanzeigler.bukkit_plugin_utils.language;

import java.util.Locale;

/**
 * Languages supported by the plugin
 */
public enum Language {
    ENGLISH("en", Locale.ENGLISH);

    /**
     * The path of the file for each language
     */
    String code;
    Locale locale;

    Language(String code, Locale locale) {
        this.code = code;
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    public static String getResourceBundleBase() {
        return "Language";
    }

    public static String getResourceBundleDir() {
        return "Languages/";
    }

    public String getFileName() {
        return getResourceBundleBase() + "_" + code + ".properties";
    }

    public String getLangCode() {
        return code;
    }
}
