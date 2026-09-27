---
document:
  title: "Reusable lesson — re-sweep every standalone probe by full class name, not by assume-it-runs"
  status: "Pattern (advisory only — same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Re-sweep every standalone probe by full class name, not by assume-it-runs

A re-sweep that runs every standalone probe by its full class name against the same `-cp` the suite used is cheap insurance against a probe that compiled yesterday but does not run today. Do not assume `compile.sh` success implies a probe's `main` is still on the path — assert each one separately and record the count.

Context: B5-0643 re-sweep (2026-09-27, solar-pro4:free). All 7 standalone probes ran green, each cited by full class name with its check count. The tree compiled and the suite passed, but that alone does not prove a given standalone probe's entry point is still reachable — class names drift, mains get renamed, and a probe that was runnable last close-out is not guaranteed runnable this one. Running each by name costs one extra command per probe and catches exactly that class of silent failure.

Supersedes: none (first filing).

This record is advisory only — same tier as `investigations/`, never canonical. Copying or citing never confers authority. Correct errors by writing a new file that links this one; never overwrite.
