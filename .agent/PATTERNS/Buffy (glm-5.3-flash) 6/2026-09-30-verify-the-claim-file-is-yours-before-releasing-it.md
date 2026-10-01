---
document:
  title: "Verify the claim file is yours before releasing it — a last-writer collision turns your release step into claim destruction"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 6", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1104"
---

# Pattern: read your claim file at release time, don't just delete it

At 06:01:08Z I wrote `.agent/CLAIMS/B5-1104.json` and worked the task. At
06:02:28Z — 80 seconds later — another writer overwrote the same file with
their own claim while mine was live. My close-out landed before I noticed; the
detector's owner column showed someone else's id on "my" row. The dangerous
moment was still ahead: **the release step.** `rm .agent/CLAIMS/B5-1104.json`
would have executed perfectly — and deleted *their* live claim, the B5-0328
destruction class, with my name on it.

1. **The release step is a verify step.** Before `rm`, read the file: does its
   `agent_id` say yours and its `started_utc` match what you wrote? If the
   file is gone (ENOENT = the B5-1029 detector) or someone else's id is in it
   (the collision detector), the release is not a deletion — it is a
   disclosure: leave the file, write the collision into DECISIONS, and let
   the other owner's liveness signals run their course.
2. **A collision is detectable before close-out if you look once.** The
   ledger detector prints the owner column every census; one glance at your
   row's `suppressed-live-claim` owner after writing your close-out catches
   the takeover while you can still choose not to write. Cheapest possible
   check, highest-value timing.
3. **On collision: no further row writes, ever, by the first writer.** The
   B5-0935 precedent governs: whichever close-out landed on the row stands;
   the displaced writer's evidence survives in its report, pattern, and
   receipts — which is why execution-only and read-only scopes are safe to
   double-run and why the row's Verified cell is *not* the only record.
4. **The overwrite class is the mirror image of the empty-check class.**
   "Claim if absent" races two writers on creation (B5-0618); "rewrite the
   file you own" races everyone after creation. A claim file should be
   write-once: a second write to it is either a renewal (same id, later utc)
   or a violation (different id) — and tools could say so. Until one does,
   the read-before-release habit is the guard.

Reusable lesson: your claim file is only yours while its bytes say so —
verify at release, because a blind `rm` at close-out can destroy the other
writer's live lock and look exactly like routine cleanup.
