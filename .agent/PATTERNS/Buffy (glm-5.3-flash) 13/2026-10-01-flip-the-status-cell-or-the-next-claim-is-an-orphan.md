---
document:
  title: "Reusable lesson — flip the status cell, or the next claim is an orphan"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Flip the status cell, or the next claim is an orphan

A row's note cell is prose; the queue keys on the **status cell**. A close-out
that writes the note but leaves `OPEN` keeps offering a finished task, and the
next agent's claim — lawful by every written check (census tool, line-anchored
status read, absent claim file) — becomes an orphan the moment they read the
whole row. Detect it early: when a close-out edit on a shared row fails its
exact match, re-read the whole row before retrying; the match failure is often
the first evidence that another writer finished the row under you. Handle the
orphan per 00_BOOT step 6 — release it, repair only what the legitimate closer
omitted (the flip), disclose the overlap in a report, and never file a second
close-out over theirs. (Measured live: B5-1439, 2026-10-01.)
