package com.ethanzeigler.tactical_insertions.respawns;

import com.ethanzeigler.tactical_insertions.Insertion;
import com.ethanzeigler.tactical_insertions.TacticalInsertions;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Map;

/**
 * Created by ethan on 6/30/16.
 */
public class RespawnEventListener {
    private Map<Location, Insertion> insertions;
    private TacticalInsertions plugin;

    public RespawnEventListener(Map<Location, Insertion> insertions, TacticalInsertions plugin) {
        this.insertions = insertions;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        for (Insertion insert : insertions.values()) {
            if (insert.getOwner().equals(e.getPlayer().getUniqueId())) {
                e.setRespawnLocation(insert.getLoc());
                // disable the insertion, then break the tac in a second.
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> insertions.remove(insert), 0);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    Block block = insert.getLoc().getBlock();
                    block.getDrops().clear();
                }, 20);
            }
        }
    }
}

