package com.ethanzeigler.tactical_insertions;

import org.bukkit.Location;

import java.util.UUID;

/**
 * Created by ethan on 6/28/16.
 */
public class TacticalInsertion {
    private Location loc;
    private String name;
    private UUID owner;

    public TacticalInsertion(Location loc, String name, UUID owner) {
        this.loc = loc;
        this.name = name;
        this.owner = owner;
    }

    /**
     * Returns whether or not the tac is at the given location
     * @param loc matching location
     * @return whether or not the tac matches that location
     */
    public boolean isAtLocation(Location loc) {
        return loc.equals(loc);
    }

    /**
     * Gets the owner of the tac
     * @return the owner's UUID
     */
    public UUID getOwner() {
        return owner;
    }

    public Location getLoc() {
        return loc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
