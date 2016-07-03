package com.ethanzeigler.tactical_insertions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.ethanzeigler.bukkit_plugin_utils.ConfigValue;
import com.ethanzeigler.bukkit_plugin_utils.Language;
import com.ethanzeigler.bukkit_plugin_utils.PluginCore;

/**
 * Created by ethan on 6/28/16.
 */
public class TacticalInsertions extends JavaPlugin implements Listener, CommandExecutor {
    private static PluginCore pluginCore;
    private Map<Location, TacticalInsertion> insertions;
    //I have to use this as a map to save the location of where the insert is for use in PlayerChatEvent
    private Map<UUID, Location> tacNameWait = new HashMap<>();

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        // load plugin resources
        pluginCore = new PluginCore(this, false, Language.ENGLISH);

        // get plugin mode
        boolean mode = (boolean) pluginCore.getConfigManager().get(ConfigValue.IS_WARP_MODE);

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
    public void onBlockPlace(BlockPlaceEvent e){
    	if(e.getItemInHand().equals(TorchStackFactory.getTorchStack())){
    		Player player = e.getPlayer();
    		//Not sure if multiple are allowed to be placed.
    		for(TacticalInsertion insert : insertions.values()){
    			if(player.getUniqueId().equals(insert.getOwner())){
    				player.sendMessage(ChatColor.RED + "You have already placed a Tactical Insertion.");
    				return;
    			}
    		}
    		tacNameWait.put(player.getUniqueId(), e.getBlock().getLocation());
    		player.sendMessage(ChatColor.GOLD + "Type in chat the name of your tactical insert.");
    	}
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e){
    	if(tacNameWait.containsKey(e.getPlayer().getUniqueId())){
    		String name = e.getMessage();
    		Location loc = tacNameWait.get(e.getPlayer().getUniqueId());
    		insertions.put(loc, new TacticalInsertion(loc, name, e.getPlayer().getUniqueId()));
    		e.getPlayer().sendMessage(ChatColor.GOLD + "You have successfully placed a tactical insert called '" + ChatColor.GRAY + name + ChatColor.GOLD + "'");
    		//Now would be the time to activate particles to indicate the tac insert has been "enabled"
    	}
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        for (TacticalInsertion insert : insertions.values()) {
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

    public Map<Location, TacticalInsertion> getInsertions() {
        return insertions;
    }
}
