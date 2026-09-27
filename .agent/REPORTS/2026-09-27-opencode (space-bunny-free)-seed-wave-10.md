---
document:
  title: "Seed wave 10 - repo verification, one data-gate discovery, eight seeds"
  status: "Report"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Seed wave 10 - 2026-09-27

Agent id: `opencode (space-bunny-free)`. See Provenance disclosure in section 6.
User order: *verify repo. seed lots of tasks. list open tasks.*
Machine: javac 1.8.0_292 (JDK 8, AdoptOpenJDK 1.8.0_292-b10).
Claims held this pass: **none**. No source, resource or data file was edited.

## 1. Verification (gate-first, on the exact working tree)

| Gate | Command | Result |
|---|---|---|
| Compile | `b5ccg/compile.bat` | **exit 0** - `Build successful`, `-source 6 -target 6`, 1 expected bootstrap classpath warning |
| Heartbeat store | `.agent/tools/validate-heartbeats.ps1` | **exit 0** - 27 files, 27 conforming, 0 non-conforming, 27 distinct `agent_id`, 0 collisions |
| Ledger structure | `.agent/tools/ledger-query.ps1 -Status "*"` | 346 rows, every row `pipeCount 7` / `doubleLead no` |
| Tool agreement | `.agent/tools/census-crosscheck.ps1` | **exit 0** - `CONSISTENT -- 354 row(s)`, run-queue and ledger-query agree |
| Queue | `.agent/run-queue.ps1 -DryRun` | exit 0, no warnings, 1 claimable OPEN before seeding / 9 after |

Working tree at boot carried 12 modified files and 6 untracked, all of them
coordination state (`.agent/`) left by other agents' in-flight passes. No conflict
with anything I touched.

## 2. The discovery that changed the wave

B5-0639 closed as a section-VI triage and left a list of 15 remainders, R1 to R15,
with one proposed slice (R5, unconditional surrender). Seeding from that list
without re-reading the data is how a wave seeds waste, so every candidate was
re-grounded this pass. Two candidates did not survive.

**R7 is data-gated, not model-implementable.** Its grounding in the B5-0639 report
is the rulebook :830 Marks example that the Agenda "Servants of Order" provides the
ambassador with 1 Vorlon Mark. The repo's own record for that card disagrees:

```json
{"id": "de_agenda_servants_of_order", "title": "Servants of Order", "type": "AGENDA",
 "text": "Minbari only. Win Condition: Have the most characters in your Inner Circle.
 Ongoing: At the start of each round, if you have 3 or more characters in your Inner
 Circle, gain 1 Influence. (Deluxe text change: threshold reduced to 2 characters.)",
 "isMajorAgenda": false, "winCondition": "MOST_INNER_CIRCLE"}
```

No mark, and a different ongoing effect entirely. A search of **both** card files for
the word `mark` in any card text returns **0 cards out of 383 deluxe and 446
premiere**. There is no card in the authored pool that grants a mark or is subject to
one, so an R7 model slice would have had nothing to assert. Seeded in its place:
**B5-0675**, which reclassifies all fifteen remainders against the card data rather
than against rulebook examples - precisely the inference that failed here.

**R15's premise is stronger than "unimplemented".** The report says the engine "treats
influence as power". Confirmed directly rather than by citation: a case-insensitive
search for `power` over `b5ccg/src/b5ccg/model/Player.java` returns **zero
occurrences**, and no Power field exists anywhere in `model/`. The rule at rulebook
:1034 is therefore *vacuous* today, not *missing* - a materially different starting
position, and the reason B5-0667 is a sizing task rather than an implementation.

## 3. Absence proofs taken this pass (the seeds' grounding)

| Seed | Rulebook / source | Absence proof on the live tree |
|---|---|---|
| B5-0661 | surrender :815-:819 | `surrender` over `b5ccg/src` = **0** hits |
| B5-0663 / B5-0665 | B5-0641's own unseeded proposal | `checkVictory` returns `Player` only; `MainWindow.java:1431-1436` renders the name alone |
| B5-0667 | Negative Power :1034 | `power` over `model/Player.java` = **0** hits; no Power field in `model/` |
| B5-0669 | Civil War :990-:1009 | `civil war` over `b5ccg/src` = **0**; `unrest` over `b5ccg/src` = **0** |
| B5-0671 | triggered effects :771-:775 | `testMinesReactive` @ `HeadlessConformanceTest.java:1710` covers the reactive half; no-action half unasserted |
| B5-0673 | Legal Targets :823 | per-slice gates exist, no dedicated section (B5-0639 R6) |
| B5-0675 | B5-0639 R1-R15 | the R7 contradiction in section 2 |

Every duplicate-topic check against the ledger returned hits only inside **DONE**
rows' narrative cells (B5-0497, B5-0629, B5-0639, B5-0641, B5-0645, B5-0361,
B5-0407, B5-0441) - mentions, not competing work. No existing row implements any
of the eight.

## 4. The wave

| ID | Kind | Scope | Gate |
|---|---|---|---|
| B5-0661 | Unconditional Surrender (R5) | engine + model + SUR suite section | none |
| B5-0663 | Victory-path query, engine/model leg | model + engine + VPS suite section | none |
| B5-0665 | Victory-path rendering, ui leg | `ui/` only | **after B5-0663** |
| B5-0667 | Negative Power design proposal | `docs/proposals/` only | none |
| B5-0669 | Civil War state-machine proposal | `docs/proposals/` only | none |
| B5-0671 | Triggered effects are not actions (R1) | suite section only | serialize behind 0661, 0663 |
| B5-0673 | Legal-targets gate census | report only | none |
| B5-0675 | R1-R15 data-gate reclassification | report only | none |

IDs `0661, 0663, 0665, 0667, 0669, 0671, 0673, 0675` continue the odd spacing of the
`0629/0631/0633` and `0635..0647` waves so a concurrent even-sequence seeder cannot
collide. All eight pre-checked free at zero matches each, and the **post-write**
duplicate-ID census then returned **empty**, which is the pass condition and the
check that actually matters.

## 5. Post-write verification

```
duplicate-ID census (00_BOOT step 9)  -> empty            PASS
ledger-query -Status OPEN             -> 10 rows, all 7/no PASS
census-crosscheck                     -> CONSISTENT, 354  PASS
run-queue -DryRun                     -> exit 0, no warnings PASS
```

Composition: 2 code rows touching `b5ccg/src` (0661, 0663), 1 ui row (0665), 2
docs-only proposals that change no code (0667, 0669), 1 suite-assertions-only
(0671), 2 report-only measurement instruments (0673, 0675). Three of eight are
measurement instruments, which is the ratio the QUEUE 0615 wave asked for. The suite
file is the contended resource and is one writer at a time, so 0661, 0663 and 0671
serialize lowest-number-first. No row requires an external library, so the human
approval gate is untouched.

## 6. Disclosures

1. **The census moved during the pass.** At boot it was 3 OPEN rows (B5-0653 and
   B5-0658 `suppressed-live-claim`, B5-0660 unclaimed). At close it is 10, and
   B5-0658 is no longer OPEN - its owner closed it mid-pass. I did not touch that
   row and did not reap anything. B5-0653 is still under a live Buffy
   (glm-5.3-flash) claim and is correctly still suppressed.
2. **No claim was held for the meta-seeding pass**, following seven prior waves that
   record the wave as a `QUEUE` note paragraph plus a report file rather than as a
   self-seeded ledger row. The seeded rows themselves are `OPEN` and unclaimed, which
   is what AGENTS.md section 6 requires of a seed. I did not invent a new governance
   shape for the meta-task on my own initiative.
3. **Agent identity.** I wrote under the established id `opencode (space-bunny-free)`
   rather than minting a fresh spelling. The B5-0623 ledger note records the A1.1
   hazard of two live sessions under one id, and minting a new spelling is the one
   response that guarantees fragmentation rather than preventing it. Disclosed
   rather than silently resolved, because it is a human-level call.
4. **Nothing seeded on B5-0388** (closed by ruling 2026-09-27) or on the B5-0454
   mercenary/contingency data backlog (parked by standing human directive).

## 7. Reusable lesson

Re-ground a prior triage against the **data**, not against the rulebook. B5-0639's
R7 row was carefully sourced - it cited a rulebook line and a named card - and was
still wrong, because the rulebook describes an edition of "Servants of Order" this
repo does not ship. A rulebook example naming a card is a claim about a card, and
the card file is where that claim is checkable. Discovery before seeding killed one
candidate here and downgraded another from "implement a missing rule" to "the rule is
vacuous", and neither fact was visible in the prior close-out.
