package com.ethanzeigler.tactical_insertions.universal;

import com.ethanzeigler.bukkit_plugin_utils.ConfigValue;
import com.ethanzeigler.bukkit_plugin_utils.language.LanguageManager;
import com.ethanzeigler.bukkit_plugin_utils.PluginCore;
import com.ethanzeigler.tactical_insertions.Insertion;
import com.ethanzeigler.tactical_insertions.TacticalInsertions;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by Ethan on 7/11/16.
 */
public class ModCommandListener implements CommandExecutor {
    private PluginCore pluginCore;
    private LanguageManager langManager;
    private TacticalInsertions plugin;

    public ModCommandListener(PluginCore pluginCore, TacticalInsertions plugin) {
        this.pluginCore = pluginCore;
        this.langManager = pluginCore.getLanguageManager();
        this.plugin = plugin;
    }

    /**
     * Executes the given command, returning its success
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    public void runClearInsertions(CommandSender sender, String[] args) {
        // todo add permissions for operations
        if (args.length == 0 || (args.length == 1 && !args[0].equalsIgnoreCase("abracadabra"))) {
            // no confirmation
            langManager.sendMessage(sender, ChatColor.BOLD.toString() + ChatColor.RED + "Warning: This is not reversible. " +
                    "Are you sure you want to do this? All tactical insertions will be deleted. To confirm, run this " +
                    "command again followed by " + ChatColor.YELLOW + "abracadabra" + ChatColor.RED + ".");
        } else {
            // confirmation
            langManager.sendMessage(sender, ChatColor.GOLD, "Cleared all tactical insertions. The plugin will now let " +
                    "you change the warp mode.");
            for (Insertion insert : plugin.getInsertions().values()) {
                plugin.getInsertions().remove(insert.getLoc());
                insert.getLoc().getBlock().setType(Material.AIR, true);
            }
        }
    }

    public void changeWarpMode(CommandSender sender) {
        if (plugin.getInsertions().size() == 0) {
            // no insertions in the world. Good to go.
            pluginCore.getConfigManager().set(ConfigValue.IS_WARP_MODE,
                    !((boolean) pluginCore.getConfigManager().get(ConfigValue.IS_WARP_MODE)));
            pluginCore.getMainSaveFile().set(MainSaveFile.Path.LAST_RUN_MODE,
                    ((boolean) pluginCore.getConfigManager().get(ConfigValue.IS_WARP_MODE) ? "WARP":"RESPAWN"));

            langManager.sendMessage(sender, ChatColor.GOLD, "Mode changed to " +
                    ((boolean) pluginCore.getConfigManager().get(ConfigValue.IS_WARP_MODE) ? "WARP":"RESPAWN"));

            // todo reload plugin
        } else {
            // there are still insertions in the world
            langManager.sendMessage(sender, ChatColor.RED, "There must be no insertions in the server to change modes. " +
                    "Run the command " + ChatColor.AQUA + "/ti clearinsertions " + ChatColor.RED + "to clear" +
                    " all insertions.");
        }
    }
}
