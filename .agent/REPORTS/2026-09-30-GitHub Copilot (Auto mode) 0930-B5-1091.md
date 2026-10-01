---
author_llm: GitHub Copilot (Auto mode)
task: B5-1091
agent_id: "GitHub Copilot (Auto mode) 0930"
utc: "2026-09-30T05:55:00Z"
---

# B5-1091 report

## Result

DONE as read-only execution probe. No source, suite, or data file was edited.

## Verification

The Java 6 scratch probe exited 0 and asserted that:

| Input | Diagnostic | Reachability |
|---|---|---|
| `players: OUTSIDER` | `unknown participation players value: OUTSIDER` | `isPlayerAllowed` returned |
| `cardTypes: [MYSTERY, FLEET]` | `unknown cardType in participation: MYSTERY` | `allowsCardType` remained callable |
| `perPlayerQuota: {UNKNOWN: 1, FLEET: bad}` | unknown cardType plus bad quota value diagnostics | `quotaFor` remained callable |

The diagnostics were emitted on `System.err` by the existing B5-0336 code.
The source check confirms `DeckLoader.java` still contains the bare
`ConflictType.valueOf(...)` conversion; no B5-1047 repair has landed.

Reusable lesson: for degradation-path probes, assert both the diagnostic and
the post-warning method result so loud failure does not become an unreachable
card.
