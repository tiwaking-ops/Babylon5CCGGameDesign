---
document:
  title: "B5-0900 close-out - opt-in seed branch in run-queue.ps1"
  status: "Report"
provenance:
  author_llm: {name: "opencode (big-pickle-free) bp3", version: "big-pickle-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (big-pickle-free) bp3", version: "big-pickle-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0900 close-out: the runner could never seed

## The gap, stated as the code sees it

`.agent/AGENT_LOOP.md` LOOP step 1 makes seeding an agent duty, and the user's
mental model was that the runner does it. Measured this session: it never has.
The runner's drain arm was a three-line `if` that printed
`Queue drained: no OPEN task without a live claim. Done.` and broke. `git log -G
seed -- .agent/run-queue.ps1` returned prose only, never a function or a call
site. The root `seed_tasks.sh` exists but is a one-shot ledger appender, not part
of the loop. So the fleet had a documented self-feeding step that no executable
path reached.

## What this pass changed

`.agent/run-queue.ps1`, 130 insertions and 2 deletions in six hunks:

1. Header documentation of the branch.
2. `param(...)`: `[int]$MaxSeedInvocations = 0`.
3. New `Test-SeedableQueue` immediately after `Get-ClaimableOpenTasks`.
4. `$SeedPromptTemplate` plus an AGENT_LOOP step-1 boundary paragraph inside
   `$TaskPromptTemplate`, so a task agent does not seed on the side.
5. The drain arm: an opt-in branch inside the existing `if (-not $free ...)` arm.

The six hunks are the whole footprint. The pre-existing B5-0771 `REPORTS` path fix
in `Get-CensusSuppression` and `Test-LiveClaim` is present in the diff and was not
touched; the liveness, TTL, lane, prerequisite and duplicate-census regions carry
no hunk at all, so they are byte-identical.

## The three defects found by testing, not by reading

**A dry run that looped.** The first working version printed its decision and
then `continue`d, so `-DryRun -MaxIterations 3` printed the same "would invoke"
line three times with the counter stuck at `1/1` and the run's own output claimed
"this run ends here" while it visibly did not. Nothing is invoked in a dry run, so
no iteration can change the next iteration's answer. Fixed by ending the run on
the decision, with the reason stated in the output.

**A native CLI, not a PowerShell one, is what the runner talks to.** The first
end-to-end attempt used a `.ps1` stub as `-AgentCli`. It failed with a parser
error on the prompt text: a PowerShell-based CLI re-parses the multi-line prompt as
code. That is the B5-0628 lesson's second half landing on a test harness, not a
runner bug - the task prompt has the same multi-line shape and the same
invocation code. Replaced with a Java 6 stub (JDK 1.8.0_292 was already on the
path for the build gate): the native process received the seed prompt as one argv
entry, 1824 characters, 22 newlines intact, and zero surviving double quotes,
which is the `-replace '"', "'"` neutralisation doing its job. A second run proved
the row that stub wrote was claimed on the very next iteration.

**The stagnation counter would have eaten a good seed.** With
`-BlockedStreakLimit 1`, iteration 1 (the seed) produced no streak message and the
run continued; iteration 2 (the task) produced `stagnant streak: 1/1` and stopped.
The discriminating test: had the seed been counted, the run would have halted
immediately after the seed with a false "shared red gate" diagnosis. This is why
the branch deliberately never touches `$stagnant`.

## Guard behaviour, with the hold named

The branch is a literal reading of "no claimable OPEN task", which is a
three-valued state, two thirds of which must not seed:

| Fixture ledger state | Branch | Evidence |
|---|---|---|
| All rows terminal, no claims | fires | `[1] Queue drained and seedable: one agent invocation ... (seed invocation 1/1).` |
| Default `-MaxSeedInvocations` omitted | silent | `[1] Queue drained: no OPEN task without a live claim. Done.` |
| One BLOCKED row, otherwise drained | holds, names it | `Holding seed: 1 row(s) still read OPEN, CLAIMED or BLOCKED (B5-0003).` |
| Live claim on a DONE row | holds, names it | `Holding seed: a live claim exists (B5-0004).` |
| Real repo, 11 claimable OPEN rows | never reached | dry run only ever claimed lanes |

Both holds print the ids that caused them, because a silent hold cannot be
distinguished from the bug this branch fixes. The live-claim rule is literal, so
residue on closed rows holds seeding: the real repo still carries
`.agent/CLAIMS/B5-0481.json` with a midnight placeholder `started_utc`, and that
would block production seeding today. Intended, and recorded in
`docs/DECISIONS.md`: reaping a claim is a human decision, and a runner that reaps
to unblock itself is the behaviour the guard exists to prevent.

## Gates

- `powershell -NoProfile -ExecutionPolicy Bypass -File .agent/run-queue.ps1 -DryRun -MaxIterations 2 -MaxSeedInvocations 1` on the real repo: exit 0, branch never reached, no side effects.
- `parse` of the file via `[System.Management.Automation.Language.Parser]`: 0 errors.
- `run-dup-census.ps1`: `PASS (0 duplicate task IDs)`, exit 0.
- `ledger-query.ps1 -Status OPEN`: B5-0900 at `pipeCount 7`, `doubleLead no`.
- `validate-heartbeats.ps1`: exit 0.
- `b5ccg/compile.bat -source 6` on JDK 1.8.0_292: `Build successful.`, exit 0. (The compile script lives in `b5ccg/`, not the repo root; a root-level `compile.bat` does not exist and a first attempt failed on that path, not on the code.)
- Production behaviour is unchanged until a human passes `-MaxSeedInvocations N`.

## Not done, deliberately

- `b5ccg/src/` untouched. The build ran only as the close-out gate.
- No commit. `.agent/AGENT_LOOP.md` says the loop does not commit.
- Root `seed_tasks.sh` not executed and not modified; B5-0841 is assessing it.
- Foreign rows, claims and heartbeats untouched, including B5-0481's residue.

## Reusable lesson

A dry run that changes nothing should decide once, not once per iteration - and
when the first end-to-end test of a script says "the tool is broken", check
whether the tool you picked shares a shell with the code under test before
believing it.
