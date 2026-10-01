---
document:
  title: "B5-1062 — R7 lookalike-codepoint rule and its enforcement"
  status: "Report"
provenance:
  author_llm: {name: "opencode (big-pickle)", version: "big-pickle"}
  assessor_llm:
  last_modified_by_llm: {name: "opencode (big-pickle)", version: "big-pickle"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# B5-1062 — R7 lookalike-codepoint rule and its enforcement

> **TASK-ID COLLISION, disclosed.** I filed this work under `B5-1062` before
> reading the ledger tail. A row `B5-1062` **already existed** (OPEN, seeded
> 2026-09-29, heartbeat-retirement policy) and its scope cell **names
> `.agent/HEARTBEATS/README.md` — the file I edited**. The ledger row was
> UNCLAIMED (no `.agent/CLAIMS/B5-1062.json` existed before mine; my claim file
> was the only one, and it is now released), so there was no live foreign
> writer at the time, and the work is additive to that row's subject matter
> rather than duplicating it. But the id was taken and I should have read the
> ledger tail before claiming. The exception record and its proper id are
> `B5-1066`; this report keeps `B5-1062` because renaming it would break the
> id already embedded in the R7 provenance entries and comment markers in
> `validate-heartbeats.ps1` and `README.md`, which is exactly the citation-
> breaking class R5 exists to prevent.

Human-approved item 2 of a two-item authorisation ("APPROVED 2, APPROVED 3").
Item 3 was **stopped before any byte was written**; see §3. The human then
ruled (a) leave the 17 alone, and (b) write the exception record — done as
`docs/proposals/lookalike-filename-r5-exception-proposal.md` under `B5-1066`.

## 1. The gap, measured not assumed

`validate-heartbeats.ps1` reported **0 identity collisions on a store that held
three.** The cross-file `agent_id` uniqueness check could not see them:

* the correctly-spelled sibling of `solar-pro4<U+F03A>free.json` was in
  `.agent/HEARTBEATS/_quarantine/`, and the file listing used `Get-ChildItem
  -File` with **no `-Recurse`**, so the quarantine directory was never enumerated;
* the cross-file check keys on the *parsed* `agent_id`, which is the correctly
  spelled `solar-pro4:free` in **both** files. Correct data, invisible defect.

Before: 94 files / 93 conforming / 1 non-conforming / **0 collisions** / exit 1.
After: 102 files / 93 conforming / 9 non-conforming / **3 collisions** / exit 1.

## 2. What changed

Two files, governance only. No `src/` edit, no rename, no commit, no push.

**`.agent/tools/validate-heartbeats.ps1`**

1. `FILENAME LOOKALIKE` per-file check. A lookalike codepoint in the *filename* is
   a finding in its own right, independent of whether a sibling happens to be
   present: PUA `U+E000–U+F8FF`, astral PUA (explicit surrogate pairs), and
   separators `U+2028 / U+2029 / U+00A0`. The finding names the codepoint.
2. `-Recurse` on the store listing, so `_quarantine/` is audited. A quarantined
   file is out of the live store by decision, but a lookalike name is a defect of
   the *name*, not of the location, and the sibling's presence is the only
   cross-file evidence there is.
3. A documented trap, in-line: `\u{100000}` is **invalid** in .NET regex
   ("Insufficient hexadecimal digits") and a throwing `Matches()` yields 0
   findings. My first attempt used it and the check silently passed the exact
   file it was written to catch — caught only because I ran it. Astral PUA is
   matched by surrogate pair for that reason.

**`.agent/HEARTBEATS/README.md`** — added **R7** to the Identity section: the
`:`/`/` → `-` transliteration is the only legal filename spelling, the `-` must
be U+002D, and no filename may carry a PUA or separator codepoint. R7 is marked
as proposed by me on B5-1062 and **explicitly not covered by the 2026-09-28
ruling**, which adopted R1–R6 only — the section header otherwise reads
"Adopted by human ruling", and an unmarked R7 would have falsely attributed
itself to a human. It is additive; it alters no existing `agent_id`.

Pre-existing uncommitted edits in both files (B5-1002 explicit-UTF-8 pinning,
B5-1049 `AGENTS.md` provenance) were left intact and extended, not reverted.

## 3. Item 3 STOPPED — two ratified constraints, not my judgement to override

The approved on-disk rename of the 17 U+F03A path components is **not done**.

1. **R5** (`.agent/HEARTBEATS/README.md`, human-ruled 2026-09-28, decision
   B5-0785): *"never retro-rename an existing id. `solar-pro4:free` is cited in
   114 ledger rows and 154 reports; renaming breaks every citation to fix a
   defect the rename causes."* **R6**: R1–R6 *"authorise nobody to edit a
   foreign heartbeat."* Every one of the 17 paths is a foreign agent's file.
   B5-0793 reached the same conclusion and left both files byte-identical.
2. **11 live ledger rows cite `PATTERNS/solar-pro4*` paths** (lines 42, 56, 57,
   62–64, 74, 75, 139, 152, 157, 160, 164, 183, 189). A rename rewrites
   committed history that citations point at. This repo has prior form here:
   under B5-0613 a rename-drift episode *hid 14 ledger rows*.

Also unresolved and needing the same ruling: 2 non-identical name collisions
(`B5-0515`, `B5-0526` — 1523 vs 3166 bytes, 1753 vs 2350), so a rename is not
mechanically lossless without a suffix decision; and `solar-pro4-free-0978` is
unclassified.

R7 makes the *next* instance impossible. It does not retro-fix the 17, and
per R5 it must not.

## 4. Verification

| Check | Result |
|---|---|
| AST parse of edited validator | 0 errors |
| Clean 1-file store | `files: 1, conforming: 1, collisions: 0`, **exit 0** |
| Same store + one U+F03A file | `non-conforming: 1, collisions: 1`, names `U+F03A`, **exit 1** |
| Live store | 102 files / 3 collisions, exit 1 |
| Pre-existing diffs | intact (B5-1002, B5-1049) |

Both directions were tested, because a check that only ever goes red is a
permanent red, and the repo's own lesson (B5-0613) is that a validator which
never went red proves nothing. The clean-store case is what proves the new
check is specific rather than simply reacting to `-Recurse`.

## 5. Reusable lesson

A cross-file uniqueness check is only as good as the enumeration feeding it: if
the colliding sibling sits outside the listing, correct `agent_id` data yields a
clean report on a dirty store. And validate the *predicate* against the real
input — `\u{100000}` threw, and the check reported zero findings on the exact
file written to catch it.
