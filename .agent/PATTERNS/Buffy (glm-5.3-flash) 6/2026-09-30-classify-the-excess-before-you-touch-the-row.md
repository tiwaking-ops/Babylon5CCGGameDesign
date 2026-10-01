---
document:
  title: "Classify the excess by offset before you touch the row — a double-lead repair is one byte, not a re-balance"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 6", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1039"
---

# Pattern: the offset tells you the repair

B5-1039's target row read 8 pipes / doubleLead yes. Two repairs were possible:
(a) remove one byte at the front, (b) "re-balance" cells by editing the row's
cell structure. They are not equivalent, and only one is lawful under B5-0568's
preserve-content-pipes rule — and which one is right depends entirely on
*where* the excess pipe sits.

1. **Offset the cells before editing.** Aligning the defective row against a
   conforming row cell-by-cell shows a double-lead row has its excess at byte 1
   and nowhere else. If any excess pipe sits *inside* a quoted span or an
   adjudicated span, that is content and stays byte-identical (the B5-1020
   exempt class); if it sits between cells, it is structural. Same symptom,
   different repairs.
2. **A one-byte repair deserves a whole-file proof.** The splice itself is
   trivial; the evidence is what prevents the next session from suspecting
   collateral damage. Keep a pre-splice copy, record the SHA before, and diff
   the whole file after: "exactly one line differs, lead pipe only, file one
   byte shorter" is a complete receipt. (A byte-diff tool will report ~100k
   shifted offsets for a one-byte deletion — the line diff is the proof, the
   cmp count is noise. Name which is which in the report.)
3. **The repaired row must leave the defect census, not merely look better.**
   The pass condition is `7 / no / reportable` from the shipped detector, not
   a visual check. doubleLead rows still *parse* (the runner tolerates them
   since B5-0613), which is exactly why they can sit unrepaired for census
   after census: tolerance is not health.
4. **Re-census the target at claim time, not at seed time.** The row said
   "8 pipes, doubleLead yes" from a 2026-09-29 pass; by claim time it had to
   be re-measured live (it was, unchanged) and checked for a live claim —
   standing down without touching the row is a designed outcome, not a
   failure.

Reusable lesson: measure the offset, not the count — the count tells you a row
is broken, the offset tells you the one lawful way to fix it.
