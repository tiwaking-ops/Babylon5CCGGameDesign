---
document:
  title: "Read the throw-order before designing the log fix; a handoff row is a gift, wrap it"
  status: "Pattern (advisory; B5-0430 store, same tier as investigations/)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1111"
---

# Pattern: read the throw-order before designing the log fix

**Context.** B5-1111 decomposed the loader's UNKNOWN FIELD fire (2,699
lines/pass) for the B5-1068 owner. The source shows the trio `id/title/
type` is presence-checked at lines 326–332 but never added to the expected
set, and the AGENDA (364–368) and AFTERMATH (369–373) switch cases are
comment-only — their required keys live only in the missing-required
switch at 430/442. Six `expected.add` calls are the whole fix.

**Lesson 1 — check-then-throw inverts the warning's reachability.** The
required-key checks throw before the unknown-field loop runs, so a record
missing a required key can never warn — the log only ever fires on
compliant records. Any fix design that tunes the warning without reading
the throw-order fixes the wrong half.

**Lesson 2 — two independent censuses that reconcile to the line turn a
dispute into a mechanism.** 2,699 (single pass) ×2 = 5,398 (double pass):
the disagreement between two probes was the discovery of the two-pass
structure. Reconciliation is stronger than agreement.

**Lesson 3 — a handoff row is measured in the next agent's edit count.**
"6 set-adds, here are the line numbers, here is the trap, here is what
NOT to touch" converts a logging redesign temptation into a mechanical
patch. Delivering the decomposition — not just the diagnosis — is the
row's whole value.

Links: [B5-1111 report](../../REPORTS/2026-09-30-Freebuff%20(Buffy%20glm-5.3-flash)%201-B5-1111.md) ·
consumed by B5-1068; census lineage B5-1104 → B5-1111.
