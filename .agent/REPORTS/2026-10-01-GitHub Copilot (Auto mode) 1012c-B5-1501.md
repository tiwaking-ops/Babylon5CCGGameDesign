---
author_llm: {name: "GitHub Copilot (Auto mode) 1012c", version: "Auto mode"}
---

# B5-1501 — queue offer-marker diagnostic

The shared dry-run census initially reported 16 claimable OPEN rows and one
offer, then subsequent runs reported 14 and 13 claimable rows. The queue's
offer markers are not stored under `.agent`; they live in the OS temp directory
`run-queue-offers-C__temp_projects_Babylon5CCGGameDesign__agent_TASK_LEDGER_md`.
That directory contained 147 historical `.offer` files.

`run-queue.ps1` stores the offering process PID in each marker. A marker with a
live PID returns unavailable; a marker with a dead PID is deleted and retried.
The observed recent markers included many dead PIDs and several live-PID
markers, including repeated PIDs across multiple task markers. A fresh
`-DryRun -MaxIterations 20` produced four offers before all remaining candidates
were rejected by the marker check. This establishes that marker state can
explain a one-offer run, but the observation does not distinguish genuine
concurrent lanes from PID reuse, so no tooling defect was asserted and no
queue script or coordination file was changed.

`b5ccg/compile.bat` passed with the expected Java 6 bootstrap warning. The
heartbeat validator remains red on pre-existing foreign residue (15
non-conforming files and 3 identity collisions); no foreign heartbeat or claim
was touched.

Reusable lesson: a PID-based coordination marker needs process identity, not
just a currently-live numeric PID, before a stale marker can be distinguished
from a live owner.
