package b5ccg.model;

import b5ccg.model.enums.*;

public class CharacterCard extends Card {
    private int     diplomacy;
    private int     intrigue;
    private int     psi;
    private int     leadership;
    private final boolean isAmbassador;

    public CharacterCard(String id, String title, String subtype,
                         Rarity rarity, Faction faction, CardSet cardSet,
                         String imageKey, String text,
                         int diplomacy, int intrigue, int psi, int leadership,
                         boolean isAmbassador) {
        super(id, title, CardType.CHARACTER, subtype, rarity, faction,
              cardSet, imageKey, text);
        this.diplomacy    = diplomacy;
        this.intrigue     = intrigue;
        this.psi          = psi;
        this.leadership   = leadership;
        this.isAmbassador = isAmbassador;
    }

    public int  getDiplomacy()    { return diplomacy; }
    public int  getIntrigue()     { return intrigue; }
    public int  getPsi()          { return psi; }
    public int  getLeadership()   { return leadership; }
    public boolean isAmbassador() { return isAmbassador; }

    /** Applies a stat delta (from Enhancements). Floors each stat at 0. */
    public void applyStatDelta(int dDip, int dInt, int dPsi, int dLead) {
        diplomacy  = Math.max(0, diplomacy  + dDip);
        intrigue   = Math.max(0, intrigue   + dInt);
        psi        = Math.max(0, psi        + dPsi);
        leadership = Math.max(0, leadership + dLead);
    }

    @Override
    public int getPrimaryStatValue(ConflictType type) {
        switch (type) {
            case DIPLOMACY: return isFaceDown() ? 0 : diplomacy;
            case INTRIGUE:  return isFaceDown() ? 0 : intrigue;
            case PSI:       return isFaceDown() ? 0 : psi;
            case MILITARY:  return isFaceDown() ? 0 : leadership;
            default:        return 0;
        }
    }
}
