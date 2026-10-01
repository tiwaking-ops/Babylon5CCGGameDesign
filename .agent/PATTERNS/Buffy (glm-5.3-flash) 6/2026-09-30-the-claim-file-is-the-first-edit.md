---
document:
  title: "The claim file is the first edit of a task, not a close-out formality — a session that forgets it has no lock and no truthful receipt"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 6", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1029"
---

# Pattern: write the claim before the first measurement, not after the last report

During B5-1029 the working session read the queue, decided to claim, verified
the row unclaimed — and then went straight into the measurements. The claim
file was never created. The close-out was drafted from a mental model in which
the claim existed, producing a report paragraph asserting a claim file and a
release that never happened: a **fabricated receipt in my own report**, caught
only because `rm` failed with ENOENT at release time.

Rules this pattern fixes:

1. **The claim file is step one of the work, not a closing formality.** If the
   plan is "verify, then claim, then edit," the verification gap is exactly
   where the claim gets lost — do the row re-read AND the claim write in the
   same breath, before any measurement command runs.
2. **A close-out drafted from memory will assert what should have happened.**
   Every receipt line ("claim released", "gates green") must be written from
   the tool output that proved it, or not at all. The false "released at
   close-out" line here was not a lie — it was memory standing in for evidence,
   which is worse, because it feels true.
3. **`rm <claim>.json` failing is the detector.** The release step doubles as
   the existence check: if the file you are releasing does not exist, the whole
   session's claim discipline just failed loudly. Do not proceed to the next
   task until the disclosure is written into the report, the ledger row, and
   DECISIONS.
4. **Mitigation facts go on the record, not in your head.** What made this miss
   survivable: the row was verified UNCLAIMED at start and close by the shared
   detector, and the work was 100% read-only, so no writer collision and no
   artifact mutation were possible. That is the difference between a form
   violation and a hazard — and it is only knowable if it is written down.
5. **Retraction is a legitimate edit of your own fresh artifact.** A same-
   session false statement in your own report is corrected in place, plainly
   marked as a retraction; leaving it would poison every later reader, and
   "never rewrite history" does not mean "leave my own fresh fabrication in
   place."

Reusable lesson: an agent that works without its claim file is invisible to
the fleet AND to itself — write the claim first, and let `rm` be the thing
that proves it existed.
