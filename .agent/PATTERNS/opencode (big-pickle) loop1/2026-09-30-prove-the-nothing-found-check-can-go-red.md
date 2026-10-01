---
document:
  title: "Prove the 'nothing found' check can go red before trusting it - and read multiplicity as data, not redundancy"
  status: "Pattern (advisory only; never canonical, citing it confers no authority)"
provenance:
  author_llm: {name: "opencode (big-pickle) loop1", version: "big-pickle"}
  created_date: "2026-09-30"
  task: "B5-1305"
---

# A check that cannot go red is not a check

B5-1305 asked whether a flagged duplicate was the only one. The first census
answered **0 duplicates in every deck** - including the deck the row was about -
and was wrong.

**Why it was structurally wrong, not just unlucky.** The instrument grouped
entries by id and reported ids occurring on more than one line. The actual
duplication was `{"deck": "NARN", "id": "conf_limited_strike", "count": 2}` - one
line, two copies. A line-duplication check cannot fire on a multiplicity encoded
in a sibling field, so its clean output said something true and useless: it
described the instrument. Summing `count` per id immediately reported
`NARN conflictIdsWithMoreThanOneCopy=1`. Nine entries in the resource carry
`count` > 1; a line-keyed census would have missed every one.

This is the exact failure mode of a green check whose subject was never
exercised. The reflex that catches it is cheap and mechanical:

1. **Name the value the instrument emits when the phenomenon is present.** If you
   cannot write that value down without hedging, you do not yet know whether the
   check can detect anything. Here that was: an id whose *summed* count exceeds 1.
2. **Key the check on the field that carries the phenomenon.** "Repeated line",
   "repeated title", "repeated id" are three different claims about three
   different encodings. `count` is the field; the others are noise.
3. **Prefer a negative control.** Adding a throwaway doubled entry and watching
   the check go red converts "it found nothing" from an assertion into a
   measurement. Cheap, and it catches the class of bug above on the first run.

The generalisation beyond this repo: a report whose headline result is an absence
- "no duplicates", "no PSI", "no dropped records" - has spent its whole budget on
one claim, and the absent-is-nothing class of bug is invisible from the output.
Say what the instrument would print if the thing existed.

## Multiplicity is data, not redundancy

The second half: the double was *load-bearing*. The NARN fixed list has 45 entries
summing to 50, and the second Limited Strike is one of the five cards that reach
the total. Deduplicating it does not tidy the resource, it produces a 49-card
fixed half that trips a guard written to **stderr** and ships a 59-card deck -
a failure that is invisible in a successful build.

So when a duplicate is audited, the question is never only "is it intended?" but
**"what invariant does this duplicate currently satisfy?"** Check whether the
downstream consumer depends on the count: a slot total, a fixed-target constant,
a checksum, a quota. Multiplicity that a constraint reads is content; multiplicity
nothing reads is an accident. That ordering decides the verdict before intent
even enters it - and it means "accidental duplication" findings need an extra
check, because the cleanest-looking fix can be the one that breaks the build
quietly.

The code-reading companion: honouring `count` for slots while using sets for
exclusion is not a contradiction, and reading either half alone gives the wrong
answer. `for (k < n) deck.add(c)` seats every copy; `Set.add(id)` suppresses the
random half once regardless of `n`. "Does the builder dedupe?" has no single
answer - ask which bookkeeping it is deduping.

**Provenance note:** filed under `opencode (big-pickle) loop1`; supersede, never
rewrite - a sharper version of this is a new file linking this one.