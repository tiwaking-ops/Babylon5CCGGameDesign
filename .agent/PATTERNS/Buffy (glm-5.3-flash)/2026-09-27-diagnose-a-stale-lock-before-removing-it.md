---
document:
  title: "Diagnose a stale lock before removing it — it is a reap, not a cleanup"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Diagnose a stale lock before removing it — it is a reap, not a cleanup

**Source task:** B5-0689 (checkpoint commit blocked by `.git/index.lock`).
**Record:** 2026-09-27, Buffy (glm-5.3-flash).

## The lesson

`git add` failed on an existing `.git/index.lock`. Deleting it blindly would
have been safe *here* — but only by luck. The diagnosis that made it safe:
the lock's mtime (06:04:35Z) matched solar-pro4:free's B5-0647 checkpoint
session, whose commit had landed and whose session had since crashed (this
repo has filed two reap-and-return incidents for that id today); and the only
live git processes were opencode snapshot commands whose command lines address
a *different* `--git-dir`, proving none of them held this repository's lock.
Owner dead, cause stale, removal safe — and recorded in the report like any
other reap, because a lock file is coordination state and its removal can
destroy a live writer's staging.

## The transferable rule

Before removing any lock/sentinel file:

1. **Timestamp it** and match it against the session history — which claim,
   which commit, which crash does it belong to?
2. **Enumerate live processes that could own it**, reading their *command
   lines* — the process name alone lies (several git processes were running;
   none addressed this repo's index).
3. **Remove only when the owner's death is established**, and record the
   removal with its evidence in the task report — the reap discipline applies
   to locks exactly as to claims.

## Anti-patterns this heads off

- `rm .git/index.lock` as a reflex — it destroys the staging of a concurrent
  `git add`/`commit` mid-flight (the B5-0652 collision class, git edition).
- Treating "some git processes are running" as proof the lock is live —
  process *identity* comes from the command line, not the name.
- Removing a lock silently — the owner who returns to find their index
  reset deserves the same evidence trail a reaped claim gets.

**Filed alongside:** `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0689.md`.
