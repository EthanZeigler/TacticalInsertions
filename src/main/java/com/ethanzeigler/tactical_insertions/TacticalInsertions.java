package com.ethanzeigler.tactical_insertions;

import com.ethanzeigler.bukkit_plugin_utils.ConfigManager;
import com.ethanzeigler.bukkit_plugin_utils.ConfigValue;
import com.ethanzeigler.bukkit_plugin_utils.PluginCorePlugin;
import com.ethanzeigler.bukkit_plugin_utils.language.Language;
import com.ethanzeigler.bukkit_plugin_utils.PluginCore;
import com.ethanzeigler.tactical_insertions.respawns.RespawnEventListener;
import com.ethanzeigler.tactical_insertions.universal.AllModeListener;
import com.ethanzeigler.tactical_insertions.universal.MainSaveFile;
import com.ethanzeigler.tactical_insertions.universal.ModCommandListener;
import com.ethanzeigler.tactical_insertions.universal.ParticleEffectManager;
import com.ethanzeigler.tactical_insertions.warps.WarpEventListener;
import com.ethanzeigler.tactical_insertions.warps.InsertionSaveFile;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ethan on 6/28/16.
 */
public class TacticalInsertions extends PluginCorePlugin implements Listener, CommandExecutor {
    private static PluginCore pluginCore;
    private Map<Location, Insertion> insertions;
    private ParticleEffectManager particleEffectManager;


    @Override
    public void onDisable() {
        // get plugin mode
        boolean mode = (Boolean) pluginCore.getMainSaveFile().get(MainSaveFile.Path.IS_WARP_MODE);

        // serlialize data for warp mode
        InsertionSaveFile saveFile = new InsertionSaveFile(pluginCore);

        Collection<Insertion> data = insertions.values();
        saveFile.setInsertions(data);
        saveFile.setIsWarpMode(mode);
        saveFile.save();

        saveConfig();
        pluginCore.getMainSaveFile().save();
    }

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(Insertion.class);

        // load plugin resources
        pluginCore = new PluginCore(this, false, Language.ENGLISH);

        // get plugin mode
        boolean mode = (Boolean) pluginCore.getMainSaveFile().get(MainSaveFile.Path.IS_WARP_MODE);

        insertions = new ConcurrentHashMap<>();
        loadData(mode);

        // set up listeners
        if (mode) {
            new WarpEventListener(this);
        } else {
            new RespawnEventListener(this);
        }

        // register listeners for both modes
        new AllModeListener(this);

        // mod commands
        new ModCommandListener(pluginCore, this);

        // particles
        particleEffectManager = new ParticleEffectManager(this);

        // check unsafe changes to config
        resolveUnsafeChanges(pluginCore, insertions);
    }

    @Override
    public void onVersionUpdate() {

    }

    public void loadData(boolean mode) {
        if (mode) {
            // warp data
            InsertionSaveFile saveFile = new InsertionSaveFile(pluginCore);
            if (!saveFile.isWarpMode()) {

                for (Insertion insertion : saveFile.getInsertions()) {
                    insertions.put(insertion.getLoc(), insertion);
                }
                pluginCore.logToConsole(pluginCore.getLanguageManager().getMessage("warp-mode-enabled") + saveFile.getInsertions().size());
            } else {
                pluginCore.logToConsole(pluginCore.getLanguageManager().getMessage("invalid-save-data"));
            }

        } else {
            // respawn data
            InsertionSaveFile saveFile = new InsertionSaveFile(pluginCore);
            if (saveFile.isWarpMode()) {

                for (Insertion insertion : saveFile.getInsertions()) {
                    insertions.put(insertion.getLoc(), insertion);
                }
                pluginCore.logToConsole(pluginCore.getLanguageManager().getMessage("respawn-mode-enabled") + saveFile.getInsertions().size());
            } else {
                pluginCore.logToConsole(pluginCore.getLanguageManager().getMessage("invalid-save-data"));
            }
        }
    }

    public void resolveUnsafeChanges(PluginCore core, Map<Location, Insertion> insertions) {
        MainSaveFile main = core.getMainSaveFile();
        ConfigManager config = core.getConfigManager();

        // material change
        if (!config.get(ConfigValue.TAC_BLOCK).equals(main.get(MainSaveFile.Path.LAST_BLOCK_MAT))) {
            core.logToConsole(pluginCore.getLanguageManager().getMessage("material-changed-clear-inserts"));
            ModCommandListener.deleteAllInsertions(insertions);
            main.set(MainSaveFile.Path.LAST_BLOCK_MAT, config.get(ConfigValue.TAC_BLOCK));
            main.save();
        }
    }

    public static PluginCore getPluginCore() {
        return pluginCore;
    }

    public Map<Location, Insertion> getInsertions() {
        return insertions;
    }
}
