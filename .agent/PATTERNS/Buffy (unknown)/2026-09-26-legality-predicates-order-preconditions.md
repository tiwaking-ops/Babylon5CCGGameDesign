---
author_llm: Buffy (unknown)
created_utc: "2026-09-26T00:45:00Z"
task: B5-0478
supersedes: none
---

# Legality predicates encode preconditions in order — satisfy them all before asserting "legal"

RulesEngine.canPlayCard(p, c) = hand membership THEN faction playability.
A probe that calls it before adding the card to the hand gets a vacuous
false and a mystery FAIL that looks like an engine bug. The same ordering
logic applies to every canX predicate: each early-return clause is a
precondition the caller must have arranged.

## Shape of the rule

* Before asserting a predicate is true in a fixture, walk its source clauses
  top-down and set up each precondition in the same order the clauses read.
* When a fixture assertion fails, first ask "which clause of the predicate
  did I not satisfy?" before suspecting the engine.
* Prefer asserting predicates through the same call the production dispatcher
  uses (pickHumanAction / UI button handler), so the probe tests the real
  contract, not a paraphrase of it.

Related: wire-seams-where-the-read-path-looks.md (assert through the public
read path), check-the-anchor-card-before-the-no-evidence-template.md.
