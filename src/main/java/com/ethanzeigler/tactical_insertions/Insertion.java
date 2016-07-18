package com.ethanzeigler.tactical_insertions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by ethan on 6/28/16.
 */
@SerializableAs("Insertion")
public class Insertion implements ConfigurationSerializable {
    private Location loc;
    private String name;
    private UUID owner;

    public Insertion(Location loc, String name, UUID owner) {
        this.loc = loc;
        this.name = name;
        this.owner = owner;
    }

    public Insertion(Map<String, Object> data) {
        loc = new Location(Bukkit.getWorld((String) data.get("world")),
                ((Integer)data.get("x")).doubleValue(), ((Integer)data.get("y")).doubleValue(),
                ((Integer) data.get("z")).doubleValue());

        name = (String) data.get("name");
        owner = UUID.fromString((String) data.get("owner"));
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

    /**
     * Creates a Map representation of this class.
     * <p>
     * This class must provide a method to restore this class, as defined in
     * the {@link ConfigurationSerializable} interface javadocs.
     *
     * @return Map containing the current state of this class
     */
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("x", loc.getBlockX());
        data.put("y", loc.getBlockY());
        data.put("z", loc.getBlockZ());
        data.put("world", loc.getWorld().getName());
        data.put("name", name);
        data.put("owner", owner.toString());

        return data;
    }
}
