---
document:
  title: "B5-1401 close-out — the heartbeat retirement policy merge is already on disk, and it is faithful on every gate while dropping three words that decide whether the first execution was authorised"
  status: "Report (observation, no authority)"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free) 2", version: "kilo-auto/free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Kilo (kilo-auto/free) 2", version: "kilo-auto/free"}
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
  task: "B5-1401"
---

# B5-1401 — merge verification of the approved heartbeat retirement policy

**Row letter:** merge the human-approved retirement policy into
`.agent/HEARTBEATS/README.md`, wording preserved, no weakening and no widening,
and **record any conflict between the sources loudly instead of resolving it
silently**.

**Verdict: the merge exists, it is DONE by another row, and it weakens no
operative condition. Three conflicts between the sources are recorded below and
NONE is resolved here, because resolving any of them is a policy decision, not a
verification.** The row closes on its own terms; the three findings are named
with the smallest repair each and left for a separately-claimed row or a human
ruling.

## What was verified, from what

| Source | Where | State |
|---|---|---|
| Approved text | `docs/proposals/2026-09-29-heartbeat-retirement-policy.md`, author `Buffy (glm-5.3-flash) 4`, task B5-1005, frontmatter `APPROVED 2026-09-29` | authoritative input |
| Approval of record | ledger line 1170, **B5-1065 DONE** (`opencode (muse-spark-1.3) approve-01`) — approval recorded, README merge deferred | read first |
| Divergent sibling | ledger line 1164, **B5-1062 SUPERSEDED** — same approval, superseded by B5-1065 over a contested id, never merged | read first |
| Residue that needed the gate | ledger line 1191, **B5-1094 DONE** — 9 non-conforming files + 3 collisions, "seven of nine unblock through the … policy whose README merge is DEFERRED" | read first |
| The merge itself | `.agent/HEARTBEATS/README.md` lines 227–256, section *Heartbeat retirement policy (approved 2026-09-29)*, assessor entry `GitHub Copilot (Auto mode) 1012b` | present, **uncommitted** |
| Who merged it | ledger line 1326, **B5-1463 DONE**; `docs/DECISIONS.md` line 283 | DONE |
| Measured diff | `git diff --stat` = **100 insertions, 2 deletions**; the same hunk also carries the *Identity* R1–R7 block (README lines 114–177), so 65 of the 100 lines are not retirement text | measured this pass |

Toolchain receipts this pass: `javac 1.8.0_292`; `b5ccg/compile.bat` exit 0,
"Build successful", 1 expected bootstrap warning; `run-dup-census.ps1` PASS,
0 duplicate task IDs; `validate-heartbeats.ps1` exit 1 on the standing
pre-existing residue (15 non-conforming files, 3 identity collisions) — all
foreign, none touched.

## Clause-by-clause: no weakening, no widening

| Approved clause | In the merge? | Verdict |
|---|---|---|
| R1 archival, never deletion or rewriting; byte-identical move to `.agent/HEARTBEATS/_retired/`; filename and JSON unchanged; `_registry.json` not edited | all four, verbatim in substance | **faithful** |
| R2.1 live-store mtime older than 24 hours (48 TTLs) | present, both numbers intact | **faithful** |
| R2.2 payload `idle`, or closed-session tombstone with no live claims (or all since DONE); `busy`/`active` never archived on mtime alone | present | **faithful** |
| R2.3 no unresolved standing finding names it: not an OPEN row, not a live claim, not an unadjudicated collision report | present, all three limbs | **faithful** |
| R2.4 body — the archiving runs under a claimed ledger row recording the file, mtime age, payload state, three-signal consequences | present | **faithful as text** (see F2 for the evidence gap) |
| R2.4 heading — **"One move per row"** | **absent** | **F2, dropped constraint** |
| R2 tail — "A file is **never** retirable because its agent_id is old, its spelling is inconvenient, or a human liked it better gone." | absent | dropped, non-operative: conditions 1–4 are the whole test |
| R3 archive location; archived identity reads `UNKNOWN`, never LIVE/STALE; retired ids not reusable, rewritten or merged | all three | **faithful** (the "validator census no longer counts it" rationale is dropped — commentary, not a gate) |
| R4 "unchanged and binding: UNKNOWN, never LIVE, never STALE" | not restated in the section, but the section's own sentence carries it and the canonical *Liveness* section is untouched by the diff | **faithful** |
| R5 out-of-enum tombstone `Cline (space-bunny) b5-0941.json` stays byte-identical in place; no adopted home yet; its standing finding prevents mass retirement until separately authorised | present, same disposition | **faithful** |
| R6 adoption path — proposal tier only until merged by the claimed process, gates green, DECISIONS entry written | merge on disk, DECISIONS line 283, `compile.bat` green this pass | **satisfied** |

**Nothing in the merge permits a move the approved text forbids, and every
operative condition survives.** The two dropped clauses are the convenience
catch-all and the census rationale, neither of which is a gate. That is the
row's gate met.

## The three conflicts, recorded loudly

### F1 — the binding document now uses the label `R1–R6` for two different rule sets

The merged README carries *Identity* R1–R6 (agent_id naming, B5-0785, extended
by R7) at lines 114–177 and *Retirement* R1–R6 at lines 227–256. Inside the
retirement section the citations are unqualified: "R5/R6 forbid another instance
from tidying it" and "R1-R6 leave the existing instance identity rules
untouched".

Read against the Identity set, the retirement policy reads as **unauthorised
outright**: Identity R6 says R1–R6 are "prospective only … authorise nobody to
edit a foreign heartbeat", and retirement moves another agent's file. Read
against the Retirement set, it authorises itself. The document does not say
which reading governs, and the merged sentence's own words — "R1-R6 leave the
existing **instance identity** rules untouched" — are the only hint, and they
are a statement *about* the identity rules rather than a qualifier on the
reference.

The collision is **inherent to the merge, not introduced by it**: the approved
proposal's own R5 and R6 also leaned on the identity rules, so the approved text
carried the same ambiguity and the merge is faithful in reproducing it. That is
precisely why it belongs in this row's conflict record rather than in a silent
edit — the row authorises a merge, not a renumbering of an approved rule set.

*Smallest repair, not applied:* qualify the two retirement-section citations
(`retirement R5/R6`, `identity R1–R6`), or renumber the retirement set `P1–P6`.
Two or three words; no policy change; no human ruling needed, because
disambiguation is not a change of scope.

### F2 — the dropped heading is the one the first execution stands on

The approved R2.4 is headed **"One move per row"**; the merge keeps the body and
drops the heading. Under the strictest reading of the approved text, a batch
retirement is what that heading forbids.

The first and only execution of the merged policy is a batch: **B5-1463 archived
five files under one row** (`agent-on-deck.json`, `big-pickle.json`,
`Buffy (deepseek-v4-flash).json`, `Claude (claude-3-7-sonnet-20250219).json`,
`buffy-unknown-loop2.json`). So the merge as written is the only reading under
which that execution is authorised, and the heading it dropped is the only
sentence that would have forbidden it.

The retained half of the condition is also thinner in practice than in text:
B5-1463's row note names the five files and their class ("verified idle,
claim-free, older than 24h") but records **no per-file mtime age and no
three-signal consequence**, which is what the condition asks the row to carry.
Per-file ages are not recoverable from the note, so this cannot be judged from
the ledger alone.

*Smallest repair, not applied — and this one is a human gate.* Either restore
the heading verbatim, or state in the merged text that a batch is permitted
provided each file's mtime age, payload state and three-signal consequence are
recorded individually. Restoring the heading would retroactively declare an
already-executed move unauthorised, so the second is the honest repair, and
choosing between them picks a reading of an approved text — the B5-0654
withdrawal-marker situation. Recorded for a human ruling, not decided here.

### F3 — R7's provenance cites a task id the ledger assigns to a different agent's different topic

The binding README dates R7 to "`opencode (big-pickle)`, **B5-1062**, on
human-approved task", in both the rule body (line 145) and the assessor entry
(line 11). The ledger's B5-1062 (line 1164) is the **retirement-policy promotion
row** owned by `opencode (muse-spark-1.3) approve-01`, SUPERSEDED, on a
contested id — a different agent, a different topic, and a different date.

The R7 work is recorded under **B5-1066** (ledger line 1171, DONE):
"Record the DECLINED R5-exception rename of the 17 U+F03A lookalike-filename
components as a proposal", and it carries `docs/proposals/lookalike-filename-r5-exception-proposal.md`,
the measured manifest, and the B5-0773 / B5-0793 decline precedents.

This is not a near-miss. B5-1066's own close-out states: **"B5-1066 chosen
because B5-1062 was already taken in the ledger (collision disclosed in the
B5-1062 report)."** The same agent that recorded the collision then cited the
taken id in the binding document. The citation is therefore not merely wrong, it
is wrong in a way the author had already written down.

Why it matters beyond tidiness: this is the B5-0618 / B5-0622 class, which the
repo treats as non-cosmetic precisely because status is keyed by id. An
implementer following the README lands on a SUPERSEDED row that has nothing to
do with R7. **B5-1475 (OPEN) is that implementer** — it implements the R7
detection half and reads this provenance, and it currently holds a claim owned
by `google-model-v1` whose liveness is UNKNOWN (no matching heartbeat), i.e.
never stale, so the row is live and not mine to approach.

*Smallest repair, not applied:* change the two R7 citations from B5-1062 to
B5-1066. Mechanical, no policy content, and it makes the README agree with the
ledger on which task authorised a rule the validator enforces. Coordinate with
B5-1475's owner rather than editing under a live claim.

## Fences honoured

No heartbeat file moved, renamed, edited or deleted; no `_registry.json` edit;
no proposal frontmatter edit; no `src` or card-data edit; no foreign ledger row
or claim touched; no `.agent/HEARTBEATS/README.md` byte written by this session
— the merge is another row's work and this report is a verification of it, not
a re-do. No commit, no push. Claim released at close-out.

**Reusable lesson:** verify a merge clause by clause against the approved text
and treat every dropped **heading** as a dropped constraint, not as editorial
tidiness — the first execution of a freshly merged policy is what tells you
whether the dropped words were load-bearing, so the merge audit and the
first-use audit are one piece of work, not two.
