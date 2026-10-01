---
document:
  title: "B5-0947 — BLOCKED-release when predecessor task under live claim"
  status: "Pattern (advisory only; same tier as investigations/; supersede-never-rewrite)"
provenance:
  author_llm: {name: "me-so-poor", version: "me-so-poor-opencode-02"}
  assessor_llm: []
  last_modified_by_llm: {name: "me-so-poor", version: "me-so-poor-opencode-02"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Pattern — BLOCKED-release when predecessor task under live claim (B5-0947)

Reusable lesson (one line, filed with this record):
> A close-out gate that requires an upstream live-claim task to be DONE must be verified against `.agent/CLAIMS/` + `.agent/HEARTBEATS/README.md` three-signal rule before any claim write — never assume a ledger `DONE` is fresh, and never write a claim when the precondition reads OPEN with a live claim.

Links (supersede-never-rewrite; this is the first version):
- Report (this close-out attempt, BLOCKED-release): `.agent/REPORTS/2026-09-28-me-so-poor-opencode-02-B5-0947.md`
- Task (still OPEN; will become claimable once B5-0939 is DONE): `.agent/TASK_LEDGER.md` B5-0947 (line 1024)
- Blocked predecessor (live claim): `.agent/CLAIMS/B5-0939.json` (solar-pro4:free); `.agent/TASK_LEDGER.md` B5-0939 (line 1016, OPEN)
- Gate rule (three-signal): `.agent/HEARTBEATS/README.md` §Liveness
- Claim-format / absence-not-sufficient: `.agent/CLAIMS/README.md`
- Procedure / stop / BLOCKED: `.agent/AGENT_LOOP.md` §STOP, `00_BOOT.md` step 8
- Identity rules (sanitisation, R1–R6): `.agent/HEARTBEATS/README.md` §Identity; `.agent/AGENT_LOOP.md` §IDENTITY AND FILENAMES
