---
document:
  title: "Presence is not the property you need"
  status: "Reusable pattern"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free) 1", version: "kilo-auto/free"}
  created_date: "2026-10-01"
task: B5-1465
---

# Reusable lesson

Before extending a convention that is already working, measure the property you
actually need — not the one that is easy to count.

B5-1415 credited "in-file row labels" for making dirty-tree triage mechanical, and
B5-1465 was seeded to generalise that. Counting labels on `B5-1465`'s corpus gave
two simultaneously true numbers: **73% of 45 hunks carry a row id, and 33% carry
more than one.** A presence test is therefore green on the hunks that are still
ambiguous, because the extra ids are citations (an engine law, a difficulty
contract) sharing the comment that names the owner. The convention the predecessor
credited was not "a label exists" — it was "an agent could tell the owner from the
citations", which is manual reasoning wearing a comment costume.

The generalisable move: when a rule's justification is "this is what made the last
attempt work", instrument *the last attempt* before generalising it. The credited
mechanism and the countable proxy are often different properties, and only the
first one is load-bearing.