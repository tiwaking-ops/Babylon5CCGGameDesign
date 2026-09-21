package b5ccg.model;

import b5ccg.model.enums.ConflictType;
import java.util.*;

public class Conflict {
    private final ConflictCard      card;
    private final Player            initiator;
    private final Map<Player, List<Card>> committed = new LinkedHashMap<>();
    private       Player            winner;
    private boolean                 resolved = false;

    public Conflict(ConflictCard card, Player initiator) {
        this.card      = card;
        this.initiator = initiator;
        committed.put(initiator, new ArrayList<>());
    }

    public ConflictCard   getCard()            { return card; }
    public Player         getInitiator()       { return initiator; }
    public ConflictType   getConflictType()    { return card.getConflictType(); }
    public int            getInfluenceReward() { return card.getInfluenceReward(); }
    public boolean        isResolved()         { return resolved; }
    public Player         getWinner()          { return winner; }

    public void commitCard(Player player, Card c) {
        committed.computeIfAbsent(player, k -> new ArrayList<>()).add(c);
    }

    public void addParticipant(Player player) {
        committed.putIfAbsent(player, new ArrayList<>());
    }

    public Set<Player>    getParticipants()          { return committed.keySet(); }
    public List<Card>     getCommittedCards(Player p) {
        return committed.getOrDefault(p, Collections.emptyList());
    }
    public Map<Player, List<Card>> getAllCommitted()  {
        return Collections.unmodifiableMap(committed);
    }

    /** Returns the stat total for one player in this conflict. */
    public int playerTotal(Player p) {
        int total = 0;
        for (Card c : committed.getOrDefault(p, Collections.emptyList())) {
            total += c.getPrimaryStatValue(getConflictType());
        }
        return total;
    }

    public void resolve(Player winner) {
        this.winner   = winner;
        this.resolved = true;
    }
}
