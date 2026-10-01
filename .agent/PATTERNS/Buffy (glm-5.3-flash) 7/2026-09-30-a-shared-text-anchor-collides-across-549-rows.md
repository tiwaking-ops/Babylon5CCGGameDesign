---
document:
  title: "A shared-text anchor collides across 549 rows — verify the pipe count of your own close-out edit, and re-claim to repair it"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 7", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1127"
---

# Pattern: the post-write gate exists for the edit you were sure of

My B5-1127 close-out used a short row-tail anchor (`…no src or suite edits…`)
that also appears in other rows — and the edit landed the verified/date cells
at the *first* match, mid-row, orphaning the scope suffix: 10 pipes. Caught
one tool-call later by the shipped detector, which is exactly why the gate
exists.

1. **Anchor on text unique to the row** — the task id itself, not a scope
   phrase. Scope texts repeat across rows by construction; ids do not.
2. **The post-write gate must run even when the edit "obviously" worked** —
   the failure here was not a typo but a *plausible-looking* splice into the
   wrong row-position. Eyeballing the diff of the intended hunk shows
   nothing; the detector's count does.
3. **Repair through a re-claim, not an off-claim patch.** The row is mine and
   freshly written, but the discipline is cheap and keeps the record honest:
   release-time verification on the *second* claim, disclosure in the row's
   own verified cell.
4. **Splice counts, not contexts.** Context-based string edits failed twice
   here (byte-variant anchors the edit tool could not match); counting the
   pipes in the row span and asserting the target number (== 7) before
   writing is the only deterministic approach — and the assertion in the
   splice script is itself a gate.
5. **A row repair cycle is not a defect-free run.** Disclose it in the same
   cell it damaged, with the cause (shared-text anchor) and the mechanism
   (re-claim, count-assert splice), so the next session inherits the fix and
   not just the scar.

Reusable lesson: trust the gate you just passed, and distrust the anchor you
did not count — 549 rows share their vocabulary; only the id is theirs alone.
