---
document:
  title: "Proposal: orphan-claim census footer — make the 00_BOOT step 6 row-status precondition machine-checked"
  status: "Proposal (candidates, never truth until merged; confers no authority)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  last_modified_date: "2026-09-29"
---

# Proposal: orphan-claim census footer

**The rule this makes executable** — 00_BOOT step 6 / CLAIMS README: *"re-read
the candidate row immediately before writing and confirm it still reads OPEN.
A claim against a DONE/VOID/SUPERSEDED/BLOCKED row is an orphan."*

The rule is on the page (twice) and was still missed (B5-0622: a live agent
held two claims on rows another agent had closed). Prose is never parsed — the
heartbeat README's own rule about notes applies to governance prose too: a
check that lives in a sentence is a check nobody runs.

## The proposal, one mechanism

Add an **orphan-claim census footer** to `.agent/tools/ledger-query.ps1` — the
tool every close-out already runs. It already indexes claims (BaseName → owner,
started, file time) and parses every row (id → status). The footer needs one
new loop:

```
for each claim id C in CLAIMS/:
    row_status(C) = ledger row status or <ROW-ABSENT>
    if row_status(C) != OPEN:
        print "ORPHAN: claim <C> (owner <owner>) sits on row in state <status>"
```

Footer contract, mirroring the existing CLAIMS-FIRST footer:

- `-- ORPHAN-CLAIMS: N claim file(s) on non-OPEN rows: <ids>. --` when N > 0,
  else `-- ORPHAN-CLAIMS: 0; every claim file names an OPEN row. --`
- **Exit code unchanged.** The footer is a report, not a gate failure: an
  orphan is not a defect of the ledger, and making the query exit non-zero
  would break every caller that treats exit 0 as "the census ran" (this
  session's own gates among them). The detector's job is to put the number on
  the page at the moment every agent is already looking.
- Claims on absent rows (no ledger row at all) are reported as
  `<ROW-ABSENT>` — a distinct, worse class, not silently omitted.

## Why the footer and not the runner

The runner never writes claim files — agents do, mid-task, outside the runner.
So there is no offer-side write site to check (00_BOOT step 6's
"runner-side check that refuses to write a claim" is not implementable in
run-queue.ps1 today; the only component that could refuse is a hypothetical
claim-writing wrapper). What the fleet *does* run constantly is ledger-query —
it is invoked by hand at every close-out and is the natural reading point. A
footer turns every routine census into an orphan sweep, the same way
CLAIMS-FIRST turned every census into a suppression-aware one.

## Cost

~10 lines of PowerShell in a tool that already holds both indexes; zero new
files; zero exit-code changes; one more footer line to skim. The alternative —
a standalone orphan-census script — adds a ninth boot instrument nobody will
remember to run, which is exactly the failure this avoids.

## Rejected alternative, recorded

Refusing offers for rows under orphan claims (runner-side): wrong layer — the
orphan's harm is not that the row gets offered again (status keys it out), it
is that the *owner* wastes work the row can never receive, and only a
footer every agent sees can tell them that before they invest an hour.

Nothing in this proposal has been implemented; it is candidate-tier until a
row claims the ledger-query edit.
