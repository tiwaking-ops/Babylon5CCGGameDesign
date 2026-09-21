package b5ccg.engine;

import b5ccg.model.*;
import b5ccg.model.enums.*;
import java.util.*;

/**
 * Enforces official B5 CCG rules for conflict initiation, resolution,
 * aftermath eligibility, and victory checking.
 */
public class RulesEngine {

    // ── Conflict resolution ──────────────────────────────────────────────────

    /**
     * Resolve the active conflict: compute totals for all participants,
     * determine the winner, apply influence rewards, and handle damage.
     */
    public Player resolveConflict(Conflict conflict, GameState state) {
        ConflictType type = conflict.getConflictType();
        Map<Player, Integer> totals = new HashMap<>();

        for (Player p : conflict.getParticipants()) {
            int total = 0;
            for (Card c : conflict.getCommittedCards(p)) {
                total += c.getPrimaryStatValue(type);
            }
            // Add base ambassador stat if not already committed
            if (p.getAmbassador() != null && !p.getAmbassador().isFaceDown()
                    && !conflict.getCommittedCards(p).contains(p.getAmbassador())) {
                total += p.getAmbassador().getPrimaryStatValue(type);
            }
            totals.put(p, Math.max(0, total));
        }

        // Determine winner — highest total wins; ties go to initiator
        Player winner = conflict.getInitiator();
        int    winVal = totals.getOrDefault(winner, 0);

        for (Map.Entry<Player, Integer> entry : totals.entrySet()) {
            if (entry.getValue() > winVal) {
                winVal = entry.getValue();
                winner = entry.getKey();
            }
        }

        conflict.resolve(winner);
        state.log(conflict.getCard().getTitle() + " won by " + winner.getName()
                  + " (total=" + winVal + ")");

        // Influence reward to winner
        winner.gainInfluence(conflict.getInfluenceReward());

        // Rotate all committed fleets
        for (Player p : conflict.getParticipants()) {
            for (Card c : conflict.getCommittedCards(p)) {
                if (c instanceof FleetCard) c.rotate();
            }
        }

        // Damage losing ambassador if winner beats by 3+
        for (Player p : conflict.getParticipants()) {
            if (p != winner) {
                int diff = winVal - totals.getOrDefault(p, 0);
                if (diff >= 3 && p.getAmbassador() != null) {
                    p.getAmbassador().damage();
                    state.log(p.getName() + "'s ambassador is damaged.");
                }
                // Supporting-role characters that participated are discarded on loss
                List<Card> pCards = new ArrayList<>(conflict.getCommittedCards(p));
                for (Card c : pCards) {
                    if (c instanceof CharacterCard) {
                        CharacterCard ch = (CharacterCard) c;
                        if (p.getSupportingRole().contains(ch)) {
                            p.getSupportingRole().remove(ch);
                            p.getDeck().discard(ch);
                            state.log(p.getName() + ": " + ch.getTitle() + " discarded (supporting role loss).");
                        }
                    }
                }
            }
        }

        return winner;
    }

    // ── Victory check ────────────────────────────────────────────────────────

    public Player checkVictory(GameState state) {
        for (Player p : state.getPlayers()) {
            if (p.getAgenda() != null && p.getAgenda().isConditionMet(state, p)) {
                return p;
            }
            // Default win condition: 20 influence with no agenda
            if (p.getAgenda() == null && p.getInfluence() >= 20) {
                return p;
            }
        }
        return null;
    }

    // ── Legality checks ──────────────────────────────────────────────────────

    public boolean canInitiateConflict(Player p, ConflictCard c, GameState state) {
        if (p.isPassed()) return false;
        if (p.getActionsLeft() <= 0) return false;
        // Card must be in hand
        if (!p.getHand().contains(c)) return false;
        // Faction check
        if (!c.getFaction().isPlayableBy(p.getFaction())) return false;
        return true;
    }

    public boolean canPlayCard(Player p, Card c) {
        if (!p.getHand().contains(c)) return false;
        return c.getFaction().isPlayableBy(p.getFaction());
    }

    public boolean canPlayAftermath(Player p, AftermathCard a,
                                    Conflict resolved, boolean playerWon) {
        if (!p.getHand().contains(a)) return false;
        boolean participated = resolved.getParticipants().contains(p);
        return a.isEligible(playerWon, participated, resolved.getConflictType());
    }

    // ── Round start ──────────────────────────────────────────────────────────

    /** Called at start of each round: unrotate all cards, collect location income. */
    public void startRound(GameState state) {
        for (Player p : state.getPlayers()) {
            p.resetActions();
            if (p.getAmbassador() != null) p.getAmbassador().unrotate();
            for (CharacterCard ch : p.getInnerCircle())  ch.unrotate();
            for (CharacterCard ch : p.getSupportingRole()) ch.unrotate();
            for (FleetCard fl : p.getFleets())           fl.unrotate();
            for (GroupCard  gr : p.getGroups())          gr.unrotate();
            p.collectLocationIncome();
        }
        state.log("=== Round " + state.getRoundNumber() + " begins ===");
    }

    // ── Draw phase ───────────────────────────────────────────────────────────

    public void drawPhase(GameState state) {
        for (Player p : state.getPlayers()) {
            p.drawCards(1);
        }
    }
}
