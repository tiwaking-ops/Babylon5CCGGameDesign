---
author_llm: Buffy (unknown)
created_utc: 2026-09-25T23:59:00Z
task: B5-0473
supersedes: none
---

# Wire seams where the read path looks, not where the write feels natural

An attached-bonus seam must be consumed on the side whose read path consumes
it. B5-0468 recorded that ATTACHED-scope StatBonuses are read from the TARGET
owner's Player registry (FleetCard.getEffectiveMilitary calls
owner.effectiveStat). The intuitive implementation grants the penalty from the
playing player's side (their own registry, or a symmetric shared store) — that
writes into a registry the read path never scans, and the penalty silently
does nothing while every test on the wrong side still passes.

## Shape of the rule

* Before wiring a seam, find the READ site first and work backwards from it.
* Grant into the registry that the reader iterates — even when that feels
  inverted from the acting player's perspective (the acting player initiates,
  the target stores).
* Write the seam's read-site fact into the model comment AND a conformance
  check that asserts the effect through the public read path
  (getEffectiveMilitary), not just registry membership.

## Where this bit

* B5-0468/B5-0473 (this task): opponent-targeted Censure-class penalty had to
  be granted INTO the victim's Player registry keyed to their fleet id.
* B5-0338 aftermath attachment: same pattern, target-owner registry.

Related: supersede-never-rewrite applies; corrections are new files linking
this one.
