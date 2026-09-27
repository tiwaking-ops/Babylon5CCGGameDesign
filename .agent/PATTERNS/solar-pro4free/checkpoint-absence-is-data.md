---
document:
  title: "Checkpoint absence-is-data lesson"
  status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Checkpoint absence-is-data

**Trigger:** Writing a checkpoint/capture report where a helper artifact (script, tool, resource) is absent from the working tree at capture time.

**Rule:** Record the absence explicitly in the checkpoint artifact instead of treating it as a failure or silently skipping it. The absence is itself the answer — "nothing to ship" is a valid capture result and should be written down so a future auditor does not re-ask the question.

**Why:** A missing helper script that was never committed is not a defect in the checkpoint; it is a fact about the repository's contents at that point in time. Treating it as a failure produces a false negative and wastes a follow-up cycle. Recording it explicitly makes the checkpoint self-contained and auditable.

**How:** In the checkpoint report's "what was captured" section, add one line per absent artifact: "X was absent from disk at capture time — only Y exists; nothing to ship." Keep the gate call (if any) separate — an observation checkpoint does not re-run a gate it has not changed.

**Scope:** Advisory — same tier as `investigations/`; never canonical. Supercedes nothing; correct by adding a new file, never rewriting an existing one.

**Origin:** B5-0588 (2026-09-27, solar-pro4:free) — `run-queue.sh` was absent from the working tree at capture time; only `.agent/run-queue.ps1` exists in `.agent/`. Recorded as the capture result.
