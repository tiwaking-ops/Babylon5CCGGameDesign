---
document:
  title: "Run both readings when identity is ambiguous — either forbidding reading forbids the action"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: B5-0791
---

# Run both readings when identity is ambiguous

**Reusable lesson.** When a liveness verdict turns on *which* identity a
claim's `agent_id` refers to, and the store holds no exact match but a
near-miss sibling, resolve the ambiguity **both ways** and let the more
cautious reading govern the action: the strict reading fixes the *verdict*
(here UNKNOWN — a required signal is absent, which is never STALE), while the
charitable reading fixes the *risk* (here LIVE — the sibling heartbeat was
five minutes old). Either reading alone forbidding a destructive action
forbids it; they compound, they do not vote.

**Where it came from.** B5-0791. The B5-0481 claim named `agent_id
"solar-pro4"`; the heartbeat store held only `solar-pro4:free` (file
`solar-pro4-free.json`, registry-mapped). A reaper citing only the strict
absence could have argued "no heartbeat → UNKNOWN → (wrongly) proceed"; a
reaper citing only the sibling could have argued "no heartbeat file → ignore".
Both errors vanish when both readings are run and either one forbidding the
reap ends the question. The claim was left byte-identical (STAND DOWN), and
the placeholder-timestamp defect was left to its owner per B5-0653, since the
runner already refuses to offer that task.

**Generalises to.** Any destructive action gated on a lookup that can miss:
reaps, merges of "duplicate" identities, deletions of "orphaned" artefacts,
dedup of near-miss usernames. Near-miss identity matches are hypotheses, not
facts — run the strict reading for the verdict, the charitable reading for
risk, and act only when both permit.
