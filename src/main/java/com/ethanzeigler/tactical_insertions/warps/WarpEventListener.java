package com.ethanzeigler.tactical_insertions.warps;

import com.ethanzeigler.bukkit_plugin_utils.LanguageManager;
import com.ethanzeigler.bukkit_plugin_utils.PluginCore;
import com.ethanzeigler.tactical_insertions.TacticalInsertion;
import com.ethanzeigler.tactical_insertions.TacticalInsertions;
import com.ethanzeigler.tactical_insertions.TorchStackFactory;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.material.Torch;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

/**
 * Created by ethan on 6/30/16.
 */
public class WarpEventListener implements Listener, CommandExecutor {
    private TacticalInsertions plugin;
    private Map<Location, TacticalInsertion> insertions;
    private PluginCore pluginCore;
    private LanguageManager langManager;

    public WarpEventListener(TacticalInsertions plugin) {
        this.plugin = plugin;
        insertions = plugin.getInsertions();
        pluginCore = TacticalInsertions.getPluginCore();
        langManager = pluginCore.getLanguageManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {

    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            langManager.sendAndFormatMessage(sender, ChatColor.RED, "Sorry, that's for players.");
        }
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

    private void listWarps(Player sender) {

    }

    private void goToWarp(Player sender, String[] args) {

    }

    private void getTacBlock(Player sender) {
        if (sender.hasPermission("tacticalinsertions.getblock")) {
            // bukkit has already checked for perm node
            Player player = (Player) sender;
            // sender is a player
            if (player.getInventory().contains(TorchStackFactory.getTorchStack())) {
                langManager.sendAndFormatMessage(
                        sender, ChatColor.RED, "You already have a Tactical Insertion in your inventory.");
            } else {
                sender.getInventory().addItem(TorchStackFactory.getTorchStack());
                langManager.sendAndFormatMessage(sender, ChatColor.GOLD, "You got a Tactical Insertion!");
            }
        }
    }
}
