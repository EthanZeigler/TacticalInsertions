package com.ethanzeigler.tactical_insertions.warps;

import com.ethanzeigler.bukkit_plugin_utils.ConfigValue;
import com.ethanzeigler.bukkit_plugin_utils.language.LanguageManager;
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
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

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
    private Map<UUID, Insertion> waitingToNameMap = new ConcurrentHashMap<>();

    public WarpEventListener(TacticalInsertions plugin) {
        plugin.getCommand("tacwarp").setExecutor(this);
        plugin.getCommand("tacwarps").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        insertions = plugin.getInsertions();
        pluginCore = TacticalInsertions.getPluginCore();
        langManager = pluginCore.getLanguageManager();
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

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (TacStackFactory.isTacStack(e.getItemInHand())) {
            // is a tactical insertion
            if (e.getPlayer().hasPermission("tacticalinsertions.placeblock")) {
                if (!waitingToNameMap.containsKey(e.getPlayer().getUniqueId())) {
                    // is not waiting on naming another, name tac state
                    e.setCancelled(true);
                    // run validity check
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                        TacPositionValidity validity = TacPositionValidity.validate(e.getBlock().getLocation(),
                                (Integer) pluginCore.getConfigManager().get(ConfigValue.DISTANCE_FROM_TAC),
                                insertions.values(), waitingToNameMap.values());

                        switch (validity) {
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
                    // does not have the necessary permission
                    langManager.getAndSendMessage(e.getPlayer(), "block-place-denied");
                }
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
            langManager.sendMessage(sender, ChatColor.RED, langManager.getMessage(""));
        } else {
            switch (cmd.getName()) {
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


    private boolean isValidName(String name, UUID owner) {
        for (Insertion insertion : insertions.values()) {
            if (insertion.getName().equalsIgnoreCase(name) && insertion.getOwner().equals(owner)) {
                return false;
            }
        }
        return true;
    }
}
