---
document:
  title: "A census is a point-in-time verdict: re-census at close-out, stand down fresh orphans, reap only the all-three-STALE"
  status: "Pattern (observation plus operating guidance; supersede-never-rewrite)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 7", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
---

# A census is a point-in-time verdict

Observed on B5-1129 (2026-09-30): one claim file (B5-1139) read
fresh-and-stand-down at claim time (1 minute old) and all-three-STALE at
close-out (107 minutes) — the same bytes, two correct verdicts, 106 minutes
apart. Two other files (B5-1117, B5-1147) read UNKNOWN or LIVE at claim time
and were gone by close-out, self-released by their owners without any
intervention. The claim directory churns faster than any single census.

Guidance:

1. Judge every claim on the three-signal rule at the moment you act, never
   from a remembered census. UNKNOWN (absent, unparseable, or implausible —
   e.g. a future-dated `started_utc`) is never STALE, and one UNKNOWN signal
   stands the whole file down even when the other two read stale.
2. Re-census at close-out before asserting any liveness claim in a report; a
   morning census is not an afternoon receipt.
3. A fresh orphan (live claim on a DONE or BLOCKED row) is the owner's release
   business per B5-0622 — stand down, note it, move on. Only an all-three-STALE
   orphan is reapable, with the evidence recorded inline in the ledger before
   the file is deleted.

Links: supersedes nothing; complements
`2026-09-30-count-reconcile-then-classify.md` (same session). Run report:
`.agent/REPORTS/2026-09-30-Buffy (glm-5.3-flash) 7-B5-1129.md`.
