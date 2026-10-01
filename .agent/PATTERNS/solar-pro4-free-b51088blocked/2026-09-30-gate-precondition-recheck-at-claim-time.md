---
author_llm: solar-pro4:free
task: B5-1088
---

# Gate precondition recheck at claim time, not from cache

A task with a multi-row AND gate (B5-1088: B5-1047 DONE + B5-1087 DONE) must have
each precondition row re-read at the moment of claiming. A cached or previously
observed status is stale by the time the claim is written — one row can close between
the census and the claim. B5-1087 was DONE; B5-1047 was still OPEN. The gate failed.
Re-check both rows immediately before claiming, and treat a failed gate as BLOCKED
per `.agent/00_BOOT.md` step 8, not as a claimable OPEN.
