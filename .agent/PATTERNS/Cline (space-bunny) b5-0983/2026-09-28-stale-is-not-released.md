---
document:
  title: "Stale is not released — a claim with a future-dated started_utc never opens a gate"
  status: "Advisory pattern (never canonical; copying or citing confers no authority)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0983", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0983", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Pattern: stale is not released

Filed under B5-0983 (`BLOCKED` at its row gate — `engine/` still claimed by
B5-0953). Report:
`.agent/REPORTS/2026-09-28-Cline (space-bunny) b5-0983-B5-0983.md`.

## The shape of the trap

A row is gated on a foreign claim being *released*. You go to check, and the
evidence looks like permission:

* all three filesystem signals are far past the 30-minute TTL — claim mtime
  148.5 min, owner heartbeat 148.4 min, report ~144 min;
* the owner looks long gone;
* and the scope is exactly the directory you were told to work in.

Every instinct says *this is abandoned, take it*. That reading is wrong, and the
reason is the one field you have not looked at: the claim's `started_utc` is
dated **634 minutes in the future**. Negative age compares as younger than any
TTL, so the claim is permanently `LIVE` and permanently unstealable. It is not
stale — it is *pinned*.

This is the mirror image of the B5-0597 failure that the three-signal rule was
built to stop. There, a reaper keyed on one signal destroyed live work. Here, a
taker keyed on three stale signals would have destroyed live work just as
reliably — and would have felt *more* justified doing it, because it had three
signals instead of one. **Signal count is not signal correctness.** Three stale
signals do not outvote one canonical timestamp that says otherwise.

## What to do instead

1. **Check the canonical timestamp, not only the mtimes.** The claim's
   `started_utc` and the owner's heartbeat `utc` field are the fields that
   decide the verdict; the mtimes are supporting evidence. A future-dated value
   in either is a hard stop.
2. **Do not reap, do not repair, do not touch the row.** A claim file belongs to
   its owner — the runner's own warning says this in as many words. Repairing
   someone else's timestamp is out of scope however good your reasons.
3. **Mark `BLOCKED`, release your own claim, stop that item only.** A red lane
   is not a red tree. Run the compile gate anyway and *record* that it was
   green, so the next reader does not spend a cycle hunting a build break that
   was never there.
4. **Bank a read-only census.** The blocked run is the cheapest possible place
   to gather the work the next claimant will need, because you have the scope
   open and the time. Size the gap, name the mechanism, record the data-shape
   gotchas. Gather it; do not act on it.

## The corollary worth stealing

The census is where the actual reusable value sits, and it is available *only*
because the gate blocked you. `CardEffects` turns out to dispatch through
fourteen id-keyed tables holding ~49 hard-coded card ids, against 829 distinct
ids in the card data — so 780 cards have no branch at all. That is the first
number the real implementer needs, and it cost one read-only pass to obtain.

A blocked task that returns nothing but a red verdict has thrown away the one
thing it was uniquely positioned to find out.

## And one data lesson, free of charge

Both card files are **top-level JSON arrays**, not `{ "cards": [...] }`. The
first census script assumed the object form and died with `Index operation
failed; the array index evaluated to null` — an error message that reads like
corrupt data and is actually a shape mismatch. Before trusting any join built on
a file's structure, assert the structure. The join is one line; re-deriving that
it was even pointed at real data is an hour.