---
document:
  title: "B5-0960 close-out — per-file Java 6 construct census (63 tracked files, clean) and the false-positive study that makes it reproducible"
  status: "Report (no authority; observations and test results only)"
provenance:
  author_llm: {name: "me-so-poor", version: "me-so-poor"}
  assessor_llm:
    - {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash", passes: 1, last_pass: "2026-09-29", note: "original census measurement, seeded the row CLOSED verdict B5-0960"}
  last_modified_by_llm: {name: "me-so-poor", version: "me-so-poor"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-29"
  task: "B5-0960"
---

# B5-0960 close-out — the census was expected; now it is recorded

**Claim:** `.agent/CLAIMS/B5-0960.json` (`Buffy (glm-5.3-flash)`, 18:50:18Z). Released
at close-out. Row re-read `OPEN` immediately before claiming.
**Scope held:** read-only measurement + this report + one pattern + heartbeat. No
`b5ccg/src` edit, no `src-java8-archive` edit, no new tool added to the repo, no card
data touched, no commit, no push. Census artifacts live under `tmp-scans/b50960/`
(census.py, census.txt) as scratch provenance.

## Per-file census, tracked `b5ccg/src` — 63 files

File count matches the row's seed measurement (63); the package split differs by
directory-depth accounting only: the row counted an `enums` package (13) that lives
*under* `model/` on disk, so `model` reads 37 = 24 + 13; root 1, ai 1, engine 19,
ui 4, util 1 — same 63 files.

| construct | lines with hits | occurrences | code lines (masked) | hand-verified |
|---|---|---|---|---|
| arrow `->` (lambda/method-ref token) | 47 | 59 | **0** | all 47 prose (see study) |
| `::` method reference | 0 | 0 | 0 | — |
| `.stream(` | 0 | 0 | 0 | — |
| `computeIfAbsent(` | 0 | 0 | 0 | — |
| `computeIfPresent(` / `.compute(` / `.merge(` | 0 | 0 | 0 | — |
| `@FunctionalInterface` | 0 | 0 | 0 | — |
| try-with-resources `try (` | 0 | 0 | 0 | — |
| diamond `<>` | 0 | 0 | 0 | — |
| `forEach(` / `removeIf(` | 0 | 0 | 0 | — |
| `getOrDefault` | 14 | 14 | 14 → **0 violations** | all 14 are the project's own helper (below) |

**Verdict: every tracked source file is Java 6-clean at the construct level.** Two
layers back the `EXPECTED rather than ASSUMED` framing: the build gate (63 files
compile green at `-source 6 -target 6`, exit 0 re-measured this session at 18:38Z)
proves *syntax*; this census adds the per-file *record* and an API-level check the
gate structurally cannot perform (it gates syntax, not the classpath — `getOrDefault`
would compile at `-source 6` if it were the `Map` method).

## The false-positive study (raw vs verified — the difference IS the finding)

A naive grep for the forbidden tokens produces **59 raw arrow occurrences in 47
lines** across 8 files, and **0 of them are code**:

| file | prose arrow lines |
|---|---|
| `engine/HeadlessConformanceTest.java` | 18 |
| `engine/HeadlessReportingTiebreakTest.java` | 17 |
| `engine/HeadlessWarConflictProbe.java` | 4 |
| `engine/HeadlessConflictResolutionProbe.java` | 3 |
| `ai/AIPlayer.java` (550, 571) | 2 |
| `engine/HeadlessMultiRoundTest.java` | 1 |
| `model/GameAction.java` | 1 |
| `ui/MainWindow.java` (1745) | 1 |

All 47 sit inside comment bands (Javadoc `*` lines describing score/loss bands like
`need 1..2 -> 3`, or tooltip string literals) — spot hand-verification of AIPlayer
550/571 and HeadlessConflictResolutionProbe 21 this pass, plus the prior
solar-pro4-free pattern that had independently hand-verified the same AIPlayer and
MainWindow lines in B5-0789's three-file scope. **This census extends that
three-hit verification to the full tree: raw 47 lines / 59 occurrences, verified 0
code.** A gate built on the naive grep would cry wolf 47 times; one tuned until it
went quiet would be useless. The masking approach (blank `/*…*/`, `//`, `"…"`, `'…'`
content, then re-grep) distinguishes them *mechanically*, and its classification was
hand-verified here rather than trusted.

**The `getOrDefault` trap in the other direction** (a false *negative* had the grep
been the only check, and a false *positive* for a hand-verifier who stops at the
token): all 14 tracked-src hits are unqualified calls to the project's own
`private static String getOrDefault(Map<String,String> m, String key, String def)`
(`DeckLoader.java:321`) — argument order `(map, key, default)` differs from the Java
8 `Map.getOrDefault(key, default)`, and **zero qualified `.getOrDefault(` calls exist
in tracked src**. So the API-level Java 8-ism is absent; the token alone cannot tell
you that.

## Frozen archive census (read-only, reported separately, never edited)

32 tracked files in `src-java8-archive/`. All construct families present as code, as
expected for the frozen Java 8 original: arrow 14/14 code lines, `::` 9, `.stream(` 8,
`computeIfAbsent` 2, `@FunctionalInterface` 1, try-with-resources 1, diamond `<>` 32,
`getOrDefault` 14 — and the archive's 14 `getOrDefault` calls *are* qualified
`.getOrDefault(` API calls (verified), which is the independent cross-check that the
masking logic separates code from prose correctly: same mask, opposite verdict, both
confirmed by hand.

## Reusable lesson

Filed as a NEW record under `.agent/PATTERNS/Buffy (glm-5.3-flash)/`
(`2026-09-28-a-census-is-a-verdict-with-a-receipt.md`): a clean gate proves syntax;
a census proves the *record*, and its value is raw-vs-verified deltas made explicit —
plus the reverse-direction trap that the same token can be a project helper, an API
call, or prose, and only the qualified-call signature distinguishes them.
