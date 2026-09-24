package b5ccg.model;

import b5ccg.model.enums.*;

public class LocationCard extends Card {
    private final int influencePerRound;
    private final int military;
    private Player capturedBy; // B5-0376 Phase A stub
    private boolean effectsSuppressed; // B5-0376 Phase B: true while captured

    /** B5-0376 Phase A: record captured-by owner. Stub — no gameplay effect yet. */
    public Player getCapturedBy() { return capturedBy; }
    public void setCapturedBy(Player owner) { this.capturedBy = owner; }

    /** B5-0376 Phase B: returns true when location effects (income/military)
     *  are currently suppressed because the location is under enemy capture. */
    public boolean isEffectsSuppressed() { return effectsSuppressed; }
    public void setEffectsSuppressed(boolean suppressed) { this.effectsSuppressed = suppressed; }

    public LocationCard(String id, String title, String subtype,
                        Rarity rarity, Faction faction, CardSet cardSet,
                        String imageKey, String text, int influencePerRound) {
        this(id, title, subtype, rarity, faction, cardSet, imageKey, text,
                influencePerRound, 0);
    }

    public LocationCard(String id, String title, String subtype,
                        Rarity rarity, Faction faction, CardSet cardSet,
                        String imageKey, String text, int influencePerRound,
                        int military) {
        super(id, title, CardType.LOCATION, subtype, rarity, faction,
              cardSet, imageKey, text);
        this.influencePerRound = influencePerRound;
        this.military = military;
    }

    public int getInfluencePerRound() { return (isRotated() || effectsSuppressed) ? 0 : influencePerRound; }

    public int getMilitary() { reconcileDamage(); return effectsSuppressed ? 0 : Math.max(0, military - getDamageTokens()); }

    @Override public int getGreatestAbility() { return military; }

    @Override
    public int getPrimaryStatValue(ConflictType type) {
        if (effectsSuppressed) return 0;
        reconcileDamage();
        if (isFaceDown() || type != ConflictType.MILITARY) return 0;
        return getMilitary();
    }
}
