package b5ccg.engine;

import b5ccg.ai.AIPlayer;
import b5ccg.model.*;
import b5ccg.model.enums.*;
import java.util.*;


/**
 * Central game loop. Runs off the Swing EDT via a background thread.
 * Notifies the UI through a GameStateCallback callback after each state change.
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
            // Place ambassador
            CharacterCard amb = findAmbassador(p);
            if (amb != null) {
                p.getHand().remove(amb);
                p.setAmbassador(amb);
            }
            // Starting hand: draw 3 more
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
                pause(600); // Brief delay so the human can see AI thinking
            }

            processAction(current, action);
            notifyUI();

            if (action.getType() == GameAction.Type.PASS) {
                current.setPassed(true);
                passCount++;
            } else {
                passCount = 0; // Reset: someone acted
            }

            // Check victory after every action
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
                    p.removeFromHand(cc);
                    Conflict conflict = new Conflict(cc, p);
                    // Auto-commit ambassador
                    if (p.getAmbassador() != null && !p.getAmbassador().isFaceDown()) {
                        conflict.commitCard(p, p.getAmbassador());
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

            default:
                break;
        }

        p.useAction();
    }

    private void resolveCurrentConflict() {
        Conflict conflict = state.getActiveConflict();
        if (conflict == null) return;

        // AI players decide whether to join
        for (Player p : state.getPlayers()) {
            if (p == conflict.getInitiator()) continue;
            if (p.isHuman()) continue; // human joins via UI
            AIPlayer ai = getAI(p);
            if (ai.shouldJoinConflict(state, p, conflict)) {
                conflict.addParticipant(p);
                if (p.getAmbassador() != null) conflict.commitCard(p, p.getAmbassador());
            }
        }

        Player winner = rules.resolveConflict(conflict, state);
        state.setPhase(GamePhase.AFTERMATH);
        notifyUI();

        // Simple aftermath: AI plays one eligible aftermath card
        for (Player p : state.getPlayers()) {
            if (p.isHuman()) continue;
            boolean won = (winner == p);
            for (Card c : new ArrayList<Card>(p.getHand())) {
                if (c instanceof AftermathCard) {
                    AftermathCard am = (AftermathCard) c;
                    if (rules.canPlayAftermath(p, am, conflict, won)) {
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

        if (card instanceof EnhancementCard) {
            p.getEnhancements().add((EnhancementCard) card);
        } else if (card instanceof LocationCard) {
            p.getLocations().add((LocationCard) card);
        } else if (card instanceof GroupCard) {
            p.getGroups().add((GroupCard) card);
        } else if (card instanceof AgendaCard) {
            p.setAgenda((AgendaCard) card);
        } else if (card instanceof EventCard) {
            // Apply simple event effect: draw a card
            p.drawCards(1);
            p.getDeck().discard(card);
        } else {
            p.getDeck().discard(card);
        }
        state.log(p.getName() + " plays " + card.getTitle());
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
