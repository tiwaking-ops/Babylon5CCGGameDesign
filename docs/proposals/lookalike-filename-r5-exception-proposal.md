---
document:
  title: "Proposal B5-1066 - the lookalike-filename R5 exception: what a later human ruling would have to decide"
  status: "Proposal"
provenance:
  author_llm: {name: "opencode (big-pickle)", version: "big-pickle"}
  assessor_llm:
  last_modified_by_llm: {name: "opencode (big-pickle)", version: "big-pickle"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# Proposal B5-1066 — the lookalike-filename R5 exception

**Status: PROPOSAL ONLY. NOT A RULING, NOT A TASK, NOT A PLEDGE.**
Nothing in this document has been executed. On 2026-09-30 the human was offered
option (a) *leave the 17 components alone* and option (b) *authorise the rename
as an exception to R5*, and ruled:

> (a) leave the 17 alone — R7 stops the next instance and R5 forbids the
> retro-fix
>
> for (b) create a proposal so there is a record of this exception. I will rule
> on it later, not now.

So (a) is DONE and (b) is this file: a record, written down so the exception
survives this session and can be ruled on without re-deriving the evidence. The
ruling is explicitly deferred. This document changes nothing on disk.

## 1. What the exception would be

`.agent/HEARTBEATS/README.md` **R5**, adopted by human ruling 2026-09-28
(decision B5-0785), reads:

> **R5 — never retro-rename an existing id.** `solar-pro4:free` is cited in 114
> ledger rows and 154 reports; renaming breaks every citation to fix a defect
> the rename causes.

An R5 exception would be: rename 17 filesystem paths whose names carry U+F03A
(FULLWIDTH COLON) to the canonical sanitised spelling, **without** altering a
single `agent_id`, and repair the citations the rename breaks. R5's actual
objection — that renaming an `agent_id` breaks citations — is respected; the
exception is to the blanket "never rename a path" reading, not to the
citation-preservation intent.

## 2. The measured evidence (re-verified 2026-09-30, not recalled)

Exactly **17 path components** carry U+F03A in their own name: **1 directory +
16 files**. A further 13 files inherit the codepoint only from their parent
directory's name, so **30 paths** contain it somewhere — the distinction
matters, because only the 17 are rename candidates in their own right.

| Path (U+F03A shown as `<U+F03A>`) | Bytes | Git | SHA-256 (12) |
|---|---:|---|---|
| `.agent\HEARTBEATS\_quarantine\solar-pro4<U+F03A>free.json` | 398 | untracked | `6936A5C7C0EF` |
| `.agent\PATTERNS\solar-pro4<U+F03A>free\` **(dir, 13 files inside)** | — | tracked | — |
| `.agent\REPORTS\2026-09-26-solar-pro4<U+F03A>free-B5-0487.md` | 5539 | tracked | `C007B1936E1B` |
| `.agent\REPORTS\2026-09-26-…-B5-0488.md` | 3654 | tracked | `D1301B6DE593` |
| `.agent\REPORTS\2026-09-26-…-B5-0509.md` | 3401 | tracked | `CEB48103CEA3` |
| `.agent\REPORTS\2026-09-26-…-B5-0511.md` | 1934 | tracked | `A91B725DEB68` |
| `.agent\REPORTS\2026-09-26-…-B5-0512.md` | 4105 | tracked | `8BB4566AD14A` |
| `.agent\REPORTS\2026-09-26-…-B5-0513.md` | 1478 | tracked | `03C7B4CA7B92` |
| `.agent\REPORTS\2026-09-26-…-B5-0514.md` | 3386 | tracked | `A5A586C5378D` |
| `.agent\REPORTS\2026-09-26-…-B5-0515.md` | 3166 | tracked | `115428223E03` |
| `.agent\REPORTS\2026-09-26-…-B5-0526.md` | 2350 | tracked | `777E01CC2A58` |
| `.agent\REPORTS\2026-09-26-…-B5-0534.md` | 2431 | tracked | `50E8F136AFF5` |
| `.agent\REPORTS\2026-09-27-…-B5-0571.md` | 1618 | tracked | `7E6006E2D6E8` |
| `.agent\REPORTS\2026-09-27-…-B5-0573.md` | 1513 | tracked | `B6308A2FA3FB` |
| `.agent\REPORTS\2026-09-27-…-B5-0574.md` | 4795 | tracked | `A0E329B58E4C` |
| `.agent\REPORTS\2026-09-27-…-B5-0588.md` | 3006 | tracked | `A272E7BB4CB7` |
| `b5ccg\src\.agent\HEARTBEATS\solar-pro4<U+F03A>free.json` | 240 | tracked | `A0C175FE7461` |

**14 tracked + 1 untracked file + 1 tracked directory.** The two heartbeats are
foreign files (R6: "authorise nobody to edit a foreign heartbeat"); the 13
files inside the PATTERNS directory are an advisory-tier agent's own namespace,
which this agent may read and must never write.

## 3. What a ruling would have to settle

An approval is **not** a single yes. These four are unresolved, and a ruling
that omits any of them is not executable:

1. **Two collisions are non-identical, so the rename is not mechanically
   lossless.** `…-B5-0515.md` exists at both spellings with *different content*
   (1523 vs 3166 bytes); same for `…-B5-0526.md` (1753 vs 2350). A ruling must
   name the suffix convention that keeps BOTH, e.g. `-<U+F03A>` retained as a
   disambiguator, or a merge decision. Overwriting either destroys a report.
2. **11 live ledger rows cite `PATTERNS/solar-pro4*` paths** (lines 42, 56, 57,
   62–64, 74, 75, 139, 152, 157, 160, 164, 183, 189). These must be repaired in
   the same change or the rename creates dangling citations. Prior form exists:
   under B5-0613 a rename-drift episode **hid 14 ledger rows**.
3. **`solar-pro4-free-0978` is unclassified** — neither a known spelling of
   `solar-pro4:free` nor obviously distinct. A rename needs a decision on it.
4. **Precedent points the other way.** B5-0793 faced this exact collision and
   left both files byte-identical on R5/R6 grounds; B5-0773 established
   quarantine-not-delete. An exception should say *why those were right to
   decline*, or it silently overrides two prior rulings.

## 4. The case for granting it

* The defect is real and observable: `validate-heartbeats.ps1` went from
  reporting **0** identity collisions on a store holding **3** to reporting all
  3 once `-Recurse` reached `_quarantine/`.
* Windows permits the name, so nothing warns; the two spellings coexist
  indefinitely and `*.json` globs cannot distinguish them.
* R7 (B5-1062) stops the *next* instance but deliberately does not repair these,
  per R5. The exception is the only route to a clean store.

## 5. The case against, which is currently stronger

* R5 exists precisely to prevent "rename cited paths to fix a defect the rename
  causes", and 11 live citations + 2 non-identical collisions mean the rename
  *does* cause the damage R5 names.
* The detected harm is bounded: 3 collisions, 1 of them already quarantined and
  out of the live store. No data is at risk while the names persist.
* Two prior agents, working from the same evidence, declined.

## 6. Current state

**17 components untouched, byte-identical, verified by the table in §2.** The
`kilo` repair completed earlier in this session was a *different* defect —
slash-derived directory nesting under `PATTERNS\kilo\`, fixed under its own
task — and is not part of this exception.

If a future ruling grants (b), the minimum executable form is: claim first,
`git mv` the 14 tracked reports, repair all 11 citations in the same change,
apply the §3.1 suffix to the 2 collisions, decide §3.3, and re-run
`validate-heartbeats.ps1` expecting **exit 0 with 0 collisions**. If it declines,
no action is needed — R7 already prevents recurrence, which is the point of
§5's "no data is at risk".

*Reusable lesson: a deferred ruling still needs its evidence written down while
fresh, or the next session re-derives it and re-litigates it.*
