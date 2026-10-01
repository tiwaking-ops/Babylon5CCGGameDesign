---
document:
  title: "A reporter must not silently adjudicate, and a policy tool must not silently report"
  status: "Pattern"
  task: "B5-1017"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  last_modified_date: "2026-09-29"
---

# A reporter must not silently adjudicate, and a policy tool must not silently report

**Measured 2026-09-29, Babylon 5 CCG, while reconciling the two liveness
divergence records (B5-1017).**

The same claim file produced two verdicts: the query printed **LIVE (age −554.4
min)**, the runner **declined to offer**. Both were right. The query's job is to
report what the files say — the negative age is a *fact about the file* and the
only usable evidence of the forgery. The runner's job is to apply policy — never
offer on a signal that cannot be trusted. The defect was that both printed a
bare word (`LIVE` vs silence) with nothing naming which mode the reader was in,
so every consumer had to *already know* the designs differed — and the whole
point of the divergence records is that nobody did.

## The rule

When two tools hold a defensible disagreement about the same fact:

1. Do not force convergence unless one of them is *wrong about its own rule*
   (that was B5-0775: a replica that mirrored the original inaccurately).
2. Name the mode on the page: a reporter labels its output as
   reported/arithmetic (`[reported-age:...]`), a policy tool states its policy
   in the message (the runner already did: "UNKNOWN is never LIVE... NOT
   offered").
3. Keep the label out of the machine-readable token so compatibility consumers
   (the crosscheck's leading-token comparison) are untouched.

The forged-future case is the sharpest instance: **a negative age must be
printed, never normalised** — hiding it would destroy the evidence — but the
verdict carrying it must wear its provenance on its face.

## Reusable lesson

A defensible disagreement between two instruments is fixed by labelling which
instrument is speaking, not by converging the verdicts: report what the file
says with its provenance attached, apply policy where the policy lives, and let
a fixture where the forgery prints its own name prove the difference is now
explicit.
