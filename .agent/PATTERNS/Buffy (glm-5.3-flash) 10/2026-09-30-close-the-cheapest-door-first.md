---
document:
  title: "Pattern — close the cheapest door first"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 10", version: "glm-5.3-flash"}
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 10", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# Close the cheapest door first

**Context.** B5-1109's probe established a corrupting path: a `CharacterCard`
reaching the generic `applyGenericCardPlay` dispatch is charged raw and
discarded without being seated. The deep fix — rerouting or refusing characters
inside the engine dispatch — sits behind the B5-1047 → B5-1088 chain, which is
BLOCKED at a future-dated chain head nobody can claim.

**What happened.** The report also named a smaller fix: the UI's Play Card
button. Its enable gate read no type route and its handler refused only
`ConflictCard`, so the corrupting path was *reachable from the button*. Two
guards in `MainWindow.java` — refuse `CharacterCard` in `playOnly`, exclude it
from `canPlay` — close that reachability completely, without touching the
engine, under a gate (compile green) that is actually runnable in the current
tree state.

**The pattern.**

1. When a defect has a layered fix and the deep layer is gated behind a blocked
   chain, check whether the affordance layer can close *reachability* now. A
   path no input can take is a path that cannot corrupt state.
2. Prefer refuse-and-never-offer (enable predicate) over offer-then-refuse
   (handler guard) — do both when the handler is a dispatch seam a future
   caller might reuse.
3. Name the remaining deep fix in the report and leave it in the backlog
   explicitly; a closed door is mitigation, not a fix, and the next agent
   should not have to rediscover that.
4. Match the gate to the layer: a UI-scope row closed on compile green is
   honest when the standing red suite is owned by another row and the UI change
   cannot alter engine behaviour.

**Bounds.** Advisory only; provenance per AGENTS.md section 1; supersedes
nothing.
