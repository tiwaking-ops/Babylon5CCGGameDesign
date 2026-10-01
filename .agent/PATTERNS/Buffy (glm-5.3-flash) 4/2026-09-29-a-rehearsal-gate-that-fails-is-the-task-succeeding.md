---
document:
  title: "A rehearsal gate that fails is the task succeeding"
  task: "B5-1001"
  date: "2026-09-29"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 4", version: "glm-5.3-flash"}
---

# A rehearsal gate that fails is the task succeeding

**One-line lesson:** a repair whose safety proof fails before the write has
produced its deliverable — the proof *is* the deliverable — and the honest
terminal state is BLOCKED with the measured failure shape, never a partial
write, and never a "fix" that guesses the missing bytes.

## Shape of the case

B5-1001 prescribed the B5-0989 guarded C1 inverse on the ledger, gated on a
pre-write rehearsal: *every* marked line (77) must be cleanly reversible with
0 guarded failures. The rehearsal reversed only 5. The dominant residue is a
bare C1 U+009D right after `--` — the orphan tail of a right-double-quote
whose `E2 80` prefix bytes were lost in an earlier pass. Byte 0x9D is valid in
neither UTF-8 nor cp1252: there is no inverse without guessing the intended
character. The row forbade guessing (deep class explicitly out of scope), so
the write was forbidden, and the task closed BLOCKED with the failure shape
measured line by line.

The trap this avoids: the algorithm worked on DECISIONS.md (B5-0989, 957/957)
and "worked on the other file" is exactly the intuition that tempts an agent
into forcing the write and manufacturing silent corruption — or worse,
"repairing" `--\x9d` into some guessed character and filing it as DONE.

## What worked

- Pin the instrument first and name it in the record (explicit UTF-8 decode
  that raises, per the row's own measurement note).
- Rehearse per-line with hard asserts (decodes UTF-8, no residual C1, not a
  no-op, ASCII payload identical) and let the abort be loud.
- Diagnose the failure *shape* before concluding: truncation residue (bare
  0x9D after `--`) is a different defect class than single- or double-round
  mojibake, and the distinction is what makes the BLOCKED verdict evidence
  rather than defeat.
- Close per 00_BOOT step 8: BLOCKED + log excerpt + claim released, zero
  ledger bytes changed, follow-up recommendation seeded in the report only.

## Related records

- B5-0989 (the recipe and the DECISIONS.md fixpoint; its "second finding" is
  the class this row fell into), B5-0979 (the proof pass), B5-1001 (this
  block), and the seed-wave report §3 (the instrument-naming trap).
