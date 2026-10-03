---
document:
  title: "Proposal — DECISIONS correction-entry shape per defect class, and per-entry application order (B5-1629)"
  status: "Proposal"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 16", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
---

# DECISIONS correction-entry shape — proposal (B5-1629)

Consumes three DONE/OPEN predecessors, read in full before drafting:

* **B5-1435** (DONE, Kilo (kilo-auto/free) 2) — the structural-defect census:
  classes P0–P9 with counts and line pointers over two corpora, **W** = worktree
  `docs/DECISIONS.md` (436 lines at its last sample, 19 headers, all
  2026-10-01) and **H** = `git show HEAD:docs/DECISIONS.md` (5,036 lines, 292
  header lines / 290 distinct texts, newest 2026-09-28). W ∩ H header sets = 0.
* **B5-1425** (DONE, Buffy (glm-5.3-flash) 12) — the newest-at-bottom
  remediation proposal: Option A relocate-to-bottom with forward-supersession
  marker entries (recommended, human-gated per the B5-0654 withdrawal-marker
  precedent); acceptance = the B5-0966 checklist with byte-hash +
  header-census invariants + **cite-by-title-not-line**.
* **B5-1505** (OPEN) — the post-incident regrowth watch: append-integrity
  sampling by ending + mtime signature; flags any shrink or ending flip.

## 1. The one-line answer the row asks for

**Under a strict append-only discipline, every agent-filed correction to a
defective DECISIONS entry is a new dated bottom entry. The child-body-under-
the-failing-header shape is never an agent move: any insertion between existing
entries is a mid-file write, which is exactly the whole-file-rewrite class
B5-1453 named as the defect instrument. The child-body shape is reserved for
the human-gated tracks only** — B5-1425 Option A relocations (bodies move
anyway, so a body is not made worse) and P4-class body restoration for the
unreproducible line-8972 header (whose home corpus no longer exists in the
worktree, so nothing can be inserted into it today regardless).

## 2. Per-class correction shape

Each correction entry carries: `CORRECTION (append-only, <class>)`, the
defective entry named **by exact title** (never by line — B5-1425/B5-0966),
the corrective content, evidence pointers, and the sentence *"No existing byte
is moved, edited, or deleted."* Header form follows the house entry header:
`## <YYYY-MM-DD> - <agent id>: B5-<task> DONE - DECISIONS correction: <class> ...`.

| Class | Defect (B5-1435 counts) | Shape that applies | Why not the other |
|---|---|---|---|
| **P9** collision allegation unadjudicated (1, W ~line 303) | **Bottom entry (evidence, not verdict).** Names the allegation entry by title, states the contradicting artefacts (ledger claim cell `Buffy (glm-5.3-flash) 12` / report path), and requests adjudication by the owner or a human. Never rules. | A child under the incident header inherits its date and stays buried below every later reader's attention; the point of the entry is to be the newest, dated word on the claim. Adjudicating inside the entry body would also edit the allegation's meaning in place. |
| **P8** undated level-2 headers (2, H 5012 `B5-0725 — BLOCKED`, 5020 `B5-0749 — DONE`) | **Bottom entry with a derived-date table**: one row per header — exact title, derived date, derivation (entry body content, adjacent same-day entries, the task row's DONE stamp in the ledger). | A child body under the header is invisible to the very date-order readers the correction serves — they cannot find the header to find the child. The bottom entry is dated and therefore findable; the header stays untouched. |
| **P5** headers invisible to a `^##` anchor (12 of 304 at H: 3 leading-whitespace 709/1195/3817, 1 U+FEFF 3965, 8 `###` sub-entries 1338–1435) | **One consolidated bottom "header-registry erratum"**: a table listing each invisible header with its byte-exact form, the offending codepoints (U+FEFF, leading space), the anchor that does match (e.g. BOM- and indent-tolerant), and — for the 8 `###` — an explicit statement they are sub-entries, not missing level-2 headers. | Twelve separate entries is twelve chances to re-introduce order defects; one dated entry with a table carries the same information and keeps the application order flat. Promotion of `###` to `##` is a header rewrite — forbidden. |
| **P6** unreproducible titles (U+FFFD ×2 at H 1764 `B5-0378`; U+FEFF at 3965) | **Bottom erratum per title** (may share the P5 consolidated entry): canonical byte-exact reconstructed title + codepoint inventory, so every later citation cites the canonical form and every byte-exact search fails loudly instead of silently. | The broken title cannot be fixed in place (header = identity, B5-1435). A child under it is only reachable *through* the broken title — the correction must exist where a title-keyed reader looks: the dated tail. |
| **P7** byte-identical duplicate header pairs (2: `B5-0408` H 2205/2238; `B5-0487` H 3424/3430) | **One bottom entry per pair (or one entry for both)**: names both members in reading order, declares the disambiguation convention — cite as `<title> (first|second occurrence)` **plus** date + agent from the body — until a human-gated dedupe lands. | Renaming either header is a rewrite (forbidden). A child under either member cannot say "I am #1" without the other being edited to say "I am #2" — a two-sided in-place edit. The bottom entry is one-sided and safe. |
| **P1/P2** out-of-order entries (23 ascending jumps + 16 straddlers at H; 0 in W today) | **Bottom "order inventory" entry** listing each misplaced entry by title + true date, explicitly labelled *inventory, not relocation*. The actual relocate-to-bottom move stays **B5-1425 Option A, human-gated**, with forward-supersession markers. | Moving a body is a mid-file write — never agent-permitted. And every misplaced entry already carries its own date + agent + receipts, so the inventory entry fully restores *authority* without moving anything; the move is cosmetic by comparison. |
| **P0** total history loss (94% of committed bytes absent from W) | **No correction entry can repair this.** Append-only interface = the forward-pointer entry B5-1437 (DONE) already drafted, plus the reconstruction backlog (B5-1509 source classes A–D). Restoration itself needs a human-ratified row per the file's own incident entry. | Anything else is reconstruction, not an append. |
| **P4** body-less header (1, old line 8972, **unreproducible** — lived in uncommitted bytes the truncation destroyed) | **Child-body-under-header is the theoretically right shape and the practically impossible one**: its home corpus does not exist in W, so there is nothing to insert into. Correct handling = B5-1437's drafted restoration text, applied only inside a future human-ratified restoration. | N/A — the target is gone; only the restored corpus could host it. |
| **New arrivals (prevention)** | **No correction entry.** A close-out entry that lands above an older-dated entry in a concurrent-append window is *self-describing* (its own header date + agent). Standing rule for writers: **never backfill, never reorder your own or others' entries**; B5-1505's chronology receipts are the monitor. | Chasing perfect ordering at write time recreates the P1/P2 repair problem continuously; ordering defects that matter (undated, uncitable) are covered by P8/P6 above. |

## 3. Per-entry application order

Phased, because the P5–P8 targets live in **H** (the committed blob), which is
absent from the live worktree W until P0 restoration lands.

| # | Entry | Target corpus | Phase | Unlocks |
|---|---|---|---|---|
| 1 | **P9 evidence entry** | W (live file, ~line 303 region) | **now** | nothing downstream; highest authority impact (it is the only defect that makes a live statement about another session false) |
| 2 | **P8 derived dates** | H targets, entry filed in W tail, keyed by title | after restoration | date-order tooling can finally see 2 headers that every P1/P2 audit currently skips — this is why B5-1435 ranks it first among the cheap set |
| 3 | **P5+P6 consolidated title erratum** | H targets, keyed by title | after restoration | cite-by-title-not-line becomes safe (B5-0966 acceptance precondition) |
| 4 | **P7 duplicate disambiguation** | H targets, keyed by title | after restoration | completes citability; independent of #2/#3 so may parallel them |
| 5 | **P1/P2 order inventory** | H targets, keyed by title | after restoration | reader trust in ordering; lowest authority value — first to be dropped if effort-bound |
| — | **P0 forward pointer** | already filed (B5-1437) | done | — |
| — | **P4 restoration** | human-ratified restoration only | blocked on human | — |

Rationale for P9-first over B5-1435's identical verdict: both orderings agree;
the phases only re-house B5-1435's H-targeted repairs until the corpus they
describe is live again. Filing them earlier would create errata for entries no
worktree reader can even see — a correction to a ghost.

## 4. Acceptance (inherits B5-0966 checklist)

1. Every correction entry is byte-append (B5-1505 ending + mtime signature
   before/after its write).
2. Every defective entry is named by **exact title**, never by line number.
3. Header-census invariant: the number of strict `^##` headers grows by exactly
   the number of correction entries appended (plus zero moves).
4. Byte-hash of the pre-write file is recorded in the entry's evidence line.
5. No entry deletes, edits, or reorders any existing byte; human gates
   (B5-1425 Option A moves, P4 restoration) are disclosed in-file as that
   proposal already does.

## 5. Scope honesty

This proposal edits **nothing** in `docs/DECISIONS.md`, the ledger (outside its
own close-out), or any coordination file. It is a proposal — advisory, never
canonical (AGENTS.md §3). The class inventory and all line pointers are
B5-1435's, consumed not re-measured; line numbers are H-blob-relative and will
drift the moment restoration begins, which is precisely why every correction is
title-keyed.
