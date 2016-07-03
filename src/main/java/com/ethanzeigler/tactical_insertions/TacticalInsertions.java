package com.ethanzeigler.tactical_insertions;

import com.ethanzeigler.bukkit_plugin_utils.ConfigValue;
import com.ethanzeigler.bukkit_plugin_utils.Language;
import com.ethanzeigler.bukkit_plugin_utils.PluginCore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ethan on 6/28/16.
 */
public class TacticalInsertions extends JavaPlugin implements Listener, CommandExecutor {
    private static PluginCore pluginCore;
    private Map<Location, TacticalInsertion> insertions;


    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        // load plugin resources
        pluginCore = new PluginCore(this, false, Language.ENGLISH);

        // get plugin mode
        boolean mode = (Boolean) pluginCore.getConfigManager().get(ConfigValue.IS_WARP_MODE);

        // register command
        getCommand("gettac").setExecutor(this);

        // register listeners (this system is idiotic...)
        getServer().getPluginManager().registerEvents(this, this);

        insertions = new ConcurrentHashMap<>();
        //todo retrieve values

    }

    @EventHandler
    public void onBlockDrop(PlayerDropItemEvent e) {
        if ((Boolean) pluginCore.getConfigManager().get(ConfigValue.ALLOW_TORCH_DROP)) {
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

    public Map<Location, TacticalInsertion> getInsertions() {
        return insertions;
    }
}
