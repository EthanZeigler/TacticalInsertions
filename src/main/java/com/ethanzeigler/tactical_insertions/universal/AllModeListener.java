package com.ethanzeigler.tactical_insertions.universal;

import com.ethanzeigler.bukkit_plugin_utils.PluginCore;
import com.ethanzeigler.bukkit_plugin_utils.language.LanguageManager;
import com.ethanzeigler.tactical_insertions.Insertion;
import com.ethanzeigler.tactical_insertions.TacStackFactory;
import com.ethanzeigler.tactical_insertions.TacticalInsertions;
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
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Created by Ethan on 7/24/16.
 */
public class AllModeListener implements Listener, CommandExecutor {
    private PluginCore pluginCore;
    private LanguageManager lang;
    private TacticalInsertions plugin;
    private Map<Location, Insertion> insertions;

    public AllModeListener(TacticalInsertions plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.pluginCore = TacticalInsertions.getPluginCore();
        this.insertions = plugin.getInsertions();
        this.lang = pluginCore.getLanguageManager();

        plugin.getCommand("gettac").setExecutor(this);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType().equals(TacStackFactory.getSeedMaterial())) {
            if (insertions.get(e.getBlock().getLocation()) != null) {
                // todo this may cause concurrent mod issues. Investigate.
                e.getBlock().setType(Material.AIR, false);
                insertions.remove(e.getBlock().getLocation());
                lang.sendMessage(e.getPlayer(), ChatColor.GOLD, lang.getMessage("insertion-smashed-to-smasher"));
            }
        }
    }

    @EventHandler
    public void preventCraft(CraftItemEvent e) {
        ItemStack[] stacks = e.getInventory().getStorageContents();

        for (ItemStack stack : stacks) {
            if (TacStackFactory.isTacStack(stack)) {
                e.setCancelled(true);
                lang.getAndSendMessage(e.getWhoClicked(), "cant-craft-using-insertions");
            }
        }
    }

    /**
     * Executes the given command, returning its success
     *
     * @param sender Source of the command
     * @param cmd    Command which was executed
     * @param label  Alias of the command which was used
     * @param args   Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            lang.getAndSendMessage(sender, "player-only-warning");
        } else {
            switch (cmd.getName()) {
                case "gettac":
                    getTacBlock((Player) sender);
                    break;
            }
        }
        return true;
    }

    private void getTacBlock(Player sender) {
        if (sender.hasPermission("tacticalinsertions.getblock")) {
            // bukkit has already checked for perm node
            Player player = (Player) sender;
            // sender is a player
            if (TacStackFactory.containsTacStack(player.getInventory())) {
                lang.getAndSendMessage(sender, "already-have-insertion-in-inventory");
            } else {
                sender.getInventory().addItem(TacStackFactory.getTacStack());
                lang.sendMessage(sender, "given-insertion");
            }
        }
    }

    /*@EventHandler
    public void onPhysics(BlockPistonEvent e) {
        BlockFace dir = e.getDirection();
        Vector vector = new Vector(dir.getModX(), dir.getModY(), dir.getModZ());
        BlockIterator blockIterator = new BlockIterator(e.getBlock().getWorld(),
                e.getBlock().getLocation(), )
    }*/
}
