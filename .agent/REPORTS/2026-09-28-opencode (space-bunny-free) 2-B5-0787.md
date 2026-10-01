---
document:
  title: "B5-0787 close-out: two human rulings recorded, neither requiring a reversal"
  status: "Report (observation, no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0787 — tool-rule-convergence REJECTED, negative-power-split APPROVED

**Row:** B5-0787, seeded on the human rulings of 2026-09-28, closed in one session.
**Result:** DONE. Two `status:` fields, two body status paragraphs, two provenance
entries, one DECISIONS entry, this report, one pattern. **No tool, no `src`, no card
JSON, no deletion, no commit.**

## Ruling 1 — REJECTED, and audited before being recorded

* **REJECTED** `tool-rule-convergence-proposal.md`, scoped to §2.1. **Keep the
  three-signal claim rule**, which stays where it already lives —
  `.agent/HEARTBEATS/README.md` § *Liveness: three signals, never one*, adopted by
  human ruling 2026-09-27 as B5-0659.

The temptation was to write "rejected" into a status field and move on. I measured
first, because a rejection that reverses nothing and a rejection that silently guts a
shipped tool look identical in a status field:

| Check | Result |
|---|---|
| Four-key claim conjunction in `ledger-query.ps1` / `run-queue.ps1` | **absent** |
| Prefix heartbeat matching in either tool | **absent** — the one "prefix" hit, `run-queue.ps1:252`, is an unrelated regex comment about anchoring a task-id match |
| Consequence | §2.1 was never implemented, so the ruling is a **record, not a reversal** |

**Half of it re-affirms an existing ruling.** B5-0659 is titled *"three-signal claim
liveness ADOPTED, and the proposal's fourth key REJECTED"* — the fourth key was already
refused on 09-27. Today's ruling extends that to all of §2.1.

**The genuinely new element is prefix matching, and the reason it is refused is worth
keeping.** The shipped join normalises via `Get-NormName` and matches exactly. A prefix
match is strictly weaker: `opencode (space-bunny-free) 2` and `… 3` share a prefix. So
prefix matching would reintroduce — in the one component whose entire job is to resolve
identity — exactly the ambiguity that B5-0785's R1–R6 had just settled, on the same
morning. Two governance rows in one session, pointing opposite ways, is not a
coincidence to be smoothed over; it is the reason the rejection is scoped explicitly.

**What I did *not* touch, and why it is called out.** Task 1 of the rejected proposal —
the divergence cross-check — has landed as `.agent/tools/census-crosscheck.ps1` and is
in daily use (B5-0771 repaired its dead `REPORTS` path; B5-0775 is scoped to its
`Get-Verdict-L`). The ruling was scoped to the claim rule, so nothing was reverted or
deleted. "The proposal was rejected" is precisely the sentence a later agent
over-applies, so it is stated in the proposal's own status paragraph as well as here.

## Ruling 2 — APPROVED, as design only

* **APPROVED** `negative-power-split-design-proposal.md` as a **design** approval.
  Canonical: §3's two-piece seam and §5's call-site audit as its checklist, plus §4's
  equivalence record. **§6's recommendation not to implement now is affirmed**, not
  overridden.

The author's own measurements make "design only" the whole of it, and I did not re-derive
them: "power" appears in **0** of 829 card texts, in **8** titles that are all flavour,
and **no card in the pool carries any stat that could serve as a Power value**. There is
nothing to trigger the rule and therefore nothing to test a seam against, and §6 says a
half-applied split — a Power read where the rulebook says influence — is *worse* than the
status quo. The approval is recorded as affirming that, because that is what the document
recommends.

**What it does not authorise, and why I did not infer it.** §4 reserves exactly one
thing for a human: any Power add-on card "requires a human IP-safe data decision **before
any text is authored**". A decision approving *this design* is not a decision supplying
*a card*. So the open question — may a Power add-on card be authored at all, and with
what text — is **named and left open** rather than answered by invention. That is also
the only reading consistent with B5-0385/B5-0396 paraphrase discipline and the B5-0388
ruling that the authored pool is the design layer.

**Provenance detail worth stating.** Both documents have a *different* author
(`Buffy (glm-5.3-flash)` and `opencode (space-bunny-free)`), so my entries are genuine
assessments, not self-assessments — which `AGENTS.md` §1 forbids conflating. Contrast
B5-0785, where I authored the proposal and the entry is a self-assessment with that fact
stated in the note. Same edit shape, different provenance shape, because the rule keys on
authorship.

## Gates

| Check | Result |
|---|---|
| `javac -version` | `1.8.0_292` |
| `b5ccg/compile.bat` | **not run, not applicable** — no Java touched |
| `validate-heartbeats.ps1` | 32 files, 31 conforming, 1 collision — the pre-existing foreign `solar-pro4` pair, unchanged |
| `run-dup-census.ps1` | `PASS (0 duplicate task IDs)`, exit 0 |
| `HUMAN RULING (2026-09-28)` entries on file | **3** — one from B5-0785, two from here, counted by reading the register back |
| Encoding of the appended segment | 17 × U+2014, **zero** U+00E2 mojibake |
| DECISIONS byte form | LF-only, LF-terminated |
| Row read back **off disk** | `B5-0787 / DONE / 7 pipes` |
| Both proposals read back **off disk** | yes — argument bodies confirmed unedited, only frontmatter plus inserted status paragraphs moved |

## Reusable lesson

**A ruling's scope is part of the ruling, and "reject" is not "revert".** Both of today's
rulings could have been executed destructively by a reasonable reader: one by deleting a
tool the rejected proposal happened to have delivered, the other by treating design
approval as a content licence. Neither was intended, and in both cases the difference
between the right and wrong action was thirty seconds of measurement and one sentence of
scope. When a human rules on a document rather than on a change, the document's *parts*
keep their separate fates — a proposal can be rejected while its one landed component
stays load-bearing, and approved while the question it deferred stays deferred.

## Still owed

1. **Not seeded — ID withdrawn.** An earlier draft of this list named **B5-0789** for the
   pass-bias ruling registration. That was wrong twice over: I never seeded the row, and
   **B5-0789 is a real, unrelated row already closed DONE by `solar-pro4:free` at
   06:08Z** — a full-pipeline verification (compile green, 643/643 conformance, smoke
   PASS). I named an ID I had not checked for freedom; the IDs I *did* check, B5-0785
   and B5-0787, were verified free immediately before the append. The pass-bias
   registration is genuinely still owed. A verified-free ID at the time of this
   correction: **B5-0801**.
2. *Known unresolved* in the HEARTBEATS README — one-line governance edit, own row.
3. Store-level **normalised-key** collision check in `validate-heartbeats.ps1` — own row.
4. The open question from Ruling 2: may a Power add-on card be authored, and with what
   text? Needs a human.
