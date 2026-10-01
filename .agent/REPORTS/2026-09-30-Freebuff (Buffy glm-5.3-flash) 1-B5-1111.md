---
document:
  title: "B5-1111 — decomposed UNKNOWN FIELD census for the B5-1068 owner: the fix is exactly 6 set-adds"
  status: "DONE 2026-09-30"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  claimed_at: "2026-09-30T06:32:51Z"
  addressed_to: "the B5-1068 owner (loader validation logging, gated on B5-1047)"
  instruments: "source read of DeckLoader.java validateFields (lines 324-447); census corroboration from this session's independent probes (B5-1104: 5,398 lines over two passes; Buffy (glm-5.3-flash) 6 per the B5-1104 row: 2,699 lines single pass)"
---

# B5-1111 — to the B5-1068 owner: the warning fire is 6 missing `expected.add` calls

## The decomposition (per-key, both independent probes agree)

| key | warning lines/pass | records carrying it | which DeckLoader line fails to expect it |
|---|---|---|---|
| id | 829 | 829 (all) | **326–332**: `alwaysRequired` is presence-checked (`containsKey` → throw if missing) but never added to `expected` |
| title | 829 | 829 | same |
| type | 829 | 829 | same |
| triggerCondition | 117 | 117 AFTERMATHs (59 pre + 58 del) | **369–373**: `case AFTERMATH:` body is comments only — no `expected.add` at all |
| isMajorAgenda | 47 | 47 AGENDAs (26 pre + 21 del) | **364–368**: `case AGENDA:` body is comments only — no `expected.add` |
| winCondition | 47 | 47 | same as isMajorAgenda |
| timing | **1** | 1 (`de_event_armistice`) | none — **genuinely unknown**, contract-forbidden (B5-1022/B5-1025), owned by B5-1101 |

## Why the omission is per-branch, not structural

The same switch **does** add the other types' keys: CHARACTER (341–350:
diplomacy/intrigue/psi/leadership/isAmbassador), FLEET (352–356),
CONFLICT (358–363: conflictType/influenceReward/participation),
CONTINGENCY (378–384), ENHANCEMENT (386–395: the five bonuses +
participation), LOCATION (424–428). AGENDA and AFTERMATH are the only two
cases whose required keys exist **only** in the second (missing-required)
switch at 430/442 — their expected-set halves were simply never written.
Plus the common trio from `alwaysRequired` (326). **Total fix: 6
`expected.add(...)` calls** (id, title, type via the common set or the
`alwaysRequired` loop; isMajorAgenda + winCondition at 364; triggerCondition
at 369).

## The trap the owner must know (already measured, twice)

The presence-checks **throw before the unknown-field loop runs** (326–332
and the missing-required switch at 411–447 execute first; the
`UNKNOWN FIELD` loop at 445–448 runs last). So a record *missing* one of
these keys can never produce a warning — the warning is only reachable
when the key is present, which is exactly the compliant case. The warning
is therefore unreachable for every non-compliant record the contract cares
most about, and fires 2,699 times per pass on compliant ones. (Pass count:
B5-1104 measured the ×2 doubling over two validation passes — 5,398 on the
double-pass path; the single-pass number is 2,699.)

## Requested fix shape (for B5-1068 to implement, not this row)

Add the 6 keys to `expected`; leave the `timing` warning intact (it is the
one true positive, contract-forbidden on armistice, already owned by
B5-1101's adjudication); optionally gate the loop behind the same
missing-required order-of-operations concern if the owner wants warnings
for absent-required records (a separate decision). No exit code, no
mapping, no threshold changes — per B5-1068's own bounds.

## Provenance of the census numbers

This session produced two independent counts that reconcile exactly:
Buffy (glm-5.3-flash) 6's B5-1104 close-out (2,699 lines, single pass,
scratch probe `agent/tmp_b51104/`) and my B5-1104 corroboration report
(5,398 over the two-pass path, same keys). The key list (7 distinct) and
per-key counts agree to the line.

No tool, loader, or src file touched by this row — the deliverable is this
handoff.

**Reusable lesson:** a warning that fires on every record and a warning
that can never fire for the record that matters are the same defect seen
from two ends — read the throw-order before designing the log fix.
