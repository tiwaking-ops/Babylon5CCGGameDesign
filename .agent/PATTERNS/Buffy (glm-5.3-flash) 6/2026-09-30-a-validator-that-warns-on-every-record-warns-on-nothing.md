---
document:
  title: "A validator that warns on every record warns on nothing — generate the expected set from the contract, not beside it"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 6", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1104"
---

# Pattern: the expected-set and the contract must share one source of truth

B5-1104's re-census found the pool unchanged (446/383/63/0 — the good news)
and the validation channel flooded: 2,699 UNKNOWN FIELD warnings, one per
record for its own required fields, with the single true warning (the
`timing` field the contract forbids) buried at 1:2,698.

1. **100% warning rate = 0% signal.** When every record triggers the alarm,
   the alarm is measuring the validator's expected-field set, not the data.
   The B5-1025 contract says `id`/`title`/`type` are required on every record;
   a validator that warns on them has two lists that drifted apart — here,
   the expected set was likely written per-type for the *type-specific* fields
   and never unioned with the base core.
2. **The fix is structural, not a hand-tuned ignore list.** Generate the
   expected field set from the same table the contract publishes (base core
   ∪ per-type required ∪ per-type optional); forbidden = present ∧ not
   expected. An ignore list re-creates the drift one edit later.
3. **A buried true warning is worse than a lost one.** The `timing` finding
   survived only because someone already knew to look for it. Before shipping
   a warning channel, prove a planted defect surfaces: run the loader against
   a fixture with one deliberately-unknown field and one deliberately-missing
   required field (B5-1055's own probe did this — but against a build whose
   expected set later drifted).
4. **Re-verify baselines through the production path.** The static census and
   the loader can disagree (B5-1043's two instruments proved it); this row's
   value is that it re-measured the *loader's own* view post-validation and
   could therefore also testify the parse-drop suspicion does not reproduce
   through `loadBothSets()`.

Reusable lesson: validation is a signal channel — budget its false-positive
rate like any other gate, and treat "warns on every record" as the channel
being down, not as 829 findings.
