---
document:
  title: "B5-1107 — the stray timing key deleted from de_event_armistice; the store's last genuine unknown is gone"
  status: "DONE 2026-09-30"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  claimed_at: "2026-09-30T06:36:58Z"
  authorization: "overseer-authorized single-field data fix per the B5-0317/B5-0318 precedent, adjudication supplied by B5-1101"
---

# B5-1107 — one key, one record, verified three ways

## The edit

`b5ccg/resources/cards/deluxe.json`, record `de_event_armistice`: the line
`"timing": "ANY"` deleted (and the preceding `text` line's trailing comma
removed with it). `git diff --stat`: **1 insertion, 2 deletions, one
record** — surgical, as scoped. No other key, record, or file touched.

## Verification (the B5-1104 probe receipt method)

1. **Compile gate**: `sh compile.sh` exit 0.
2. **Pool census unchanged**: `PROBE pool=446 duplicateTitles=0
   unionTitles=446 poolMissingFromUnion=0 deluxeOnlyTitles=0` — deleting a
   key the loader never consumed changed nothing about membership.
3. **Warning census**: UNKNOWN FIELD lines **5,398 → 5,396** — exactly the
   2 timing emissions (1 record × 2 validation passes, the B5-1104 ×2
   rule). The store's one genuine unknown-field warning is gone; the
   remaining 5,396 are the expected-set noise owned by B5-1068 (the 6
   set-adds per B5-1111's handoff).

## Provenance chain of the deleted key

B5-1022/B5-1025 flagged it contract-forbidden → B5-1101 adjudicated it
stray (zero consumers across all 63 src files, 1-of-829 records, no
printed basis — the B5-0935 face transcription shows Armistice's timing
lives entirely in its rules text) → this row executed the deletion under
the B5-0317/B5-0318 overseer authorization precedent. The premiere twin
never carried the key; the deluxe text change (gain 1 Influence) is
unaffected and remains in the text field.

No commit, no push; the dirty tree carries the fix for the next
checkpoint row (B5-1106's gated commit).

**Reusable lesson:** a well-scoped deletion row reads like a checklist —
authorization precedent, completed adjudication, one-key scope, and a
numeric verification target (5,398 → 5,396) that proves both the removal
and the non-collision with the noise class.
