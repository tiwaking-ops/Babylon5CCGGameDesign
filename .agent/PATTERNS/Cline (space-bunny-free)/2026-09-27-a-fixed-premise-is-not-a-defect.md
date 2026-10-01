---
document:
  title: "A fixed premise is not a defect"
  status: "Pattern"
  provenance_note: "Advisory only, per AGENTS.md section 6. Never canonical; citing confers no authority."
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A fixed premise is not a defect

**Reusable lesson (B5-0717, 2026-09-27).** When a task's quoted premise may
already have been resolved, re-measure before reporting. A confident violation
report about a file that is now correct is a **false defect report** — and the
cost of the re-census is one command.

## The shape

The row said: *assess the NON-CONFORMING `me-so-poor.json` and either confirm
its repair or record the exact violation.* It even warned me to re-census fresh
"because the owner may have already fixed it."

They had. The validator read 30/30 conforming, 0 non-conforming, exit 0. The
correct output was therefore a **confirmation**, and the row's other branch —
"record the exact violation with evidence" — was not taken, because its
precondition was false.

## The rule

1. **The premise in a task row is a claim about the past, not a fact about the
   present.** Rows are seeded by one agent and worked by another, sometimes
   minutes later. Treat every quoted premise as stale until measured.
2. **Both branches of an assessment row can be correct answers.** "Confirm the
   repair" is not the consolation prize; when the premise was fixed, it is the
   *accurate* result, and writing it up as such is the whole job.
3. **Reconstruct the original violation from committed bytes anyway.** The
   assessment still has value: `git show HEAD:<path>` recovered exactly what
   was wrong (three absent required fields plus `heartbeat_utc`/`status` for
   `utc`/`state`). "It's fixed now" without "it was *this* wrong" leaves the
   next reader unable to tell whether the fix was correct or accidental.

## The part worth remembering

A missing `live_claims` key is not a cosmetic schema gap. It turns a positive
assertion — *this agent holds nothing* — into `UNKNOWN`, which is exactly the
verdict the three-signal rule forbids substituting with a fallback (B5-0609).
So the reason the repair mattered is worth writing down: it restored a liveness
signal, not a comma.

## Supersedes

Nothing. Fifth record in this namespace; see also
`2026-09-27-an-empty-census-is-not-evidence-of-an-empty-queue.md`,
`2026-09-27-the-code-outranks-the-summary-line.md`,
`2026-09-27-measure-the-gap-by-searching-the-layer-you-are-allowed-to-touch.md`
and `2026-09-27-a-null-result-is-a-result-record-it-as-one.md`.
