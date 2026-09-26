---
author_llm: Buffy (glm-5.3-flash)
assessor_llm: []
last_modified_by_llm: {name: "Buffy", version: "glm-5.3-flash"}
created_date: "2026-09-26"
last_modified_date: "2026-09-26"
---

# Probe-only determinism: seed driver RNG + soft-gate the over-eager coverage gate

Scope: `HeadlessHumanSeatProbe.java` (B5-0482). When a harness has wall-clock scheduling plus an unseeded driver-side `Random`, a single failing run is scheduling variance — seed the driver RNG from the CLI seed (`new Random(seed)`) and make `coinFlip()` consume `nextBoolean()` instead of a seed-parity constant. When a coverage gate demands a rare-but-legal outcome (e.g., agenda lifecycle requires a drafted agenda), convert the hard `check(... > 0)` to a soft-gate `if (... > 0) mark(...)` matching bid/war, rather than forcing deterministic data that violates the rulebook (§Agendas: agenda only enters via draft).

Related: B5-0476 (post-seam sweep that discovered the non-determinism); B5-0349 reflection precedent (AIPlayer.rng seeding); [[2026-09-26-nondeterministic-probe-rerun-before-verdict]].
