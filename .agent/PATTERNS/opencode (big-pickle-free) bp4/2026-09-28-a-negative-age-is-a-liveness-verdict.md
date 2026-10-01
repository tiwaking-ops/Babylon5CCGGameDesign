---
document:
  title: "A negative age is a liveness verdict no guard rejects"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "opencode (big-pickle-free) bp4", version: "big-pickle-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (big-pickle-free) bp4", version: "big-pickle-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A negative age is a liveness verdict no guard rejects

**Reusable lesson.** Every liveness guard in this repository is written as
`age < TTL` or `age <= 0`, and both tests pass when `age` is **negative**. A
future-dated timestamp is therefore not an edge case the existing checks
degrade on — it is a value they certify as the freshest possible signal.

## The measurement

* Wall clock at seed time: `2026-09-28T10:14Z`.
* `.agent/CLAIMS/B5-0939.json` carries `started_utc: 2026-09-28T22:05:00Z` —
  about **twelve hours ahead**.
* `.agent/tools/ledger-query.ps1` printed that claim's age as **−707.8 minutes**
  and rendered the verdict **`LIVE`**.

The B5-0653 guard is real and it fires — but only on the *lower* bound. It
rejects `started_utc` that is exactly midnight UTC or at/before the Unix epoch
("cannot be a just-now claim"). A timestamp in the future is not "a long time
ago", so it is not what that guard was written to catch, and it sails through.

## Why this is the B5-0597 failure-3 inversion again

`HEARTBEATS/README.md` records the worst failure in the fleet's history as: a
lookup that matched nothing returned `-1`, and `-1` compares as *younger* than
the TTL, so four claims rendered LIVE with zero verification. The mechanism was
manufacturing a positive liveness claim out of an absent signal.

A future-dated claim reaches the same verdict by the opposite route: the signal
is not absent, it is *implausibly fresh*, and the same comparison arithmetic
manufactures the same positive verdict. Different input, identical failure. That
is the tell — when a guard is expressed as a comparison against TTL rather than as
a membership test against a plausible range, there is a whole class of inputs
that satisfies it.

## The shape of the fix

Make the test a **band**, not a comparison. Refuse `started_utc` that is later
than *now* by more than a stated tolerance, and keep the B5-0653 lower bound.
The direction rule from B5-0653 carries over unchanged and is the reason the
tolerance must be generous:

* a wrongly-**implausible** claim costs one blocked task;
* a wrongly-**plausible** claim costs a collision and destroyed work.

A timezone mistake or a few seconds of clock skew is common; twelve hours is not.
A tolerance that absorbs the former and refuses the latter is the whole
engineering content, and it must be *written down*, not chosen silently.

## The same class, second instance

The B5-0949 DECISIONS entry recorded the identical shape on a **heartbeat
payload**: `.agent/HEARTBEATS/me-so-poor.json` carried
`utc: 2026-09-28T12:15:00Z` against a wall clock of roughly `09:52Z`. Liveness
math was unaffected there because the three-signal rule keys on **mtime**, not
on the payload — which is exactly why the defect is quiet. It survives in a file
that every validator reports as `CONFORMS`.

Two instances, in two different files, both still on disk, one of them inside a
file the validator certifies as conforming. That is the argument for seeding the
band test rather than filing the single fix.

## Where this connects

* **B5-0653** — the lower-bound guard this extends (a claim-timestamp row).
* **B5-0952** — seeded from this measurement.
* **B5-0597** — the original inversion, in `HEARTBEATS/README.md` failure 3.
* **B5-0609** — the sibling class: a missing signal read as a present one.

**Advisory only.** Same tier as `investigations/`, never canonical. Citing this
record confers no authority.
