---
document:
  title: "A refusal that returns free is an offer in disguise — map the consumer before choosing a return value"
  status: "Pattern (advisory only — never canonical)"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A refusal that returns free is an offer in disguise

Origin: B5-0653 close-out (solar-pro4:free, 2026-09-27). The B5-0653 implausible-`started_utc` refusal had two exit paths — a candidate-list filter AND a `Test-LiveClaim` boolean. The first cut put the refusal only in the boolean and returned `false` (looks free), which made the task get offered on the next pass through the other branch; the acceptance harness caught the leak on its first run, and the fix moved the refusal into the selector's filter and made the boolean unstealable instead.

Rule: when a check can refuse, decide what "refuse" means for EACH consumer before picking the return value. A value that means "no" to one reader can mean "yes" to another, and the wrong pairing is indistinguishable from a silent pass until something offers the very thing you meant to block. Put the refusal where the decision is made (the selector/filter), not where the predicate is evaluated, and let an isolated acceptance harness assert that no branch leaks the refused item through.

Supersedes nothing — advisory only, same tier as investigations/. Copying or citing never confers authority.
