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

    // B5-0371: healing a ready character clears normal damage. A neutralized
    // IC member may heal severe damage one token at a time, then flip up.
    public boolean canHealCharacter(Player p, CharacterCard ch) {
        if (p == null || ch == null || ch.isRotated() || !ch.canActAfterNeutralization()) return false;
        boolean inner = p.getInnerCircle().contains(ch);
        boolean supporting = p.getSupportingRole().contains(ch);
        if (!inner && !supporting) return false;
        if (ch.isNeutralized()) {
            return inner;
        }
        if (ch.isFaceDown()) return false;
        // IC characters may rotate undamaged to provide aid to their ambassador.
        return inner || ch.getDamageTokens() > 0;
    }

    public boolean executeHealCharacter(Player p, CharacterCard ch, GameState state) {
        if (!canHealCharacter(p, ch)) return false;
        ch.rotate();
        int healed = ch.healDamage();
        if (p.getInnerCircle().contains(ch)) p.markInnerCircleHealed(ch);
        state.log(p.getName() + " rotates " + ch.getTitle() + " to heal (" + healed + " step).");
        // B5-0506 (B5-0497 slice 1): shunned-class reactive discard -- after a
        // successful heal, the discard-on-heal enhancement ATTACHED TO THIS
        // character is discarded from WHOEVER holds it (its playing player,
        // typically the opponent) and its granted bonuses are lifted. Matching
        // is by targetCardId == the healed character, so other held
        // discard-on-heal cards attached to other characters are untouched.
        for (Player holder : state.getPlayers()) {
            Iterator<EnhancementCard> eit = holder.getEnhancements().iterator();
            while (eit.hasNext()) {
                EnhancementCard enh = eit.next();
                if (CardEffects.discardsOnHeal(enh.getId())
                        && ch.getId().equals(enh.getTargetCardId())) {
                    eit.remove();
                    for (Player r : state.getPlayers()) {
                        r.removeBonusesBySource(enh.getId());
                    }
                    state.log(holder.getName() + "'s " + enh.getTitle()
                            + " is discarded (" + ch.getTitle() + " healed).");
                }
            }
        }
        return true;
    }

    // Repairs affect normal damage only. Severe damage and neutralization
    // require healing rules and are never purchased with influence.
    public boolean canRepairCard(Player p, Card card) {
        if (p == null || card == null || card.isRotated() || card.isNeutralized()
                || card.isFaceDown() || !card.canActAfterNeutralization()
                || card.getDamageTokens() <= 0) return false;
        if (!(card instanceof FleetCard) && !(card instanceof LocationCard)) return false;
        return p.controlsCard(card) && p.getAppliedPool() >= card.getDamageTokens();
    }

    public boolean executeRepairCard(Player p, Card card, GameState state) {
        if (!canRepairCard(p, card)) return false;
        int amount = card.getDamageTokens();
        if (!p.applyInfluence(amount)) return false;
        int repaired = card.repairDamage(amount);
        if (repaired != amount) {
            p.gainInfluence(amount - repaired);
            return false;
        }
        card.rotate();
        state.log(p.getName() + " repairs " + card.getTitle() + " (" + repaired
                + " damage for " + repaired + " influence from pool).");
        return true;
    }

    /** End-of-action-round ambassador aid: all IC members must have healed. */
    public void finishActionRound(GameState state) {
        for (Player p : state.getPlayers()) {
            if (p.allInnerCircleHealed() && p.getAmbassador() != null) {
                p.getAmbassador().heal();
                state.log(p.getName() + " fully heals the ambassador with Inner Circle aid.");
            }
            p.resetInnerCircleHealing();
        }
    }

    // ── Build Influence action ────────────────────────────────────────────────

    /** Returns true when player p may Build Influence:
     *  Influence Rating 1..9 AND at least one unrotated Inner Circle character. */
    public boolean canBuildInfluence(Player p) {
        if (p.getInfluence() > 9) return false;
        if (p.getAppliedPool() < 3) return false;
        for (CharacterCard ch : p.getInnerCircle()) {
            if (!ch.isRotated() && ch.canActAfterNeutralization()) return true;
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
        if (leader.isRotated() || !leader.canActAfterNeutralization()) {
            state.log(p.getName() + " tried to Build Influence with already-rotated "
                      + leader.getTitle() + ".");
            return;
        }

        if (!p.applyInfluence(3)) return;
        leader.rotate();
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
        if (ch.isRotated() || ch.isFaceDown() || !ch.canActAfterNeutralization()) return false;
        boolean hasLeader = false;
        for (CharacterCard ic : p.getInnerCircle()) {
            if (!ic.isRotated() && ic.canActAfterNeutralization()) { hasLeader = true; break; }
        }
        if (!hasLeader) return false;
        return p.getAppliedPool() >= promotionCost(p, ch);
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
                || leader.isRotated() || !leader.canActAfterNeutralization()) {
            state.log(p.getName() + " tried to promote with an invalid leader.");
            return;
        }

        int cost = promotionCost(p, ch);
        if (!p.applyInfluence(cost)) return;
        leader.rotate();
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
    /** Base sponsor cost (rulebook §Sponsor): card cost, doubled for other-race
     *  loyalty; neutral characters at no additional cost. */
    public int baseRecruitCost(Player p, CharacterCard ch) {
        int base = ch.getCost();
        if (isDoubleCostRequired(ch, p.getFaction())) {
            base = base * 2;
        }
        return base;
    }

    /**
     * Rulebook §Sponsor (:659): loyal characters whose race differs from the
     * sponsoring faction cost double; neutral characters are exempt.
     */
    public boolean isDoubleCostRequired(CharacterCard ch, Faction faction) {
        if (ch == null || faction == null) return false;
        Faction cardFaction = ch.getFaction();
        return cardFaction != faction && cardFaction != Faction.NEUTRAL
                && cardFaction != Faction.ANY;
    }

    /** B5-0339: effective sponsor cost — the base minus the ambassador's
     *  assistant sponsor discount (rulebook §IV), floored at 0. */
    public int recruitCost(Player p, CharacterCard ch) {
        return Math.max(0, baseRecruitCost(p, ch) - p.getSponsorDiscount());
    }

    /**
     * Sponsor evaluation keeps the full cost and waiver semantics together.
     * A free sponsor waives the entire listed cost, including any race-based
     * doubling; normal recruiting retains the existing discount calculation.
     */
    public SponsorCost sponsorCost(Player p, CharacterCard ch) {
        if (CardEffects.sponsorWaiver(ch) == CardEffects.WaiverEffect.FREE_SPONSOR) {
            return SponsorCost.waived(baseRecruitCost(p, ch));
        }
        return new SponsorCost(recruitCost(p, ch), true, false);
    }

    /** True when p may recruit ch from hand now: the card is in hand and
     *  the faction can apply its influence cost (rulebook: "apply the
     *  required influence cost ... or this action may not be performed"). */
    public boolean canRecruit(Player p, CharacterCard ch) {
        if (ch == null || !p.getHand().contains(ch)) return false;
        return p.getAppliedPool() >= sponsorCost(p, ch).getAmount();
    }

    // ── Agenda lifecycle (B5-0364; rulebook :520/:719, B5-0345 Tier-1 #3) ───

    /**
     * Sponsor legality (one-major rule, rulebook :520 "sponsor a new one if
     * your faction does not currently have an agenda card in play"; row scope
     * = this gate only — the :719 sponsor ROTATION cost is a pre-existing
     * gap recorded for a later task). Faction playability stays with the
     * offer layers (AI gate) as today.
     */
    public boolean canSponsorAgenda(Player p, AgendaCard ag) {
        if (p == null || ag == null) return false;
        return p.getAgenda() == null;
    }

    /** Discard (:719 "considered an action"; :522 Major agendas cannot be
     *  discarded — they must be REPLACEd). */
    public boolean canDiscardAgenda(Player p) {
        AgendaCard a = p.getAgenda();
        if (a == null) return false;
        return !a.isMajorAgenda();
    }

    /**
     * Replace (:520 an Inner Circle character rotates to replace; :719 the
     * new agenda comes from your hand and must be one you can sponsor, a
     * Major Agenda may only be replaced by another Major, and a hidden
     * agenda may never be the replacement).
     */
    public boolean canReplaceAgenda(Player p, AgendaCard replacement,
                                    CharacterCard leader) {
        AgendaCard current = p.getAgenda();
        if (current == null || replacement == null) return false;
        if (current == replacement) return false;
        if (!p.getHand().contains(replacement)) return false;
        if (replacement.isFaceDown()) return false;
        if (!replacement.getFaction().isPlayableBy(p.getFaction())) return false;
        if (current.isMajorAgenda() && !replacement.isMajorAgenda()) return false;
        if (leader == null || !p.getInnerCircle().contains(leader)) return false;
        if (leader.isRotated() || leader.isFaceDown() || !leader.canActAfterNeutralization()) return false;
        return true;
    }

    /** Reveal (:520 hidden agendas have no effect until revealed; :719
     *  reveal is an action). The sponsor-at-that-time check lives in the
     *  controller ("could not sponsor at that time, so discard instead"). */
    public boolean canRevealAgenda(Player p) {
        AgendaCard a = p.getAgenda();
        return a != null && a.isFaceDown();
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
        String cardTitle = conflict.getCard() != null
                ? conflict.getCard().getTitle()
                : "War Conflict";
        state.log(cardTitle + " won by " + winner.getName()
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

        // B5-0376: war-conflict outcome resolution — influence swing,
        // location capture, and tension increment.
        if (conflict.isWarConflict()) {
            resolveWarOutcome(conflict, winner, state);
        }

        return winner;
    }

    // ── B5-0376: war conflict outcome resolution ──────────────────────────────
    /** B5-0376 Phase B: resolve a war-conflict outcome — influence swing,
     *  location capture/suppression, and tension increment. Made public
     *  so conformance tests can drive resolution directly.
     *  uncontested RACE_TARGET, location capture for LOCATION_TARGET won by
     *  the initiator, and a +1 tension increment toward the target (clamped
     *  at 5). Proposal §3.3.
     */
    public void resolveWarOutcome(Conflict conflict, Player winner, GameState state) {
        Player initiator = conflict.getInitiator();
        if (conflict.getWarKind() == WarKind.RACE_TARGET) {
            boolean initiatorWon = winner == initiator;
            boolean contested = !conflict.getOpposers().isEmpty()
                    || conflict.anyAttackOccurred();
            if (initiatorWon && !contested) {
                Player target = conflict.getTarget();
                if (target != null && target != winner) {
                    target.loseInfluence(1);
                    winner.gainInfluence(1);
                    state.log("War outcome: " + target.getName()
                            + " loses 1, " + winner.getName()
                            + " gains 1 (uncontested race war).");
                }
            }
        }
        if (conflict.getWarKind() == WarKind.LOCATION_TARGET
                && conflict.getTargetLocation() != null
                && winner == initiator) {
            LocationCard loc = conflict.getTargetLocation();
            boolean wasSuppressed = loc.isEffectsSuppressed();
            if (wasSuppressed && loc.getFaction() == winner.getFaction()) {
                loc.setCapturedBy(null);
                loc.setEffectsSuppressed(false);
                state.log("War outcome: " + winner.getName()
                        + " recaptures " + loc.getTitle()
                        + " (effects restored).");
            } else {
                loc.setCapturedBy(initiator);
                loc.setEffectsSuppressed(true);
                removeLocationIncomeEnhancements(initiator);
                int suppressIncome = loc.getInfluencePerRound();
                state.log("War outcome: " + initiator.getName()
                        + " captures " + loc.getTitle() + " ("
                        + suppressIncome + "/round suppressed).");
                // B5-0437: station capture is a source — raise the capturing
                // faction's station influence rating (B5-0428 proposal
                // §Sources). Only the human-side station rating is wired
                // today; shadow/vorlon ratings stay inert until a card hook
                // lands (B5-0354 discipline).
                state.getStation().gainInfluence(1);
                state.setStationSourceFired(true);
            }
        }

        Player target;
        if (conflict.getWarKind() == WarKind.LOCATION_TARGET) {
            // Tension runs toward the location's original faction owner (the
            // printed faction), not the current occupier — capture mutates
            // capturedBy before this block, so read the card's faction here.
            Faction owner = conflict.getTargetLocation() != null
                    ? conflict.getTargetLocation().getFaction() : null;
            target = null;
            if (owner != null) {
                for (Player pp : state.getPlayers()) {
                    if (pp.getFaction() == owner) { target = pp; break; }
                }
            }
        } else {
            target = conflict.getTarget();
        }
        if (target != null) {
            Faction targetFaction = target.getFaction();
            Faction initiatorFaction = initiator.getFaction();
            if (targetFaction != null && initiatorFaction != null
                    && targetFaction != initiatorFaction) {
                state.raiseTension(targetFaction, initiatorFaction, 1);
                state.log("Tension: " + targetFaction + " toward "
                        + initiatorFaction + " raised to "
                        + state.getTensionMatrix().getTension(targetFaction, initiatorFaction)
                        + " (war outcome).");
            }
        }
    }

    /** B5-0376 Phase B: clear player-wide location-income enhancements after a
     *  location capture. Enhancements are player-scoped (not attached to a
     *  specific location), so all held location-income-augmenting enhancements
     *  are discarded. Lives in the engine (not model) to keep the model layer
     *  free of engine dependencies. */
    private void removeLocationIncomeEnhancements(Player p) {
        Iterator<EnhancementCard> it = p.getEnhancements().iterator();
        while (it.hasNext()) {
            if (CardEffects.isLocationIncomeEnhancement(it.next())) it.remove();
        }
    }

    // ── B5-0376: war conflict legality ────────────────────────────────────────
    /**
     * Returns true when p may declare a war conflict now: p has an action
     * remaining, has not passed, has not initiated any conflict this turn
     * (one-conflict-per-turn gate), and p's faction is at war with at least
     * one other faction. Proposal §3.2.
     */
    public boolean canDeclareWarConflict(Player p, GameState state) {
        if (p.isPassed()) return false;
        if (p.getActionsLeft() <= 0) return false;
        if (state.hasInitiatedConflictThisTurn(p)) return false;
        return state.isAtWar(p.getFaction());
    }

    /**
     * Returns true when p may initiate the specific war conflict described by
     * {@code kind}, {@code raceTarget}, and {@code locTarget}. For
     * RACE_TARGET the race target must be non-null and at war with p; for
     * LOCATION_TARGET the location must be non-null and owned by a faction
     * at war with p. Proposal §3.2.
     */
    public boolean canInitiateWarConflict(Player p, WarKind kind,
                                           Player raceTarget, LocationCard locTarget,
                                           GameState state) {
        if (!canDeclareWarConflict(p, state)) return false;
        if (kind == WarKind.RACE_TARGET) {
            if (raceTarget == null) return false;
            return state.isAtWar(p.getFaction(), raceTarget.getFaction());
        }
        if (kind == WarKind.LOCATION_TARGET) {
            if (locTarget == null) return false;
            Player owner = state.findLocationOwner(locTarget);
            if (owner == null) return false;
            return state.isAtWar(p.getFaction(), owner.getFaction());
        }
        return false;
    }

    /**
     * Creates and returns a new war conflict after legality checks pass.
     * The caller (GameController) is responsible for setting it as the
     * active conflict and resolving it.
     */
    public Conflict declareWarConflict(Player p, WarKind kind,
                                        Player raceTarget, LocationCard locTarget,
                                        GameState state) {
        if (!canInitiateWarConflict(p, kind, raceTarget, locTarget, state)) {
            return null;
        }
        Conflict conflict;
        if (kind == WarKind.RACE_TARGET) {
            conflict = new Conflict(kind, p, raceTarget);
        } else {
            conflict = new Conflict(kind, p, locTarget);
        }
        return conflict;
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

        // Standard Victory condition 2 (rulebook :176): if Babylon 5 has an
        // Influence Rating of 20 or more at the end of a turn — and one player
        // eligible to win a standard victory is leading in Power — then that
        // player wins. checkVictory runs at the round boundary (the engine's
        // "end of turn"; interpretation recorded in DECISIONS B5-0340).
        // Eligibility (rulebook :170 "eligible to win by scoring a Standard
        // Victory" = not BARRED): no major agenda, and no Shadow War
        // (rulebook :178 — during the Shadow War NO Standard Victory is
        // possible, so condition 2 is inert there; B5-0354 finding). The
        // "leading in Power" predicate is strictly-greatest, single winner —
        // a tie crowns nobody (D12 discipline).
        Player stationLeader = stationVictory(state);
        if (stationLeader != null) return stationLeader;

        for (Player p : state.getPlayers()) {
            if (p.hasForfeited()) continue;
            AgendaCard agenda = p.getAgenda();
            // B5-0364: a hidden (face-down) agenda has NO effect on play
            // until revealed (rulebook :520) — neither its win condition nor
            // its major-agenda standard bar applies while face-down.
            // Agenda-driven win: the agenda's own condition governs
            // (e.g. "Reach 20 Influence", Military Supremacy, Most Inner Circle).
            if (agenda != null && !agenda.isFaceDown()
                    && agenda.isConditionMet(state, p)) {
                return p;
            }
            // Standard victory (rulebook: Victory): 20 power AND more than any
            // other player. A major agenda in play blocks the standard path
            // (a hidden one does not — it takes effect only on reveal).
            if (agenda == null || agenda.isFaceDown() || !agenda.isMajorAgenda()) {
                if (standardVictory(state, p)) return p;
            }
        }
        return null;
    }

    /**
     * B5-0340 — Standard Victory condition 2 (rulebook :176). Fires only when
     * the station's Influence Rating is 20+ AND exactly one non-forfeited,
     * standard-ELIGIBLE player strictly leads in Power; a tie crowns nobody.
     * Eligibility = not barred: no major agenda (rulebook :178 "Other cards
     * in play may also make a player ineligible ... e.g. playing a Major
     * Agenda") and no Shadow War (rulebook :178 — no Standard Victory during
     * the Shadow War, and condition 2 IS a Standard Victory). Runs before the
     * per-player scan; today it can never fire (station influence starts at
     * 0 and nothing moves it — B5-0354 research), which is the intended
     * rulebook-conservative default.
     */
    private Player stationVictory(GameState state) {
        if (state.isShadowWar()) return null;
        if (state.getStation().getInfluence() < Babylon5Station.CONDITION_2_THRESHOLD) return null;
        Player leader = null;
        int leaders = 0;
        for (Player p : state.getPlayers()) {
            if (p.hasForfeited()) continue;
            AgendaCard agenda = p.getAgenda();
            // B5-0364: hidden agendas are inert (:520) — a face-down Major
            // does not bar its owner from the station-crown path until reveal.
            if (agenda != null && !agenda.isFaceDown() && agenda.isMajorAgenda()) continue;
            if (strictlyLeads(state, p, true)) {
                leader = p;
                leaders++;
            }
        }
        return leaders == 1 ? leader : null;
    }

    /**
     * Standard victory: at least 20 power and STRICTLY more than every other
     * non-forfeited player ("Have 20 Power, and more than any other player").
     * A tie with any opponent blocks the win; forfeited players do not count.
     */
    private boolean standardVictory(GameState state, Player p) {
        if (p.getInfluence() < 20) return false;
        return strictlyLeads(state, p, false);
    }

    /** Shared D12 strict-leader predicate. Condition 2 excludes barred players
     *  from candidate and tie-blocking comparisons; condition 1 compares all. */
    private boolean strictlyLeads(GameState state, Player p,
                                  boolean ignoreMajorAgendaPlayers) {
        for (Player q : state.getPlayers()) {
            if (q == p || q.hasForfeited()) continue;
            if (ignoreMajorAgendaPlayers) {
                AgendaCard agenda = q.getAgenda();
                if (agenda != null && !agenda.isFaceDown() && agenda.isMajorAgenda()) continue;
            }
            if (q.getInfluence() >= p.getInfluence()) return false;
        }
        return true;
    }

    // ── Legality checks ──────────────────────────────────────────────────────

    public boolean canInitiateConflict(Player p, ConflictCard c, GameState state) {
        return canInitiateConflict(p, c, null, state);
    }

    /**
     * B5-0336 overload: initiation with a declared target. Conflicts whose
     * participation carries requiresTarget:true are ILLEGAL to initiate
     * without one (proposal §3.2; rulebook Conflict round step 2, §III).
     * The pre-B5-0336 3-arg form delegates with target null, which keeps
     * every existing caller and test behaving exactly as before.
     */
    public boolean canInitiateConflict(Player p, ConflictCard c, Player target,
                                       GameState state) {
        if (p.isPassed()) return false;
        if (p.getActionsLeft() <= 0) return false;
        if (state.hasInitiatedConflictThisTurn(p)) return false;   // B5-0302
        if (!p.getHand().contains(c)) return false;
        if (!c.getFaction().isPlayableBy(p.getFaction())) return false;
        Participation part = c.getParticipation();
        if (part != null && part.isRequiresTarget() && target == null) return false;
        return true;
    }

    public boolean canPlayCard(Player p, Card c) {
        if (!p.getHand().contains(c)) return false;
        return c.getFaction().isPlayableBy(p.getFaction());
    }

    /** B5-0365: contingencies may only attach to a valid in-play host controlled by the player. */
    public boolean canPlayContingency(Player p, ContingencyCard contingency,
                                      Card host) {
        if (p == null || contingency == null || host == null
                || p.isPassed() || p.getActionsLeft() <= 0
                || !p.getHand().contains(contingency)
                || !contingency.getFaction().isPlayableBy(p.getFaction())
                || !contingency.canTarget(host)) return false;
        return controlsInPlay(p, host);
    }

    private boolean controlsInPlay(Player p, Card host) {
        if (p.getAmbassador() == host || p.getInnerCircle().contains(host)
                || p.getSupportingRole().contains(host) || p.getFleets().contains(host)
                || p.getLocations().contains(host) || p.getGroups().contains(host)
                || p.getEnhancements().contains(host) || p.getAgenda() == host) return true;
        return false;
    }

    /** Reveal trigger surface: only the player who placed it may reveal it. */
    public boolean canRevealContingency(Player p, ContingencyCard contingency) {
        return p != null && contingency != null && contingency.getPlayedBy() == p
                && !contingency.isRevealed() && contingency.getPlacedUnder() != null;
    }

    public boolean initiatorWon(Conflict resolved, Player winner) {
        return resolved.getInitiator() == winner;
    }

    public boolean canPlayAftermath(Player p, AftermathCard a,
                                    Conflict resolved, boolean initiatorWon) {
        // Pre-B5-0338 form: self-target (the engine played aftermaths on the
        // playing player himself) and no D4 registry check. Kept as a
        // delegate so every existing caller and test behaves exactly as
        // before; the live play site uses the 6-arg form.
        return canPlayAftermath(p, a, resolved, initiatorWon, p, null);
    }

    /**
     * B5-0338: aftermath play with an explicit target. Play conditions
     * (Won/Lost/Participant, conflict type) are still evaluated for the
     * PLAYING player; the rulebook's target rule (audit D2) is evaluated
     * for the target — Aftermath Cards / Participant: aftermaths "may
     * normally be played only upon the faction that initiated the just
     * resolved conflict... if 'Participant' is one of the Play Conditions,
     * then appropriate aftermath cards may target cards in play for any
     * faction that either Supported, Opposed or Attacked". The engine
     * represents Supported/Opposed/Attacked as the resolved conflict's
     * participants ("Attacked" has no distinct model representation —
     * recorded). D4: one of each named aftermath per target, enforced via
     * the GameState registry when a state is supplied.
     */
    public boolean canPlayAftermath(Player p, AftermathCard a, Conflict resolved,
                                    boolean initiatorWon, Player target,
                                    GameState state) {
        if (!p.getHand().contains(a)) return false;
        boolean participated = resolved.getParticipants().contains(p);
        if (!a.isEligible(initiatorWon, participated, resolved.getConflictType()))
            return false;
        if (target == null) return false;
        if (!a.isParticipantCondition()) {
            if (target != resolved.getInitiator()) return false;   // D2 normal rule
        } else if (!resolved.getParticipants().contains(target)) {
            return false;   // D2 Participant: supported/opposed/attacked only
        }
        if (state != null && !state.canAttachAftermath(a, target)) return false;   // D4
        return true;
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
        boolean freeParticipant = CardEffects.participantWaiver(conflict)
                == CardEffects.WaiverEffect.FREE_PARTICIPANT;
        if (freeParticipant) {
            // Non-Aligned Support grants one ready Non-Aligned fleet entry;
            // it does not consume influence or rotate the fleet.
            for (FleetCard fleet : p.getFleets()) {
                if (fleet.getFaction() == Faction.NON_ALIGNED
                        && !fleet.isRotated() && conflict.canCommitCard(p, fleet)) {
                    conflict.commitCard(p, fleet, support);
                    break;
                }
            }
            state.log(p.getName() + " joins the conflict as " + side
                    + " under its free-participant waiver.");
        } else {
            state.log(p.getName() + " joins the conflict as " + side + ".");
        }
    }

    /**
     * An attack uses the same conflict ability as the target. The attacker
     * must be controlled by an existing participant, ready and unneutralized;
     * the target must already participate and belong to another faction.
     * Conflict.canCommitCard is the participation-restriction authority for
     * the newly participating attacker.
     */
    public boolean canAttackConflictParticipant(Player p, Card attacker,
                                                Card target, Conflict conflict) {
        if (p == null || attacker == null || target == null || conflict == null) return false;
        if (conflict.isResolved() || !conflict.getParticipants().contains(p)) return false;
        if (!p.controlsCard(attacker) || attacker == target) return false;
        if (attacker.isRotated() || attacker.isFaceDown()
                || !attacker.canActAfterNeutralization()) return false;
        if (conflict.isParticipantCard(attacker)) return false;
        if (!conflict.isParticipantCard(target) || conflict.isLeaderOfParticipantFleet(target)) return false;
        target.reconcileDamage();
        if (target.isNeutralized() || target.isFaceDown()) return false;
        Player targetOwner = ownerOfCard(target, conflict);
        if (targetOwner == null || targetOwner.getFaction() == p.getFaction()) return false;
        if (attacker.getPrimaryStatValue(conflict.getConflictType()) <= 0) return false;
        return conflict.canCommitCard(p, attacker);
    }

    /** Executes simultaneous attack damage, then rotates/commits the attacker. */
    public boolean executeAttackConflictParticipant(Player p, Card attacker,
                                                    Card target, Conflict conflict,
                                                    GameState state) {
        if (!canAttackConflictParticipant(p, attacker, target, conflict)) return false;
        ConflictType ability = conflict.getConflictType();
        int attackDamage = attacker.getAttackDamage(ability);
        int returnDamage = target.getAttackDamage(ability);
        boolean support = conflict.isSupporting(p);
        if (!conflict.commitCard(p, attacker, support)) return false;
        conflict.markAttackOccurred(); // B5-0376 Phase C: attack resolves → contested
        attacker.rotate();
        int targetOverflow = target.applyDamage(attackDamage);
        int attackerOverflow = attacker.applyDamage(returnDamage);
        state.log(attacker.getTitle() + " attacks " + target.getTitle()
                + " using " + ability + " (" + attackDamage + " damage; "
                + returnDamage + " damage returned)." );
        if (target.isNeutralized()) state.log(target.getTitle() + " is neutralized"
                + (targetOverflow > 0 ? " (" + targetOverflow + " severe damage)." : "."));
        if (attacker.isNeutralized()) state.log(attacker.getTitle() + " is neutralized"
                + (attackerOverflow > 0 ? " (" + attackerOverflow + " severe damage)." : "."));
        return true;
    }

    private Player ownerOfCard(Card card, Conflict conflict) {
        for (Player candidate : conflict.getParticipants())
            if (candidate.controlsCard(card)) return candidate;
        return null;
    }

    // ── Fleet leadership (B5-0337; rulebook Action Details, Support or
    //    Oppose / audit D5) ─────────────────────────────────────────────────

    /**
     * True when character ch may rotate to lead fleet fl for player p
     * (rulebook: "one character per fleet may rotate to add his Leadership
     * Ability to the Military Ability of any fleet"): ch is p's ready
     * (unrotated, face-up) Inner Circle or supporting-role character, fl is
     * p's unrotated fleet, and fl has no leader yet. The check lives here;
     * the player-facing ACTION plumbing is the lead-fleet slice of B5-0345.
     */
    public boolean canLeadFleet(Player p, CharacterCard ch, FleetCard fl) {
        if (p == null || ch == null || fl == null) return false;
        if (!p.getInnerCircle().contains(ch)
                && !p.getSupportingRole().contains(ch)) return false;
        if (!p.getFleets().contains(fl)) return false;
        if (ch.isRotated() || ch.isFaceDown() || !ch.canActAfterNeutralization()) return false;
        if (fl.isRotated() || !fl.canActAfterNeutralization()) return false;
        if (fl.getLeader() != null) return false;   // one leader per fleet
        return true;
    }

    /**
     * Rotates ch to lead fl, adding his Leadership to the fleet's effective
     * Military until startRound clears the relation. Leading is a rotation,
     * not a zone change and not an influence spend.
     */
    public void executeLeadFleet(Player p, CharacterCard ch, FleetCard fl,
                                 GameState state) {
        if (!canLeadFleet(p, ch, fl)) {
            state.log(p.getName() + " cannot lead "
                      + (fl == null ? "(null fleet)" : fl.getTitle()) + " with "
                      + (ch == null ? "(null character)" : ch.getTitle()) + ".");
            return;
        }
        ch.rotate();
        fl.setLeader(ch);
        state.log(ch.getTitle() + " leads " + fl.getTitle()
                  + " (Military now " + fl.getEffectiveMilitary() + ").");
    }

    // ── Ambassador's assistant (B5-0339; rulebook §IV) ─────────────

    /**
     * True when ch may use an assistant ability for p's ambassador
     * (rulebook §IV "Your Ambassador's Assistant": rotate the assistant to
     * give the ambassador +1 Diplomacy/Intrigue/Leadership while he remains
     * rotated, or let the ambassador sponsor 1 influence cheaper later that
     * turn; the assistant must be "ready and unneutralized", and he is
     * "attached to a specific ambassador"). The model has no card-ownership
     * graph, so a player's supporting-role assistant assists that player's
     * own ambassador (recorded in DECISIONS).
     */
    public boolean canUseAssistant(Player p, CharacterCard ch, CharacterCard amb) {
        if (p == null || ch == null || amb == null) return false;
        if (!p.getSupportingRole().contains(ch)) return false;
        if (ch.isRotated() || ch.isFaceDown() || !ch.canActAfterNeutralization()) return false;
        if (p.getAmbassador() == null || p.getAmbassador() != amb) return false;
        return true;
    }

    /**
     * Rotates the assistant to boost the ambassador: +1 Diplomacy, Intrigue
     * and Leadership computed by CharacterCard.getPrimaryStatValue while the
     * bonus flag is up — it expires with the round (startRound), i.e. while
     * the assistant remains rotated. The flag is binary: a second assisting
     * assistant does not stack (rulebook silent on stacking; conservative).
     */
    public void executeAssistantAbilityBoost(Player p, CharacterCard ch,
                                             GameState state) {
        CharacterCard amb = (p == null) ? null : p.getAmbassador();
        if (!canUseAssistant(p, ch, amb)) {
            state.log(p.getName() + " cannot use "
                      + (ch == null ? "(null)" : ch.getTitle()) + " as an assistant.");
            return;
        }
        ch.rotate();
        amb.setAssistantBonus(true);
        state.log(ch.getTitle() + " assists " + amb.getTitle()
                  + " (+1 Diplomacy/Intrigue/Leadership while rotated).");
    }

    /**
     * Rotates the assistant so the ambassador may sponsor 1 influence
     * cheaper later this turn (§IV); the discount is consumed by the first
     * recruit and expires with the turn.
     */
    public void executeAssistantSponsorDiscount(Player p, CharacterCard ch,
                                                GameState state) {
        CharacterCard amb = (p == null) ? null : p.getAmbassador();
        if (!canUseAssistant(p, ch, amb)) {
            state.log(p.getName() + " cannot use "
                      + (ch == null ? "(null)" : ch.getTitle()) + " as an assistant.");
            return;
        }
        ch.rotate();
        p.grantSponsorDiscount(1);
        state.log(ch.getTitle() + " assists " + amb.getTitle()
                  + " (sponsor 1 influence cheaper this turn).");
    }

    // ── Rotate-for-effect (B5-0366; B5-0345 Tier-2 #5, rulebook §IV) ────────────

    /**
     * B5-0366: the two player-chosen assistant effects, promoted from the
     * wired B5-0339 special cases to a generic rotate-for-effect vocabulary
     * (the kind enum lives on GameAction so no model→engine import is
     * needed). Both ride the existing canUseAssistant gate (ready,
     * unneutralized, supporting, own ambassador) and expire at the round
     * boundary (B5-0339 startRound). Future Vir-style rotate effects reuse
     * this vocabulary with new execute branches; they do NOT touch the
     * B5-0339 flags.
     */

    /**
     * True when ch may rotate for the named effect now: the effect kind is
     * known and the B5-0339 readiness gate passes (ready supporting assistant,
     * own ambassador). One gate for every kind today — per-kind legality
     * diverges here, not in the executors, when future effects land.
     */
    public boolean canUseRotateEffect(Player p, CharacterCard ch,
                                      CharacterCard amb,
                                      GameAction.RotateEffectKind kind) {
        if (kind == null) return false;
        return canUseAssistant(p, ch, amb);
    }

    /**
     * Rotates the assistant for the named effect. Refusals log and leave
     * state unchanged (same contract as the B5-0339 executors they delegate
     * to — the rotation IS the cost, consumed even on an already-flagged
     * ambassador for the boost, exactly as B5-0339 behaved).
     */
    public void executeRotateEffect(Player p, CharacterCard ch,
                                    GameAction.RotateEffectKind kind,
                                    GameState state) {
        if (kind == null) {
            state.log(p.getName() + " cannot use a rotate effect (unknown kind).");
            return;
        }
        if (kind == GameAction.RotateEffectKind.USE_ABILITY_BOOST) {
            executeAssistantAbilityBoost(p, ch, state);
        } else {
            executeAssistantSponsorDiscount(p, ch, state);
        }
    }

    // ── B5-0336: mandatory participation enforcement ──────────────────────

    /**
     * Enforces the mandatory-commit dimensions of a conflict's participation
     * restriction BEFORE resolution (proposal §3.2): mustCommitAmbassador,
     * allPlayersMustCommit, mustTakeSide. Players not yet participating are
     * pulled in on the opposition side — the same side AI joiners take
     * (B5-0309 join path) — and mandated ambassadors commit via
     * Conflict.commitMandatory, which bypasses card-kind/quota filters
     * because the mandate itself names the commitment. Eligibility still
     * respects the "players" gate and forfeits. Null/absent participation
     * is a no-op (§3.4 open default).
     */
    public void enforceMandatoryParticipation(Conflict conflict, GameState state) {
        Participation part = conflict.getCard().getParticipation();
        if (part == null || !part.isRestricted()) return;

        List<Player> eligible = new ArrayList<Player>();
        for (Player p : state.getPlayers()) {
            if (p.hasForfeited()) continue;
            if (conflict.canJoinConflict(p)) eligible.add(p);
        }

        if (part.isMustTakeSide() || part.hasAllPlayersMustCommit()
                || part.isMustCommitAmbassador()) {
            for (Player p : eligible) {
                if (p == conflict.getInitiator()) continue;   // already participates
                if (conflict.getParticipants().contains(p)) continue;
                if (conflict.addParticipant(p, false)) {
                    state.log(p.getName() + " is compelled to participate in "
                            + conflict.getCard().getTitle() + " (opposes).");
                }
            }
        }

        boolean commitMandate = part.isMustCommitAmbassador()
                || part.hasAllPlayersMustCommit() || part.isMustTakeSide();
        if (commitMandate) {
            for (Player p : conflict.getParticipants()) {
                Card amb = p.getAmbassador();
                if (amb == null || amb.isFaceDown()) continue;
                if (conflict.getCommittedCards(p).contains(amb)) continue;
                if (part.hasAllPlayersMustCommit()
                        && part.getAllPlayersCardType() != CardType.CHARACTER) {
                    // The mandate names a kind the engine cannot auto-commit
                    // (no card-picking semantics yet) — loud, never silent.
                    System.err.println("B5-0336: allPlayersMustCommit "
                            + part.getAllPlayersCardType() + " not auto-satisfiable for "
                            + conflict.getCard().getTitle());
                    continue;
                }
                if (conflict.commitMandatory(p, amb, conflict.isSupporting(p))) {
                    state.log(p.getName() + " commits their Ambassador to "
                            + conflict.getCard().getTitle() + " (mandatory).");
                }
            }
            if (part.hasAllPlayersMustCommit() && part.getAllPlayersCount() > 1) {
                System.err.println("B5-0336: allPlayersMustCommit count "
                        + part.getAllPlayersCount() + " exceeds engine auto-commit"
                        + " (ambassador only) for " + conflict.getCard().getTitle());
            }
        }
    }

    // ── Mercenary control (B5-0395; rulebook §Mercenaries :735–:741) ────────
    // Bidding spends APPLIED-POOL influence only (applied_pool, per D9 —
    // never the influence Rating). A bid is a turn-ordered ACTION: one action
    // per player per visit, cumulative per player per turn, capped by what the
    // pool holds at the moment of each bid. Control is decided at the
    // MERCENARY phase as the single highest cumulative total — a tie crowns
    // nobody (DECISIONS: conservative default, D12 discipline; the rulebook
    // is silent on ties).

    /** A bid is legal when the card is a mercenary that is offered (i.e. in
     *  play), the amount is positive, and the player's applied pool can cover
     *  it NOW (cumulative bids may drain the pool across the round). */
    public boolean canBidOnMercenary(Player p, Card merc, int amount, GameState state) {
        if (p == null || merc == null || state == null) return false;
        if (amount <= 0) return false;
        if (!merc.isMercenary() || !state.isMercenaryInPlay(merc)) return false;
        return p.getAppliedPool() >= amount;
    }

    /** Applies the bid: records it on merc, then spends `amount` from p's
     *  applied pool. Record-then-spend: canBidOnMercenary has already proven
     *  afford-now and offered/positive, and nothing else can change the pool
     *  between the two calls (single-threaded controller), so a mid-way
     *  failure is impossible by construction — if it ever happens, it is
     *  logged loudly, never silently refunded. */
    public boolean executeBidOnMercenary(Player p, Card merc, int amount, GameState state) {
        if (!canBidOnMercenary(p, merc, amount, state)) return false;
        if (!state.placeMercenaryBid(merc, p, amount)) {
            System.err.println("B5-0395: bid on " + merc.getId() + " could not be "
                    + "recorded despite legality checks — aborting without "
                    + "charging the pool.");
            return false;
        }
        if (!p.applyInfluence(amount)) {   // defensive: pool changed under us
            state.placeMercenaryBidRollback(merc, p, amount);
            return false;
        }
        state.log(p.getName() + " bids " + amount + " influence to control "
                + merc.getTitle() + " (total " + state.getMercenaryBid(merc, p) + ").");
        return true;
    }

    // ── Round start ──────────────────────────────────────────────────────────

    public void startRound(GameState state) {
        for (Player p : state.getPlayers()) {
            p.resetNeutralizedTurnLocks();
            p.restoreAppliedPool();
            p.resetActions();
            if (p.getAmbassador() != null) p.getAmbassador().unrotate();
            for (CharacterCard ch : p.getInnerCircle())  ch.unrotate();
            for (CharacterCard ch : p.getSupportingRole()) ch.unrotate();
            // B5-0339: assistant effects expire with the round — the ability
            // bonus lasts while the assistant remains rotated, and the
            // sponsor discount dies with the turn.
            for (CharacterCard ch : p.getInnerCircle())     ch.setAssistantBonus(false);
            for (CharacterCard ch : p.getSupportingRole())  ch.setAssistantBonus(false);
            p.consumeSponsorDiscount(p.getSponsorDiscount());
            for (FleetCard fl : p.getFleets()) {
                fl.unrotate();
                fl.setLeader(null);   // B5-0337: the leadership relation expires each round
            }
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
        // B5-0395: mercenary control is per-turn — bids and controllers die
        // with the round; the offer list (a game-setup surface) persists.
        state.clearMercenaryState();
        state.log("=== Round " + state.getRoundNumber() + " begins ===");
    }

    // ── B5-0437: station end-of-round maintenance ────────────────────────────
    /** B5-0437 (0428 proposal §Sources/§Sinks), called by GameController at
     *  the END of each round, BEFORE advanceRound() resets the source marker
     *  (state mutation order matters: the marker must still be readable
     *  here). Sequence: (1) presence-bleed source — a Vorlon player holding
     *  any captured location raises vorlon influence 1 (Shadow has no faction
     *  in the enum, B5-0354, so shadow presence-bleed waits for a card hook);
     *  (2) capture-source decay sink — if NO source fired this round (neither
     *  capture nor bleed), all three ratings decay 1 toward the neutral
     *  baseline; small step so a burst of captures is not erased at once.
     *  Rounds with a capture or a bleed keep their rating (no-source guard). */
    public void applyEndOfRoundStation(GameState state) {
        Babylon5Station stn = state.getStation();
        boolean sourceFired = state.isStationSourceFired();
        for (Player p : state.getPlayers()) {
            if (p.getFaction() == Faction.VORLON) {
                for (Card c : p.getLocations()) {
                    if (c instanceof LocationCard) {
                        LocationCard lc = (LocationCard) c;
                        if (lc.getCapturedBy() != null) {
                            stn.gainVorlonInfluence(1);
                            sourceFired = true;
                            state.log("Station: Vorlon presence-bleed raises "
                                    + "vorlon influence to "
                                    + stn.getVorlonInfluence() + ".");
                            break;
                        }
                    }
                }
            }
        }
        // The marker's only consumer is the decay decision above; the
        // boundary discharges it so the next round starts clean (capture and
        // bleed hooks re-set it the moment a source fires again).
        state.setStationSourceFired(false);
        if (!sourceFired) {
            if (stn.getInfluence() > 0)        stn.loseInfluence(1);
            if (stn.getShadowInfluence() > 0)  stn.loseShadowInfluence(1);
            if (stn.getVorlonInfluence() > 0)  stn.loseVorlonInfluence(1);
        }
    }

    // ── Draw phase ───────────────────────────────────────────────────────────

    public void drawPhase(GameState state) {
        for (Player p : state.getPlayers()) {
            p.drawCards(1);
        }
    }
}
