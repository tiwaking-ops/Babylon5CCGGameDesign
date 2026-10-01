---
document:
  title: "Run the gate through the documented build path"
  status: "current"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 11", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  supersedes: none
  related: ["B5-1461", "B5-1301", "B5-0967"]
---

# Pattern: the gate is only green through the documented build path

## The failure this records

The B5-1301 close-out attempted its step-8 compile gate via
`cmd.exe /c 'cd /d <abs>\b5ccg && compile.bat'` and hit the 240 s tool ceiling
with no verdict — the task was read-only so nothing could have broken, but the
gate produced UNKNOWN, and UNKNOWN is not green. One task later, the same build
through **`sh compile.sh` under Git Bash** compiled 65 sources in seconds.

The repo had already documented the answer: `compile.bat`'s own B5-0967 note
records that the verified end-to-end build on this platform is
`RUN_TESTS=1 sh compile.sh`, because the test selection lives only in
compile.sh's RUN_TESTS=1 branch (B5-0956 owns the gate list). The cmd.exe route
was never the documented invocation.

## The shape

A build gate is not just a program — it is a program **plus an invocation**.
Shell choice changes path resolution (MSYS vs cmd), working-directory handling,
and in this repo the very gate list that runs. Recording "compile GREEN" without
the invocation hides the one variable that made the difference between a hang
and a pass.

## The check

Record the exact invocation with every gate verdict:
`sh compile.sh` (fast, compile-only) or `RUN_TESTS=1 sh compile.sh` (full
verified build), run from the repo's documented shell (Git Bash on this
platform). If a gate fails or hangs through one route, try the documented route
before declaring the tree red — and if the documented route itself hangs, that
is a finding, not a wall to work around silently.

**Reusable lesson:** record the invocation, not just the verdict — a gate
verdict without its build path is unreproducible, and an unreproducible green
is worth as little as a red.
