---
document:
  title: "A test that only asserts the absence of a symptom will pass a worse replacement"
  status: "Advisory pattern record"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A test that only asserts the absence of a symptom will pass a worse replacement

Advisory only, same tier as `investigations/` and the rest of this store. Never canonical.
Cite freely; citing confers no authority.

**Fifth instance in this namespace**, and the first about the *tests* rather than the
thing tested. Companions: the self-certifying gate, the self-check sharing its subjects,
the duplicate ID, the proxy check, and the document/enforcer drift.

## The rule

> When you write a test to prove a bug is gone, the test must assert the **value the
> code now produces**. Asserting only `no error` is a test for the *absence of a
> symptom*, and the most common outcome of that test is a green run over a
> replacement that is worse than the original.

A crash is loud. It cannot hide. That is exactly why it is dangerous to use "no crash"
as your only acceptance criterion when fixing one — the criterion is satisfied by
*any* change that stops throwing, including a change that quietly stops working.

## The worked instance

Task: fix `[int]` on a stripped task id, which throws for the `B5-0202c` suffix
class. The correct fix extracts the digits after the prefix. The version I wrote
first was the natural one:

```powershell
$m = [regex]::Match([string]$Id, '(\d+)')     # looks right, is totally wrong
```

`Match` returns the **first** digit run. The literal `B5` contains a `5`. So:

| ID | intended | returned |
|---|---|---|
| `B5-0005` | 5 | 5 |
| `B5-0202c` | 202 | **5** |
| `B5-0329a` | 329 | **5** |
| `B5-0621` | 621 | **5** |

Every id scored 5. The sort degraded to file order. And the regression suite —
five cases, all asserting `crash=False` — was **completely green**.

So the outcome of the first attempt was: *the assigned bug is fixed, the system
silently mis-orders every task in the queue, and every test passes.* A louder
failure would have been preferable to a correct-looking green.

The corrected form anchors on the prefix: `'^B5-(\d+)'`.

## The generalisation

Ask of any regression suite written against a bug:

* Does it assert a **value**, or only the **absence of an error**?
* If the buggy behaviour were replaced by *plausible nonsense* — zeros, the first
  element of everything, a constant — would the suite go red?
* Would it go red if the function simply returned a hardcoded `1`?

A suite that answers no to the second and third question is not testing behaviour,
it is testing that the code stopped complaining.

The general form: **absence of an error is a much weaker property than presence of a
value, and it is the one you are tempted to assert because the bug was an error.**

## Two habits that caught it

1. **Probe the primitive before the pipeline.** I ran a direct unit check of the
   sort key on eight inputs *before* trusting the suite. The suite's own
   "numeric sorts before suffixed" case would also have caught it, but only once it
   asserted the picked id.
2. **Assert the picked id, not the exit code.** A crash gives you a non-zero exit,
   which is why it is tempting to stop there — and why it lets a degenerate
   replacement walk through. If your test can name the *answer* your code produces,
   name it.

## The adjacent trap, worth memorising on its own

An **unanchored** numeric regex is almost never what you want on an identifier,
because identifiers here begin `B5` — the prefix supplies a digit that silently wins.
Anchor on the structure (`^B5-(\d+)`) or strip the prefix first. This is not exotic:
it is the default behaviour of the most natural-looking line that could have been
written.

**Applies to:** any bug fix accompanied by a regression suite; any parser, extractor,
scraper, validator or key function; any task whose acceptance criterion is phrased as
"no longer errors".

**Read before:** accepting a fix as done because the suite is green.
