---
document:
  title: "A gate named on a string the tool never parsed is unsatisfiable, not merely unmet"
  status: "Pattern (advisory; .agent/PATTERNS store is never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny)", version: "space-bunny"}
  created_date: "2026-09-30"
  last_modified_by_llm: {name: "Cline (space-bunny)", version: "space-bunny"}
  last_modified_date: "2026-09-30"
---

# A gate named on a string the tool never parsed

**Pattern.** When a task row's success criterion names a *literal token* the
verifying tool is supposed to read ("the detector must read `adjudicated` on
all 9"), grep the tool for that exact token **before** writing anything. If the
string appears only inside comments, or only in a sibling document, the gate is
**unsatisfiable** - not temporarily unmet, and not a naming mismatch to be
tidied away later.

**Observed (B5-1096, 2026-09-30).** The row asked for an `ADJUDICATED-KEEP`
marker in nine verdict cells, gated on "post-write detector must read
adjudicated on all 9". The detector that the gating task (B5-1020) landed
contains no such parse: `ADJUDICATED` occurs in `ledger-query.ps1` only in
prose comments, and the tool emits `exempt (<class>)` / `reportable`, never
`adjudicated`. The proposal the row cited (B5-1061) had *assumed* the parse
would land; the gating task shipped a different, narrower contract instead.

**Why it matters.** The failure is worse than a red gate, because it is
*invisible after you do the work*. Writing the nine markers produces a ledger
that reads as migrated to any human and to any agent that greps for the
marker, while the instrument that was supposed to confirm it sees nothing at
all. The next reader inherits nine plausible-looking markers and a green-looking
row. A visible blocker that you decline to clear is recoverable; an invisible
one that you manufacture is not.

**The rule it generalises.** A gate is a function of the *tool*, not of your
edit. Before satisfying a gate, establish that the mechanism exists and can
fail. This is the same discipline as "a test never observed red is not
evidence" in `AGENT_LOOP.md` - except one step earlier, at the point of
deciding whether to act at all.

**Distinguish the two failure modes before reporting.** "The token is spelled
differently" is weak and is often a trivial fix. "The tool has no such code
path, and the affected rows are structurally out of reach of the rule it does
implement" is strong. On B5-1096 both held: 3 of the 9 rows carry excess pipes
that sit outside any paired quoted or backtick span, and the shipped exempt
classes are **positional**, so no cell edit could ever move them. Report the
structural reason, because it is the one that survives the token being renamed
tomorrow.

**Cheap implementation.** One `Select-String -Recurse` for the literal token
over the tool directory, plus reading which values the tool can actually emit.
Two commands, run before the first edit, that convert a whole class of
plausible-but-futile work into a correct BLOCKED.

**Counter-pattern to avoid.** Treating "the proposal says the tool will read
this" as evidence that it does. A proposal confers no authority over what its
successor actually shipped - and where the two disagree, the shipped tool is
current truth (AGENTS.md section 3).
