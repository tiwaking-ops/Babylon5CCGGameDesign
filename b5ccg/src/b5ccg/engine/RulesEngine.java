package b5ccg.engine;

import b5ccg.model.*;
import b5ccg.model.enums.*;
import java.util.*;

/** Enforces official B5 CCG rules for conflict initiation, resolution,
 *  aftermath eligibility, and victory checking.
 *
 *  Build Influence (rulebook V. / VI.):
 *    - A faction may spend an action to "Build Influence" only when its
 *      Influence Rating is <= 9. The action requires rotating an Inner
 *      Circle character, spends 3 influence (raised from the faction pool,
 *      which means net rating change is -3 + 1 = -2 influence entering the
 *      pool, but the rating itself goes up by 1 — implemented here as
 *      spend 3, then +1 rating token), and raises the rating by 1.
 *      Rulebook VI.: factions with a Rating >= 10 may not use this action.
 */
public class RulesEngine {

    // ── Build Influence action ────────────────────────────────────────────────

    /** Returns true when player p may Build Influence:
     *  Influence Rating 1..9 AND at least one unrotated Inner Circle character. */
    public boolean canBuildInfluence(Player p) {
        if (p.getInfluence() > 9) return false;
        if (p.getInfluence() < 1) return false;
        for (CharacterCard ch : p.getInnerCircle()) {
            if (!ch.isRotated()) return true;
        }
        return false;
    }

    /** Rotate the chosen leader, spend 3 influence, then raise rating by 1. */
    public void executeBuildInfluence(Player p, CharacterCard leader, GameState state) {
        if (!canBuildInfluence(p)) {
            state.log(p.getName() + " tried to Build Influence but cannot.");
            return;
        }
        if (!p.getInnerCircle().contains(leader)) {
            state.log(p.getName() + " tried to Build Influence with non-IC character.");
            return;
        }
        if (leader.isRotated()) {
            state.log(p.getName() + " tried to Build Influence with already-rotated "
                      + leader.getTitle() + ".");
            return;
        }

        leader.rotate();
        p.loseInfluence(3);           // spend 3 from pool
        p.gainInfluence(1);           // rating does +1 (net pool change = -2)

        state.log(p.getName() + " builds influence: "
                  + leader.getTitle() + " rotates, rating now " + p.getInfluence());
    }

    // ── Promote Character to Inner Circle (rulebook VI., B5-0321) ───────────

    /**
     * Cost to promote supporting character ch into p's Inner Circle
     * (rulebook: ACTION - Promote a Character to the Inner Circle):
     * the character's influence cost (doubled if loyal to a different race)
     * PLUS one additional influence for each character already in the Inner
     * Circle. "Plus one for each character that is already a member" — the
     * ambassador IS a member, so this is simply innerCircle.size().
     *
     * B5-0323 note: cards carry no cost field yet, so the influence cost is
     * currently 0 for every card. The double-cost rule still composes when a
     * cost exists; the +IC-member term is live today.
     */
    public int promotionCost(Player p, CharacterCard ch) {
        int base = ch.getCost();   // B5-0323: the card's influence cost
        if (ch.getFaction() != p.getFaction()
                && ch.getFaction() != Faction.NEUTRAL
                && ch.getFaction() != Faction.ANY) {
            base = base * 2;   // double-cost for other-race loyal characters
        }
        return base + p.getInnerCircle().size();
    }

    /** Returns true when p may promote ch into the Inner Circle now:
     *  ch is a ready supporting character, an unrotated Inner Circle member
     *  exists to rotate, and p can afford promotionCost. */
    public boolean canPromote(Player p, CharacterCard ch) {
        if (ch == null || !p.getSupportingRole().contains(ch)) return false;
        if (ch.isRotated() || ch.isFaceDown()) return false;
        boolean hasLeader = false;
        for (CharacterCard ic : p.getInnerCircle()) {
            if (!ic.isRotated()) { hasLeader = true; break; }
        }
        if (!hasLeader) return false;
        return p.getInfluence() >= promotionCost(p, ch);
    }

    /** Rotate the chosen IC leader, apply the promotion cost, move ch into
     *  the Inner Circle (ready — the rulebook only requires the sponsor to
     *  rotate, not the promoted character). */
    public void executePromote(Player p, CharacterCard ch, CharacterCard leader,
                               GameState state) {
        if (!canPromote(p, ch)) {
            state.log(p.getName() + " tried to promote "
                      + (ch == null ? "(null)" : ch.getTitle()) + " but cannot.");
            return;
        }
        if (leader == null || !p.getInnerCircle().contains(leader)
                || leader.isRotated()) {
            state.log(p.getName() + " tried to promote with an invalid leader.");
            return;
        }

        leader.rotate();
        p.spendInfluence(promotionCost(p, ch));
        p.recruitToInnerCircle(ch);

        state.log(p.getName() + " promotes " + ch.getTitle()
                  + " to the Inner Circle (" + leader.getTitle() + " rotates, cost "
                  + promotionCost(p, ch) + "); IC now " + p.getInnerCircle().size());
    }

    // ── Recruit (sponsor) a supporting character (B5-0323) ────────────────

    /** Cost to recruit (sponsor) supporting character ch from hand
     *  (rulebook §Sponsor): the character's influence cost, doubled when the
     *  character is loyal to a different race; neutral characters at no
     *  additional cost. */
    public int recruitCost(Player p, CharacterCard ch) {
        int base = ch.getCost();
        if (ch.getFaction() != p.getFaction()
                && ch.getFaction() != Faction.NEUTRAL
                && ch.getFaction() != Faction.ANY) {
            base = base * 2;
        }
        return base;
    }

    /** True when p may recruit ch from hand now: the card is in hand and
     *  the faction can apply its influence cost (rulebook: "apply the
     *  required influence cost ... or this action may not be performed"). */
    public boolean canRecruit(Player p, CharacterCard ch) {
        if (ch == null || !p.getHand().contains(ch)) return false;
        return p.getInfluence() >= recruitCost(p, ch);
    }

    // ── Conflict resolution ──────────────────────────────────────────────────

    public Player resolveConflict(Conflict conflict, GameState state) {
        ConflictType type = conflict.getConflictType();
        HashMap<Player, Integer> totals = new HashMap<Player, Integer>();

        for (Player p : conflict.getParticipants()) {
            int total = 0;
            for (Card c : conflict.getCommittedCards(p)) {
                total += c.getPrimaryStatValue(type);
            }
            if (p.getAmbassador() != null && !p.getAmbassador().isFaceDown()
                    && !conflict.getCommittedCards(p).contains(p.getAmbassador())) {
                total += p.getAmbassador().getPrimaryStatValue(type);
            }
            totals.put(p, Math.max(0, total));
        }

        // Rulebook ("Conflicts"): the initiator wins only if the conflict
        // receives MORE support than opposition; equal or more opposition
        // means the initiator loses. Before B5-0309 the Conflict could not
        // express sides and this loop fed a highest-total-wins heuristic
        // (audit deviation D14).
        Player winner;
        int    winVal;
        if (conflict.supportTotal() > conflict.oppositionTotal()
                || conflict.oppositionTotal() == 0) {
            winner = conflict.getInitiator();
        } else {
            winner = leadingOpposer(conflict, totals);
        }
        winVal = totals.containsKey(winner) ? totals.get(winner) : 0;

        conflict.resolve(winner);
        state.log(conflict.getCard().getTitle() + " won by " + winner.getName()
                  + " (support=" + conflict.supportTotal()
                  + ", opposition=" + conflict.oppositionTotal() + ")");

        winner.gainInfluence(conflict.getInfluenceReward());

        for (Player p : conflict.getParticipants()) {
            for (Card c : conflict.getCommittedCards(p)) {
                if (c instanceof FleetCard) c.rotate();
            }
        }

        for (Player p : conflict.getParticipants()) {
            if (p != winner) {
                int diff = winVal - (totals.containsKey(p) ? totals.get(p) : 0);
                // B5-0309 note (audit D10): the pre-0309 code damaged ANY
                // loser's ambassador (≥3-point gap) regardless of conflict
                // type; damage is a MILITARY-resolution consequence.
                if (diff >= 3 && p.getAmbassador() != null
                        && conflict.getConflictType() == ConflictType.MILITARY) {
                    p.getAmbassador().damage();
                    state.log(p.getName() + "'s ambassador is damaged.");
                }
                // Supporting-role characters that participated are discarded on loss
                List<Card> pCards = new ArrayList<Card>(conflict.getCommittedCards(p));
                for (Card c : pCards) {
                    if (c instanceof CharacterCard) {
                        CharacterCard ch = (CharacterCard) c;
                        if (p.getSupportingRole().contains(ch)) {
                            p.getSupportingRole().remove(ch);
                            p.getDeck().discard(ch);
                            state.log(p.getName() + ": " + ch.getTitle()
                                      + " discarded (supporting role loss).");
                        }
                    }
                }
            }
        }

        return winner;
    }

    /** The opposition participant with the highest total (insertion order
     *  breaks ties); only called when opposition strictly exceeds support. */
    private Player leadingOpposer(Conflict conflict, Map<Player, Integer> totals) {
        Player best    = null;
        int    bestVal = -1;
        for (Player p : conflict.getOpposers()) {
            int v = totals.containsKey(p) ? totals.get(p).intValue() : 0;
            if (v > bestVal) { bestVal = v; best = p; }
        }
        return best;
    }

    // ── Victory check ────────────────────────────────────────────────────────

    public Player checkVictory(GameState state) {
        Player lastStanding = null;
        int remaining = 0;
        for (Player p : state.getPlayers()) {
            if (p.hasForfeited()) continue;
            remaining++;
            lastStanding = p;
        }
        if (remaining == 1) return lastStanding;

        for (Player p : state.getPlayers()) {
            if (p.hasForfeited()) continue;
            AgendaCard agenda = p.getAgenda();
            // Agenda-driven win: the agenda's own condition governs
            // (e.g. "Reach 20 Influence", Military Supremacy, Most Inner Circle).
            if (agenda != null && agenda.isConditionMet(state, p)) {
                return p;
            }
            // Standard victory (rulebook: Victory): 20 power AND more than any
            // other player. A major agenda in play blocks the standard path.
            if (agenda == null || !agenda.isMajorAgenda()) {
                if (standardVictory(state, p)) return p;
            }
        }
        return null;
    }

    /**
     * Standard victory: at least 20 power and STRICTLY more than every other
     * non-forfeited player ("Have 20 Power, and more than any other player").
     * A tie with any opponent blocks the win; forfeited players do not count.
     */
    private boolean standardVictory(GameState state, Player p) {
        if (p.getInfluence() < 20) return false;
        for (Player q : state.getPlayers()) {
            if (q == p || q.hasForfeited()) continue;
            if (q.getInfluence() >= p.getInfluence()) return false;
        }
        return true;
    }

    // ── Legality checks ──────────────────────────────────────────────────────

    public boolean canInitiateConflict(Player p, ConflictCard c, GameState state) {
        if (p.isPassed()) return false;
        if (p.getActionsLeft() <= 0) return false;
        if (state.hasInitiatedConflictThisTurn(p)) return false;   // B5-0302
        if (!p.getHand().contains(c)) return false;
        if (!c.getFaction().isPlayableBy(p.getFaction())) return false;
        return true;
    }

    public boolean canPlayCard(Player p, Card c) {
        if (!p.getHand().contains(c)) return false;
        return c.getFaction().isPlayableBy(p.getFaction());
    }

    public boolean initiatorWon(Conflict resolved, Player winner) {
        return resolved.getInitiator() == winner;
    }

    public boolean canPlayAftermath(Player p, AftermathCard a,
                                    Conflict resolved, boolean initiatorWon) {
        if (!p.getHand().contains(a)) return false;
        boolean participated = resolved.getParticipants().contains(p);
        return a.isEligible(initiatorWon, participated, resolved.getConflictType());
    }

    // ── Join conflict action (B5-0322) ─────────────────────────────────────────

    /** Returns true when p may join the active conflict on the given side:
     *  p has an action remaining, has not passed, there is an active conflict,
     *  p is not already a participant, and p's faction may play the conflict card. */
    public boolean canJoinConflict(Player p, Conflict conflict) {
        if (p.isPassed()) return false;
        if (p.getActionsLeft() <= 0) return false;
        if (conflict == null) return false;
        if (conflict.isResolved()) return false;
        if (conflict.getParticipants().contains(p)) return false;
        return true;
    }

    /** Adds p to the given side of the active conflict, commits p's ambassador
     *  if face-up, and logs the join. The conflict's resolveConflict() handles
     *  ambassador damage based on the final result; this method does not resolve. */
    public void executeJoinConflict(Player p, Conflict conflict, boolean support,
                                    GameState state) {
        if (!canJoinConflict(p, conflict)) {
            state.log(p.getName() + " cannot join — already joined or no active conflict.");
            return;
        }
        conflict.addParticipant(p, support);
        if (p.getAmbassador() != null && !p.getAmbassador().isFaceDown()) {
            conflict.commitCard(p, p.getAmbassador(), support);
        }
        String side = support ? "support" : "oppose";
        state.log(p.getName() + " joins the conflict as " + side + ".");
    }

    // ── Round start ──────────────────────────────────────────────────────────

    public void startRound(GameState state) {
        for (Player p : state.getPlayers()) {
            p.resetActions();
            if (p.getAmbassador() != null) p.getAmbassador().unrotate();
            for (CharacterCard ch : p.getInnerCircle())  ch.unrotate();
            for (CharacterCard ch : p.getSupportingRole()) ch.unrotate();
            for (FleetCard fl : p.getFleets())           fl.unrotate();
            for (GroupCard  gr : p.getGroups())          gr.unrotate();
            p.collectLocationIncome();
            CardEffects.applyAgendaStartOfRound(state, p);
            int enhIncome = CardEffects.enhancementLocationIncomeBonus(p);
            if (enhIncome > 0) {
                p.gainInfluence(enhIncome);
                state.log(p.getName() + " gains " + enhIncome
                        + " influence from location enhancement(s).");
            }
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
