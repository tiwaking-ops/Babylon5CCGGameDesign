---
document:
  title: "Hit-test geometry must be derived from the paint path, not from memory"
  status: "Pattern (advisory; never canonical)"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  last_modified_date: "2026-09-25"
---

# Hit-test geometry must be derived from the paint path

A click handler and the paint handler are two implementations of the same
layout. When they are written separately, they are two sources of truth, and
only one of them fails visibly.

**Observed (B5-0423):** a board panel had a callback surface that looked
finished — mouse listener, selection field, callback setter, highlight calls
at five sites, and a `clearSelection` — while `handleClick` and
`drawSelectionMark` were *called* but never *defined*. The file did not
compile, yet the surrounding code read as though the feature had shipped. A
caller existing is not evidence that its callee does.

**How to apply:**

1. Write the hit-test by copying the paint geometry into a table — size,
   origin, step per row — not from recollection of what the layout "looks
   like". A row drawn at a hand-tuned offset is exactly the row a
   memory-written hit-test misses.
2. When a control is reported "always dark", separate the two failure modes
   before editing: is the *callback* never wired, or is the *plumbing* absent?
   They look identical from the outside and have opposite fixes.
3. Enablement predicates that duplicate an engine check will drift. Point the
   enablement at the same authority the action handler already gates on,
   rather than patching the copy — a duplicated partial predicate is a
   standing bug, and here it had already disabled a legal move.
4. Resetting a combo box is not always `setSelectedIndex(-1)`. If any refresh
   path parses `getSelectedItem()` without a null guard, `-1` converts a stale
   value into an NPE. Reset to a real, non-actionable item instead.
