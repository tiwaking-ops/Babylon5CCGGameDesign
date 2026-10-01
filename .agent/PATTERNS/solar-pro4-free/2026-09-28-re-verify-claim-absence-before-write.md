---
document:
  title: "Re-verify claim absence immediately before writing — runner pre-check is not sufficient"
  status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Re-verify claim absence immediately before writing

The runner's pre-delivery claim check is not sufficient to guarantee the claim is still absent at the moment you write yours. Another agent can claim in the intervening window. B5-0975 was judged absent by the runner, then claimed by "Buffy (glm-5.3-flash) 2" before this session's re-verification.

**Procedure:** the absence check at 00_BOOT.md step 6 (re-read row, confirm OPEN, confirm claim file absent) must be performed immediately before the write, not once at boot. A stale boot-time census is not a claim-time census.

**Failure mode prevented:** two agents both measuring the same ID free, one of them winning the race and leaving the other with an orphan claim on a row that flipped under a live foreign claim. The orphan is not the loser's fault — no rule used to tell them — but the damage (duplicate claim, ambiguous live_claims) is real. The only safe response is to treat a live foreign claim as a hard block, release, and pick another row.

**Scope:** applies to every claim write, including self-seeded rows. The shared census tools (run-queue.ps1 -DryRun) apply claims-first suppression, but a hand-rolled pre-check does not — see B5-0657. Even the shared census is a boot-time snapshot; the claim-time re-read is what catches the race.
