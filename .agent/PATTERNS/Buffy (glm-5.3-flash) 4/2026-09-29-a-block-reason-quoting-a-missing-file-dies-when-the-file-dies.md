---
document:
  title: "A block reason quoting a missing file dies when the file dies"
  task: "B5-0999"
  date: "2026-09-29"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 4", version: "glm-5.3-flash"}
---

# A block reason quoting a missing file dies when the file dies

**One-line lesson:** a gate recorded as ledger prose is a stale snapshot, not a
live condition — when the artifact it quotes changes state, nothing re-evaluates
the prose, so a released gate stays BLOCKED until a claimed row re-censuses it
by hand.

## Shape of the failure

Rows B5-0969 and B5-0990 carried their gate ("claimable only after the B5-0953
claim is released") inside their note cells. When the ghost claim
`.agent/CLAIMS/B5-0953.json` was deleted under explicit human authorization
(admin release, `docs/DECISIONS.md` 2026-09-28T22:28Z), the release note itself
said the gates were now clear — and both rows still read BLOCKED hours later,
because no tool in the repo re-evaluates row prose. The runner correctly
would not offer a BLOCKED row, so the intent recorded in DECISIONS.md was inert
until a claimed task flipped the cells.

## What worked

- Re-census with the shipped tools, not prose: `ls .agent/CLAIMS/` for the
  quoted file's absence, heartbeat mtime ages for the ghost's last signals,
  `run-queue.ps1 -DryRun` and `ledger-query.ps1 -Status *` for the
  no-second-blocker proof.
- Flip only the status cell via an exact-anchor replace; every other cell
  byte-identical; post-write `run-dup-census` and `ledger-query` 7-pipe /
  doubleLead checks on each touched row.
- Respect the standing exceptions the re-census surfaces (the B5-0481 orphan
  stands per B5-0791; reap is a separate, separately-authorized action).

## Related records

- Supersedes nothing; this is the first record of this shape in this namespace.
- Companion: `B5-0997` measured the ghost as reap-ready; the admin release
  (B5-0976 escalation option 2) did the reap and named the consequences; this
  record is the third beat — the gates the release opened still needed a
  claimed re-census to become real.
