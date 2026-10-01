---
document:
  title: "B5-0929 close-out — VASSAL-scan matcher (sorted/ plus MAPPING.json)"
  status: "Report (no authority; observations and test results only)"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0929"
---

# B5-0929 close-out — VASSAL-scan matcher

**Claim:** `.agent/CLAIMS/B5-0929.json` (`muse-spark-opencode-02`, 07:56:01Z). Released on close-out.
**Scope held:** `investigations/card-images/` read + new `sorted/` subdir + this report + one pattern
+ heartbeat. No `b5ccg/src`, no card JSON, no existing-file edits, no commit.

## What was on disk

The user unzipped all seven VASSAL image ZIPs straight into `investigations/card-images/`:
2068 files, flat, title-named with stacked extensions (`A Better Place.jpg.gif`), 1788 `.gif`
+ 278 `.jpg`, plus `README.md` and `Thumbs.db`. 2066 image files in total.

## Method

1. Normalised every stem (strip stacked image extensions, lowercase to alphanumerics) and
   matched against pool titles normalised the same way, deluxe winning title ties (B5-0320).
2. Exact matches copied 1:1 as `<id>.<true-ext>` into `investigations/card-images/sorted/`.
3. The 27 pool titles with no exact match were investigated: filename probes, then reading
   the candidate card faces (18 images read, all legible, none guessed).
4. Results recorded in `sorted/MAPPING.json` (`mapping`, `verified_aliases`,
   `verified_alias_titles`, `unmatched_files`, `nonimage_skipped`,
   `pool_titles_with_no_scan`).

## Results

- **419 exact normalized matches**, zero title-key collisions, copied 1:1.
- **17 more titles resolved by reading card faces (18 files):** 8 bare `X Fleet.jpg.gif`
  scans all read "Minbari Fleet" on the face (the VASSAL convention: the Minbari printing
  carries no race suffix while Centauri/Human/Narn do); 5 filename typos read the correct
  pool title on the face (`Moral Quandry`, `Personnal Enemies`, `Vital Interest`,
  `Peace in Our Times`, `Defense of Depth`); `Reserve Fleet (Human)` reads "Reserve Fleet"
  / Human Fleet; `Strike Fleet (Narn)` reads "Strike Fleet" / Narn Fleet;
  `Level the Playing Field.jpg.gif` reads the pool's `Level the Playing Field 3+9` event;
  two `Psi Corps Intelligence` arts (Bester, Psi) share one pool record — clearest (Psi,
  33466 bytes) is primary, Bester suffixed `_2` per the intake README.
- **sorted/ holds 437 id-named scans covering 436 of 446 pool titles** (437 = 419 + 16
  aliases + 2 psi arts).
- **10 pool titles have no scan anywhere in the folder:** Changing Opinion, Compatible
  Goals, Condemn Deportations, Emperor Turhan, Expeditionary Fleet (Human), Exploration,
  Judgment by Success, Knowledge is Power, Non-Aggression Pact, Unrecognized Data.
- **1647 files unmatched and listed, not copied:** expansion/virtual/promo/token images
  outside the Premiere-plus-Deluxe pool (spot-verified). Not forced.
- Skipped non-images: `README.md`, `Thumbs.db`. Originals byte-identical; intake README untouched.

## Observations for B5-0927 (not findings against the pool)

- Printed card text visibly diverges from the authored paraphrase layer on the very cards
  read here (e.g. Moral Quandary, Personal Enemies, Peace In Our Time, Defense in Depth).
  That is the B5-0654 design-layer situation, not a matcher defect — but B5-0927's diff
  should expect large per-card deltas, not cosmetic ones.
- Printed titles can differ from pool titles (`Defense of Depth` filename vs
  `Defense in Depth` face vs pool title; pool `Level the Playing Field 3+9` vs printed
  `Level the Playing Field`). B5-0927 must key strictly on the id filename, never on
  re-derived titles.

## Verification

- `ledger-query.ps1`: B5-0929 reads 7 pipes / doubleLead no / DONE.
- `run-dup-census.ps1`: pre-existing `B5-0833 x2` still the only duplicate (foreign rows,
  lines 976/978, untouched); own rows single.
- Arithmetic: 436 covered + 10 no-scan = 446 pool. 437 files = 419 + 16 + 2.
- No compile gate run: no `src` touched (report-only + file-copy scope).

## Reusable lesson

One-line version filed under `.agent/PATTERNS/muse-spark-opencode-02/`: normalise-then-match
resolves the bulk, but the residue needs eyes — filename typos and race-suffix conventions
are only provable by reading the card face, and the read also catches title drift the
matcher would otherwise bake in.
