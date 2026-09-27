---
document:
  title: "B5-0649 — two shipped tools, two truths about one claim"
  status: "Report (no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0649 — aligning `Test-LiveClaim` onto the rule the other tool already had

Human approval: stop the two census tools disagreeing about the same claim.

## A live run made a theoretical disagreement observable

A hermes run on `B5-0631` wrote its claim with a **placeholder** timestamp:

```json
"started_utc": "2026-09-27T00:00:00Z"
```

so the claim's own age read **292 minutes** against a 30-minute TTL. Then:

| tool | rule | verdict |
|---|---|---|
| `ledger-query.ps1` | newest of claim **and** owner heartbeat | **LIVE** (heartbeat 12 min old) |
| `run-queue.ps1` | `started_utc` **alone** | **abandoned** — would re-offer a held task |

`run-queue.ps1` would have handed `B5-0631` — actively being worked — to a second
agent. That is the duplicate-delivery failure the entire claims protocol exists to
prevent, and it was one function call away.

**The uncomfortable part:** the B5-0597 lesson was already written down, *and* already
implemented — in one tool. The second tool never received it. So the lesson existed
simultaneously as prose, as a working implementation, and as the opposite behaviour,
with nothing reconciling them. A rule that is written down and implemented *once* is
not a rule the system follows; it is a rule one component follows.

## The fix

`Test-LiveClaim` now takes the **newest** of the claim's own `started_utc` (falling back
to the claim file's mtime) and the owner's heartbeat mtime, joined through a
`Get-NormName` helper **copied verbatim** from `ledger-query.ps1`.

Verbatim matters. A second hand-rolled normaliser is how these two came to disagree in
the first place; the fix for "two implementations drifted" is one implementation, not
two corrected ones.

The heartbeat index is rebuilt on each call rather than cached — a single run may
iterate many tasks over many minutes, and a cache captured at start-up would report a
growing agent as silent.

## One deliberate divergence, documented in the function

`ledger-query` reports `UNKNOWN` when no heartbeat matches the owner, because it is a
**reporting** tool and `UNKNOWN` is a verdict it can print. The runner's question is
binary — offer this task, or not — so an owner who never wrote a heartbeat at all falls
back to the claim's own age. Returning "not live" there would let a single
heartbeat-less claim block its task **forever**, which is a worse failure than the one
being fixed. When nothing can be determined at all, the task is still not offered.

## Verification, live case first

```
ledger-query : B5-0631 | claim 292.7 | heartbeat 12.3 | LIVE
run-queue    : declines B5-0631, offers B5-0633 instead
```

Then seven synthetic cases in an isolated temp harness, all PASS:

| case | result |
|---|---|
| 9h claim, no heartbeat → fallback to claim age | FREE |
| 9h claim + 2min heartbeat (the hermes case) | **TAKEN** |
| 9h claim + 8h heartbeat | FREE |
| fresh claim, no heartbeat | TAKEN |
| corrupt claim file | TAKEN (never steal) |
| no claim file | FREE |
| future-dated claim | TAKEN (clock skew) |

## The harness was broken first, and that is the second lesson

The first synthetic run reported a **uniform wrong answer** — nearly every case
"TAKEN". The cause was mine, twice over:

1. a typo in the timestamp format string (`THTHH`), which made every generated
   timestamp unparseable, so every case silently fell back to claim-file mtime;
2. a `U+2028` separator typed as literal placeholder text, which is not a legal
   filename character.

I discarded the run rather than reading it as a code defect. The corrected harness
**asserts up front that its generated timestamp actually parses** — which is exactly
the check whose absence let a broken harness look authoritative. A test harness needs
its own tests; a green run from a harness that cannot fail is worse than no run,
because it is believed.

**Reusable lesson:** when two components must agree, ship one implementation and
import it — do not maintain two. And a shared rule that has been written down and
implemented *once* is not yet a rule the system follows; it is a rule one component
follows, which is a latent duplicate-delivery bug wearing the costume of a
convention.
