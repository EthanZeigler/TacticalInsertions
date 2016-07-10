package com.ethanzeigler.tactical_insertions.warps;

import com.ethanzeigler.bukkit_plugin_utils.ConfigValue;
import com.ethanzeigler.bukkit_plugin_utils.LanguageManager;
import com.ethanzeigler.bukkit_plugin_utils.PluginCore;
import com.ethanzeigler.tactical_insertions.Insertion;
import com.ethanzeigler.tactical_insertions.TacticalInsertions;
import com.ethanzeigler.tactical_insertions.TacStackFactory;
import com.ethanzeigler.tactical_insertions.universal.TacPositionValidity;
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
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ethan on 6/30/16.
 */
public class WarpEventListener implements Listener, CommandExecutor {
    private TacticalInsertions plugin;
    private Map<Location, Insertion> insertions;
    private PluginCore pluginCore;
    private LanguageManager langManager;
    private Material tacMaterial;
    private Map<UUID, Insertion> waitingToNameMap = new ConcurrentHashMap<>();

    public WarpEventListener(TacticalInsertions plugin) {
        plugin.getCommand("gettac").setExecutor(this);
        plugin.getCommand("tacwarp").setExecutor(this);
        plugin.getCommand("tacwarps").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        insertions = plugin.getInsertions();
        pluginCore = TacticalInsertions.getPluginCore();
        langManager = pluginCore.getLanguageManager();
        tacMaterial = (Material) pluginCore.getConfigManager().get(ConfigValue.TAC_BLOCK);
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent e) {
        // if the player is waiting for a tac to be placed, forget it.
        if (waitingToNameMap.containsKey(e.getPlayer().getUniqueId())) {
            waitingToNameMap.remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (waitingToNameMap.containsKey(uuid)) {
            e.setCancelled(true);
            String[] words = e.getMessage().split(" ");
            if (words.length == 1) {
                if (isValidName(words[0], uuid)) {
                    // the formatting is correct. One word. Add waiting tac to active list and change it's name
                    Insertion insertion = waitingToNameMap.get(uuid);
                    insertion.setName(words[0].toLowerCase());

                    insertions.put(waitingToNameMap.get(uuid).getLoc(), waitingToNameMap.get(uuid));
                    // make block
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        insertion.getLoc().getBlock().setType((Material) pluginCore.getConfigManager().get(ConfigValue.TAC_BLOCK));
                    });
                    // clear from waiting name
                    waitingToNameMap.remove(e.getPlayer().getUniqueId());
                    // send confirm message (sync, this is an async event)
                    langManager.sendSyncMessage(e.getPlayer(), ChatColor.GOLD, String.format("Your warp %s%s%s is set.",
                            ChatColor.AQUA, insertion.getName(), ChatColor.GOLD));
                } else {
                    // name already used by player
                    langManager.sendSyncMessage(e.getPlayer(), ChatColor.RED, "You already have a" +
                            " tactical insertion named " + ChatColor.WHITE + words[0]);
                }
            } else {
                // multiple words. Incorrect formatting (send sync, in async)
                    langManager.sendSyncMessage(e.getPlayer(), ChatColor.RED, "The tactical insertion's name must" +
                            " be one word.");
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType().equals(tacMaterial)) {
            if (insertions.get(e.getBlock().getLocation()) != null) {
                // todo this may cause concurrent mod issues. Investigate.
                e.getBlock().getDrops().clear();
                insertions.remove(e.getBlock().getLocation());
                langManager.sendMessage(e.getPlayer(), ChatColor.GOLD, "The tactical insertion has been smashed.");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getItemInHand().equals(TacStackFactory.getTacStack())) {
            // is a tactical insertion
            if (!waitingToNameMap.containsKey(e.getPlayer().getUniqueId())) {
                // is not waiting on naming another, name tac state
                e.setCancelled(true);
                // run validity check
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    TacPositionValidity validity = validatePosition(e.getBlock().getLocation());

                    switch(validity) {
                        case VALID:
                            waitingToNameMap.put(e.getPlayer().getUniqueId(), new Insertion(
                                    e.getBlock().getLocation(), null, e.getPlayer().getUniqueId()));
                            langManager.sendSyncMessage(e.getPlayer(), ChatColor.GOLD, "That spot's fine. Chat the name you want to give to the tactical insertion.");

                            // remove from inventory synchronously
                            plugin.getServer().getScheduler().runTask(plugin, () ->
                                    e.getPlayer().getInventory().remove(TacStackFactory.getTacStack()));
                            break;

                        case TOO_CLOSE_TO_EXISTING:
                            langManager.sendSyncMessage(e.getPlayer(), ChatColor.RED, "That's too close to another tactical insertion");
                            break;

                        case TOO_CLOSE_TO_PROPOSED:
                            langManager.sendSyncMessage(e.getPlayer(), ChatColor.RED, "That's too close to another tactical insertion someone is currently naming");
                            break;

                    }
                });
            } else {
                // is waiting on naming another
                e.setCancelled(true);
                langManager.sendMessage(e.getPlayer(), ChatColor.RED, "You must name another tac by chatting it first.");
            }
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            langManager.sendMessage(sender, ChatColor.RED, "Sorry, that's for players.");
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

            // combine tacs
            for (Insertion insertion : insertions.values()) {
                if (insertion.getOwner().equals(sender.getUniqueId())) {
                    sb.append(insertion.getName() + ", ");
                }
            }

            // send message
            if (sb.length() > 0) {
                // found entries
                langManager.sendSyncMessage(
                        sender, ChatColor.GOLD, sb.toString().substring(0, sb.toString().length() - 2));

            } else {
                langManager.sendSyncMessage(sender, ChatColor.GOLD, "You don't have any tacs. " +
                        "You never placed one or they were smashed by another player.");
            }

        });
    }

    private void goToWarp(Player sender, String[] args) {
        if (args.length != 1) {
            // incorrect syntax. Args must lenght 1
            langManager.sendMessage(sender, ChatColor.RED, "Incorrect usage: /tacwarp <name>");
        } else {
            // search for warp from that player with the same name async
            plugin.getServer().getScheduler().runTaskAsynchronously(
                    plugin, () -> {
                        for (Insertion insertion : insertions.values()) {
                            if (insertion.getOwner().equals(sender.getUniqueId()) &&
                                    insertion.getName().equalsIgnoreCase(args[0])) {
                                // warp
                                plugin.getServer().getScheduler().runTask(plugin, () -> {
                                    sender.teleport(insertion.getLoc().clone().add(.5, 1, .5));
                                    langManager.sendMessage(
                                            sender, ChatColor.GOLD, "You warped to " + insertion.getName().toLowerCase());
                                });

                                return;
                            }
                        }

                        langManager.sendSyncMessage(sender, ChatColor.RED,
                                "You don't have a warp named " + args[0].toLowerCase());
                    });
        }
    }

    private void getTacBlock(Player sender) {
        if (sender.hasPermission("tacticalinsertions.getblock")) {
            // bukkit has already checked for perm node
            Player player = (Player) sender;
            // sender is a player
            if (player.getInventory().contains(TacStackFactory.getTacStack())) {
                langManager.sendMessage(
                        sender, ChatColor.RED, "You already have a Tactical Insertion in your inventory.");
            } else {
                sender.getInventory().addItem(TacStackFactory.getTacStack());
                langManager.sendMessage(sender, ChatColor.GOLD, "You got a Tactical Insertion!");
            }
        }
    }

    /**
     * Checks if the given location is a valid location for a new tactical.
     * This function should be called asynchronously.
     * @param loc the proposed location
     * @return the validity of the proposed location
     */
    private TacPositionValidity validatePosition(Location loc) {
        // check if there isn't a conflicting tactical

        // check all existing tacticals
        if (!hasEnoughDistanceBetween(loc, insertions.values())) {
            return TacPositionValidity.TOO_CLOSE_TO_EXISTING;
        } else if (!hasEnoughDistanceBetween(loc, waitingToNameMap.values())) {
            // check proposed tacticals
            return TacPositionValidity.TOO_CLOSE_TO_PROPOSED;
        }

        return TacPositionValidity.VALID;
    }

    private boolean hasEnoughDistanceBetween(Location loc, Collection<Insertion> insertions) {
        Location secondLoc;
        for (Insertion insertion : insertions) {
            secondLoc = insertion.getLoc();
            if (!isEnoughDistanceBetween(loc, secondLoc)) {
                return false;
            }
        }
        // is enough distance

        return true;
    }

    private boolean isEnoughDistanceBetween(Location loc1, Location loc2) {
        return getDistanceBetween(loc1, loc2) >=
                (int) pluginCore.getConfigManager().get(ConfigValue.DISTANCE_FROM_TAC);
    }

    private boolean isValidName(String name, UUID owner) {
        for (Insertion insertion: insertions.values()) {
            if (insertion.getName().equalsIgnoreCase(name) && insertion.getOwner().equals(owner)) {
                return false;
            }
        }
        return true;
    }

    public static double getDistanceBetween(Location loc1, Location loc2) {
        float dx = loc1.getBlockX() - loc2.getBlockX();
        float dy = loc1.getBlockY() - loc2.getBlockY();
        float dz = loc1.getBlockZ() - loc2.getBlockZ();

        // We should avoid Math.pow or Math.hypot due to perfomance reasons
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
