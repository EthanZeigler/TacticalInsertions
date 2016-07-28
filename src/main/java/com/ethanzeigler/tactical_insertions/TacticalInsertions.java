package com.ethanzeigler.tactical_insertions;

import com.ethanzeigler.bukkit_plugin_utils.ConfigManager;
import com.ethanzeigler.bukkit_plugin_utils.ConfigValue;
import com.ethanzeigler.bukkit_plugin_utils.language.Language;
import com.ethanzeigler.bukkit_plugin_utils.PluginCore;
import com.ethanzeigler.tactical_insertions.respawns.RespawnEventListener;
import com.ethanzeigler.tactical_insertions.universal.AllModeListener;
import com.ethanzeigler.tactical_insertions.universal.MainSaveFile;
import com.ethanzeigler.tactical_insertions.universal.ModCommandListener;
import com.ethanzeigler.tactical_insertions.universal.ParticleEffectManager;
import com.ethanzeigler.tactical_insertions.warps.WarpEventListener;
import com.ethanzeigler.tactical_insertions.warps.WarpSaveFile;
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
public class TacticalInsertions extends JavaPlugin implements Listener, CommandExecutor {
    private static PluginCore pluginCore;
    private Map<Location, Insertion> insertions;
    private ParticleEffectManager particleEffectManager;


    @Override
    public void onDisable() {
        // get plugin mode
        boolean mode = (Boolean) pluginCore.getMainSaveFile().get(MainSaveFile.Path.IS_WARP_MODE);
        if (mode) {
            // serlialize data for warp mode
            WarpSaveFile saveFile = new WarpSaveFile(pluginCore);

            Collection<Insertion> data = insertions.values();
            saveFile.setInsertions(data);
            saveFile.save();
        }

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
        //todo retrieve values

        if (mode) {
            // warp mode listeners/commands
            WarpSaveFile saveFile = new WarpSaveFile(pluginCore);

            for(Insertion insertion: saveFile.getInsertions()) {
                insertions.put(insertion.getLoc(), insertion);
            }

            WarpEventListener listener = new WarpEventListener(this);

            pluginCore.logToConsole("Successfully enabled warp mode: " + insertions.size() + " insertions loaded.");

        } else {
            // todo respawn mode
            new RespawnEventListener(this);

            pluginCore.logToConsole("Successfully enabled respawn mode");
        }

        particleEffectManager = new ParticleEffectManager(this);

        // check unsafe changes to config
        resolveUnsafeChanges(pluginCore, insertions);

        // register mod commands
        new ModCommandListener(pluginCore, this);

        // register listeners (this system is idiotic...)
        getServer().getPluginManager().registerEvents(this, this);

        // register listeners for both modes
        new AllModeListener(this);
    }

    public void resolveUnsafeChanges(PluginCore core, Map<Location, Insertion> insertions) {
        MainSaveFile main = core.getMainSaveFile();
        ConfigManager config = core.getConfigManager();

        // material change
        if (!config.get(ConfigValue.TAC_BLOCK).equals(main.get(MainSaveFile.Path.LAST_BLOCK_MAT))) {
            core.logToConsole("The block material has changed. Deleting all insertions.");
            ModCommandListener.deleteAllInsertions(insertions);
            main.set(MainSaveFile.Path.LAST_BLOCK_MAT, config.get(ConfigValue.TAC_BLOCK));
            main.save();
        }
    }

    @EventHandler
    public void onBlockDrop(PlayerDropItemEvent e) {
        if (!(boolean) pluginCore.getConfigManager().get(ConfigValue.ALLOW_TORCH_DROP)) {
            if (e.getItemDrop().getItemStack().equals(TacStackFactory.getTacStack())) {
                e.setCancelled(true);
                // we cancelled the drop, but we still want to get rid of the torch.
                e.getPlayer().getInventory().remove(TacStackFactory.getTacStack());
            }
        }
    }

    public static PluginCore getPluginCore() {
        return pluginCore;
    }

    public Map<Location, Insertion> getInsertions() {
        return insertions;
    }
}
