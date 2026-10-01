---
document:
  title: "B5-0837 close-out — a half-closed row, verified not redone"
  status: "Report (close-out, task DONE)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0837", version: "space-bunny"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0837 — Close-out of a half-closed row

Agent: Cline (space-bunny) b5-0837 · Claim: `.agent/CLAIMS/B5-0837.json`
(started 2026-09-28T07:53:30Z, released at close-out) · javac 1.8.0_292 · No commit.

## Verdict

The scope was **already complete on disk** when the runner offered this row. I
verified every deliverable from measured bytes, flipped the one status cell that
had been left behind, and changed nothing else. No `.gitignore` line, no
filename, no `b5ccg` file.

## The row was half-closed, not unstarted

This is the finding, and it is a different defect class from either "not begun"
or "done". At claim time:

| Cell / artifact | State on arrival |
|---|---|
| status cell | **`OPEN`** |
| note cell | a complete DONE write-up, 2026-09-28T07:49:35Z |
| report | present, `.agent/REPORTS/2026-09-28-Buffy (glm-5.3-flash)-B5-0837.md` |
| pattern | present |
| `docs/DECISIONS.md` entry | present |
| `.gitignore` lines 72, 73 | present |

A prior session (`Buffy (glm-5.3-flash)`) had completed every element of the
scope and never written the status cell. Since the runner keys the queue by
status, the row was **simultaneously finished and offerable** — any agent could
have re-claimed it and redone completed hygiene work, and nothing in the row
would have said otherwise. That is the real hazard: not wasted effort, but
finished work being invisible to the one field that gates it.

Neither cell was trusted. I re-derived the deliverable from disk.

## Independent re-measurement (bytes, not renderings)

Per the B5-0793 lesson, codepoints are the evidence and a console rendering is
not:

| Path | Name bytes | Length | Contents |
|---|---|---|---|
| dir 1 | `2e 63 ef 80 ba` (`002E 0063` + U+F03A) | 3 | 1 file, 5930 b |
| dir 2 | `43 ef 80 ba 74 65 6d 70 62 35 63 63 67 2d 68 61 72 6e 65 73 73` | 19 | 0 files |
| dir 3 | `50 65 6e 65` (`Pene`, plain ASCII) | 4 | 0 files |

All three byte-for-byte identical to the prior report's inventory, so **no
drift occurred** between the two sessions.

## Containment re-proven by exit code

Not by inspection of the line, which is the discipline the prior pattern
(`Buffy (glm-5.3-flash)/2026-09-28-a-containment-line-that-does-not-match-is-a-silent-failure.md`)
established and which caught that session's own stray colon byte:

* `git check-ignore -v` → dir 1 = `.gitignore:72`, dir 2 = `.gitignore:73`,
  `Pene` = `.gitignore:38`, **exit 0** on all three.
* `git status --porcelain --untracked-files=all` → **zero** entries for all
  three, so containment holds end to end.

## No live coordination state

A recursive sweep for `CLAIMS`, `HEARTBEATS`, `REPORTS`, `PATTERNS` or `.json`
inside the three paths returns **exactly one** hit: the mirrored
`.agent/REPORTS` directory under dir 1, holding the already-inventoried
displaced copy of the **closed** B5-0765 report. That is residue, not a live
claim or heartbeat, and I left it untouched — deleting it is a separate action
under the row's own note.

## Gate

`b5ccg/compile.bat` → **exit 0**, `Build successful`, javac 1.8.0_292 with
`-source 6`. Captured by redirecting output and reading `$LASTEXITCODE`, because
PowerShell reports exit 1 for any native command that writes to stderr and
`compile.bat` always emits the `-source 1.6` bootstrap-classpath warning. This
is the third session to be misled by that capture (B5-0835, B5-0839, now me),
which is why it is recorded rather than assumed.

Post-write `ledger-query.ps1`: my row reads **7 pipes / doubleLead no**.
`run-dup-census.ps1` exits 1 on `B5-0833 x2` (rows 976/978) — foreign rows
written by other agents, both pre-dating this pass, left byte-identical per
B5-0622 with no renumber into their slot.

## Scope accounting

Changed: one status cell in `.agent/TASK_LEDGER.md` plus my note (no pipe added
or removed, byte count otherwise preserved), one `docs/DECISIONS.md` entry plus
its provenance line, this report, one pattern, one heartbeat, my claim file
(created then deleted). **Unchanged: `.gitignore`** — the correct lines were
already there, so adding more would have been a no-op presented as work. No
rename, no deletion, no `.agent` coordination state touched, no `b5ccg` src or
card data edited, no commit.

## Reusable lesson

**A status cell is the last byte of a close-out, not the first.** Every other
artifact — note, report, pattern, log entry — is written before it, so any
crash, timeout or lost write in that tail leaves a row that reads `OPEN` while
being complete. Re-derive the deliverable from disk before starting work, and
make the status cell the first thing you write *and* the last thing you verify.
