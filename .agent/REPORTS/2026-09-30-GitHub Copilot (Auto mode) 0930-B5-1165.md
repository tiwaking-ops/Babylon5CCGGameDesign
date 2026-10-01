---
author_llm: GitHub Copilot (Auto mode)
task: B5-1165
agent_id: "GitHub Copilot (Auto mode) 0930"
utc: "2026-09-30T07:09:29Z"
---

# B5-1165 report

## Result

DONE as a read-only UI and engine audit. No source, card data, or follow-up fix
row was created.

`MainWindow.refreshCostPreview` uses `RulesEngine.recruitCost` for the sponsor
preview at line 1334. The reachable recruit action charges
`RulesEngine.sponsorCost` at `GameController.java:231`. B5-1109's close-out
establishes that `sponsorCost` differs only when a waiver is active, and the
current card/effect data has no `FREE_SPONSOR` waiver entries. Therefore the
preview and charge agree for the current pool.

## Probe receipt

The Java 6 scratch probe passed all assertions:

- `de_char_narn_agent`: affordable at cost 3; preview equaled charge and
  `canRecruit` accepted it with the starting pool.
- `de_char_dunar`: unaffordable at cost 11; preview equaled charge and
  `canRecruit` rejected it.
- Probe result: `PROBE-COMPLETE`, exit 0.
- `b5ccg\compile.bat`: prior unchanged-tree gate exit 0.
- `javac -version`: `1.8.0_292`.

The UI preview is correct for the current data. A future `FREE_SPONSOR` card
would require the preview to use `sponsorCost` rather than `recruitCost`, but
that condition is not present today and was not seeded as a fix here.

Reusable lesson: different helper names do not prove different behavior;
compare their composed inputs and special-case state before filing a fix.
