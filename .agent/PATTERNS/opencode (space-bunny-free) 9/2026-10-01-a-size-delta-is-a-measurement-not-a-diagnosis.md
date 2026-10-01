---
document:
  title: "A size delta is a measurement, not a diagnosis"
  status: "Pattern (advisory only — never canonical)"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 9", version: "space-bunny-free"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_by_llm: {name: "opencode (space-bunny-free) 9", version: "space-bunny-free"}
  last_modified_date: "2026-10-01"
---

# A size delta between a working tree and HEAD is a measurement, not a diagnosis

**Pattern.** A tracked file is shorter in the working tree than at HEAD. The reflex is
to call it truncation and restore it.

**Why it misleads.** Two independent axes move at once: content that was *lost* and
content that was *added*. A byte or line count sums them and cancels the sign.
Measured on 2026-10-01 against `docs/DECISIONS.md`: the tree was 47 KB and 2307 lines
smaller, which read as mass loss. Counting identifiers the encoding could not touch
(`B5-\d{4}`) gave 387 ids at HEAD against 491 in the tree — 371 ids that existed only
in the tree, 267 only at HEAD. It was a **diverged later generation**, and
`git checkout HEAD --` would have destroyed all 371 newer entries without a warning.

**What to measure instead.** Count a token the damage cannot touch, and compare the
*sets* in both directions, not the sizes:

- identifier-shaped tokens (`B5-\d{4}`, not prose — mojibake mangles prose but not ASCII digits)
- file-level distinct counts, since a shared basename across tiers silently collapses
- the count in each direction, printed separately

**Cross-check before believing any of it.** A file that was truncated and never
repaired looks nothing like one that was truncated *and recovered*. The receipts live
in the repo: 175 entries here were headed `restored after the <timestamp> truncation`
by DONE row B5-1481. Grepping the file for its own incident notes reclassifies the
finding in one command, and that is cheaper than a restore.

**Corollary — the same shape, inverted.** This repo already recorded the mirror lesson
in B5-1669: *mention-presence is not restoration-presence*. Presence in the tree is
likewise not restoration of HEAD. Both failures come from treating one weak signal as
the conclusion.

**Sibling pattern.** Report a gate's output only after reading the gate's contract. The
battery header printed `16 non-conforming` against a declared inventory of 15, which
reads as an undeclared regression; it was 15 distinct names across 16 files because a
quarantine file shared a basename with a live one. Codepoint dumps settled it in one
command. A gate's *number* is a claim about its own scope; the scope is in its source.