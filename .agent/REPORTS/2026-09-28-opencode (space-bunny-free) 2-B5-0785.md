---
document:
  title: "B5-0785 close-out: instance-qualified agent_id promoted"
  status: "Report (observation, no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0785 — instance-qualified `agent_id` promoted

**Row:** B5-0785, seeded on the human ruling of 2026-09-28 and closed in one session.
**Ruling recorded:** *HUMAN RULING (2026-09-28): **APPROVED** — adopt
`docs/proposals/agent-instance-identity-proposal.md`.*
**Result:** DONE. Two governance paragraphs, one DECISIONS entry, one frontmatter status
change, this report, one pattern. **No tool, registry entry, existing `agent_id`, foreign
heartbeat or `src` file was touched. No commit.**

## What changed, exactly

| File | Change |
|---|---|
| `.agent/HEARTBEATS/README.md` | New subsection *The id names a session, not a model* under *Identity*, carrying R1–R6 |
| `.agent/00_BOOT.md` | One paragraph in step 3: pick a discriminator no running instance is using, with a letter or digit; never reuse or retro-rename; never edit a foreign heartbeat; report a collision rather than merging it |
| `docs/proposals/agent-instance-identity-proposal.md` | `status:` → APPROVED, body status paragraph replaced, assessor entry added. Sections 1–7 unchanged |
| `docs/DECISIONS.md` | `## 2026-09-28T06:05Z - B5-0785 DONE` with the ruling in the file's established `* HUMAN RULING (date): **APPROVED** —` style |

The normative rule lives in exactly one place. `00_BOOT.md` carries a pointer, not a
second copy — the same reasoning the B5-0659 entry gives for adopting the three-signal
rule by reference rather than by restatement. Two normative copies of one rule is a rule
that will drift, and this repo has the diff history to prove it.

## The adoption needed no code, and that is the finding

R4 is the whole reason. `validate-heartbeats.ps1` resolves an `agent_id` in this order:
**exact stem match**, then `_registry.json`, then `:`/`/` sanitisation. Exact stem match
comes *first*, so a discriminated id made of characters Windows accepts resolves on the
first branch. No migration, no schema amendment, no registry row, no validator change.

This was not inferred — B5-0783 proved it live in the previous session by writing the new
heartbeat and reading `CONFORMS` on its first validator run with `_registry.json`
untouched. The promotion consumed that measurement instead of re-deriving it, which is
the difference between a promotion and a re-litigation.

## The hazard the rule defends, stated once

`Get-NormName` keeps **only letters and digits**. `Get-HeartbeatIndex` holds
`key -> NEWEST mtime`, **one value per key**. Therefore two heartbeat files whose stems
differ but whose normalised key agrees are **one key** — and the quieter instance's
heartbeat is *absent from the liveness index*, not merely ambiguous to a human reading
it. A live claim can then read `STALE` on the heartbeat signal: the B5-0597 false-reap
shape, reached by a route no gate currently covers.

The route is cheap. `solar-pro4<U+F03A>free` — fullwidth colon, char code 61498 —
satisfies every constraint Windows places on a filename, produces a different stem,
resolves to the same `agent_id` with no registry entry, and normalises to the same key as
the ASCII spelling. No malformed file, no parse failure, no validator exception. A tool
that reports collisions on *exact* `agent_id` strings is the only thing that sees it at
all.

## Two things I deliberately did not do

**1. I did not widen the claim to fix a now-stale paragraph.** The README's *Known
unresolved* section still says, in the present tense, that two opencode-family sessions
"cannot see each other" and that "the fleet's identity discipline is not [sound]". That
was true when written and is stale the moment R1–R6 exist. My claim covered the Identity
section, not that one, and the honest move was not to help myself to a governance
paragraph mid-row. Instead the new subsection states **in place** that *Known unresolved*
describes the pre-rule state and that both colliding files remain byte-identical because
R5 and R6 forbid one instance tidying another — so no reader is misled, and the residual
is visible.

**Closing that paragraph properly is owed and unclaimed.** It is a one-line edit to a
governance file and it needs its own row. It is named as such in both the DECISIONS entry
and the ledger note so it cannot quietly die.

**2. I did not fix the validator's blind spot.** The proposal's §5 residual is real: the
collision check compares **exact** `agent_id` strings while the liveness join compares
**normalised** keys, so a punctuation-only variant (`…free _` → base key) passes the very
tool whose job is to catch it. R3 mitigates this by discipline. Mechanically closing it
needs a *store-level* check on normalised keys — a real change to
`validate-heartbeats.ps1`, correctly a row of its own rather than a drive-by from a
promotion row.

## Gates

| Check | Result |
|---|---|
| `javac -version` | `1.8.0_292` |
| `b5ccg/compile.bat` | **not run, not applicable** — no Java touched. Recorded explicitly (B5-0771: a gate that never ran reads identically to one that passed) |
| `validate-heartbeats.ps1` | 32 files, 31 conforming, **1 collision — the pre-existing foreign `solar-pro4` pair**. The governance edit introduced no new store-level finding |
| `run-dup-census.ps1` | `PASS (0 duplicate task IDs)`, exit 0 |
| `git diff --numstat` on both governance files | `25 added / 2 deleted` and `45 added / 2 deleted` |
| The 4 deleted lines, inspected | **all four** are `last_modified_by_llm` or `last_modified_date` — provenance only, zero governance content lost |
| Both edits read back **off disk** | yes — not trusted from the editor's return value |
| Encoding of inserted text | U+2014 em-dash present, **zero** U+00E2 mojibake in both segments |
| DECISIONS append | LF-only, left LF-terminated (a here-string carries no trailing newline) |
| Ledger row read back off disk | `B5-0785 / DONE / 7 pipes / doubleLead no` |

**On reading back.** The check that caught the B5-0783 defect — reading the closed row
**off disk** instead of trusting the variables that produced it — is now simply how I
write, and it ran on both close-outs in this session. B5-0785's write was correct on the
first attempt (`$lines[$idx] = $new` before serialising, the exact line B5-0783 omitted)
and the disk read-back confirmed `DONE` immediately.

> **Correction, recorded rather than quietly fixed.** The first version of this paragraph
> claimed B5-0785 *repeated* the B5-0783 no-op defect. It did not, and the claim was false
> when written — I drafted it as a narrative beat before re-reading my own command. The
> row closed correctly on the first write. A report asserting a near-miss that never
> happened is worse than no report, because it trains the reader to discount the
> paragraph that would have mattered. Same root as B5-0783: a statement about a check,
> written from intention instead of from the artefact.

**On the encoding check.** The PowerShell console renders an em-dash as a plain hyphen,
which made a correct file look corrupted. It was checked at byte level instead: 8212
present, zero U+00E2. A rendering artefact is not evidence, and neither is a tool's
return value — both were confirmed against the medium.

## Reusable lesson

**Read the consumer's resolution order before you propose a migration.** The instinct on
an identity defect is to add a field, a schema amendment or a registry entry. Here the
consumer already had a first-match branch that accepted the entire fix, and the whole
promotion was two paragraphs and zero code. The migration would have been three files,
a 32-file migration, and a new invariant to keep true forever.

## Still owed, named

1. *Known unresolved* in the HEARTBEATS README — one-line governance edit, own row.
2. Store-level **normalised-key** collision check in `validate-heartbeats.ps1` — own row.
3. Register the pass-bias human ruling (1=A on the 0454 brief) in DECISIONS, which four
   places cite and the register does not contain. **Not seeded.** See the correction below.
4. B5-0787, seeded and unclaimed: the tool-rule-convergence rejection and the
   negative-power-split approval.
