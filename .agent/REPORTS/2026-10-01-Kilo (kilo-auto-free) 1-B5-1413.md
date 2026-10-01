---
document:
  title: "B5-1413 close-out — every AIPlayer.java hunk attributes to a DONE receipt; zero orphans, zero reopened findings"
  status: "Report (observations, no authority)"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free) 1", version: "kilo-auto/free"}
  last_modified_by_llm: {name: "Kilo (kilo-auto/free) 1", version: "kilo-auto/free"}
  created_date: "2026-10-01"
---

# B5-1413 — triage of the uncommitted `AIPlayer.java` hunks

Claimed 2026-10-01T03:19:25Z by `Kilo (kilo-auto/free) 1`, released on close-out.
Scope honored: read-only `ai/` plus `git diff`. **No src file, no suite file, no card
JSON, no ledger row other than this one, no commit, no push.**

## The row's premise, and what it measures now

The row states the working tree carries "plus 167 lines over AIPlayer.java" and
proposes B5-1171 + B5-1177 + the B5-0301 lineage as the nearby owners. Both halves
need correcting, and the correction is the deliverable.

**The 167 is a `git diff --stat` graph width, not a line count.**

```
git diff --shortstat -- b5ccg/src/b5ccg/ai/AIPlayer.java
  1 file changed, 154 insertions(+), 13 deletions(-)
git diff --stat -- b5ccg/src/b5ccg/ai/AIPlayer.java
  b5ccg/src/b5ccg/ai/AIPlayer.java | 167 ++++++++++++++++++++++++++++++++++++---
```

`--stat` pads the graph to a fixed width, so the leading `167` is `154 + 13` — the
raw changed-line total, rendered as the widest cell. `--numstat` and `--shortstat`
both read **154 added / 13 removed**. B5-0965's own close-out already made this
exact measurement and wrote it down correctly
(`.agent/REPORTS/2026-09-28-Buffy (glm-5.3-flash)-B5-0965.md:24`: "The 167-line
diff stat (154 insertions / 13 deletions, `AIPlayer.java` only)"). Any row or
report that calls 167 "lines added" is reading the graph, not the diff.

**The proposed owners are all `ui/`, none is `ai/`.**

| Row named by the premise | Status | Actual scope |
|---|---|---|
| B5-1171 | DONE | `ui/` only — "Refuse characters on the generic Play Card path **in the UI**", edits `MainWindow` |
| B5-1177 | DONE | `ui/` only — "affordability precheck to the Play Card enable predicate **in `MainWindow`**" |
| B5-0301 | DONE | `model/` + `engine/` — BUILD_INFLUENCE action, `canBuildInfluence`, engine offer plumbing |

None of the three ever held a claim on `ai/`, and no hunk below belongs to any of
them. The premise's own evidence contradicts it: it says these rows "own landed
hunks **in nearby offer predicates**", and the offer-predicate hunks that *are*
dirty here are self-labelled `B5-0727`.

## The 17 hunks, each mapped to its owning receipt

`-U0` yields 17 hunks (14 at `-U3`, because three adjacent pairs merge at default
context). Line numbers are the **new-file** start of each hunk.

| # | New line | +/− | Change | Owner | Receipt |
|---|---|---|---|---|---|
| 1 | 121 | +7 −0 | `mediumJoinSide` — same-race guard in Civil War | **B5-0727** DONE | `.agent/REPORTS/2026-09-27-Buffy (glm-5.3-flash)-B5-0727.md` |
| 2 | 147 | +9 −0 | MEDIUM join-side — never feed brother-war | **B5-0727** DONE | same |
| 3 | 591 | +62 −0 | Five `public static` Civil War helpers (`unrestPressure`, `raceInCivilWar`, `civilWarEntryRisk`, `mergeExposure`, `civilWarMergeExposureTerm`) | **B5-0727** DONE | same (helper table) |
| 4 | 684 | +6 −0 | MEDIUM conflict-initiation terms | **B5-0727** DONE | same ("Conflict initiation") |
| 5 | 817 | +7 −0 | MEDIUM `:992` same-race declare-war branch | **B5-0727** DONE | same |
| 6 | 861 | +4 −0 | MEDIUM surrender — unrest urgency | **B5-0727** DONE | same (SURRENDER) |
| 7 | 866 | +7 −1 | MEDIUM surrender — `−8` civil-surrender discount | **B5-0727** DONE | same; interpretation 1 records it as defensive depth |
| 8 | 920 | +6 −0 | Initiator aftermath anticipation terms | **B5-0727** DONE | same |
| 9 | 953 | +3 −0 | HARD `BUILD_INFLUENCE` `+8` in Civil War | **B5-0727** DONE | same |
| 10 | 957 | +2 −1 | HARD `PASS` `−0.5 → −0.5 + (inCivilWar ? 8 : 0)` | **B5-0727** DONE | same ("the PASS guard is the mirror of the BUILD guard") |
| 11 | 1023 | +4 −1 | HARD `PASS` guard body | **B5-0727** DONE | same |
| 12 | 1047 | +7 −0 | HARD `:992` same-race declare-war branch | **B5-0727** DONE | same |
| 13 | 1090 | +4 −0 | HARD surrender — unrest urgency | **B5-0727** DONE | same |
| 14 | 1095 | +7 −1 | HARD surrender — `−8` civil-surrender discount | **B5-0727** DONE | same |
| 15 | 1175 | +1 −1 | `agendaProximityScore` tail: `p.getInfluence()/2` → `p.getPower()/2` | **B5-0703** DONE | `2026-09-27-solar-pro4-free-B5-0703.md:21` names this site explicitly |
| 16 | 1204 | +17 −7 | `eventCatchUpBonus` → `getPower()` | **B5-0703** DONE | `2026-09-27-…` line 22 + `2026-09-28-solar-pro4-free-B5-0703.md:17` |
| 17 | 1228 | +1 −1 | `leadingPlayer` leader read: `getInfluence()` → `getPower()` | **B5-0703** DONE | unattributed in any report; folded into B5-0703 by the `docs/DECISIONS.md:5122` ruling and behaviourally covered by B5-0739 DONE (`:5198`) |

**Zero orphans.** Every hunk resolves to a DONE row's close-out. 14 hunks to
B5-0727, 3 to B5-0703.

### The one attribution gap, and why it is already closed

Hunk 17 (`leadingPlayer`) is in **no** B5-0703 report: solar's report and the
`docs/DECISIONS.md:4976` entry describe only `eventCatchUpBonus` and list
`leadingPlayer` as unchanged, which the diff contradicts. That gap was recorded,
not ignored — `docs/DECISIONS.md:5122` folds it into B5-0703's `ai/` scope
provisionally, and `:5198` records B5-0739 (DONE) closing it behaviourally. Hunk
15 (`agendaProximityScore`) is the mirror case and is *better* documented: solar's
2026-09-27 report names it as site 1 of 3. So hunk 17 is a known, adjudicated gap
with a named closing row, not a new one — this triage re-confirms the gap and its
closure rather than discovering it.

## Does any hunk reopen a closed finding?

**No.** Measured, not assumed:

- Both harnesses were run against the compiled **dirty** tree this pass.
  `HeadlessSmokeTest` → exit 0, `SMOKE TEST PASSED` (24 AI actions, 33 UI
  callbacks, 4/4 legal decisions). `HeadlessConformanceTest` → exit 1,
  `ClassCastException: FleetCard cannot be cast to ConflictCard` at
  `testParticipation:507`. **That red is not `ai/`'s.** It is the standing red
  already recorded at `docs/DECISIONS.md:10483`, `:10514` and `:11643`, owned by
  the B5-1047 → B5-1088 chain (`B5-1088` BLOCKED, gated on `B5-1047` OPEN, which
  is under a live `solar-pro4:free` claim on `engine/DeckLoader.java`). The
  failing fixture parses cards through `DeckLoader.parseCards`, whose dirty hunks
  (`@@ +247`, `@@ +270`, `@@ +315`, `@@ +507`) are the B5-1055 validator additions.
  **B5-0965's verdict "643/643 PASS" no longer reproduces, and the cause is a
  different file's uncommitted work** — which is precisely why a verdict close
  has to be re-measured rather than re-read.
- `b5ccg/compile.bat` → **Build successful**, JDK 1.8.0_292, `-source 6 -target 6`,
  one expected bootstrap warning. The build gate the row asks for is green.
- Java 6 construct scan over the 154 added lines: **0** `.getOrDefault(`, **0**
  `.stream(`, **0** `computeIfAbsent`, **0** `->`, **0** diamond `new X<>()(`.
- Line endings: the file is **1233 bare LF, 0 CRLF**, so the fleet append
  instrument preserved the file's dominant ending rather than flipping it.
- Scoring semantics are unchanged from the B5-0727 and B5-0703 contracts as
  written: every added term reads 0 on today's pool (no card invokes any Civil
  War axis per the B5-0669 census; `getPower() == getInfluence()` with no POWER
  bonus attached). No hunk edits a cost-scoring baseline, a `PROMOTE_CHARACTER`
  path, or `easyChoose`.

One cosmetic defect, named because it is real and **not** fixed here: hunk 16's
body is indented 16 spaces inside a method whose body is 8
(`AIPlayer.java:1204`, `eventCatchUpBonus`). It compiles and behaves identically —
whitespace only — but it is a genuine blemish in a hunk owned by B5-0703, whose
close-out describes the edit as "two local variables … now cache". This row is
read-only and `ai/` hunks belong to those DONE receipts, so it is recorded, not
corrected.

## What was deliberately not done

No `ai/` edit (the 17 hunks are attributed landed work; editing them would
double-deliver). No repair of the `eventCatchUpBonus` indentation (foreign DONE
receipt, and the row is read-only). No repair of the conformance red (it belongs
to `engine/`'s owning rows, and B5-1088/B5-1047 already carry it). No ledger row
edited except B5-1413's own. No commit, no push.

**Reusable lesson:** a `git diff --stat` leading number is a graph width, not a
line count — `--numstat`/`--shortstat` are the only counts, and a row that quotes
the graph as "N lines" hands its worker a premise that is wrong by the deletion
count. Paired with it: verify a task's *named* owners actually held the scope
before trusting them as candidates, because three `ui/`-scoped DONE rows can be
cited as the owners of `ai/` hunks by a proximity argument that no evidence
supports. Pattern:
`.agent/PATTERNS/Kilo (kilo-auto-free) 1/2026-10-01-stat-graph-width-is-not-a-line-count.md`.