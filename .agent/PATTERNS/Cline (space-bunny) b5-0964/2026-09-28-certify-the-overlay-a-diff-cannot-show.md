---
document:
  title: "Certify the overlay a diff cannot show"
  status: "Advisory pattern (never canonical; .agent/PATTERNS/ tier)"
provenance:
  author_llm: {name: "Cline (space-bunny)", version: "space-bunny"}
  last_modified_by_llm: {name: "Cline (space-bunny)", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Certify the overlay a diff cannot show

**Reusable lesson (B5-0964):** a certification row should record the *overlay* facts a
diff cannot show, and the one grep hit worth reporting is usually a string literal.

**Rule.** When certifying an unread-then-reviewed diff:

1. **Pair the construct grep with a syntactic read of every hit.** A Java 6 grep for
   `->` over UI code returns the tooltip string `" -> "` before it returns a lambda.
   A grep that reports "1 hit" without saying *where the token sits* is not a
   conformance result; the finding is `string literal, not a construct`. Report the
   hit, not just its absence.
2. **Ask what a new `drawString`/`setText` lands ON TOP OF, not what it says.** Two
   added labels sharing a coordinate with a pre-existing string is a paint-order
   finding — real, cheap to state, and invisible in a line-by-line hunk read because
   each hunk is individually correct. It is *not* a behaviour change, and conflating
   the two makes the report less actionable.
3. **Separate label accuracy from action accuracy.** When a readout renders a
   different object than the one the commit path submits (here: hint says
   `legal.get(0)`, submit resolves the selection), the severity is *misleading
   label*, not *wrong action* — provided the commit re-checks the engine. State which
   one it is, because the fix sizes differ by an order of magnitude.
4. **Distrust a seeded measurement that will not reproduce, and say so.** The row
   note read `39 UI callbacks`; the run reported `43`. A non-reproducing figure in a
   certification row is itself a reportable observation — report the drift and why it
   is benign, rather than silently matching the row.

**Why:** the four checks above are all *absence-of-error* traps. A green compile and
a green conformance suite both pass over a paint-order overlap, a stale label, and a
token in a string literal equally well; the suite proves the engine, never the
overlay.

**Source:** `.agent/REPORTS/2026-09-28-Cline (space-bunny) b5-0964-B5-0964.md` (B5-0964).
