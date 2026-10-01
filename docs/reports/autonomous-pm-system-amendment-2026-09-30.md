---
document:
  title: "Autonomous PM System — Amendment 2026-09-30 to the 2026-09-29 Replication Manual"
  status: "Report (observation tier — describes, does not govern; copying confers no authority)"
  audience: "Autonomous AI worker agents provisioning this system in a new repo (kit deltas); secondary reader is the single human owner (verdict + direction)"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm:
    - {name: "Muse Spark", version: "muse-spark-1.3-contributor-free", passes: 1, last_pass: "2026-09-30", note: "correction pass: run-verification-battery.ps1 has a DECISIONS entry (claimed row, exit contract 0/1/2) so it is assessed, not unassessed; B5-1014-harness.ps1 still unprovenanced"}
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
baseline: "docs/reports/autonomous-pm-system-replication-manual-2026-09-29.md"
companion_delta: "docs/reports/clock-integrity-in-agent-coordination-files-2026-09-30.md"
measured_on: "2026-09-30, live tree (ledgerreports/heartbeats/patterns counted on disk; DECISIONS.md line-counted)"
---

# Amendment 2026-09-30 — what changed since the 2026-09-29 replication manual

## 0. How to read this amendment

- **Tier:** observation. This file amends nothing by itself. Anything herein takes effect in any repo only through that repo's own `AGENTS.md` + gate + decision log.
- **The manual remains the base.** Apply this amendment as a diff on top of it: §1 verdict, §2 scale, §3 governance, §4 coordination, §5 kit deltas (copy vs. avoid), §6 gaps/risks for the fundamental system, §7 re-issue recommendation.
- **Conventions:** MUST/SHOULD/MAY as in the manual. `[GUESS]` flags inference — see §8.
- **Method:** no ledger row flipped, no claim/heartbeat touched, no commit. Ledger/report/heartbeat/pattern counts measured on disk; "new since 09-29" via file mtimes + DECISIONS content (git log unavailable in-session — see §8).

## 1. Verdict: SIGNIFICANT drift (both artifacts warranted)

Three independent triggers fire: (a) a governance-rule addition (heartbeat identity **R7**, 2026-09-30); (b) a new measured failure class amending manual §7.5/§11 (authored/future-dated timestamps, clock-integrity report 2026-09-30); (c) ~25–35% scale growth in one day across all four coordination stores. Per the agreed threshold this amendment is filed now, and a full manual v2 re-issue is recommended after the pending proposals in §4 adjudicate (§7).

## 2. Scale table (measured)

| Store | Manual 09-29 census | Live 09-30 | Delta |
|---|---|---|---|
| Ledger B5- rows (`.agent/TASK_LEDGER.md`) | 494 | **662** (DONE 555 / OPEN 52 / BLOCKED 43 / SUPERSEDED 7 / VOID 5) | +168 (+34%) |
| Close-out reports (`.agent/REPORTS/*.md`) | 593 | **722** | +129 |
| Heartbeat files (`.agent/HEARTBEATS/*.json`) | 88 | **125** | +37 |
| Pattern namespaces (`.agent/PATTERNS/*/`) | 60 | **96** | +36 |
| `docs/DECISIONS.md` | 7906 lines | **9171 lines** | +1265 |
| Task-ID frontier | ~B5-10xx seeds | **B5-1461 DONE; B5-1411..B5-1457 wave seeded** | ID space advanced ~400 |

## 3. Governance deltas (rule changes — MUST-copy candidates)

1. **R7 identity rule ADDED** (`.agent/HEARTBEATS/README.md`, `last_modified 2026-09-30`, B5-1062, human-approved): ASCII-only single transliteration; PUA/separator codepoints barred from filenames. Additive to R1–R6; no existing `agent_id` renamed, no file renamed. Replicating agents MUST carry R1–R7 as one block, not R1–R6 alone.
2. **AGENTS.md assessor entry 2026-09-29** (`me-so-poor`, note "B5-1049 cost gate surfacing in UI"): postdates the manual's §10 09-29 close-out list (which names only B5-0953 + B5-0998). No section text changed; the cost-gate precedent it cites is the thing to watch, not the entry itself.
3. **Unchanged (confirming stability):** `.agent/00_BOOT.md` (2026-09-28), `.agent/AGENT_LOOP.md` (2026-09-27), `.agent/CLAIMS/README.md` (2026-09-27), `guidelines/Guidelines.md` (2026-09-27). The 11-step boot, claim primitive, and three-signal liveness text are byte-stable versus the manual.

## 4. Coordination deltas

- **Clock integrity amendment EXISTS** (`docs/reports/clock-integrity-in-agent-coordination-files-2026-09-30.md`, observation tier, self-declared amendment to manual §7.5/§11): the rule is "never let an agent author a timestamp a decision depends on — either the tool writes it, or the decision reads the filesystem instead." Measured 125 files: 11 DISAGREE (payload vs. mtime beyond tolerance), 87 IN-BULK-CLUSTER (mtime bulk-assigned, gap uninformative), 25 idle-correct, 2 ok-live; one agent +741.8 min skew, two more +186–215 min. Replicating agents MUST add the future-dated-timestamp row to the manual's §11 failure table and the payload-vs-mtime check to any census tooling.
- **New tools (2):** `.agent/tools/B5-1014-harness.ps1` (zero DECISIONS mentions — unprovenanced, carry as candidate only) and `.agent/tools/run-verification-battery.ps1` (**assessed**: six-instrument cold-boot battery with exit contract 0 green slash 1 red slash 2 layout-broken via a claimed row, DECISIONS ~L9816 plus scope ~L10032; correction 2026-09-30 to the first-pass wording that called both unassessed). Neither is in the manual's §7.6 tool list; the battery is a v2-kit candidate pending boot-wiring review.
- **New proposals since 09-29pm (all advisory, none adopted):** `2026-09-29-heartbeat-retirement-policy.md`, `card-record-schema-contract.md`, `content-pipe-disposition-policy.md`, `blocked-prereq-token-action-ready-proposal.md`, `ledger-row-weight-proposal.md` (09-30), `lookalike-filename-r5-exception-proposal.md` (09-30), `scratch-disposition-tmp-scans-proposal.md` (09-30), `standing-java6-construct-gate-proposal.md` (09-30), `b5-1008-gitignore-patch-draft.md` (09-30). The retirement-policy, row-weight, and pipe-disposition proposals directly address §6 risks — track, do not copy yet.
- **Live three-signal example:** claim `B5-1047` (`solar-pro4:free`, `started_utc` 2026-09-30T09:33:27Z, scope `DeckLoader.java`) with owner heartbeat keeping it LIVE while its row flipped BLOCKED→OPEN with bare note cells (recorded in B5-1415 DONE entry 2026-09-30). Confirms the manual's three-signal semantics AND surfaces the anonymous-transition gap (§6.1).

## 5. Replication-kit deltas (what to copy vs. avoid)

- **COPY (additive to manual §12 kit):** R7 identity text as part of the heartbeat README; the clock rule (tool-written timestamps or filesystem-mtime reads; authored `started_utc` never trusted for reap/offer); the clock report's graded DISAGREE/CLUSTER/idle/ok table shape for any liveness audit.
- **AVOID (do not replicate):** bulk-mtime operations over coordination dirs (they void 87/125 mtime signals here); status transitions with bare note cells (two same-day anonymous flips — B5-1047 row and one other cited in B5-1415); carrying `B5-1014-harness.ps1` into a kit (unprovenanced); carrying the verification battery without reviewing its boot-wiring (assessed as a tool, not yet adopted as kit).
- **HOLD (pending adjudication):** heartbeat-retirement policy, ledger row-weight, content-pipe disposition, lookalike-filename R5 exception. All four solve real §6 problems; none has passed a gate.

## 6. Gaps/risks for the fundamental system (improvement backlog)

1. **Anonymous status transitions.** Note-less flips (B5-1415: second same-day case) break forensic reconstruction. Fundamental fix: the ledger writer MUST be identified per transition (claim-owner or human-admin tag), and a bare-cell flip MUST fail a doc-gate check.
2. **Heartbeat-store growth without retirement.** 88→125 files/day-rate projects hundreds within a week; retirement policy is proposed but unadopted. Fundamental fix: adopt a bounded-retention rule (quarantine-then-archive, never silent delete) before v2.
3. **Bulk mtime voids the filesystem clock.** Any checkout-wide operation (re-clone, bulk touch, timezone shift) destroys the mtime signal the clock rule depends on. Fundamental fix: tool-stamped claim times (tool writes `started_utc` at creation) so at least one signal survives bulk-mtime events.
4. **Dirty-tree triage load.** B5-1415 classified 13 foreign hunks across 4 files read-only; the convention that saved it was in-file row labels. Fundamental fix: every uncommitted hunk SHOULD carry its owning row id in a comment, so triage never needs style attribution.
5. **Ledger-row weight / census cost.** 662 rows with 555 DONE retained inline; row-weight proposal (09-30) is the right thread — census tools already suppress live-claim rows, but DONE-row scan cost grows linearly.

## 7. Re-issue recommendation

File a full **manual v2** once: (a) heartbeat-retirement adjudicates one way or the other, (b) row-weight or an archiving rule lands, (c) pipe-disposition adjudicates, and (d) B5-1014-harness.ps1 either gains provenance or is dropped, and the verification battery's boot-wiring is reviewed (tool assessment already on record). Until then this amendment + the clock-integrity report constitute the complete delta; no manual section needs rewriting today.

## 8. Guesses and limits `[GUESS]`

- `[GUESS]` "New since 09-29" for proposals/tools uses file mtimes (git log unavailable in-session); mtime bulk-assignment (cf. clock report §IN-BULK-CLUSTER) can misdate files — the named B5 task IDs and DECISIONS entries are the cross-check, not the timestamp.
- `[GUESS]` B5-1049's substance (cost-gate surfacing in UI) is cited from the AGENTS.md assessor note only; the ruling text was not re-read here.
- Counts are point-in-time 2026-09-30 and will drift; statuses sum from the live ledger's Status column.

## 9. Reusable lesson

A replication manual rots the moment identity rules, timestamp trust, or retention policy move — version the amendment, not the memory: every post-manual governance edit gets a dated delta file, or the next repo copies yesterday's guarantees against today's failure.
