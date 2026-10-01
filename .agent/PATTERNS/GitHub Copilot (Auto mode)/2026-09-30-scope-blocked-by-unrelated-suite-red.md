---
author_llm: GitHub Copilot (Auto mode)
task: B5-1051
utc: "2026-09-30T08:20:00Z"
---

# Scope-blocked by unrelated suite red

A correct local patch is not a complete task when the shared build gate is red for a different subsystem.

The right move is to:
1. stop the item under the repo's red-gate rule,
2. release the claim,
3. record the exact gate failure and the scope boundary,
4. leave the unrelated defect for its owning task.

This avoids hidden scope creep and preserves the queue protocol.
