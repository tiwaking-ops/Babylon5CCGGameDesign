package b5ccg.ai;

import b5ccg.model.*;
import b5ccg.model.enums.*;
import java.util.*;

/**
 * AI player for three difficulty tiers: EASY, MEDIUM, HARD.
 *
 * EASY:   Random legal action.
 * MEDIUM: Greedy — prefers actions that maximise immediate Influence gain.
 * HARD:   Evaluative — scores board state, uses opponent awareness,
 *         targets the leading player, protects against threats.
 */
public class AIPlayer {

    private final Player      player;
    private final AIDifficulty difficulty;
    private final Random      rng = new Random();

    public AIPlayer(Player player, AIDifficulty difficulty) {
        this.player     = player;
        this.difficulty = difficulty;
    }

    public Player      getPlayer()    { return player; }
    public AIDifficulty getDifficulty() { return difficulty; }

    // ── Main decision entry point ─────────────────────────────────────────────

    public GameAction chooseAction(GameState state, Player p) {
        List<GameAction> legal = buildLegalActions(state, p);
        if (legal.isEmpty()) return GameAction.pass();

        switch (difficulty) {
            case EASY:   return easyChoose(legal);
            case MEDIUM: return mediumChoose(legal, state, p);
            case HARD:   return hardChoose(legal, state, p);
            default:     return GameAction.pass();
        }
    }

    public boolean shouldJoinConflict(GameState state, Player p, Conflict conflict) {
        switch (difficulty) {
            case EASY:   return rng.nextBoolean();
            case MEDIUM: return mediumJoin(state, p, conflict);
            case HARD:   return hardJoin(state, p, conflict);
            default:     return false;
        }
    }

    // ── Legal action builder ──────────────────────────────────────────────────

    private List<GameAction> buildLegalActions(GameState state, Player p) {
        List<GameAction> actions = new ArrayList<GameAction>();
        actions.add(GameAction.pass());

        for (Card c : p.getHand()) {
            if (!c.getFaction().isPlayableBy(p.getFaction())) continue;

            if (c instanceof ConflictCard) {
                // Rulebook §V: "Each faction may normally initiate only one conflict per turn."
                // Skip conflict initiation if a conflict is already active this turn.
                if (state.getActiveConflict() == null) {
                    for (Player target : state.getPlayers()) {
                        if (target != p) {
                            actions.add(GameAction.initiateConflict(c, target));
                        }
                    }
                }
            } else if (c instanceof CharacterCard) {
                actions.add(GameAction.recruitCharacter(c));
            } else {
                actions.add(GameAction.playCard(c));
            }
        }
        return actions;
    }

    // ── EASY ──────────────────────────────────────────────────────────────────

    private GameAction easyChoose(List<GameAction> legal) {
        // 30% chance to pass even if other actions exist
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
                    // Score = reward * probability-proxy
                    return myTotal > oppTotal
                        ? cc.getInfluenceReward() * 10 + (myTotal - oppTotal)
                        : -5;
                }
                return 0;
            case RECRUIT_CHARACTER:
                return 5; // Recruiting is generally good
            case PLAY_CARD:
                if (a.getCard() instanceof AgendaCard) return 8;
                if (a.getCard() instanceof LocationCard) return 6;
                if (a.getCard() instanceof GroupCard)    return 4;
                return 2;
            case PASS:
                return 0;
            default:
                return 1;
        }
    }

    private boolean mediumJoin(GameState state, Player p, Conflict conflict) {
        int myTotal  = p.conflictTotal(conflict.getConflictType());
        int oppTotal = conflict.getInitiator().conflictTotal(conflict.getConflictType());
        // Join to support if we'd benefit, oppose if initiator has lead
        return myTotal >= oppTotal / 2;
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
                // Bonus for targeting the leader
                double leaderBonus = (opp == leader) ? 3.0 : 0;
                // Penalty if likely to lose
                double lossPenalty = myTotal < oppTotal ? -3.0 : 0;
                return base + leaderBonus + lossPenalty;
            }
            case RECRUIT_CHARACTER: {
                if (!(a.getCard() instanceof CharacterCard)) return 3;
                CharacterCard ch = (CharacterCard) a.getCard();
                // Score by best stat for our conflict strategy
                int maxStat = Math.max(Math.max(ch.getDiplomacy(), ch.getIntrigue()),
                              Math.max(ch.getPsi(), ch.getLeadership()));
                return 4 + maxStat * 0.5;
            }
            case PLAY_CARD:
                if (a.getCard() instanceof AgendaCard)     return 9;
                if (a.getCard() instanceof LocationCard)   return 7;
                if (a.getCard() instanceof GroupCard)      return 5;
                if (a.getCard() instanceof EnhancementCard) return 4;
                if (a.getCard() instanceof EventCard)      return 3;
                return 2;
            case PASS:
                // Slight negative: passing concedes initiative
                return -0.5;
            default:
                return 1;
        }
    }

    private boolean hardJoin(GameState state, Player p, Conflict conflict) {
        Player initiator = conflict.getInitiator();
        int initInfluence = initiator.getInfluence();
        int myInfluence   = p.getInfluence();

        // Always oppose the leader if we can
        Player leader = leadingPlayer(state, p);
        if (initiator == leader) {
            int myTotal  = p.conflictTotal(conflict.getConflictType());
            int oppTotal = initiator.conflictTotal(conflict.getConflictType());
            return myTotal >= oppTotal * 0.75;
        }

        // Support initiator if they're far behind and we might benefit
        if (initInfluence < myInfluence - 4) return rng.nextBoolean();

        return false;
    }

    /** Returns the player with the most influence (excluding self). */
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

    // ── Pseudocode summary (comment) ─────────────────────────────────────────
    /*
     * HARD AI decision tree:
     * 1. Identify the leading opponent.
     * 2. Score every legal action using scoreActionHard().
     * 3. Conflict score = winProbability × reward + leaderBonus - lossPenalty.
     * 4. Permanent card plays (Agenda, Location, Group) score 5–9.
     * 5. Recruiting values characters by their peak stat.
     * 6. Passing scores slightly negative to discourage early passes.
     * 7. Choose the highest-scoring action.
     */
}
