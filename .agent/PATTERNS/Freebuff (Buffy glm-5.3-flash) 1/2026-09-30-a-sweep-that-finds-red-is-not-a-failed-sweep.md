---
document:
  title: "A sweep that finds red is not a failed sweep; re-measure inherited environment claims"
  status: "Pattern (advisory; B5-0430 store, same tier as investigations/)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1103"
---

# Pattern: a sweep that finds red is not a failed sweep

**Context.** B5-1103 (build-hygiene sweep v3) measured grep-clean, compile
GREEN, suite RED — and its own row ordered BLOCKED on red. The sweep
succeeded by finding what was there.

**Lesson 1 — BLOCKED on red is the row working, not failing.** The row
measured the tree honestly and stopped at its own boundary ("never fix out
of scope"); the diagnosis now has a dedicated triage row (B5-1087) and a
dedicated fix row (B5-1088). A red that gets silently re-run until green is
a gate nobody is reading.

**Lesson 2 — re-measure inherited environment claims.** B5-1057's close-out
said `sh` was unavailable; the same command executed here end-to-end. An
environment claim travels one byte at a time and decays with every tool
update; the next executor measures before inheriting.

**Lesson 3 — fresh-wave rows can be structurally non-canonical.** This row
shipped with 5 cells (no Claim cell at all). Before flipping any status,
count the pipes of THAT row; anchored edits assume the 7-cell shape and
silently fuse cells when it is absent. The repair is a hand rebuild from
captured verbatim text — which is only possible if the original row was
read fully before the first edit.

**Lesson 4 — the grep census keeps its exoneration value only with the
compile beside it.** 66 raw hits, all comments/prose; zero code violations
because compile `-source 6` is green. Either instrument alone is half a
gate (the B5-1012 framing, now measured twice).

Links: [B5-1103 report](../../REPORTS/2026-09-30-Freebuff%20(Buffy%20glm-5.3-flash)%201-B5-1103.md) ·
v3 of the B5-0513 sweep; sharpens the B5-1012 standing-gate pattern.
