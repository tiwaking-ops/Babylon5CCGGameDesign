---
document:
  title: "DECISIONS relocation byte-integrity amendment — proposal (from the B5-1487 dry run)"
  status: "Proposal (candidate, never truth until merged; AGENTS.md section 3)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  task: "B5-1487"
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Amendment: the B5-1425 relocation and B5-1437 insertion acceptance criteria, corrected by measurement

**Deliverable of B5-1487 (scratch dry run of both proposals' tooling).** The
dry run executed both proposal shapes against synthetic fixtures
(`b5ccg/out/scratch-b51487/`, git-ignored; real DECISIONS untouched — its
post-truncation state lacks both proposal targets anyway, see findings).

## Result matrix

| Invariant | B5-1437 insertion (child body) | B5-1425 relocation (Option A) |
|---|---|---|
| Header census unchanged | PASS (2→2) | PASS (7→7) |
| Pure insertion at chosen site | PASS (all pre-insert lines byte-identical) | n/a |
| Block ORDER after move | n/a | PASS (stray lands before first 09-30 header; top block lands at bottom) |
| Moved block byte-identity | n/a | **FAIL (line-array instrument)** |

## What failed and why (the mechanism, measured)

The relocation was implemented the obvious way — read lines into an array,
splice ranges, `Set-Content` back. The moved block landed in exactly the
right position, yet the byte-identity invariant failed **for every line of
the file**: `Set-Content -Encoding UTF8` wrote a **UTF-8 BOM** (`EF BB BF`,
absent before) and normalized **bare-LF to CRLF** (0/37 → 37/37 CR lines).
A "successful" move by whole-file line-array rewrite corrupts 100% of the
untouched bytes. This is the same instrument class B5-1453 caught flipping
DECISIONS.md whole-file — measured here on a fixture where nothing but the
move was supposed to change.

## Amended acceptance invariants (supersede the originals in both proposals)

1. **Untouched-region byte equality:** the complement of the moved/inserted
   ranges must hash byte-identical before and after. Per-line or block-only
   checks are insufficient.
2. **Moved-block byte equality:** the extracted block's sha256 taken before
   must equal its sha256 after (catches in-move corruption).
3. **Ending census equality:** CRLF and LF line counts must be identical
   before and after, file-wide.
4. **BOM assertion:** no BOM before, no BOM after.

## Amended executor recipe (the fix the mechanism demands)

Relocation must be a **byte-splice, never a line-array rewrite**: read the
file as bytes (`ReadAllBytes`), locate block boundaries by exact byte search,
write the concatenation `[prefix]+[moved blocks in order]+[suffix]+[marker
entries]` with `WriteAllText` using `new System.Text.UTF8Encoding($false)`
(explicit no-BOM) — or a raw byte append/insert — so every byte outside the
intended change survives untouched. The insertion shape (B5-1437) already
passes as specified; keep its invariants and add the ending-census and BOM
assertions for defense in depth.

**Reusable lesson:** dry-run the tooling before ratifying the plan — the
dry run's failure was not in the approved logic but in the obvious
instrument, which would have corrupted 100% of untouched bytes on the real
governance file on execution day.
