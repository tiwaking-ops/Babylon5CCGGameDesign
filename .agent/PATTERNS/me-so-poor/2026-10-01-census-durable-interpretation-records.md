---
author_llm: {name: "me-so-poor", version: "me-so-poor"}
assessor_llm: []
created_date: "2026-10-01"
last_modified_by_llm: {name: "me-so-poor", version: "me-so-poor"}
last_modified_date: "2026-10-01"
---

# Pattern — census durable interpretation records (me-so-poor, B5-1447, 2026-10-01)

**Scope:** This is a NEW pattern file (supersede-never-rewrite — a corrected version would be a NEW file linking this one). Written in the `me-so-poor` namespace only.

**Reusable lesson (one line, as required by AGENTS.md §6 / B5-0430):** A census that names unrecorded interpretations is not a fix — it makes the gap findable for the interpretation's own writer (B5-1313) and for the next agent who must verify whether a new caller (e.g., MainWindow:2134) has changed the interpretation's conditions; measure the property that matters (durable doc present vs report-only) rather than counting reports.

**Context (B5-1447 close-out):**
- Task B5-1447 (OPEN, scope docs + src read-only, no edit) ran a census over Done close-outs from the last 7 days (B5-1313 through B5-1469).
- The named open interpretation — B5-1313 (sponsor-cost / canPlayCard / applyGenericCardPlay — `.agent/REPORTS/2026-09-30-solar-pro4-free-B5-1313.md`) — lives only in its report; DECISIONS.md has zero entry; the rulebook (canonical, unedited) has no interpretation record.
- The census also found 4 other behaviors with DONE reports but no DECISIONS.md entry (B5-1341 inherits B5-1313's unrecorded status; B5-1419 feeds B5-0202c already done; B5-1459 is measurement not behavior; B5-1439 / B5-1441 / B5-1453 / B5-1469 ARE recorded in DECISIONS.md).
- Key measurement: presence-test ("does a report exist?") passes on 13 of 13 done items; durable-doc test ("does DECISIONS.md carry it?") passes on only 4 of 13 — the census's value is the gap, not the count.
- Related: B5-1313 report (`.agent/REPORTS/2026-09-30-solar-pro4-free-B5-1313.md`) carries its own "Reusable lesson" (interpretation record worth writing when three rows touch the same seam but none writes the full picture); this pattern complements it by adding the census discipline that makes the gap findable.
- Related patterns: `.agent/PATTERNS/Buffy (glm-5.3-flash) 14/2026-10-01-offer-markers-schedule-but-do-not-claim.md` (claim ≠ schedule); `.agent/PATTERNS/Buffy (glm-5.3-flash) 12/2026-10-01-line-ending-churn-is-whole-file-rewrites.md` (append-only discipline); `.agent/PATTERNS/me-so-poor/2026-10-01-gate-at-claim-time-not-seed-time.md` (gate evaluated at claim time).

**What to do with this pattern:** Before claiming any interpretation-writing task, check both `DECISIONS.md` (durable) and `.agent/REPORTS/` (corpus) — if the report exists but DECISIONS.md is silent, the interpretation is unrecorded; do not assume "report exists = durable"; write to DECISIONS.md per AGENTS.md §2, not to the rulebook, and fence B5-1313's open interpretation with the census evidence.

**Not corrected / superseded:** Nothing superseded. First version.
