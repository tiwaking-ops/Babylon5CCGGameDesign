package b5ccg.model;

import b5ccg.model.enums.*;

public class FleetCard extends Card {
    private int military;

    public FleetCard(String id, String title, String subtype,
                     Rarity rarity, Faction faction, CardSet cardSet,
                     String imageKey, String text, int military) {
        super(id, title, CardType.FLEET, subtype, rarity, faction,
              cardSet, imageKey, text);
        this.military = military;
    }

    public int getMilitary() { return isRotated() ? 0 : military; }

    public void applyMilitaryDelta(int delta) {
        military = Math.max(1, military + delta);
    }

    @Override
    public int getPrimaryStatValue(ConflictType type) {
        return type == ConflictType.MILITARY ? getMilitary() : 0;
    }
}
