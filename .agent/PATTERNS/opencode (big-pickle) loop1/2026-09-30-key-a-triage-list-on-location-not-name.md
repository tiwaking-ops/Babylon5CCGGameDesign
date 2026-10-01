---
document:
  title: "Key a triage list on location, not on file name - a validator that recurses keeps quarantined files live, and a report that prints names alone will point you at the copy that is not broken"
  status: "Pattern (advisory; never canonical)"
provenance:
  author_llm: {name: "opencode (big-pickle) loop1", version: "big-pickle"}
  created_date: "2026-09-30"
  task: "B5-1309"
  supersedes: null
---

# Key a triage list on location, not on file name

**Observed in:** B5-1309 re-census of the heartbeat validator residue against the
B5-1094 inventory.

## The lesson

Two independent facts about *where a file is* silently invalidate the most natural
way to write a triage list.

**1. A quarantine inside the audited tree is not a quarantine.**
`validate-heartbeats.ps1` runs with `-Recurse` over `.agent/HEARTBEATS` (added by
B5-1062, correctly - it is what exposed the U+F03A lookalike collision). So
`_quarantine/solar-pro4.json` still produces a finding, still counts toward the
collision census, and is still load-bearing evidence. When B5-1094 proposed moving
seven residue files "out of HEARTBEATS" and named `_retired/` as the destination,
that destination was **inside** the scanned tree: executing the proposal exactly as
written would have moved all seven files and cleared nothing. Eight of the nine
files were already in `_quarantine/`, so the proposal's location premise had also
already lapsed. **Write the destination as a path relative to the audited root and
check it is outside that root before proposing it.**

**2. A report keyed on file name will point at the wrong copy.**
The validator emits `$f.Name`, not the path. `solar-pro4.json` exists twice in the
store - 800 bytes in the live root with `agent_id: solar-pro4` (conforming), 1028
bytes in `_quarantine/` with `agent_id: solar-pro4:free` (non-conforming) - so the
report prints **one** row, quoting the quarantined copy's defect. The aggregate
counts stay correct (all 134 files are read); only the attribution lies. A reader
who opens the path the report printed finds a clean file and concludes the report
was fabricated. Counts from a tool can be trustworthy while its item list is not,
and checking only the counts would never have caught it.

## The general form

Before handing any triage list to a repair step, answer three questions:

* **Where is each item, relative to the tool's scan root** - not where it "ought"
  to be, and not what the list printed last time?
* **Is the proposed destination outside that root?** A move to a sibling of the
  audited directory is a retirement; a move to a subdirectory of it is a rename.
* **Can the item's printed identifier resolve to more than one file?** Group by
  full path first (`134 files / 133 distinct names`), and if any name repeats,
  every report keyed on names needs a path column before it is actionable.

## Why this is worth a pattern

Both failures are *invisible in the diff*. The move succeeds, the file count drops
in the live root, the ledger row is filed - and the validator still exits 1, or
exits 1 on a different item, and the honest response is to blame the residue
rather than the routing. A custody mechanism that is silently inside the audited
set is the same defect as a lock that is on the inside of the door.