package b5ccg.model;

import b5ccg.model.enums.ConflictType;
import java.util.*;

public class Conflict {
    private final ConflictCard      card;
    private final Player            initiator;
    private final Map<Player, List<Card>> committed = new LinkedHashMap<Player, List<Card>>();
    private       Player            winner;
    private boolean                 resolved = false;

    public Conflict(ConflictCard card, Player initiator) {
        this.card      = card;
        this.initiator = initiator;
        committed.put(initiator, new ArrayList<Card>());
    }

    public ConflictCard   getCard()            { return card; }
    public Player         getInitiator()       { return initiator; }
    public ConflictType   getConflictType()    { return card.getConflictType(); }
    public int            getInfluenceReward() { return card.getInfluenceReward(); }
    public boolean        isResolved()         { return resolved; }
    public Player         getWinner()          { return winner; }

    public void commitCard(Player player, Card c) {
        if (!committed.containsKey(player)) committed.put(player, new ArrayList<Card>());
        committed.get(player).add(c);
    }

    public void addParticipant(Player player) {
        if (!committed.containsKey(player)) committed.put(player, new ArrayList<Card>());
    }

    public Set<Player>    getParticipants()          { return committed.keySet(); }
    public List<Card>     getCommittedCards(Player p) {
        if (committed.containsKey(p)) return committed.get(p);
        return Collections.emptyList();
    }
    public Map<Player, List<Card>> getAllCommitted()  {
        return Collections.unmodifiableMap(committed);
    }

    /** Returns the stat total for one player in this conflict. */
    public int playerTotal(Player p) {
        int total = 0;
        if (committed.containsKey(p)) {
            for (Card c : committed.get(p)) {
                total += c.getPrimaryStatValue(getConflictType());
            }
        }
        return total;
    }

    public void resolve(Player winner) {
        this.winner   = winner;
        this.resolved = true;
    }
}
