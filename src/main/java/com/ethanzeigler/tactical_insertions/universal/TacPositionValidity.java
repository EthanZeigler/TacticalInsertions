package com.ethanzeigler.tactical_insertions.universal;

import com.ethanzeigler.tactical_insertions.Insertion;
import org.bukkit.Location;

import java.util.Collection;

/**
 * Represents a response to checking if a proposed tactical insertion's location is valid.
 */
public enum TacPositionValidity {
    VALID(true),
    TOO_CLOSE_TO_EXISTING(false),
    TOO_CLOSE_TO_PROPOSED(false);

    public boolean isValid;

    TacPositionValidity(boolean isValid) {
        this.isValid = isValid;
    }

    public static TacPositionValidity validate(Location loc, int requiredDistance, Collection<Insertion> insertions) {
        if (hasEnoughDistanceBetween(loc, requiredDistance, insertions)) {
            return VALID;
        } else {
            return TOO_CLOSE_TO_EXISTING;
        }
    }

    public static TacPositionValidity validate(Location loc, int requiredDistance, Collection<Insertion> existing,
                                               Collection<Insertion> proposed) {
        TacPositionValidity validity = validate(loc, requiredDistance, existing);
        if (validity == TOO_CLOSE_TO_EXISTING) {
            return TOO_CLOSE_TO_EXISTING;
        } else {
            validity = validate(loc, requiredDistance, proposed);
            if (validity == TOO_CLOSE_TO_EXISTING) {
                return TOO_CLOSE_TO_PROPOSED;
            }
        }

        return VALID;
    }

    public static double getDistanceBetween(Location loc1, Location loc2) {
        float dx = loc1.getBlockX() - loc2.getBlockX();
        float dy = loc1.getBlockY() - loc2.getBlockY();
        float dz = loc1.getBlockZ() - loc2.getBlockZ();

        // We should avoid Math.pow or Math.hypot due to perfomance reasons
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static boolean isEnoughDistanceBetween(Location loc1, Location loc2, int requiredDistance) {
        return getDistanceBetween(loc1, loc2) >= requiredDistance;
    }

    public static boolean hasEnoughDistanceBetween(Location loc, int requiredDistance, Collection<Insertion> insertions) {
        Location secondLoc;
        for (Insertion insertion : insertions) {
            secondLoc = insertion.getLoc();
            if (!isEnoughDistanceBetween(loc, secondLoc, requiredDistance)) {
                return false;
            }
        }
        // is enough distance
        return true;
    }

}
