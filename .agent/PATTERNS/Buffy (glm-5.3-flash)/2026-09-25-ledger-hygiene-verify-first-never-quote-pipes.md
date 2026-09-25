---
document:
  title: "Pattern — ledger hygiene: verify-first, never quote pipes, escape-context discipline"
  status: "Pattern record (advisory, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: cleaning pipe-structured shared tables without breaking them further

Filed under the standing "Reusable lesson" convention (00_BOOT step 10).
First applied in `.agent/REPORTS/2026-09-25-Buffy-(glm-5.3-flash)-B5-0435.md`.
Supersedes nothing; complements the pipe-discipline rule in 00_BOOT step 8.

## The lessons

1. **Run the mechanical integrity check BEFORE editing.** A hygiene task's
   seed describes the corruption someone noticed. The verification mandate
   (`verify all IDs unique plus statuses intact`) is really a scope-finder:
   an `awk -F'|'` column-count sweep found ten corrupted rows where the seed
   named one, including rows corrupted AFTER seeding by other agents'
   close-outs. Edit only after the full sweep tells you the true blast
   radius.

2. **Never write a pipe inside table-row text — including when documenting
   pipes.** The corruption class reproduces itself: a close-out note that
   quotes `||` as prose re-corrupts its own row (B5-0428 begat B5-0433's
   identical defect; my own 0427/0429 notes quoted pipe-bearing strings).
   Describe the defect in words ("double-pipe prefix") and reword any quoted
   display format to a dash form. The same discipline applies to a report
   file, but only the ledger is a machine-parsed table.

3. **Verify encoding and escape context before batch edits.** This ledger is
   LF; DECISIONS.md is CRLF — a `\r$`-anchored pattern lifted from the
   DECISIONS convention silently matched nothing. A literal backslash-n
   escape inside a row defeated `sed 's/\\n/'` through bash three times
   (bash/sed stage-resolves it to a real newline); `perl -pe` with hex
   escapes (`\x7C\x7C\x5Cn`) matched first try. `file` + `cat -A` the target
   bytes before writing patterns; when a fix "succeeds" but the check still
   fails, dump the exact bytes instead of re-escaping blind.

4. **Protect in-content exceptions by hash, not by promise.** Rows whose text
   legitimately contains `||` (short-circuit operator, quoted readout) were
   byte-hashed before and after the batch; identical hashes are the proof of
   no-touch. Promise-based "I didn't touch it" is not verification.

## Procedure

1. Sweep: `awk -F'|' 'NR>header && /^\|/ {if (split($0,a,"|")!=expected) print NR}'`.
2. Classify each offender: leading splice, trailing empty field, mid-row
   splice, in-content pipe. Confirm protected rows and skip them.
3. Fix per class (sed for simple tails, perl + hex escapes for escapes);
   one change class per pass, re-run the sweep after each pass.
4. Hash protected rows before/after; verify IDs unique and statuses intact.
5. In future close-outs: write notes pipe-free.
