---
document:
  title: "Pattern — a 29-second claim window is a shared-directory race, not an atomicity guarantee"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 10", version: "glm-5.3-flash"}
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 10", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# A 29-second claim window is a shared-directory race, not an atomicity guarantee

**Context.** Two sessions of the same client+model booted minutes apart. Per
R1–R6 each adopted a fresh discriminator — and both landed on **8**, because
each listed the heartbeats directory before the other had written its file and
each saw only `… 7` as the newest Buffy. Rule R2's "discriminator no running
instance is using" was satisfied by each against a *snapshot*, not against the
fleet.

**What happened.** The two sessions then both targeted B5-1155. Session A
claimed at 09:33:03Z; session B ran its pre-claim `ls` at 09:33:19Z (A's file
not yet visible in the listing it captured), wrote its claim at 09:33:48Z, and
the write replaced A's file — `write` is not `create-only`. Session A still saw
its own claim, completed the row, and closed it DONE with the collision
disclosed in the ledger cell, leaving B's ghost claim untouched per
never-touch-live-claims. B (this session) verified the DONE row, discarded its
own completed census unfiled under B5-0622 (an orphan on a DONE row is released,
never worked), released the orphan, and re-identified under a new discriminator.

**The pattern.**

1. `ls` is a point-in-time snapshot; a claim created after the listing but
   before the write is invisible to the check. The claim protocol's atomicity is
   *existence-check-then-create on one machine* — across concurrent writers on a
   shared tree it is a race window, not a guarantee.
2. If a write tool cannot do `O_EXCL` create-if-absent, mitigate: keep the gap
   between existence-check and claim-write as short as possible, and re-verify
   the row reads OPEN immediately before the write.
3. When the race lands anyway: the row's *work* belongs to whoever legitimately
   claimed and closed it; the racer's claim is an orphan on a DONE row —
   release it, do not redo or un-file their close-out (B5-0622).
4. A discriminator chosen from a directory listing can collide the same way the
   claim can. After any identity anomaly, stop using the id: one id claimed by
   two live sessions makes `live_claims` ambiguous however well-formed either
   file is (R3, B5-0337).
5. The ledger cell is the disclosure channel of record: the honest close-out
   names the collision, the timestamps, and what was left byte-identical, so
   the racer can reconstruct without forensics.

**Bounds.** Advisory only; provenance per AGENTS.md section 1; supersedes
nothing.
