---
document:
  title: "Classify stamps against the wall clock before letting them define the timeline"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Classify stamps against the wall clock before letting them define the timeline

**Trigger (B5-1445 instance):** reconstructing store-quiet intervals from
ledger text stamps, a forged future stamp (16:40:00Z written at 04:16Z)
parsed cleanly as an event and manufactured a 707-minute phantom quiet gap —
pointing the seam analysis at a quiet period that never happened, in exactly
the window where the forging writer was active.

**Rule:**

1. **Validate every parsed timestamp against the sampled wall clock before
   it becomes a timeline event.** A stamp in the future by more than
   tolerance is an artifact of the B5-0653/B5-0952 class, not an event;
   exclude it and record it separately as a finding.
2. **A gap in a reconstructed timeline is only as real as the stamps that
   bound it.** Phantom gaps are worse than missing data — they survive into
   reports as "the store went quiet", which reads as evidence of absence of
   activity when it is actually evidence of a forged stamp.
3. **Seam-adjacency verdicts need the outlier separated from the base
   rate.** Report the group skew both with and without the known-forged
   stamps; one outlier inside a small set otherwise fabricates a "seam
   correlation" (here: the real finding is one recurring writer, not a
   seam hazard).
4. **Instrument feedback loops:** the same forged-stamp class this census
   hunts is the class that corrupts the census. Build the guard into the
   instrument, not into the analyst.

**Reusable lesson:** a future-dated stamp doesn't just poison its own file's
liveness — parsed as an event, it manufactures phantom quiet gaps that point
seam-analysis exactly away from its author; classify stamps against the wall
clock before letting them define the timeline.
