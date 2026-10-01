---
document:
  title: "B5-1053 close-out — RARE_WITHDRAWN exclusion: already landed, named"
  status: "Close-out report (observation, no authority)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
---

# B5-1053 — Enforce the exclusion if and only if B5-1026 finds it missing

Row letter: gated claim ONLY after B5-1026 is DONE; **if B5-1026 reports
"already excluded", close this row through its own cycle naming the landed
mechanism with zero src edits.** That branch is the one taken.

## Gate verification (before claiming)

B5-1026 → DONE at 2026-09-29T09:20:30Z (this session, this agent, claim
released). Verdict: already excluded, by side effect, with the caveat that no
line owns the exclusion. Claim taken 09:33:30Z per the row letter.

## The landed mechanism (named, as required)

Two independent mechanisms, neither written for withdrawal:

1. **Rarity whitelist** — `StarterDeckBuilder.drawRandomUncommonsRares`,
   lines 152–156: candidates admitted only where rarity is `UNCOMMON` or
   `RARE`. `RARE_WITHDRAWN` fails the predicate and cannot enter any random
   starter slot. Incidental: the line knows rarity economics, not withdrawal.
2. **Fixed starter lists** — `b5ccg/resources/decks/premiere-starter-decks.json`
   contains zero `as_it_was_meant` entries (re-verified this pass). The fixed
   path excludes by data absence.

**Pool precision (recorded this pass):** under the `loadBothSets` title-dedup
the *deluxe* copy of the shared title wins the pool slot — and the deluxe copy
is precisely the `RARE_WITHDRAWN` record. The pool slot for "As It Was Meant
To Be" therefore carries the withdrawn rarity, which *strengthens* the
incidental exclusion: the premiere RARE twin is deduped away.

## Remaining caveat (carried from B5-1026, unchanged)

The exclusion is real but owned by nobody: a withdrawn card marked `RARE`, or
a future deck-list entry, would enter play silently. The narrow follow-up — an
explicit exclusion at pool assembly plus a conformance probe — is named in the
B5-1026 report and **not seeded here**, per row scope.

## Gates

Zero src bytes changed since the 09:16Z compile green (no recompile needed);
no scratch probe required (no exclusion implemented); run-dup-census exit 0
post-write; own row 7 pipes / doubleLead no; claim released; no commit, no push.

## Reusable lesson

A gated row whose gate says "do nothing if X" and whose X holds is a *real*
close, not a skip — the deliverable is the verified no-op with the landed
mechanism named, and skipping it outright would leave the queue keying on a
verdict nobody recorded.
