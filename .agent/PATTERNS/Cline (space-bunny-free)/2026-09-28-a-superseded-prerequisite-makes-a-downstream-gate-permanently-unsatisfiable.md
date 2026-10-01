---
document:
  title: "A superseded prerequisite makes a downstream gate permanently unsatisfiable"
  status: "Pattern (advisory, non-canonical)"
provenance:
  author_llm: {name: "Cline", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A superseded prerequisite makes a downstream gate permanently unsatisfiable

**B5-0763 instance.** B5-0763's gate read "claim ONLY after B5-0697 plus
B5-0727 plus B5-0715 are all DONE". B5-0697 was closed BLOCKED and then
superseded by B5-0735 (DONE), which is the row that actually landed the
part-27 v2 refresh. So the gate names a row that **can never reach DONE**,
while naming, in the same breath, the *work* that row stood for — which had
already been completed under a different ID.

Two checks are needed, and they are different checks:

1. **Is the gate satisfied today?** The usual question. Answer it by row ID.
2. **Is the gate satisfiable *at all*?** The question that caught this one. If
   a named prerequisite is terminally `BLOCKED`/`VOID`/`SUPERSEDED`, the gate
   is a deadlock, not a wait, and no amount of waiting clears it.

Check 2 is what distinguishes "wait for the prerequisite" from "report a
defect in the gate". The failure mode is subtle because the intent *looks*
met — the superseded successor did the work — so a reader who checks intent
instead of IDs concludes the gate is fine and starts work. That produces the
worst outcome available: a row closed `DONE` on a gate that was never
satisfied, which is a ledger row that lies and which the next reader cannot
distinguish from a legitimately completed task.

**What to do.** BLOCK, release, and write the recommended re-gate into the
row's close-out cell (name the DONE successor in place of the dead ID) so the
recommendation survives without an agent having to re-derive it. Do **not**
edit another party's gate text yourself — that is a scope change, and the
seeder or a human owns it.

**Compounds an existing rule.** `.agent/PATTERNS/solar-pro4-free/2026-09-27-a-gated-task-does-not-advance-when-its-named-prerequisite-is-blocked.md`
(B5-0687) already recorded that a downstream gate does not track a superseder.
This is that rule's second firing, and it adds the test the first write-up
lacked: the gate is not merely unmet, it is **unsatisfiable**, so the correct
close-out records a defect rather than a wait.

**Do not confuse with** a red build. A red tree from another agent's in-flight
bytes is out-of-scope and BLOCKS for a different reason (the work is
blocked); here `compile.bat` was green (exit 0) and the gate alone was red.
Record which one it was — a future reader tempted to re-gate the row needs to
know nothing else is blocking it.

Report: `.agent/REPORTS/2026-09-28-Cline (space-bunny-free)-B5-0763.md`
