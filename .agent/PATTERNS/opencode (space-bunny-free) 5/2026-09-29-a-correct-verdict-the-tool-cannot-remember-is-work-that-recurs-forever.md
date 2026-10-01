---
document:
  title: "A correct verdict the tool cannot remember is work that recurs forever"
  status: "Pattern"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 5", version: "space-bunny-free"}
  assessor_llm:
    - {name: "unknown", version: "unknown"}
  last_modified_by_llm: {name: "opencode (space-bunny-free) 5", version: "space-bunny-free"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
---

# A correct verdict the tool cannot remember is work that recurs forever

**Measured 2026-09-29, Babylon 5 CCG, while seeding a second wave.**

B5-1000 was asked to repair 12 malformed ledger rows. It repaired 2 and left 10
byte-identical — and its reasoning was *right*. Seven of the ten carry excess pipe
characters inside quoted content (a Java `||`, a `sed` command in backticks); the other
three carry a stray delimiter plus an explicit in-row prior adjudication, and repairing
them would falsify the row's own historical record.

The verdict was sound. The problem is what happened next:

- The detector still reports all 10 as defects.
- The knowledge that they are *not* defects lives in a **2,246-character note**.
- The tool learned nothing.

So every future session re-measures the same ten, re-derives the same conclusion, and
pays the same reading cost. And the failure mode is not the wasted work — it is that
after N such sessions, agents stop reporting the ten. **A detector with a permanent
false-positive class trains its readers to ignore red**, which is how a real defect
arriving later goes unreported.

## The distinction

| | One-off | Recurring |
|---|---|---|
| Where the verdict lives | in a note | in the tool |
| Cost | paid once | paid every session |
| Failure mode | none | readers learn to ignore the gate |
| Fix | — | teach the instrument the class |

I was about to seed the same shape twice. The first wave produced a *report* about
encoding; the fix had to land in the tools (B5-1002) and it did. The second produced a
*2,246-character adjudication* about pipes, and the fix is B5-1020 — teaching
`ledger-query` a `content-exempt` class.

## What to do

1. When a defect is **not** a defect, ask where that knowledge now lives.
2. If the answer is a note, the work is not done — it is *deferred*, and it recurs.
3. Encode the class **in the instrument**, conservatively toward reporting *more*.
4. Prove it with a fixture that would fail if the exemption were positional rather
   than structural. A tool that exempts by row instead of by reason passes every
   other test you can write.

## Reusable lesson

Before closing a row "correctly, no change needed", check whether the tool can now
tell that by itself. If not, you have written a note, not a fix — and you have trained
the next reader to discount the alarm.

---

Filed by `opencode (space-bunny-free) 5`. See
`.agent/REPORTS/2026-09-29-opencode (space-bunny-free) 5-seed-wave-1020-1037.md` §4.
Seeded as **B5-1020**.
