---
document:
  title: "Engine error-handling conventions: throw, default, drop"
  status: "Proposal"
provenance:
  author_llm: {name: "Hermes (stealth-space-bunny-alpha) 2229", version: "stealth-space-bunny-alpha"}
  assessor_llm: []
  created_date: "2026-10-02"
  last_modified_date: "2026-10-02"
---

# Engine error-handling conventions: throw, default, drop

A proposal, per `AGENTS.md` section 3. It confers no authority and changes no
code. Every line number below is a **working-tree** reading of
`b5ccg/src/b5ccg/engine/DeckLoader.java` taken on 2026-10-02, against a file that
is modified and uncommitted (680 lines in the working tree, 526 at `HEAD`); see
"Line numbers" at the end before citing any of them.

## What this proposes

Three named mechanisms, one rule for choosing between them, and a fourth
mechanism the codebase already uses that has no name. The loader already
implements all of them correctly and *consistently in intent*; what it lacks is a
stated convention, and the absence is why two contradictions survive in a
validator that four separate censuses read from source and did not report.

## The three mechanisms, with witnesses

### 1. THROW — absence is unrecoverable; fail loudly and do not construct

`validateFields` (`DeckLoader.java:477`-`:624`), called from `buildCard` at
`DeckLoader.java:402`, throws `IllegalArgumentException` on a missing field:

| Required group | Witness | Cardinality |
|---|---|---|
| `id`, `title`, `type` | `:479`-`:484` | every record |
| CHARACTER's five | `:564`-`:570` | 161 records |
| FLEET `military` | `:571`-`:575` | 80 records |
| CONFLICT two | `:576`-`:582` | 108 records |
| AGENDA two | `:583`-`:589` | 47 records |
| AFTERMATH one | `:590`-`:594` | 117 records |
| CONTINGENCY three | `:595`-`:601` | 0 records today |
| ENHANCEMENT five | `:602`-`:608` | 78 records |
| LOCATION one | `:609`-`:613` | 21 records |

There is a second throwing helper with a different name and the same intent:
`req(m, key)` (`:628`-`:632`), which throws `"Missing required field: " + key`
with no record id. `buildCard` calls it for the three always-required fields at
`:385`-`:387` — so `id`/`title`/`type` are checked **twice**, once by `req` and
once by `validateFields`, and the first check throws a message that cannot name
the offending record.

### 2. DEFAULT — absence is legal and the default is the intended value

`getOrDefault(m, key, def)` (`:634`-`:637`). The common six at `:388`-`:393`:

    subtype -> ""        rarity -> COMMON     faction -> ANY
    set     -> PREMIERE  imageKey -> id       text    -> ""

Every one of these six is present on **829 of 829** records, so the mechanism is
unreachable from shipped data — but it is the mechanism a *future* data edit
meets, and `text -> ""` is the dangerous one: an absent `text` renders as a blank
rules box with no stderr line and no dropped card (recorded by B5-2091 §F3 and
B5-2071).

A third default shape, `intVal` (`:639`-`:643`), defaults on **parse failure** as
well as on absence: `if (v == null) return def;` then
`catch (NumberFormatException e) { return def; }`. So `"cost": "abc"` and an
absent `cost` are indistinguishable downstream. A fourth, `boolVal`
(`:645`-`:650`), defaults on absence and coerces anything that is not
case-insensitively `"true"` to `false` — a typo'd boolean is silently `false`,
not silently `true`, which is the safer of the two but is still silent.

### 3. DROP — the record is discarded and the pool is one card smaller

The per-record `catch` in `parseCards` (`:278`-`:281`) logs one stderr line,
`"Skipping card, parse error: "`, and **does not add the card** (`cards.add(c)`
at `:277` is inside the `try`). A `throw` from `validateFields` therefore does
not fail the load; it *quietens* it to one line on stderr and removes a card from
every future game.

**This is the mechanism with no name.** Nothing in the file says a card was
dropped; the pool simply arrives shorter. Measured on the shipped corpus today:
**0 of 829 records are dropped** — I re-derived the throw set from both JSON
files against both switches and every required field is present — so the
mechanism is live and untripped.

### 4. The unnamed fourth: LOG-LOUD-AND-CARRY-ON

Two sites, and they are the codebase's own precedent for the right answer:

* `parseConflictType` (`:669`-`:678`), adopted under B5-1047. Its comment at
  `:662`-`:668` names the exact failure it replaced: the bare `valueOf` *"threw
  IllegalArgumentException on any unexpected value, dropping the card at load
  with only a stderr line."* The replacement logs loudly, names the record id
  **and** the bad value, and falls back to a playable `DIPLOMACY`.
* `Participation.parse` (`:263`-`:267`) — a malformed fragment is logged loudly
  and participation is left open.

Neither throws. Both are the deliberate exception to rule 1, and both carry a
comment saying why. **A convention that admits no exception is not a
convention; these two are the exceptions, and they are already documented as
exceptions.** The proposal's only substantive content is to name the shape so
that the next agent adding a third one is choosing rather than improvising.

## The rule

> **Throw when absence means the record cannot be represented. Default when
> absence means "no modifier" and the default is the designed state. Log loudly
> and carry on when absence would otherwise destroy a card a player paid for.
> Never let a throw reach a pool boundary and become a silent drop.**

Three corollaries that fall out of the rule and are the parts most likely to be
useful:

* **A throw that crosses a loop boundary must be counted.** `parseCards` catches
  `Exception` per record (`:278`). Every throw in `validateFields` is therefore
  inside that catch, so "throw" and "drop" are the same mechanism wearing two
  names, and the loader's own error surface is one stderr line per lost card.
* **Default and drop are not opposites.** A default is recoverable and
  observable-in-the-value; a drop is recoverable and observable in *nothing*.
  A field can be defaulted by one reader and thrown by another in the same
  build — `military` is exactly that case (below).
* **Announce a default at the boundary once, not at every read site.** Today
  `cost` is read by `getOrDefault`-shaped code at `:247`-`:255` and by
  `Card.getCost()` (`model/Card.java:67`) across **22 call sites** — 4 in `ai/`,
  9 in `engine/` (including `RulesEngine.promotionCost:235` and
  `baseRecruitCost:404`), 8 in `ui/`, 1 declaration; 446 of 829 records read it
  as 0 by default, which renders as *cost-less* rather than as *free* because
  both UI sites append `" — Cost: "` only when `getCost() > 0`
  (`HandPanel.java:252`-`:253`, `GameBoardPanel.java:1032`-`:1033`).
  (B5-2093 §F4 records 14 call sites; the working tree measures 22. Stated as a
  measurement with its own receipt rather than inherited.)

## F1 — `validateFields` contradicts itself: the expected-set switch and the
## required-fields switch are two hand-maintained lists of the same facts

`expected` is built at `:488`-`:490` from eight common names, then extended by
the switch at `:493`-`:560`. A **second, separate** switch at `:563`-`:616`
declares which fields are *required* and throws on their absence. The two lists
must agree; nothing makes them agree; and on the current tree they **do not**,
in three places:

| Field | Required by switch 2 (throws) | Added to `expected` by switch 1? | Result |
|---|---|---|---|
| `id`, `title`, `type` | `:479`-`:484` | **no** | every record |
| AGENDA `isMajorAgenda`, `winCondition` | `:583`-`:589` | **no** | 47 records each |
| AFTERMATH `triggerCondition` | `:590`-`:594` | **no** | 117 records |

The `AGENDA` and `AFTERMATH` cases are the *same defect as the first one and in
the same method*, and they are not covered by the fix that has been proposed for
the first one. A fix that adds the three always-required keys to the `expected`
literal at `:488` would silence 2,487 of the 2,698 lines and leave 211 firing
on records that are **perfectly legal** — an `AFTERMATH` carrying the
`triggerCondition` the contract requires is simultaneously the required field and
an unknown field.

**Measured, not inherited.** B5-2071 §F1 measured this at 2,487 lines per pool
load, attributing all of them to the three always-required keys. I re-derived it
from both JSON files against both switches and the figure is **2,698** — the
extra 211 are the `AGENDA` and `AFTERMATH` cases above, which B5-2071's
measurement (correctly, for its own question) did not model. The per-type
breakdown of the 2,698:

    EVENT 498 | CHARACTER 483 | AFTERMATH 468 | CONFLICT 324 | FLEET 240
    AGENDA 235 | ENHANCEMENT 234 | GROUP 153 | LOCATION 63

and by key: `id` 829, `title` 829, `type` 829, `triggerCondition` 117,
`isMajorAgenda` 47, `winCondition` 47. Six keys, 2,698 lines, and **all six are
fields the contract itself requires on the records that carry them.**

**The generalisation, which is the actual point of this proposal:** a validator
that announces required fields must derive the announcement from the same
declaration that enforces them. Two hand-maintained lists of one fact will drift,
and when they drift the validator's happy path emits an error per required
field — at which point the signal is buried in 2,698 lines of noise on every
`loadBothSets()` (`:190`-`:199`), which is called on every game start, and a
validator that cannot report a finding is not reporting anything. B5-2071's
reusable lesson already names the symptom; the cause is structural, and no
one-line fix addresses it.

The structural fix is to build `expected` from the required-lists themselves
rather than restating them: one table of `{type -> required[], optional[]}`,
used by both switches, with `expected` derived as `BASE + required + optional`.
That is an `engine/` edit and is **not** proposed for application here.

## F2 — one field, two mechanisms, and the default is non-obvious

`military` is **required** for FLEET and **optional** for LOCATION:

* FLEET: absent -> `validateFields` **throws** at `:572`-`:574` -> `:278` drops
  the card.
* LOCATION: absent -> **legal**; it is added to `expected` at `:556` and read
  with a default at `:459`, `intVal(m, "military", 1)`.

So the same key, same file, same method, is unrecoverable on one card type and
silently defaulted to **1** on another. B5-2093 §F2 recorded that a fleet that
slipped past validation would read 1 through the build path and 0 through
`FleetCard.getMilitary()`'s field. That path is unreachable today because
validation runs first (`:402`), and it stays unreachable **only because switch 2
runs before switch 1's consumers** — an ordering property of one method, not a
documented invariant.

Coverage makes the asymmetry visible rather than hypothetical: `military` is
present on 80 of 80 FLEET records and **0 of 21 LOCATION** records. Every
location's military is 0 by default, so `LocationCard.getGreatestAbility()`
returns 0 for all 21. That is legal and B5-2091 declined to call it a defect; the
convention point is that a reader cannot tell "0 by data" from "0 by default"
without counting the records, which is why optional-field coverage should be
censusable **as a rate per type** (B5-2093's lesson) and not assumed uniform.

## F3 — a stale comment names a violation that no longer exists

`DeckLoader.java:529`, in the EVENT case:

    // Forbidden: timing (one record has it, de_event_armistice - will be reported as unknown)

**No record in either file carries a `timing` key** — 0 of 829, verified this
pass. The record named in the comment, `de_event_armistice`, does exist; the key
does not. A comment stating a live violation in the present tense sends the next
reader to verify a defect on a clean record. Same class as B5-2117 F2 and
B5-2119's, arriving from the other side: there the *finding* went stale, here the
*documentation of a finding* went stale and the data was fixed silently. Editing
it is an `engine/` edit, **not** done here.

## F4 — the census that cannot fail is the one worth generalising

Three sibling censuses (B5-2069 CONFLICT, B5-2091 LOCATION, B5-2093 FLEET) each
graded *the data* against *the contract read from source*. All three returned
**CLEAN**, and all three are correct. None asked the second question B5-2071
asked — "does a perfectly legal record leave the validator silent?" — and if any
of them had, it would have found F1 immediately.

This is worth stating as a convention because it is a *method* rule rather than a
code rule, and it is the same shape as the three mechanisms above applied to
instruments: **an instrument that cannot fail on the correct input is not an
instrument.** A census that asserts absence is impossible proves nothing; the
negative control belongs to the census, not to the corpus.

## Line numbers

Working-tree readings of `b5ccg/src/b5ccg/engine/DeckLoader.java` on
2026-10-02, taken with `sed`/`grep` and read back before citing. The file is
**modified and uncommitted** — 680 lines in the working tree against 526 at
`HEAD` (`git show HEAD:b5ccg/src/b5ccg/engine/DeckLoader.java | wc -l`), and
`git diff --numstat` reports **154 insertions, 0 deletions** — so all four
upstream reports' anchors (B5-2069, B5-2071, B5-2091, B5-2093, which cite
`validateFields` at `:405`-`:444` and the drop catch at `:206`-`:209`) are
**stale by roughly 70 lines** against this tree. The mechanisms they describe
are all still present and all still real; the numbers are not. Re-verify before
citing, which is what this pass did.

## What this proposal does not do

* It applies nothing. `expected` is not fixed, `:529` is not corrected, no
  field's mechanism is changed.
* It proposes no value for any field — what an AFTERMATH should cost, whether an
  ambassador is free, what a location's military should be. Those are rulebook
  and data questions owned by other rows (B5-2101, B5-2103, B5-2133, B5-2000).
* It does not touch `b5ccg/src-java8-archive/`, which is frozen.
* It asserts nothing about conformance-test results: the build was red this pass
  (see the B5-2229 report), so no probe was compiled and no observation here is
  an executed end-to-end run. Every mechanism is a code reading; the only
  executed measurements are the JSON census over 829 records and the two file
  line counts.
