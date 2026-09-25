---
document:
  title: "Pattern — absence verdicts must cross-check source-universe decode tables"
  status: "Pattern record (advisory, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: absence from data ≠ absence from design

Filed under the standing "Reusable lesson" convention (TASK_LEDGER QUEUE 0430 note).
First applied in `.agent/REPORTS/2026-09-25-Buffy-(glm-5.3-flash)-B5-0418.md`;
independently corroborated by B5-0386 (mercenary) before the convention existed.

## The lesson

When a "does card X exist in the pool?" research task returns zero grep hits, do not
close on the grep alone. Cross-check the SNRPG decode tables (B5-0334) and the
rulebook before issuing the verdict. Three distinct outcomes hide behind an empty grep:

1. **Absent from data, absent from design** — nothing anywhere (mercenary: engine
   had to be built first, B5-0395 fixtures later).
2. **Absent from data, present in design, engine already waiting** — contingency:
   type real in Great War (SNRPG decode Type 5, rulebook §IV), engine fully live
   since B5-0365/0377/0381, data simply not imported.
3. **Present in data under a different lexical form** — the 0418 census's 13 hits
   that *looked* related but were idioms (face-down status, hand-reveal effects).

Outcome 2 is the dangerous one to mislabel: closing "no evidence" without the decode
check would have hidden that a future data import can light up a finished mechanic
with zero new keys.

## Procedure

1. Grep matrix over both JSONs (broad + literal tokens).
2. Full-blob census over every string field (python json walk), classify every hit.
3. Cross-check B5-0334 decode tables + rulebook set provenance.
4. Verdict names which outcome (1/2/3) applies and what each gated task should do
   (no-op close vs. await import vs. flag the specific rows).
