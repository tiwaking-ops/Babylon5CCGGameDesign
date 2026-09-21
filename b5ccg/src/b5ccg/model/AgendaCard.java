package b5ccg.model;

import b5ccg.model.enums.*;

public class AgendaCard extends Card {
    private final boolean isMajorAgenda;
    private final String  winConditionKey;

    public AgendaCard(String id, String title, String subtype,
                      Rarity rarity, Faction faction, CardSet cardSet,
                      String imageKey, String text,
                      boolean isMajorAgenda, String winConditionKey) {
        super(id, title, CardType.AGENDA, subtype, rarity, faction,
              cardSet, imageKey, text);
        this.isMajorAgenda   = isMajorAgenda;
        this.winConditionKey = winConditionKey != null ? winConditionKey : "INFLUENCE_20";
    }

    public boolean isMajorAgenda()      { return isMajorAgenda; }
    public String  getWinConditionKey() { return winConditionKey; }

    public boolean isConditionMet(GameState state, Player owner) {
        if ("INFLUENCE_20".equals(winConditionKey)) {
            return owner.getInfluence() >= 20;
        }
        if ("MILITARY_SUPREMACY".equals(winConditionKey)) {
            int ownerMil = 0;
            for (FleetCard f : owner.getFleets()) ownerMil += f.getMilitary();
            boolean supreme = true;
            for (Player p : state.getPlayers()) {
                if (p == owner) continue;
                int mil = 0;
                for (FleetCard f : p.getFleets()) mil += f.getMilitary();
                if (mil >= ownerMil) { supreme = false; break; }
            }
            return supreme;
        }
        if ("MOST_INNER_CIRCLE".equals(winConditionKey)) {
            int ownerSize = owner.getInnerCircle().size();
            if (ownerSize == 0) return false;
            boolean most = true;
            for (Player p : state.getPlayers()) {
                if (p == owner) continue;
                if (p.getInnerCircle().size() >= ownerSize) { most = false; break; }
            }
            return most;
        }
        return owner.getInfluence() >= 20;
    }

    @Override
    public int getPrimaryStatValue(ConflictType type) { return 0; }
}
