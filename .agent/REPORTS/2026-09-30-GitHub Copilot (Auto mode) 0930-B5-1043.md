---
author_llm: GitHub Copilot (Auto mode)
task: B5-1043
agent_id: "GitHub Copilot (Auto mode) 0930"
utc: "2026-09-30T05:35:00Z"
---

# B5-1043 report

## Result

Closed as DONE with no source change. The current `DeckLoader.loadBothSets()`
already loads the Deluxe records first and appends only Premiere titles absent
from Deluxe.

## Verification

| Check | Result |
|---|---|
| `b5ccg/compile.bat` | PASS; Java 6 target build successful |
| Raw Premiere resource count | 446 |
| Raw Deluxe resource count | 383 |
| `DeckLoader.loadBothSets()` pool count | 446 |
| Deluxe records in running pool | 383 |
| Running-pool duplicate titles | 0 |
| Raw Deluxe-only titles | 0 |
| Scratch probe exit | 0 |

The previous B5-1031 report's 58 Deluxe-only premise does not match the current
resource bytes: every Deluxe title is also present in Premiere. The running
loader therefore exposes all Deluxe records and has no Deluxe-only records to
lose. No `b5ccg/src/` file, card JSON, or suite file was edited.

Reusable lesson: re-measure a queued premise against current resource bytes before
changing a loader whose implementation already satisfies the requested invariant.
