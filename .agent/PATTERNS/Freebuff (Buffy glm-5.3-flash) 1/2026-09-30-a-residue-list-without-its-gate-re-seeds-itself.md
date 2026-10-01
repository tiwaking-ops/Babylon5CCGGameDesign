---
document:
  title: "A residue list without its gate re-seeds itself; frozen reds are a class, not a queue"
  status: "Pattern (advisory; B5-0430 store, same tier as investigations/)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1094"
---

# Pattern: a residue list without its gate re-seeds itself

**Context.** B5-1094 triaged the heartbeat store's standing red. Two runs
~2.5 h apart showed 9→18 new conforming files while the 9 non-conforming
files and 3 collisions stayed byte-identical: a frozen residue, not a live
defect generator.

**Lesson 1 — measure the red twice before triaging it.** One snapshot
cannot distinguish "defects being generated" from "defects frozen at the
moment the rule tightened". The delta run reclassified the whole set from
urgent to inert, and the triage becomes an inventory instead of an alarm.

**Lesson 2 — a triage row is finished when every defect carries three
fields: owner, mechanism, and why the mechanism cannot run today.** Here
seven of nine share one unblocking gate (the deferred README merge of the
approved retirement policy), one needs a human enum ruling, and one is
forbidden a rename by B5-1066. Without that third field, the next triage
row re-derives the same list and files it again.

**Lesson 3 — the newest file in a collision pair is not automatically the
authoritative one.** `me-so-poor.json.bak` is *younger* than the live
`me-so-poor.json` it backs up. Liveness verdicts on colliding identities
are already untrustworthy; mtime seniority inside the pair is worse.

**Lesson 4 — record the blockage where the next agent will look.** The
battery's expected-red list declares validate-heartbeats standing-observed
1; this triage names all nine files behind that 1 so any change in the set
is detectable as a delta rather than discovered as a surprise.

Links: [B5-1094 report](../../REPORTS/2026-09-30-Freebuff%20(Buffy%20glm-5.3-flash)%201-B5-1094.md) ·
feeds the retirement-policy merge; expected-red contract per B5-1019.
