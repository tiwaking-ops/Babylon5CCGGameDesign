---
document:
  title: "B5-1019 — the standing verification battery: one command, proven green and red"
  status: "DONE 2026-09-30"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  claimed_at: "2026-09-30T04:52:33Z"
  battery_proven: "GREEN exit 0 at 2026-09-30T05:06:41Z; RED exit 1 via -SelfTest"
---

# B5-1019 — the standing self-verification battery

## The deliverable

[run-verification-battery.ps1](../../.agent/tools/run-verification-battery.ps1).
One command assembles the six cold-boot instruments in boot order (plus
`census-crosscheck` as a declared bonus), prints each instrument's
**DESIGNED vs OBSERVED vs VERDICT vs DECLARED-RED** row with its own detail
line, and returns one verdict: exit **0** green, **1** red, **2** layout
broken — so a battery that could not run never reports as a pass (the
run-dup-census 0/1/2 lesson applied to the battery itself).

## The expected-red contract (the row's central requirement)

Declared in the script, with the measured reason printed on every run:

- `validate-heartbeats`: designed 0, **standing observed 1** — the
  out-of-enum tombstone `Cline (space-bunny) b5-0941.json` (state
  'released') plus the legacy `solar-pro4` / `freebuff-NN` lookalike class.
  All foreign, none battery-owned.
- Every instrument not listed expects observed 0.
- An observed match on a declared standing state reads **GREEN with the
  reason still printed** — declared red is *reproduced*, never silently
  ignored. An undeclared nonzero reads RED. This is the difference between
  an expected-red list and a red-suppression list.

## Proofs

- **Main path GREEN, exit 0** (2026-09-30T05:06:41Z): all seven checks at
  designed-or-declared codes; the compile gate genuinely executed (the
  `javac -source 1.6` bootstrap warnings in stderr are the real build's
  signature — a stub would print nothing); the census line reported the same
  13 claimable rows the boot census saw.
- **Self-test RED, exit 1**: `-SelfTest` builds a synthetic duplicate-ID
  ledger in `%TEMP%` and lets the *real* `run-dup-census.ps1` judge it
  through its own `-LedgerPath` parameter — observed 1, battery exits 1,
  fixture cleaned up, zero live files touched. A battery that cannot go red
  is an ornament.

## Bring-up defects, all mine, none in any instrument

1. **The compile probe cwd trap (measured twice).** `Set-Location` is a
   PowerShell provider move that does not touch the process cwd; setting
   `[Environment]::CurrentDirectory` fixes .NET but `cmd` still inherited
   PowerShell's own location. Two cwd mechanisms, both ignored by the child.
   Fix: invoke `compile.bat` by **absolute path** — it self-locates via its
   `%~dp0` line, so no cwd semantics matter at all.
2. The `javac` display picked the JVM's `Picked up JAVA_TOOL_OPTIONS`
   banner instead of the version line — display fixed by selecting the line
   matching `javac `, verdict logic unchanged (it matched `1.8.` on the
   whole output, so the gate itself was never wrong).
3. The first draft was discarded uncommitted after self-review found a
   hallucinated module include and a dead self-test stub. Supersede-never-
   rewrite governs REPORTS and PATTERNS; a same-claim rewrite of a file the
   claim created minutes earlier is bring-up, not history rewriting.

## Bounds respected

No instrument's exit code, threshold, TTL, or verdict changed. The battery
reports and does not act — no repair, no reap, no self-healing. No src or
card-data edits, no foreign artifact touched, no commit, no push.

**Reusable lesson:** a gate without a red twin is a claim, not a gate — the
self-test is what converts this battery from a convenience script into an
instrument; and the second-best thing it did was catch its own author
declaring a tree red before the tree had been measured at all.
