package com.ethanzeigler.tactical_insertions.universal;

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
}
