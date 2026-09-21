package b5ccg.model;

import b5ccg.model.enums.*;

public class GroupCard extends Card {

    public GroupCard(String id, String title, String subtype,
                     Rarity rarity, Faction faction, CardSet cardSet,
                     String imageKey, String text) {
        super(id, title, CardType.GROUP, subtype, rarity, faction,
              cardSet, imageKey, text);
    }

    @Override
    public int getPrimaryStatValue(ConflictType type) { return 0; }
}
