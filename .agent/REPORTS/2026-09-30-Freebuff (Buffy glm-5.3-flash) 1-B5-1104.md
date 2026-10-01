---
document:
  title: "B5-1104 — pool re-verification after validation landed: membership unchanged, warning census taken"
  status: "DONE 2026-09-30"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  claimed_at: "2026-09-30T06:02:28Z"
  instruments: ".agent/tmp_b51043/DupTitleProbe.java + DupTitleProbe2.java through DeckLoader itself; stderr census of a full loadBothSets run"
---

# B5-1104 — validation changed the pool by zero records

## Headline comparison (vs the B5-1043 measurement of 2026-09-30T05:35Z)

| metric | B5-1043 | this re-verification | delta |
|---|---|---|---|
| pool size | 446 | 446 | 0 |
| duplicate titles | 0 | 0 | 0 |
| title union covered | 446/446 | 446/446 | 0 |
| deluxe twins in pool | 383 | 383 | 0 |
| premiere-only in pool | 63 | 63 | 0 |

**Validation changed pool membership by exactly zero records.** The
B5-1043 verdict (deluxe-only = 0 by the loader's title key; the pool is the
complete title union) is reproduced unchanged after the B5-1055 validating
loader landed.

## UNKNOWN FIELD warnings by key (one full `loadBothSets()` run, stderr)

| key | count | per-file reality |
|---|---|---|
| id | 1,658 | 829 records × 2 passes |
| title | 1,658 | 829 × 2 |
| type | 1,658 | 829 × 2 |
| triggerCondition | 234 | 117 records × 2 |
| isMajorAgenda | 94 | 47 agendas × 2 |
| winCondition | 94 | 47 × 2 |
| timing | 2 | 1 record (de_event_armistice) × 2 |
| **total** | **5,398** | |

**The ×2 regularity is the finding:** every standard field of every record
logs as unknown, exactly twice per load — the validation pass and the build
pass each emit the same warning. One noisy pass is bad; two identical noisy
passes per load is a fingerprint for the B5-1068 repair (which is gated on
B5-1047 and untouched here).

## Instrument-scope note (no fault assigned)

B5-1101's premise ("exactly one UNKNOWN FIELD warning for timing on an
829-record load") and this census (5,398 on the runtime pool path) are both
plausible — different entry points, different scopes. My instrument drives
the shipped `loadBothSets()` path that every harness uses; B5-1068's
reproduction instruction ("a plain load") matches this path. The timing
count here is 2 (armistice appears once, logged twice by the ×2 pass rule).

## Bounds

Execution only: two scratch probes compiled to git-ignored `b5ccg/out/`,
zero src edits, zero data edits, no suite edits, no commit, no push.

**Reusable lesson:** a re-verification row earns its keep twice — once
confirming the base measurement survives the new code, once taking the
census the base row was too narrow to take (the ×2 pass rule was invisible
until somebody counted by key).
