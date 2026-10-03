---
document:
  title: "B5-2223 — Tie-breaking rules catalog: every tie the rulebook creates, with the rulebook section and the code witness for each"
  status: "Proposal"
provenance:
  author_llm: {name: "Hermes (stealth-space-bunny-alpha) 2258", version: "stealth-space-bunny-alpha"}
  assessor_llm: []
  last_modified_by_llm: {name: "Hermes (stealth-space-bunny-alpha) 2258", version: "stealth-space-bunny-alpha"}
  created_date: "2026-10-03"
  last_modified_date: "2026-10-03"
---

# B5-2223 — Tie-breaking rules catalog

Scope of this deliverable: `docs/proposals/` plus read-only evidence from
`b5ccg/src/`. **No src file was created, edited or deleted**, per the row.
Every line reference below is a working-tree reading taken during this pass
with foreign uncommitted edits present (see §5).

**Relationship to B5-2157.** B5-2157 is BLOCKED and asked for the *evaluation
order* across last-standing, standard and agenda victory paths — a **precedence
map**. This catalog deliberately does not draw that map. It records, per tie
class, only three things: what the rulebook says, what the code does with a tie
there, and what kind of tie-break (if any) the rulebook itself supplies. Where
a tie's resolution depends on which victory path fires first, that dependency is
named as a pointer to B5-2157, not answered.

## 0. The four tie-break shapes actually in use

Every tie the rulebook creates in this implementation resolves one of four
ways. Naming the shapes once makes the catalog scannable:

| Shape | Rule | Where it shows up |
|---|---|---|
| **STRICT** | a tie means *no one wins* — play continues | standard victory, major victory, mercenary control, agenda-condition-free comparisons |
| **CONSERVATIVE** | the rulebook is silent, so a tie resolves to the least assertive outcome | leading opposition participant, reporting tiebreak step 4 |
| **ESCALATE** | a tie hands the decision to a named third party | League of Non-Aligned Worlds council tie-break, B5-5 station condition 2 |
| **ORDER-DEPENDENT** | the tie resolves to whichever candidate the code iterated first — an artifact, not a rule | `leadingOpposer` |

## 1. Conflict ties (rulebook §Conflicts :550, glossary **Won** :1210)

### 1.1 Support-vs-opposition — STRICT, the initiator loses

Rulebook :550: *"If a conflict receives more support than opposition, then the
faction which initiated it 'wins' the conflict. If a conflict receives the same
amount or more opposition than support, then the faction which initiated it
'loses' the conflict."* The glossary at :1210 repeats it and adds the
non-opposable variant.

Code witness: `RulesEngine.resolveConflict`, `b5ccg/src/b5ccg/engine/RulesEngine.java:523-528`.

```
523|        if (conflict.supportTotal() > conflict.oppositionTotal()
524|                || conflict.oppositionTotal() == 0) {
525|            winner = conflict.getInitiator();
526|        } else {
527|            winner = leadingOpposer(conflict, totals);
528|        }
```

A support-equals-opposition tie sends the conflict down the `else` branch, so
**the initiator loses the tie**. `conflict.oppositionTotal() == 0` is an
uncontested-conflict carve-out, not a tie rule.

### 1.2 Two opposers on the same total — ORDER-DEPENDENT, the sharpest finding

Once the initiator has lost, the winner is the opposition participant with the
highest committed total. `leadingOpposer` at `RulesEngine.java:774-784` picks
that participant with a strict `>` scan:

```
774|    /** The opposition participant with the highest total (insertion order
775|     *  breaks ties); only called when opposition strictly exceeds support. */
776|    private Player leadingOpposer(Conflict conflict, Map<Player, Integer> totals) {
...
781|            if (v > bestVal) { bestVal = v; best = p; }
```

The comment is honest — it says insertion order breaks ties — but nothing in the
rulebook authorises an order-based winner, and no DECISIONS entry records one.
Three of the four shapes above have a stated rationale (D12 discipline,
"conservative default", the League rule); this one has only a comment. The
iteration order is `Conflict.getOpposers()`, i.e. **join order within the
conflict**, not seat order and not a rulebook quantity, so the winner of a tied
opposition is a function of who joined the conflict first.

Note the interaction with §1.1: because the `else` branch is entered on a tie
too, an initiator who ties *and* faces a tied opposition gets a winner chosen
by join order. This is a real reachable state, not a degenerate one.

Recommendation for a future engine-scoped row: either crown nobody on a tied
opposition (STRICT, consistent with §1.1) or adopt a stated rulebook-anchored
quantity. Do **not** leave it to insertion order.

### 1.3 The non-opposable, multiple-side variant — NOT IMPLEMENTED

Rulebook :1210 defines **Won** twice: the opposition comparison above, and *"or
more support than any other side (for non-opposable, support multiple side
conflicts)."* `ConflictType` (`b5ccg/src/b5ccg/model/enums/ConflictType.java:3-4`)
has exactly four constants — `DIPLOMACY, INTRIGUE, MILITARY, PSI` — and
`Conflict` has a two-sided model only (`supporters` / `opposers`, with
`supportTotal()` at :255 and `oppositionTotal()` at :262). **There is no
multi-side support structure in the code, so the second "Won" rule has no
implementation and no reachable tie.** Recorded so the catalog is not read as
claiming coverage: a card that is non-opposable and support-multiple would be
resolved by the opposition rule instead, which is a different rule.

### 1.4 Loser-side ties are not ties

The 3-point ambassador-damage gap at `RulesEngine.java:549-556` compares
`winVal` to each loser's total, so tied losers are damaged together. That is
not a tie needing a break — it is a shared consequence. Noted for completeness
only.

## 2. Victory ties (rulebook §Victory :179, :180, :186, :825)

All three victory predicates share one strict-comparison helper,
`strictlyLeads` at `RulesEngine.java:1024-1035`, whose single line
`if (q.getInfluence() >= p.getInfluence()) return false;` is the STRICT shape
in code.

### 2.1 Standard victory condition 1 — STRICT, a tie crowns nobody

Rulebook :179: *"Have 20 Power, and more than any other player."*
Code: `standardVictory` at `RulesEngine.java:994-998`, delegating the comparison
to `strictlyLeads(state, p, false)` at :997. A tie with any non-forfeited,
non-surrendered opponent returns false. Rationale recorded in DECISIONS
(D12 discipline) and in the method comment at :985-987.

### 2.2 Standard victory condition 2 — ESCALATE plus STRICT, and currently unreachable

Rulebook :180: *"If Babylon 5 has an Influence Rating of 20 or more at the end
of a turn — and one player eligible to win a standard victory is leading in
Power — then that player wins."*
Code: `stationVictory` at `RulesEngine.java:965-982`. Two tie rules in one method:

* **"one player"** is enforced by counting leaders and requiring exactly one:
  `return leaders == 1 ? leader : null;` at :981. Two players tied at the top
  produces `leaders == 2`, so the station crowns **nobody**. This is the
  rulebook's own wording, not an interpretation.
* Eligibility narrows the comparison: `strictlyLeads(state, p, true)` at :976
  excludes major-agenda players from the tie-blocking comparison (:1028-1030),
  and `state.isShadowWar()` at :966 plus the :178 guard make the whole path
  inert during the Shadow War.

**Reachability caveat, stated as read not measured:** the method comment at
:960-963 records that the station's Influence Rating starts at 0 and nothing
moves it (B5-0354 research), so this path "can never fire" today. The tie rule
above is therefore specified but unexercised.

### 2.3 Major victory — STRICT, and the gap is 10 not 1

Rulebook :186: *"Have at least 20 Power, and at least 10 more than each other
player."*
Code: `majorVictory` at `RulesEngine.java:1012-1019`, the comparison at :1016
being `if (q.getInfluence() > p.getInfluence() - 10) return false;`. A tie and a
9-point lead are both crowned by nobody; only a lead of 10 or more wins. This is
one of the few places where the code's strictness is *stricter* than a naive
reading, and the comment at :1000-1004 records the interpretation (forfeited
**and** surrendered players excluded, per B5-0661).

### 2.4 Last-standing / all-surrendered — no tie is possible

Rulebook :825: *"If all other players have surrendered, the last remaining player
in the game scores a Major Victory."* Code: `checkVictory` at
`RulesEngine.java:789-796` counts non-forfeited players and returns at :796 only
when `remaining == 1`. With two or more remaining, no tie-break is needed and
none is written. The all-surrendered variant at :812-814 requires
`activeRemaining == 1` for the same reason. A tie here is unreachable by
construction, which is worth recording precisely because it is the one victory
path that needs no tie rule.

### 2.5 Hidden-agenda ordering — a precedence question, fenced to B5-2157

`checkVictory`'s per-player scan at `RulesEngine.java:830-856` interleaves three
predicates per player (agenda condition at :838, major victory at :849, standard
victory at :853-855), and `stationVictory` runs before the scan at :827-828. Two
players can satisfy *different* paths on the same check, and which one is
returned is a question about **path order**, not about ties. That is B5-2157's
deliverable and is deliberately not answered here.

## 3. Council ties (rulebook §Votes :789-:797)

### 3.1 The League tie-break — ESCALATE, the only rulebook-supplied tie-break

Rulebook :791: *"the League of Non-Aligned worlds (acting as if it were a single
race) may cast one vote to break any tie."* Rulebook :797: *"There must be at
least one more 'Yes' than 'No' vote for a measure to pass."*
Code: `resolveCouncilVote` at `RulesEngine.java:2465-2528`, the tie-break at
:2515-2518:

```
2515|        if (yes == no) {
2516|            yes++; // League breaks tie in favor of the measure
2517|            state.log("League of Non-Aligned Worlds breaks tie with Yes vote.");
2518|        }
```

then `boolean passed = (yes >= no + 1);` at :2520.

**The sharpest finding in this catalog.** The rulebook says the League *may*
cast a vote to break a tie. The code does not model the League as a voter with
a ballot at all — `councilRaces` at :2474-2476 is the five ambassadors only, and
the League's vote is synthesised as a bare `yes++` at :2516. Two consequences:

1. **The tie always breaks toward passage.** After `yes++`, `yes == no + 1`, so
   :2520 is satisfied and the measure passes. "May cast one vote to break any
   tie" is a *permission*, and the code reads it as an obligation to vote Yes.
   A reading in which the League may also vote No is not implemented, and the
   in-code rationale at :2510-2514 says so in as many words ("we interpret this
   as: if Yes == No, the League votes Yes").
2. **A 0-0 tie is a tie.** With all five ambassadors abstaining or with no
   ballots cast, `yes == no == 0`, the League fires, and the measure passes on a
   body that cast no votes. Unplayed races abstain by default (:2490-2504,
   rulebook :793), so a table with a single playing race reaches 0-0 easily.

Point 2 is stated as a code fact read from the branch condition, not as an
observed game outcome — this row is read-only and the build is red, so no probe
was run (see §5).

### 3.2 Council membership and the head — not ties

`councilHead` at `RulesEngine.java:2294-2307` implements rulebook :795's two-step
head rule (Earth Alliance ambassador, else the player of the requiring card,
else null). It is an ordering surface, not a tie-break, and is listed here only
because it is the other half of the §Votes surface and readers will look for it.

### 3.3 Fenced, not catalogued

B5-1993, B5-1994 and B5-2001 hold live claims over the council vote and ballot
surfaces. The code comment at :2253-2263 explicitly fences the one-more-Yes rule,
the League tie-break and unplayed-race abstention to B5-1993's
`resolveCouncilVote`, and forbids building a second tally authority. §3.1 reads
that fenced method read-only and duplicates nothing; no tally was proposed and
no hook was designed.

## 4. Other tie classes found in the engine

Not named in the row, recorded so the catalog is complete rather than
selective.

### 4.1 Mercenary control — STRICT, a tie means no one controls

Rulebook :739: *"Their action is dictated by the faction which applied the most
influence during the turn (bids are cumulative)."* The rulebook does not say
what happens on equal bids.
Code: `GameState.resolveMercenaries`, `b5ccg/src/b5ccg/model/GameState.java:545-568`:

```
550|            boolean tie = false;
...
557|                } else if (bid == best && bid > 0) {
558|                    tie = true;
...
561|            if (tie || best <= 0) controller = null;
```

Explicit `tie` flag, and :561 crowns nobody. The rationale is recorded in
`RulesEngine.java:1817-1824` as a conservative default (D12 discipline, rulebook
silent on ties). Note the `bid > 0` guard at :557 — a 0-bid player is never
recorded as tying, so a table where nobody bid produces `best <= 0` and no
controller, which is the same outcome reached by a different branch.

### 4.2 Reporting tiebreak (B5-0350 Option C) — CONSERVATIVE, a four-step chain

Not a rulebook rule at all; a proposal adopted as a report-layer surface in
`docs/proposals/tiebreak-agenda-victory-design-proposal.md` (B5-0332 §2).
Code: `HeadlessReportingTiebreakTest.evaluateAtRoundCap`,
`b5ccg/src/b5ccg/engine/HeadlessReportingTiebreakTest.java:118-189`.

The chain is the only place in the codebase where a tie is *deliberately broken*:

* step 0 (:130-139) — the engine's own winner ends reporting; the tiebreak
  never outranks the engine.
* trigger gate (:141-150) — fewer than 2 players at 20+ is `NOT_APPLICABLE`.
* steps 1-3 (:152-185) — fleet Military conflict total, then Inner Circle size,
  then influence gained over the run. Each step narrows the pool to the players
  tied on the maximum so far (`keepMax`), and a pool of 1 decides.
* **step 4 (:186-188) — still tied means a SHARED victory** among the whole pool,
  not a single winner.

Steps 1-3 are *narrowing* rules, so no rule selects among equals; step 4 is
where equals are finally resolved, and it resolves them to a joint outcome. That
is CONSERVATIVE in the §0 sense and the honest label.

## 4.3 Corrected line-citation set for the victory surface (handoff from B5-2157)

B5-2157 (BLOCKED, `docs/DECISIONS.md` entry of 2026-10-02) recorded stale
line citations in the `checkVictoryPath` comments and the `executeForfeit`
javadoc, and named **this row** as the natural home for a corrected set. It is
a read-only deliverable, so what follows is the corrected set as a table, not
an edit — repairing the comments is an engine-scoped row of its own.

### 4.3.1 `checkVictoryPath` comment citations — all five wrong, all ~230 lines low

`checkVictoryPath` now occupies `RulesEngine.java:875-951` and `checkVictory`
occupies `:788-858`. The per-path comments inside `checkVictoryPath` still cite
the positions `checkVictory` occupied before an earlier insertion.

| Comment line | Says | Actually at | Correct citation for |
|---|---|---|---|
| :880 | `checkVictory lines 560–568` | :560 is `for (Card c : pCards) {` inside conflict loser handling | last-standing block, `:789-796` |
| :891 | `checkVictory line 599` | :599 is a bare `}` | station condition 2 call, `:827-828` (definition `:965-982`) |
| :913 | `checkVictory lines 610–613` | :610 is a javadoc line about location capture | agenda-condition branch, `:830-841` |
| :920 | `checkVictory line 621` | :621 is `|| conflict.anyAttackOccurred()` | major-victory call, `:849` (definition `:1012-1019`) |
| :934 | `checkVictory lines 625–626` | :625 is a `B5-0691` comment line | standard-victory call, `:853-855` (definition `:994-998`) |

Every cited line number is currently occupied by unrelated code, so a reader
following any one of them lands in conflict resolution or location capture and
cannot detect the staleness from the landing site.

### 4.3.2 `executeForfeit` javadoc `:1140-1142` — two of five cite the wrong FILE

The javadoc reads `(RulesEngine.java:682, :698, :774, :861, :877)` for
`isPlayerActive`, `activePlayersCount`, checkVictory's last-standing check, and
the station/tiebreak win conditions. The first two are **not in RulesEngine at
all** — both are `GameState` methods — so the `RulesEngine.java:` prefix is
wrong for two of the five regardless of the line drift:

| Cites | Should be | What it is |
|---|---|---|
| `isPlayerActive` | `GameState.java:293` | `p != null && !p.hasForfeited() && !p.hasSurrendered()` |
| `activePlayersCount` | `GameState.java:310` | same predicate, counted |
| last-standing check | `RulesEngine.java:789-796` | the `remaining == 1` return |
| station condition 2 | `RulesEngine.java:827-828` (def. `:965-982`) | called before the per-player scan |
| tiebreak win conditions | `RulesEngine.java:1024-1035` | `strictlyLeads`, the shared tie predicate |

This is a distinct defect class from the drift in §4.3.1 and worth separating:
a **wrong line number** is a stale pointer, while a **wrong file** is a wrong
claim about ownership, and no amount of re-measuring lines fixes it.

### 4.3.3 The `checkVictoryPath` javadoc `:869-873` is one path short

It inventories five paths — last standing, station, agenda, major, standard —
and omits the B5-0661 surrender block at `:805-814` entirely. The code still
reports that state correctly, because the path-reporting logic routes it to
MAJOR; the prose is short by one test, not wrong in its conclusion. Same
inventory-vs-code gap as B5-2157 recorded, restated here only so the corrected
set is complete.

**No behaviour depends on any number in §4.3.** These are comment-accuracy
defects: no gate reads a comment, so a correct tree and a stale-cited tree
compile and run identically. That is exactly why they survive — and why the
correction has to be filed as a proposal rather than found by the build.

## 5. Gate state, and what this catalog could not do

**`bash b5ccg/compile.sh` is RED**, exit 1, two errors, both
`b5ccg/src/b5ccg/ui/MainWindow.java:2922 cannot find symbol: class
DeckBuilderDialog`. `ui/` is outside this row's scope, `MainWindow.java` is `MM`
in git status, and live claim `.agent/CLAIMS/B5-2007.json` (Cline
(space-bunny-free), scope `ui/`) covers it. Left alone per AGENTS.md section 5.
The row is therefore closed **BLOCKED** per 00_BOOT step 8.

This is **not** a defect in this deliverable: the row's gate cell reads
`gated none`, the deliverable is a `docs/proposals/` file, and no src file was
written. But it does bound the evidence: **no probe was run and no outcome was
observed.** Every claim above is a code fact read at the cited line. The two
places where a read is a step short of an observation are marked in place:

* §1.2 — the join-order dependence of a tied opposition is read from
  `leadingOpposer`'s strict `>` scan and `Conflict.getOpposers()`'s ordering; the
  winning consequence is derived, not observed.
* §3.1 — the 0-0 passage is read from the `yes == no` branch condition; no game
  was played to reach it.

Both would be confirmed by two small probes, which this row's read-only scope
forbids. They are the natural acceptance criteria for whichever engine-scoped
row picks them up.

## 6. Index

| # | Tie class | Shape | Rulebook | Code |
|---|---|---|---|---|
| 1.1 | support vs opposition | STRICT (initiator loses) | :550, :1210 | `RulesEngine.java:523-528` |
| 1.2 | tied opposition totals | ORDER-DEPENDENT | — (comment only) | `RulesEngine.java:774-784` |
| 1.3 | non-opposable multi-side Won | not implemented | :1210 | `ConflictType.java:3-4`, `Conflict.java:255-266` |
| 2.1 | standard victory, condition 1 | STRICT | :179 | `RulesEngine.java:994-998`, `:1024-1035` |
| 2.2 | standard victory, condition 2 | ESCALATE + STRICT | :180, :178 | `RulesEngine.java:965-982` |
| 2.3 | major victory | STRICT (gap 10) | :186 | `RulesEngine.java:1012-1019` |
| 2.4 | last standing / all surrendered | no tie possible | :825 | `RulesEngine.java:789-796`, `:812-814` |
| 3.1 | council Yes/No tie | ESCALATE (always Yes) | :791, :797 | `RulesEngine.java:2515-2520` |
| 4.1 | mercenary control | STRICT | :739 (silent) | `GameState.java:545-568` |
| 4.2 | reporting tiebreak | CONSERVATIVE (shared win) | none (B5-0332) | `HeadlessReportingTiebreakTest.java:118-189` |

**Reusable lesson:** a tie catalog is not a list of tie-break rules — it is a
list of the places a tie is *unreachable*, because "no tie possible" and "a tie
crowns nobody" are different engineering facts and only the second one has a
code path to review.
