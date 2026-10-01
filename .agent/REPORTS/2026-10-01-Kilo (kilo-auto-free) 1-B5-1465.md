---
document:
  title: "B5-1465 close-out — the hunk-label rule proposed, and the presence test shown insufficient (73% labelled, 33% ambiguous)"
  status: "Report (observations, no authority)"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free) 1", version: "kilo-auto/free"}
  last_modified_by_llm: {name: "Kilo (kilo-auto/free) 1", version: "kilo-auto/free"}
  created_date: "2026-10-01"
---

# B5-1465 — the hunk-label rule, proposed and measured

Claimed 2026-10-01T03:36:57Z by `Kilo (kilo-auto/free) 1`. Scope honored:
`docs/proposals/` only. **No src edit, no ledger row edited other than B5-1465's
own, no commit, no push.** Deliverable:
`docs/proposals/hunk-ownership-marker-proposal.md`.

## The two forensics I read first, per the row

- **B5-1415** (`2026-09-30-Buffy (glm-5.3-flash) 11-B5-1415.md`) — classified 13
  foreign hunks across 4 engine files, 361 insertions / 3 deletions. Its
  observation 2 is the seed of this row: *"Every hunk self-labels its owning row —
  this convention is why zero orphans exist and why no style/date attribution was
  needed. It is what keeps an uncommitted tree auditable at all."*
- **B5-0755** (`2026-09-28-Buffy (glm-5.3-flash)-B5-0755.md`) — the first
  attribution sweep, and the source of the method rule the proposal adopts
  verbatim: attribute by labels on **added lines only**, *"never by labels
  anywhere in the diff: context lines carry the old file's comments and will
  happily attribute new bytes to unrelated old tasks."*

## The measurement that shaped the proposal

The row asks for a rule "so dirty-tree triage never needs style attribution".
Measuring the existing convention before proposing to extend it produced a result
that changed what the rule should be.

45 hunks across the 7 dirty `b5ccg/src/` files, 792 added lines, per-hunk census
over added lines only:

| File | Hunks | ≥1 row id | 0 row ids | >1 row id |
|---|---|---|---|---|
| `ai/AIPlayer.java` | 17 | 12 | 5 | 2 |
| `engine/DeckLoader.java` | 4 | 3 | 1 | 1 |
| `engine/CardEffects.java` | 5 | 5 | 0 | **5** |
| `engine/GameController.java` | 2 | 2 | 0 | 0 |
| `engine/RulesEngine.java` | 2 | 2 | 0 | 0 |
| `ui/MainWindow.java` | 13 | 8 | 5 | 6 |
| `ui/GameBoardPanel.java` | 2 | 1 | 1 | 1 |
| **total** | **45** | **33 (73%)** | **12 (27%)** | **15 (33%)** |

**73% of hunks already carry a row id.** The convention B5-1415 relied on is real
and widespread, so this is not a proposal to start something nobody does.

**33% of hunks carry more than one row id** — `CardEffects.java` is 5 of 5. So a
presence test ("does this hunk mention a row id?") passes on a third of the
corpus while the question it was meant to answer — *which row owns it* — is still
open, because the extra ids are citations (B5-0691's engine law, B5-0351's
difficulty contract, B5-0669's card census) sitting in the same explanatory
comments that name the owner. B5-1415 got the right answer because an agent
reasoned about which id was claiming and which were citing. **That reasoning is the
actual mechanism, and it is manual.**

So the rule proposed is not "add a label" — it is **"add exactly one *distinguishable*
label"**: the reserved token `// OWNER: B5-NNNN`, one per hunk, naming the row the
author worked rather than a row the code depends on. Citations keep their current
free-form form precisely because the reserved token makes them distinguishable by
construction.

## What the proposal says, and what it deliberately refuses

- A five-step triage procedure in which every branch is mechanical
  (`empty → UNLABELLED`, `>1 → CONFLICT`, `else → attributed`, plus a status
  confirmation). The only judgement left is reached by absence or multiplicity,
  never by reading prose and guessing.
- **A label is a claim, not proof.** The procedure confirms the named row exists
  and reads its status; it does not conclude the work was done. This is what
  preserves B5-1415's sharpest observation — B5-1047's work product sitting in
  the tree while the row reads OPEN — instead of hiding it behind a green check.
- **No gate.** The counter-argument is recorded in full: a comment convention is
  unenforceable, a stale marker is worse than a missing one because it is
  believed, and a rule that only ever produces a finding trains the fleet to
  ignore findings. The proposal adopts the convention and explicitly declines the
  gate, on the grounds that its entire value is in the 15 ambiguous and 12
  unlabelled hunks and a gate would cost more in false positives than the triage
  it saves.
- **Retagging is permitted**, because the marker is commentary and `git diff`
  remains the arbiter. This is what makes adoption incremental: 33 hunks can be
  tagged in one pass with no code change, leaving the 12 unlabelled as the only
  ones needing real forensic work.

## Not done, deliberately

No edit to `AGENTS.md` or `guidelines/Guidelines.md` — adoption is a human
adjudication of a proposal, and per `.agent/00_BOOT.md` step 4 a proposal is never
truth until merged. No src retagging pass (out of scope; the proposal names it as
a later, separately-claimed task). No ledger row touched but B5-1465's own. No
commit, no push.

**Reusable lesson:** a convention measured only by presence passes on the cases it
was meant to catch — 73% labelled and 33% ambiguous are both true and only the
second describes the work, so measure the property you need (one owner,
distinguishable from a citation), not the one that is easy to count. Pattern:
`.agent/PATTERNS/Kilo (kilo-auto-free) 1/2026-10-01-presence-is-not-the-property-you-need.md`.