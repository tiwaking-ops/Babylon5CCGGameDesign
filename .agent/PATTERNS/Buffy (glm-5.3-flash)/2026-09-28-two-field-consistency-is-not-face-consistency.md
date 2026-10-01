---
document:
  title: "A two-field consistency check is not a face-consistency check"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A two-field consistency check is not a face-consistency check

**Task:** B5-0939 (AFTERMATH transcribe-and-diff, 2026-09-28).

**The trap.** The pool's AFTERMATH `subtype` and `triggerCondition` fields agree with
each other on all 117 records — a 100% internal-consistency pass. Read against the
printed faces, they still mismatch 3 of 59 titles (Glory, Wounded, Secondary
Experience). Two fields that agree with each other can jointly disagree with the
primary source; internal consistency is a weaker check than it feels like when both
fields were authored together.

**The useful counterweight found in the same data.** The pool's own text layer
carries deliberate annotations ("Deluxe text change: …") that sometimes *side with the
face against the structured field* (Secondary Experience's annotation says "now
classified as Aftermath - Won" while the `subtype` field still reads
`AFTERMATH_WON_PARTICIPANT`). When a store has an annotation/notes layer, diff the
layers against each other *and* against the source — the annotation layer can tell you
which divergences were authored drift and which are defects.

**Method note that carried the batch.** The dual-preprocessing corroboration discipline
from B5-0935 scales to a full batch: color and grayscale-autocontrast passes agreed
≥0.90 on 58/59 rules bands (mean 0.995). Read the header line *out of the rules-band
crop*, not a guess-band above it — on these cards the subtype line sits a few pixels
below where a "header" crop naturally ends, and all 59 dedicated header bands OCR'd
empty while every rules band began with the header line.

**Rule of thumb:** for every consistency check, ask "which side of this comparison is
the primary source?" — and if the answer is "a third thing the check doesn't touch",
the check can pass while the answer is wrong.
