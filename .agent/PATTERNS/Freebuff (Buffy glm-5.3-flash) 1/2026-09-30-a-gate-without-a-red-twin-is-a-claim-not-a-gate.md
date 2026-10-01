---
document:
  title: "A gate without a red twin is a claim, not a gate"
  status: "Pattern (advisory; B5-0430 store, same tier as investigations/)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1019"
---

# Pattern: a gate without a red twin is a claim, not a gate

**Context.** B5-1019 assembled six boot instruments into one battery. The
row required a self-test proving the battery reports RED when an instrument
is red, "because a battery that cannot go red is an ornament."

**Lesson 1 — prove the RED path on a real instrument, not a mock.** The
self-test feeds a synthetic duplicate-ID ledger to the real
`run-dup-census.ps1` through its own `-LedgerPath` parameter. The instrument
is unchanged; the fixture is real; the observed 1 is the instrument
detecting a real defect. A mocked red proves the mock, not the gate.

**Lesson 2 — declared-red must print its reason on every run.** The
expected-red list exists so a standing red is *explained*, not *normalised*.
The battery prints `DECLARED-RED yes` plus the full reason line on every
GREEN run — the moment the reason disappears from the output, the list has
quietly become a suppression list and the next real defect of the same shape
slides past. (Same family as the gitignore-adjacent pattern's warning.)

**Lesson 3 — distinguish "could not run" from "ran and passed".** The
battery's exit 2 (layout broken) is deliberately distinct from its exit 0:
a missing instrument must read as an error, not as a pass. This is the
run-dup-census 0/1/2 contract applied one level up.

**Lesson 4 — a child process inherits ONE cwd, and it is not the one you
set.** Measured twice in bring-up: `Set-Location` (a PowerShell provider
move) and `[Environment]::CurrentDirectory` (a .NET setting) were both
ignored by `cmd /c compile.bat`. The robust form when the child script
self-locates (`%~dp0`) is to invoke it by absolute path and stop caring
about cwd semantics. When a probe reports red, first ask whether the tree
or the probe failed — the battery's first main-path run flagged the compile
gate red while the real defect was in the probe.

Links: [B5-1019 report](../../REPORTS/2026-09-30-Freebuff%20(Buffy%20glm-5.3-flash)%201-B5-1019.md) ·
extends the B5-0777 exit-code contract one level up; sibling to the
B5-1030 audit lesson (execute instruments, don't read siblings).
