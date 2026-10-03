---
document:
  title: "Card id namespace registry and collision guard (B5-0311 C3 / P6)"
  status: "Proposal (candidate, never truth until merged + compiled)"
provenance:
  author_llm: {name: "Hermes (stealth-space-bunny-alpha) 2343", version: "stealth-space-bunny-alpha"}
  assessor_llm: []
  last_modified_by_llm: {name: "Hermes (stealth-space-bunny-alpha) 2343", version: "stealth-space-bunny-alpha"}
  created_date: "2026-10-03"
  last_modified_date: "2026-10-03"
---

# Card id namespace guard — registry format + collision check

Implements proposal **P6** from B5-0311 finding C3 (P2/MEDIUM), recorded in
`.agent/REPORTS/2026-09-21-solar-pro4-B5-0311.md` §5. That report measured the
gap and this document drafts the two things it asked for: the **registry
format** and the **collision check**. It proposes no edit to any card JSON and
no edit to `b5ccg/src/`; both are staged behind the acceptance criteria below.

Docs-only pass, per the B5-2143 row scope ("docs/proposals only, no src or data
edits, no commit").

## 1. What C3 found, and what has changed since

C3 recorded: no cross-file id uniqueness invariant, no documented id prefix
convention, and no id → set manifest. The `de_` prefix was observed to be
avoiding collision by accident rather than by rule.

Re-measured 2026-10-03 against `b5ccg/resources/cards/`:

| Measurement | Value |
|---|---|
| `premiere.json` cards | 446 |
| `deluxe.json` cards | 383 |
| duplicate ids within `premiere.json` | 0 |
| duplicate ids within `deluxe.json` | 0 |
| **cross-file id collision** | **0** |
| deluxe ids carrying the `de_` prefix | 383 (100%) |
| premiere ids carrying the `de_` prefix | 0 |
| deluxe ids whose `de_`-stripped form is a premiere id | 325 |
| `set` field values | `PREMIERE` (446), `DELUXE` (383) — single-valued per file |

So the status quo is **clean and stricter than C3 assumed**: `de_` is not
partial. It is applied to *every* deluxe id, including the 58 cards that have
no premiere id counterpart. C3 described the prefix as covering "the same
logical card where a premiere counterpart exists"; measurement shows it covers
the whole file. The gap is therefore purely documentary — the invariant holds,
nothing enforces or records it.

## 2. Why the invariant is load-bearing (measured, not assumed)

Three call sites make a cross-file id collision a real defect rather than a
tidiness matter.

1. **Copy-limit accounting is keyed by id.** `DeckLoader.java:112-116` counts
   deck copies into an `idCounts` map and `:129-147` raises a rulebook
   "maximum 3 copies" problem off that map. Two distinct cards sharing one id
   would have their copy counts **summed**, so a legal 2+2 split would report as
   4 and reject a legal deck.
2. **Rarity lookup on the max-copy path is by id.** `DeckLoader.java:134-139`
   finds a sample card by `getId().equals(...)` to test `FIXED` rarity. Under a
   collision the sample is whichever card appears first, so the
   `FIXED`-exempts-the-limit branch can be decided by the wrong card.
3. **Starter decks resolve by id.** `b5ccg/resources/decks/premiere-starter-decks.json`
   references **146 distinct card ids**, all of which resolve against
   `premiere.json` today (0 unresolved, 0 of them `de_`). An id collision would
   make a deck entry resolve to the wrong card or to nothing.

Note the asymmetry this creates: the starter decks are **100% premiere
(bare-id)**. `DeckLoader.loadBothSets()` (`:190-199`) builds the pool by
**title**, not by id — it keeps all 383 deluxe cards plus the 63 premiere cards
never reprinted (446 total, confirmed). So the pool de-duplication and the
starter-deck references use **different keys**, and only the id side is
exposed to a collision. The `de_` prefix is the only thing standing between a
future third set and case 1 above.

## 3. Registry format (proposed)

A new file `b5ccg/resources/cards/index.json`, one object per set, plus the
declared prefix:

```json
{
  "schema_version": 1,
  "sets": [
    {
      "file": "premiere.json",
      "set": "PREMIERE",
      "idPrefix": "",
      "cardCount": 446,
      "idPrefixRequired": false
    },
    {
      "file": "deluxe.json",
      "set": "DELUXE",
      "idPrefix": "de_",
      "cardCount": 383,
      "idPrefixRequired": true
    }
  ]
}
```

Field semantics:

* `idPrefix` — the mandatory token every id in that file must begin with.
  Empty string means "bare ids", and is a **positive declaration**, not an
  absence of information. This is the field C3 found missing.
* `idPrefixRequired` — when `true`, an id without the prefix is a hard error
  even if globally unique. Rationale: a global-unique-but-unprefixed deluxe id
  would pass a collision check and then break the moment a premiere counterpart
  is added, which is the exact failure mode C3 describes.
* `cardCount` — a cheap cross-check. Measured against the files on
  2026-10-03: 446 and 383.

Deliberately **not** in the registry: a list of individual ids. It would be a
second source of truth that drifts from the card files, and every invariant it
could express is already checkable against the card files themselves. The
registry declares the *rule*; the card files remain the only *data*.

## 4. Collision check (proposed)

A read-only checker, run on demand, that fails on either of:

**C-NAMESPACE-1 — cross-file id collision.** The same id string in two
different `cards/*.json` files. Severity: error. Today: **0 occurrences**.

**C-NAMESPACE-2 — prefix violation.** An id in a file whose `idPrefix` is
non-empty does not begin with it, or an id in a file whose `idPrefix` is empty
does begin with a token that another registered set claims. Severity: error.
Today: **0 occurrences** (383/383 deluxe prefixed, 0/446 premiere prefixed).

Recommended placement, in preference order:

1. `b5ccg/src/b5ccg/tools/` as a `main()`-bearing class, invoked from
   `b5ccg/compile.sh` after compilation — runs with no new tooling. Java 6,
   stdlib only, so it satisfies `AGENTS.md` §2 with no library question.
2. `.agent/tools/check-card-namespaces.ps1` — consistent with the existing
   verification battery, but it duplicates JSON parsing in PowerShell and does
   not run under `compile.sh`, so a contributor who never reads `.agent/` gets
   no check.

Placement 1 is recommended. Placement 2 is recorded as the fallback for if the
project prefers all repo gates to live under `.agent/tools/`.

### Gate placement and the known-red-battery precedent

`.agent/00_BOOT.md` §2a and the B5-1730/B5-1732 records are the reason the
"pass condition" is written as an **exit code** here rather than as output
text. A check that can pass over an empty scan is the defect class this repo
has hit repeatedly (`measure-task-cells.ps1` matched zero rows while printing a
green verdict; `validate-heartbeats.ps1` is known-red and its exit 1 is not news
about your own file). So: **exit 0 clean, exit 1 findings present, exit 2 the
card directory missing or unreadable** — and exit 2 is a false pass that must
never be read as clean. The checker must additionally print the **denominator**
(card files read, ids seen) alongside the verdict, so "0 violations" is only
meaningful next to a non-zero count.

## 5. Naming rule to document for future set authors

1. Card ids are **globally unique across all `cards/*.json`**.
2. `premiere.json` uses bare ids. `deluxe.json` uses the `de_` prefix on
   **every** id, not only those with a premiere counterpart.
3. A new set picks a short, distinct, lowercase prefix (`ex_`, `x1_`) and
   registers it in `index.json` before its cards land.
4. A prefix is never reused by a second set, and never retired while cards
   carrying it remain in the pool.
5. The prefix is a *namespace*, not a version marker. It does not change when
   a card is reprinted; the deluxe version of a reprinted card is a **new
   distinct card** with a `de_` id, which is why the two coexist in the pool by
   title and not by id.

Rule 2's "every id, not only counterparts" is the substantive addition over
C3's wording, and it is what measurement showed the data already does.

## 6. Acceptance criteria for promotion

Per `AGENTS.md` §4, this becomes truth when all of:

1. `b5ccg/resources/cards/index.json` added, values matching §1.
2. The checker exists at the chosen placement and **is proven able to fail** —
   three mutated fixtures (duplicate id across files; a deluxe id with `de_`
   stripped; a `cardCount` that disagrees with the file) each produce exit 1.
   A green verdict from an unmutated run proves nothing.
3. `b5ccg/compile.sh` green on JDK 8 with `-source 6`.
4. Change logged in `docs/DECISIONS.md`.
5. Claim released.

Not required, and explicitly out of scope here: a `cards/README.md` (C3's
literal first suggestion). `index.json` plus this document carries the same
information in a machine-readable place, and the repo has no precedent for
prose READMEs inside `resources/`.

## 7. Open questions for a human ruling

1. **Should the guard be a build gate or an on-demand tool?** Section 4
   recommends build-gating, but `compile.sh` is currently a compile gate, and
   failing a contributor's build on a data typo is a policy choice, not a
   technical one.
2. **Should a future re-release keep the old prefix or take a new one?** Rule 5
   assumes a re-release is a distinct card. If a "Premium" edition should be
   the *same* card id with revised text, the whole namespace model changes and
   this proposal is wrong.
3. **Is `de_` the right abbreviation to enshrine?** It is terser than
   `deluxe_` and is what the data uses. Recording it makes it binding, which is
   the point, but it also makes it hard to change later.
