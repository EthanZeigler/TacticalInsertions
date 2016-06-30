package com.ethanzeigler.tactical_insertions;

import com.ethanzeigler.bukkit_plugin_utils.ConfigManager;
import com.ethanzeigler.bukkit_plugin_utils.ConfigValue;
import com.ethanzeigler.bukkit_plugin_utils.Language;
import com.ethanzeigler.bukkit_plugin_utils.PluginCore;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Created by ethan on 6/28/16.
 */
public class TacticalInsertions extends JavaPlugin implements Listener, CommandExecutor {
    private static PluginCore pluginCore;
    private List<TacticalInsertion> insertions;

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        // load plugin resources
        pluginCore = new PluginCore(this, false, Language.ENGLISH);

        // register command
        getCommand("gettac").setExecutor(this);

        // register listeners (this system is idiotic...)
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        // bukkit has already checked for perm node
        if (sender instanceof Player) {
            Player player = (Player) sender;
            // sender is a player
            if (player.getInventory().contains(TorchStackFactory.getTorchStack()))
                player.sendMessage(ChatColor.RED + "You already have a Tactical Insertion in your inventory.");
        } else {
            sender.sendMessage("This is for players only");
        }
        return true;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

    }

    @EventHandler
    public void onBlockDrop(PlayerDropItemEvent e) {
        if ((Boolean) pluginCore.getConfigManager().get(ConfigValue.ALLOW_TORCH_DROP)) {
            if (e.getItemDrop().getItemStack().equals(TorchStackFactory.getTorchStack())) {
                e.setCancelled(true);
                // we cancelled the drop, but we still want to get rid of the torch.
                e.getPlayer().getInventory().remove(TorchStackFactory.getTorchStack());
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        for (TacticalInsertion insert : insertions) {
            if (insert.getOwner().equals(e.getPlayer().getUniqueId())) {
                e.setRespawnLocation(insert.getLoc());
                // disable the insertion, then break the tac in a second.
                getServer().getScheduler().runTaskLater(this, () -> insertions.remove(insert), 0);
                getServer().getScheduler().runTaskLater(this, () -> {
                    Block block = insert.getLoc().getBlock();
                    block.getDrops().clear();
                }, 20);

            }
        }
    }

    public static PluginCore getPluginCore() {
        return pluginCore;
    }
}
