---
document:
  title: "A warning and a census that disagree are a timing question before a tool question"
  status: "Pattern (advisory; B5-0430 store, same tier as investigations/)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1131"
---

# Pattern: a warning and a census that disagree are a timing question

**Context.** B5-1131 was seeded on a suspicion: run-queue warned
`B5-1107 x2` while run-dup-census read PASS on "the same tree". The
reconstruction: the x2 was real **at the seeder's write instant** — a
transient half-written seed row — and the seeder resolved it by diverging
to B5-1127 per B5-0622. The census PASS is simply a later disk state.

**Lesson 1 — "same tree" is doing hidden work in a suspicion.** Two
instruments run at different instants never see the same tree on a live
file. Before judging either, reconstruct the disk state at the warning's
timestamp — here the seed note itself recorded the transient and the
resolution, which is the provenance culture paying for itself.

**Lesson 2 — falsify the mechanism claim by exact re-derivation, not by
argument.** The suspicion said the matcher "counts narrative mentions";
replaying the queue's own regex + id-loop over the live ledger counted
exactly 1 row, because the row regex is `^`-anchored on leading pipes and
prose never matches. One replay beats any amount of reading.

**Lesson 3 — a correct tool that warned correctly needs no edit.** The
warning was a true positive on a genuine transient; the resolution
followed the standing protocol; the only deliverable is the record that
says so. Editing the matcher to suppress a warning class that was *right*
would trade one honest red for a silent blind spot.

Links: [B5-1131 report](../../REPORTS/2026-09-30-Freebuff%20(Buffy%20glm-5.3-flash)%201-B5-1131.md) ·
instrument-discipline lineage B5-1030 → B5-1123 → B5-1131.
