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
        switch (winConditionKey) {
            case "INFLUENCE_20":
                return owner.getInfluence() >= 20;
            case "MILITARY_SUPREMACY": {
                int ownerMil = owner.getFleets().stream()
                    .mapToInt(FleetCard::getMilitary).sum();
                return state.getPlayers().stream()
                    .filter(p -> p != owner)
                    .allMatch(p -> p.getFleets().stream()
                        .mapToInt(FleetCard::getMilitary).sum() < ownerMil);
            }
            case "MOST_INNER_CIRCLE": {
                int ownerSize = owner.getInnerCircle().size();
                return ownerSize > 0 && state.getPlayers().stream()
                    .filter(p -> p != owner)
                    .allMatch(p -> p.getInnerCircle().size() < ownerSize);
            }
            default:
                return owner.getInfluence() >= 20;
        }
    }

    @Override
    public int getPrimaryStatValue(ConflictType type) { return 0; }
}
