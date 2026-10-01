---
document:
  title: "B5-0953 human-authorized administrative release"
  status: "Report (coordination action, no game work)"
provenance:
  author_llm: {name: "opencode (muse-spark-1.3)", version: "verify-seed-03"}
  created_date: "2026-09-28"
---

# B5-0953 administrative release — executed on explicit human order

**agent_id:** opencode (muse-spark-1.3) verify-seed-03
**date:** 2026-09-28T22:27-22:28Z
**outcome:** `.agent/CLAIMS/B5-0953.json` deleted. Row B5-0953 stays OPEN and is now offerable. No game files touched, no commit, no push.

## 1. Authorization

The user queried the operator behind `solar-pro4:free` (Hermes), who denied owning the claim, and then explicitly authorized a human-authorized administrative release. Stated reason: the claim most likely comes from an Omniroute-style rotating-model session that cannot be found and asked to re-claim, possibly with a mangled agent name. This matches the B5-0976 escalation's option 2, recorded there as acceptable to a human but not to an agent acting alone.

## 2. Three-signal evidence at release (wall clock 2026-09-28T22:27:26Z)

- Claim file mtime 18:04:33Z, age 263 min — STALE.
- Owner heartbeat `solar-pro4.json` mtime 18:04:40Z, age 263 min — STALE by mtime, but payload `utc` 2026-09-29T07:04:00Z is future-dated, so LIVE by content under the B5-0952 never-ages-out class. Mixed signal; the human order resolves the ambiguity.
- No completion report for B5-0953 on disk (only an aborted close-out attempt by an unrelated agent).

## 3. Claim never matched its row

- Claim description: engine refactor replacing `ResolveConflict()` with `ConflictResolver.Resolve()` in `b5ccg/src/b5ccg/engine/ConflictResolution.java`. `Test-Path` proves that file ABSENT.
- The B5-0953 ledger row is a census-crosscheck coordination task about B5-0481 tool divergence, explicitly scoped as NOT a reap row with no engine work.
- A refactor claim on a nonexistent file attached to a coordination row is the signature of a mangled or misrouted session, corroborating the human's Omniroute theory.

## 4. What was and was not touched

- Deleted: `.agent/CLAIMS/B5-0953.json` only.
- Left byte-identical: foreign heartbeat `solar-pro4.json`, the B5-0953 ledger row bytes (release recorded in a dated note line below the row, not spliced into it), all engine/model/ai/ui sources, all card data.
- Post-release proofs: run-dup-census PASS 0 duplicates; ledger-query OPEN and run-queue DryRun re-run to confirm the row is offerable (see DECISIONS entry).

## Reusable lesson

A claim whose description names a file that does not exist and whose row describes unrelated work is not a live worker to wait for — it is a misrouted artifact. Description-to-row mismatch is cheaper evidence than any clock reading and should be checked before any three-signal debate.
