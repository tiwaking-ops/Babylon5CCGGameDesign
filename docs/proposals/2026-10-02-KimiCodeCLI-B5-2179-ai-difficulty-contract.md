author_llm: {name: "Kimi Code CLI", version: "unknown"}

# AI Difficulty Behavioral Contract

This proposal documents the behavioral contract for the Babylon 5 CCG AI across its three difficulty tiers: EASY, MEDIUM, and HARD.

## Difficulty Tiers

The AI behavior is determined by the `AIDifficulty` enum, with distinct strategies implemented in `AIPlayer.chooseAction` and supporting scoring methods.

### 1. EASY
*   **Behavior:** Random selection among all legal actions.
*   **Scoring:** No scoring logic.
*   **Pass Bias:** 20% skip chance for legal actions (excluding PASS).

### 2. MEDIUM
*   **Behavior:** Greedy. Prefers actions that maximize immediate Influence gain or board position value.
*   **Scoring:**
    *   Uses a greedy scoring function (`scoreActionMedium`) that evaluates immediate reward for conflict initiation, recruitment, and card plays.
    *   Subtracts card costs from positional value (B5-0324, B5-0323).
    *   Considers unrest pressure and civil war merge exposure (B5-0727).
    *   Includes a modest preference for DIPLOMACY-type conflicts (B5-1709).
*   **Pass Bias:** Does not proactively pass; the engine's pass-loop handles consecutive passes.

### 3. HARD
*   **Behavior:** Evaluative. Uses board state scoring, opponent awareness, and strategic weighting.
*   **Scoring:**
    *   Uses an evaluative scoring function (`scoreActionHard`) that considers win probability and opponent threat levels (B5-1974).
    *   Evaluates conflict initiation based on win probability (`winProb`), threat targets (B5-1974), and military posture (B5-0309).
    *   Considers station influence and major victory urgency (B5-0453, B5-0635).
    *   Stronger penalty for feeding influence to a winning rival (B5-0679).
    *   Leans MILITARY in conflict types (B5-1709).
*   **Pass Bias:** Similar to MEDIUM, but with more sophisticated scoring to ensure optimal play.

## Verification
The behavior of each tier can be verified using the `HeadlessAIDifficultyContractTest` harness, which asserts legality (0 illegal moves), non-determinism for EASY, and cost-aware ordering for MEDIUM and HARD.
