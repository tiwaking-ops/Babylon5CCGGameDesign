package b5ccg.model;

import b5ccg.model.enums.*;

public class LocationCard extends Card {
    private final int influencePerRound;

    public LocationCard(String id, String title, String subtype,
                        Rarity rarity, Faction faction, CardSet cardSet,
                        String imageKey, String text, int influencePerRound) {
        super(id, title, CardType.LOCATION, subtype, rarity, faction,
              cardSet, imageKey, text);
        this.influencePerRound = influencePerRound;
    }

    public int getInfluencePerRound() { return isRotated() ? 0 : influencePerRound; }

    @Override
    public int getPrimaryStatValue(ConflictType type) { return 0; }
}
