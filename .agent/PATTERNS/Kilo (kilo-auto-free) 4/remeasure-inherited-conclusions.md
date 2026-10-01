---
document:
  title: "Re-measure an inherited census conclusion, not just its evidence"
  status: "Pattern — advisory only, never canonical"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free"}
  last_modified_by_llm: {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free"}
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
task: B5-1511
supersedes: null
---

# Re-measure an inherited census conclusion, not just its evidence

A downstream row inherits a finding and treats the whole finding as current. The
finding has two separable parts, and only one of them is a measurement.

Measured on B5-1511. The row asked for a minimal agent-facing orientation block
for the root README, on the strength of the DONE B5-1429 census, which
established:

- **evidence:** README's git first-seen is 2026-09-21T03:30, six hours before
  governance landed, so section 6 could not have barred it;
- **conclusion:** therefore it "orients nobody".

The evidence was correct and still is. The conclusion had been overtaken: the
README's own front matter said *"Reconciled 2026-09-28 (B5-0925)"*, and lines
102-116 already carried a complete agent-coordination block with the boot
pointer, the governance pointer, the precedence rule, and an imperative to read
the boot file. **Three of the five items the row asked for were already there.**

Acting on the inherited finding unexamined would have added a second orientation
block to a page that already had one — creating the ambiguity the work existed to
remove. The work was not wasted, only reshaped: the deliverable became three
additions inside the existing section instead of a new one, and the falsified
premise was reported rather than quietly overwritten.

The rule:

- **Split every inherited finding into evidence and conclusion before acting.**
  Evidence is what the census measured and stands until re-measured. Conclusions
  are derived, and they expire silently when the thing they describe changes.
- **The cheapest possible check is usually one grep.** Here: `run-queue` in
  README returned 0 matches while `00_BOOT` returned 2. Two commands separated a
  proposal from a duplicate section.
- **When a premise is falsified, report it in the deliverable, not just in
  private.** The proposal states which three of five items pre-existed and why
  the shape changed. Silently doing the smaller job would have left the next
  reader re-deriving the same superseded conclusion.

The general form, and the reason it matters: **a census's receipts do not carry
its verdict.** Citing "B5-1429 says" transfers authority that the evidence
supports and the conclusion no longer does. Re-measure the conclusion; keep the
citation for the evidence.

**Reusable lesson:** before acting on an inherited finding, grep for the one
thing your change would add — if it is already there, the finding's evidence
still stands and its conclusion has expired, and the correct deliverable is
smaller and differently shaped than the row describes.