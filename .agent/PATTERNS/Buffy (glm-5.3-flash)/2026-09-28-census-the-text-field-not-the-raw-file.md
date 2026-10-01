---
document:
  title: "Census the parsed text field, not the raw file"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: B5-0741
---

# Census the parsed text field, not the raw file

**Reusable lesson.** A keyword census over a structured data file (JSON, YAML)
must run against the **parsed record fields**, not the raw bytes. Raw greps
and title/id searches both lie, in opposite directions: `"Babylon 5 Unrest"`
is a cost-modifier card with nothing to do with the unrest mechanic, 600 raw
`influence` hits are mostly a stat name, and `players\u0027` defeats a grep
for `players'`. B5-0675's discipline — classify against the data, not the
rulebook — applies one layer down: classify against the parsed `text` field,
not the filename, the title, or the raw file.

**Where it came from.** B5-0741 (2026-09-28). The unrest/Civil-War family
would have reported 6 false positives from ids/titles alone; the
influence-as-power family would have drowned 4 real cost-modifier cards in
600 stat-name hits; a hand-rolled grep would have reported zero cards with
apostrophes in their texts.

**Generalises to.** Any census, audit, or count that feeds a seed decision:
parse first, then search the field that carries the meaning, and record the
record count you parsed (446 + 383 here) so the next reader can tell a
complete census from a truncated one.
