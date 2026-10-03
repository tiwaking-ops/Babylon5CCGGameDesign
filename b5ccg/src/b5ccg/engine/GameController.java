package b5ccg.engine;

import b5ccg.ai.AIPlayer;
import b5ccg.model.*;
import b5ccg.model.enums.*;
import java.io.IOException;
import java.util.*;

/** Central game loop. Runs off the Swing EDT via a background thread.
 *  Notifies the UI through a GameStateCallback callback after each state change.
 *
 *  Build Influence handling added per B5-0301 — the ACTION branch now treats a
 *  BUILD_INFLUENCE action by calling RulesEngine.canBuildInfluence() and
 *  RulesEngine.executeBuildInfluence().
 */
public class GameController {

    private final GameState         state;
    private final RulesEngine       rules   = new RulesEngine();
    private final List<AIPlayer>    aiPlayers;
    private final GameStateCallback uiCallback;

    private volatile boolean        waitingForHuman = false;
    private volatile GameAction     pendingHumanAction;
    private volatile boolean        waitingForHumanConflictJoin = false;
    private volatile GameAction     pendingHumanConflictJoin;
    private volatile boolean        waitingForHumanConflictAttack = false;
    private volatile GameAction     pendingHumanConflictAttack;

    // ── B5-2275: consecutive-full-table-pass stall guard ─────────────────────
    //  B5-0312 measured a game reaching a 20-20 tie with no winner: D12 crowns
    //  a player only on a STRICTLY-greatest Influence Rating, so a table where
    //  nobody can act any more reaches a fixed point the rulebook never
    //  resolves.  The guard below is a liveness measure, NOT a victory rule:
    //  it changes no legal action and adds no win anyone could earn in play.
    //  After MAX_CONSECUTIVE_FULL_PASS_ROUNDS consecutive rounds in which the
    //  WHOLE table passed, the controller stops the game: a strictly-greatest
    //  Influence leader is crowned, and a tie is reported loudly and left
    //  uncrowned.
    public static final int         MAX_CONSECUTIVE_FULL_PASS_ROUNDS = 3;
    private int                     consecutiveFullPassRounds = 0;
    private volatile boolean        stallTieEnding = false;
    private boolean                 lastRoundWasFullTablePass = false;

    public GameController(GameState state, List<AIPlayer> aiPlayers,
                          GameStateCallback uiCallback) {
        this.state      = state;
        this.aiPlayers  = aiPlayers;
        this.uiCallback = uiCallback;
    }

    // ── Called from a background thread ──────────────────────────────────────

    public void runGame() {
        setupGame();
        notifyUI();

        while (!state.isGameOver()) {
            rules.startRound(state);
            state.setPhase(GamePhase.ACTION);
            notifyUI();

            runActionPhase();
            if (state.isGameOver()) break;

            // B5-2275: liveness guard. A round in which the WHOLE table passed
            // is a fixed point the rulebook cannot resolve (D12 needs a
            // strictly-greatest leader). Count consecutive such rounds; any
            // round containing a real action resets the count to zero, so a
            // single legal action proves the table is alive.
            if (lastRoundWasFullTablePass) consecutiveFullPassRounds++;
            else                               consecutiveFullPassRounds = 0;
            if (consecutiveFullPassRounds >= MAX_CONSECUTIVE_FULL_PASS_ROUNDS) {
                resolveStall();
                break;
            }

            runMercenaryPhase();   // B5-0395 (rulebook §Mercenaries :735–:741)
            if (state.isGameOver()) break;

            // B5-1982: AFTERMATH phase — draw 2, discard to 7 with reshuffle
            state.setPhase(GamePhase.AFTERMATH);
            notifyUI();
            rules.aftermathPhase(state);
            if (state.isGameOver()) break;

            runDrawPhase();

            // B5-0437: station end-of-round maintenance must run BEFORE
            // advanceRound() — advanceRound() resets stationSourceFired,
            // which the maintenance consults to decide decay
            // (RulesEngine.applyEndOfRoundStation).
            rules.applyEndOfRoundStation(state);

            state.advanceRound();
            notifyUI();
        }
    }

    private void setupGame() {
        state.setPhase(GamePhase.SETUP);
        List<Card> pool = null;
        try {
            pool = DeckLoader.loadBothSets();
        } catch (IOException e) {
            state.log("Failed to load card pool for starting setup: " + e.getMessage());
        }
        for (Player p : state.getPlayers()) {
            CharacterCard amb = findAmbassador(p);
            if (amb != null) {
                p.getHand().remove(amb);
                p.setAmbassador(amb);
                // B5-0321: the ambassador IS an Inner Circle member
                // (rulebook: "your ambassador is always considered a member
                // of your faction's Inner Circle"). Without this seat,
                // canBuildInfluence and canPromote see an empty Inner Circle
                // and those actions are never legal — the root cause of
                // B5-0202 Finding 7's unreachable Build Influence.
                p.getInnerCircle().add(amb);
            }
            // B5-1973: Faction-specific starting setup (homeworld, starting fleet, starting agenda)
            if (pool != null) {
                StarterDeckBuilder.StartingSetup setup = StarterDeckBuilder.getStartingSetup(p.getFaction(), pool);
                if (setup != null) {
                    if (setup.homeworld != null) {
                        p.getLocations().add(setup.homeworld);
                        state.log(p.getName() + " starts with homeworld: " + setup.homeworld.getTitle());
                    }
                    if (setup.startingFleet != null) {
                        p.getFleets().add(setup.startingFleet);
                        state.log(p.getName() + " starts with fleet: " + setup.startingFleet.getTitle());
                    }
                    if (setup.startingAgenda != null) {
                        p.setAgenda(setup.startingAgenda);
                        state.log(p.getName() + " starts with agenda: " + setup.startingAgenda.getTitle());
                    }
                }
            }
            p.drawCards(3);
        }
        state.log("Game setup complete.");
    }

    private CharacterCard findAmbassador(Player p) {
        for (Card c : p.getHand()) {
            if (c instanceof CharacterCard) {
                CharacterCard ch = (CharacterCard) c;
                if (ch.isAmbassador() && ch.getFaction() == p.getFaction()) return ch;
            }
        }
        return null;
    }

    private void runActionPhase() {
        int passCount = 0;
        int playerCount = state.getPlayers().size();
        int totalActions = 0;
        // B5-2275: a round counts as a FULL-TABLE pass only when every action
        // taken in it was a PASS. One real action anywhere makes the round a
        // non-pass, so the stall counter cannot be driven by one busy player
        // while the rest of the table idles.
        boolean sawNonPassAction = false;
        // B5-0372 (D6): initiative-cycle loop — each player acts once per cycle;
        // the round ends only when all players pass consecutively. No per-player
        // action cap (the loop itself delivers one action per eligibility visit).
        // Safety cap (non-rulebook, liveness only): 8 × playerCount.
        final int MAX_ACTIONS_PER_ROUND = playerCount * 8;

        while (passCount < playerCount && !state.isGameOver()) {
            if (totalActions >= MAX_ACTIONS_PER_ROUND) {
                state.log("Action round safety cap reached (" + MAX_ACTIONS_PER_ROUND
                          + " actions); ending round.");
                break;
            }
            Player current = state.getActivePlayer();
            GameAction action;

            if (current.isHuman()) {
                action = waitForHumanAction();
            } else {
                AIPlayer ai = getAI(current);
                action = ai.chooseAction(state, current);
                pause(600);
            }

            processAction(current, action);
            notifyUI();
            if (action.getType() == GameAction.Type.PLAY_CONTINGENCY) {
                runContingencyRevealWindow();
            }

            if (action.getType() == GameAction.Type.PASS) {
                current.setPassed(true);
                passCount++;
            } else {
                sawNonPassAction = true;      // B5-2275
                current.setPassed(false);   // B5-0372: un-pass — a passer may act later (rulebook §V)
                passCount = 0;
            }

            totalActions++;

            Player winner = rules.checkVictory(state);
            if (winner != null) {
                state.setWinner(winner);
                notifyUI();
                lastRoundWasFullTablePass = false;   // B5-2275: game decided, not a stall
                return;
            }

            endOfTurn(current);

            state.advanceTurn();
        }
        // B5-2275: a round that took no action at all is a full-table pass. The
        // safety-cap break also lands here; it counts as a pass only if every
        // action it did take was a PASS, which is the honest reading.
        lastRoundWasFullTablePass = !sawNonPassAction;
        rules.finishActionRound(state);
    }

    // ── B5-2275: stall resolution ─────────────────────────────────────────────

    /**
     * Ends a game that has stalled: the whole table passed in
     * MAX_CONSECUTIVE_FULL_PASS_ROUNDS consecutive rounds.
     *
     * <p>Deliberately NOT a victory rule. The rulebook has no provision for
     * "nobody could act", and D12 crowns a player only on a strictly-greatest
     * Influence Rating, so a 20-20 table has no rulebook winner. This method
     * therefore does two things and no more:
     *
     * <ol>
     *   <li>if exactly one ACTIVE player is strictly greatest in Influence
     *       Rating, that player is crowned as the stall winner and the log
     *       says so explicitly — it is a stall crown, not a Standard Victory,
     *       and it is deliberately reachable at any rating;</li>
     *   <li>otherwise the tie is reported loudly, per player, in the log, and
     *       NO winner is set: {@link GameState#isGameOver()} stays false and
     *       the run simply stops. Nothing is invented to break the tie.</li>
     * </ol>
     *
     * <p>Forfeited and surrendered players are excluded from both the crown
     * and the tie report, matching {@link GameState#isPlayerActive(Player)}
     * and every path in {@link RulesEngine#checkVictory(GameState)}.
     */
    private void resolveStall() {
        List<Player> active = new ArrayList<Player>();
        for (Player p : state.getPlayers()) {
            if (state.isPlayerActive(p)) active.add(p);
        }

        if (active.isEmpty()) {
            // Nothing active to crown and nothing to report. Say so rather
            // than looping on a table that no longer exists.
            state.log("STALL: " + consecutiveFullPassRounds
                    + " consecutive all-pass rounds and no active players remain.");
            stallTieEnding = true;
            notifyUI();
            return;
        }

        int best = Integer.MIN_VALUE;
        for (Player p : active) {
            if (p.getInfluence() > best) best = p.getInfluence();
        }
        List<Player> leaders = new ArrayList<Player>();
        for (Player p : active) {
            if (p.getInfluence() == best) leaders.add(p);
        }

        if (leaders.size() == 1) {
            Player leader = leaders.get(0);
            state.log("STALL after " + consecutiveFullPassRounds
                    + " consecutive all-pass rounds: " + leader.getName()
                    + " is the sole active leader at Influence Rating " + best
                    + " and is crowned on the stall guard (not a Standard Victory"
                    + " — the rulebook provides no winner for an all-pass table).");
            state.setWinner(leader);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("STALL TIE: ").append(consecutiveFullPassRounds)
              .append(" consecutive all-pass rounds and ").append(leaders.size())
              .append(" players tied at Influence Rating ").append(best)
              .append(" — NO WINNER DECLARED: ");
            for (int i = 0; i < active.size(); i++) {
                if (i > 0) sb.append(", ");
                Player p = active.get(i);
                sb.append(p.getName()).append(" ").append(p.getInfluence());
            }
            sb.append(". D12 crowns a strictly-greatest leader only, so the"
                    + " rulebook has no winner for this position and none is"
                    + " invented; the game ends here as a reported tie.");
            state.log(sb.toString());
            stallTieEnding = true;
        }
        notifyUI();
    }

    /** B5-2275: consecutive all-pass rounds counted so far. */
    public int  getConsecutiveFullPassRounds()    { return consecutiveFullPassRounds; }

    /** B5-2275: true when the game ended on a REPORTED TIE, winner uncrowned. */
    public boolean isStalledOnTie()              { return stallTieEnding; }

    private void processAction(Player p, GameAction action) {
        state.log(p.getName() + ": " + action);

        switch (action.getType()) {
            case PASS:
                break;

            case INITIATE_CONFLICT:
                if (action.getCard() instanceof ConflictCard) {
                    ConflictCard cc = (ConflictCard) action.getCard();
                    Player target = action.getTarget();
                    // B5-0336: the 4-arg check enforces requiresTarget
                    // (Border Raid etc.); the conflict carries the declared
                    // target so the participation "players" gate can apply.
                    if (!rules.canInitiateConflict(p, cc, target, state)) {
                        boolean needsTarget = cc.getParticipation() != null
                                && cc.getParticipation().isRequiresTarget()
                                && target == null;
                        state.log(p.getName() + " cannot initiate " + cc.getTitle()
                                  + (needsTarget
                                     ? " — a target must be declared (participation)."
                                     : " — one conflict per turn."));
                        break;
                    }
                    p.removeFromHand(cc);
                    state.markConflictInitiated(p);
                    Conflict conflict = new Conflict(cc, p, target);
                    if (target != null) {
                        state.log(p.getName() + " targets " + target.getName()
                                  + " with " + cc.getTitle() + ".");
                    }
                    if (p.getAmbassador() != null && !p.getAmbassador().isFaceDown()) {
                        conflict.commitCard(p, p.getAmbassador(), true);   // B5-0309: initiator supports
                    }
                    state.setActiveConflict(conflict);
                    state.setPhase(GamePhase.CONFLICT_RESOLUTION);
                    notifyUI();
                    resolveCurrentConflict();
                    state.setPhase(GamePhase.ACTION);
                }
                break;

            case PLAY_CARD:
                applyGenericCardPlay(p, action.getCard(), action.isHidden());
                break;

            case PLAY_CONTINGENCY:
                if (action.getCard() instanceof ContingencyCard) {
                    ContingencyCard contingency = (ContingencyCard) action.getCard();
                    Card host = action.getTargetCard();
                    if (rules.canPlayContingency(p, contingency, host)
                            && contingency.placeUnder(p, host)) {
                        p.removeFromHand(contingency);
                        state.log(p.getName() + " places a face-down contingency under "
                                + host.getTitle() + ".");
                    } else {
                        state.log(p.getName() + " cannot place that contingency.");
                    }
                }
                break;

            case REVEAL_CONTINGENCY:
                if (action.getCard() instanceof ContingencyCard) {
                    ContingencyCard contingency = (ContingencyCard) action.getCard();
                    if (rules.canRevealContingency(p, contingency)) {
                        CardEffects.revealContingency(state, p, contingency);
                    } else {
                        state.log(p.getName() + " cannot reveal that contingency.");
                    }
                }
                break;

            case RECRUIT_CHARACTER:
                if (action.getCard() instanceof CharacterCard) {
                    CharacterCard ch = (CharacterCard) action.getCard();
                    // B5-0323: the faction must apply the card's influence
                    // cost (rulebook §Sponsor) — free cards (cost 0, the
                    // current data) behave exactly as before.
                    if (rules.canRecruit(p, ch)) {
                        // B5-0339: the effective cost carries the assistant's
                        // sponsor discount; consume exactly what was applied.
                        int baseCost = rules.baseRecruitCost(p, ch);
                        SponsorCost sponsorCost = rules.sponsorCost(p, ch);
                        int cost = sponsorCost.getAmount();
                        if (!p.applyInfluence(cost)) {
                            state.log(p.getName() + " cannot apply enough influence to sponsor "
                                    + ch.getTitle() + ".");
                            break;
                        }
                        if (!sponsorCost.isWaived() && cost < baseCost) {
                            p.consumeSponsorDiscount(baseCost - cost);
                            state.log(p.getName() + " sponsors at an assistant discount ("
                                      + cost + " instead of " + baseCost + ").");
                        }
                        p.removeFromHand(ch);
                        p.placeInSupportingRole(ch);
                        state.log(p.getName() + " recruits " + ch.getTitle());
                    } else {
                        state.log(p.getName() + " cannot recruit " + ch.getTitle()
                                  + " — influence cost not affordable.");
                    }
                }
                break;

            case BUILD_INFLUENCE:
                if (action.getCard() instanceof CharacterCard) {
                    CharacterCard leader2 = (CharacterCard) action.getCard();
                    rules.executeBuildInfluence(p, leader2, state);
                }
                break;

            case PROMOTE_CHARACTER: // B5-0321
                if (action.getCard() instanceof CharacterCard) {
                    CharacterCard ch = (CharacterCard) action.getCard();
                    rules.executePromote(p, ch, action.getLeader(), state);
                }
                break;

            case SPONSOR_CARD: // B5-2245 (rulebook :657)
                // The card types RECRUIT_CHARACTER does not name. executeSponsorCard
                // owns the whole payment (refuse when short, deduct on success) and
                // the rotation, so this branch stays a dispatch and does not
                // re-derive the cost the way the RECRUIT_CHARACTER branch above
                // must for its assistant-discount bookkeeping.
                if (action.getCard() instanceof EnhancementCard
                        || action.getCard() instanceof LocationCard
                        || action.getCard() instanceof FleetCard) {
                    rules.executeSponsorCard(p, action.getCard(),
                            action.getLeader(), state);
                }
                break;

            case LEAD_FLEET: // B5-0362 (B5-0345 Tier-1 #1): one action per
                // (leader, fleet) pair — the central p.useAction() below
                // consumes it exactly like every other action branch.
                if (action.getCard() instanceof FleetCard && action.getLeader() != null) {
                    FleetCard leadFl = (FleetCard) action.getCard();
                    if (rules.canLeadFleet(p, action.getLeader(), leadFl)) {
                        rules.executeLeadFleet(p, action.getLeader(), leadFl, state);
                    } else {
                        state.log(p.getName() + " cannot lead "
                                + leadFl.getTitle() + " with "
                                + action.getLeader().getTitle() + ".");
                    }
                }
                break;

            case USE_ROTATE_EFFECT: // B5-0366 (B5-0345 Tier-2 #5): one action
                // per (assistant, effect-kind) pair — the central
                // p.useAction() below consumes it exactly like every other
                // branch. The pair rides the existing (card, leader) fields
                // (card = rotating assistant, leader = ambassador); the kind
                // rides getRotateKind. Effect payloads compose with the
                // B5-0357 bonus layer through the B5-0339 executors (flag +
                // discount are registry-compatible computed reads, never
                // field mutations) — no CardEffects call sites touched.
                if (action.getCard() instanceof CharacterCard
                        && action.getLeader() != null) {
                    CharacterCard rotCh  = (CharacterCard) action.getCard();
                    CharacterCard rotAmb = action.getLeader();
                    GameAction.RotateEffectKind kind = action.getRotateKind();
                    if (rules.canUseRotateEffect(p, rotCh, rotAmb, kind)) {
                        rules.executeRotateEffect(p, rotCh, kind, state);
                    } else {
                        state.log(p.getName() + " cannot use "
                                + (kind == null ? "(unknown rotate effect)"
                                               : kind.toString())
                                + " with " + rotCh.getTitle() + ".");
                    }
                }
                break;

            // ── Agenda lifecycle (B5-0364; rulebook :520/:719) ───────────────

            case DISCARD_AGENDA:
                if (action.getCard() instanceof AgendaCard
                        && p.getAgenda() == action.getCard()
                        && rules.canDiscardAgenda(p)) {
                    AgendaCard gone = (AgendaCard) action.getCard();
                    // B5-1995: remove mark grants from the discarded agenda
                    CardEffects.removeAgendaMarkGrants(state, p, gone);
                    // B5-2251: also lift the WHILE_IN_PLAY stat bonuses it
                    // granted. A bonus has no round boundary to expire on, so
                    // without this the agenda's +1 Military outlived the agenda.
                    int lifted = CardEffects.removeAgendaBonuses(p, gone);
                    if (lifted > 0) {
                        state.log(p.getName() + ": " + gone.getTitle()
                                + " stops granting " + lifted + " bonus(es).");
                    }
                    p.setAgenda(null);
                    p.getDeck().discard(gone);
                    state.log(p.getName() + " discards agenda " + gone.getTitle() + ".");
                } else {
                    state.log(p.getName() + " cannot discard that agenda"
                            + (action.getCard() instanceof AgendaCard
                                    && ((AgendaCard) action.getCard()).isMajorAgenda()
                                ? " — Major agendas cannot be discarded (:719)."
                                : "."));
                }
                break;

            case REPLACE_AGENDA:
                if (action.getCard() instanceof AgendaCard
                        && rules.canReplaceAgenda(p, (AgendaCard) action.getCard(),
                                                  action.getLeader())) {
                    AgendaCard oldAg = p.getAgenda();
                    AgendaCard newAg = (AgendaCard) action.getCard();
                    action.getLeader().rotate();
                    p.removeFromHand(newAg);
                    // B5-1995: remove mark grants from the old agenda
                    if (oldAg != null) {
                        CardEffects.removeAgendaMarkGrants(state, p, oldAg);
                        // B5-2251: and lift its WHILE_IN_PLAY stat bonuses, or
                        // the outgoing agenda's +1 Military keeps being paid
                        // out by the fleets after it has left play.
                        CardEffects.removeAgendaBonuses(p, oldAg);
                    }
                    p.setAgenda(newAg);
                    // :719 — the old agenda is REMOVED FROM THE GAME (not discarded).
                    state.log(p.getName() + " replaces " + oldAg.getTitle()
                            + " with " + newAg.getTitle()
                            + " (" + action.getLeader().getTitle() + " rotates).");
                    // B5-1995 / B5-2251: a replacement agenda is an agenda coming
                    // into play, so it runs the full on-play path — the same
                    // stat bonuses a sponsored one gets, plus its mark grants.
                    // Calling this path called applyAgendaMarkGrants ALONE, so a
                    // fleet-wide +1 Military agenda installed by REPLACE was
                    // live in the agenda slot and inert on the fleets. It is
                    // called instead of, not after, the mark-grant call:
                    // applyAgendaOnPlay invokes applyAgendaMarkGrants itself and
                    // marks ACCUMULATE, so running both would double them.
                    CardEffects.applyAgendaOnPlay(state, p, newAg);
                } else {
                    AgendaCard cur = p.getAgenda();
                    state.log(p.getName() + " cannot replace that agenda"
                            + (cur != null && cur.isMajorAgenda()
                                    && action.getCard() instanceof AgendaCard
                                    && !((AgendaCard) action.getCard()).isMajorAgenda()
                                ? " — Major agendas may only be replaced by another Major."
                                : "."));
                }
                break;

            case REVEAL_AGENDA:
                if (action.getCard() instanceof AgendaCard
                        && p.getAgenda() == action.getCard()
                        && rules.canRevealAgenda(p)) {
                    AgendaCard hid = (AgendaCard) action.getCard();
                    // :719 — if it is one you could not sponsor at that time,
                    // it is discarded instead of being revealed.
                    if (!hid.getFaction().isPlayableBy(p.getFaction())) {
                        // Hidden agenda had no effect (was face-down), so no marks to remove
                        p.setAgenda(null);
                        p.getDeck().discard(hid);
                        state.log(p.getName() + "'s hidden agenda could not be "
                                + "sponsored by that faction — discarded instead.");
                    } else {
                        hid.setFaceDown(false);
                        state.log(p.getName() + " reveals hidden agenda "
                                + hid.getTitle() + " — it takes effect immediately.");
                        CardEffects.applyAgendaOnPlay(state, p, hid);
                    }
                } else {
                    state.log(p.getName() + " has no hidden agenda to reveal.");
                }
                break;

            case JOIN_CONFLICT_SUPPORT:
                if (state.getActiveConflict() != null) {
                    if (rules.canJoinConflict(p, state.getActiveConflict())) {
                        rules.executeJoinConflict(p, state.getActiveConflict(), true, state);
                    } else {
                        state.log(p.getName() + " cannot support — join not allowed.");
                    }
                } else {
                    state.log("No active conflict to join.");
                }
                break;

            case JOIN_CONFLICT_OPPOSE:
                if (state.getActiveConflict() != null) {
                    if (rules.canJoinConflict(p, state.getActiveConflict())) {
                        rules.executeJoinConflict(p, state.getActiveConflict(), false, state);
                    } else {
                        state.log(p.getName() + " cannot oppose — join not allowed.");
                    }
                } else {
                    state.log("No active conflict to join.");
                }
                break;

            case ATTACK_CONFLICT_PARTICIPANT: // B5-0370
                if (state.getActiveConflict() != null
                        && rules.executeAttackConflictParticipant(p, action.getCard(),
                                action.getTargetCard(), state.getActiveConflict(), state)) {
                    // A legal attack rotates its card and adds it to the current
                    // conflict; the shared action spend below consumes one action.
                } else {
                    state.log(p.getName() + " cannot attack that conflict participant.");
                }
                break;

            case HEAL_CHARACTER: // B5-0371
                if (!(action.getCard() instanceof CharacterCard)
                        || !rules.executeHealCharacter(p, (CharacterCard) action.getCard(), state)) {
                    state.log(p.getName() + " cannot heal that character.");
                }
                break;

            case REPAIR_CARD: // B5-0371
                if (!rules.executeRepairCard(p, action.getCard(), state)) {
                    state.log(p.getName() + " cannot repair that fleet or location.");
                }
                break;

            // ── B5-0661: Unconditional Surrender (rulebook :815–:821) ─────────
            case SURRENDER: // B5-0661
                if (action.getTarget() != null
                        && rules.canSurrender(p, action.getTarget(), state)) {
                    rules.executeSurrender(p, action.getTarget(), state);
                } else {
                    state.log(p.getName() + " cannot surrender"
                            + (action.getTarget() == null
                                ? " — no valid target."
                                : " — not at war, or target is invalid."));
                }
                break;

            // ── B5-1979: voluntary forfeit ─────────────────────────────────────
            case FORFEIT:
                if (rules.canForfeit(p, state)) {
                    // Immediate loss: executeForfeit marks the player forfeited
                    // and, when that leaves a sole survivor, awards that
                    // survivor the game. The game-over transition and the UI
                    // wake-up are done HERE rather than left to the
                    // end-of-action checkVictory at GameController.java:167,
                    // because that check runs only after the turn advances --
                    // one more action would have been processed for a player
                    // who has already lost. Returning here stops the action
                    // round immediately, which is the "immediate loss" the row
                    // asks for.
                    Player forfeitWinner = rules.executeForfeit(p, state);
                    if (forfeitWinner != null) {
                        state.setWinner(forfeitWinner);
                        notifyUI();
                        return;
                    }
                } else {
                    state.log(p.getName() + " cannot forfeit right now.");
                }
                break;

            // ── B5-0376: war conflict declaration ──────────────────────────────
            case DECLARE_WAR_CONFLICT: {
                WarKind kind = null;
                Player raceTarget = action.getTarget();
                LocationCard locTarget = (LocationCard) action.getTargetCard();
                if (raceTarget != null) {
                    kind = WarKind.RACE_TARGET;
                } else if (locTarget != null) {
                    kind = WarKind.LOCATION_TARGET;
                }
                if (kind == null) {
                    state.log(p.getName() + " declares an invalid war conflict (no target).");
                    break;
                }
                Conflict conflict = rules.declareWarConflict(p, kind, raceTarget, locTarget, state);
                if (conflict == null) {
                    state.log(p.getName() + " cannot declare a war conflict (not at war, or target not at war with p).");
                    break;
                }
                state.markWarConflictInitiated(p);
                state.markConflictInitiated(p);
                state.setActiveConflict(conflict);
                state.setPhase(GamePhase.CONFLICT_RESOLUTION);
                notifyUI();
                resolveCurrentConflict();
                state.setPhase(GamePhase.ACTION);
                break;
            }

            // ── B5-0395: mercenary control bids ──────────────────────────────
            case BID_ON_MERCENARY:
                if (action.getCard() != null
                        && rules.canBidOnMercenary(p, action.getCard(),
                                                   action.getAmount(), state)) {
                    rules.executeBidOnMercenary(p, action.getCard(),
                                                action.getAmount(), state);
                } else {
                    state.log(p.getName() + " cannot bid "
                            + action.getAmount() + " on that mercenary"
                            + (action.getCard() == null
                                ? "."
                                : (action.getCard().isMercenary()
                                    ? " — not affordable or not in play."
                                    : " — " + action.getCard().getTitle()
                                        + " is not a mercenary.")));
                }
                break;

            default:
                break;
        }
    }

    /**
     * B5-2243: a contingency placement opens the rulebook's timing-dispute
     * window. Players get one reveal-or-pass opportunity in initiative order;
     * the window closes after the whole table passes consecutively. A reveal
     * is a reaction to the placement, not a second action for the player.
     */
    private void runContingencyRevealWindow() {
        List<Player> players = state.getPlayers();
        if (players.isEmpty() || !hasRevealableContingency(players)) return;

        Player active = state.getActivePlayer();
        int start = players.indexOf(active);
        if (start < 0) start = 0;
        int passes = 0;
        int visits = 0;
        while (passes < players.size() && !state.isGameOver()) {
            Player current = players.get((start + visits) % players.size());
            visits++;
            if (!state.isPlayerActive(current)) {
                passes++;
                continue;
            }

            GameAction response = GameAction.pass();
            if (hasRevealableContingency(current)) {
                if (current.isHuman()) {
                    response = waitForHumanAction();
                } else {
                    response = firstRevealAction(current);
                }
            }

            if (response != null
                    && response.getType() == GameAction.Type.REVEAL_CONTINGENCY
                    && response.getCard() instanceof ContingencyCard
                    && rules.canRevealContingency(current,
                                                  (ContingencyCard) response.getCard())) {
                processAction(current, response);
                passes = 0;
            } else {
                passes++;
            }
            notifyUI();
        }
        state.log("Contingency reveal window closed after " + passes
                + " consecutive passes.");
    }

    private boolean hasRevealableContingency(List<Player> players) {
        for (Player p : players) {
            if (hasRevealableContingency(p)) return true;
        }
        return false;
    }

    private boolean hasRevealableContingency(Player p) {
        return firstRevealAction(p) != null;
    }

    private GameAction firstRevealAction(Player p) {
        for (Card host : inPlayCards(p)) {
            for (Card placed : host.getContingencies()) {
                if (placed instanceof ContingencyCard
                        && rules.canRevealContingency(p, (ContingencyCard) placed)) {
                    return GameAction.revealContingency((ContingencyCard) placed);
                }
            }
        }
        return null;
    }

    private List<Card> inPlayCards(Player p) {
        List<Card> cards = new ArrayList<Card>();
        if (p.getAmbassador() != null) cards.add(p.getAmbassador());
        cards.addAll(p.getInnerCircle());
        cards.addAll(p.getSupportingRole());
        cards.addAll(p.getFleets());
        cards.addAll(p.getLocations());
        cards.addAll(p.getGroups());
        cards.addAll(p.getEnhancements());
        if (p.getAgenda() != null) cards.add(p.getAgenda());
        return cards;
    }

    /** B5-0395: the Mercenary phase — after ACTION closes, control of each
     *  offered mercenary is decided from the cumulative bids; ties crown
     *  nobody, and a controller's mercenary acts immediately through the
     *  id-keyed CardEffects table. No-ops cleanly when no mercenaries are on
     *  the table (the pool carries none today — B5-0386).
     *
     *  B5-2281: the resolution now belongs to
     *  {@link RulesEngine#resolveMercenaryControl}, which breaks a tie in
     *  rulebook :352 initiative order and logs the tie loudly instead of
     *  crowning nobody. GameState.resolveMercenaries() (model scope, the
     *  B5-0395 strict reading) is no longer on this path; the divergence is
     *  pinned by the MER-TIE conformance witness so the two can never be
     *  mistaken for interchangeable authorities. */
    private void runMercenaryPhase() {
        if (state.getMercenaryOffers().isEmpty()) return;
        state.setPhase(GamePhase.MERCENARY);
        notifyUI();
        Map<Card, Player> controllers = rules.resolveMercenaryControl(state);
        for (Map.Entry<Card, Player> entry : controllers.entrySet()) {
            if (state.isGameOver()) break;
            Card merc = entry.getKey();
            Player ctl = entry.getValue();
            state.log(ctl.getName() + " controls mercenary " + merc.getTitle()
                    + " (" + state.getMercenaryBid(merc, ctl) + " influence).");
            CardEffects.applyMercenaryAction(state, ctl, merc);
            notifyUI();
        }
    }

    private void resolveCurrentConflict() {
        Conflict conflict = state.getActiveConflict();
        if (conflict == null) return;

        for (Player p : state.getPlayers()) {
            if (p == conflict.getInitiator()) continue;
            if (p.isHuman()) continue;
            AIPlayer ai = getAI(p);
            // B5-0343: the AI picks its side (+1 support, -1 oppose, 0 abstain)
            // by conflict outcome — the controller no longer hardcodes the
            // always-oppose default the B5-0310 audit flagged.
            int side = ai.decideJoinSide(state, p, conflict);
            if (side != 0) {
                boolean support = side > 0;
                // B5-0336: addParticipant is the participation gate — a
                // refusal means the conflict's restriction rejects this
                // joiner (logged loudly at the refusal site).
                if (conflict.addParticipant(p, support)) {                // B5-0309: joiners pick a side
                    if (p.getAmbassador() != null) {
                        conflict.commitCard(p, p.getAmbassador(), support);   // B5-0309
                    }
                }
            }
        }

        // B5-0363: the human receives a real decision window after the AI
        // seats have chosen and before mandatory participation/resolution.
        // Conflicts run synchronously on the controller thread; this wait
        // releases the monitor so the Swing UI can submit the existing
        // JOIN_CONFLICT_SUPPORT / JOIN_CONFLICT_OPPOSE action.
        Player human = state.getHumanPlayer();
        if (uiCallback != null && human.isHuman() && state.isPlayerActive(human)
                && human != conflict.getInitiator()
                && rules.canJoinConflict(human, conflict)
                && conflict.canJoinConflict(human)) {
            state.log(human.getName() + " may choose a side in the active conflict.");
            GameAction join = waitForHumanConflictJoin();
            if (join != null && join.getType() == GameAction.Type.JOIN_CONFLICT_SUPPORT) {
                rules.executeJoinConflict(human, conflict, true, state);
            } else if (join != null
                    && join.getType() == GameAction.Type.JOIN_CONFLICT_OPPOSE) {
                rules.executeJoinConflict(human, conflict, false, state);
            }
        }

        // B5-0336: enforce mandatory participation dimensions (mustTakeSide,
        // allPlayersMustCommit, mustCommitAmbassador) before resolution.
        rules.enforceMandatoryParticipation(conflict, state);

        if (uiCallback != null && human.isHuman() && !human.hasForfeited()
                && hasLegalHumanConflictAttack(human, conflict)) {
            state.log(human.getName() + " may attack a participant in the active conflict.");
            GameAction attack = waitForHumanConflictAttack();
            if (attack != null
                    && attack.getType() == GameAction.Type.ATTACK_CONFLICT_PARTICIPANT) {
                processAction(human, attack);
            }
        }

        Player winner = rules.resolveConflict(conflict, state);

        // B5-1923: card-specific loser outcome moved into
        // RulesEngine.resolveConflict itself (same primary-loser selection,
        // same position after war outcome): it was UI-only before, so every
        // headless resolution silently skipped it.
        //
        // B5-2251: the agenda Diplomacy-win bonus moved there for the same
        // reason and is now applied inside resolveConflict. Applying it here
        // as well would pay the winner twice on the UI path.

        state.setPhase(GamePhase.AFTERMATH);
        notifyUI();

        for (Player p : state.getPlayers()) {
            if (p.isHuman()) continue;
            boolean initiatorWon = rules.initiatorWon(conflict, winner);
            for (Card c : new ArrayList<Card>(p.getHand())) {
                if (c instanceof AftermathCard) {
                    AftermathCard am = (AftermathCard) c;
                    // B5-0338: D2 target selection + 6-arg legality (with the
                    // D4 registry); the effect lands on the TARGET.
                    Player target = selectAftermathTarget(p, am, conflict);
                    if (rules.canPlayAftermath(p, am, conflict, initiatorWon,
                                               target, state)) {
                        p.removeFromHand(am);
                        state.log(p.getName() + " plays aftermath: " + am.getTitle()
                                  + " on " + target.getName() + ".");
                        state.attachAftermath(am, target);   // D4: in play on the target
                        // B5-0990/B5-2249: id-keyed dispatch first; the generic
                        // text-sniffing effect stays as fallback for ids with
                        // no registered effect (unknown ids keep the old
                        // behaviour, matching the events rule).
                        String amId = am.getId();
                        boolean dispatched = amId != null
                                && (CardEffects.isNegotiatedSurrender(amId)
                                    || CardEffects.isDiplomaticAdvantage(amId)
                                    || CardEffects.isUnitedFront(amId)
                                    || CardEffects.isDrawTwoIntrigue(amId)
                                    || CardEffects.isHiddenAgent(amId)
                                    || CardEffects.isRefugees(amId)
                                    || CardEffects.isRiseToPower(amId)
                                    || CardEffects.isWarHero(amId)
                                    || CardEffects.isApprovalOfTheGrey(amId)
                                    || CardEffects.isFocusYourEfforts(amId)
                                    || CardEffects.isGlory(amId)
                                    || CardEffects.isRetribution(amId)
                                    || CardEffects.isReverseAdvances(amId)
                                    || CardEffects.isOpponentLoseOneInf(amId)
                                    || CardEffects.isOpponentLoseTwoInf(amId)
                                    || CardEffects.isCrisisOfSelf(amId)
                                    || CardEffects.isHealFaceDown(amId)
                                    || CardEffects.isBattleTested(amId)
                                    || CardEffects.isRescue(amId)
                                    || CardEffects.isMartyr(amId));
                        if (dispatched) {
                            CardEffects.applyAftermathEffect(state, conflict,
                                    am, target, winner);
                        } else {
                            applySimpleAftermathEffect(target, am, winner, state);
                        }
                        // After effect, discard one-shot aftermaths (non-permanent)
                        // Permanent aftermaths are identified by the word "permanently"
                        // in the card's text. They stay attached to the target.
                        if (!am.getText().toLowerCase().contains("permanently")) {
                            p.getDeck().discard(am);
                        }
                        break;
                    }
                }
            }
        }

        state.clearActiveConflict();
    }

    private void applyGenericCardPlay(Player p, Card card, boolean hidden) {
        if (card == null) return;
        // B5-1038: charge card cost BEFORE hand removal, following the
        // RECRUIT_CHARACTER affordance pattern (applyInfluence, log, break
        // without removing the card when unaffordable). Cost-0 cards behave
        // byte-identically to before (applyInfluence(0) is always successful).
        int cost = card.getCost();
        if (cost > 0 && !p.applyInfluence(cost)) {
            state.log(p.getName() + " cannot apply enough influence to play "
                      + card.getTitle() + ".");
            return;
        }
        // B5-0364: sponsor-agenda one-major legality (:520 — a faction may
        // sponsor an agenda only while it has NONE in play; with one installed
        // the action is REPLACE_AGENDA). Refused BEFORE the hand removal below,
        // so the card stays in hand.
        if (card instanceof AgendaCard && !rules.canSponsorAgenda(p, (AgendaCard) card)) {
            state.log(p.getName() + " cannot sponsor " + card.getTitle()
                    + " — one agenda in play at a time (:520); REPLACE instead.");
            return;
        }
        // B5-1999 (rulebook :496): a GROUP with a race in its Card Type is
        // RESTRICTED to that race — "Only the player controlling the race
        // listed as part of the Card Type may bring a restricted card into
        // play". This is an absolute refusal, NOT the :484 double-cost rule
        // that applies to characters. canSponsorGroup also re-checks hand
        // membership and affordability, so a group that reaches this handler
        // without passing the offer gate is still refused here rather than
        // half-played. Refused BEFORE the hand removal above, so the card
        // stays in hand.
        if (card instanceof GroupCard && !rules.canSponsorGroup(p, (GroupCard) card)) {
            state.log(p.getName() + " cannot sponsor " + card.getTitle()
                    + " — a race-restricted group is playable only by its own race (:496).");
            return;
        }
        p.removeFromHand(card);
        state.log(p.getName() + " plays " + card.getTitle());

        if (card instanceof EnhancementCard) {
            CardEffects.applyPlayEnhancement(state, p, (EnhancementCard) card);
        } else if (card instanceof LocationCard) {
            p.getLocations().add((LocationCard) card);
        } else if (card instanceof GroupCard) {
            p.getGroups().add((GroupCard) card);
        } else if (card instanceof AgendaCard) {
            p.setAgenda((AgendaCard) card);
            // B5-0464 (0459 proposal): emit a distinct, greppable install
            // line for face-up agendas. The multi-round runner's parser
            // counts the token " sets agenda:" — historically a dead token
            // with no emitter, which is why harness aggregates printed
            // agendas=0 while installs visibly occurred. Emitter and parser
            // bucket land in the same change (B5-0459 standing rule).
            if (!hidden) {
                state.log(p.getName() + " sets agenda: " + card.getTitle());
            }
            if (hidden) {
                // B5-0364/:520 — hidden agenda: no effect on play until revealed.
                card.setFaceDown(true);
                state.log(p.getName() + " sponsors a hidden agenda (face down).");
            } else {
                CardEffects.applyAgendaOnPlay(state, p, (AgendaCard) card);
            }
        } else if (card instanceof EventCard) {
            CardEffects.applyPlayEvent(state, p, card);
            p.getDeck().discard(card);
        } else {
            p.getDeck().discard(card);
        }
    }

    /**
     * B5-0338: deterministic D2 target for an AI aftermath play. Participant
     * aftermaths target the playing player himself when he is a conflict
     * participant (the common WON_PARTICIPANT/LOST_PARTICIPANT self case);
     * everything else targets the conflict's initiator — the rulebook's
     * "normally played only upon the faction that initiated" default.
     */
    private Player selectAftermathTarget(Player p, AftermathCard am, Conflict conflict) {
        if (am.isParticipantCondition()
                && conflict.getParticipants().contains(p)) return p;
        return conflict.getInitiator();
    }

    private void applySimpleAftermathEffect(Player player, AftermathCard am,
                                            Player winner, GameState gs) {
        String t = am.getTriggerCondition();
        if (t.contains("WON") && player == winner) player.gainInfluence(1);
        if (t.contains("LOST") && player != winner) player.drawCards(1);
        if (t.contains("PARTICIPANT")) player.drawCards(1);
    }

/**
     * B5-1998: the Draw Round is now the engine's whole round-lifecycle draw
     * step (rulebook III. :440-:465), replacing the bare 1-card drawPhase call.
     * drawPhase() is left in RulesEngine for its existing callers.
     */
    private void runDrawPhase() {
        state.setPhase(GamePhase.DRAW);
        Player winner = rules.drawRound(state);
        state.setPhase(GamePhase.END_ROUND);
        if (winner != null) state.setWinner(winner);
        notifyUI();
    }

    // ── Human input sync ─────────────────────────────────────────────────────

    public synchronized void submitHumanAction(GameAction action) {
        if (waitingForHumanConflictJoin) {
            if (action != null && (action.getType() == GameAction.Type.JOIN_CONFLICT_SUPPORT
                    || action.getType() == GameAction.Type.JOIN_CONFLICT_OPPOSE)) {
                pendingHumanConflictJoin = action;
                waitingForHumanConflictJoin = false;
                notifyAll();
            }
            return;
        }
        if (waitingForHumanConflictAttack) {
            Player human = state.getHumanPlayer();
            boolean validAttack = action != null
                    && action.getType() == GameAction.Type.ATTACK_CONFLICT_PARTICIPANT
                    && rules.canAttackConflictParticipant(human, action.getCard(),
                            action.getTargetCard(), state.getActiveConflict());
            if (action != null && (action.getType() == GameAction.Type.PASS || validAttack)) {
                pendingHumanConflictAttack = action;
                waitingForHumanConflictAttack = false;
                notifyAll();
            }
            return;
        }
        // Join actions are only valid in the explicit conflict decision window;
        // ignore stale/double clicks after the controller has collected a side.
        if (action != null && (action.getType() == GameAction.Type.JOIN_CONFLICT_SUPPORT
                || action.getType() == GameAction.Type.JOIN_CONFLICT_OPPOSE
                || action.getType() == GameAction.Type.ATTACK_CONFLICT_PARTICIPANT)) return;
        pendingHumanAction = action;
        waitingForHuman    = false;
        notifyAll();
    }

    private synchronized GameAction waitForHumanAction() {
        waitingForHuman    = true;
        pendingHumanAction = null;
        notifyUI();
        while (waitingForHuman) {
            try { wait(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        return pendingHumanAction != null ? pendingHumanAction : GameAction.pass();
    }

    public boolean isWaitingForHuman() { return waitingForHuman; }

    private boolean hasLegalHumanConflictAttack(Player human, Conflict conflict) {
        List<Card> attackers = new ArrayList<Card>();
        if (human.getAmbassador() != null) attackers.add(human.getAmbassador());
        attackers.addAll(human.getInnerCircle());
        attackers.addAll(human.getSupportingRole());
        attackers.addAll(human.getFleets());
        attackers.addAll(human.getLocations());
        attackers.addAll(human.getGroups());
        attackers.addAll(human.getEnhancements());
        if (human.getAgenda() != null) attackers.add(human.getAgenda());
        for (Card attacker : attackers) {
            for (Player participant : conflict.getParticipants()) {
                for (Card target : conflict.getCommittedCards(participant)) {
                    if (rules.canAttackConflictParticipant(human, attacker, target, conflict)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private synchronized GameAction waitForHumanConflictJoin() {
        waitingForHumanConflictJoin = true;
        pendingHumanConflictJoin = null;
        notifyUI();
        while (waitingForHumanConflictJoin) {
            try { wait(200); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                waitingForHumanConflictJoin = false;
            }
        }
        return pendingHumanConflictJoin;
    }

    /** True only while the active conflict is waiting for the human side choice. */
    public boolean isWaitingForHumanConflictJoin() {
        return waitingForHumanConflictJoin;
    }

    private synchronized GameAction waitForHumanConflictAttack() {
        waitingForHumanConflictAttack = true;
        pendingHumanConflictAttack = null;
        notifyUI();
        while (waitingForHumanConflictAttack) {
            try { wait(200); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                waitingForHumanConflictAttack = false;
            }
        }
        return pendingHumanConflictAttack;
    }

    public boolean isWaitingForHumanConflictAttack() {
        return waitingForHumanConflictAttack;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Enforces hand limit of 7 cards at end of turn (B5-1968). */
    private void endOfTurn(Player p) {
        List<Card> hand = p.getHand();
        while (hand.size() > 7) {
            Card toDiscard = hand.remove(hand.size() - 1);
            p.getDeck().discard(toDiscard);
            state.log(p.getName() + " discards " + toDiscard.getTitle() + " (end-of-turn hand limit).");
        }
    }

    private AIPlayer getAI(Player p) {
        for (AIPlayer ai : aiPlayers) {
            if (ai.getPlayer() == p) return ai;
        }
        return aiPlayers.get(0);
    }

    private void notifyUI() {
        if (uiCallback != null) uiCallback.accept(state);
    }

    private void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public GameState getState() { return state; }
}
