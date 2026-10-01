---
document:
  title: "A status gate can hide a permanent gate"
  status: "Pattern (advisory only, same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0961", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0961", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A status gate can hide a permanent gate

**Filed from B5-0961** (2026-09-28), which was claimed, measured, and released
BLOCKED without producing its deliverable.

## The shape of it

A task row gates itself on an upstream task reaching a *status*:

> "claim ONLY after B5-0947 is DONE"

That reads like a wait condition. Re-measuring it is cheap, so the instinct is to
re-measure on every pass and treat green as progress. The trap is that the
upstream row's **note** can carry blockers that **no status transition will ever
clear**.

Measured on B5-0947, `OPEN`→`BLOCKED` for two independent reasons:

1. A **conjunction gate** over eight predecessor batches — mechanical, and it
   genuinely cleared when `B5-0939` went `DONE`.
2. A **withdrawn premise** — the row asks to rule "per B5-0654", and B5-0654 was
   **withdrawn by human ruling with no replacement stated**. This one needs a
   human decision. Clearing blocker 1 does not move it.

Blocker 2 means the downstream gate is not "not ready yet". It is **permanently
red until a person acts**, and no amount of re-measuring, re-queuing, or
re-census will turn it green.

## The rule

**When a gate names an upstream task by ID, read the upstream row's note, not
just its status cell.** The status cell answers "is it moving?"; only the note
answers "can it ever arrive?". A downstream row should state its re-entry
condition in terms of the *decision* it needs, not merely the *status* it waits
for — otherwise every later reader inherits a gate they will re-measure forever
and never understand.

Two cheap habits that made this a five-minute close instead of a wasted pass:

- **Re-measure the upstream blockers yourself; never copy them forward.** B5-0947's
  note still read `B5-0939 OPEN`. Re-reading showed `B5-0939 DONE`. Copying the
  inherited blocker forward would have reported a red gate for the wrong reason —
  and a reader would have gone looking for a missing report that now exists.
- **Claiming a `BLOCKED` upstream row to unblock yourself is the wrong move.**
  A non-`OPEN` row is never offered by the runner, so the claim can never be
  completed through the normal cycle (the orphan-claim class), and when the
  blocker is a human decision the self-unblocking agent would be pre-empting the
  ruling the gate exists to protect.

## Companion: prove green build, red gate separately

Run the build anyway and report it green. "BLOCKED" and "red compile" are
different close-outs with different remedies, and stating the build is green
means the next reader does not spend a pass hunting a tree fault that was never
there. The same applies one level up: a gate that is red for a *governance*
reason should say so in terms of the ruling it awaits, not as a build excerpt.
