---
document:
  title: "Measure the gap by searching the layer you are allowed to touch"
  status: "Pattern"
  provenance_note: "Advisory only, per AGENTS.md section 6. Never canonical; citing confers no authority."
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Measure the gap by searching the layer you are allowed to touch

**Reusable lesson (B5-0701, 2026-09-27).** Before writing a "missing feature",
grep **the layer you are allowed to edit** for the capability's name. Zero hits
there, combined with a landed engine/model API, is the signature of an
*unsurfaced* capability — and it tells you the work is wiring, not design.

## Why the layer matters

Search the whole tree and you cannot tell a missing capability from a missing
*call site*: both look like "the name appears somewhere else". Search only the
layer you may touch and the ambiguity disappears. In B5-0701, `surrender` and
`getPower` each returned **0 hits over `b5ccg/src/b5ccg/ui/`**, while
`GameAction.surrender`, `RulesEngine.canSurrender` and `Player.getPower()` were
all already landed. That is not a feature request; it is a wiring task, and the
row's "readout only" framing was correct rather than a constraint to work around.

## The pairing that carries the most weight

1. **Zero hits in your layer** — the capability is not exposed there.
2. **A landed API elsewhere** — the behaviour already exists and is tested.
3. ⇒ You are surfacing settled law. Do not re-derive it, do not redesign it, and
   do not treat the engine as incomplete.

## Two things this buys you

- **The engine stays the authority.** Because the law already exists, the UI
  asks `canX(...)` and renders the answer. The B5-0423 failure — a hand-rolled
  partial predicate drifting from the engine and disabling a *legal* move — is
  structurally impossible when you never wrote a predicate.
- **Honest-by-construction readouts.** If the selectable set is exactly what the
  engine accepts, then an empty set *is* "unavailable", and the display cannot
  show a stale "available" for a state the engine has since rejected.

## The trap worth naming

A readout that gets a *direction* wrong is worse than no readout, because it is
trusted. Rulebook :817 grants +3 influence to the **target**; a label reading
"you gain 3" would have been plausible, fluent, and false. When surfacing a
consequence, state who receives it.

## Supersedes

Nothing. Third record in this namespace; see also
`2026-09-27-an-empty-census-is-not-evidence-of-an-empty-queue.md` and
`2026-09-27-the-code-outranks-the-summary-line.md`.
