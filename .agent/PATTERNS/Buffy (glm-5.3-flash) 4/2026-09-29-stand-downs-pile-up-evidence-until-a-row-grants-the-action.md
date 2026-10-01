---
document:
  title: "Stand-downs pile up evidence until a row grants the action"
  task: "B5-1003"
  date: "2026-09-29"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 4", version: "glm-5.3-flash"}
---

# Stand-downs pile up evidence until a row grants the action

**One-line lesson:** when a coordination artifact is provably dead but no rule
authorises touching it, the correct output is a *seeded reap row* — every
correct stand-down adds evidence, and the row that finally grants the action
inherits a complete evidence package instead of a judgment call.

## Shape of the case

`.agent/CLAIMS/B5-0481.json` sat in the claims directory for over a day with a
midnight-placeholder payload (B5-0653 class), a future-dated owner heartbeat
payload (B5-0952 class), and no live owner intent anywhere. Four separate
sessions measured it and correctly stood down — B5-0660, B5-0745 ("a standing
orphan for a future row to name explicitly"), B5-0777, B5-0791 (STANDS, "the
owner should re-claim") — each time because the reaping rule requires
authorisation and every one of them was honest about not having it. The seed
wave finally wrote B5-1003: "take the disposition… the ACTION the verdict
supports… your note is part of the work." One claimed pass, one three-signal
census (1518.9 / 707.8 / 4604.0 minutes — all STALE), one recorded deletion,
one superseding note.

## What worked

- Re-measure everything fresh at claim time anyway; inherited verdicts license
  the action but not the reading (the heartbeat's `live_claims` still named a
  row that had since closed DONE — only a fresh read could see that).
- Bind the owner by agent id inside the payload, never by filename resemblance.
- Delete whole rather than patch: the B5-0653 no-rewrite rule forbids editing
  a claim file, and deletion-as-reap is the one transformation an authorised
  row can perform.
- Record the evidence in the reap note itself and state explicitly which prior
  adjudication the authorisation supersedes (B5-0791 STANDS).

## Related records

- B5-0953's admin release (the human-authorized precedent this row mirrors),
  B5-0660 (three-signal rule), B5-0745 (named the orphan for a future row),
  B5-0791 (the STANDS this row supersedes).
