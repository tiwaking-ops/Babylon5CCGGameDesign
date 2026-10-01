---
document:
  title: "Measure the ratio before diagnosing the queue; the split beats the cap and the cap beats the archive"
  status: "Pattern (advisory; B5-0430 store, same tier as investigations/)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1034"
---

# Pattern: measure the ratio before diagnosing the queue

**Context.** Two adjacent rows (B5-1015, B5-1034) asked whether the ledger
is growing faster than it closes. B5-1015's premise was built on a one-day
snapshot; the week-long instrument (creation from QUEUE wave notes,
closures from first date in each DONE row's Verified cell) says the
created/closed ratio oscillated 0.59–1.27 — equilibrium, not growth. The
monotonic growth was in the prose: mean Task+Verified payload ~1,871 chars
per row, 74% of the ledger's bytes.

**Lesson 1 — a ratio measured over one day is an anecdote; over a week, a
trend.** Both instruments were cheap (one regex pass each) and both named.
The snapshot's dramatic framing did not survive the week view.

**Lesson 2 — when the data contradicts the row's premise, the premise is
the finding.** The row expected to adjudicate a growing backlog; the
measured equilibrium redirects the recommendation entirely — toward the
cost per row rather than the count of rows.

**Lesson 3 — the split (short row + long report) dominates a bare length
cap.** A cap fights the provenance culture (receipts *are* the culture);
the split relocates the receipt to the document that already must exist,
making the culture cheaper instead of shallower. An archive threshold is
the strongest lever and the most dangerous — the pass counts, the erratum
precedent, and every grep-based audit assume rows stay; leave it behind a
human ruling.

**Lesson 4 — parser bugs manufacture phantom findings.** The first
measurement pass mis-indexed the date column and reported 504 rows with no
close date. The signature of a parser bug is a number that is too extreme
to be true; re-derive before publishing any census.

Links: [B5-1034 report](../../REPORTS/2026-09-30-Freebuff%20(Buffy%20glm-5.3-flash)%201-B5-1034.md) ·
companion measurement to B5-1015; instrument discipline per B5-1030.
