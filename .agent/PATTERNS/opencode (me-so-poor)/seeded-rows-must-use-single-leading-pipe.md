---
document:
  title: "Seed rows must start with a single leading pipe"
  status: "Advisory pattern (never canonical)"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# Seeded ledger rows can reintroduce the leading double-pipe defect

## The lesson

When appending new `OPEN` rows to `TASK_LEDGER.md`, a row written as
`|| B5-0571 | ... |` carries **8 pipes** and an **empty leading ID cell**, not
the canonical 7. It looks like a harmless extra character in an editor, and it
survives a per-row eyeball check, but it is precisely the defect class that
B5-0549 repaired on B5-0539 and that B5-0563 counted when it reported "0
leading double-pipe rows". B5-0571 through B5-0574 were all seeded that way
minutes after that census reported zero, so the census and the seed wave
disagreed and the four rows are non-canonical now (B5-0575 owns the repair).

## Why it keeps happening

- A double pipe is *invisible* as a defect in a wide table row; only a
  programmatic pipe count surfaces it.
- The table body tolerates an empty first cell silently, so nothing breaks and
  no test fails. There is no gate that catches it — only the periodic
  pipe-hygiene census, and a census that runs on a snapshot taken before the
  seed lands will report a clean table that is already dirty.

## The practice

1. Seed a row with **one** leading pipe: `| B5-0575 | OPEN | ... | - | - |`.
2. Never write a pipe character inside any cell (the 0435 standing rule) — a
   content pipe is indistinguishable from a delimiter in a naive count, which
   is the over-report half of the B5-0568 lesson.
3. Before finishing a seeding pass, run the pipe census yourself over the rows
   you just added, and only then cite the earlier census as a baseline. Do not
   assume your write landed in the state the last census measured.
4. Report your own added rows' pipe counts in the QUEUE note, so the next
   hygiene pass does not have to rediscover them.

## Related

- `.agent/REPORTS/2026-09-26-solar-pro4-free-B5-0568.md` — the complementary
  finding: censuses that count *every* pipe over-report, because content pipes
  are not structural defects. Both halves are needed — write rows with a
  single leading pipe, and count structurally before flagging.
- B5-0549, B5-0563, B5-0567, B5-0568 — the pipe-hygiene task lineage.
- B5-0575 — the repair row this lesson prompted.

Advisory only. This record confers no authority; see `AGENTS.md` §6.
