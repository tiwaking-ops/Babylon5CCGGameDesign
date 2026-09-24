---
document:
  title: "Saturday Night RPG bulk-extraction strategy (Perplexity advisory)"
  version: "1.0"
  status: "Advisory working document (not canonical, not a ruling)"
provenance:
  author_llm: {name: "Perplexity", version: "unknown"}
  assessor_llm:
    - {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-22"
  last_modified_date: "2026-09-22"
---

# Saturday Night RPG bulk-extraction strategy (Perplexity advisory)

**Purpose and Origin.** Pasted by the user on 2026-09-22 as "Perplexity says".
Original author is Perplexity (version unknown). Stored as **advisory only** per
AGENTS.md section 6 — incoming/external material goes to `investigations/`,
never canonical, no DEC inferred. Assessed and stored by Muse Spark
(muse-spark-1.3-contributor-free).

**Assessment (2026-09-22, Muse Spark).** Accepted with corrections:

- ACCEPT: 4-stage pipeline (bulk pull SNRPG → normalize → validate vs CCG
  Trader scans → backfill cost-only on agreement). Matches our governance
  (scan authority, cost-only separable step, no JSON edits without a seeded
  data task).
- ACCEPT: the six pre-bulk validations (cost semantics, Set/Race/mark decoding,
  duplicate titles, scan-wins conflict policy) and the extraction prompt's
  rules (preserve source ints, null≠0, no decoding without a mapping, no text
  cleaning, duplicate passthrough, confidence flags). Validation examples match
  our scan ground truth exactly (Adira 0/3/0/0/5; Du'Nar 7/4/0/4/11).
- ACCEPT: the legal caution (no robots.txt ≠ permission; rate limit, identify
  client, cache, terms). Consistent with our recorded position.
- CORRECTION 1 — row count: the proposal says "all 446 Premiere rows". Wrong
  number on two counts: 446 is our DEDUPED pool (382 Deluxe + 63
  premiere-only + 1 mis-set, B5-0320), not Premiere rows; Premiere itself is
  458 per CCG Trader. The SNRPG table covers ALL expansions (Crusade-era rows
  present at low KeyIDs, ~74k output lines), so scope is thousands of rows and
  the backfill task must define its row filter (our 446 pool titles), not
  "Premiere rows".
- CORRECTION 2 — verification status UPGRADED: Perplexity wrote "source pages
  could not be fetched directly from this environment... conditionally
  verified". We fetched them live this session (SNRPG table + both CCG Trader
  asset URLs, images read): server-rendered table confirmed, both DB rows
  match both scans on every contested field. Status here is
  directly-verified-by-Muse-Spark, not conditional.
- OPEN (agreed, still unresolved): Set/Race/mark code decoding; Cost=0 vs
  absent-bubble semantics against the B5-0323 absent-to-0 default (rulebook
  :1054 undefined≠0); composite-key matching (title+set+type, KeyID retained
  as external ID) against our title-dedup pool rule.
- Closest existing entries: B5-0311 (schema audit), B5-0315 (cost design),
  B5-0320 (title-dedup pool), B5-0323 (cost plumbing), and
  `docs/reports/ccgtrader-2card-pilot-2026-09-22.md` (scan ground truth + SNRPG
  verification this session).

## Original pasted material (preserved verbatim)

Yes—OpenCode's finding changes the plan. If the Saturday Night RPG endpoint is genuinely server-rendered HTML with a `Cost` column, it is the best **bulk source** for backfilling printed influence costs; CCG Trader scans become the validation layer rather than the primary extraction source.

## Revised extraction strategy

| Stage | Source | Purpose |
|---|---|---|
| 1. Bulk pull | Saturday Night RPG HTML table | Extract `KeyID`, `Name`, `Cost`, `Type`, `SubType`, stats, `Set`, `Race`, and text fields |
| 2. Normalize | Your existing database + mapping tables | Match records by title/set/type; resolve coded `Set` and `Race` values |
| 3. Validate | CCG Trader card images | Spot-check a stratified sample and all low-confidence/conflicting rows |
| 4. Backfill | Combined result | Write `cost` only where DB and scan agreement—or explicit review—exists |

This is much more efficient than vision-extracting 458+ images, while still preserving your scan-as-authority rule.

## Critical validation before bulk pull

Do **not** run the full extraction until these are decoded and tested:

1. **Confirm the cost semantics.** Verify that `Cost` means the printed orange influence-cost bubble, not a generic database field, deckbuilding cost, or internal ID-like value. The two matching examples—Adira Tyree `5` and Du'Nar `11`—are strong evidence, but test 20–30 more cards across types and factions.
2. **Decode `Set`.** Adira's `Set=4` versus Du'Nar's `Set=1` must be mapped to Premiere/Deluxe (and possibly another printing/variation). Do not assume numeric order corresponds to release order.
3. **Decode `Race`.** Map integer race codes to `HUMAN`, `MINBARI`, `CENTAURI`, `NARN`, etc.; keep unresolved codes as `UNKNOWN`, not guessed faction names.
4. **Decode the seven mark columns.** Identify whether they are rarity marks, faction marks, expansion marks, promo indicators, or card-status flags.
5. **Check duplicate titles.** The same character may occur in multiple sets or printings. Match on a composite key: normalized title + decoded set + type/subtype, and retain `KeyID` as an external source ID.
6. **Establish conflict policy.** If database `Cost` disagrees with a card scan, the scan wins; mark the DB row `review_required` and do not write a cost until resolved.

## Safe bulk-pull prompt

```text
ROLE
You are a careful data-archaeology agent extracting Babylon 5 CCG card records from a server-rendered HTML table.

SOURCE
Use only the supplied HTML/URL content. Do not invent values, infer missing fields, or use outside card databases to fill gaps.

TARGET TABLE
The source table has columns including:
KeyID, Name, Rarity, Type, SubType, Diplomacy, Intrigue, Psi, Leadership, Military, Cost, BoldText, EffectsText, seven unnamed mark columns, FlavorText, Set, Race.

PRIMARY OBJECTIVE
Extract every card row into valid JSON Lines, preserving source values exactly.

REQUIRED OUTPUT SCHEMA
Return one JSON object per card, one per line:

{"source_key_id":<int>,"title":"...","rarity_code":"...","type":"...","subtype":"...","diplomacy":<int|null>,"intrigue":<int|null>,"psi":<int|null>,"leadership":<int|null>,"military":<int|null>,"cost":<int|null>,"bold_text":"...","effects_text":"...","mark_columns":[...],"flavor_text":"...","set_code":<int|null>,"race_code":<int|null>,"raw_row":"...","extraction_confidence":"HIGH|MEDIUM|LOW","parse_issues":[]}

EXTRACTION RULES
- Preserve `KeyID`, `Cost`, `Set`, and `Race` as source integers or null; do not decode them unless an explicit mapping is supplied.
- Numeric stats must be integers or null; preserve zero as 0.
- Preserve original capitalization and punctuation in `Name`, `BoldText`, `EffectsText`, and `FlavorText`.
- If a cell is empty, use null for structured fields and an empty string for text fields.
- If duplicate KeyIDs occur, output every row and add `"duplicate_key_id":true` to each affected object.
- If duplicate normalized titles occur, output every row; do not merge them.
- Do not interpret `Cost=0` as an absent printed cost bubble.
- Do not treat an absent `Cost` value as 0; use null and add a parse issue.
- Do not map `Set` or `Race` integers to named sets/factions without an explicit mapping table.
- Do not modify, shorten, paraphrase, or "clean" card text.
- Do not use flavor text as game-rules evidence.

QUALITY CONTROL
Before writing each object:
1. Confirm every column value belongs to the correct row.
2. Confirm numeric parsing has not dropped leading/trailing characters.
3. Confirm `Cost` is separated from stats such as Diplomacy, Intrigue, Psi, Leadership, and Military.
4. If column alignment is uncertain, set extraction_confidence to LOW and describe the issue in parse_issues.

VALIDATION EXAMPLES
These two records must extract exactly as follows, with no inferred changes:

Adira Tyree, KeyID 33:
- Diplomacy 0
- Intrigue 3
- Psi 0
- Leadership 0
- Cost 5
- Set code 4
- Race code as printed/source-encoded

Du'Nar, KeyID 416:
- Diplomacy 7
- Intrigue 4
- Psi 0
- Leadership 4
- Cost 11
- Set code 1
- Race code as printed/source-encoded

OUTPUT
Return JSONL only. No Markdown, commentary, code fences, or explanatory prose.
```

## Validation sample

Before committing all 446 Premiere rows, validate at least:

- 10 Premiere CHARACTER cards across all factions.
- 5 FLEET cards.
- 5 LOCATION cards.
- 5 GROUP cards.
- 5 ENHANCEMENT cards.
- 5 non-sponsorables (Agenda/Event/Conflict/Aftermath) to confirm whether `Cost` is null, zero, or populated unexpectedly.
- Every card where `Cost` is `0`, null, negative, or unusually high.
- 10 randomly selected scans from CCG Trader, comparing database cost to the visible orange bubble.

For the two known examples, the target database values would be:

```jsonl
{"id_or_title_match":"Adira Tyree (PREMIERE CHARACTER)","title":"Adira Tyree","set":"PREMIERE","type":"CHARACTER","cost":5,"confidence":"HIGH","source":"Saturday Night RPG B5CCG database, KeyID 33; cross-checked against CCG Trader scan https://www.ccgtrader.net/card/378076/adira-tyree"}
{"id_or_title_match":"Du'Nar (PREMIERE CHARACTER)","title":"Du'Nar","set":"PREMIERE","type":"CHARACTER","cost":11,"confidence":"HIGH","source":"Saturday Night RPG B5CCG database, KeyID 416; cross-checked against CCG Trader scan https://www.ccgtrader.net/card/57794/dunar"}
```

The source pages could not be fetched directly from this environment, so treat these as **conditionally verified**: verified by your OpenCode report and the two supplied scan cross-checks, but not independently re-fetched here. The absence of `robots.txt` is not permission to scrape; use conservative rate limiting, identify your client, cache results locally, and comply with the site's terms and applicable law.
