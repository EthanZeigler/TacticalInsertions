package com.ethanzeigler.tactical_insertions.warps;

import com.ethanzeigler.bukkit_plugin_utils.PluginCore;
import com.ethanzeigler.tactical_insertions.Insertion;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Ethan on 7/7/16.
 */
public class InsertionSaveFile {
    private static final String TAC_PATH = "insertions";
    private static final String MODE = "mode";
    private static final String FILE_NAME = "insertion_save_data.yml";

    private PluginCore pluginCore;
    FileConfiguration file;

    public InsertionSaveFile(PluginCore pluginCore) {
        this.pluginCore = pluginCore;
        this.file = pluginCore.getFile(FILE_NAME);
        file.options().header("This is a save data file for the positions of tactical\n" +
                "insertions. DO NOT EDIT IT.");
    }

    public Collection<Insertion> getInsertions() {
        List<Insertion> insertions = (List<Insertion>) file.get(TAC_PATH);
        return insertions == null ? new ArrayList<Insertion>() : insertions;
    }

    public void setInsertions(Collection<Insertion> insertions) {
        Insertion[] data = insertions.toArray(new Insertion[insertions.size()]);
        file.set(TAC_PATH, data);
    }

    public boolean isWarpMode() {
        return (boolean) file.get(MODE, true);
    }

    public void setIsWarpMode(boolean isWarpMode) {
        file.set(MODE, isWarpMode);
    }

    public void save() {
        try {
            file.save(pluginCore.getFileDirectory() + FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
