package com.ethanzeigler.tactical_insertions.universal;

import com.ethanzeigler.tactical_insertions.Insertion;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Created by Ethan on 7/15/16.
 */
public interface ModeEventListener {
    /**
     * Called when an insertion is broken.
     * @return whether or not to cancel the event
     */
    public boolean onInsertionBreak(Block block, Insertion insertion, Player player);

    /**
     * Called when an insertion is placed.
     * @param block the block that broke
     * @param player the player that broke the block
     * @return whether or not to
     */
    public boolean onInsertionPlace(Block block, Player player);

    /**
     * Checks if any special properties of a mode would deny the position of a
     * tactical insertions for any reason. This includes respawn mode's
     * "one insertion per person" check and warp mode's "waiting to name" insertions
     * @param location The location of the proposed insertion
     * @return the position's validity
     */
    public TacPositionValidity validateSpecialProperties(Location location);
}
