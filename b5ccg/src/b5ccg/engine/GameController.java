package b5ccg.engine;

import b5ccg.ai.AIPlayer;
import b5ccg.model.*;
import b5ccg.model.enums.*;
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

            runDrawPhase();
            state.advanceRound();
            notifyUI();
        }
    }

    private void setupGame() {
        state.setPhase(GamePhase.SETUP);
        for (Player p : state.getPlayers()) {
            CharacterCard amb = findAmbassador(p);
            if (amb != null) {
                p.getHand().remove(amb);
                p.setAmbassador(amb);
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

        while (passCount < playerCount && !state.isGameOver()) {
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

            if (action.getType() == GameAction.Type.PASS) {
                current.setPassed(true);
                passCount++;
            } else {
                passCount = 0;
            }

            Player winner = rules.checkVictory(state);
            if (winner != null) {
                state.setWinner(winner);
                notifyUI();
                return;
            }

            state.advanceTurn();
        }
    }

    private void processAction(Player p, GameAction action) {
        state.log(p.getName() + ": " + action);

        switch (action.getType()) {
            case PASS:
                break;

            case INITIATE_CONFLICT:
                if (action.getCard() instanceof ConflictCard) {
                    ConflictCard cc = (ConflictCard) action.getCard();
                    // B5-0302: engine-side enforcement of one-conflict-per-turn
                    // (rulebook "Conflicts"). Rejected card stays in hand.
                    if (!rules.canInitiateConflict(p, cc, state)) {
                        state.log(p.getName() + " cannot initiate " + cc.getTitle()
                                  + " — one conflict per turn.");
                        break;
                    }
                    p.removeFromHand(cc);
                    state.markConflictInitiated(p);
                    Conflict conflict = new Conflict(cc, p);
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
                applyGenericCardPlay(p, action.getCard());
                break;

            case RECRUIT_CHARACTER:
                if (action.getCard() instanceof CharacterCard) {
                    CharacterCard ch = (CharacterCard) action.getCard();
                    p.removeFromHand(ch);
                    p.placeInSupportingRole(ch);
                    state.log(p.getName() + " recruits " + ch.getTitle());
                }
                break;

            case BUILD_INFLUENCE:
                if (action.getCard() instanceof CharacterCard) {
                    CharacterCard leader = (CharacterCard) action.getCard();
                    rules.executeBuildInfluence(p, leader, state);
                }
                break;

            default:
                break;
        }

        p.useAction();
    }

    private void resolveCurrentConflict() {
        Conflict conflict = state.getActiveConflict();
        if (conflict == null) return;

        for (Player p : state.getPlayers()) {
            if (p == conflict.getInitiator()) continue;
            if (p.isHuman()) continue;
            AIPlayer ai = getAI(p);
            if (ai.shouldJoinConflict(state, p, conflict)) {
                conflict.addParticipant(p, false);                      // B5-0309: joiners oppose
                if (p.getAmbassador() != null) {
                    conflict.commitCard(p, p.getAmbassador(), false);   // B5-0309
                }
            }
        }

        Player winner = rules.resolveConflict(conflict, state);

        Player primaryLoser = null;
        if (conflict.getInitiator() != winner) {
            primaryLoser = conflict.getInitiator();
        } else {
            for (Player q : conflict.getParticipants()) {
                if (q != winner) { primaryLoser = q; break; }
            }
        }
        if (primaryLoser != null) {
            CardEffects.applyConflictOutcome(state, conflict, winner, primaryLoser);
        }
        if (conflict.getConflictType() == ConflictType.DIPLOMACY) {
            int agendaBonus = CardEffects.agendaDiplomacyWinBonus(winner);
            if (agendaBonus > 0) {
                winner.gainInfluence(agendaBonus);
                state.log(winner.getName() + " gains " + agendaBonus
                        + " influence from agenda (Diplomacy win).");
            }
        }

        state.setPhase(GamePhase.AFTERMATH);
        notifyUI();

        for (Player p : state.getPlayers()) {
            if (p.isHuman()) continue;
            boolean initiatorWon = rules.initiatorWon(conflict, winner);
            for (Card c : new ArrayList<Card>(p.getHand())) {
                if (c instanceof AftermathCard) {
                    AftermathCard am = (AftermathCard) c;
                    if (rules.canPlayAftermath(p, am, conflict, initiatorWon)) {
                        p.removeFromHand(am);
                        p.getDeck().discard(am);
                        state.log(p.getName() + " plays aftermath: " + am.getTitle());
                        applySimpleAftermathEffect(p, am, winner, state);
                        break;
                    }
                }
            }
        }

        state.clearActiveConflict();
    }

    private void applyGenericCardPlay(Player p, Card card) {
        if (card == null) return;
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
            CardEffects.applyAgendaOnPlay(state, p, (AgendaCard) card);
        } else if (card instanceof EventCard) {
            CardEffects.applyPlayEvent(state, p, card);
            p.getDeck().discard(card);
        } else {
            p.getDeck().discard(card);
        }
    }

    private void applySimpleAftermathEffect(Player player, AftermathCard am,
                                            Player winner, GameState gs) {
        String t = am.getTriggerCondition();
        if (t.contains("WON") && player == winner) player.gainInfluence(1);
        if (t.contains("LOST") && player != winner) player.drawCards(1);
        if (t.contains("PARTICIPANT")) player.drawCards(1);
    }

    private void runDrawPhase() {
        state.setPhase(GamePhase.DRAW);
        rules.drawPhase(state);
        state.setPhase(GamePhase.END_ROUND);
        notifyUI();
    }

    // ── Human input sync ─────────────────────────────────────────────────────

    public synchronized void submitHumanAction(GameAction action) {
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

    // ── Helpers ───────────────────────────────────────────────────────────────

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
