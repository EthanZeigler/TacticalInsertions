package com.ethanzeigler.tactical_insertions.respawns;

import com.ethanzeigler.bukkit_plugin_utils.PluginCore;
import com.ethanzeigler.bukkit_plugin_utils.language.LanguageManager;
import com.ethanzeigler.tactical_insertions.Insertion;
import com.ethanzeigler.tactical_insertions.TacStackFactory;
import com.ethanzeigler.tactical_insertions.TacticalInsertions;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Map;

/**
 * Created by ethan on 6/30/16.
 */
public class RespawnEventListener implements Listener {
    private Map<Location, Insertion> insertions;
    private PluginCore pluginCore;
    private TacticalInsertions plugin;
    private LanguageManager lang;

    public RespawnEventListener(TacticalInsertions plugin) {
        this.plugin = plugin;
        this.insertions = plugin.getInsertions();
        this.pluginCore = TacticalInsertions.getPluginCore();
        this.lang = pluginCore.getLanguageManager();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        for (Insertion insert : insertions.values()) {
            if (insert.getOwner().equals(e.getPlayer().getUniqueId())) {
                e.setRespawnLocation(insert.getLoc());
                // disable the insertion, then break the tac in a second.
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> insertions.remove(insert.getLoc()), 0);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    Block block = insert.getLoc().getBlock();
                    block.setType(Material.AIR, false);
                }, 20);
            }
        }
    }

    @EventHandler
    public void onInsertionPlace(BlockPlaceEvent e) {
        if (TacStackFactory.isTacStack(e.getItemInHand())) {
            if (e.getPlayer().hasPermission("tacticalinsertions.placeblock")) {
                if (!playerHasInsertion(e.getPlayer())) {
                    insertions.put(e.getBlock().getLocation(), new Insertion(e.getBlock().getLocation(), null, e.getPlayer().getUniqueId()));
                    lang.getAndSendMessage(e.getPlayer(), "insertion-placed");
                } else {
                    lang.getAndSendMessage(e.getPlayer(), "already-have-insertion");
                    e.setCancelled(true);
                }
            } else {
                lang.getAndSendMessage(e.getPlayer(), "block-place-denied");
            }
        }
    }

    private boolean playerHasInsertion(Player player) {
        for (Insertion insertion : insertions.values()) {
            if (insertion.getOwner().equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }
}

