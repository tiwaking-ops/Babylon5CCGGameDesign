---
document:
  title: "Check what your fixture actually exercises"
  status: "Pattern"
  provenance_note: "Advisory only, per AGENTS.md section 6. Never canonical; citing confers no authority."
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Check what your fixture actually exercises

**Reusable lesson (B5-0719, 2026-09-27).** A harness can reproduce a symptom
perfectly while testing the wrong case. When the first result is nonsensical,
suspect the **fixture** before the code.

## What happened

The harness compared two census tools' liveness verdicts. The first run said
**both tools diverged on every case** — a result so uniform it was obviously
not measuring anything.

The cause: the tools read a heartbeat's **file mtime**, not its `utc` field. My
fixture wrote `"utc": "<40 minutes ago>"` but did not back-date the file's
mtime, so the tools read the heartbeat as **age 0** — live. The fixture was
silently testing "fresh heartbeat" while claiming to test "stale heartbeat".

The fix was one line — `(Get-Item $p).LastWriteTimeUtc = $now.AddMinutes(-$minsAgo)`
— and it converted a meaningless result into the real finding.

## The rule

1. **A suspiciously uniform result is a fixture smell, not a finding.** If every
   case agrees, or every case disagrees, ask what your fixture is *actually*
   feeding the code before believing the output.
2. **Match the fixture to the input the code really reads.** Ask "which field
   does this function touch?" and set *that* one. Writing a plausible-looking
   value into a neighbouring field is the easiest way to build a test that
   passes for the wrong reason.
3. **Make the expectation explicit and let the run falsify it.** The corrected
   harness printed `expect 1` / `expect 2` beside the counts. The first run's
   expectation was *wrong in a way that was visible*, which is exactly how the
   bug surfaced rather than hiding.

## The second-order lesson

This is the same shape as the repository's own recurring class: **an absent
signal read as a value.** The mtime/`utc` confusion is a fixture supplying
absence-where-a-value-was-expected; the B5-0719 finding it then uncovered is a
tool reading a *nonexistent directory* as "no report signal" rather than as an
error. Both are `UNKNOWN` silently becoming `false`. The `if (Test-Path ...)`
guard that makes the shipped tools skip a bad path without complaint is the same
failure mode in production form.

## Supersedes

Nothing. Sixth record in this namespace; see also
`2026-09-27-an-empty-census-is-not-evidence-of-an-empty-queue.md`,
`2026-09-27-the-code-outranks-the-summary-line.md`,
`2026-09-27-measure-the-gap-by-searching-the-layer-you-are-allowed-to-touch.md`,
`2026-09-27-a-null-result-is-a-result-record-it-as-one.md` and
`2026-09-27-a-fixed-premise-is-not-a-defect.md`.
