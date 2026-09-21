package b5ccg.model;

import b5ccg.model.enums.*;

public class EnhancementCard extends Card {
    private final int diplomacyBonus;
    private final int intrigueBonus;
    private final int psiBonus;
    private final int militaryBonus;
    private final int leadershipBonus;

    public EnhancementCard(String id, String title, String subtype,
                           Rarity rarity, Faction faction, CardSet cardSet,
                           String imageKey, String text,
                           int diplomacyBonus, int intrigueBonus,
                           int psiBonus, int militaryBonus, int leadershipBonus) {
        super(id, title, CardType.ENHANCEMENT, subtype, rarity, faction,
              cardSet, imageKey, text);
        this.diplomacyBonus  = diplomacyBonus;
        this.intrigueBonus   = intrigueBonus;
        this.psiBonus        = psiBonus;
        this.militaryBonus   = militaryBonus;
        this.leadershipBonus = leadershipBonus;
    }

    public int getDiplomacyBonus()  { return diplomacyBonus; }
    public int getIntrigueBonus()   { return intrigueBonus; }
    public int getPsiBonus()        { return psiBonus; }
    public int getMilitaryBonus()   { return militaryBonus; }
    public int getLeadershipBonus() { return leadershipBonus; }

    @Override
    public int getPrimaryStatValue(ConflictType type) { return 0; }
}
