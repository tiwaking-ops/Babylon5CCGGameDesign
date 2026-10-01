---
document:
  title: "A good deletion row reads like a checklist; verify a removal by its numeric delta"
  status: "Pattern (advisory; B5-0430 store, same tier as investigations/)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1107"
---

# Pattern: a good deletion row reads like a checklist

**Context.** B5-1107 deleted one key (`timing: "ANY"`) from one record
(`de_event_armistice`) under an overseer authorization, with the
adjudication (zero consumers, no printed basis, contract-forbidden)
already supplied by B5-1101.

**Lesson 1 — deletion is safe when three things pre-exist: authorization,
adjudication, and scope.** The row spent its words on all three, so the
executor's judgement never entered the loop: no re-derivation, no
temptation to also "fix" the neighbouring noise, no invention. Deletion
rows that lack any of the three are the ones that delete history.

**Lesson 2 — verify a removal by its numeric delta, not by absence.** The
warning census moved exactly 5,398 → 5,396 (1 record × the known 2-pass
rule): proof the key was seen by the loader exactly as predicted, the
deletion landed, and nothing else moved. "No more warning" alone would
not have distinguished removal from the loader never reading the file.

**Lesson 3 — structural syntax travels with the key.** The trailing comma
on the preceding line is part of the deletion; `git diff --stat` (1/2)
and a green parse prove the JSON stayed well-formed. A key deleted but a
comma left behind is a different, worse bug.

Links: [B5-1107 report](../../REPORTS/2026-09-30-Freebuff%20(Buffy%20glm-5.3-flash)%201-B5-1107.md) ·
closes the armistice lineage B5-1022 → B5-1025 → B5-1101 → B5-1104/B5-1111 → B5-1107.
