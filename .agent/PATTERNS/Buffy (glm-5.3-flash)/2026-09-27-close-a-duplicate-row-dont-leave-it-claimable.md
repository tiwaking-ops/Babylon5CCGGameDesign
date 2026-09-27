---
document:
  title: "Close a duplicate row through its own cycle — don't leave it claimable"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Close a duplicate row through its own cycle — don't leave it claimable

**Source task:** B5-0711 (duplicate of DONE B5-0691). **Record:** 2026-09-27,
Buffy (glm-5.3-flash).

## The lesson

Two seeding passes filed the same Civil War implementation row minutes apart
under different IDs; one (B5-0691) was completed while the other (B5-0711) sat
OPEN and claimable, its text calling the completed one "invisible". An
unattended run taking B5-0711 at face value would have re-implemented landed
code — engine and model edits over a file set whose CWR section already held
26 passing checks — and the divergent second implementation would have
collided with the first at the next merge. The duplicate was closed
DONE-via-supersession by claiming it, pointing at the landed artifacts, and
releasing the claim — the full cycle, because a row is never closed *around*
the protocol, only through it.

## The transferable rule

When you detect that an OPEN row duplicates work that is already DONE:

1. **Claim it** — the close itself is a writer action on shared state and
   needs the claim's protection and the ownership trail.
2. **Prove the duplication** — cite the DONE row's report, its suite section
   counts, and the gate evidence; a supersession close without evidence is a
   status flip, not a close.
3. **Point the dependents somewhere true** — rows gated on the duplicate must
   be readable as unstalled by the supersession note or their owners will
   re-derive the gate from a false premise.
4. **Release and move on** — do not re-implement, do not "do it slightly
   differently", do not treat the second claim as licence to touch the code.

## Anti-patterns this heads off

- Editing the duplicate's row to say "see B5-XXXX" without claiming or
  closing — an OPEN row with a comment is still offered by the runner.
- Re-implementing "properly this time" — divergence between two landed
  implementations of one design is worse than either implementation.
- Reaping/voiding the duplicate without a supersession pointer — the gate
  chain behind it then stalls on a missing row, the B5-0681 lesson again.

**Filed alongside:** `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0711.md`.
