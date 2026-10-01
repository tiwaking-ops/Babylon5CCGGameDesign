---
document:
  title: "A gate naming a missing artifact is red by construction — execute the stand-down, name the gap"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# A gate naming a missing artifact is red by construction

**Trigger (B5-1493 instance):** a row gated its execution on "a
human-ratified destination pointer exists in the ledger or docs". No such
pointer existed — the row's own premise included the unblocking condition,
so the correct execution was the BLOCKED stand-down the row itself
prescribed, not a creative reading of "propose" as "ratify".

**Rule:**

1. **Verify the gate by searching for the artifact the gate names, not by
   re-arguing the gate.** Ledger grep plus docs grep; if the named artifact
   class (ratification, approval, pointer) has zero hits, the gate is red by
   construction and no amount of queue pressure changes that (the
   B5-1167/B5-1145 precedent line).
2. **A gate-red row with a prescribed stand-down is a complete execution**:
   claim, verify, flip to BLOCKED with the gap named, release, report. The
   deliverable is the named pointer gap — precise enough that the human can
   unblock in one sentence.
3. **Run no part of the gated work "while you are here".** Half of a
   gated pair (moving one file, drafting the other's destination) converts a
   clean stand-down into an unsanctioned scope expansion.
4. **Disclose the skipped compile gate explicitly** when nothing changed:
   a gate that never ran must not read as one that passed (B5-0785 lesson),
   and citing the gate-red precedent (B5-1145) makes the skip deliberate.

**Reusable lesson:** a gate that requires an artifact which does not exist is
red by construction — the cheapest execution of such a row is the stand-down
itself, and the pointer gap must be named precisely enough that the human
can close it in one sentence.
