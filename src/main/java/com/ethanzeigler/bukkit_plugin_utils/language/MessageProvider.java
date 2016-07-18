package com.ethanzeigler.bukkit_plugin_utils.language;

/**
 * Created by Ethan on 7/14/16.
 */
public interface MessageProvider {
    public String get(String key);

    public String getAndFormat(String key, Object... replacements);
}
