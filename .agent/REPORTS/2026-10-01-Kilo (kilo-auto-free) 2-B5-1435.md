---
document:
  title: "B5-1435 close-out — whole-file structural census of DECISIONS.md: the four inherited defect classes are unreproducible in the worktree because 100% of the committed entry headers are gone, and one of the four was a reader artifact, not a file defect"
  status: "Report (observation, no authority)"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free) 2", version: "kilo-auto/free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Kilo (kilo-auto/free) 2", version: "kilo-auto/free"}
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
  task: "B5-1435"
---

# B5-1435 — whole-file structural census of `docs/DECISIONS.md`

**Row letter:** take the three structural observations the DONE B5-1311
close-out recorded *incidentally* in a 13-entry sample, and turn each into a
whole-file census with line pointers and counts; report per-defect counts, the
safest repair order, and which repairs an append-only discipline permits at
all. Inventory, not repair.

**Headline: the census is green on all four inherited classes in the working
tree, and that green is the finding.** 100% of the committed entry headers are
absent from the worktree, so the classes read clean because their evidence was
deleted, not because the defects were fixed. One of the four — the mojibake
class — turns out never to have been a property of the file at all.

## Instrument, corpora, and the disclosure that makes the numbers mean something

Read-only, git-ignored scratch dir `b5ccg/out/scratch-b51435/`:

| File | What it is |
|---|---|
| `census.ps1` | the four-class census (P1 order, P2 straggler, P3 mojibake, P4 body-less) + a non-ASCII codepoint inventory |
| `census2-tolerant.ps1` | the same checks re-run with a **deliberately more permissive** header anchor, for the reason `run-queue.ps1`'s `Get-LedgerRows` self-check gives |
| `probe-dash.ps1` | codepoint dump of the 09-28 header class + the replacement-character hunt |
| `census-output.txt`, `census-output-HEAD.txt`, `census2-output.txt`, `probe-dash-output.txt` | receipts |

**Two corpora, because the target changed under the row.** B5-1311 measured a
10,747-line working tree on 2026-09-30. That file no longer exists. Every count
below therefore carries the corpus it was taken from, and every worktree count
carries the line total it was sampled at — **the fleet was appending to this
file throughout the pass** (observed 412 → 429 → 436 lines, 18 → 19 headers),
so a bare line number here is only meaningful against the sample it came from.

* **W — worktree** `docs/DECISIONS.md`: 40,158 bytes, 436 lines, body starts at
  line 56, 19 strict `^##` headers, **every one dated 2026-10-01**, one date
  cluster spanning lines 60–425.
* **H — committed bytes** `git show HEAD:docs/DECISIONS.md`: blob **674,398
  bytes**, 5,036 lines, **292** strict `^##` header lines / **290 distinct**
  header texts, oldest-first, newest 2026-09-28.

**Header-set intersection W ∩ H = 0.** Every one of the 290 distinct committed
entry headers is absent from the worktree; the worktree's 19 are all
2026-10-01 entries written after the last commit. The provenance frontmatter
survived and grew (37 assessor entries at H, 43 at W), so **the loss is confined
to the body** — which is why the file still looks well-formed at the top.

## Per-class census

| Class (B5-1311's incidental observations) | W (worktree) | H (committed) | Verdict |
|---|---|---|---|
| **P1** entry/QUEUE note out of chronological order vs *newest at bottom* | **0** | **23 ascending jumps** | class is REAL and large. Line pointers in `census-output-HEAD.txt`; clusters alternate 09-25/09-26 across lines 2340–4194 and 09-26/09-27 across 4015–4194, plus a 09-27 single header at 1063 sitting above 19 headers of 09-23 at 1094–1565. W reads 0 only because it holds one date. |
| **P2** straggler dated entry | **0** | **16 straddlers** | same corpus effect. Every one is a 09-2x header sitting above a later-dated block, e.g. line 1612 (09-23) inside the 09-24 run at 1600–1646. |
| **P3** mojibake byte residue (`â€"` em-dash form in the 09-28 headers) | **0** | **0** | **CORRECTION: this was never a file defect.** The only 2026-09-28 header at H (line 5024) is **pure ASCII** — `## 2026-09-28 - B5-0703 adjudication …`, a plain hyphen, zero non-ASCII codepoints. B5-1311's P3 was rendered by its own read path, not read off the bytes. What P3 was *describing* — a title that cannot be cited byte-exactly — is real, but it is P6 below. |
| **P4** entry header carrying no body before the next header | **0** | **0** | **unreproducible.** The single known instance (line 8972 of the 10,747-line tree) lived only in uncommitted bytes that the truncation destroyed; it is not in H and not in W, so it can be neither confirmed nor re-cited. B5-1437 (DONE) already drafted its restoration text from the report corpus. |
| **P0** *total history loss* — **not in B5-1311's set, and the largest item here** | 40,158 of 674,398 committed bytes present (**94.0% absent**); 290 of 290 distinct committed headers absent (**100%**) | n/a | recorded in the file itself at W line 303 (*DECISIONS.md TRUNCATION INCIDENT*, "no restoration attempted"); H is the only surviving copy of the committed history, and the 2026-09-28→09-30 uncommitted layer exists nowhere at all. |
| **P5** *headers invisible to a `^##` anchor* — **new** | 0 (19 strict = 19 tolerant) | **12 of 304 headers invisible to `^##`** | 3 carry leading whitespace before `##` (H lines 709, 1195, 3817); **1 carries a U+FEFF at H line 3965**, which is neither whitespace nor ASCII and so defeats both `^##` and `TrimStart`; 8 are level-3 `###` sub-entries (1338, 1348, 1358, 1400, 1412, 1420, 1427, 1435). The 3965 case is a full entry header — `## 2026-09-26 — Solar Pro4 …: B5-0560 DONE` with a complete body — that a header sweep cannot see. |
| **P6** *replacement characters* — **new, and the defect P3 was aiming at** | 0 | **U+FFFD ×2 at H line 1764**, U+FEFF ×1 at 3965 | `## 2026-09-24 <U+FFFD> Cline (unknown) <U+FFFD> B5-0378` — the two em-dashes are *gone*, replaced by U+FFFD. This is the real "cannot be cited byte-exactly" instance, and it is on **09-24**, not 09-28. |
| **P7** *duplicate entry headers* — **new** | 0 | **2 pairs** | H lines 2205 and 2238 are byte-identical (`B5-0408 DONE — multi-round balance probe`); 3424 and 3430 likewise (`B5-0487 DONE; B5-0488 checkpoint claimed and committed`). 292 header lines, 290 distinct texts. Cite-by-title cannot disambiguate either pair. |
| **P8** *undated level-2 headers* — **new** | 0 | **2** | H lines 5012 and 5020 (`## B5-0725 — BLOCKED`, `## B5-0749 — DONE`) carry no date, so they are invisible to any date-order check including the newest-at-bottom convention they are supposed to satisfy. |
| **P9** *unadjudicated identity-collision allegation in the log* — **new** | **1, at W line 303 region** | n/a | the truncation-incident entry states that a `Buffy (glm-5.3-flash) 12: B5-1333 BLOCKED` entry was signed by a session that "never claimed B5-1333", and calls it an agent_id collision. The ledger's B5-1333 row (line 1276) reads **CLAIM = `Buffy (glm-5.3-flash) 12`, status BLOCKED**, and `.agent/REPORTS/2026-10-01-Buffy (glm-5.3-flash) 12-B5-1333.md` exists on disk. The allegation is therefore contradicted by two artefacts in the repo, and it sits **unadjudicated** in the authoritative log where every later reader meets it. Not adjudicated here: the heartbeat rules say a collision is reported, never merged, and the ruling is the owner's or a human's. |

Counts: **P0 1 (whole-file), P1 23, P2 16, P3 0 (corrected down from "pervades"),
P4 0 (unreproducible), P5 12, P6 3 codepoints on 2 lines, P7 2 pairs, P8 2, P9 1.**
Against the worktree alone every count is 0 except P9, which is why the
two-corpus framing is the deliverable and not a footnote.

## Safest repair order, and what append-only actually permits

Ordered by *how much authority is currently unsupported*, not by how many lines
move.

1. **P9 first — it is the only class that makes a live statement about another
   session false.** Append-only permits an appended adjudication entry stating
   the evidence (ledger claim cell plus the report path). It does **not**
   permit editing or deleting the allegation.
2. **P6 and P5 next — they are the "an authority record with no citable title"
   class, one instance of which (P4 at old line 8972) is already unrestorable.**
   Append-only permits an erratum entry per line giving the byte-exact title and
   the codepoint inventory; it does **not** permit rewriting a header in place,
   because a header is the entry's identity.
3. **P0 last, and it cannot be done append-only at all.** Restoration is
   reconstruction, not an append: `git show HEAD:docs/DECISIONS.md` is the only
   surviving copy of the committed layer, and the 09-28→09-30 uncommitted layer
   has no surviving copy in this repo at all — the file's own incident entry
   already says recovery needs a human-ratified row and names the sources. The
   only append-only move available now is a forward pointer entry, which is
   exactly what B5-1437's DONE proposal drafted.
4. **P1/P2 (39 violations at H) are last of the repairs and first to be
   dropped.** They are ordering, not authority: every entry they misplace still
   carries its own date, agent id and receipts, so a reader who reads the date
   in the header is never misled. B5-1425's DONE proposal already chose the
   shape (forward-supersession markers, Option A) and disclosed its human gate.
5. **P7 and P8 are cheap and safe** — an erratum line naming both members of
   each duplicate pair, and a date for each undated header. P8 is the one to do
   first among the cheap ones, because an undated header is invisible to every
   date-ordered reader, which is how P1/P2 audits keep missing entries.

**Not permitted under append-only, stated so no later pass proposes it:** moving
a body (P1/P2), rewriting a header in place (P5/P6/P7/P8), deleting the
truncation disclosure or the collision allegation (P9/P0), and any `git checkout`
/ wholesale rewrite of the file — which is the instrument the B5-1453 census
already named as the defect class.

## Fences honoured

Read `AGENTS.md` §3 status model: this is a report, not truth, and it repairs
nothing. The fenced predecessors are already closed — B5-1135 **DONE** (so its
2026-09-30 sample range no longer has an owner to protect) and B5-1425 **DONE**
(so the remediation proposal exists and this row stayed inventory, as its letter
requires). `docs/DECISIONS.md` was **read only**; the sole DECISIONS write was
this task's own close-out entry, appended at the bottom. No ledger row edited
but this one. No src, no card data, no heartbeat, no claim but my own. No
commit, no push. Compile gate not run and not applicable — no Java touched,
recorded per the B5-0785 lesson that a gate that never ran reads identically to
one that passed. Claim written with `.agent/tools/new-claim.ps1`, so
`started_utc` is tool-stamped `2026-10-01T04:08:29Z` with payload-vs-mtime
AGREE; released at close-out.

**Reusable lesson:** a census that returns zero for every class is a claim about
the *corpus*, not about the file — so name the corpus in every count, and when
the class you are asked to census has no surviving instance, say whether it was
**fixed** or **deleted**, because only one of those is a repair. And audit the
auditor: re-running your own census under a deliberately looser anchor is what
turned up 12 headers the strict pattern could not see, one of them a complete
entry with a body.
