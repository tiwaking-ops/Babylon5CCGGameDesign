---
document:
  title: "A gated row closing as a no-op is a real close, not a skip"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
  task: "B5-1053"
---

# A gated row closing as a no-op is a real close, not a skip

Traces to: B5-1053 (enforce the RARE_WITHDRAWN exclusion iff B5-1026 finds it
missing).

**One-line lesson:** conditional rows whose condition resolves to "do nothing"
still need the full cycle — claim, verify, name the landed mechanism, close —
because the queue cannot key on a verdict that lives only in another row's
report.

## The shape

- B5-1053's letter had two branches: implement (if missing) or close naming
  the landed mechanism (if present). The condition was resolved by B5-1026
  earlier the same session. Taking the second branch *required* every step the
  first branch would have taken except the edit — claim, gate verification,
  mechanism identification, row close-out with gates run.
- The close-out earned its keep anyway: re-verification under the claim
  surfaced a pool-precision fact neither prior row had recorded (the
  title-dedup hands the pool slot to the *deluxe* copy, so the slot itself
  carries the withdrawn rarity — the incidental exclusion is stronger than
  first stated). A no-op close still measures.
- Skipping the row as "already done by 1026" would have been the lazy twin of
  the orphan-claim error: leaving a live OPEN row whose premise was resolved
  elsewhere, for the next census to trip over.

## What worked

- Verify the gate row's status and *its own* close-out content before
  claiming, then claim and close through the normal cycle — never
  retroactively edit another agent's verdict to unlock yourself.
- Re-run the named checks under your own claim (grep counts, line reads) even
  when trusting the prior report — the precision bonus falls out of the
  re-measurement, not the trust.

**Reusable lesson:** a conditional row is a contract with two deliverables —
the action, or the verified reason no action was needed — and only one of them
is skippable, the one nobody writes down.
