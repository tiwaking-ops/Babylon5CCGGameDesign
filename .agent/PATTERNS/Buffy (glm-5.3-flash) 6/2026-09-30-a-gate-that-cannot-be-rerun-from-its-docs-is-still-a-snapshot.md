---
document:
  title: "A standing gate is a documented command, not a wired-in script — and the run instruction is part of the gate"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 6", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1012"
---

# Pattern: the gate is the command + the standing state, not the wiring

Context: B5-1012 asked the 2026-09-28 Java 6 construct census (B5-0960) to
become "standing" rather than a point-in-time report. The temptation is to wire
the check into `compile.bat` or add a new `.agent/tools/` script — that is how
gates accrete until nobody can name which instrument produced which finding.

What worked instead:

1. **Reuse the existing script byte-for-byte and cite it.** The census script
   already exists as provenance (`tmp-scans/b50960/census.py`). A proposal that
   says "run *this* file, unmodified, with *this* command" is standing; a copy
   in `tools/` is a fork that rots independently of its source.
2. **Publish the standing-state table, not just the tool.** Expected values
   (all families `code-lines 0`; arrow prose ~50 and allowed to grow;
   getOrDefault raw 14 but qualified `.getOrDefault(` = 0) are what let the
   next session judge a fresh run in seconds instead of re-deriving the
   methodology. A gate without an expected-state table is a report generator.
3. **State the authority split explicitly.** javac is decisive on syntax; the
   census owns the record and the API-level dimension the compiler gate cannot
   see (`Map.getOrDefault` compiles fine at `-source 6`). Any second opinion
   that contradicts javac on syntax is a defect in the second opinion.
4. **The run instruction is part of the gate.** The first re-run crashed with
   `UnicodeEncodeError` under the default cp1252 console because the script
   echoes raw source lines carrying box-drawing glyphs. `PYTHONIOENCODING=utf-8`
   is load-bearing. If the canonical command needs an environment variable to
   work, the environment variable belongs in the governance text — otherwise
   the first session that runs it bare files "tool broken" or, worse, quietly
   stops running it.

Traps this avoids (both already named in this repo's history): the snapshot
that rots silently (B5-1012's premise), and the detector tuned until it stops
detecting (the B5-1020 false-positive hazard — report MORE, never fewer).

Reusable lesson: a gate that cannot be re-run from its documentation alone is
still a snapshot, whatever directory its script lives in.
