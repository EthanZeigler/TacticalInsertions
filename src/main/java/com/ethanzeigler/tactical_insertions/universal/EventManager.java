package com.ethanzeigler.tactical_insertions.universal;

import com.ethanzeigler.bukkit_plugin_utils.ConfigValue;
import com.ethanzeigler.bukkit_plugin_utils.PluginCore;
import com.ethanzeigler.bukkit_plugin_utils.language.LanguageManager;
import com.ethanzeigler.tactical_insertions.Insertion;
import com.ethanzeigler.tactical_insertions.TacStackFactory;
import com.ethanzeigler.tactical_insertions.warps.WarpEventListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Ethan on 7/15/16.
 */
public class EventManager implements Listener {
    private Map<Location, Insertion> insertions;
    private LanguageManager language;
    private ModeEventListener modeEventListener;
    private PluginCore pluginCore;

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        if (e.getBlock().getType().equals(TacStackFactory.getSeedMaterial())) {
            Insertion insert = insertions.get(e.getBlock().getLocation());

            if (insert != null) {
                if (modeEventListener.onInsertionBreak(e.getBlock(), insert, e.getPlayer())) {
                    // todo this may cause concurrent mod issues. Investigate.
                    e.getBlock().getDrops().clear();
                    insertions.remove(e.getBlock().getLocation());
                    language.sendMessage(e.getPlayer(), ChatColor.GOLD, "The tactical insertion has been smashed.");
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getItemInHand().equals(TacStackFactory.getTacStack())) {
            modeEventListener.onInsertionPlace(e.getBlock(), e.getPlayer());
        }
    }

    /**
     * Checks if the given location is a valid location for a new tactical.
     * This function should be called asynchronously.
     *
     * @param loc the proposed location
     * @return the validity of the proposed location
     */
    private TacPositionValidity validatePosition(Location loc) {
        // check if there isn't a conflicting tactical

        // check all existing tacticals

        TacPositionValidity secondaryValidity;
        if (!hasEnoughDistanceBetween(loc, insertions.values())) {
            return TacPositionValidity.TOO_CLOSE_TO_EXISTING;
        } else if ((secondaryValidity = modeEventListener.validateSpecialProperties(loc)) != TacPositionValidity.VALID) {
            return secondaryValidity;
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

    public boolean isEnoughDistanceBetween(Location loc1, Location loc2) {
        return getDistanceBetween(loc1, loc2) >=
                (int) pluginCore.getConfigManager().get(ConfigValue.DISTANCE_FROM_TAC);
    }


    public static double getDistanceBetween(Location loc1, Location loc2) {
        float dx = loc1.getBlockX() - loc2.getBlockX();
        float dy = loc1.getBlockY() - loc2.getBlockY();
        float dz = loc1.getBlockZ() - loc2.getBlockZ();

        // We should avoid Math.pow or Math.hypot due to perfomance reasons
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
