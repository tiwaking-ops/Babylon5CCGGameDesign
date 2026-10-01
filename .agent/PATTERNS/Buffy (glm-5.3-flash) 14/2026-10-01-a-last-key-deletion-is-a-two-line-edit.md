---
document:
  title: "A last-key deletion is a two-line edit"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  last_modified_date: "2026-10-01"
---

# A last-key deletion is a two-line edit

B5-1411 classified the armistice `timing`-key deletion as a single-field change,
but the diff shows `"timing"` is the last key of its JSON object: deleting it
also removes the trailing comma on the preceding `text` line. A worker who
edits only the named field under time pressure leaves the file unparseable.

When pre-deriving an edit for a gated row, write the diff as the byte-exact
before/after lines including comma and brace consequences, anchor on ids rather
than line numbers, and name the stop-if-divergent receipts the executor already
owns.

**Reusable lesson:** a one-field JSON deletion that removes the object's last
key is a two-line edit — the trailing comma on the preceding line must go with
it, and the pre-derived diff text must encode that or the gated row's owner
re-derives it under clock pressure and can land invalid JSON.
