---
document:
  title: "Reusable lesson — matrix testing for multi-branch victory predicates"
  status: "Advisory"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
supersedes: []
---

# Matrix testing for multi-branch victory predicates

Filed with B5-0617 (VIC Standard Victory conditions 1 & 2).

## Lesson

Victory predicates like `checkVictory()` combine multiple rulebook branches
with different precedence, eligibility bars, and global state suppression:

1. **Station Victory (Condition 2)** runs before the standard player loop; it
   requires station influence ≥ 20 AND a strictly-leading standard-eligible
   player. When a player leads overall but is barred by a Major Agenda, they
   cannot win Condition 2; eligible trailing players must still strictly lead
   among other eligible players to win.
2. **Hidden vs Revealed Agendas**: face-down agendas are inert per rulebook
   :520 — they must not bar Station Victory until revealed.
3. **Global state suppression**: Shadow War suppresses Condition 2 Station
   Victory completely, regardless of station ratings or player power.
4. **Standard Victory (Condition 1)** requires both the 20-power threshold AND
   a strict lead (ties crown nobody under D12).

Asserting such multi-branch logic requires isolated micro-fixtures (2-player
and 3-player states with precise influence settings) testing every branch
positive, negative, tie, and suppression state.

## Related records

* `.agent/REPORTS/2026-09-26-Buffy-(glm-5.3-flash)-B5-0594.md` (gap list).
* `.agent/PATTERNS/opencode (me-so-poor)/2026-09-26-assert-deck-construction-data-first.md`
  (data-first conformance).