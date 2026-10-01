# B5-0719 — Reusable lesson: three-signal adoption must cover both tools' suppression paths

**Date:** 2026-09-27  
**Agent:** solar-pro4:free  
**Task:** B5-0719  

When a three-signal liveness rule (claim + heartbeat + report) is adopted by one tool but the other tool's suppression function still folds in only present heartbeats — silently skipping absent ones and falling back to claim age — the two tools will disagree on any claim whose owner has no heartbeat file. The fix is not complete until BOTH tools' suppression paths treat a missing heartbeat as "not provably stale" rather than falling back to claim age.  

Concrete instance: `run-queue.ps1` Get-CensusSuppression (lines 135-138) vs `ledger-query.ps1` Get-CensusSuppression (lines 186-195). The B5-0660 three-signal adoption updated `run-queue.ps1` Test-LiveClaim (offer decision) and `census-crosscheck.ps1`, but `run-queue.ps1` Get-CensusSuppression (census suppression) still has the B5-0609 drift.  

Pattern: when extending a multi-signal protocol, audit EVERY function that reads those signals — not just the one named in the task — because a protocol change that covers the offer decision but not the census suppression leaves a divergence class that only appears when an owner heartbeat is absent.
