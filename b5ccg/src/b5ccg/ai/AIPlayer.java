package b5ccg.ai;

import b5ccg.engine.RulesEngine;
import b5ccg.model.*;
import b5ccg.model.enums.*;
import java.util.*;

/** AI player for three difficulty tiers: EASY, MEDIUM, HARD.
 *
 *  EASY:   Random legal action.
 *  MEDIUM: Greedy — prefers actions that maximise immediate Influence gain.
 *  HARD:   Evaluative — scores board state, uses opponent awareness,
 *          targets the leading player, protects against threats.
 *
 *  Build Influence (rulebook V): offered as a legal action whenever this
 *  player's Influence Rating is 9 or below and at least one Inner Circle
 *  character is unrotated. Scored as a positive value by MEDIUM/HARD.
 *
 *  Promote Character (rulebook VI., B5-0321): offered for every affordable,
 *  ready supporting character, rotating an unrotated IC member. This is what
 *  makes Build Influence reachable: recruiting alone never grows the Inner
 *  Circle, and Build Influence needs IC characters to rotate.
 *
 *  Lead a Fleet (B5-0362; B5-0345 Tier-1 #1): every legal (leader, fleet)
 *  pair is offered as its own GameAction.LEAD_FLEET through canLeadFleet —
 *  own ready Inner-Circle or supporting character, own unrotated unled fleet.
 *  MEDIUM/HARD score it modestly by the unused Leadership it seats; HARD
 *  additionally weighs the projected leader Military into its own
 *  initiative-conflict win-probability calculation. EASY picks uniformly
 *  from the same list (difficulty contract untouched).
 *
 *  Cost-aware scoring (B5-0202 Finding 6 / B5-0324): MEDIUM and HARD subtract
 *  a card's influence cost (B5-0323 plumbing) from its positional value, so
 *  once the data carries costs the AI prefers cheaper plays. All costs are 0
 *  until the data backfill, so today's ordering is unchanged.
 *
 *  Agenda/aftermath/event scoring (B5-0344): agendas score by how close the
 *  owner's current board state is to the agenda's own win condition (the
 *  INFLUENCE_20/MILITARY_SUPREMACY/MOST_INNER_CIRCLE vocabulary already
 *  implemented in AgendaCard.isConditionMet), conflicts anticipate the
 *  aftermaths a win would unlock, aftermath cards are no longer voluntarily
 *  discarded via the generic play path (the controller auto-plays eligible
 *  ones after each conflict — an un-playable aftermath is a held resource,
 *  not a dead card), and events get a behind-the-leader catch-up bonus
 *  matching the rulebook's underdog reading of event timing. EASY stays
 *  random (the difficulty contract; B5-0351 owns its verification).
 *
 *  Mercenary bidding (B5-0403; B5-0395 engine, rulebook §Mercenaries): every
 *  offered mercenary gets one bid offer per visit — the minimal increment
 *  that puts this player strictly ahead of the best competing cumulative
 *  total (ties crown nobody) — when the applied pool can cover it. MEDIUM and
 *  HARD score the bid by win probability of the projected total versus its
 *  cost; bids spend the D9 applied pool only, never the Rating.
 */
public class AIPlayer {

    /** Stateless helper for legality/cost checks inside the offer builder. */
    private final RulesEngine rules = new RulesEngine();

    private final Player       player;
    private final AIDifficulty difficulty;
    private final Random       rng = new Random();

    public AIPlayer(Player player, AIDifficulty difficulty) {
        this.player     = player;
        this.difficulty = difficulty;
    }

    public Player       getPlayer()     { return player; }
    public AIDifficulty getDifficulty() { return difficulty; }

    // ── Main decision entry point ─────────────────────────────────────────────

    public GameAction chooseAction(GameState state, Player p) {
        List<GameAction> legal = buildLegalActions(state, p);
        if (legal.isEmpty()) return GameAction.pass();

        switch (difficulty) {
            case EASY:   return easyChoose(legal, p);
            case MEDIUM: return mediumChoose(legal, state, p);
            case HARD:   return hardChoose(legal, state, p);
            default:     return GameAction.pass();
        }
    }

    public boolean shouldJoinConflict(GameState state, Player p, Conflict conflict) {
        return decideJoinSide(state, p, conflict) != 0;
    }

    /**
     * B5-0343: side-aware join decision. Returns +1 (support), −1 (oppose)
     * or 0 (abstain). Replaces the pre-B5-0343 behavior where the boolean
     * shouldJoinConflict fed a hardcoded opposition commit in the controller
     * (the "always-oppose default" the audit flagged). Side logic by tier:
     *
     * EASY:   random side when joining at all (unchanged join rate).
     * MEDIUM: opposes when its committed total would OUTWEIGH the initiator's
     *         (the B5-0309 win rule: opposition wins ties), supports when its
     *         total is less than half the initiator's (free-ride on a
     *         winning conflict), abstains in the middle band (previously the
     *         join band — recorded as a deliberate tightening).
     * HARD:   as MEDIUM, plus: never strengthens a leading player's conflict
     *         (supports only against the leader), and weighs the military
     *         loss rule (a >= 3 gap damages the LOSER's ambassador), so it
     *         abstains from hopeless opposition it cannot win or tilt.
     */
    public int decideJoinSide(GameState state, Player p, Conflict conflict) {
        switch (difficulty) {
            case EASY:   return rng.nextBoolean()
                                ? (rng.nextBoolean() ? 1 : -1)   // join, random side
                                : 0;                             // abstain (old ~50% rate)
            case MEDIUM: return mediumJoinSide(state, p, conflict);
            case HARD:   return hardJoinSide(state, p, conflict);
            default:     return 0;
        }
    }

    private int mediumJoinSide(GameState state, Player p, Conflict conflict) {
        int myTotal  = p.conflictTotal(conflict.getConflictType());
        int oppTotal = conflict.getInitiator().conflictTotal(conflict.getConflictType());
        if (myTotal > oppTotal) return -1;   // outweigh: oppose and win (ties included)
        if (myTotal * 2 < oppTotal) return 1; // free-ride: support a sure winner
        return 0;
    }

    private int hardJoinSide(GameState state, Player p, Conflict conflict) {
        Player initiator = conflict.getInitiator();
        Player leader    = leadingPlayer(state, p);
        int myTotal  = p.conflictTotal(conflict.getConflictType());
        int oppTotal = initiator.conflictTotal(conflict.getConflictType());

        // A leading player's conflict is never strengthened (B5-0343):
        // oppose it when we can outweigh, otherwise abstain — supporting a
        // leader's win only entrenches them.
        if (initiator == leader) {
            if (myTotal > oppTotal) return -1;
            return 0;
        }

        // Military loss rule (B5-0309): a loser whose total trails by >= 3
        // takes ambassador damage. Do not oppose unless the opposition would
        // actually win; prefer support when the initiator is a sure winner.
        if (conflict.getConflictType() == ConflictType.MILITARY) {
            if (myTotal > oppTotal) return -1;
            if (myTotal * 2 < oppTotal) return 1;
            return 0;
        }

        // Non-military: outweigh-oppose, free-ride support, else the old
        // HARD band (strong-initiator caution).
        if (myTotal > oppTotal) return -1;
        if (myTotal * 2 < oppTotal) return 1;
        if (initiator.getInfluence() >= p.getInfluence() - 4) return 0;
        return rng.nextBoolean() ? 1 : -1;
    }

    // ── Legal action builder ──────────────────────────────────────────────────

    private List<GameAction> buildLegalActions(GameState state, Player p) {
        List<GameAction> actions = new ArrayList<GameAction>();
        actions.add(GameAction.pass());

        for (Card c : p.getHand()) {
            if (!c.getFaction().isPlayableBy(p.getFaction())) continue;

            if (c instanceof ConflictCard) {
                if (state.getActiveConflict() == null
                        && !state.hasInitiatedConflictThisTurn(p)) { // B5-0302
                    for (Player target : state.getPlayers()) {
                        if (target != p) {
                            actions.add(GameAction.initiateConflict(c, target));
                        }
                    }
                }
            } else if (c instanceof CharacterCard) {
                if (rules.canRecruit(p, (CharacterCard) c)) { // B5-0323: affordability
                    actions.add(GameAction.recruitCharacter(c));
                }
            } else if (c instanceof AftermathCard) {
                // B5-0344: aftermaths are conflict-resolved resources. The
                // controller auto-plays eligible ones for AIs after each
                // conflict (resolveCurrentConflict); a voluntary play here
                // would just discard the card through the generic branch
                // (an aftermath has no standalone resolution). Hold it.
            } else if (c instanceof ContingencyCard) {
                // B5-0365: ContingencyCard has its own dedicated offer loop
                // below (playContingency). Skip here — the generic playCard
                // path is not valid for contingencies.
            } else if (c instanceof AgendaCard) {
                // B5-0364: agenda cards are only offered through the generic
                // playCard path when the slot is empty (sponsor). When an
                // agenda is already in play, canSponsorAgenda would refuse
                // the action at execution time; the agenda lifecycle actions
                // (discard/replace/reveal) cover those cases instead.
                if (p.getAgenda() == null) {
                    actions.add(GameAction.playCard(c));
                }
            } else {
                actions.add(GameAction.playCard(c));
            }
        }

        // ── Damage subsystem (B5-0370/B5-0371) ──────────────────────────────
        // Attacks are offered only to a participant against an opposing
        // committed card. The engine legality gate remains authoritative.
        if (state.getActiveConflict() != null && !state.getActiveConflict().isResolved()
                && state.getActiveConflict().getParticipants().contains(p)) {
            Conflict active = state.getActiveConflict();
            List<Card> opposing = new ArrayList<Card>();
            for (Player participant : active.getParticipants()) {
                if (participant == p || participant.getFaction() == p.getFaction()) continue;
                opposing.addAll(active.getCommittedCards(participant));
            }
            for (Card attacker : p.getSupportingRole()) {
                for (Card target : opposing) {
                    if (rules.canAttackConflictParticipant(p, attacker, target, active)) {
                        actions.add(GameAction.attackConflictParticipant(attacker, target));
                    }
                }
            }
        }

        // Heal/repair actions are deliberately offered before generic plays so
        // damaged assets are restored when the action is still legal.
        for (CharacterCard ch : p.getInnerCircle()) {
            if ((ch.isNeutralized() || ch.getDamageTokens() > 0) && rules.canHealCharacter(p, ch)) {
                actions.add(GameAction.healCharacter(ch));
            }
        }
        for (CharacterCard ch : p.getSupportingRole()) {
            if ((ch.isNeutralized() || ch.getDamageTokens() > 0) && rules.canHealCharacter(p, ch)) {
                actions.add(GameAction.healCharacter(ch));
            }
        }
        for (Card c : p.getFleets()) {
            if (rules.canRepairCard(p, c)) actions.add(GameAction.repairCard(c));
        }
        for (Card c : p.getLocations()) {
            if (rules.canRepairCard(p, c)) actions.add(GameAction.repairCard(c));
        }

        // ── Declare War Conflict (B5-0376) ──────────────────────────────────
        // Offered only while a declaration is legal (at war, action slot free,
        // not passed). RACE_TARGET wars target each enemy faction at war with
        // p; LOCATION_TARGET wars target each location held by such an enemy.
        // Engine legality remains authoritative; the score reflects capture or
        // swing value per proposal §3.6 (modest, B5-0301 house style).
        if (rules.canDeclareWarConflict(p, state)) {
            for (Player enemy : state.getPlayers()) {
                if (enemy == p
                        || enemy.getFaction() == null
                        || enemy.getFaction() == p.getFaction()) continue;
                if (!state.isAtWar(p.getFaction(), enemy.getFaction())) continue;
                actions.add(GameAction.declareWarConflict(WarKind.RACE_TARGET,
                        enemy, null));
                for (LocationCard loc : enemy.getLocations()) {
                    actions.add(GameAction.declareWarConflict(WarKind.LOCATION_TARGET,
                            null, loc));
                }
            }
        }

        // ── Build Influence ────────────────────────────────────────────────
        // Rulebook V.: "Rotate to Build Influence"
        //   - Faction Influence Rating must be <= 9 (rulebook VI.: >= 10 cannot).
        //   - An unrotated Inner Circle character serves as the leader.
        if (rules.canBuildInfluence(p)) {
            for (CharacterCard ch : p.getInnerCircle()) {
                if (!ch.isRotated() && ch.canActAfterNeutralization()) {
                    actions.add(GameAction.buildInfluence(ch));
                }
            }
        }

        // ── Promote Character (B5-0321, rulebook VI.) ──────────────────────
        for (CharacterCard ch : p.getSupportingRole()) {
            if (rules.canPromote(p, ch)) {
                CharacterCard leader = unrotatedInnerCircleMember(p);
                if (leader != null) {
                    actions.add(GameAction.promoteCharacter(ch, leader));
                }
            }
        }

        // ── Lead a Fleet (B5-0362; B5-0345 Tier-1 #1, rulebook Support or
        // Oppose: "one character per fleet may rotate to add his Leadership").
        // Each legal (leader, fleet) pair is its own action — one pair, one
        // action; canLeadFleet is the single legality source (own ready
        // IC-or-supporting character, own unrotated unled fleet). Both offer
        // paths honor it: this full build, and the passed/out-of-actions
        // early return above, which offers no actions at all.
        for (FleetCard fl : p.getFleets()) {
            for (CharacterCard ch : p.getInnerCircle()) {
                if (rules.canLeadFleet(p, ch, fl)) {
                    actions.add(GameAction.leadFleet(ch, fl));
                }
            }
            for (CharacterCard ch : p.getSupportingRole()) {
                if (rules.canLeadFleet(p, ch, fl)) {
                    actions.add(GameAction.leadFleet(ch, fl));
                }
            }
        }

        // ── Rotate-for-effect (B5-0366; B5-0345 Tier-2 #5, rulebook §IV):
        // generic rotate-for-effect action promoting the B5-0339 assistant
        // paths to a player-chosen action. Each legal (assistant, ambassador,
        // effect-kind) triple is its own action; the gate is canUseRotateEffect
        // (delegates to canUseAssistant: ready, unneutralized, supporting,
        // own ambassador). Both RotateEffectKind values are offered.
        CharacterCard amb = p.getAmbassador();
        if (amb != null) {
            for (CharacterCard ch : p.getSupportingRole()) {
                for (GameAction.RotateEffectKind kind : GameAction.RotateEffectKind.values()) {
                    if (rules.canUseRotateEffect(p, ch, amb, kind)) {
                        actions.add(GameAction.useRotateEffect(ch, amb, kind));
                    }
                }
            }
        }

        // ── Mercenary bids (B5-0403; B5-0395 engine, rulebook §Mercenaries
        // :735–:741) ────────────────────────────────────────────────────────
        // One offer per offered mercenary per visit: the minimal increment
        // that puts p STRICTLY ahead of the best competing cumulative total
        // (ties crown nobody, so equal is not enough), when the applied pool
        // can cover it now. A player who already strictly leads does not
        // re-bid — extending a won auction only drains the pool; if a rival
        // overbids, the offer reappears on a later visit (passing is not
        // sticky under D6). EASY picks uniformly from the same offer list
        // (difficulty contract untouched).
        for (Card merc : state.getMercenaryOffers()) {
            int mine = state.getMercenaryBid(merc, p);
            int bestOther = bestOtherMercenaryBid(state, p, merc);
            if (mine > bestOther) continue;   // hold: the auction is already won
            int inc = bestOther - mine + 1;   // strictly outbid, or open at 1
            if (rules.canBidOnMercenary(p, merc, inc, state)) {
                actions.add(GameAction.bidOnMercenary(merc, inc));
            }
        }

        // ── Agenda lifecycle (B5-0364; B5-0345 Tier-1 #3) ──────────────────
        // DISCARD_AGENDA: non-Major agendas only (engine refuses Majors).
        if (rules.canDiscardAgenda(p)) {
            actions.add(GameAction.discardAgenda(p.getAgenda()));
        }
        // REVEAL_AGENDA: face-down sponsor-cost-hidden agenda activation
        // (rulebook :520).
        if (rules.canRevealAgenda(p)) {
            actions.add(GameAction.revealAgenda(p.getAgenda()));
        }
        // REPLACE_AGENDA: every hand agenda that could replace the current
        // (non-hidden) one. canReplaceAgenda is the single legality source
        // (face-down replacements refused, Major-to-non-Major refused, leader
        // must be a ready unrotated IC member).
        AgendaCard current = p.getAgenda();
        if (current != null && !current.isFaceDown()) {
            CharacterCard leader = unrotatedInnerCircleMember(p);
            if (leader != null) {
                for (Card c : p.getHand()) {
                    if (c instanceof AgendaCard && c.getFaction().isPlayableBy(p.getFaction())) {
                        AgendaCard repl = (AgendaCard) c;
                        if (rules.canReplaceAgenda(p, repl, leader)) {
                            actions.add(GameAction.replaceAgenda(repl, leader));
                        }
                    }
                }
            }
        }

        // ── Contingencies (B5-0365; B5-0345 Tier-2 #4) ─────────────────────
        // PLAY_CONTINGENCY: each hand contingency placed under a controlled
        // in-play host that satisfies canTarget. REVEAL_CONTINGENCY: each
        // un-revealed contingency already placed under a host.
        // The offer builder scans the player's in-play cards for hosts.
        for (Card c : p.getHand()) {
            if (c instanceof ContingencyCard) {
                ContingencyCard con = (ContingencyCard) c;
                if (!con.getFaction().isPlayableBy(p.getFaction())) continue;
                for (Card host : inPlayHosts(p)) {
                    if (rules.canPlayContingency(p, con, host)) {
                        actions.add(GameAction.playContingency(con, host));
                    }
                }
            }
        }
        // Reveal offers run once over placed contingencies (NOT per hand card —
        // two contingencies in hand would otherwise double every reveal offer).
        for (Card host : inPlayHosts(p)) {
            for (ContingencyCard placed : host.getContingencies()) {
                if (rules.canRevealContingency(p, placed)) {
                    actions.add(GameAction.revealContingency(placed));
                }
            }
        }

        return actions;
    }

    /** First unrotated Inner Circle character (the rotating leader), or null. */
    private CharacterCard unrotatedInnerCircleMember(Player p) {
        for (CharacterCard ch : p.getInnerCircle()) {
            if (!ch.isRotated() && ch.canActAfterNeutralization()) return ch;
        }
        return null;
    }

    /** All in-play cards controlled by p (ambassador + IC + supporting + fleets +
     *  locations + groups + enhancements + current agenda) — used to find
     *  valid contingency hosts (B5-0365). Immutable snapshot view.
     */
    private List<Card> inPlayHosts(Player p) {
        List<Card> hosts = new ArrayList<Card>();
        CharacterCard amb = p.getAmbassador();
        // The ambassador may also sit in the Inner Circle — avoid adding it twice
        // (a duplicate host would yield duplicate PLAY_CONTINGENCY / reveal offers).
        if (amb != null) hosts.add(amb);
        for (CharacterCard ch : p.getInnerCircle()) {
            if (ch != amb) hosts.add(ch);
        }
        hosts.addAll(p.getSupportingRole());
        hosts.addAll(p.getFleets());
        hosts.addAll(p.getLocations());
        hosts.addAll(p.getGroups());
        hosts.addAll(p.getEnhancements());
        AgendaCard ag = p.getAgenda();
        if (ag != null) hosts.add(ag);
        return hosts;
    }

    /** B5-0403: the highest cumulative mercenary bid on `merc` by any player
     *  other than p (0 when none) — the total a winning bid must strictly
     *  exceed (B5-0395 resolution: ties crown nobody). */
    private int bestOtherMercenaryBid(GameState state, Player p, Card merc) {
        int bestOther = 0;
        for (Player q : state.getPlayers()) {
            if (q == p) continue;
            int bid = state.getMercenaryBid(merc, q);
            if (bid > bestOther) bestOther = bid;
        }
        return bestOther;
    }

    // ── EASY ──────────────────────────────────────────────────────────────────

    private GameAction easyChoose(List<GameAction> legal, Player p) {
        if (legal.size() > 1 && rng.nextInt(10) < 3) return GameAction.pass();
        return legal.get(rng.nextInt(legal.size()));
    }

    // ── MEDIUM ────────────────────────────────────────────────────────────────

    private GameAction mediumChoose(List<GameAction> legal, GameState state, Player p) {
        GameAction best     = GameAction.pass();
        int        bestScore = -1;

        for (GameAction a : legal) {
            int score = scoreActionMedium(a, state, p);
            if (score > bestScore) { bestScore = score; best = a; }
        }
        return best;
    }

    private int scoreActionMedium(GameAction a, GameState state, Player p) {
        switch (a.getType()) {
            case INITIATE_CONFLICT:
                if (a.getCard() instanceof ConflictCard) {
                    ConflictCard cc = (ConflictCard) a.getCard();
                    int myTotal = p.conflictTotal(cc.getConflictType());
                    int oppTotal = a.getTarget() != null
                        ? a.getTarget().conflictTotal(cc.getConflictType()) : 0;
                    return myTotal > oppTotal
                        ? cc.getInfluenceReward() * 10 + (myTotal - oppTotal)
                        : -5;
                }
                return 0;
            case RECRUIT_CHARACTER: {
                // B5-0324: prefer cheaper recruits (floored at 0; the offer
                // gate already guarantees affordability).
                if (a.getCard() instanceof CharacterCard) {
                    CharacterCard ch = (CharacterCard) a.getCard();
                    return Math.max(0, 5 - rules.recruitCost(p, ch));
                }
                return 5;
            }
            case PLAY_CARD: {
                // B5-0324: positional value minus the card's influence cost
                // (zero until the data backfill — ordering unchanged today).
                // B5-0344: agendas are scored by win-condition proximity
                // (see agendaProximityScore), not by a flat base.
                if (a.getCard() instanceof AgendaCard) {
                    AgendaCard ag = (AgendaCard) a.getCard();
                    return Math.max(0, agendaProximityScore(state, p, ag)
                                       - a.getCard().getCost());
                }
                if (a.getCard() instanceof EventCard) {
                    return Math.max(0, 2 + eventCatchUpBonus(state, p)
                                       - a.getCard().getCost());
                }
                int base;
                if (a.getCard() instanceof LocationCard) base = 6;
                else if (a.getCard() instanceof GroupCard)    base = 4;
                else                                          base = 2;
                return Math.max(0, base - a.getCard().getCost());
            }
            case BUILD_INFLUENCE:
                // Positive value: pushing toward the Influence cap.
                // Score scales with how far below the cap we still are.
                int remaining = Math.max(0, 10 - p.getInfluence());
                return 3 + remaining; // 4..13
            case PROMOTE_CHARACTER:
                // B5-0321: builds the Inner Circle (more conflict power + the
                // deck-out buffer). Better early, when the IC-member cost term
                // is still small.
                return Math.max(1, 8 - p.getInnerCircle().size());
            case LEAD_FLEET: {
                // B5-0362: modest unused-leader value — seats Leadership that
                // currently contributes nothing (D5: characters never feed
                // MILITARY directly). Deliberately below the promote/build
                // peaks so leading is a good, not THE grab.
                if (!(a.getCard() instanceof FleetCard) || a.getLeader() == null) return 2;
                return 2 + p.effectiveStat(a.getLeader().getId(), StatKey.LEADERSHIP,
                        a.getLeader().getLeadership(), true);   // modest; includes active bonuses
            }
            case USE_ROTATE_EFFECT: {
                // B5-0366: modest value for the two player-chosen assistant
                // effects (rulebook §IV). The ability boost is worth a few
                // positional points (it adds +1 to up to 3 stats on the
                // ambassador); the sponsor discount is worth ~1 (it saves 1
                // influence on the next recruit). Below promote/build so the
                // AI leads with Inner-Circle growth first.
                if (!(a.getCard() instanceof CharacterCard) || a.getLeader() == null) return 1;
                GameAction.RotateEffectKind kind = a.getRotateKind();
                if (kind == GameAction.RotateEffectKind.USE_SPONSOR_DISCOUNT) return 1;
                // USE_ABILITY_BOOST: +1 Dip/Intrigue/Leadership on the ambassador
                // while the assistant stays rotated — worth a small positional bump.
                return 2;
            }
            case DISCARD_AGENDA:
                // B5-0364: agenda discard is rarely voluntary (Majors cannot
                // be discarded engine-side), so score it low — used only when
                // the current agenda is clearly worse than a replacement in hand.
                return 1;
            case REPLACE_AGENDA: {
                // B5-0364: agenda replacement — score by the gap between the
                // new agenda's proximity and zero (the current agenda's implicit
                // floor when it's being replaced). Costs one IC rotation.
                if (a.getCard() instanceof AgendaCard) {
                    return Math.max(0, agendaProximityScore(state, p, (AgendaCard) a.getCard()) - 1);
                }
                return 2;
            }
            case REVEAL_AGENDA:
                // B5-0364: agenda reveal — a hidden agenda has no effect until
                // revealed (:520), so this action converts a held resource into
                // an active win condition. Score by proximity (same reading as
                // agenda sponsor through PLAY_CARD).
                if (a.getCard() instanceof AgendaCard) {
                    return agendaProximityScore(state, p, (AgendaCard) a.getCard());
                }
                return 3;
            case PLAY_CONTINGENCY:
                // B5-0365: contingency placement — face-down resource under a
                // controlled host, waiting for its trigger. Modest value (it's
                // a future-proofing play, not an immediate effect).
                return 2;
            case REVEAL_CONTINGENCY:
                // B5-0365: contingency reveal — converts to an event-like effect
                // when the trigger is met. Score as an event (the B5-0365 design:
                // contingencies share the Event dispatcher on reveal).
                return 2;
            case PASS:
                return 0;
            case DECLARE_WAR_CONFLICT:
                // B5-0376: modest war-declaration scoring — race targets value
                // the +1 uncontested swing; location targets value the captured
                // per-round income (suppressed for the owner). Parked above the
                // generic floor so wars are preferred once they are legal.
                if (a.getTargetCard() instanceof LocationCard) {
                    return 2 + ((LocationCard) a.getTargetCard()).getInfluencePerRound();
                }
                return 4;
            case ATTACK_CONFLICT_PARTICIPANT:
                return (int) Math.round(damageAttackScore(a, state, p, false));
            case HEAL_CHARACTER: {
                CharacterCard ch = a.getCard() instanceof CharacterCard
                        ? (CharacterCard) a.getCard() : null;
                if (ch == null) return 0;
                int urgency = ch.isNeutralized() ? 8
                        : 2 + Math.min(5, ch.getDamageTokens() + ch.getSevereDamageTokens());
                return urgency;
            }
            case REPAIR_CARD: {
                Card c = a.getCard();
                if (c == null) return 0;
                return 3 + Math.min(6, c.getDamageTokens() * 2);
            }
            case BID_ON_MERCENARY: {
                // B5-0403: control value discounted by the bid cost, weighted
                // by the win probability of the projected total (the same
                // winProb idiom the HARD initiation/LEAD_FLEET paths use).
                // What a controlled mercenary is worth is card-specific (the
                // CardEffects mercenary action fires for the controller at
                // the MERCENARY phase) and not readable here, so the base is
                // the modest B5-0301 house value.
                Card merc = a.getCard();
                if (merc == null) return 0;
                int inc = a.getAmount();
                if (inc <= 0) return 0;
                int projected = state.getMercenaryBid(merc, p) + inc;
                double winProb = (projected + 1.0)
                        / (projected + bestOtherMercenaryBid(state, p, merc) + 2.0);
                return (int) Math.round(4.0 * winProb - inc * 0.5);
            }
            default:
                return 1;
        }
    }

    private boolean mediumJoin(GameState state, Player p, Conflict conflict) {
        // Superseded by mediumJoinSide (B5-0343); kept for reference.
        return mediumJoinSide(state, p, conflict) != 0;
    }

    // ── HARD ──────────────────────────────────────────────────────────────────

    private GameAction hardChoose(List<GameAction> legal, GameState state, Player p) {
        Player leader = leadingPlayer(state, p);
        GameAction best     = GameAction.pass();
        double     bestScore = Double.NEGATIVE_INFINITY;

        for (GameAction a : legal) {
            double score = scoreActionHard(a, state, p, leader);
            if (score > bestScore) { bestScore = score; best = a; }
        }
        return best;
    }

    private double scoreActionHard(GameAction a, GameState state, Player p, Player leader) {
        switch (a.getType()) {
            case INITIATE_CONFLICT: {
                if (!(a.getCard() instanceof ConflictCard)) return 0;
                ConflictCard cc  = (ConflictCard) a.getCard();
                Player       opp = a.getTarget();
                int myTotal      = p.conflictTotal(cc.getConflictType());
                int oppTotal     = opp != null ? opp.conflictTotal(cc.getConflictType()) : 0;
                double winProb   = myTotal + 1.0 / (myTotal + oppTotal + 2.0);
                double base      = winProb * cc.getInfluenceReward();
                // B5-0343: pick targets WEAKER than the auto-leader — the
                // pre-0343 +3 leaderBonus steered initiation AT the leading
                // player (feeding the strongest opponent influence rewards);
                // now leading the board is a penalty, and weak targets score
                // higher through the win-probability term.
                double leaderPenalty = (opp == leader) ? -3.0 : 0;
                double lossPenalty   = myTotal < oppTotal ? -3.0 : 0;
                // B5-0344: conflicts a WIN would make aftermath-eligible are
                // worth more (the controller auto-plays eligible aftermaths
                // right after resolution). Rewards on the initiator's side.
                base += aftermathAnticipationBonus(state, p, cc);
                return base + leaderPenalty + lossPenalty;
            }
            case RECRUIT_CHARACTER: {
                if (!(a.getCard() instanceof CharacterCard)) return 3;
                CharacterCard ch = (CharacterCard) a.getCard();
                int maxStat = Math.max(Math.max(p.effectiveStat(ch.getId(), StatKey.DIPLOMACY, ch.getDiplomacy(), true), p.effectiveStat(ch.getId(), StatKey.INTRIGUE, ch.getIntrigue(), true)),
                              Math.max(p.effectiveStat(ch.getId(), StatKey.PSI, ch.getPsi(), true), p.effectiveStat(ch.getId(), StatKey.LEADERSHIP, ch.getLeadership(), true)));
                return 4 + maxStat * 0.5 - rules.recruitCost(p, ch); // B5-0324
            }
            case PLAY_CARD: {
                // B5-0324: positional value minus the card's influence cost.
                // B5-0344: agenda proximity + event catch-up (see helpers).
                double base;
                if (a.getCard() instanceof AgendaCard) {
                    base = agendaProximityScore(state, p, (AgendaCard) a.getCard());
                } else if (a.getCard() instanceof EventCard) {
                    base = 3 + eventCatchUpBonus(state, p);
                } else if (a.getCard() instanceof LocationCard) base = 7;
                else if (a.getCard() instanceof GroupCard)    base = 5;
                else if (a.getCard() instanceof EnhancementCard) base = 4;
                else                                          base = 2;
                return base - a.getCard().getCost();
            }
            case BUILD_INFLUENCE:
                // B5-0324: capped value — HARD avoids over-tinging the score table.
                // Still positive since pushing toward the Influence cap is useful.
                int depr = Math.max(0, 10 - p.getInfluence());
                return 0.25 * depr; // 0..2.25
            case PROMOTE_CHARACTER: {
                // B5-0321: value the new member's best stat, discounted by the
                // influence spent (raw promotion cost).
                if (!(a.getCard() instanceof CharacterCard)) return 1.0;
                CharacterCard ch = (CharacterCard) a.getCard();
                int maxStat = Math.max(Math.max(p.effectiveStat(ch.getId(), StatKey.DIPLOMACY, ch.getDiplomacy(), true), p.effectiveStat(ch.getId(), StatKey.INTRIGUE, ch.getIntrigue(), true)),
                              Math.max(p.effectiveStat(ch.getId(), StatKey.PSI, ch.getPsi(), true), p.effectiveStat(ch.getId(), StatKey.LEADERSHIP, ch.getLeadership(), true)));
                return 2.0 + maxStat * 0.5 - rules.promotionCost(p, ch);
            }
            case LEAD_FLEET: {
                // B5-0362: unused-leader base + the projected initiative —
                // the same win-probability reading scoreActionHard uses for
                // its own conflict-initiation calculation, evaluated WITH the
                // leader's Military seated (offer precondition fl unled, so
                // the delta is exactly this character's Leadership).
                if (!(a.getCard() instanceof FleetCard) || a.getLeader() == null) return 1.5;
                CharacterCard lch = a.getLeader();
                int leadership = p.effectiveStat(lch.getId(), StatKey.LEADERSHIP,
                        lch.getLeadership(), true);
                double base    = 1.5 + leadership * 0.5;
                int proj       = p.conflictTotal(ConflictType.MILITARY) + leadership;
                int opp        = leader.conflictTotal(ConflictType.MILITARY);
                double winProb = (proj + 1.0) / (proj + opp + 2.0);
                return base + 2.0 * winProb;
            }
            case USE_ROTATE_EFFECT: {
                // B5-0366: HARD scores the two assistant effects modestly.
                // The ability boost is worth a small positional term; the
                // sponsor discount is worth ~1. Deterministic (no RNG), same
                // list-order tie-breaking as every other action type.
                if (!(a.getCard() instanceof CharacterCard) || a.getLeader() == null) return 0.5;
                GameAction.RotateEffectKind kind = a.getRotateKind();
                if (kind == GameAction.RotateEffectKind.USE_SPONSOR_DISCOUNT) return 0.5;
                return 1.0;   // USE_ABILITY_BOOST
            }
            case DISCARD_AGENDA:
                // B5-0364: agenda discard is rarely voluntary, scored low.
                return 0.5;
            case REPLACE_AGENDA: {
                // B5-0364: replacement value = proximity to new agenda's win
                // condition minus the IC rotation cost (1.0). Negative when the
                // new agenda is distant, positive when close.
                if (a.getCard() instanceof AgendaCard) {
                    return agendaProximityScore(state, p, (AgendaCard) a.getCard()) - 1.0;
                }
                return 1.5;
            }
            case REVEAL_AGENDA: {
                // B5-0364: hidden-to-active conversion — score by proximity
                // (same as the sponsor path, since this IS the activation).
                if (a.getCard() instanceof AgendaCard) {
                    return agendaProximityScore(state, p, (AgendaCard) a.getCard()) * 1.0;
                }
                return 2.5;
            }
            case PLAY_CONTINGENCY:
                // B5-0365: face-down future-proofing resource. Modest positional
                // value (no immediate effect).
                return 1.5;
            case REVEAL_CONTINGENCY:
                // B5-0365: trigger-activated event-like effect. Score as a
                // modest event (the reveal is player-chosen timing, not automatic).
                return 2.0;
            case PASS:
                return -0.5;
            case DECLARE_WAR_CONFLICT:
                // B5-0376: HARD weighs the war payoff — location income when
                // capturing a location, plus a preference to hit the leading
                // enemy race. Kept modest (B5-0301 house style).
                if (a.getTargetCard() instanceof LocationCard) {
                    LocationCard loc = (LocationCard) a.getTargetCard();
                    double base = 2.0 + loc.getInfluencePerRound();
                    if (loc.getFaction() != null
                            && leader != null && leader.getFaction() == loc.getFaction()) base += 1.0;
                    return base;
                }
                if (a.getTarget() != null
                        && leader != null && a.getTarget().getFaction() == leader.getFaction()) {
                    return 5.0;
                }
                return 4.0;
            case ATTACK_CONFLICT_PARTICIPANT:
                return damageAttackScore(a, state, p, true);
            case HEAL_CHARACTER: {
                CharacterCard ch = a.getCard() instanceof CharacterCard
                        ? (CharacterCard) a.getCard() : null;
                if (ch == null) return 0;
                // Prefer restoring a neutralized IC leader; avoid spending the
                // action on a lightly damaged supporting card when help exists.
                return (ch.isNeutralized() ? 9.0 : 2.0)
                        + Math.min(4, ch.getDamageTokens() + ch.getSevereDamageTokens());
            }
            case REPAIR_CARD: {
                Card c = a.getCard();
                if (c == null) return 0;
                double value = 2.0 + Math.min(5, c.getDamageTokens() * 1.5);
                if (c instanceof FleetCard && c.getDamageTokens() > 0) value += 1.0;
                return value;
            }
            case BID_ON_MERCENARY: {
                // B5-0403: HARD weighs control higher and charges the bid at
                // a steeper opportunity cost (bids spend the D9 applied pool
                // only — the Rating is never touched, per B5-0395).
                Card merc = a.getCard();
                if (merc == null) return 0.0;
                int inc = a.getAmount();
                if (inc <= 0) return 0.0;
                int projected = state.getMercenaryBid(merc, p) + inc;
                double winProb = (projected + 1.0)
                        / (projected + bestOtherMercenaryBid(state, p, merc) + 2.0);
                return 5.0 * winProb - inc * 0.75;
            }
            default:
                return 1;
        }
    }

    /** Score a legal attack by expected damage and retaliation risk. HARD
     * additionally avoids exposing a high-value leader as the attacker. */
    private double damageAttackScore(GameAction a, GameState state, Player p, boolean hard) {
        Card attacker = a.getCard();
        Card target = a.getTargetCard();
        if (attacker == null || target == null) return 0;
        Conflict conflict = state.getActiveConflict();
        if (conflict == null) return 0;
        int dealt = attacker.getAttackDamage(conflict.getConflictType());
        int risk = target.getAttackDamage(conflict.getConflictType());
        double value = dealt - risk * 0.5;
        if (hard) {
            if (attacker instanceof CharacterCard && p.getAmbassador() == attacker) value -= 2.0;
            if (target instanceof CharacterCard) {
                CharacterCard tc = (CharacterCard) target;
                if (p.getInnerCircle().contains(tc)) value -= 3.0;
                if (tc.isNeutralized()) value -= 5.0;
            }
        }
        return value;
    }
    private boolean hardJoin(GameState state, Player p, Conflict conflict) {
        // Superseded by hardJoinSide (B5-0343); kept for reference.
        return hardJoinSide(state, p, conflict) != 0;
    }

    // ── B5-0344: agenda/aftermath/event scoring helpers ─────────────────────

    /**
     * How close p's board state is to winning through agenda ag (0..9,
     * keyed on the winConditionKey vocabulary implemented in
     * AgendaCard.isConditionMet):
     *   INFLUENCE_20       → share of the 20-power bar reached;
     *   MILITARY_SUPREMACY → best rival's Military vs own (rulebook:
     *                        "more than any other player");
     *   MOST_INNER_CIRCLE  → best rival's IC size vs own;
     * unknown keys fall back to the influence reading (the same fallback
     * isConditionMet uses). Score 9 means the agenda's condition is met —
     * playing it wins the game at the next victory check — so MEDIUM/HARD
     * always take a winning agenda.
     */
    private int agendaProximityScore(GameState state, Player p, AgendaCard ag) {
        String key = ag.getWinConditionKey();
        if ("MILITARY_SUPREMACY".equals(key)) {
            int myMil = 0;
            for (FleetCard f : p.getFleets()) { f.setOwner(p); myMil += f.getEffectiveMilitary(); }
            int best = 0;
            for (Player q : state.getPlayers()) {
                if (q == p) continue;
                int mil = 0;
                for (FleetCard f : q.getFleets()) { f.setOwner(q); mil += f.getEffectiveMilitary(); }
                if (mil > best) best = mil;
            }
            if (myMil > best) return 9;
            return Math.min(8, myMil * 4 / (best + 1));
        }
        if ("MOST_INNER_CIRCLE".equals(key)) {
            int mySize = p.getInnerCircle().size();
            int best = 0;
            for (Player q : state.getPlayers()) {
                if (q == p) continue;
                if (q.getInnerCircle().size() > best) best = q.getInnerCircle().size();
            }
            if (mySize > best) return 9;
            return Math.min(8, mySize * 8 / (best + 1));
        }
        return Math.min(9, p.getInfluence() / 2);
    }

    /**
     * Conflicts a WIN would make aftermath-eligible are worth more: the
     * controller auto-plays eligible aftermaths right after resolution
     * (resolveCurrentConflict). Projects this player winning the conflict
     * and counts in-hand aftermaths eligible under that win (initiator
     * always participates; target rule approximated — the conflict's final
     * participant set is unknown at initiation time).
     */
    private double aftermathAnticipationBonus(GameState state, Player p, ConflictCard cc) {
        double bonus = 0;
        for (Card c : p.getHand()) {
            if (c instanceof AftermathCard) {
                AftermathCard am = (AftermathCard) c;
                if (am.isEligible(true, true, cc.getConflictType())) bonus += 1.0;
            }
        }
        return bonus;
    }

    /**
     * Events are timing cards (played from the hand, resolved immediately):
     * a player behind the leader gets extra value from the disruption/tempo
     * an event buys (rulebook framing: events bend the story of the trailing
     * faction). Leading players score no bonus.
     */
    private int eventCatchUpBonus(GameState state, Player p) {
        int bestOther = -1;
        for (Player q : state.getPlayers()) {
            if (q == p) continue;
            if (q.getInfluence() > bestOther) bestOther = q.getInfluence();
        }
        if (bestOther < 0 || p.getInfluence() >= bestOther) return 0;
        return Math.min(3, bestOther - p.getInfluence());
    }

    private Player leadingPlayer(GameState state, Player self) {
        Player leader = self;
        int bestInf = -1;
        for (Player p : state.getPlayers()) {
            if (p == self) continue;
            int inf = p.getInfluence();
            if (inf > bestInf) { bestInf = inf; leader = p; }
        }
        return leader;
    }
}
