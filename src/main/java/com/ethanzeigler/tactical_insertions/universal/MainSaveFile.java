package com.ethanzeigler.tactical_insertions.universal;

import com.ethanzeigler.bukkit_plugin_utils.PluginCore;
import com.ethanzeigler.bukkit_plugin_utils.SaveFile;

/**
 * Holds the plugin's core save data
 */
public class MainSaveFile extends SaveFile {
    private static final String FILE_NAME = "general_save_data.yml";
    private static final String HEADER = "DO NOT EDIT THE CONTENTS OF THIS FILE";

    enum Path {
        LAST_BLOCK_MAT("tactical_insertions.last_insert_material", null),
        LAST_RUN_VERSION("last_run_version", 1.0),
        /**
         * The last run mode. If true, warp mode. If false, respawn.
         */
        LAST_RUN_MODE("tactical_insertions.last_run_mode", "WARP");

        String path;
        Object defaultValue;

        Path(String path, Object defaultValue) {
            this.defaultValue = defaultValue;
            this.path = path;
        }

        /**
         * Returns the name of this enum constant, as contained in the
         * declaration.  This method may be overridden, though it typically
         * isn't necessary or desirable.  An enum type should override this
         * method when a more "programmer-friendly" string form exists.
         *
         * @return the name of this enum constant
         */
        @Override
        public String toString() {
            return path;
        }
    }

    public MainSaveFile(PluginCore pluginCore) {
        super(pluginCore, FILE_NAME);
    }

    public Object get(Path value) {
        switch (value) {
            case LAST_RUN_MODE:
                String mode = (String) getRawData(value);
                if (mode.equalsIgnoreCase("WARP")) {
                    return true;
                } else {
                    return false;
                }

            default:
                return getRawData(value);
        }
    }

    public void set(Path path, Object value) {
        switch (path) {
            case LAST_RUN_MODE:
                if (value instanceof String &&
                        (((String) value).equalsIgnoreCase("WARP") || ((String) value).equalsIgnoreCase("RESPAWN"))) {
                    getFile().set(path.toString(), value);
                }

            default:
                getFile().set(path.toString(), value);
        }
    }

    private Object getRawData(Path value) {
        return getFile().get(value.toString(), value.defaultValue);
    }
}
