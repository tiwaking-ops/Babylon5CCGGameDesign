---
author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
assessor_llm: []
created_date: "2026-09-26"
---

# Re-sweep after read-path edits must run the standalone probes

**Context:** B5-0486 landed a floor pass inside `FleetCard.getEffectiveMilitary()` — a scoring READ path. Its close-out verified the conformance suite (444/444) and smoke, but not the standalone probes. The next re-sweep (B5-0489) found `HeadlessLeadFleetScenarioProbe` crashing deterministically: the new `for (StatBonus b : owner.getBonuses())` loop sat outside the method's existing `if (owner != null)` guard, so owner-less fixture fleets NPE'd.

**Pattern:**
1. Any edit to a shared read path (stat/effective-value getters, registries) needs the standalone-probe sweep, not just the suite — probes keep minimal fixtures (owner-less, unclassed) that thin callers can reach. The 0446/0457/0476/0489 re-sweep rows exist for exactly this; treat "suite green" as necessary, never sufficient, after touching read paths.
2. When a method already has a null guard, new code added to it must land INSIDE the guarded region — or re-state the guard. A guard protects the code that was there when it was written, not code added later.
3. Probe exit codes: capture the java process's exit (`java …; echo $?`), not a downstream `tail`/pipe — a crashing probe can look green through a pipeline. Repeat crashing runs to prove determinism before diagnosing (0476 flake lesson).

**Inverse lesson for close-outs:** when your task touches a read path, run at least the probes that exercise that path (here: 0384 lead-fleet calls `getEffectiveMilitary` via `executeLeadFleet`) before marking DONE — the next agent's re-sweep should be a backstop, not the first line of defense.
