---
document:
  title: "Size the prerequisite that makes a subsystem's actors possible, not the subsystem"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Size the prerequisite that makes a subsystem's actors possible, not the subsystem

**Source task:** B5-0669 (Civil War, B5-0639 remainder R13).
**Record:** 2026-09-27, Buffy (glm-5.3-flash).

## The lesson

The Civil War rule (:990–:1009) reads as a large but self-contained
implementation task, and the triage seeded exactly that framing. Measured, the
gate was elsewhere twice over: the expected expansion gate was not there at
all (the section sits under §VI Additional Rules, not an expansion chapter),
and the real gate is that a Civil War needs *two factions of one race* —
actors the engine cannot construct, because race and faction identity are
1:1 everywhere and `TensionMatrix.raiseTension` no-ops when
`source == target`. The honest sizing was therefore of the **multi-faction
identity slice**, a prerequisite larger than the subsystem itself, with the
state machine as a sequel.

## The transferable rule

When a rule defines interactions among entities (factions of a race, holders
of a mark, parties to a state), before sizing the rule, ask: **can the
entities it names exist?** Two checks:

1. **Constructability** — is there any code path that builds the entity the
   rule needs? (`source != target` same-race tension; a Psi Corps faction of
   the Human race; a second ambassador.)
2. **Gate location** — is the rule gated where its chapter heading implies, or
   somewhere else entirely? Grep the section headers; do not trust the
   framing a triage row or an expansion-structure note hands you.

If the actors cannot exist, the deliverable is the design of the
prerequisite, a collapse rule for sibling remainders that need the same
actors, and an explicit "parked until a human orders it" — not a code row.

## Anti-patterns this heads off

- Seeding the machine as code when its actors are unconstructable — four
  sibling remainders (R11/R12/R13/R14) each implying their own architecture
  is how four partial implementations happen.
- Accepting a gate location from prose ("the expansion that introduces this")
  without checking the headers — the gate was multi-faction play, not an
  expansion.
- Modelling a new axis (unrest) as a variant of a nearby existing one
  (tension) because both are 1–5 numbers — the rulebook distinguishes them,
  and they clamp, start and scope differently.

**Filed alongside:** `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0669.md`
and `docs/proposals/civil-war-state-machine-design-proposal.md`.
