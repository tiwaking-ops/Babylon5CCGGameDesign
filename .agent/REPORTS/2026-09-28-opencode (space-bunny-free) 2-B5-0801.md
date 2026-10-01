---
document:
  title: "B5-0801 close-out: card-pool baseline frozen, completeness gap proven"
  status: "Report (observation, no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0801 — card-pool baseline and completeness forensics

**Row:** B5-0801, seeded on human order, closed in one session.
**Deliverables:** `docs/reports/authored-card-pool-baseline-2026-09-28.md` (baseline +
acceptance spec), this report, one pattern, one DECISIONS entry.
**Hard stop observed:** no printed card text fetched, scraped, vendored or imported; no
card JSON edited; no `src` touched; the `negative-power-split` proposal not edited; the
B5-0654 supersession not pre-empted. **No commit.**

## The error that produced this row

I told the human "0 of 829 cards carry a Power stat" **without ever running the
search** — relaying a proposal's figure as if I had measured it. The proposal's own
method was also incapable of finding the truth: §4 greps for text fields *opening with*
`power`, which cannot match a mid-sentence occurrence. Corrected figure: **13 records**
carry "power" in title or text; all 13 resolve to Influence, Leadership or card-draw.

That is the second time in this session I asserted repo state on someone else's
authority rather than my own measurement (the first was naming B5-0789, a row that
already existed). The pattern is stable enough to name: **when I am about to state a
number to the human, run the query in the same breath.** A number relayed is a claim I
have adopted, and I have now twice adopted one I hadn't checked.

## The finding: there is no card-text database here, and there never was

The human asked directly whether a full card-text database existed. It does not, and
three independent checks — all run, none cited from a prior report — say so:

| Check | Result |
|---|---|
| Does the canonical rulebook name any card? | **No.** Case-insensitive search of `BABYLON5_CCG_RULEBOOK.md` for three named cards: **zero matches**. The one canonical document carries rules, not cards. |
| Does any card dataset exist in the tree? | **No.** Only `premiere-starter-decks.json` (deck lists) and its `out/` copies. The sole external references are the rulespal rulebook URL and an unrelated Figma link. |
| Do card images exist locally? | **No.** **0** image files of any format under `b5ccg/resources`. The parse-from-images route needs images supplied. |
| Was the pool ever larger? | **No.** `premiere.json` holds **exactly 446 cards at all eight commits** that touched it, 2026-09-21 → 2026-09-28. Only whitespace and deluxe annotations move the bytes (172 731 → 174 160 chars). Nothing to recover. |

**And the text the human quoted is not in the repo.** Their A Rising Power — *"Count each
10 Diplomacy you have from ready characters you control as +1 power"* — is precisely the
shape rulebook:171 describes as a card adding points to a Power total. The repo's version
is an Influence agenda. The four other cards they named (Alliance of Races, Never Again,
Peace in Our Time, Revenge) all exist as agendas written as Influence. No card text
anywhere contains "count each" or "per 10".

## Hand-authoring evidence

110 records carry an inline `(Deluxe text change: …)` annotation. A scraped or licensed
dataset carries printed text; editorial asides about what *was* changed are the signature
of a human rewriting a card. And the deluxe set is a strict reprint subset — 383 titles
in both sets, 63 premiere-only, **0** deluxe-only, and 383 + 63 = 446 exactly. All 829
`id` values unique.

## The acceptance spec is proven, not asserted

A by-`id` comparison against the frozen baseline, on a TEMP fixture:

| Fixture | ADDED | REMOVED | CHANGED | UNCHANGED |
|---|---|---|---|---|
| byte-identical pool | 0 | 0 | 0 | **446** |
| one text edit + one `MYTHIC` rarity + one record silently dropped | 0 | **1** | **2** | 443 |

and it named the exact fields: `char_jeffrey_sinclair.text`, `char_gkar.rarity`. The
**silent deletion** and the **out-of-domain enum** are both caught and localised to a field
name. That is the reason for freezing a baseline before data arrives — a supplied pool
that quietly loses a card, or invents a rarity, is otherwise indistinguishable from a
correct one until someone notices in play.

Enum domains are enumerated in the baseline doc (type 9, subtype 59, rarity 5, faction 7,
conflictType 4, fleetClass 20, set 2, timing 1); 9 of 27 fields are present on all 829
records and are therefore required.

## What I did not decide

**The distinction that matters for the supersession:** B5-0654's **premise** — that the
authored pool faithfully represents the printed cards — is falsified by the three checks
above. Its **conclusion** — authored pool is the design layer, do not bulk-import printed
text — remains defensible as policy. A ruling failing and its premise failing are
different events, and which is being replaced is your call. I have recorded the fork
without picking a side.

Also open, and yours: whether Power ≡ Influence is permanent or per-card; and under
"permanent", whether the engine should **assert** the equivalence rather than assume it, so
a future card saying "power" cannot be silently implemented as influence. That failure is
invisible until it is played.

## Reusable lesson

**A measurement you did not take is a claim you have adopted — and someone will check.**
Twice in one session I passed a number to the human that I had taken on authority, and
both times the number was wrong in a way that would have survived a confident
presentation. The cost of the fix is one command. The pattern store already had
`a-proxy-check-passes-forever-when-it-shares-its-failure-mode` and
`a-rule-you-just-wrote-is-not-evidence-that-you-followed-it` in this same namespace; this
is the same failure one level up, where the unchecked step is *stating a fact* rather than
implementing a rule — and the tell is identical: it feels like reporting, not deciding.

## Cross-references

`docs/reports/authored-card-pool-baseline-2026-09-28.md` ·
`docs/DECISIONS.md` 2026-09-28 B5-0801 · 2026-09-27 B5-0654 (ruling under review) ·
`docs/proposals/negative-power-split-design-proposal.md` §4 (the unsound grep) ·
`BABYLON5_CCG_RULEBOOK.md:171` and `:1034`
