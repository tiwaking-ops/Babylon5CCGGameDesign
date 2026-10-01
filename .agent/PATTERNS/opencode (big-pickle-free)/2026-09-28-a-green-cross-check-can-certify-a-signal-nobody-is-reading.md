---
document:
  title: "A green cross-check can certify a signal nobody is reading"
  status: "Pattern"
provenance:
  author_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  assessor_llm:
    - {name: "unknown", version: "unknown", note: "no independent assessment recorded"}
  last_modified_by_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: B5-0771
---

# A green cross-check can certify a signal nobody is reading

**Reusable lesson.** A cross-check that cannot see a dead signal will report
CONSISTENT about it. A green agreement between two tools proves only that they are
wrong in the same way, so when a signal is added to one tool, prove it on a fixture
where the OTHER tool's copy is the one still missing, and treat a fixture that stays
green as the suspicious outcome, not the reassuring one.

**Where it came from.** B5-0771. The B5-0660 report-mtime signal was dead in two of
three tools — both resolved the reports directory to `<repo>/REPORTS` instead of
`<repo>/.agent/REPORTS`, and a `Test-Path` guard swallowed the miss without warning.
Before the fix the cross-check printed `CONSISTENT` on a fixture where the correct
verdict was `LIVE`, because the dead path had aligned two wrong two-signal readings.
After the fix the same fixture printed `DIVERGENT` and named a second, independent
defect in the cross-check's own emulation of the other tool. The tool built to catch
the class had been the blind one.

**Generalises to.** Any duplicated rule — a validator that re-implements a parser, a
cross-check that re-implements a liveness rule, a census wrapper that re-implements a
row regex. Duplication makes agreement cheap and coverage invisible. The only thing
that measures whether a signal is *read* is a fixture constructed so the correct and
incorrect readings **differ**, and if the tool under test reports agreement on that
fixture, the fixture found the tool's blind spot, not the tool's health.
