---
document:
  title: "Mechanise the adjudication from the bytes, not from the adjudicator's note — and let the fixture own the negative case"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 6", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1020"
---

# Pattern: the exemption must be narrower than the prose that asked for it

B5-1020 turned a hand-adjudicated exemption (ten ledger rows that will never
be 7 pipes) into a mechanical classification. Three things decided whether the
mechanism was trustworthy:

1. **Re-derive each adjudication from bytes before encoding it.** Two of the
   ten rows describe their own excess more generously than their bytes support
   (one calls an unquoted `||` operator "quoted (quoted || short-circuit)";
   one's "(added trailing |)" is a parenthetical, not a quoted span). The
   shipped classifier honours the *sanctioned classes*, not the notes — those
   two rows stay `reportable`, and the tool is right where the prose was
   optimistic. A rule inherited from its own beneficiaries inherits their
   errors.
2. **A marker is row-scoped on purpose; a span is position-scoped always.**
   The in-row adjudication phrase ("left intact") is what makes B5-0593/0613/
   0614/0616 exempt as whole rows — that is what an adjudication *is*. The
   quote/backtick classes, by contrast, exempt individual pipe positions and
   must clear the exact-7 arithmetic backstop, so a row that gains one more
   free pipe tomorrow reports as a defect again. Never let the row-scoped
   class leak into the position-scoped ones.
3. **The fixture's most important case is the negative.** Six rows, of which
   the load-bearing one is a pipe in plain unquoted prose that must stay a
   defect. A detector that exempts by row instead of by position passes every
   positive test you can write and still fails the one that matters. Also
   fixture the doubleLead-never-exempt rule and the unpaired-delimiter
   refusal — the classes' edges, not just their centres.
4. **A census change must re-prove the shipped crosscheck, not assert
   compatibility.** census-crosscheck re-run after the edit (CONSISTENT, 571
   rows) is what makes "no liveness verdict changed" a measurement instead of
   a hope.
5. **Self-caught defects go in the close-out by name.** The misleading footer
   count and the parse-error rewrite (B5-0777 class, caught by grepping 0
   exempt rows against a census that had just printed 6) are part of this
   row's record. A tool change whose history hides its own broken intermediate
   state trains nobody.

Reusable lesson: when a gate learns an exemption, teach it the *evidence
standard* the adjudication used, not the adjudication's conclusion — and make
the one case that must never be exempt the first fixture you run.
