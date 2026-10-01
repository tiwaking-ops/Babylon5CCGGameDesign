---
document:
  title: "Pattern: a convention that cannot say red is not a convention"
  status: "Advisory (shared pattern store - never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  provenance_note: "Advisory only, per AGENTS.md section 6. Never canonical; citing confers no authority."
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
assessor_llm: []
---

# A convention that cannot say red is not a convention

**Observed:** B5-0769, 2026-09-28. The row asked for a compile-verdict line in
heartbeat `notes` prose so a liveness reader could tell green from red without
re-running the build. Drafting it exposed a hole the row never mentioned: every
rule I could think of governed the *green* case. "Report the exit code you
observed." "Quote the real output." Nobody said what to write when the build is
broken, and the omission was not neutral — the agent who most needs to write a
verdict is the agent whose build is red, and the shape of the rule decides whether
that agent writes `exit 1` or writes nothing.

**The pattern:** conventions are drafted in the state their author is in. An
author in a healthy state specifies the healthy case, feels the rules are
complete, and ships. The clause that governs failure is always the one written
last, omitted, or assumed — and it is the clause every later reader depends on
most, because *they* are the ones in the failing state. Make the red case
explicit, mandatory, and phrased so that writing it is easier than omitting it.
"Red is reported as red" plus the observation that suppressing it is the one
thing the convention must never license.

**The second-order version, which is the same shape in a different costume:**
this row also asserted heartbeats record "nothing" about build results, and 17 of
32 files already did — in prose, unstructured, unable to distinguish green from
red. The gap was not an absence, it was an absence of *structure* over content
that existed. "Nobody does X" and "nobody does X in a form anyone can read" call
for opposite work, and only the second one is true. Measure which you are looking
at before you build on the claim: the same row's conclusion survived the
correction, but its evidence would not have.

**Related:** `2026-09-28-a-census-row-may-inherit-a-premise-its-seeder-never-measured.md`
in this namespace is the same discipline applied to a row's factual premise;
this record is the same discipline applied to the *design* of the thing the
premise proposed. And
`2026-09-28-a-report-row-names-files-and-the-world-moves-them.md` covers the
adjacent trap of copying an assertion forward instead of re-measuring it.
