---
document:
  title: "run-queue.ps1 census must be the boot-mandated discovery path"
  status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
---

## When to apply

Any time a shared tooling fix (runner, census, query, harness) is made available to all agents via a repo file, and the boot protocol does not yet mandate its use.

## What to do

1. Pair the code/tool fix with a boot-step amendment that tells agents to use the tool, placed before the claim step so the census precedes claiming.
2. Give the exact invocation in the boot step (powershell -File ... -DryRun, bash equivalent, etc.) — a bare "use the runner" with no command is just another hand-rolled-census invitation.
3. State the manual fallback (ripgrep one-liner, etc.) for when the tool is unavailable.
4. State the hard rules the tool now enforces (mention≠claim, absent=UNKNOWN, etc.) so agents following the boot step adopt the same discipline.
5. Renumber every later step when inserting a new numbered step — a duplicate step number is invisible to quick scans and will misdirect agents who follow the later one.
6. Log the boot-step addition in `docs/DECISIONS.md` per AGENTS §4.

## What NOT to do

- Do not change claim/lane/TTL rules in the same pass unless that is the explicit scope — the boot-step amendment documents the entry point, it does not redefine the rules.
- Do not leave the boot step as prose without an exact command — agents need copy-pasteable invocation, not a description.
- Do not skip the fallback — a tool-dependent boot step that breaks when the tool is missing leaves agents with no census path.

## Why

The B5-0613 fix (tolerant row regex in run-queue.ps1) was correct and high-value, but without a boot-step amendment every agent that followed `00_BOOT.md` would hand-roll its own census, re-introducing the double-pipe visibility defect and the stop-condition defect. The fix only reaches all models when the boot protocol tells them to use the fixed tool. The B5-0614 boot-step amendment closes that gap.
