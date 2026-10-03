---
document:
  title: "Snapshot of docs/DECISIONS.md as truncated, retained as incident evidence"
  status: "Archive (superseded, retained for history)"
provenance:
  author_llm: {name: "opencode (big-pickle)", version: "reap1"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (big-pickle)", version: "reap1"}
  last_modified_date: "2026-10-03"
  created_date: "2026-10-03"
---

# Snapshot: the truncated `docs/DECISIONS.md` (2026-10-03)

Verbatim copy of `docs/DECISIONS.md` as it stood at `2026-10-03T05:25:20Z`, taken
under claim B5-2315 immediately before the human-authorised restore. It is the
**tail** of the file, not its head: 297 elements, 23898 bytes,
sha256 \$sha\.

The file had been reduced from roughly 8900 lines to 86 by an unclaimed writer
unknown to this repository between `03:20Z` and `05:15Z` on 2026-10-03, then grew
to 237 elements when a second session appended. Zero `2026-09` entries survive,
which is what identifies the content as a tail. Nothing was recovered beyond this
tail and the `tmp-scans/DECISIONS-before-b50989.md` base; entries appended between
`2026-10-02T02:46Z` and the truncation are not on disk.

Any session that appended decision entries in that window and still holds its own
copy should re-file them.

---

## Verbatim content follows


## 2026-10-03 — Cline (claude-4-sonnet): B5-2267 DONE — Victory plus elimination banner with draw-round buy-cards (ui only)

* **Scope delivered:** Three UI-only additions to MainWindow.java, engine read-only.
* **Deliverable 1 — Victory/elimination banner (refresh:1993-2040):**
  - Shows winner name, Power total, and explicit victory type.
  - MAJOR victory: "20+ Power, lead ≥10" (rulebook :182).
  - STANDARD victory: "20+ Power, strictly greatest" (rulebook :176).
  - Path-specific qualifiers: Agenda condition key, Station influence, Last-Standing sole survivor, Lead margin.
  - Next-highest non-forfeited/non-surrendered opponent Power shown for context.
* **Deliverable 2 — Forfeit button (toolbar:1228-1233, refreshForfeitControl:2338-2361):**
  - Beside Surrender in toolbar; no target selector needed.
  - Confirmation dialog: warns of immediate exit, ambassador discard (not transferred), sole-survivor win.
  - Enablement via RulesEngine.canForfeit (ACTION phase, not game over, player active, ≥1 other active player).
  - Submits GameAction.forfeit() on confirmation; engine authority re-checked at commit.
* **Deliverable 3 — Draw-round buy-cards offer (toolbar:1242-1246, refreshDrawRoundBuyControl:2372-2392):**
  - Visible only during DRAW phase (GamePhase.DRAW).
  - Label shows "Draw Round: N INF available" from human's applied pool.
  - "Buy Card (3 INF)" button enabled when applied pool ≥3.
  - Calls RulesEngine.buyMoreCards(human, state) for manual top-up; engine auto-buys in drawRound step 4.
* **Gate green:** compile.bat PASS (javac 1.8.0_292, -source 6 -target 6, 1 bootstrap warning); census-b50960 clean (0 code-lines for all tracked Java 6 violation families, 14 getOrDefault hits are project's own helper); RUN_TESTS=1 all 9 gates PASS (HeadlessConformanceTest 787/787, HeadlessSmokeTest, HeadlessAIDifficultyContractTest, HeadlessConflictResolutionProbe, HeadlessHumanConflictAttackWindowTest, HeadlessLeadFleetScenarioProbe, HeadlessParticipationGatesProbe, HeadlessStationVictoryTest, HeadlessWarConflictProbe).
* **Files edited:** b5ccg/src/b5ccg/ui/MainWindow.java only.
* **Report:** .agent/REPORTS/2026-10-03-Cline (claude-4-sonnet)-B5-2267.md
* **Pattern:** .agent/PATTERNS/Cline (claude-4-sonnet)/2026-10-03-victory-forfeit-drawround-pattern.md

**Reusable lesson:** When surfacing an engine-automatic action (draw-round buy-cards) in the UI, show the engine's own state (applied pool) and offer a manual trigger that re-uses the same engine method (buyMoreCards) rather than reimplementing — this guarantees the UI and engine can never disagree on cost, eligibility, or side effects.

## 2026-10-03 — Cline (space-bunny-free): B5-2199 DONE — Map sponsor plus assistant discount mechanics (read-only engine/model)

* **Scope delivered:** read-only trace of every sponsor/assistant discount path from card text to paid cost at play time, with file and line numbers. No src edits, no commit.
* **Paths verified:**
  * `baseRecruitCost` (RulesEngine.java:403-406) — card cost doubled for off-faction loyal, neutral free.
  * `recruitCost` (RulesEngine.java:425) — `Math.max(0, base - sponsorDiscount)`, max not sum.
  * `executeAssistantSponsorDiscount` (RulesEngine.java:1540-1552) — rotates assistant, calls `grantSponsorDiscount(1)` (Player.java:418, max logic).
  * `sponsorCost` (RulesEngine.java:433-438) — `FREE_SPONSOR` waiver checked first, dominates all numeric discounts.
  * RECRUIT_CHARACTER branch (GameController.java:384-402) — consumes exact discount applied via `consumeSponsorDiscount(baseCost - cost)`.
  * `startRound` (RulesEngine.java:2160) — expires all remaining discount: `consumeSponsorDiscount(getSponsorDiscount())`.
  * `promotionCost` (RulesEngine.java:234-242) — base cost (with double off-faction) + Inner Circle size surcharge.
  * `groupSponsorshipCost` (RulesEngine.java:338-340) — listed cost only, undoubled per rulebook :496.
  * `executeAssistantAbilityBoost` (RulesEngine.java:1521-1533) — mutually exclusive with sponsor discount per `canUseAssistant` gate; sets `assistantBonus` flag (sustained), not a discount.
* **Stacking rules confirmed:** sponsor discounts are max not sum (Player.grantSponsorDiscount uses `Math.max`); only one discount applies per recruit; FREE_SPONSOR waiver overrides entirely; assistant ability boost and sponsor discount are mutually exclusive per assistant per turn.
* **Gate green:** `b5ccg/compile.bat` PASS (exit 0), Java 6 census clean.
* **Report:** `.agent/REPORTS/2026-10-03-Cline (space-bunny-free)-B5-2199.md`
* **Pattern:** `.agent/PATTERNS/Cline (space-bunny-free)/2026-10-03-sponsor-discount-max-not-sum.md`

**Reusable lesson:** A sponsor discount should always be modeled as a max not a sum: stacking discounts leads to player confusion and logic churn. Each bonus (assistant, card, waiver) should carry its own effect type; a full cost waiver always expresses with dominance precedence.

## 2026-10-03 — poor-claude: B5-2255 DONE — support/oppose participation buttons + running totals (ui only claim, no edit)

* **Feature audited — already live.**
    * Join Support/Oppose buttons exist and are correctly enabled in MainWindow.java (L88-89 declaration, L703–716 construction/wiring, L1113–1114 toolbar add, L1822–1823 enablement on ACTION-phase join window).
    * Running support vs oppose totals are live and visible in joinPromptLabel (rendered from Conflict.supportTotal()/oppositionTotal(), MainWindow.java:2476–2477, refreshed every state update).
    * No in-scope files (`b5ccg/src/b5ccg/ui/`) were edited or added by this claim.
* **Gate green**: compile.sh exit 0, javac 1.8.0_292; census-b50960 code-lines 0 for every tracked construct family; no qualified getOrDefault; RUN_TESTS=1 exit 0, verification passed conformance 818/818 PLUS smoke and 7-probe PASS.
* **Red at claim, green at close:** At claim/start, the gate was red (compile.sh exit 1, 7 `cannot-find-symbol applyAftermathEffect` errors, ALL in engine/ — arrived from the foreign claim B5-2249, Cline (space-bunny-free)); by close, B5-2249 completed, claim released, method restored, and the UI scope went green with **no edit needed** and no secondary authority minted for the same facts.
* **No commit, no push, all artifacts filed:**
    * Report: `.agent/REPORTS/2026-10-03-poor-claude-B5-2255.md`
    * Pattern: `.agent/PATTERNS/poor-claude/2026-10-03-a-present-feature-is-not-a-second-authority.md`

**Reusable lesson:** a vacant UI feature row whose deliverable is already present must not re-implement; confirm, document, and close out to preserve single authority.

## B5-2177 — Starter-deck composition convention: two of the three recorded invariants do not hold (2026-10-03)

* **Agent:** Kilo (kilo-auto/free) 31
* **Scope:** `docs/proposals/` plus data read-only — zero `src/` edits, zero card JSON edits, zero harness edits. Build gate green and unchanged by this row.
* **Claim:** Tool-stamped via `new-claim.ps1`, started_utc 2026-10-03T04:07:58Z, payload-vs-mtime AGREE (delta -0.009 min). Preceded by a recorded reap of the stale foreign claim on the same row; see the reap note in `.agent/TASK_LEDGER.md`.
* **Why this row was picked over the four higher-priority ones:** `ui/` was held by a LIVE claim (`B5-2255`, `poor-claude`, owner heartbeat 28.9 min old) plus an UNCLAIMABLE one (`B5-1980`, owner heartbeat does not resolve, so UNKNOWN is never STALE), and `engine/` + `model/` were held LIVE by `B5-2001` (`pi (poor-pi)`, heartbeat 7.0 min old). All four one-writer-per-scope dirs were held, and this was the only claimable row whose scope is docs-only.
* **INTERPRETATION 1 (the rulebook makes the harness/rule boundary provable).** The convention doc asserted "neither invariant is game-logic" while citing no rulebook line, leaving its central claim unprovable from the document. The anchors that settle it: **`:132`/`:197`** set a 45-card **floor** and state there is "**no maximum**" for a play deck, so the harness's 60 is not a rules ceiling; **`:134`** shows 60 is the **retail starter-product** size (50 fixed commons + 10 random), which is where the harness borrowed it; **`:193`/`:203`–`:206`** give a deck-building *method* and no conflict/agenda ratio, so the 12 + 2 quota has **no rulebook basis at all**; **`:266`** has both players play their starting Ambassador straight into the faction, so the ambassador pin is a harness simulation of `:266` compensating for `GameController.setupGame()` extracting from the hand instead.
* **INTERPRETATION 2 (one Starting Ambassador is a minimum, not a maximum).** Rulebook **`:199`**/`:230`** read "must contain one Starting Ambassador", but this project's validator implements it as **at least** one — `DeckLoader.java:73` and `:103`–`:108` set a `hasAmbassador` flag and never count. Recorded because finding F2 below produces a deck holding the ambassador **twice**, and the correct reading is what stops that being reported as a rules violation when it is a harness-fidelity defect.
* **FINDING F1 (P1, engine scope, reported not fixed) — the 60-card cap is not enforced; the live harness deck is 74 cards.** Javadoc `:251`–`:252` and the pass-3 comment `:290` both claim a 60-card cap. Pass 1 (`:275`) and pass 3 (`:291`) test `deck.size() < 60`; the quota pass (`:281`,`:286`) tests only its own counters and appends 14 cards regardless of size. Measured over all 829 live records: own-faction counts are MINBARI 72, CENTAURI 62, HUMAN 61, NARN 61, NON_ALIGNED 8 — so **all four factions that own a printed ambassador saturate pass 1 at 60**, the quota pass appends 14 more, and pass 3 adds nothing because the deck is already 74. Violates no rule (`:132` has no maximum); it matters because the document's purpose is to record load-bearing invariants and it was recording a ceiling the code lacks. **Not fixed:** `engine/` is held by live claim `B5-2001`.
* **FINDING F2 (P1, engine scope, reported not fixed) — the ambassador pin duplicates the card instead of relocating it.** `:86` `new Deck(deckCards)` does `drawPile.addAll(cards)` (`Deck.java:10`), so the ambassador is already in the pile; `:92` `findAmbassadorCard` searches **that same list** and so can only return a card already present; `:93` `addToTop(amb)` is `drawPile.addFirst(c)` (`Deck.java:54`), an **insert with no removal**. `Deck.draw(int)` (`:22`–`:28`) draws from the front, so the inserted copy is guaranteed in the opening four and the original stays buried. Unconditional on live data: **10 records carry `isAmbassador: true`**, all `CHARACTER` with a faction. Reads as a violation only if "one" means exactly one; per INTERPRETATION 2 the validator says at least, so the cost is that the smoke test plays a **74-card deck holding two copies of one card**, which no physical deck can be, and every card-count assertion against it is off by one.
* **FINDING F3 (P2, refuted — recorded because a refutation is evidence).** The quota loops never test membership, so a quota pick already added by pass 1 would be duplicated. It does not happen now, by ordering accident not by guard: all 108 CONFLICT records are `ANY`; the first two AGENDA records in load order (`agenda_a_rising_power`, `agenda_as_it_was_meant_to_be`) are both `ANY`; the 12 faction-specific agendas (MINBARI 4, HUMAN 3, CENTAURI 3, NARN 2) never reach the quota loop. **Data-dependent, not enforced** — reordering the card files silently introduces duplicates with no gate to catch it.
* **FINDING F4 (P2) — the printed-deck path's quota rests on a comment, never a measurement.** `buildFactionDeck` short-circuits to `StarterDeckBuilder.build` at `:265`–`:267`; the claim that this path needs no quota rests entirely on the comment at `:263`–`:264`. F1 and F2 apply to this path too since the pin runs after either path. Whether it independently satisfies 12 + 2 is **unverified**: `StarterDeckBuilder` is `engine/` and read-only here.
* **Gate:** `b5ccg/compile.bat` **GREEN**, exit 0, "Build successful", javac 1.8.0_292 `-source 6 -target 6`, 1 expected bootstrap warning only. The Java 6 construct census was not re-run because this row edits no `src/` file.
* **Deliverable:** `docs/proposals/b5-2177-starter-deck-composition-convention-proposal.md` — original author's 20 cited anchors **re-measured byte-exact, zero drift** despite `engine/` being under active foreign edit during the pass; rulebook-anchor table added; two false "capped at 60" statements corrected in place with the correction visible rather than silently overwritten.
* **Report:** `.agent/REPORTS/2026-10-03-Kilo (kilo-auto-free) 31-B5-2177.md`
* **Pattern:** `.agent/PATTERNS/Kilo (kilo-auto-free) 31/2026-10-03-a-convention-doc-must-measure-the-invariant.md`
* No `src` file created, edited or deleted; no foreign row, claim, heartbeat, report or pattern touched; no card JSON touched; no rulebook body text edited; no commit, no push.
---
## 2026-10-03 - Muse Spark (muse-spark-1.3-contributor-free): B5-2269 DONE - AI legal-action parity verified, no src edit

* The eight families (PROMOTE, LEAD_FLEET, HEAL, REPAIR, DISCARD plus REPLACE plus REVEAL agenda, ATTACK, BID_ON_MERCENARY) are all offered by buildLegalActions plus scored above PASS at MEDIUM and HARD plus selected by chooseAction in dominance rigs: 45 of 45 scratch reflection assertions PASS (git-ignored probe, deleted after use). Predecessor rows landed every offer plus both scorers, so this closes by verification per the B5-1473 precedent, not by duplication. PLAY_AFTERMATH and FORFEIT correctly unoffered (no human path per B5-2235, voluntary loss). Engine execution branches present for all eight (GameController, read-only).
* Gate green: compile.bat exit 0 on arrival (javac 1.8.0_292), conformance 818 of 818 PASS, smoke exit 0 (4 of 4 decisions legal), 7 gate-tier probes exit 0. No src file created edited or deleted; engine fenced read-only under live Cline B5-2249 claim; ai scope was claim-free.
* DISCLOSURE: docs/DECISIONS.md re-truncated mid-session (worktree 53 lines vs HEAD 5037 lines and 674 KB, 4999 uncommitted deletions, mtime ~03:41Z). Same stale-buffer rewrite class as B5-1439. No restoration attempted, recovery needs a human-ratified row; content survives in ledger note cells plus reports plus patterns. This entry byte-appended to the regrown tail in sampled LF.
* Report: `.agent/REPORTS/2026-10-03-Muse Spark (muse-spark-1.3-contributor-free)-B5-2269.md`; Pattern: `.agent/PATTERNS/Muse Spark (muse-spark-1.3-contributor-free)/2026-10-03-verify-shipped-parity-before-extending.md`.
* Reusable lesson: a verify-the-contract row closes with a behavioral selection proof, not a re-landing - offer plus score can both be present while a sibling offer wins selection, which only a dominance rig reveals.

## 2026-10-03: B5-2314 by opencode (big-pickle) reap1. ESCALATION of B5-2097 for a human ruling, no action taken.

Escalation only. Nothing was reaped, no administrative release was performed, and
no B5-2097 or B5-2002 claim, heartbeat, or ledger row was edited. **A ruling is
requested; the work below is not authorised by this entry.**

### The disposition under question

`.agent/CLAIMS/B5-2097.json` holds a claim that no living session can own, over a
row of real unfinished work, which makes that row permanently unofferable through
the runner's own B5-0653 guard.

Measured at clock `2026-10-03T05:14:48Z`, TTL 30 min, ages in minutes:

| signal | measurement | verdict |
|---|---|---|
| sig1 claim file | `.agent/CLAIMS/B5-2097.json`, mtime `2026-10-02T05:42:24Z`, age 1412.4, sha256 prefix `C82C2AB55EB3BD57` | stale |
| sig2 **owner signal** | **ABSENT** — no `.agent/HEARTBEATS/Inky.json`; no `_registry.json` row containing `Inky`; **no heartbeat anywhere in the store names B5-2097 in `live_claims`** | **UNKNOWN** |
| sig3 owner report | `.agent/REPORTS/2026-10-02-Inky-B5-2097.md`, mtime `2026-10-02T05:44:29Z`, age 1410.3 | stale |

The payload names `agent_id: "Inky"` with `status: "BLOCKED"` and the note
*"Per .agent/00_BOOT.md step 8: gate red => BLOCKED, release, do not fix outside
scope"*. The owner's own report confirms it did no card-JSON work: it blocked at a
red boot gate (MM/MD/A/D across CLAIMS/HEARTBEATS/00_BOOT/AGENT_LOOP), preserved
its read-only scope, filed the claim BLOCKED, and stopped.

**Two of three signals stale plus one absent is `UNKNOWN`, not `STALE` (B5-0791).
A missing owner signal is never `STALE`.** That is the whole reason this row was
not reaped in B5-2313, and the whole reason it now needs a human.

### What is blocked is real work

Ledger row 1668 (B5-2097) is `OPEN`, owner `-`, and asks for a **census of all
AFTERMATH card JSON field presence against the loader contract** — verifying `id
title type subtype rarity faction set imageKey text` on all 117 records and
reporting violations by card ID, data layer read-only, explicitly not editing card
JSON and explicitly fencing B5-2002 which owns trigger evaluation. No other report
in the store addresses it. So the residue is not protecting anything; it is
stranding a legitimate task behind a file nobody is permitted to delete.

### The coupled fence: B5-2002, same class

B5-2097's text fences `OPEN` B5-2002 (ledger row 1618), which implements
triggered effects for the same 117 AFTERMATH cards in `CardEffects.java`. Measured
at clock `2026-10-03T05:14:24Z`, its claim `.agent/CLAIMS/B5-2002.json` is
**corrupt JSON** — `ConvertFrom-Json` fails with *"Invalid object passed in, ':' or
'}' expected. (805)"* — carrying `agent_id: "Inkling (inkling-1)"`,
`status: "BLOCKED"`, and the same midnight placeholder `started_utc
2026-10-02T00:00:00Z`:

| signal | measurement | verdict |
|---|---|---|
| sig1 claim file | mtime `2026-10-02T04:05:29Z`, age 1507.9, **unparseable** | stale |
| sig2 owner heartbeat | `.agent/HEARTBEATS/Inkling (inkling-1).json`, mtime `2026-10-02T04:06:08Z`, age 1504.1, `state busy` | stale |
| sig3 owner report | `.agent/REPORTS/2026-10-02-Inkling (inkling-1)-B5-2002.md`, age 1504.9 | stale |

All three definite and stale — so B5-2002 *would* satisfy a reap — but its file is
unreadable, and B5-2311 already recorded that inserting one brace at index 804
makes it parse while ruling that only the owner may remove a claim. **This entry
takes no action on B5-2002 either.** It is reported because releasing B5-2097 alone
would only move it behind an equally stuck row.

### Options

**1. Human-authorised administrative release of `.agent/CLAIMS/B5-2097.json`, and
separately of `.agent/CLAIMS/B5-2002.json`. RECOMMENDED.**
The one non-owner removal path with live precedent (the B5-1008 admin release). It
returns two real tasks to the queue for fresh sessions with proper identities, and
clears the B5-0653 warnings that currently fire once per lane forever. Cost: two
administrative deletions, each of which should carry an explicit human note in this
register so the removal is never mistaken for a self-serve reap.

**2. Leave both claims in place.**
Zero risk to live work, since both owners are demonstrably gone. Cost: B5-2097 and
B5-2002 are unofferable indefinitely — the AFTERMATH census and the AFTERMATH
trigger implementation are permanently stranded — and the queue carries a permanent
per-lane warning for each. This is the status quo, not a resolution.

**3. Owner re-claim. CLOSED — unavailable, not merely unattractive.**
For the B5-1008 reason: *an `agent_id` names a running session instance, not a
model*. The `Inky` session ended. A new session adopting that id would be
impersonating a dead session and would raise an IDENTITY COLLISION in
`validate-heartbeats.ps1` — the store already reports 5 such collisions, which is
what that failure looks like. CLAIMS/README independently forbids editing another
agent's claim. There is no honest version of this option, which is precisely why
only the human route remains.

### Recommendation and stop point

Option 1, on both files, as an explicitly human-authorised administrative release.
**This row stops here.** No further action is available to an agent without a human
ruling, and no escalation was performed that assumed one.

## 2026-10-03 B5-2007 (hermes (stealth-space-bunny-alpha) 2341) — deck-builder feedback verified already delivered; constructed deck is discarded

**Decision:** close B5-2007 DONE with **zero `src` bytes written**, and record
the real defect as unowned work rather than patch it out of scope.

**Premise measured before any edit.** All four warnings the row names were
already implemented and live: 45-card floor (`DeckLoader.java:88-91`),
one-Starting-Ambassador (`:106-109`, `:124-126`), max-3-copies with its FIXED
exemption (`:128-147`), faction playability (`:119-122`). They are re-run on
every mutation by `refreshValidation` (`MainWindow.java:4067-4085`), which
delegates to `DeckBuilderModel.problems()` rather than reimplementing any rule,
and `Construct` is gated on the empty problem list (`:4084`) and re-checked at
click time (`:3951-3952`). B5-2247 wrote the dialog; B5-2287 moved the rules
into a headlessly testable model.

**Gates.** `compile.sh` exit 0. `RUN_TESTS=1 compile.sh` exit 0,
`Verification passed`, `CONFORMANCE SUITE PASSED (818 checks)`,
`SMOKE TEST PASSED`. Java 6 census over all 8 `ui/` files: 0 code hits (the 6
grep matches are an arrow in string literals and arrow prose in Javadoc).

**Interpretation recorded (B5-1730 applied, not just cited).** A green
conformance suite proves nothing until it has been seen failing. Raising
`DeckLoader.MAX_COPIES_PER_CARD` 3 → 99 turned `[DB4]` red on 2 of 818 checks
(the max-3 assertion and the Construct-gate assertion); restoring the file
returned 818/818. The gate was observed red, then green.

**The finding, and why it was not repaired.** `showDeckBuilder`
(`MainWindow.java:3790-3793`) calls `showDeckBuilderDialog(this)` as a bare
statement and **discards the returned `List<Card>`**, which is the dialog's only
path out (`getConstructedDeck` `:3977-3979`, `constructedDeck` `:3953`). No seam
exists to receive it: `GameController` exposes one constructor
(`GameController.java:45`) and zero `setDeck`/`loadDeck`/`startGame`, while
`Main.java:55-66` deals every faction deck inside `main` **before any
`MainWindow` exists**. The deck builder is therefore a legality checker that
cannot be used to play — a different defect from the four warnings this row
asked for.

Repair would require a `model/` + `engine/` injection seam that does not exist
yet; `ui/`-only scope cannot create one, and `engine/` is held by a **live**
claim (B5-2237, Mercury-1.0). A scan of all 993 ledger rows for *constructed
deck*, *handover*, *hand-off* and *discard* found **zero** owning rows.

**Standing consequence:** seed a new row scoped `model/` + `engine/` to add the
deck-injection seam and route `showDeckBuilder`'s result into it. Left to the
seeder, because per 00_BOOT step 4 self-seeding must go through the normal
OPEN-claim-DONE cycle and this session's loop ends here.

**Lesson.** Verify the deliverable existed before writing it — grep by the row's
nouns, not its task id — and when it is already delivered the honest close-out is
a measurement plus the *next* defect, not a redundant diff. Companion: a returned
value with no consumer is a defect no gate in this repo catches.

Report: `.agent/REPORTS/2026-10-03-hermes (stealth-space-bunny-alpha) 2341-B5-2007.md`
Pattern: `.agent/PATTERNS/hermes (stealth-space-bunny-alpha) 2341/2026-10-03-verify-the-deliverable-existed-before-writing-it.md`

No `src`, card JSON or rulebook file created, edited or deleted. No foreign row,
claim, heartbeat, report or pattern touched. No commit, no push.

## 2026-10-03 B5-1957 (hermes (stealth-space-bunny-alpha) 2341) — pattern-namespace fragmentation measured; the fifth namespace is a PUA look-alike, not a spelling

**Decision:** close B5-1957 DONE as a **measurement**, with the earlier report's
per-namespace count corrected, and record that the merge the row contemplates is
**not an agent's to perform today**.

**Precondition, recorded because it nearly decided the row wrongly.** The row
carried a foreign claim from `Muse Spark (muse-spark-1.3-contributor-free)`.
Three-signal measurement: claim 1595.2 min, owner heartbeat 53.6 min (state
`idle`, `live_claims` empty), owner report 1595.3 min. Newest 53.6 min ≈ 1.8× TTL,
all definite, none UNKNOWN → reapable; note appended before deletion. An earlier
pass on this same row declined to act because the owner heartbeat was then 3 min
old and LIVE. Both verdicts were correct at their moment — the **B5-0685** lesson
that a liveness reading must be re-measured fresh at action time and never
inherited. The owner left a complete report and stopped before flipping the row;
the reap destroyed no work.

**Counts as measured** (`solar-pro4` 1, `solar-pro4-free` 42,
`solar-pro4-free-0978` 1, `solar-pro4-free-b51088blocked` 1,
`solar-pro4<U+F03A>free` **12**; total **57**; one README, in the fifth).

**Correction.** The earlier report records 13 records in the fifth namespace. It
holds 13 *files*, of which 12 are records and 1 is `README.md`. The grand total 57
is correct; the per-namespace figure over-counted by treating the README as a
record. Recorded in place rather than silently.

**The material finding, and why it changes the answer.** The fifth namespace's
separator is **U+F03A**, a private-use codepoint that renders as a colon. It is
therefore neither the ASCII `:` Windows forbids nor its documented sanitisation
`-`. Confirmed with the shipped instrument: `detect-filename-lookalikes.ps1`
classes it `FOREIGN-NS` and finds 14 `RECORD` files, 1 `QUARANTINE` heartbeat and
3 `OUT-OF-STORE` paths bearing the same codepoint, including an agent heartbeat
**nested inside `b5ccg/src/`**.

**Consequence for the merge.** The four ASCII spellings are a rename the rules
permit in principle. The fifth is **R6-protected**: its remediation is part (b) of
`docs/proposals/lookalike-filename-r5-exception-proposal.md`, which the human
**deferred to a later ruling on 2026-09-30**, and until that ruling every component
stays byte-identical. A merge treating all five as equivalent spelling drift would
breach R5/R6 on the namespace that matters most — and no tool searching for `:`
finds U+F03A. **No agent performs this merge.**

**Merge cost, measured.** 88,346 bytes across 61 files — trivial in bytes, blocked
in authority. Ordering is **unrecoverable from the filesystem**: all 61 files share
one mtime from a bulk checkout, so recency must come from filename dates (09-25 ×1,
09-26 ×1, 09-27 ×15, 09-28 ×16, 09-29 ×5, 09-30 ×13, 10-01 ×4, plus 2 records with
no date in the name). A merge sorting by mtime would scramble history silently.
Separately, **5 of the 57 records carry no `author_llm`** (2 in `solar-pro4-free`,
1 in `solar-pro4-free-b51088blocked`, 2 in the look-alike namespace), which
AGENTS.md §1 requires; a merge is the natural moment to repair them and the wrong
moment to do so silently.

Fences honoured: DONE B5-1455 and DONE B5-1689 read, not redone. No namespace
moved, renamed, created or deleted; no pattern file edited; no foreign heartbeat,
claim, report or namespace touched; no `src`, card JSON or rulebook edit; no commit,
no push.

Report: `.agent/REPORTS/2026-10-03-hermes (stealth-space-bunny-alpha) 2341-B5-1957.md`
Pattern: `.agent/PATTERNS/hermes (stealth-space-bunny-alpha) 2341/2026-10-03-match-a-suspicious-separator-by-codepoint-not-by-glyph.md`
