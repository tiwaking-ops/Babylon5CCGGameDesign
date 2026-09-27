---
document:
  title: "Prose inside a JSON field is not a field"
  status: "Advisory pattern (never canonical)"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A task ID mentioned in a summary is not a task held

## What happened

Another agent read my heartbeat file to work out what I was doing. Its
conclusion, quoted from the transcript:

> "The heartbeat file for `opencode (me-so-poor)` mentions ... and claims
> B5-0572/0573/0574/0577."

I hold no claims. The file says:

```json
"live_claims": [],
"current_task": null
```

`B5-0572`, `B5-0573`, `B5-0574` and `B5-0577` appear **only inside the
`last_completed` narrative string** — a sentence describing a *census I ran*,
naming rows I inspected. The other agent grepped the blob for task IDs and got
four claims that do not exist.

This is a false claim attribution across agents, derived from prose. It is more
dangerous than a slow query, because it is confidently wrong and it points at
another agent's live work.

## The practice

1. **Parse fields, never substrings.** `live_claims` is the claim set. Nothing
   else in a heartbeat is. A regex over the raw JSON is not a substitute.
2. **Keep narration out of machine-read files**, or make it unmistakably prose.
   A `last_completed` string that enumerates task IDs is a grep magnet. If it
   must name them, keep the list out of the file and put it in a report — the
   place prose belongs.
3. **Distinguish "mentioned" from "held"** in any tool output. A cross-reference
   audit should report prose-mentions as their own class, so a human can see the
   difference instead of inferring it.
4. **When two signals disagree, the structured one wins and the disagreement
   gets reported.** Never let a prose match override an empty array.

## Related — three passes, three tools, one cause

This is the third consecutive pass in which a cheap heuristic produced a
confident wrong answer about shared state:

| Pass | Cheap signal | What it actually measured |
|---|---|---|
| 1 | pipe count during another agent's repair | intent, not state |
| 2 | claim-file mtime | staleness, not idleness |
| 3 | task IDs inside a JSON blob | mention, not ownership |

The generalization: **a snapshot of shared state is not a fact about the
process that produced it.** Grep, timestamps, and delimiters are all
*observations of bytes*. Every one of them was wrong in the direction that
looked safe — mid-repair read as corruption, staleness read as abandonment, a
mention read as a claim. That asymmetry is the tell: when a heuristic's failure
mode is invisible, it will be trusted.

The discipline that actually works is to ask what the signal *cannot* tell you,
and to prefer a tool that fails loud over one that fails plausibly.

## Related records

- `a-census-during-someone-elses-repair-reads-intent-not-state.md`
- `claim-mtime-is-not-a-liveness-signal.md`
- Buffy (glm-5.3-flash), `2026-09-26-a-pre-write-grep-is-not-a-lease.md`
- B5-0609 — the read-only query tool seeded from this, which parses structured
  fields only and must report `UNKNOWN` rather than defaulting.
- B5-0598 — the cross-reference audit, which must separate prose-mentions from
  claims.

Advisory only. This record confers no authority; see `AGENTS.md` §6.
