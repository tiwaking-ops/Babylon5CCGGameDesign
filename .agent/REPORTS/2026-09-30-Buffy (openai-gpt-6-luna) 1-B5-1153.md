---
document:
  title: "B5-1153 close-out — aftermath trigger coverage is not effect coverage"
  status: "Report (standalone audit; task not closed)"
provenance:
  author_llm: {name: "Buffy (openai/gpt-6-luna) 1", version: "openai/gpt-6-luna"}
  created_date: "2026-09-30"
  last_modified_by_llm: {name: "Buffy (openai/gpt-6-luna) 1", version: "openai/gpt-6-luna"}
  last_modified_date: "2026-09-30"
task: B5-1153
agent_id: "Buffy (openai/gpt-6-luna) 1"
javac: "1.8.0_292"
---

# B5-1153 — aftermath trigger-value coverage

## Coordination status

This is a standalone findings report, **not a task close-out**. The task claim
was written under this session's identity at 09:20:27Z, then a later reread at
09:31Z found `.agent/CLAIMS/B5-1153.json` replaced with a claim for `me-so-poor`
(started 09:21:25Z). I did not create that replacement and did not modify or
delete it. Per the claim-is-authority rule, I stopped all shared close-out work:
B5-1153's ledger row remains OPEN, no DECISIONS entry was appended, and no
pattern was filed. The census below is preserved as independent evidence; the
report does not assert that this task is closed.

## Findings

The trigger vocabulary is **fully handled by the legality predicate**, not by a
14-case effect switch. A strict JSON census of `premiere.json` and `deluxe.json`
found 117 AFTERMATH records (59 Premiere + 58 Deluxe), 14 distinct
`triggerCondition` values, and zero values outside the tokens consumed by
`AftermathCard.isEligible`. The engine accepts all 14 trigger strings as
combinations of `WON`, `LOST`, `PARTICIPANT`, `MILITARY`, `DIPLOMACY`,
`INTRIGUE`, `PSI`, and `ANY`.

That is **eligibility coverage, not effect fidelity**. Generic aftermath
resolution uses the three broad tokens Won/Lost/Participant and currently
lands only the generic +1 influence / draw-1 behavior. B5-0990 and B5-1051
register four card ids for bespoke effects. The rest may have text promises
beyond that generic shape; this census does not claim those effects are
implemented.

## The 14 values and counts

Counts were independently generated from strict JSON using PowerShell
`ConvertFrom-Json` on the two source card files and `Group-Object triggerCondition`:

| Trigger condition | Premiere + Deluxe records | Eligibility handling |
|---|---:|---|
| `ANY` | 2 | No token restriction; eligible after any conflict, subject to target rules |
| `DIPLOMACY_PARTICIPANT` | 4 | Requires Diplomacy and participation |
| `INTRIGUE_PARTICIPANT` | 4 | Requires Intrigue and participation |
| `LOST` | 33 | Requires initiator loss |
| `LOST_DIPLOMACY` | 4 | Requires initiator loss + Diplomacy |
| `LOST_INTRIGUE` | 4 | Requires initiator loss + Intrigue |
| `LOST_MILITARY` | 4 | Requires initiator loss + Military |
| `MILITARY_PARTICIPANT` | 16 | Requires Military and participation |
| `PARTICIPANT` | 10 | Requires participation |
| `WON` | 14 | Requires initiator win |
| `WON_DIPLOMACY` | 4 | Requires initiator win + Diplomacy |
| `WON_INTRIGUE` | 6 | Requires initiator win + Intrigue |
| `WON_MILITARY` | 10 | Requires initiator win + Military |
| `WON_PARTICIPANT` | 2 | Requires initiator win + participation |
| **Total** | **117** | **14 values; none unrecognised** |

No current record uses the implemented `PSI` token. `ANY` is the sole
unrestricted value. The sums agree with the independently measured per-set
counts (59 + 58).

## Three different dispatch/eligibility mechanisms

1. **Model eligibility — all 14 values.**
   [`AftermathCard.isEligible`](../../b5ccg/src/b5ccg/model/AftermathCard.java#L27-L41)
   reads the stored trigger string and uses token containment to enforce outcome,
   participation, and conflict-type constraints. [`RulesEngine.canPlayAftermath`](../../b5ccg/src/b5ccg/engine/RulesEngine.java#L977-L1006)
   calls that predicate before applying target and duplicate-attachment rules.
2. **Generic effect path — broad tokens, not a 14-value switch.**
   [`GameController.applySimpleAftermathEffect`](../../b5ccg/src/b5ccg/engine/GameController.java#L700-L704)
   awards 1 influence when `WON` applies, draws 1 when `LOST` applies, and draws
   1 for `PARTICIPANT`. This is the generic floor; it does not interpret every
   card's full text or encode each conflict type as a separate effect.
3. **Bespoke per-card dispatch — four ids.**
   [`CardEffects.applyAftermathEffect`](../../b5ccg/src/b5ccg/engine/CardEffects.java#L467-L508)
   handles Negotiated Surrender (B5-0990) and Diplomatic Advantage (B5-1051),
   two set records each. Their trigger values are respectively
   `MILITARY_PARTICIPANT` (2 records) and `WON_DIPLOMACY` (2 records). Because
   the two Diplomatic Advantage records happen to share `WON_DIPLOMACY` with
   two `United Front` records, matching a trigger does not itself mean an
   effect is registered for that trigger.

## Covered predecessors and remaining inventory

- **B5-1051:** the Diplomatic Advantage effect is already implemented and
  asserted: winner gains 2 influence and draws 1. No new work is claimed here.
- **B5-1099:** the continuation is BLOCKED on the engine/suite gate, and its
  requested one-record dispatch attempt is explicitly about another card with
  text beyond the generic shape. No trigger value or record was selected here;
  this read-only census does not pre-empt that claim.
- **B5-1127:** its id-keyed coverage census is a separate axis. B5-1153 is
  trigger-keyed: a trigger count cannot say whether a particular id has a
  matching bespoke dispatch, and a registered id cannot establish coverage of
  every record carrying its trigger.

The compact effect-site view of the 14-value inventory is:

| Trigger value | Records | Generic token site | Bespoke record-level site |
|---|---:|---|---|
| `ANY` | 2 | No outcome/participation token effect; generic handler does nothing | None |
| `DIPLOMACY_PARTICIPANT` | 4 | Participant draw-1 only | None |
| `INTRIGUE_PARTICIPANT` | 4 | Participant draw-1 only | None |
| `LOST` | 33 | Draw-1 | None |
| `LOST_DIPLOMACY` | 4 | Draw-1 | None |
| `LOST_INTRIGUE` | 4 | Draw-1 | None |
| `LOST_MILITARY` | 4 | Draw-1 | None |
| `MILITARY_PARTICIPANT` | 16 | Participant draw-1 only | Negotiated Surrender's 2 ids are bespoke; remaining 14 records have no current registered bespoke id effect |
| `PARTICIPANT` | 10 | Draw-1 | None |
| `WON` | 14 | +1 influence | None |
| `WON_DIPLOMACY` | 4 | +1 influence | Diplomatic Advantage's 2 ids are bespoke; the 2 United Front records do not have a dedicated id handler |
| `WON_INTRIGUE` | 6 | +1 influence | None |
| `WON_MILITARY` | 10 | +1 influence | None |
| `WON_PARTICIPANT` | 2 | +1 influence + draw-1 | None |

“None” here means no bespoke handler in the inspected current sites; the
eligibility predicate still recognizes the value. It does not equate to
“unplayable” or establish the exact card-text deviation for every record.

## Verification and bounds

- `javac -version`: `1.8.0_292`.
- Strict JSON source counts: 829 records total; 59 Premiere AFTERMATH and 58
  Deluxe AFTERMATH; 117 combined, 14 distinct values; histogram sums to 117.
- Read-only tracing: `AftermathCard.java`, `RulesEngine.java`,
  `GameController.java`, `CardEffects.java`, plus existing B5-1033,
  B5-1045, B5-1051, B5-1099, and B5-1127 task evidence.
- No `src`, suite, or card-data file edited. No dispatch entry added.
- `compile.bat` was attempted twice and timed out (120 seconds, then 240
  seconds) without returning output; the build gate is **unverified**, not green.
  No source files were changed during either timed-out command.
- `run-dup-census.ps1`: PASS, 0 duplicate task IDs.
- `ledger-query.ps1 -Status OPEN`: during my claim it read 7 pipes / single
  lead; the claim is now foreign and suppressed-live-claim, not a defect report.
- `run-dup-census.ps1`: PASS, zero duplicate IDs after the other claim appeared.
- `validate-heartbeats.ps1`: this heartbeat conforms; whole store retains the
  standing red (9 non-conforming files, 3 identity collisions).

## Reusable lesson

A trigger census measures whether a card can be offered, not whether its text
is carried out: separate eligibility-token coverage from per-record effect
coverage, and keep trigger counts distinct from id-keyed dispatch counts.
