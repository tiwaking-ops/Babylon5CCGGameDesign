---
document:
  title: "A census that does not name its instrument is not reproducible"
  status: "Pattern"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 4", version: "space-bunny-free"}
  assessor_llm:
    - {name: "unknown", version: "unknown"}
  last_modified_by_llm: {name: "opencode (space-bunny-free) 4", version: "space-bunny-free"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
---

# A census that does not name its instrument is not reproducible

**Measured 2026-09-29, Babylon 5 CCG, during a boot-verification pass.**

I measured mojibake in `docs/DECISIONS.md` two ways and got two different files:

| Read | Result | Reality |
|---|---|---|
| `Get-Content -Raw` (host default decode) | `C1 = 1600` | phantom — file is clean |
| `[System.IO.File]::ReadAllText(path, UTF8Encoding($false))` | `C1 = 0` | the truth |

Same bytes. The first reading would have seeded a repair task against a file that
B5-0989 had already fixed correctly, and the task would have looked impeccably
evidenced — I would have pasted the number into the row myself.

The same file pair is the reason the trap is easy to fall into: the *ledger* really
does carry 143 C1 marks. So one session, one pass, two files, one real defect and
one phantom, differing only in how they were read.

## The rule

**A measurement without a named instrument is a number, not a finding.**

This repo already encodes the same principle twice and had to learn it twice:

- The dup census's exit triple (`0` clean, `1` found, `2` could-not-read) exists
  because *a census that could not do its job must never report as a clean one*
  (B5-0777).
- "A census is a verdict with a receipt" is a filed pattern in this very store.

Encoding is a third instance of the same failure: a reader that cannot say **how**
it read the bytes cannot be checked, and two readers disagreeing will each be
convinced they are right.

## What to do

1. **Pin the encoding** in any tool that reads text for a verdict. Host defaults are
   an environment, not a specification.
2. **Emit which encoding you used** in the tool's own output. A tool that reports a
   count without reporting how it counted has made its number unauditable.
3. **When two instruments disagree, suspect the measurement before the file.** The
   file cannot be both clean and dirty; the reader can.
4. **Before seeding a defect, ask what the clean-file reading would have said.** If
   a second instrument gives 0, you have a reader bug, not a repair task.

## Reusable lesson

Encode a defect count only alongside the decode you used. The cheapest possible
check on a red finding — re-read it with a pinned encoding — is cheaper than
discovering a month later that a repair row existed only because of a shell default.

---

Filed by `opencode (space-bunny-free) 4`. See
`.agent/REPORTS/2026-09-29-opencode (space-bunny-free) 4-seed-wave-1000-1019.md` §3.
Seeded as **B5-1002**.
