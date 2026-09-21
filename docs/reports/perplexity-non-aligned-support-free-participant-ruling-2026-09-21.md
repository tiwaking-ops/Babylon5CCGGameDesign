---
document:
  title: "Q4 — freeParticipant (Non-Aligned Support) — Babylon 5 CCG Ruling"
  status: "Advisory working document (not canonical, not a ruling)"
provenance:
  author_llm: {name: "Perplexity AI", version: "2026 release"}
  assessor_llm:
    - {name: "opencode", version: "big-pickle"}
  last_modified_by_llm: {name: "opencode", version: "big-pickle"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-21"
---

# Purpose and Origin

This document is an **advisory working document**, not canonical and not a
ruling. It is a chat report produced by **Perplexity AI (2026 release)** in a
session where OpenCode / big-pickle asked Perplexity to clarify the meaning of
**"free participant"** on the **Non-Aligned Support** conflict card. It was
assessed and stored into this repository by opencode / big-pickle on
2026-09-21 under the analyze-assess-record-store workflow.

Assessment verdict (opencode / big-pickle, 2026-09-21): the interpretation is
**consistent** with this repo's canonical `BABYLON5_CCG_RULEBOOK.md`, glossary
"Free" (`BABYLON5_CCG_RULEBOOK.md:1154`): "If you are permitted to sponsor a
card for free you may do so ... for no influence cost and without rotating a
sponsoring character. You must meet any other restrictions." Storage of this
file confers **no authority**; no DEC is inferred. The stored copy below is the
original report body with its frontmatter normalized to this repo's provenance
model.

---

# Chat Report — Q4 freeParticipant (Non-Aligned Support)

## Overview

This report documents the findings, interpretations, and conclusions from a single chat session in which OpenCode requested clarification on the meaning of **"free participant"** as used on the **Non-Aligned Support** card in the *Babylon 5 Collectible Card Game* (CCG). [19][20][34]

The session produced a rules interpretation consistent with OpenCode's documentation standards: concise, auditable, and suitable for inclusion in project rules (`AGENTS.md`), decision records, or a dedicated rules reference. [24][31][34]

## Context

OpenCode presented the following question:

> **Q4 — freeParticipant (Non-Aligned Support).** That card says a Non-Aligned fleet "may join as a free participant."  
> What does it mean by "Free Participant"?

This query arises in the context of implementing or validating game logic for the Babylon 5 CCG, where precise interpretation of card text affects conflict resolution, aftermath targeting, and influence accounting. [3][4][8]

## Findings

### 1. "Participant" Definition

In the Babylon 5 CCG rules:

- A **participant** is any character, fleet, or location that **supports, opposes, or attacks** during a conflict. [3][8]
- Participant status matters because certain **aftermath cards** specifically target "participant" cards in play. [3][15]

Therefore, when a fleet "joins as a participant," it becomes a valid target for effects that reference participants in that conflict. [3][8]

### 2. "Free" / "Play for Free" Definition

The rules define **"play for free"** (and by extension, "join as a free participant") as:

- **No influence cost** is paid to bring the card into the conflict. [3][4]
- **No Inner Circle character** needs to be rotated to sponsor the card. [3][4]
- All other card restrictions (loyalty, timing, limits, etc.) still apply. [3][4]

This matches the general pattern in the game where "free" modifies the cost and sponsorship requirements, not the functional role of the card in the conflict. [3][4]

### 3. Combined Meaning on Non-Aligned Support

On **Non-Aligned Support**, the phrase:

> "a Non-Aligned fleet may join as a free participant"

means:

1. The chosen Non-Aligned fleet **becomes a participant** in the conflict (it supports, opposes, or attacks as specified by the card's effect). [3][8]
2. It does so **without paying influence** and **without rotating an Inner Circle character** to sponsor it. [3][4]
3. The fleet is then treated like any other participant for purposes of aftermath and other participant-targeting effects. [3][15]

## Decisions and Conclusions

### Decision 1 — Rules Interpretation

**Decision:**  
"Free participant" on Non-Aligned Support is interpreted as: *a Non-Aligned fleet that joins the conflict as a normal participant, but does so at zero influence cost and without requiring a sponsor rotation.* [3][4][8]

**Rationale:**

- Aligns with the explicit rules definitions of "participant" and "play for free." [3][4][8]
- Preserves the intended power level: the fleet participates fully (and can be targeted by aftermaths) but is cheaper and easier to bring in. [3][15]
- Consistent with similar wording patterns elsewhere in the game (e.g., other "free" support/oppose effects). [3][4]

**Consequences:**

- Implementation can treat such fleets identically to other participants after they join, except for the cost/sponsor step. [3][8]
- Aftermath cards that target participants will correctly apply to these fleets. [3][15]

### Decision 2 — Documentation Placement

**Decision:**  
This interpretation should be recorded in one or more of the following locations, per OpenCode conventions: [19][20][24][34]

- Project rules file: `AGENTS.md` (under a "Babylon 5 CCG Rules" or "Game Logic" section). [24][31][34]
- A dedicated rules reference: e.g., `docs/rules/babylon5-ccg.md` or `rules/babylon5-ccg.md`. [24][34]
- Optionally, as a lightweight decision record if this interpretation resolves a previously ambiguous or debated point: e.g., `docs/decisions/YYYY-MM-DD-b5-ccg-free-participant.md`. [17][23][29]

**Rationale:**

- Ensures persistence across sessions and compactions (avoids re-deriving the same ruling). [24][28]
- Makes the ruling discoverable by agents and subagents working on game logic or test cases. [19][20]

## Unresolved Issues and Open Questions

The following items were not fully resolved in this session and may warrant future investigation:

1. **Edge Cases with Existing Participation**  
   - What happens if the chosen Non-Aligned fleet is already participating in the conflict via another effect?  
   - Does "join as a free participant" allow re-joining, or is it a no-op if already participating?  
   *Status:* Not tested against full rules text or official rulings. [3][4]

2. **Interaction with Specific Aftermath Cards**  
   - Are there any aftermaths or conflict effects that treat "free participants" differently from regular participants?  
   *Status:* No evidence found; default assumption is no special treatment, but a comprehensive card-by-card review was not performed. [3][15]

3. **Official Rulings vs. Community Interpretations**  
   - Some sources are community-maintained (e.g., VASSAL rulings PDFs, fan sites).  
   - An official Precedence/WotC rulings document, if available, should be consulted to confirm this interpretation. [4][5]

## Provenance and Audit Trail

- **Source of Interpretation:**  
  Derived from publicly available Babylon 5 CCG rules summaries and rulings documents, including:  
  - Rules excerpts describing "participant" and "play for free" mechanics. [3][4][8]  
  - Aftermath and conflict interaction notes referencing participant targeting. [3][15]

- **Session Metadata:**  
  - Date: 2026-09-21  
  - Platform: OpenCode (AI coding agent)  
  - Assistant: Perplexity AI (2026 release)  
  - Document Type: Chat Report (Markdown)  

- **Related Artifacts (Suggested):**  
  - `AGENTS.md` — project rules including Babylon 5 CCG interpretations. [24][31][34]  
  - `docs/rules/babylon5-ccg.md` — dedicated rules reference. [24][34]  
  - `docs/decisions/YYYY-MM-DD-b5-ccg-free-participant.md` — decision record, if desired. [17][23][29]

## References

- Babylon 5 CCG rules and rulings (participant definitions, "play for free" mechanics). [3][4][8]  
- OpenCode documentation on agents, skills, rules, and project structure. [19][20][24][31][34]  
- Decision record and governance templates for structuring formal rulings. [17][23][29]

---

*End of Report*

---

# Assessor addendum (opencode / big-pickle, 2026-09-21)

Verified against `BABYLON5_CCG_RULEBOOK.md` glossary "Free"
(`BABYLON5_CCG_RULEBOOK.md:1154`). The stored document's "Decision 2 —
Documentation Placement" and its reference indices [3][4][8] etc. are the
original chat report's own citations and template suggestions; they do **not**
map onto this repository's governance documents and carry no authority here.
Nothing above is promoted, superseded, or reclassified by storage.