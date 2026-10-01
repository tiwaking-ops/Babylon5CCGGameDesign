---
document:
  title: "A pass rate hides the three values that matter"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
  task: "B5-1037"
---

# A pass rate hides the three values that matter

Traces to: B5-1037 (participation / conflictType executability verification).
The row prescribed the rule; this record is the receipt that it pays.

**One-line lesson:** an enum-from-string audit must emit the unrecognised-value
list, never a recognition percentage — the percentage compresses away exactly
the information the next action needs, and a perfect score still needs the
failure-mode statement to be useful.

## The shape

- "94% recognised" and "unrecognised: none" are the same verdict at different
  resolutions; only one of them tells a human what to fix. The row banned the
  pass rate for this reason (B5-1037's own text) and the ban generalises:
  pass rates also hide *which* legitimate values are rare (PSI: 4 of 108) and
  therefore which value a future typo most resembles.
- A second number hid beside the first: the row's headline 106 carriers vs its
  own breakdown summing to 108. A breakdown that sums is the check a headline
  never gets; always re-add the components.
- Executability is a two-sided claim: the data value must map, *and* the
  mapped predicate must admit a real population. conf_limited_strike's
  fleetSubtypes filter would have read "executable" even with zero matching
  fleets; the fleetClass census (22 of 80 match) is what made it a verified
  executable filter rather than a decorative one.

## What worked

- Enumerate every distinct value with counts, compare against the enum's
  constant set, and print the unrecognised list even when empty.
- Trace each value to its mapping line and its consumer, then census the
  population the predicate admits (fleetClass coverage) — mapping plus
  admitting population is executability.
- State the failure mode per side: participation degrades loudly to open
  (card kept), conflictType's unvalidated `valueOf` drops the card at load
  (silent to the player). Two fields, same "unvalidated" label, completely
  different blast radius — the label alone would have merged them.

**Reusable lesson:** verify a from-string field by listing its values against
the consuming enum and naming each failure mode — a pass rate compresses the
two facts an auditor needs into a number that answers neither.
