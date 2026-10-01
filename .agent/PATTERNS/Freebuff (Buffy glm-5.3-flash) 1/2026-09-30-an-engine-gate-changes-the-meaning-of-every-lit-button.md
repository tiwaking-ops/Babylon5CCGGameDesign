---
document:
  title: "An engine gate changes the meaning of every lit button; preview and enablement are different duties"
  status: "Pattern (advisory; B5-0430 store, same tier as investigations/)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1157"
---

# Pattern: an engine gate changes the meaning of every lit button

**Context.** B5-1157 re-audited the UI after the B5-1038 affordability
gate. Twelve of thirteen closed F-findings hold; the partial is F5: the
engine now refuses unaffordable plays, but the Play button's enablement
predicate (MainWindow 2125–2127) never consults `canPlayCard`, so the
refusal happens after the click. The preview (B5-1049's UNAFFORDABLE list)
does show the information — preview and enablement simply never met.

**Lesson 1 — adding an engine refusal retroactively breaks UI promises.**
Buttons lit under a no-refusal regime were honest then and are dishonest
now; the regression lives in predicates written before the gate, not in
the gate. Any engine-side gate needs a sweep of the enablement predicates
that route to it.

**Lesson 2 — showing the state and gating the action are separate
duties.** A preview label is not a button predicate; the two can drift for
months because they fail differently (a stale label misleads, a lit button
refuses at commit). The smallest honest closure is to make the button read
the same rules method the engine enforces — one intersection, no parallel
truth.

**Lesson 3 — a re-audit row pays when it checks the mechanism, not the
existence.** All thirteen findings still had their code present; the audit
earned its keep only by reading what each predicate actually consults
today.

Links: [B5-1157 report](../../REPORTS/2026-09-30-Freebuff%20(Buffy%20glm-5.3-flash)%201-B5-1157.md) ·
the player-facing twin of the B5-1121 sponsor-discount question.
