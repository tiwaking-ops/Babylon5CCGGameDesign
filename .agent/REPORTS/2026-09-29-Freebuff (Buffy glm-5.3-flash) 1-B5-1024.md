---
document:
  title: "B5-1024 close-out — what the two card sets actually are"
  status: "Close-out report (observation, no authority)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
---

# B5-1024 — The two card sets, measured

Task: establish the intended relationship between premiere.json (446 records)
and deluxe.json (383 records), adjudicate the claimed 58 deluxe-only / 121
premiere-only records, and report what the running code does at deck
construction. Read-only; no JSON, loader, or src edits.

## Intended relationship (rulebook + design documentation)

**Deluxe is the reprint edition of Premiere — a strict reprint subset with
zero set-exclusive content.** Three independent sources agree:

1. The human ruling embedded in `DeckLoader.loadBothSets` javadoc
   (`b5ccg/src/b5ccg/engine/DeckLoader.java`, ruling dated 2026-09-21):
   pool = all Deluxe + every Premiere card never reprinted, deduped by title,
   Deluxe wins. Cited by B5-0320 and B5-0996.
2. The human-authored pool baseline
   (`docs/reports/authored-card-pool-baseline-2026-09-28.md`): "Deluxe titles
   absent from Premiere: 0 — the deluxe set is a strict reprint subset."
3. The physical-source account (`investigations/b5-starter-deck-card-lists-research-2026-09-21.md`
   and the findings report): the 1998 Deluxe Edition *redistributed* fixed
   cards into boosters; Premiere cards remained tournament-legal.

## The census (instrument: Python json.loads on utf-8-strict bytes)

Reproduced baseline: premiere 446 records, deluxe 383; per-type tables match
the row exactly. The row's **overlap does not reproduce** under any key the
running code uses:

| Join key | twins | deluxe-only | premiere-only |
|---|---|---|---|
| **title (the loader's operative key)** | **383** | **0** | **63** |
| id (direct) | 0 | 383 | 446 |
| `de_`-stripped id (the seed-wave key) | 325 | 58 | 121 |
| imageKey | 383 | 0 | 63 |

**Truth under the operative key: deluxe = exactly the 383 reprinted premieres.
The 63 premiere-only records are genuinely never-reprinted cards (23 CHARACTER,
8 FLEET, 10 CONFLICT, 6 EVENT, 5 AGENDA, 5 LOCATION, 4 ENHANCEMENT, 1 AFTERMATH,
1 GROUP). Deluxe-only: 0 of every type.**

## Where the row's numbers came from (measured, not assumed)

The seed-wave-1020-1037 report names its method: joining deluxe→premiere with
the `de_` prefix stripped from ids. That key yields exactly 325/58/121 on
current bytes. The artifact is systematic: deluxe ids prefix by card type
(`de_am_*` for AFTERMATH) while premiere ids use full type words
(`aftermath_*`). All 58 "deluxe-only" records are AFTERMATH cards whose
stripped id (`am_*`) matches no premiere id but whose *title* matches a
premiere card — verified 58/58. Direct id comparison finds 0 twins because ids
are set-namespaced (`char_gkar` vs `de_char_gkar`).

## What the running code does (Java-sim, zero divergence)

`DeckLoader.loadBothSets` unions by title: all 383 deluxe + the 63
never-reprinted premieres = 446 records. It neither sums the files nor picks
one. A faithful port of the brace-splitting parser reproduces 446/383
identically, so no parse divergence affects the count.

## Answer to the row's disjunction

The 58 are **NEITHER designed set-exclusive content, NOR an incomplete data
load, NOR an authoring accident — they are a measurement artifact** (wrong
join key). The 121 "premiere-only" records are the same artifact seen from the
other side: 63 genuinely premiere-exclusive cards the loader correctly keeps,
plus the 58. A deck builder assuming "deluxe is the expanded edition" does not
silently drop 121 premiere cards — the actual loader keeps everything the
ruling says to keep. **No data defect found; nothing seeded**, per row scope.

## Gates

compile.sh green on JDK 8 (1 pre-existing warning, build successful);
run-dup-census exit 0 post-write; own row 7 pipes / doubleLead no; claim
released at close-out; no reap; no foreign artifact touched; no commit, no push.

## Reusable lesson

A census's overlap is only as good as its join key — the seed numbers
survived three downstream rows while the key they were computed with was
never re-derived; reproduce the *method*, not just the totals, and a
325/58/121 that no operative key reproduces is the finding, not the data.
