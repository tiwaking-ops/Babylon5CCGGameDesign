---
document:
  title: "A grep hit count is not a census — and a count is not the thing being counted"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-1133", version: "space-bunny"}
  created_date: "2026-09-30"
  task: "B5-1133"
---

# Pattern: a grep hit count is not a census

B5-1133 was asked to reconcile 63 test methods against "11 SECTION hits" in
`HeadlessConformanceTest.java`. The 11 is a **case-insensitive grep hit count on
the English word**: `-CaseSensitive` returns 0. All 11 lines are the word
*section* inside comments (one of them inside a `println` message string). The
real inventory is **75 distinct section codes over 63 methods, 681 check sites**,
and **0 methods carry no code at all**.

Two different numbers, both true, and the seed's framing makes them look like a
regression. Read the seed's number at face value and you conclude "the suite lost
52 sections" — a confident, entirely false finding that invites a repair nobody
needs.

Rules this pattern fixes:

1. **A hit count is not a census.** A census enumerates the *instances of the
   thing*. A grep enumerates *lines matching a pattern*, which includes prose
   about the thing, strings naming the thing, and — the classic — the thing
   itself at a different case. When the pattern is a word that English code
   comments also use, the two are not comparable.
2. **Count instances, then reconcile, then report both.** Here the reconciliation
   is what carries the information: 63 methods → 77 method-code pairs → 75
   distinct codes, with the 77-vs-75 gap fully explained by `ROT` (two methods)
   and `E3` (two methods). Publishing "75 codes" alone would have hidden that two
   codes are shared, which is exactly the kind of thing a future reader needs to
   know before assuming a code identifies one test.
3. **"Which carry no code" is a first-class column, and 0 is a result.** The row
   asked for it, so it gets its own line. A census that publishes a total but not
   its coverage column leaves the reader to assume the worst.

The companion lesson is about the other kind of missing number. The row also asked
for full-suite wall-time, and the suite **aborts** before its tally line. The
stopwatch read 314 ms — that is time-to-crash, not a duration. Reporting it as the
suite's wall-time would be a fabricated benchmark that looks like a measurement.
`UNKNOWN`, with the reason (the run aborts at `testParticipation`), is the honest
verdict and is a first-class one in this repo's liveness vocabulary for exactly
this reason.

Reusable lesson: before adopting a number a seed row hands you, count the thing
yourself and report both figures with the reconciliation between them — and when
the measurement you were asked for is unavailable, say `UNKNOWN` and why rather
than substituting the nearest number you happened to observe.
