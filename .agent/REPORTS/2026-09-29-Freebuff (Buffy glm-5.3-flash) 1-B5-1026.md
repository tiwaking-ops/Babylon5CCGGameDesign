---
document:
  title: "B5-1026 close-out — the RARE_WITHDRAWN singleton, verified end to end"
  status: "Close-out report (observation, no authority)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
---

# B5-1026 — Is RARE_WITHDRAWN real, and is the card actually excluded?

Instrument: Python json.loads over utf-8-strict bytes for the data side;
grep/line-read for the engine side; grep for the rulebook. Read-only.

## 1. Real lifecycle state, not a typo

- `Rarity.java:4` (src, Java-6 tree) enumerates `RARE_WITHDRAWN`; the frozen
  archive copy matches — the constant is deliberate at both layers.
- Corroboration: the B5-1022 adjudication already classed RARE→RARE_WITHDRAWN
  on this record as a deliberate lifecycle state ("deluxe text says
  `[WITHDRAWN — not legal for tournament play.]`"), and the authored-pool
  baseline lists it among the 5 live rarity values.

## 2. Is a withdrawn card excluded from play? Yes — but by side effect

All three deck-construction paths traced:

1. **Random starter draws** — excluded. `StarterDeckBuilder.java:153–155`
   admits candidates only where rarity is `UNCOMMON` or `RARE`. This is an
   **incidental** exclusion: no line knows withdrawal exists.
2. **Fixed starter lists** — excluded by data only: `premiere-starter-decks.json`
   has no `as_it_was_meant` entry. A fixed-list entry would be **loaded with
   no rarity check anywhere on that path**.
3. **Main.java / all headless harnesses** build via `StarterDeckBuilder`, so
   they inherit both mechanisms; `GameController` builds no decks itself.

No other production code reads `getRarity()` (HandPanel: colour only;
DeckLoader: parse only). **No card the data marks withdrawn is dealt into play
today — and no rule guarantees it.** The row's feared one-line defect does not
exist; the property exists by coincidence of a rarity whitelist plus a deck
list that lacks the entry. A withdrawn card marked `RARE` (or an added deck
entry) would enter play silently. Narrow future follow-up (unseeded per row
scope): an explicit `RARE_WITHDRAWN` exclusion at pool assembly + a
conformance probe.

## 3. Other withdrawal semantics on the record — not ambiguous

The record carries withdrawal in **two independent fields**: rarity
`RARE_WITHDRAWN` **and** the leading `[WITHDRAWN — not legal for tournament
play.]` text annotation (added in deluxe; premiere twin text has none and
stays RARE/legal under the title-dedup ruling). Cost, winCondition,
isMajorAgenda are fully specified and correctly so: the card is withdrawn from
*legality*, not from existence.

## 4. Rulebook cross-check — divergence recorded (DECISIONS entry written)

`grep -i withdraw BABYLON5_CCG_RULEBOOK.md` → **zero hits**. The canonical
reference never defines a withdrawal lifecycle; the data and enum encode a
real-world concept (the 1998 Deluxe redistribution history, per the
investigations research files). The divergence is documentation-shaped: the
record is not ambiguous, and the rulebook's *silence* is not contradicted by
anything in the data. Recorded in `docs/DECISIONS.md` per row authorisation;
nothing edited.

## Gates

compile.sh green on JDK 8 (09:16Z); run-dup-census exit 0 post-write; own row
7 pipes / doubleLead no; claim released at close-out; no JSON/src edit, no
rarity value written; no commit, no push.

## Reusable lesson

A property nobody wrote is a property nobody owns — exclusion of the withdrawn
card is real but lives in an accident of a whitelist and a data absence, and
the test of such a property is not "does it hold today" but "which line would
have to change for it to break silently."
