package b5ccg.model.enums;

public enum Faction {
    HUMAN, MINBARI, CENTAURI, NARN, NEUTRAL, NON_ALIGNED, VORLON, ANY;

    /** Returns true if a card of this faction can be played by playerFaction. */
    public boolean isPlayableBy(Faction playerFaction) {
        return this == ANY || this == playerFaction
            || this == NEUTRAL || this == NON_ALIGNED;
    }
}
