---
document:
  title: "B5-2131 — Card set manifest proposal C4"
  status: "Proposal"
  provenance:
    author_llm: {name: "Cline", version: "space-bunny-free"}
    created_date: "2026-10-02"
    last_modified_by_llm: {name: "Cline", version: "space-bunny-free"}
    last_modified_date: "2026-10-02"
---

# B5-2131 — Card set manifest proposal C4

Proposal-only, no `b5ccg/src/` or `b5ccg/resources/` edits. This document drafts the set manifest format and required per-card collector metadata fields as mandated by B5-0311 finding C4 (P3/LOW), which recorded that no set-level manifest or per-card collector metadata exists in the `cards/` directory.

## 1. Background

B5-0311 (Card JSON data audit, report-only) scanned `b5ccg/resources/cards/` (two files: `premiere.json`, `deluxe.json`) and produced five findings. Finding C4 (P3/LOW) states:

> **No set-level manifest or per-card collector metadata**
> The `cards/` directory has exactly two files: `premiere.json` and `deluxe.json`. No subdirectories, no `cards/` index/manifest file, no per-set metadata file (set name, card count, version, retrieval date are only in the rulebook header and this report). Both are JSON arrays of card objects terminated by a trailing newline after the closing `]`.

The B5-0311 report's proposal P8 recorded: "if the project ever wants per-card collector numbers or set metadata, add them as optional fields and document the schema. Low priority; record only."

This proposal (B5-2131) expands P8 into a concrete manifest format and per-card field specification with a three-card example.

## 2. Current state

### 2.1 File layout

```
b5ccg/resources/cards/
├── premiere.json   # 224 cards (Premiere set)
└── deluxe.json     # 224 cards (Deluxe set)
```

Both files are JSON arrays of card objects. Each card carries a `"set"` string (`"PREMIERE"` or `"DELUXE"`) as its only set-level marker. No file-level header, no rarity distribution, no collector number, no artist, no reprint flag.

### 2.2 Rulebook card anatomy (canonical reference)

The rulebook (`BABYLON5_CCG_RULEBOOK.md` §I.3 "Anatomy of a Card") defines seven printed elements:

1. **Name**
2. **Influence Cost**
3. **Card Type**
4. **Abilities** (Diplomacy, Intrigue, Psi, Leadership / Military)
5. **Effects**
6. **Marks** (Shadow, Vorlon, etc.)
7. **Caption** (flavor text, no game effect)

Collector metadata (set code, collector number, rarity, artist, reprint status) is **not** part of the rulebook anatomy but is standard in physical CCG products for collection management and deck legality verification.