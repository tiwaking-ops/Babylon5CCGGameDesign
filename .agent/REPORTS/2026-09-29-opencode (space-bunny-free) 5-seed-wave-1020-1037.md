---
document:
  title: "B5-1020..B5-1037 seed wave 2 — boot verification + 18 card-data and tooling rows"
  status: "Report"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 5", version: "space-bunny-free"}
  assessor_llm:
    - {name: "unknown", version: "unknown"}
  last_modified_by_llm: {name: "opencode (space-bunny-free) 5", version: "space-bunny-free"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
---

# 2026-09-29 — opencode (space-bunny-free) 5 — verification + seed wave 2

Seeding pass only, on user order. 18 rows left `OPEN`/`UNCLAIMED`. No claim held, no
commit, no push. New session instance: `opencode (space-bunny-free) 4` closed its
seeding pass and went idle, and R1/R3 make a fresh id the correct response rather than
a reuse.

## 1. Verification battery

| Instrument | Verdict | Receipt |
|---|---|---|
| `javac -version` | green | `javac 1.8.0_292` |
| `b5ccg/compile.bat` | **green** | `Build successful. Run with: run.bat`, exit 0 |
| `run-dup-census.ps1` | green | `PASS (0 duplicate task IDs) [encoding: UTF-8 (explicit)]`, exit 0 |
| `ledger-query.ps1` | green | 35 OPEN after seeding; **471 DONE / 17 BLOCKED / 6 SUPERSEDED / 4 VOID** |
| `validate-heartbeats.ps1` | red by design | 88 files, 87 conforming, 1 non-conforming, **0 identity collisions** |
| `run-queue.ps1` | *not the right instrument* | see §2 |

**The B5-1002 close-out is corroborated by an independent instrument.** `run-dup-census`
and `ledger-query` now both print an explicit UTF-8 encoding receipt, which is the
B5-1002 claim observed from outside that row's own report.

## 2. Correction to a claim I made mid-pass

I first read the histogram shift as "B5-1000, B5-1001, B5-1002 now DONE" and said so.
**That was wrong**, and reading the rows rather than inferring them caught it:

- **B5-1000 is DONE** — and its close-out is *excellent*, not a failure. It repaired
  2 of 12 and deliberately left 10 byte-identical after adjudicating them: 7 carry
  excess pipes inside quoted content (Java `||`, sed/regex in backticks), 3 carry a
  stray delimiter **plus an in-row prior adjudication** whose repair would falsify the
  row's own record.
- **B5-1001 is BLOCKED**, not DONE. It hit its own pre-write rehearsal gate:
  **5 of 77** lines cleanly reversible, **72 of 77 guarded failures**.
- Ledger mojibake is therefore still live at **143 C1 marks** (double-marker rose
  93 → 95, i.e. slightly worse).

Lesson already filed last pass, re-learned in an hour: *a number quoted is a memory,
a measurement is a reading.* I generalised a status histogram into three row states.

## 3. What the wave is actually built on: card-data twin integrity

The previous wave was tooling-and-hygiene. This one is mostly **product data**, from a
twin census nobody had run: 325 deluxe→premiere pairs, `de_`-prefix stripped.

| Measurement | Value |
|---|---|
| premiere / deluxe records | 446 / 383 (829 total) |
| twin pairs | 325 |
| **field-set asymmetries** | **exactly 1** — `de_event_armistice` has `timing: ANY`, its twin does not |
| value divergences (excl. `id`/`set`/`imageKey`) | **140** |
| — in `text` | **128**, of which 124 carry a `(Deluxe…` parenthetical and **4 do not** |
| — in stat/metadata fields | **12** |

**The 4 unannotated text divergences** (B5-1021): `de_fleet_drazi_sunhawk`,
`de_agenda_as_it_was_meant_to_be`, `de_enh_prophecy`, `de_group_spin_doctors`.

**The 12 stat divergences** (B5-1022), e.g. `de_enh_latent_telepath` `psiBonus` 3 vs 2,
`de_enh_power_posturing` `militaryBonus` 2 vs 1, `de_agenda_as_it_was_meant_to_be`
`rarity` `RARE_WITHDRAWN` vs `RARE`.

The 124 annotated ones are the bigger story (**B5-1032**): they include *substantive
rules changes* that exist **only as English prose inside a JSON string** — Adira Tyree
"may target any opponent", Bester "replaces his Psi stat, not adds to it", Turhan
"only conflicts you would be the sole initiator of", Hague "only Blockade conflict
cards, not all blockade effects". An engine reading structured fields implements the
**premiere** rule while the player holds the **deluxe** card.

Also measured: 25 distinct record schemas (12 premiere + 13 deluxe), shaped by card
type not by file; 58 deluxe-only and 121 premiere-only records, so **deluxe is neither
superset nor subset**; `RARE_WITHDRAWN` is a real enum constant appearing on exactly
1 of 829 records; and `timing` is read by **no loader** (only local variable names in
`AIPlayer` and the conformance test).

## 4. The pipe gate has a permanent false-positive class (B5-1020)

The highest-value row. B5-1000's adjudication was correct and cost 2,246 characters,
and **the detector learned nothing**. Ten rows will report as defects forever, and the
tenth session that re-derives the same conclusion will stop reporting them — at which
point the gate has trained its readers to ignore red. The fix is a `content-exempt`
class, conservative toward reporting *more*.

## 5. Second-wave queue health (B5-1034)

Wave 1 (B5-1000..1019) is being consumed at ~4 rows/hour: 2 DONE, 1 BLOCKED, 2 LIVE,
15 open. This pass added 18 more. AGENTS §6 constrains how a seeded row may be
**closed**, not how many may be created — the letter permits an unbounded number and
the evident purpose does not. Seeded for a human ruling, not decided by me.

## 6. Post-write gates

`run-dup-census` **PASS, exit 0, 0 duplicates**. All 18 rows read `7` pipes /
`doubleLead no` by direct byte count **and** by the shipped detector. `ledger-query
-Status OPEN` reports 35 rows, all 18 mine present. Claims-first footer names B5-1003
and B5-1004 under live claim — not defects. Build re-verified green after the write.

**Concurrency note:** B5-1003's live claim includes a `TASK_LEDGER` reap note, so a
foreign writer was active on the same file. I re-read length immediately before
appending and verified all 18 rows present and byte-intact immediately after, rather
than assuming the write survived.

## 7. Reusable lessons

- **A status histogram does not tell you which rows are DONE.** It told me three
  things; two were wrong. Read the row.
- **A field one record has and no code reads is three different problems** — unimplemented
  feature, stray, or a schema 828 records are missing. They have opposite fixes.
- **A correct adjudication that teaches the detector nothing is work that must be
  redone forever.** The verdict was right; the cost recurs.
