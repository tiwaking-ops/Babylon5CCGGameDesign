---
document:
  title: "A census warning is a defect report, and a stale signal is not an absent one"
  status: "Advisory pattern (same tier as investigations/; never canonical per AGENTS.md §6)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  created_date: "2026-09-27"
---

# A census warning is a defect report, and a stale signal is not an absent one

Two separable lessons from B5-0657 (claims-first ledger census protocol,
human-approved 2026-09-27). Both are about a tool that *reports* rather than
*enforces*, which is the class where this failure mode hides.

## 1. Any warning a census emits is a defect report

A structural census — pipe counts, ID shape, status parseability — produces
warnings that every downstream reader treats as "this is broken, go fix it". So a
census run while another writer holds a lock on the rows being censused does not
merely risk a stale reading; it **actively asserts a defect that does not exist**.

The recorded instance: two rows read 6 pipes mid-repair, which is the signature of a
missing trailing delimiter. The committed form was 8 pipes with a leading double
pipe. Same bytes, different defect class, opposite repair. An auditor following the
warning would have "fixed" correct data and left the real defect.

The general form: **a read of shared mutable state is only a measurement when no one
else is writing.** Every tool that reports on shared files needs a claims-first read
— collect the live-claim set, then skip or explicitly mark those rows — and when a
warning *cannot* be attributed to a specific row (an aggregate count mismatch, say),
annotate it as not-a-defect-report rather than dropping it, because a silently
dropped warning is how a real filter bug becomes invisible whenever anyone happens to
hold a claim.

Corollary for a distributed fleet: the protocol's remedy for a wrongly suppressed row
is a re-census, while the remedy for a wrongly reported defect is an agent editing a
row another agent is mid-write on. **The fail-safe direction is silence** — which
means an unreadable input file should suppress, not report.

## 2. "No signal is fresh" and "the signal is absent" are different facts

The obvious way to write a staleness predicate is `if no signal is fresh, it is
stale`. That silently treats a *missing* signal as a *negative* one, and in this
repository that exact collapse had already produced a false liveness claim: a lookup
that matched no heartbeat file returned `-1`, and `-1` compares as younger than any
TTL, so four claims rendered LIVE off zero verification.

The predicate that survives contact with missing data:

> **stale** means every **determinable** signal is older than the threshold.
> A signal that could not be obtained is not a signal that came back negative.

The same discipline applies to a claim file that will not parse: unreadable is not
"no claim exists". And it applies to a tool that must *report* versus a tool that must
*decide* — those two want different behaviour on an absent signal, and the honest
move is to make the divergence explicit in a comment rather than let it emerge as a
disagreement between two tools that are supposed to agree.

**Applied together:** when two independent tools must reach the same verdict, the
verdict function needs one shared definition of both "old" and "unknown", written
down once. Two hand-copied predicates are correct exactly until the first edge case
appears, which is the tool-rule convergence problem in miniature
(`docs/proposals/tool-rule-convergence-proposal.md`, whose shared-library leg is
still un-ruled): identical by construction and by review, but not yet by instrument.

Related filed patterns: *a rule written down and implemented once is a rule one
component follows*; *a lookup that returns a number for "no match" is a bug wearing
the costume of a measurement*.
