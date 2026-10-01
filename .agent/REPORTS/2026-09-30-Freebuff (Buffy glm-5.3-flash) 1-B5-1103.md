---
document:
  title: "B5-1103 — build-hygiene sweep v3: grep clean, compile green, suite red (BLOCKED per the row's own letter)"
  status: "BLOCKED 2026-09-30 (the tree is red; the sweep itself succeeded)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  claimed_at: "2026-09-30T05:49:37Z"
---

# B5-1103 — the sweep found red, so the row ends BLOCKED

## Part 1 — Java 6 construct grep (instrument: `grep -r -F`, b5ccg/src, archive excluded)

| pattern | raw hits | classification |
|---|---|---|
| `lambda` | 3 | all in "Java 6 only: no lambdas…" comments |
| `->` | 50 | comments + prose arrows |
| `::` | 0 | — |
| `stream()` | 0 | — |
| `computeIfAbsent` | 0 | — |
| `@FunctionalInterface` | 0 | — |
| `<>` (diamond) | 0 | — |
| `try (` | 13 | all comments (ATTACHED-scope notes, check() labels) |

**Zero code violations.** The decisive instrument for that conclusion is the
compile gate below — a construct hiding in code would not compile at
`-source 6` (the B5-1012 standing-gate lesson: a clean grep is necessary,
a red compile is decisive; here both agree).

## Part 2 — compile gate: GREEN, exit 0

`cmd /c b5ccg\compile.bat` → `Build successful. Run with: run.bat`
(3 files copied; `javac -source 6` warnings only).

## Part 3 — RUN_TESTS=1: RED, exit 1 (verbatim excerpt)

```
CONFORMANCE SUITE FAILED - unexpected exception:
java.lang.ClassCastException: b5ccg.model.FleetCard cannot be cast to b5ccg.model.ConflictCard
	at b5ccg.engine.HeadlessConformanceTest.testParticipation(HeadlessConformanceTest.java:507)
	at b5ccg.engine.HeadlessConformanceTest.main(HeadlessConformanceTest.java:6245)
```

Last passing line: the `[PAR]` section's loader-hydration check. The red
**reproduces the B5-1057 measurement exactly** — with one correction that
row's close-out deserves: `sh` **is** available in this environment and the
script executed end-to-end; the gate failed on the suite, not on a missing
shell. Diagnosis and the fix belong to the already-seeded B5-1087 (triage)
and B5-1088 (fix) rows; per this row's own instruction and 00_BOOT step 8,
the sweep stopped without touching anything.

## Repair note (own row)

The fresh wave's row shipped 5 cells (no Claim cell). My first flip fused
cells; my first rebuild mis-indexed them; the final state is a full hand
rebuild from session-captured verbatim text — Task restored exactly,
Claim restored, close-out in Verified with the repair note. Detector reads
`7 | no`; run-dup-census exit 0.

**Reusable lesson:** a sweep that finds red is not a failed sweep — it is
the sweep working; and "sh unavailable" is an environment claim that the
next executor must re-measure before inheriting.
