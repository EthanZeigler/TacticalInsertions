package com.ethanzeigler.tactical_insertions.warps;

import com.ethanzeigler.bukkit_plugin_utils.ConfigValue;
import com.ethanzeigler.bukkit_plugin_utils.LanguageManager;
import com.ethanzeigler.bukkit_plugin_utils.PluginCore;
import com.ethanzeigler.tactical_insertions.TacticalInsertion;
import com.ethanzeigler.tactical_insertions.TacticalInsertions;
import com.ethanzeigler.tactical_insertions.TacStackFactory;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ethan on 6/30/16.
 */
public class WarpEventListener implements Listener, CommandExecutor {
    private TacticalInsertions plugin;
    private Map<Location, TacticalInsertion> insertions;
    private PluginCore pluginCore;
    private LanguageManager langManager;
    private Material tacMaterial;
    private Map<UUID, Location> waitingToNameMap = new HashMap<>();

    public WarpEventListener(TacticalInsertions plugin) {
        this.plugin = plugin;
        insertions = plugin.getInsertions();
        pluginCore = TacticalInsertions.getPluginCore();
        langManager = pluginCore.getLanguageManager();
        tacMaterial = (Material) pluginCore.getConfigManager().get(ConfigValue.TAC_BLOCK);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (waitingToNameMap.containsKey(e.getPlayer().getUniqueId())) {
            Location loc = waitingToNameMap.get(e.getPlayer().getUniqueId());
            String name = e.getMessage().toLowerCase().split(" ")[0];
            insertions.put(waitingToNameMap.get(e.getPlayer().getUniqueId()), new TacticalInsertion(loc, name, e.getPlayer().getUniqueId()));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType().equals(tacMaterial)) {
            if (insertions.get(e.getBlock().getLocation()) != null) {
                // todo this may cause concurrent mod issues. Investigate.
                insertions.remove(e.getBlock().getLocation());
                langManager.sendAndFormatMessage(e.getPlayer(), ChatColor.GOLD, "The tactical insertion has been smashed.");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getBlock().equals(TacStackFactory.getTacStack())) {
            if (!waitingToNameMap.containsKey(e.getPlayer().getUniqueId())) { // is not waiting on naming another
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    for (TacticalInsertion insertion : insertions.values()) {
                        Location loc1 = insertion.getLoc();
                        Location loc2 = e.getBlock().getLocation();

                        if (getDistance(loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ(),
                                loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ()) <
                                (int) pluginCore.getConfigManager().get(ConfigValue.DISTANCE_FROM_TAC)) {
                            // tac is too close to another
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                langManager.sendAndFormatMessage(e.getPlayer(), ChatColor.RED, "That's too close to another tactical insertion");
                                loc2.getBlock().setType(Material.AIR);
                                e.getPlayer().getInventory().addItem(TacStackFactory.getTacStack());
                                return;
                            });
                        }
                    }
                });
               waitingToNameMap.put(e.getPlayer().getUniqueId(), e.getBlock().getLocation());
            } else {
                // is waiting on naming another
                e.setCancelled(true);
                langManager.sendAndFormatMessage(e.getPlayer(), ChatColor.RED, "You must name another tac by chatting it first.");
            }
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            langManager.sendAndFormatMessage(sender, ChatColor.RED, "Sorry, that's for players.");
        } else {
            switch (cmd.getName()) {
                case "gettac":
                    getTacBlock((Player) sender);
                    break;

                case "tacwarp":
                    goToWarp((Player) sender, args);
                    break;

                case "tacwarps":
                    listWarps((Player) sender);
                    break;
            }
        }
        return true;
    }

    private void listWarps(Player sender) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            StringBuilder sb = new StringBuilder();
            for (TacticalInsertion insertion : insertions.values()) {
                if (insertion.getOwner().equals(sender.getUniqueId())) {
                    sb.append(insertion.getName() + "\n");
                }
            }
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                langManager.sendAndFormatMessage(
                        sender, ChatColor.GOLD,sb.toString().substring(0, sb.toString().length() - 2));
            });
        });
    }

    private void goToWarp(Player sender, String[] args) {
        if (args.length != 1) {
            // incorrect syntax. Args must le lenght 1
            langManager.sendAndFormatMessage(sender, ChatColor.RED, "Incorrect usage: /tacwarp <name>");
        } else {
            // search for warp from that player with the same name async
            plugin.getServer().getScheduler().runTaskAsynchronously(
                    plugin, () -> insertions.values().stream().filter(
                            insertion -> insertion.getOwner().equals(sender.getUniqueId()) &&
                                    insertion.getName().toLowerCase().equals(args[0].toLowerCase())).forEach(insertion -> { // loop matches (will be only one)
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            sender.teleport(insertion.getLoc()); // warped player
                            langManager.sendAndFormatMessage( //send message
                                    sender, ChatColor.GOLD, "You warped to " + insertion.getName().toLowerCase());
                        });
                    }));
        }
    }

    private void getTacBlock(Player sender) {
        if (sender.hasPermission("tacticalinsertions.getblock")) {
            // bukkit has already checked for perm node
            Player player = (Player) sender;
            // sender is a player
            if (player.getInventory().contains(TacStackFactory.getTacStack())) {
                langManager.sendAndFormatMessage(
                        sender, ChatColor.RED, "You already have a Tactical Insertion in your inventory.");
            } else {
                sender.getInventory().addItem(TacStackFactory.getTacStack());
                langManager.sendAndFormatMessage(sender, ChatColor.GOLD, "You got a Tactical Insertion!");
            }
        }
    }

    public static double getDistance(float x1, float y1, float z1, float x2, float y2, float z2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        float dz = z1 - z2;

        // We should avoid Math.pow or Math.hypot due to perfomance reasons
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
