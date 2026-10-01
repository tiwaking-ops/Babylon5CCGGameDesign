---
document:
  title: "A rule is only what it covered"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A rule is only what it covered

**Task:** B5-0959 (attribution ancestry audit, 2026-09-28).

**The trap.** The B5-0948 correction discovered that an attribution without an
ancestry check is a guess, wrote the rule ("prove a commit is an ancestor of the ref
being discussed against both HEAD and origin/main before calling it an introduction"),
and applied it — to the two commits whose failure had prompted it, plus one more. The
same artifact set still contained two *other* attributed introductions
(`d1d0c6ff`, `59c4f251`) with no ancestry result beside them anywhere. The rule was
real; its coverage was the size of the incident, not the size of the problem.

**The pattern behind the miss.** A rule written mid-audit is naturally scoped by the
example that provoked it. But a correction that cites a rule implicitly re-opens every
artifact the rule could judge — including its own source artifact and its own prior
verdicts. The audit surface after a correction is the union of all attributions the
rule names, not the delta the correction touched.

**The check that costs one command per item.** Enumerate the rule's full application
set first (here: an exhaustive hex-string sweep of the two artifacts — five and six
commits, small enough to check exhaustively rather than sampled), then run the rule
over all of it. The battery took six `git merge-base --is-ancestor` pairs.

**Two useful side-effects to expect.** (1) Most previously-unverified attributions
will *survive* — that is not a null result: it converts "right by luck" into "right by
verification", and the recorded chain is the deliverable. (2) Where a number is cited
without derivation (B5-0921's object-store figure), record the gap rather than
reconstructing a method you cannot reproduce — a re-measured byte-identical count
(68,117 / 67,258 / 201,791,663, third consecutive pass) is worth more than a plausible
story about a stale one.
