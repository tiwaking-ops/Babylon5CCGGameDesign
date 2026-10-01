---
document:
  title: "A dry run that changes nothing should decide once, not once per iteration"
  status: "Pattern (advisory, non-canonical)"
provenance:
  author_llm: {name: "opencode (big-pickle-free) bp3", version: "big-pickle-free"}
  created_date: "2026-09-28"
---

# A dry run that changes nothing should decide once, not once per iteration

Learned on B5-0900 while adding an opt-in seed branch to `.agent/run-queue.ps1`.

The first working version printed its decision and then continued the loop. Under
`-DryRun` it printed the same "would invoke" line three times, with the counter
stuck at `1/1`, and its own closing sentence claimed the run ended there while the
output showed it did not. A dry run invokes nothing, so no iteration can change
the next iteration's answer: repeating the decision adds no evidence and reads as
either a loop bug or a counter bug, and the reader of the log has to work out
which.

**Rule.** If a decision provably cannot change between iterations (nothing was
invoked, nothing was written), emit it once and end the run, and say in the output
that the run ends there and why.

The same shape shows up whenever a "would" branch is placed inside a retry loop.
Ask of any such branch: what state does the next iteration read that this one
changed? If the honest answer is "none", break instead of continuing.

**Second half, from the same task.** The first end-to-end test of the new branch
used a PowerShell script as a stand-in for the agent CLI, and the CLI appeared to
be broken: a parser error on the prompt text, at the text, not at a call site. The
runner hands a multi-line prompt to a native executable, so a stub that shares a
shell with the code under test re-parses that prompt as code. Before believing
that the code under test is broken, check whether the substitute shares a shell
with it. A tiny Java 6 stub (JDK 8 was already installed for the build gate)
received the prompt as one intact argv entry - 1824 characters, 22 newlines,
zero surviving double quotes - and proved the branch end to end.

Supersedes nothing. Advisory only, per `.agent/PATTERNS/README.md`.

Report: `.agent/REPORTS/2026-09-28-opencode (big-pickle-free) bp3-B5-0900.md`.
