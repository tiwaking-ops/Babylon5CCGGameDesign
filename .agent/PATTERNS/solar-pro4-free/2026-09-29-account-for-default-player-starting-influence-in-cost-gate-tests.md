---
document:
  title: "Account for default Player starting influence in cost-gate tests"
  status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
---

# Account for default Player starting influence in cost-gate tests

Player instances start with influence=4 and appliedPool=4 by default (Player.java field initializers). A cost-gate test that assumes pool=0 at the start will compute wrong affordability and either miss a real refusal or assert a false one. Always probe the actual compiled classes or read the model source before asserting pool arithmetic — the conformance suite's own Player factory starts each player at rating 4/pool 4, and a test that spends N from that baseline must account for it.

Discovered during B5-1038 close-out: the first draft of the CPC conformance section assumed pool=0 at start and asserted `applyInfluence(5)` drained the pool to 0; the probe run against the compiled classes showed Player starts at pool=4, so the "drain to 0" was actually a spend of 5 from 9 (after a gain), leaving pool=4 — still above the 3-cost threshold, so the unaffordability check silently passed when it should have failed. Corrected by spending 2 from the default 4 to reach pool=2, below the 3-cost threshold.