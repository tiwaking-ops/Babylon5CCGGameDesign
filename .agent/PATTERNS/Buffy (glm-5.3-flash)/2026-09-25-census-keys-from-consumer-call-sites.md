---
document:
  title: "Pattern — derive census key vocabulary from the consumer, not from guesses"
  status: "Pattern record (advisory, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: census keys come from the consumer's call sites

Filed under the standing "Reusable lesson" convention (TASK_LEDGER QUEUE 0430 note).
First applied in `.agent/REPORTS/2026-09-25-Buffy-(glm-5.3-flash)-B5-0420.md`.

## The lesson

A values census must derive its key vocabulary from the **consumer** of the data —
the loader's `boolVal`/`intVal`/`getOrDefault` call sites — never from a guess,
another report's wording, or the most natural-sounding key name.

In B5-0420 the first census pass counted a `majorAgenda` key (natural guess) and
found 0 hits; the loader actually reads `isMajorAgenda` (DeckLoader.java:275) and
the real answer was 12 rows (6 per set). Because `getOrDefault(m, "isMajorAgenda",
false)` fails **silently** to False on a wrong-keyed row, a census built on a guessed
key does not error — it confidently undercounts. That is the trap: silent-False
misreads look exactly like clean data.

## Procedure

1. Before counting, grep the consumer (`DeckLoader.java`) for every
   `getOrDefault(`/`intVal(`/`boolVal(` call on the card type in scope; that list IS
   the schema.
2. Count with those exact key names; also record per-key presence (rows carrying
   the key vs. defaulted), so silent defaults are visible.
3. Cross-check any flag against a second source (subtype, enum value) — the
   isMajorAgenda↔AGENDA_MAJOR perfect correlation is what makes the corrected
   count trustworthy.
