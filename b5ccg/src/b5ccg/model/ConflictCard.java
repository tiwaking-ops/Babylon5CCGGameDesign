package b5ccg.model;

import b5ccg.model.enums.*;

public class ConflictCard extends Card {
    private final ConflictType conflictType;
    private final int          influenceReward;

    public ConflictCard(String id, String title, String subtype,
                        Rarity rarity, Faction faction, CardSet cardSet,
                        String imageKey, String text,
                        ConflictType conflictType, int influenceReward) {
        super(id, title, CardType.CONFLICT, subtype, rarity, faction,
              cardSet, imageKey, text);
        this.conflictType    = conflictType;
        this.influenceReward = influenceReward;
    }

    public ConflictType getConflictType()    { return conflictType; }
    public int          getInfluenceReward() { return influenceReward; }

    @Override
    public int getPrimaryStatValue(ConflictType type) { return 0; }
}
