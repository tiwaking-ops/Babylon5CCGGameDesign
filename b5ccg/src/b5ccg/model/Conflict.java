package b5ccg.model;

import b5ccg.model.enums.ConflictType;
import java.util.*;

/**
 * A conflict in progress. Tracks committed cards per participant and — since
 * B5-0309 — the SIDE each participant is on. Rulebook ("Conflicts"): the
 * initiator wins only if the conflict receives MORE support than opposition;
 * equal or more opposition means the initiator loses. The initiator starts
 * on the support side of his own conflict.
 */
public class Conflict {
    private final ConflictCard      card;
    private final Player            initiator;
    private final Map<Player, List<Card>> committed = new LinkedHashMap<Player, List<Card>>();
    private final Set<Player>       supporters = new LinkedHashSet<Player>();
    private final Set<Player>       opposers   = new LinkedHashSet<Player>();
    private       Player            winner;
    private boolean                 resolved = false;

    public Conflict(ConflictCard card, Player initiator) {
        this.card      = card;
        this.initiator = initiator;
        committed.put(initiator, new ArrayList<Card>());
        supporters.add(initiator);   // the initiator supports his own conflict
    }

    public ConflictCard   getCard()            { return card; }
    public Player         getInitiator()       { return initiator; }
    public ConflictType   getConflictType()    { return card.getConflictType(); }
    public int            getInfluenceReward() { return card.getInfluenceReward(); }
    public boolean        isResolved()         { return resolved; }
    public Player         getWinner()          { return winner; }

    /** Convenience: commit on the initiator's (support) side. */
    public void commitCard(Player player, Card c) {
        commitCard(player, c, true);
    }

    /** Commits a card on the given side (true = support, false = oppose). */
    public void commitCard(Player player, Card c, boolean support) {
        if (!committed.containsKey(player)) committed.put(player, new ArrayList<Card>());
        committed.get(player).add(c);
        setSide(player, support);
    }

    /** Convenience: add a participant on the initiator's (support) side. */
    public void addParticipant(Player player) {
        addParticipant(player, true);
    }

    /** Adds a participant on the given side, without committing a card yet. */
    public void addParticipant(Player player, boolean support) {
        if (!committed.containsKey(player)) committed.put(player, new ArrayList<Card>());
        setSide(player, support);
    }

    private void setSide(Player player, boolean support) {
        if (support) {
            supporters.add(player);
            opposers.remove(player);
        } else {
            opposers.add(player);
            supporters.remove(player);
        }
    }

    public Set<Player>    getParticipants()          { return committed.keySet(); }
    public Set<Player>    getSupporters()            { return Collections.unmodifiableSet(supporters); }
    public Set<Player>    getOpposers()              { return Collections.unmodifiableSet(opposers); }

    /** True if the participant is on the initiator's (support) side. */
    public boolean isSupporting(Player p) { return supporters.contains(p); }

    /** True if the participant is on the opposition side. */
    public boolean isOpposing(Player p)   { return opposers.contains(p); }

    public List<Card>     getCommittedCards(Player p) {
        if (committed.containsKey(p)) return committed.get(p);
        return Collections.emptyList();
    }
    public Map<Player, List<Card>> getAllCommitted()  {
        return Collections.unmodifiableMap(committed);
    }

    /** Total stat value of all cards committed in support of the initiator. */
    public int supportTotal() {
        int total = 0;
        for (Player p : supporters) total += sideTotal(p);
        return total;
    }

    /** Total stat value of all cards committed in opposition. */
    public int oppositionTotal() {
        int total = 0;
        for (Player p : opposers) total += sideTotal(p);
        return total;
    }

    /** Returns the stat total for one player's committed cards. */
    public int playerTotal(Player p) {
        return sideTotal(p);
    }

    private int sideTotal(Player p) {
        int total = 0;
        List<Card> cards = committed.get(p);
        if (cards != null) {
            for (Card c : cards) {
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
