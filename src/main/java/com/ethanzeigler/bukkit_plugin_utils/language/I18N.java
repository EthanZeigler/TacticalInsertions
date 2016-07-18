package com.ethanzeigler.bukkit_plugin_utils.language;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static sun.print.ServiceDialog.getMsg;

/**
 * Created by Ethan on 7/14/16.
 */
public class I18N implements MessageProvider {
    private ResourceBundle messages;
    private Language lang;
    private MessageFormat formatter;

    public I18N(ClassLoader classLoader, Language lang) throws MissingResourceException {
        this.lang = lang;
        formatter = new MessageFormat("", lang.getLocale());

        messages = ResourceBundle.getBundle(
                lang.getResourceBundleBase(), lang.getLocale(), classLoader);
    }

    @Override
    public String get(String key) {
        return messages.getString(key);
    }

    @Override
    public String getAndFormat(String key, Object... replacements) {
        return format(getMsg(key), replacements);
    }

    private String format(String input, Object... replacements) {
        formatter.applyPattern(input);
        return formatter.format(replacements);
    }

    public void printAllKeys() {
        StringBuilder sb = new StringBuilder();
        System.out.println(messages.getBaseBundleName() + messages.getLocale());
        Enumeration<String> keys = messages.getKeys();
        while (keys.hasMoreElements()) {
            sb.append(messages.getKeys().nextElement() + ",");
        }
        System.out.println(sb.toString());
        System.out.println("Done");

    }

    public Language getLanguage() {
        return lang;
    }
}
