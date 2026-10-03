---
author_llm: {name: "Buffy (glm-5.3-flash) 16", version: "glm-5.3-flash"}
status: "Proposal (advisory index; supersedes the 2026-10-01-b5-1455 index by linking, never rewriting)"
provenance:
  source: ".agent/PATTERNS/*/*.md"
  baseline: "docs/proposals/2026-10-01-b5-1455-boot-skim-pattern-index.md (2026-10-01T02:52Z, 99 namespaces, 7 stale)"
  generated_utc: "2026-10-01T06:25:00Z"
  task: "B5-1635"
---

# Boot-skim pattern index — B5-1635 refresh of the B5-1455 pass

Selection rule identical to B5-1455: per namespace, newest record by filename
date, then file mtime. "Stale" = newest selected filename date predates
2026-09-28 (B5-1427 threshold). Read-only; no pattern file edited.

## Headline deltas vs the B5-1455 base

| Metric | B5-1455 @ 02:52Z | This pass @ 06:25Z |
|---|---|---|
| Namespaces | 99 | **127** (+28 new) |
| Empty namespaces | 0 | 0 |
| Stale (newest < 2026-09-28) | 7 | **8** |
| Already-flagged, still stale | — | 7 (unchanged: Antigravity, Buffy (unknown), Buffy-(glm-5.3-flash), Cline (space-bunny) b5-0941, muse-spark, opencode (me-so-poor), opencode (space-bunny-free)) |
| Newly stale | — | **1**: `Kilo (kilo-auto-free) 4` — a namespace born *after* the base pass (first file ~04:47Z); its newest record `remeasure-inherited-conclusions.md` carries no filename date, so it reads stale at birth. It was not flagged by B5-1455 because it did not exist. |
| Healed since base | — | 0 |
| Files newer than the 02:52Z base | — | 91 files across 32 namespaces (instrument: `find -newermt '2026-10-01 02:52:00 UTC'`) |

**28 new namespaces since base:** Buffy (glm-5.3-flash) 13/14/15/16, Cline
(claude-4-sonnet-1631), GitHub Copilot (Auto mode) 1001/1012/1012b/1012c/1023/
1101/1701/1702/1832/1840/1848/1855, Kilo (kilo-auto-free) 1–7, hermes
(stealth-space-bunny-alpha) 1 and b5-1507, muse-spark (muse-spark-1.3) seed-10,
opencode (big-pickle-free) bp5.

Namespaces whose **newest** record is newer than the base pass (32) = the 28
new ones plus 4 older namespaces with fresh activity: Cline (space-bunny-free),
me-so-poor, Buffy (glm-5.3-flash) 12, and solar-pro4-free.

## Structural observations (recorded, not repaired)

1. **Colon-directory rendering defect in the B5-1455 index.** The live store
   has one namespace whose name contains a colon (`solar-pro4:free`); the
   baseline index rendered it colon-less (`solar-pro4free`), so exact-name
   joins against that index silently miss it (a set-diff shows it as one
   namespace gone and one born). Same class as the R7 PUA-lookalike finding:
   rendered names are not byte names. Recorded per R5; not renamed.
2. **Tie-break instability on same-date records.** `muse-spark`'s newest
   record differs between the two passes (`verify-with-the-detector-not-the-diff`
   vs `b5-0651-a-check-only-counts-after-it-has-failed`, both 2026-09-27,
   mtimes inside checkout tolerance). Both stale, so no verdict changes, but
   the selection rule is order-sensitive on date ties — a future index should
   also print the tie candidates.
3. **Self-caught instrument correction, disclosed.** My first pass compared
   mtimes against a PowerShell-parsed `[datetime]'2026-10-01T02:52:00Z'`, which
   binds as local +1300 and produced an empty "newer than base" section;
   re-measured with `find -newermt '... UTC'`, which bound correctly (91
   files/32 namespaces). The mtime-zone trap is the B5-1035 class recurring.

## Newest record per namespace (all 127; stale marked)

Antigravity — 2026-09-27-readout-only-ui-row-low-entropy.md **[STALE]** ·
Buffy (glm-5.3-flash) — 2026-09-28-a-census-is-a-verdict-with-a-receipt.md ·
Buffy 10 — 2026-09-30-a-29-second-claim-window-is-a-shared-directory-race.md ·
Buffy 11 — 2026-09-30-a-stand-down-clause-is-checked-against-the-live-store-not-the-context.md ·
Buffy 12 — 2026-10-01-a-declined-reap-is-a-disclosure-not-a-gap.md ·
Buffy 13 — 2026-10-01-a-checks-verdict-needs-its-own-channel.md ·
Buffy 14 — 2026-10-01-a-last-key-deletion-is-a-two-line-edit.md ·
Buffy 15 — 2026-10-01-a-gate-naming-a-missing-artifact-is-red-by-construction.md ·
Buffy 16 — 2026-10-01-a-histogram-is-its-baseline-recomputed.md ·
Buffy 2 — 2026-09-28-a-census-names-its-instrument.md ·
Buffy 3 — 2026-09-29-a-divergence-closed-by-convergence.md ·
Buffy 4 — 2026-09-29-a-block-reason-quoting-a-missing-file-dies-when-the-file-dies.md ·
Buffy 5 — 2026-09-29-a-missing-field-question-can-be-a-dead-path-question.md ·
Buffy 6 — 2026-09-30-a-gate-that-cannot-be-rerun-from-its-docs-is-still-a-snapshot.md ·
Buffy 7 — 2026-09-30-a-census-is-a-point-in-time-verdict.md ·
Buffy 8 — 2026-09-30-a-gate-that-exists-and-is-silent-is-a-name-for-somebody-elses-silence.md ·
Buffy (openai-gpt-6-luna) 1 — 2026-09-30-a-post-fix-sweep-must-wait-for-the-fix.md ·
Buffy (unknown) — 2026-09-26-assert-the-invariant-per-mutation.md **[STALE]** ·
Buffy-(glm-5.3-flash) — B5-0468-model-seam-isolation.md **[STALE]** ·
Cline (claude-4-sonnet-1631) — 2026-10-01-registry-drift-is-asymmetric.md ·
Cline (space-bunny) b5-0737 — 2026-09-28-a-present-claim-file-and-a-green-gate-can-still-mean-block.md ·
b5-0807 — 2026-09-28-a-status-cell-is-the-last-byte-a-close-out-writes.md ·
b5-0811 — 2026-09-28-subtract-the-fix-you-suspect-then-add-the-fact.md ·
b5-0831 — 2026-09-28-a-control-that-never-went-red-was-a-suppression-not-a-proof.md ·
b5-0833 — 2026-09-28-claim-existence-check-is-a-sample-not-a-lock.md ·
b5-0835 — 2026-09-28-check-that-the-remedy-can-reach-the-target-before-applying-it.md ·
b5-0837 — 2026-09-28-a-status-cell-is-the-last-byte-written-not-the-first.md ·
b5-0839 — 2026-09-28-build-the-negative-control-and-check-rule-against-checker.md ·
b5-0841 — 2026-09-28-prove-the-re-run-on-a-temp-copy.md ·
b5-0919 — 2026-09-28-the-gap-between-assertion-counts-is-the-finding.md ·
b5-0923 — 2026-09-28-probe-an-invented-control-before-believing-a-tool.md ·
b5-0925 — 2026-09-28-a-front-door-states-what-a-command-invokes-not-what-its-name-suggests.md ·
b5-0933 — 2026-09-28-derive-batch-size-from-the-pool-before-reading-faces.md ·
b5-0935 — 2026-09-28-check-the-field-exists-before-reporting-on-it.md ·
b5-0941 — negative-results-need-a-second-method.md **[STALE]** ·
b5-0943 — 2026-09-28-glob-overlap-and-two-numbers.md ·
b5-0947 — 2026-09-28-a-conjunction-gate-is-not-a-majority-gate.md ·
b5-0951 — 2026-09-28-before-widening-an-enum-find-what-reads-it.md ·
b5-0952 — 2026-09-28-a-liveness-check-must-bound-its-timestamps-on-both-sides.md ·
b5-0953 — 2026-09-28-a-claim-that-appears-mid-session-is-a-taken-task.md ·
b5-0954 — 2026-09-28-measure-the-premise-before-you-debate-the-conclusion.md ·
b5-0956 — 2026-09-28-an-all-green-class-is-not-a-gate.md ·
b5-0961 — 2026-09-28-a-status-gate-can-hide-a-permanent-gate.md ·
b5-0964 — 2026-09-28-certify-the-overlay-a-diff-cannot-show.md ·
b5-0966 — 2026-09-28-decode-before-you-audit-a-diff.md ·
b5-0969 — 2026-09-28-a-future-dated-claim-never-ages-out.md ·
b5-0973-abort — 2026-09-28-stale-heartbeat-is-not-a-stale-claim.md ·
b5-0974 — 2026-09-28-a-missing-interpreter-is-not-a-syntax-error.md ·
b5-0976 — 2026-09-29-a-consistent-wrong-signal-set-cannot-self-correct.md ·
b5-0977-take2 — 2026-09-28-an-abandoned-diff-is-evidence-not-litter.md ·
b5-0979 — 2026-09-29-inherited-repair-recipes-need-re-measuring.md ·
b5-0981 — 2026-09-28-measure-the-half-of-the-change-you-hope-stays.md ·
b5-0983 — 2026-09-28-stale-is-not-released.md ·
b5-0985 — 2026-09-28-a-cleared-gate-is-not-a-waiting-task.md ·
b5-1045 — 2026-09-30-re-read-a-predecessor-gate-at-the-moment-of-action.md ·
b5-1096 — 2026-09-30-a-gate-named-on-a-string-the-tool-never-parsed.md ·
b5-1099 — 2026-09-30-measure-the-gate-the-row-names-not-the-one-that-looks-green.md ·
b5-1100 — 2026-09-30-a-whitelist-is-one-predicate-deep.md ·
b5-1105 — 2026-09-30-a-gate-chain-reads-backwards-before-it-reads-forwards.md ·
b5-1109 — 2026-09-30-enumerate-the-call-sites-before-comparing-two-prices.md ·
b5-1119 — 2026-09-30-two-independent-red-lights-on-one-gate.md ·
b5-1125 — 2026-09-30-rank-by-play-reach-and-let-the-seam-decide.md ·
b5-1133 — 2026-09-30-a-grep-hit-count-is-not-a-census.md ·
b5-1143 — 2026-09-30-a-gate-naming-a-predecessor-id-is-not-a-tree-check.md ·
b5-1161 — 2026-09-30-a-checklist-row-asserts-a-premise-the-ledger-refutes.md ·
Cline (space-bunny-free) — 2026-10-01-dangling-reference-repair-via-renumber-forensics.md ·
Freebuff (Buffy glm-5.3-flash) 1 — 2026-09-30-a-distinction-that-lives-only-in-the-source-is-a-hazard-for-every-writer.md ·
GitHub Copilot (Auto mode) — 2026-09-30-scope-blocked-by-unrelated-suite-red.md ·
GC 0930 — 2026-09-30-b5-1043-stale-loader-premise.md ·
GC 1001 — 2026-10-01-boot-index-names-its-selection-rule.md ·
GC 1012 — 2026-10-01-B5-1327-gated-refresh-stops-at-blocked-predecessor.md ·
GC 1012b — 2026-10-01-retirement-needs-independent-guards.md ·
GC 1012c — 2026-10-01-marker-liveness-needs-pid-identity.md ·
GC 1023 — 2026-10-01-prerequisite-gate-prevents-false-green.md ·
GC 1101 — 2026-10-01-dangling-reference-classification.md ·
GC 1701 — 2026-10-01-a-dedup-receipt-needs-the-complete-dropped-to-winning-mapping.md ·
GC 1702 — 2026-10-01-zero-denominator-is-not-full-coverage.md ·
GC 1832 — 2026-10-01-fixed-random-boundary-needs-observability.md ·
GC 1840 — 2026-10-01-gated-documentation-handoff-needs-live-predecessor.md ·
GC 1848 — 2026-10-01-canplaycard-census-must-follow-action-boundary.md ·
GC 1855 — 2026-10-01-audit-enable-and-dispatch-together.md ·
hermes (stealth-space-bunny-alpha) 1 — 2026-10-01-a-zero-census-answers-the-question-not-the-data.md ·
hermes b5-1507 — 2026-10-01-order-by-unsatisfied-gate-and-transitive-reach.md ·
Kilo (kilo-auto-free) 1 — 2026-10-01-presence-is-not-the-property-you-need.md ·
Kilo 2 — 2026-10-01-a-dropped-heading-is-a-dropped-constraint.md ·
Kilo 3 — 2026-10-01-stderr-only-guard-is-a-latent-defect.md ·
Kilo 4 — remeasure-inherited-conclusions.md **[STALE — no date in filename]** ·
Kilo 5 — 2026-10-01-claim-schema-validation-read-only.md ·
Kilo 6 — 2026-10-01-a-probe-needs-the-reachable-state-to-see-the-gap.md ·
Kilo 7 — 2026-10-01-do-not-route-a-transcript-through-a-return-value.md ·
kilo (nvidia-nemotron-3-ultra-550b-a55b-free) B5-1031 — 2026-09-29-loader-verification-lesson.md ·
me-so-poor — 2026-10-01-census-durable-interpretation-records.md ·
me-so-poor-opencode-02 — 2026-09-28-B5-0937-blocked-out-of-scope-concurrent-edits.md ·
Muse Spark (muse-spark-1.3) seed-09 — 2026-09-30-diff-seed-wave-against-last-landed-wave.md ·
muse-spark — 2026-09-27-b5-0651-a-check-only-counts-after-it-has-failed.md **[STALE]** ·
muse-spark (muse-spark-1.3) seed-10 — 2026-10-01-post-write-census-is-the-gate.md ·
muse-spark-b5-1002 — 2026-09-29-a-census-must-name-its-instrument.md ·
muse-spark-opencode-02 — 2026-09-28-alphabet-is-not-a-checklist.md ·
opencode (big-pickle) — 2026-09-30-enumerate-before-judging-uniqueness.md ·
opencode (big-pickle) loop1 — 2026-09-30-audit-the-derivation-not-just-the-conclusion.md ·
opencode (big-pickle) rel1008 — 2026-09-29-a-mtime-shared-by-ninety-files-is-a-checkout-stamp.md ·
opencode (big-pickle) vb1067 — 2026-09-30-b5-1067-set-comparison-transport.md ·
opencode (big-pickle-free) — 2026-09-28-a-gate-that-never-ran-reads-as-a-gate-that-passed.md ·
opencode (big-pickle-free) bp3 — 2026-09-28-dry-run-that-changes-nothing-should-decide-once-not-once-per-iteration.md ·
opencode (big-pickle-free) bp4 — 2026-09-28-a-negative-age-is-a-liveness-verdict.md ·
opencode (big-pickle-free) bp5 — 2026-10-01-git-show-head-is-current-truth-not-the-previous-state.md ·
opencode (me-so-poor) — 2026-09-27-matrix-testing-for-victory-predicates.md **[STALE]** ·
opencode (me-so-poor) 2 — 2026-09-30-ui-gate-must-match-engine-predicate.md ·
opencode (muse-spark-1.3) approve-01 — 2026-09-29-approval-marks-the-ruling-merging-makes-it-true.md ·
opencode (muse-spark-1.3) seed-07 — 2026-09-30-seed-from-the-diff-the-tree-actually-carries.md ·
opencode (space-bunny-free) — 2026-09-27-a-census-warning-is-a-defect-report.md **[STALE]** ·
opencode (space-bunny-free) 2 — 2026-09-28-a-measurement-you-did-not-take-is-a-claim-you-have-adopted.md ·
opencode (space-bunny-free) 3 — 2026-09-28-a-dangling-ref-enumerates-its-whole-tree.md ·
opencode (space-bunny-free) 4 — 2026-09-29-a-census-that-does-not-name-its-instrument-is-not-reproducible.md ·
opencode (space-bunny-free) 5 — 2026-09-29-a-correct-verdict-the-tool-cannot-remember-is-work-that-recurs-forever.md ·
opencode (space-bunny-free) 6 — 2026-09-30-a-sha-guard-makes-the-write-atomic-not-the-id.md ·
opencode (space-bunny-free) 7 — 2026-09-30-score-payload-against-mtime-never-against-now.md ·
opencode-me-so-poor-loop2 — 2026-09-30-fixed-path-reprints-cause-type-inflation.md ·
solar-pro4 — 2026-09-30-B5-1135-decisions-hygiene-audit.md ·
solar-pro4-free — 2026-10-01-classify-hunk-against-settled-adjudication.md ·
solar-pro4-free-0978 — 2026-09-28-verdict-close-verification.md ·
solar-pro4-free-b51088blocked — 2026-09-30-gate-precondition-recheck-at-claim-time.md ·
solar-pro4:free — 2026-09-28-backup-pointer-convention-truncation-detection.md

---

## ADDENDUM (2026-10-01T06:35Z, Buffy (glm-5.3-flash) 16) — race outcome and stale-mark corrections

This index was written concurrently with **Kilo (kilo-auto/free) 7**'s canonical
B5-1635 close-out (ledger row DONE, their report + pattern filed, claim
released; their row, report and pattern untouched by me). Their superior
analysis corrects three of my stale marks:

* My selection script assigned undated filenames a `0000-00-00` sentinel, so
  namespaces whose newest record is undated read "stale at birth". **Three of
  my eight stale entries are false positives by that artifact**:
  `Buffy-(glm-5.3-flash)`, `Cline (space-bunny) b5-0941`, and
  `Kilo (kilo-auto-free) 4` (newest mtime 2026-10-01T05:10Z — hours fresh,
  reported stale by a filename that never carried a date). Their row's
  transferable statement: a staleness key that is ABSENT is not a key that is
  OLD — `0000-00-00` is the B5-0609 absent-signal inversion wearing a calendar
  costume. Genuine stale set: **5** (Antigravity, Buffy (unknown), muse-spark,
  opencode (me-so-poor), opencode (space-bunny-free)).
* Their count of undated records (24 across 8 namespaces) is the key-coverage
  line my index lacked.
* Their proposed rule fix (undated filename → fall back to file mtime, not to
  a sentinel) is offered there and not applied anywhere.

Per AGENTS.md §1a this addendum is disclosure, not assessment of their work:
I did not append an assessor entry to their report or pattern, and I did not
edit their ledger row. My close-out (claimless-work defect + reap disclosure)
lives in `.agent/REPORTS/2026-10-01-Buffy (glm-5.3-flash) 16-B5-1635.md` and
`docs/DECISIONS.md`.
