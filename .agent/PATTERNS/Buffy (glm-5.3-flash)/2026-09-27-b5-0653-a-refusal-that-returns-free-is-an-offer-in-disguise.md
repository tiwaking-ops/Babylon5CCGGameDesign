---
document:
  title: "Pattern — a refusal that returns 'free' is an offer in disguise"
  status: "Advisory pattern (B5-0653)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Pattern: a refusal that returns "free" is an offer in disguise

Filed under B5-0653 (implausible started_utc refusal).
Report: `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0653.md`.

## The situation

`Test-LiveClaim` answers one binary question: is this claim live? My first
cut answered "not live" for an implausible claim — and `Test-LiveClaim`'s
only caller interprets "not live" as "the task is free". The refusal
visibly warned and then handed the task out on the next pass. The harness's
first run caught it: CASE1 FAIL, offered=True.

## The rules

1. Map the consumer before choosing a return value. A boolean validity check
   that feeds a candidate selector cannot express "refuse": the refusal has
   to live in the selector's filter (exclude from candidates), with the
   boolean as a backstop that answers the SAFE side of the asymmetry
   (unstealable).
2. Refusals must be loud AND withholding — a warning without withholding is
   a log entry, not a refusal; withholding without a warning is a silent
   drop. The B5-0652 chain showed both halves are needed.
3. Never repair another agent's claim file; refuse the offer and let the
   owner's re-claim heal the data.
4. Harness expectations encode semantics, not vibes: "behaves exactly as
   before" for a LIVE claim is skip-and-unwarned, not offered. Get that
   wrong and the harness will demand a regression.

## Supersedes

None. New pattern.
