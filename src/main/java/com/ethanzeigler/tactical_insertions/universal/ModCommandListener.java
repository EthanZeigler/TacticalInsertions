package com.ethanzeigler.tactical_insertions.universal;

import com.ethanzeigler.bukkit_plugin_utils.ConfigValue;
import com.ethanzeigler.bukkit_plugin_utils.language.LanguageManager;
import com.ethanzeigler.bukkit_plugin_utils.PluginCore;
import com.ethanzeigler.tactical_insertions.Insertion;
import com.ethanzeigler.tactical_insertions.TacticalInsertions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Map;

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

        plugin.getCommand("tacinserts").setExecutor(this);
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
        if (!sender.hasPermission("tacticalinsertions.moderator")) {
            langManager.getAndSendMessage(sender, "not-moderator-denied");
            return true;
        } else if (args.length < 1) {
            langManager.getAndSendMessage(sender, "mod-commands-subcommand-warning");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "clearinserts":
                runClearInsertions(sender, args);
                break;
            case "changemode":
                changeWarpMode(sender);
                break;
            case "version":
                printVersion(sender);
                break;

            default:
                langManager.getAndSendMessage(sender, "mod-commands-subcommand-warning");
        }
        return true;
    }

    public void runClearInsertions(CommandSender sender, String[] args) {
        // todo add permissions for operations
        if (args.length == 0 || (args.length == 1 && !args[0].equalsIgnoreCase("abracadabra"))) {
            // no confirmation
            langManager.sendMessage(sender, ChatColor.RED, "clear-insertions-warning");
        } else {
            // confirmation
            langManager.getAndSendMessage(sender, "all-insertions-cleared");
            deleteAllInsertions(plugin.getInsertions());
        }
    }

    public void changeWarpMode(CommandSender sender) {
        if (plugin.getInsertions().size() == 0) {
            // no insertions in the world. Good to go

            boolean oldMode = (boolean) pluginCore.getMainSaveFile().get(MainSaveFile.Path.IS_WARP_MODE);
            pluginCore.getMainSaveFile().set(MainSaveFile.Path.IS_WARP_MODE, !oldMode);

            langManager.sendMessage(sender, ChatColor.GOLD, langManager.getMessage("successful-mode-swap") +
                    (!oldMode ? langManager.getMessage("warp") : langManager.getMessage("respawn")));

            langManager.getAndSendMessage(sender, "shutdown-warning-after-mode-switch");

            Bukkit.getServer().getPluginManager().disablePlugin(plugin);

        } else {
            // there are still insertions in the world
            langManager.getAndSendMessage(sender, "mode-change-denied-still-warps");
        }
    }

    public static void deleteAllInsertions(Map<Location, Insertion> insertions) {
        for (Insertion insert : insertions.values()) {
            insertions.remove(insert.getLoc());
            insert.getLoc().getBlock().setType(Material.AIR, true);
        }
    }

    public void printVersion(CommandSender sender) {
        langManager.sendMessage(sender, ChatColor.GOLD, langManager.getMessage("version") + ": " +
                plugin.getDescription().getVersion());
    }
}
