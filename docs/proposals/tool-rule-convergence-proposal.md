---
document:
  title: "Tool-rule convergence proposal — one implementation per shared rule, and a cross-check that detects divergence"
  status: "Proposal (advisory, no authority until merged)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Proposal: tool-rule convergence

**Status: proposal.** Advisory, same tier as `investigations/` and the rest of the
proposal store. This document confers no authority. It does not change
`.agent/00_BOOT.md`, `AGENTS.md`, or any tool. Promotion requires a human decision
and then implementation through the normal cycle per AGENTS.md §4.

**Origin.** A human question, 2026-09-27: *can I test hermes now?* Answering it
required reading the queue runner, and reading the queue runner beside the other
census tool revealed that two shipped tools disagreed about whether a **live,
actively-worked claim** was live. Evidence and reasoning in §1–§3.

**Author note.** This proposal is written by `opencode (space-bunny-free)`. It is
intended to be executed by a *different* agent. Authorship and assignment are
deliberately distinct here: see Appendix A for the executor's brief.

---

## 1. The problem, stated as an observation

At 2026-09-27 a queue runner was executing unattended against the live ledger. The
agent it invoked wrote its claim file with a **placeholder** timestamp:

```json
"started_utc": "2026-09-27T00:00:00Z"
```

so the claim's own age read **292 minutes** against a 30-minute TTL. The two shipped
census tools then disagreed about that claim:

| tool | rule applied | verdict |
|---|---|---|
| `.agent/tools/ledger-query.ps1` | newest of claim **and** owner heartbeat | **LIVE** (owner heartbeat 12 min old) |
| `.agent/run-queue.ps1` (before B5-0649) | `started_utc` **alone** | **abandoned** — re-offered |

The queue would have handed a task held by a working agent to a second claimant. That
is the duplicate-delivery failure the entire claims protocol exists to prevent, and it
was one function call away.

**The structural point, which is what this proposal is actually about.** The rule
involved ("liveness is the newest of several signals", B5-0597) was already written
down **and already implemented** — in `ledger-query.ps1`. The second tool never
received it. So the rule existed simultaneously as prose, as a working implementation,
and as the **opposite** behaviour, with nothing in the system capable of noticing.

B5-0649 aligned the two by copying the shared helper **verbatim**. That produced
correctness *by duplication* rather than by removing the duplication. It is correct
today and unguarded tomorrow, because **nothing compares the two tools.**

## 2. What is already on file — and the gap that is larger than it looks

Two existing proposals bear directly on this. **Neither has been adopted.**

| document | author | status on disk | implemented? |
|---|---|---|---|
| `docs/proposals/claim-liveness-protocol-proposal.md` | Buffy (glm-5.3-flash) | "not canonical until adopted per AGENTS.md §4" | **partially, twice** |
| `docs/proposals/live-repair-aware-ledger-census-protocol.md` | Solar Pro4 (solar-pro4:free) | "Proposal" | **no** |

### 2.1 The adoption gap — a human decision, not an implementation detail

`claim-liveness-protocol-proposal.md` specifies a **four-key** conjunction for judging
a claim: (1) claim age, (2) owner heartbeat silent, (3) no recent report for the task,
(4) not named by any live heartbeat's `current_task`/`live_claims`. It also specifies
matching the heartbeat by **prefix** over all spellings.

What the code does today, in both tools, after B5-0649:

| key | implemented? |
|---|---|
| 1 — claim age | yes (both) |
| 2 — owner heartbeat | yes (both, aligned in B5-0649) |
| 3 — recent report for the task | **no** |
| 4 — named by a live heartbeat | **no** |

So the shipped behaviour is an **undeclared two-key subset of a proposal that was
never adopted**, implemented in two places. The subset is not documented anywhere. A
reader of the code cannot tell that keys 3 and 4 were dropped rather than never
considered.

This is a governance question, not a code question, and it is stated here rather than
decided here. **Either** adopt the four-key rule and bring the tools to four keys,
**or** formally reduce the rule to what the tools do and record the reduction. What
cannot continue is code enforcing something nobody adopted, partially, twice.

### 2.2 The census protocol is unimplemented

`live-repair-aware-ledger-census-protocol.md` requires that a structural census read
`.agent/CLAIMS/` first and either skip or explicitly mark rows under a live claim, on
the grounds that a row mid-repair reads as a *different* defect class than its
committed form. Verified: **neither tool does this.** Neither mentions
`under live claim` nor marks a transient state. The scenario is live — this session
observed a concurrent writer repeatedly rewriting the ledger band it was working in.

## 3. The class, enumerated

There are **five** tools under `.agent/`. The fifth is easy to miss:

```
.agent/run-queue.ps1
.agent/tools/ledger-query.ps1
.agent/tools/validate-heartbeats.ps1
.agent/tools/migrate-heartbeats.ps1
.agent/tools/suite-coverage.ps1
```

| shared rule | implementations | state |
|---|---|---|
| claim liveness | `run-queue`, `ledger-query` | aligned **by copy** — fragile |
| ledger row parser | `run-queue` (`Get-LedgerRows`), `ledger-query` (own inline parser) | **divergent** |
| pipe integrity | `run-queue` (warns), `ledger-query` (prints `doubleLead`) | **divergent** |
| heartbeat schema | `validate-heartbeats`, `migrate-heartbeats` | **divergent** |
| claims-first census | *proposed only* | **unimplemented in both** |

**Nothing in this repository compares two tools.** Every divergence found to date was
found by accident, or because one agent happened to read two files in the same session.
That is a coincidence, not a detection strategy. With 5 tools and 4 shared rules there
are on the order of 40 possible pairwise disagreements; **one** has been found.

## 4. Options considered

**A. Do nothing; document the divergence.**
Rejected. The concrete hazard in §1 is a duplicate delivery, and §2.1 is an undeclared
rule weakening. Documentation is what produced this class: B5-0597 was written down
carefully and implemented in one tool.

**B. Keep the copies in sync by hand.**
Rejected. This is what B5-0649 did once. It made one instance correct and left the
condition that produced it fully armed. A convention with N hand-synced copies has
N-1 opportunities to drift and no alarm on any of them.

**C. Cross-check tool — compare the implementations and fail on divergence.**
**Recommended, first.** It is the only option that detects the divergences nobody has
enumerated yet, including future ones. It is additive, touches no working tool, and is
read-only.

**D. Shared library — one implementation, imported.**
**Recommended, second.** It removes the duplication rather than syncing it. It is the
correct end state and the higher-risk change, because it edits two working tools other
agents depend on.

**E. A documentation rule requiring rows to name their implementation sites.**
Rejected as a primary measure, for the reason in option A. Listed as Task 3 only, and
ranked last deliberately.

**F. C and D in one pass.**
Rejected. It combines an additive, zero-risk change with a refactor of two live tools
in a single unit of work, so a regression in D would be blamed on C and both would be
rolled back.

## 5. Task 1 — a cross-check that makes divergence detectable

**Deliverable:** `.agent/tools/census-crosscheck.ps1`, strictly read-only.

It must:

1. For every ledger row, compute each tool's verdict **by that tool's own logic** — the
   tolerant row regex, the status parse, the leading-pipe count, the claim-liveness
   verdict — and **diff the two**.
2. Print any disagreement as: row id, rule name, tool A's value, tool B's value.
3. **Exit non-zero on any disagreement**, mirroring `validate-heartbeats.ps1`'s
   contract. Exit 0 when the tools agree.
4. Write nothing: no ledger edits, no claim files, no heartbeats.

**Acceptance criteria — all three.**

1. Against the live tree it either runs clean or names real divergences. If it finds
   one, **file a task for it; do not silently fix it.** A cross-check that repairs
   things behind the operator's back is a second implementation, which is the disease
   this proposal exists to end.
2. A synthetic fixture with a **known, injected** divergence makes it exit 1 and name
   the row. Test in an isolated `%TEMP%` copy of the scripts plus a synthetic ledger;
   never mutate the live tree.
3. **Prove it can fail.** A checker that has only ever returned 0 is not evidence of
   anything. Show it red on the fixture before believing it green on the live tree.

**Explicitly not in Task 1:** implementing keys 3 and 4, adopting the census protocol,
or refactoring either tool.

## 6. Task 2 — one implementation, imported

**Deliverable:** `.agent/tools/lib-ledger.ps1` holding the shared primitives — tolerant
row parse, `Get-NormName`, `Get-HeartbeatIndex`, `Test-LiveClaim`, `Get-TaskNumber` —
dot-sourced by **both** `run-queue.ps1` and `ledger-query.ps1`, with their private
copies deleted.

**Sequencing is load-bearing.**

1. Add `lib-ledger.ps1` with the current, working logic. Additive and safe.
2. Switch **one** tool. Verify identical output.
3. Switch the **second**. Verify identical output.
4. Only then delete the duplicates.

Steps 2–3 edit two working tools that other agents invoke, so each needs its own claim
and a window with no queue run in flight.

**Acceptance:** capture each tool's live-tree output **before** and **after**, and
require it to be **identical**. "It still works" is not the criterion; unchanged output
is.

**Risk, stated plainly:** this touches two working tools. Doing Task 1 alone still
leaves a known problem on the table — and does so without risking the queue. That
trade is deliberate and should be taken deliberately.

## 7. Task 3 — documentation, ranked last

One line in the row conventions: *a row that changes a shared rule must name every
implementation site.* Ranked last because the evidence is that documentation-only is
what produced this class. Do it only when Tasks 1 and 2 are genuinely closed.

## 8. Out of scope for this proposal

* It does not edit `AGENTS.md`, `.agent/00_BOOT.md`, or `CLAIMS/README.md`.
* It does not edit any ledger row text.
* It does not change the TTL, the stop conditions, the lane or prereq tables, the
  duplicate-ID assertion, the B5-0624 suffix fix, or the B5-0628 quote sanitisation.
* It does not decide §2.1. That is a human call about what the rule *is*.
* It does not touch `b5ccg/src` or `b5ccg/resources`.
* It does not rename any heartbeat file or alter any `agent_id`.

## 9. Reusable lesson

Audit a convention by finding every place it is **implemented** and comparing those
places to each other — not by asking whether it is documented, because documentation is
not an implementation and never was. A convention with N implementations has N-1
opportunities to disagree, nothing in ordinary testing compares them, and every audit
that samples the compliant one reports the system as conformant. The fix is not
"document it better" and not "sync the copies"; it is **one implementation, imported**,
plus a checker that fails when two disagree.

---

## Appendix A — brief for the executing agent

You have no context from the session that produced this proposal. Read all of the above
first. Then:

**Invocation.** Read `AGENTS.md`, `.agent/00_BOOT.md` (all 11 steps; the pattern skim
is step 11, step 10 is reaping), and `.agent/AGENT_LOOP.md`. Run the shipped census
tools, never a hand-rolled census. **Execute §5 (Task 1) only, then stop.** Tasks 2
and 3 are sequenced deliberately, not neglected.

**Identity and filenames.** Adopt one `agent_id` spelling and keep it; a new spelling is
a *new agent*, not a variant. Windows forbids `:` and `/` in filenames — sanitise
**only those two** to `-`, preserving spaces and parentheses
(`solar-pro4:free` -> `solar-pro4-free`; `Kilo (kilo-auto/free)` ->
`Kilo (kilo-auto-free)`; `opencode (space-bunny-free)` unchanged). Use the sanitised
form in every filename, keep the true id in the `agent_id` field, and ensure your id
resolves to exactly one heartbeat file. **Create your own heartbeat; never edit
another agent's.** Schema is binding — see `.agent/HEARTBEATS/README.md`; required
fields `schema_version`, `agent_id`, `utc`, `state` (`active`/`idle`/`busy`),
`live_claims` (always present, `[]` asserts you hold nothing). Verify with
`.agent/tools/validate-heartbeats.ps1`.

**Traps that each produced a green result on 2026-09-27.**

1. Windows PowerShell 5.1 does **not** escape double quotes when passing an argument to
   a native executable; `& $cli @('-z', $text)` splits a text containing `"` and a
   subcommand CLI reads a trailing fragment as a command name. Newlines are safe;
   quotes are not.
2. A stub written in the **calling language** proves nothing — a `.ps1` stub receives
   .NET objects and never exercises Windows command-line marshalling. Use a different
   implementation as the probe (e.g. a native `python.exe` argv dumper).
3. A **suspiciously uniform** result across independent cases means a broken harness,
   not a consistent system. Real systems are lumpy. Assert your harness's own inputs
   before trusting its verdicts.
4. A pre-write "is this ID free?" check is necessary and **not** sufficient — two
   seeders both measured one row ID free in the same window. Run the post-write
   duplicate-ID census (`AGENT_LOOP.md` step 9) after every row you write. On a
   collision leave the other writer's row byte-identical and renumber **yours to a
   non-adjacent ID**; renumbering into the slot they just vacated deadlocks.
5. A citation census **before and after** any rename.
6. Do not send a live agent a probe prompt that can cause a side effect. A probe of
   mine told hermes to "write one reusable lesson line"; it did, into another agent's
   namespace. Tools do what they are told — delete what it wrote and disclose it.
7. Never edit a historical report or ledger row to match present behaviour. One phrase
   search found 31 hits of which exactly **one** was a live instruction.

**Constraints.** `b5ccg/src` and `b5ccg/resources` are out of scope. **Do not commit or
push** — commits are a human decision; leave the tree dirty and report what you changed.
Do not rename heartbeat files or alter any `agent_id`. Other agents are active: check
`.agent/CLAIMS/` immediately before every write, and never touch another agent's claim
or heartbeat. Do not mark work DONE that was never OPEN and claimed (AGENTS.md §6).

**Close-out.** Per `00_BOOT.md` step 9: ledger row, `docs/DECISIONS.md` entry,
`.agent/REPORTS/<date>-<sanitised-agent-id>-<task-id>.md`, delete your claim file,
refresh your heartbeat, plus one **Reusable lesson** line filed as a **new** file under
`.agent/PATTERNS/<your-agent-id>/` (supersede-never-rewrite). Report iterations, tasks
closed, and every file left uncommitted. **This loop does not commit**, so there is no
commit hash to report; a run ending with a dirty tree is a correct run.

**If you do only one thing:** build the cross-check (§5) and **prove it can go red**.
That single capability turns "we find these by accident" into "we find these on demand",
and it is the only item here that protects against divergences nobody has thought of yet.
