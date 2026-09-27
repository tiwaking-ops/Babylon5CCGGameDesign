---
document:
  title: "B5-0609 close-out — read-only ledger query tool"
  status: "Observation"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# B5-0609 close-out — read-only ledger query tool

Task (seed wave 7): add `.agent/tools/ledger-query.ps1` — optional status filter
defaulting to OPEN, one line per row as ID, status, pipe count,
leading-double-pipe flag, claim owner, claim age in minutes, owner heartbeat
age, and a LIVE/STALE/UNKNOWN verdict. Three hard rules: parse structured
fields only (never task IDs in narrative prose such as heartbeat
last_completed); normalise punctuation when joining agent_id to heartbeat
filenames (ASCII colon vs U+2028); print UNKNOWN with a visible reason when a
join finds no match, never defaulting to a value that satisfies the freshness
test. Strictly read-only. Claimed atomically via
`.agent/CLAIMS/B5-0609.json` before any write.

## Delivery

`.agent/tools/ledger-query.ps1` (PowerShell, single file, no external deps):

* **Row parsing** — regex `^(\|+)\s*(B5-[0-9]{4}[a-z]?)\s*\|([^|]*)` matched
  against each ledger line; the ID and status come only from the structured
  row-start cells. Narrative prose (QUEUE notes, task text, heartbeat JSON
  last_completed fields) is never read as a row because the optional `\|\s*`
  prefix in the pattern requires the B5-ID to sit in the ID cell position.
  Tolerates a leading double pipe (`|| B5-0604`). Pipe count = total `|` on the
  line; double-lead flag = leading pipe run longer than 1.
* **Status filter** — `-Status` parameter, default `OPEN`; `*` or empty means
  all rows (verified: DONE filter returned 291 rows without error).
* **Claim join** — reads `.agent/CLAIMS/B5-*.json`; claim age from
  `started_utc` parsed as UTC, fallback to claim file mtime when unparseable.
* **Heartbeat join with punctuation normalisation** — `Get-NormName` strips
  U+2028/U+2029, colons, spaces, dashes, underscores, dots and lowercases
  remaining letters/digits on BOTH sides. Verified: `solar-pro4:free`
  normalises to `solarpro4free` and matches the real heartbeat file
  `solar-pro4<U+2028>free.json`; `opencode (me-so-poor)` matches its own;
  `Buffy (glm-5.3-flash)` matches `.json` heartbeat with that spelling.
* **Liveness** — verdict LIVE when the NEWEST of claim age and owner heartbeat
  age is under the TTL (default 30 min); STALE when both are at/over TTL.
  Absent signals: a non-empty owner with no matching heartbeat file prints
  `UNKNOWN` plus the reason `no heartbeat file matching owner '<owner>'
  (normalised '<norm>'); absent signal never treated as live`; an owner-less
  claim prints UNKNOWN with reason; no claim at all prints `UNCLAIMED`.
* **Read-only** — uses only `[System.IO.File]::ReadAllLines`, `Get-ChildItem`,
  `Get-Content`, `ConvertFrom-Json`; zero write calls anywhere in the script.

## Verification

* Fresh claim-file and heartbeat liveness cross-check at claim time (B5-0572
  residue claim present, owner heartbeat self-declared DONE, no reap
  attempted per the B5-0597 lesson).
* OPEN census on the live ledger at execution: 19 OPEN rows listed including
  the double-lead rows B5-0574/B5-0604..0608 (visible, correctly flagged
  `yes`), and B5-0575 correctly shown LIVE under its fresh Buffy claim — the
  tool tracked a claim that landed mid-session.
* Normalisation test harness (throwaway, no repo writes): all four owners
  joined correctly; a synthetic `ghost-agent:01` printed NO MATCH — the
  UNKNOWN path.
* `compile.bat` under `b5ccg/`: `Build successful` on JDK 1.8.0_292
  `-source 6 -target 6`; only the known benign bootstrap warning and
  unchecked-ops note. No src or resources edits were made, so no conformance
  re-run was required for this tool-only row.

## Notes

* B5-0612 (checkpoint for wave 7) gates on this row being DONE plus B5-0610
  and B5-0611; the tool is staged to be committed alongside the repaired
  `.agent/run-queue.ps1` (B5-0613) and the boot line (B5-0614) so the census
  becomes reproducible by every model.
* Reusable lesson: a census tool owns its join and parses structured fields
  only — what this row proved is that a read-only query helper is the
  durable fix for the "page the table until budget runs out" failure mode,
  with the UNKNOWN-not-fresh rule as the guard against silent false-live
  claims.