---
document:
  title: "B5 CCG card images and card data sources (Perplexity research intake)"
  version: "1.0"
  status: "Advisory working document (not canonical, not a ruling)"
provenance:
  author_llm: {name: "Perplexity", version: "unknown"}
  assessor_llm:
    - {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5 CCG card images and card data sources (Perplexity research intake)

**Purpose and Origin.** User-supplied Perplexity AI research (chat `c7bc9c9a-ed76-4dc9-9841-fcd99eae27e8`,
retrieved 2026-09-28) answering where to find downloadable B5 CCG card images and
structured card data. Stored as **advisory only** — not canonical, not a ruling, no
DEC inferred, no card data or source file changed. Assessed against the live repo
by Muse Spark on 2026-09-28 (see Assessment below). Original Perplexity text is
preserved verbatim in §1; assessment and repo wiring follow in §2–§3.

## 1. Original Perplexity output (verbatim, formatting lightly normalised)

Yes — the best currently known source for a **complete downloadable image set** is the old
**VASSAL Babylon 5 CCG module**. It includes seven ZIP archives of scanned card images,
plus the module itself and rules/rulings PDFs. The project explicitly says it is intended
for people who own the physical Precedence cards, which matches your situation.
[vassalengine](https://vassalengine.org/library/projects/Babylon_5_Collectible_Card_Game)

### Best source: VASSAL module

On the VASSAL module page, download:

- `B5CCG4.vmod` — the game module; contains **no card images**
- `images1.zip` through `images7.zip` — the actual card scans, totalling roughly 55 MB
- `rulings.pdf`, `b5r13.pdf`, and `enginechanges.pdf` — useful rules and errata references
  [vassalengine](https://vassalengine.org/library/projects/Babylon_5_Collectible_Card_Game)

The installation instructions say to put the module file and the image ZIPs in the same
folder, with the images placed in a folder named `B5CCG4_ext`. The module covers the main
sets and also includes the virtual sets **Anla'shok** and **The Vorlons**, plus promo
material in later updates.
[vassalengine](https://vassalengine.org/library/projects/Babylon_5_Collectible_Card_Game)

The download links are on the module's **Packages** section:

[https://vassalengine.org/library/projects/Babylon_5_Collectible_Card_Game](https://vassalengine.org/library/projects/Babylon_5_Collectible_Card_Game)
[vassalengine](https://vassalengine.org/library/projects/Babylon_5_Collectible_Card_Game)

If the page's download buttons are awkward, right-click each `images*.zip` link and choose
"Save link as…" rather than trying to save the VASSAL page itself.

### Card text and structured data

For building an electronic version, you will likely want more than images: card name, set,
rarity, card type, faction, costs, traits, and rules text.

| Source | What it provides |
|---|---|
| CCG Trader | A complete browsable database of all printed B5 CCG sets, virtual expansions, promos, and signed cards; useful as a master checklist and for card names/rarity/set structure. [ccgtrader](https://www.ccgtrader.net/games/babylon-5-ccg/) |
| Jeff Allender's nslists.com checklists | Text-based set checklists for Premiere, Deluxe, The Shadows, The Great War, Psi Corps, Severed Dreams, Wheel of Fire, and Crusade; useful for extracting card names, rarity, and set ordering. [nslists](https://www.nslists.com/b5ccg1.htm) |
| VASSAL module | Full card images plus a playable implementation; the module is the most practical bulk-image source. [vassalengine](https://vassalengine.org/library/projects/Babylon_5_Collectible_Card_Game) |
| B5 CCG Deck Builder | A community deck-builder reportedly circulated as a ZIP file via the Babylon 5 subreddit/Facebook community; it may contain card data or exportable deck lists, but the linked MediaFire file could not be verified from here. [reddit](https://www.reddit.com/r/babylon5/comments/137oha8/babylon_5_ccg_card_and_deck_manager/) |

### Practical approach

1. Download `B5CCG4.vmod` and `images1.zip`–`images7.zip` from VASSAL.
   [vassalengine](https://vassalengine.org/library/projects/Babylon_5_Collectible_Card_Game)
2. Extract the image ZIPs and inspect the filenames: they should give you a reliable mapping between image files and cards.
3. Use CCG Trader and the nslists checklists to build a canonical card database: `card_id`, `name`, `set`, `card_type`, `faction`, `rarity`, `cost`, `text`, `image_file`. [ccgtrader](https://www.ccgtrader.net/games/babylon-5-ccg/)
4. Use OCR only as a fallback for rules text; the scans in the VASSAL set are described as readable at full size, but OCR will still need manual review for game-critical wording. [vassalengine](https://vassalengine.org/library/projects/Babylon_5_Collectible_Card_Game)
5. Keep your electronic version private or restricted to verified owners, since the card artwork and game text remain Precedence/Warner Bros.-related intellectual property. The VASSAL project takes the same position. [vassalengine](https://vassalengine.org/library/projects/Babylon_5_Collectible_Card_Game)

### Other image sources

CCG Trader has card images for every set, but it is designed as a collection/trading database
rather than a bulk-download archive, so there is no obvious "download all images" function.
The VASSAL archives are therefore the best option for obtaining the complete image library in
a small number of downloads.
[vassalengine](https://vassalengine.org/library/projects/Babylon_5_Collectible_Card_Game)

A former community member also mentioned having a Dropbox folder of B5 CCG images, but that was
in an old Reddit thread and the link was not included, so it is not a dependable current source.
[reddit](https://www.reddit.com/r/babylon5/comments/k9s429/babylon_5_collectible_card_game_still_popular/)

## 2. Assessment (Muse Spark, 2026-09-28)

- **Status:** advisory, non-canonical. No DEC inferred; no source, data, or rulebook change.
- **Corroborated 2026-09-28 via live search:**
  - The VASSAL module page exists (`vassalengine.org/library/projects/Babylon_5_Collectible_Card_Game`);
    indexed snippets confirm `B5CCG4.vmod` ships **without** images and the scans live in the
    companion `B5CCG4_ext` folder — matching the report's install claim. The `images1–7.zip`
    filenames and "~55 MB" total could not be re-verified from search snippets alone (page is
    JS-heavy; direct fetch returned title only) — treat filenames as stated-by-Perplexity until
    the user opens the Packages section.
  - The nslists checklists exist (`nslists.com/b5ccg1.htm` Premiere, `b5ccg1de.htm` Deluxe,
    `b5ccg2.htm` Shadows, `b5ccgpro.htm` promos) — the report's checklist claim is confirmed.
  - CCG Trader as a browsable per-card database (no bulk download) is consistent with the
    repo's own prior experience (see `investigations/card-images/README.md` on the
    ccgtrader + lens route).
  - The Deck Builder / MediaFire / Dropbox items are explicitly unverified in the report
    itself — carry them as leads, not sources.
- **Consistency with repo:** compatible with the standing card-data policy. The repo pool
  (`b5ccg/resources/cards/premiere.json` + `deluxe.json`) is a hand-authored paraphrase layer
  frozen at known SHAs; `investigations/card-images/README.md` (B5-0821) already defines the
  intake path: drop legible images in `investigations/card-images/` named by card `id`, diff
  against the baseline, schema-gate before content, nothing written to `resources/cards/`
  without a separate claimed task. VASSAL scans would arrive through exactly that path.
- **IP note:** the report's owner-only / private-use caution is sound and matches the VASSAL
  project's own stance. Do not publish scans; keep any image set local to this repo's
  `investigations/` intake path.
- **Closest existing entries:** B5-0311 (card JSON audit), B5-0821 (card-image intake README),
  authored pool baseline `docs/reports/authored-card-pool-baseline-2026-09-28.md`.

## 3. Suggested next steps (not tasks, no claim)

1. User downloads `B5CCG4.vmod` + `images1–7.zip` from the VASSAL Packages section into a
   scratch folder (not into the repo yet).
2. Spot-check 3–5 scans for legibility at full size; confirm filenames map to cards.
3. If good, copy a first batch (one image per card, named by repo card `id`) into
   `investigations/card-images/` per its README — extraction/diff happens only after images
   land, under a separate claimed task.
4. nslists + CCG Trader stay as checklist cross-references for names/rarity/set ordering;
   OCR stays a fallback with manual review for game-critical wording.
