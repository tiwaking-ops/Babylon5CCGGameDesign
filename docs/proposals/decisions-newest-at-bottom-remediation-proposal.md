---
document:
  title: "DECISIONS.md newest-at-bottom remediation — proposal"
  status: "Proposal (candidate, never truth until merged; AGENTS.md section 3)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  task: "B5-1425"
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# DECISIONS.md newest-at-bottom remediation — proposal

**Deliverable of B5-1425 (docs-only).** This proposal owns NO edit to
`docs/DECISIONS.md`: the file's body, frontmatter, and every ledger row other
than B5-1425's own close-out cells are untouched by this task. What follows is
the repair shape, the options, and the acceptance criteria, filed as a
candidate per AGENTS.md section 3 (`docs/proposals/` = candidates, never truth
until merged).

## 1. Problem statement (measured)

`docs/DECISIONS.md` self-declares its placement contract at the top of its
body: "**Append-only. Newest at bottom.**" Three placement defects against
that contract are on record and were re-verified against the working tree on
2026-10-01 (file then 11,473 lines):

* **P1 — newest entries at the TOP (B5-1311).** Four 2026-09-30 entries occupy
  lines **76–113**, directly under the contract lines, ABOVE the 2026-09-21
  founding entries (which resume at line 114):
  * line 76 `## 2026-09-30 — Buffy (openai/gpt-6-luna) 1: B5-1167 BLOCKED at prerequisite gate`
  * line 85 `## 2026-09-30 — Buffy (openai/gpt-6-luna) 1: B5-1145 BLOCKED before post-fix sweep`
  * line 94 `## 2026-09-30 — GitHub Copilot (Auto mode) 0930` (B5-1043)
  * line 104 `## 2026-09-30 — GitHub Copilot (Auto mode) 0930` (B5-1057)

  **Re-verified this pass: the block is NOT duplicated at the bottom** — each
  of the four entry titles has exactly ONE hit in the whole file. These are
  the only copies; relocation is a move, not a dedupe.
* **P2 — one mid-chronology 2026-09-29 entry (B5-1311).** `## 2026-09-29 —
  Solar Pro4 (solar-pro4:free): B5-1041 DONE — stand-down on fresh signal`
  spans lines **1122–1171** (next header at 1172 is a 09-23 entry), outside
  the 09-29 block at 9385–9623. Note the surrounding zone is already
  mixed-chronology (09-23 material at 1172, 09-29 at 1122), so "the 09-29
  block" is not contiguous either — the repair must define its target
  position precisely, not gesture at it.
* **P4 — the adjacent header/body defect (B5-1311's one FAIL).** Line 8972
  `## 2026-09-28 - Buffy (glm-5.3-flash) 2: TIMESTAMP INTEGRITY VIOLATION,
  self-reported …` is a header with NO body (next header at 8974). Recorded,
  not repaired, by B5-1311. It is in scope of this proposal's **option set**
  only as context: it is a different defect class (header/body integrity, see
  `.agent/PATTERNS/Buffy (glm-5.3-flash) 11/2026-09-30-an-entry-header-without-a-body-is-an-unsourced-authority-record.md`)
  and needs its own body restoration from session transcripts — a move alone
  does not fix it. Named here so a remediation pass does not silently absorb
  or skip it.
* **P3 — mojibake em-dash in 09-28 headers (B5-1311)** is explicitly OUT of
  scope: byte-form repair is a distinct defect class (B5-1002 closed the body
  lines; headers retained the `â€”` form), and mixing it into a relocation
  would violate the no-byte-edit acceptance criterion below.

**Why it matters:** a reader who trusts position — the convention's entire
purpose — mis-orders the newest decisions. A reader who trusts nothing falls
back to the date in the entry header, and AGENTS.md section 3 forbids exactly
that ("No authority from date, filename, length, or repetition"). The B5-1135
audit chain (closed DONE 2026-09-30, 6/6 entries PASS on provenance criteria)
owns the 2026-09-30 *provenance* sample; this proposal covers *placement*
only. The two are disjoint, which is why both could proceed without collision.

**Cost every option shares — citation drift.** Any body move below line 76
shifts every subsequent line number. Hundreds of line-anchored citations
exist across `.agent/REPORTS/`, ledger notes, and DECISIONS entries themselves
(e.g. B5-0966's "lines 4600 to 4664", its withdrawal pointer "line 5943").
Those citations are historical measurements (true when taken) and reports
carry no authority, but the drift is real. Mitigation that costs nothing and
survives every option: **cite DECISIONS entries by title/task-id, never by
bare line number, going forward**; a line pointer is only ever valid as of
the moment it was measured.

## 2. Acceptance criteria (the row's named checklist: B5-0966)

Whichever option is ratified, the executor must satisfy the B5-0966-derived
criteria as applied by the B5-1135/B5-1311 audits:

1. **Provenance present for the change itself** — the remediation pass is
   recorded in DECISIONS.md frontmatter `assessor_llm` (compaction form,
   AGENTS.md section 1a; never author and assessor in the same pass) plus a
   marker entry at the bottom describing what moved, from where, to where.
2. **Append-only with no body edit** — entry BYTES move; no line inside any
   entry is rewritten. The only permitted non-move lines are the blank-line
   joins between relocated entries and their new neighbours, enumerated in
   the marker entry, not improvised.
3. **Interpretations stay interpretations; no rulebook change** — nothing
   here touches `BABYLON5_CCG_RULEBOOK.md` at all.
4. **Verdict with line pointers** — the marker entry records pre-move and
   post-move line anchors for every relocated entry, so existing citations
   can be re-anchored mechanically.

## 3. Options

### Option A — relocate to chronological position, with marker entries (RECOMMENDED)

One atomic write, one agent, one claim:

1. **Move lines 76–113** (the four 09-30 entries, byte-identical, including
   their preceding blank lines) to the bottom of the file, after the last
   entry (tail currently ends with the B5-1341 entry). Same-date entries are
   order-free relative to each other, so the tail placement is contract-clean.
2. **Move lines 1122–1171** (the B5-1041 09-29 entry, byte-identical) to
   immediately BEFORE the first 2026-09-30 entry, so the tail reads
   `… 09-29 entries … | B5-1041 | … 09-30 entries … | relocated 09-30 block`.
   This is an interior insertion — it shifts every later line and is the
   honest price of correct ordering; the marker entry records the shift.
3. **Leave no placeholder at the vacated sites.** Any pointer text left in
   the body at the old site becomes a permanent new line in an append-only
   file; the pointer obligation is carried by the marker entry instead,
   which is append-compatible.
4. **Append ONE marker entry at the bottom** (newest, so bottom-most):
   `## 2026-10-01 — <agent>: DECISIONS placement remediation (B5-1425
   proposal ratified by <pointer>)` listing: what moved (four titles +
   B5-1041 title), exact pre-move and post-move line anchors, byte-hash of
   each moved block, the −38 / interior line shifts, the blank-line joins
   added, and the title-based-citation convention. It also records that
   P4 (8972) and P3 (mojibake headers) remain OPEN, owned elsewhere.
5. Record the pass in DECISIONS.md frontmatter `assessor_llm` (compaction
   form).

Post-state: contract reads true; the two strata (P1, P2) are gone; P3/P4
remain, visibly owned by their own defect classes.

### Option B — amend the contract instead of the body (zero-byte-move fallback)

Append one marker entry declaring the placement exceptions by anchor:
"newest at bottom, except the four 09-30 entries at lines 76–113 and the
B5-1041 entry at 1122–1171, recorded as exceptions on <date>." Optionally
amend the header contract line itself (a one-line body edit — the same class
of edit B5-1311 disclosed and did not make).

* Pros: zero line shifts, zero citation drift, one write.
* Cons: bakes the anomaly into the contract permanently; every future
  placement audit must carry the exception list; exception lists rot; the
  file keeps lying to position-trusting readers, now *by ratification*.

### Option C — record only; do nothing (default posture, rejected)

Cheapest, and the repo's default is "recorded, not repaired". Rejected
because the trap stays armed: B5-1311 found P1–P4, the queue then seeded
B5-1425 to plan the repair — the recursion is visible in the ledger. Doing
nothing guarantees the next audit re-finds the same four observations and
re-seeds another planning row, spending an agent-run per cycle forever.

## 4. Human gate (disclosed, not assumed)

Relocation rewrites the bytes of a governance file's body — every moved line
is deleted from one offset and inserted at another, even though no line's
content changes. Two readings exist:

* **Autonomous-promotion reading:** docs-only change, compile gate
  non-applicable, AGENTS.md section 4 promotion applies (claimed scope,
  DECISIONS entry, claim release — all normal).
* **Human-gate reading:** by analogy with the external-library gate (the
  repo's sole human gate), the *decision record itself* is the one artifact
  whose silent rewriting no agent should perform unilaterally — the B5-0966
  finding (a withdrawn decision carrying no withdrawal marker for 3,070
  lines) is precisely what happens when decision-record bytes drift without
  an anchor.

**Recommendation:** take the human-gate reading for Option A (body moves),
and treat Option B's marker-only form as autonomous if a human would rather
not be bothered — it is the same write class as any close-out entry. The
ratifying human or session records their choice in the marker entry itself.

## 5. Verification plan for the executor (acceptance = all green)

1. **Byte identity of moved blocks:** sha256 of the extracted 76–113 and
   1122–1171 ranges pre-move equals post-move (extract, hash, re-insert —
   never retype).
2. **Header census invariant:** `rg -c "^## "` and the full ordered list of
   header lines identical before/after (same multiset, same order, none
   added or lost — the marker entries are the only new headers, and they
   appear once at the bottom).
3. **Diff shape:** `git diff --numstat docs/DECISIONS.md` shows only the
   moved-block sizes (+/− symmetric, e.g. 38/38 for the top zone) plus the
   marker entries; `git diff` shows zero modified (as opposed to moved) lines.
4. **Re-run the sample:** the B5-0966/B5-1311 criteria re-applied to the
   relocated entries produce the same per-entry verdicts B5-1135/B5-1311
   recorded.
5. **Universal close-out gates:** post-write `run-dup-census.ps1` (exit 0)
   and `ledger-query.ps1` reading the remediation row `7` pipes /
   `doubleLead no`, per 00_BOOT step 9.

## 6. What this proposal does NOT do

No edit to DECISIONS.md, the rulebook, any ledger row other than B5-1425's
own close-out cells, `b5ccg/src/`, or card data. The foreign live claims
holding adjacent read-only measurements at claim time (B5-1047, B5-1331,
B5-1345) were fenced: nothing their scopes cover was read into evidence
beyond what B5-1311's closed report already published, and no file of theirs
was touched.

**Reusable lesson:** a placement convention in an append-only file can only
be repaired by either moving bytes (needs a ratification gate and a
line-drift disclosure) or amending the convention (needs a permanent
exception list) — there is no third write that both moves nothing and
changes nothing, and choosing between those two costs is the whole decision.
