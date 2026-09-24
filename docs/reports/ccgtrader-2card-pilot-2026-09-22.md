---
document:
  title: "CCG Trader 2-card pilot (Google Lens oracle)"
  status: "Report (observations only, no authority)"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm:
    - {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-22"
  last_modified_date: "2026-09-22"
---

# CCG Trader 2-card pilot (Google Lens oracle) — 2026-09-22

**Origin.** User-supplied manual pilot via "Search this image with Google Lens"
on 2026-09-22. Two Premiere cards with CCG Trader page + image URLs and Lens
OCR text. Stored as **report-only**: observations, no authority, no JSON or
source edits. Follows
`investigations/b5-ccgtrader-premiere-crawler-2026-09-22.md` (the crawler
proposal intake).

**Live verification this session.** Direct fetches of both card pages returned
empty bodies (consistent with the intake finding: Gatsby client-rendered pages
expose no server-side card content). The page_URL/image_URL pattern below is
therefore **user-corroborated, not independently re-fetched**. URL shapes match
the proposal exactly (`/card/<numeric-id>/<slug>` + `api.ccgtrader.co.uk/_/assets/...`).

## Card 1 — Adira Tyree

- page_URL: `https://www.ccgtrader.net/card/378076/adira-tyree`
- image_URL: `https://api.ccgtrader.co.uk/_/assets/frq4idvqa3488c0c`
- Lens OCR (user-supplied): "Adira Tyree / 03 / Centauri Character / Every time
  Adira attacks or is attacked and is not neutralized, she gains +1 Intrigue. /
  Adira Tyree is a dancer and Centauri slave. She may attempt to steal important
  political secrets to win her freedom from her Golian master, Trakis. / 5"
- Candidate cost bubble: **5** (trailing number, lower-right position per
  rulebook §Cost bubble).
- Dataset today (`premiere.json` `char_adira_tyree`): diplomacy 3 (matches "03"),
  intrigue 4, psi 0, leadership 1, text "While Adira Tyree is in your Inner
  Circle, you may look at one opponent's hand once per round."
- **Mismatch:** game text differs completely (scan: +1 Intrigue on
  attack/being-attacked; dataset: Inner Circle hand-look). Flavor text in the
  scan is absent from the dataset (expected — dataset carries game text only),
  but the game-text divergence is a real authenticity flag.

## Card 2 — Du'Nar

- page_URL: `https://www.ccgtrader.net/card/57794/dunar`
- image_URL: `https://api.ccgtrader.co.uk/_/assets/evqdryy6lc00wk4s`
- Lens OCR (user-supplied): "Du'Nar / 07 / 04 / 14 / Narn Character / Member of
  the Kha'Ri. / If Du'Nar is injured but not neutralized, remove 1 damage token
  from him during each ready round. / Members of the First Circle of the Kha'Ri
  led the insurgency against Centauri oppression. ... / 11"
- Candidate cost bubble: **11** (trailing number).
- Dataset today (`premiere.json` `char_dunar`): diplomacy 2, intrigue 4, psi 0,
  leadership 2, text "Narn character. While in Inner Circle, your Intrigue
  conflicts gain +1."
- **Mismatches:** (a) game text differs completely (scan: heal-1-when-injured;
  dataset: Inner Circle Intrigue +1); (b) stats diverge (see human reading
  below — dataset 2/4/0/2 does not match the scan).
- **Human by-eye reading (2026-09-22, user): Du'Nar is Diplomacy 7, Intrigue 4,
  Leadership 4, Cost 11.** This resolves the OCR line fully: "07" = Diplomacy 7
  (leading zero), "04" = Intrigue 4, "14" = Leadership icon "L" + 4 misread as
  "1" + "4". No Psi value shown (consistent with Psi 0 being omitted from the
  stat row — same reason Adira's OCR shows only "03").
- **OCR rule recorded for the vision stage:** a Leadership value > 10 is really
  an "L"-glyph + digit — apply Leadership = OCR_value − 10. Generalize: stat
  OCR must be icon-aware (D/I/Psi/L prefixes), never bare integers; leading
  zeroes are padding ("07" = 7).

## ChatGPT cross-check — Adira Tyree (2026-09-22, user-supplied output)

Prompt used: the icon-aware extraction prompt (D/I/Psi/L prefixes, L-glyph
minus-10 rule, zero-stats omitted, cost from orange lower-right bubble).
ChatGPT returned MEDIUM confidence with `review_required: true`:

- intrigue 3, diplomacy/psi/leadership null (= 0 blanks); cost 5, bubble true.
- game_text: "After every time Adira Tyree attacks or is attacked without being
  neutralized, she gains +1 Intrigue." — core matches the Lens OCR ("Every time
  Adira attacks or is attacked and is not neutralized, she gains +1 Intrigue");
  wording differences are OCR-level noise, not a different ability.
- flavor_text: "Adira Tyree is a dancer and a Centauri slave. Earth has good
  relations with the League." — first sentence matches Lens; the second
  ("Earth has good relations with the League") contradicts Lens ("...steal
  important political secrets to win her freedom from her Golian master,
  Trakis") and reads like another card's text or a hallucination. Unresolved.
- **Method caveat (ChatGPT's own note): the browser extraction did NOT expose
  the `api.ccgtrader.co.uk/_/assets/...` image URL, so no scan was directly
  read; stats were cross-checked against an unnamed independent B5 CCG database
  (D0/I3/P0/L0/cost 5). That database is unverified and must NOT be treated as
  scan truth.**
- **Stat conflict surfaced:** ChatGPT (via third-party DB) says Intrigue 3,
  Diplomacy 0; dataset says Diplomacy 3, Intrigue 4; Lens showed a bare "03"
  we assumed was Diplomacy 3 — it may instead be Intrigue 3. Adira's stat row
  still needs a by-eye read of the actual scan.
- **Corroborated by two oracles:** cost bubble 5 (Lens + ChatGPT agree) and the
  +1-Intrigue-on-attack game-text core (Lens + ChatGPT agree). Both still
  contradict the dataset's Inner-Circle hand-look text.

## Direct scan reads (2026-09-22, Muse Spark — both asset URLs fetched, images read)

Both `api.ccgtrader.co.uk/_/assets/...` URLs are hotlinkable with no auth
(single polite fetches, evaluation only — not a bulk run).

**Adira Tyree** (`frq4idvqa3488c0c`): ONE stat bubble, left edge, blue **"I 3"**
= Intrigue 3. No D/Psi/L bubbles printed (all zero, omitted). Cost bubble
orange, lower-right: **5**. Game text: "Every time Adira attacks or is attacked
and is not neutralized, she gains +1 Intrigue." Flavor: "Adira Tyree is a
dancer and Centauri slave. She may attempt to steal important political secrets
to win her freedom from her Golian master, Trakis." Ground truth:
**D0/I3/P0/L0, cost 5.** Verdicts: ChatGPT's stats (0/3/0/0) CORRECT; its
flavor second sentence ("Earth has good relations with the League")
HALLUCINATED — Lens had it right. Dataset (`char_adira_tyree` 3/4/0/1 +
hand-look text) wrong on diplomacy, intrigue, leadership, AND game text.
The Lens bare "03" was Intrigue, not Diplomacy — the original assumption is
corrected.

**Du'Nar** (`evqdryy6lc00wk4s`): three stat bubbles, left edge — green **"D 7"**,
blue **"I 4"**, orange-brown **"L 4"** (the L glyph is visually unambiguous at
full size; the "14" misread is purely a low-res OCR artifact). No Psi bubble
(Psi 0, omitted). Cost bubble orange, lower-right: **11**. Game text: "Member of
the Kha'Ri. If Du'Nar is injured but not neutralized, remove 1 damage token
from him during each ready round." Flavor: Kha'Ri/First Circle text ending
"...leaders of the Narn Regime." (Lens's "Nam Regime" was a misread of
"Narn".) Ground truth: **D7/I4/P0/L4, cost 11** — the human by-eye reading is
CONFIRMED in full.

**Layout rules now grounded (not hypothesized):** only non-zero stats print, in
D/I/Psi/L order top-to-bottom; bubble colors Diplomacy-green, Intrigue-blue,
Leadership-orange; cost bubble orange lower-right (same orange family as
Leadership — position, not color, disambiguates); "Member of the Kha'Ri."
 style trait line belongs to game text above the divider, lore below it.

## Saturday Night RPG database — verified live (2026-09-22, Muse Spark)

User asked whether the DB ChatGPT cited
(`saturdaynightrpg.co.uk/index.php?pageName=Cardgames&type=B5CCG&var=B5CCG`)
is usable for card information. Fetched live this session: YES —
full server-rendered table, no JS needed. Schema: KeyID, Name, Rarity, Type,
SubType, Diplomacy, Intrigue, Psi, Leadership, Military, Cost, BoldText,
EffectsText, 7 mark columns, FlavorText, Set, Race, ShowInFilter.

- Adira Tyree (KeyID 33): D0/I3/P0/L0/M0, **cost 5**, +1-Intrigue text, Trakis
  flavor — matches the scan exactly. (ChatGPT's "independent database" claim
  corroborated; its Earth/League flavor sentence came from elsewhere, not
  this DB.)
- Du'Nar (KeyID 416): **D7/I4/P0/L4**/M0, **cost 11**, heal text, "Narn Regime"
  flavor — matches the scan exactly.
- Both DB records agree with the scans on every contested field, including
  both cost bubbles. Integer code columns still need decoding (Adira
  Set=4/Race=4 vs Du'Nar Set=1/Race=3 — Race 3=Narn, 4=Centauri likely; Set
  codes unresolved).
- No `robots.txt` on the host (404) — politeness (delay + User-Agent) and a
  human go-ahead still apply before any bulk pull. Fan-run host: treat as
  cross-check-grade, scans authoritative on conflict.
- Backfill implication: this DB (plain text, scrapable, no vision needed) can
  supply cost+stats+text for the pool with scan spot-checks — strictly better
  than 458 vision extractions. ChatGPT's one valid methodological point
  stands: scan = authority, DB = separated cross-check.

## What the pilot proves

1. The URL pattern works: numeric card IDs (378076, 57794) are stable keys,
   preferable to title slugs for joining to internal `id` — as the proposal
   recommended.
2. Google Lens as a manual oracle succeeds where the plain scraper fails
   (JS-rendered pages) — viable for a 5-card pilot without Playwright.
3. Cost bubbles are present and OCR-readable in trailing position (5, 11).

## What blocks bulk backfill

1. **Game-text authenticity gap — CONFIRMED by direct scan reads.** Both
  `premiere.json` game texts are wrong (Adira hand-look vs scan +1 Intrigue;
  Du'Nar Intrigue +1 vs scan heal-when-injured). Stats too: Adira dataset
  3/4/0/1 vs scan 0/3/0/0; Du'Nar dataset 2/4/0/2 vs scan 7/4/0/4. B5-0311
  never checked authenticity against scans. Cost-only backfill stays the safe
  separable step; any text/stats overwrite needs its own verification pass.
2. **Undefined vs zero.** Rulebook (`BABYLON5_CCG_RULEBOOK.md:1054`): a card
   with no orange bubble has *undefined* cost, explicitly not zero. B5-0323
   defaults absent cost to 0, so every card is currently free to recruit. If
   Adira=5 / Du'Nar=11 are correct, the engine's recruit economy is
   systematically under-priced today. Fixing that is a data task plus a
   possible `undefined`-vs-`0` model distinction — not part of this report.
3. **Stat OCR rules grounded.** Omission rule generalized: ALL zero-stats are
  omitted (not just Psi). Minus-10 L-correction confirmed as low-res artifact
  only — at full size the L glyph is unambiguous. Bubble colors: D-green,
  I-blue, L-orange; cost orange lower-right (position disambiguates from
  Leadership). No open stat questions remain on these two cards.
4. **Terms still unverified** (`/terms` HTTP 522 at last check). Manual
   few-card viewing is evaluation; scripted 458-download remains unapproved.

## Recommended next step (needs human/task seed, not done here)

Expand the manual pilot to 5 cards (the proposal's own gate), reading cost
bubble + stats by eye from the scan images, and record a cost-only table
(internal_id, ccgtrader page, bubble value or explicitly absent). No repo JSON
edits until a data-backfill task goes through the normal OPEN-claim cycle.
No `b5ccg/src/` changes from this report.
