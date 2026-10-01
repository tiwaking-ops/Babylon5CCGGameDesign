---
document:
  title: "B5-1508 — armistice timing-key one-field diff text for the gated B5-1433 row"
  status: "Proposal (advisory; nothing applied)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  last_modified_date: "2026-10-01"
---

# Armistice timing-key one-field diff text

## Status and boundary

Proposal only. No card JSON, source, queue, or coordination file is changed by
this document. It consumes the DONE B5-1411 classification (hunk matches the
settled B5-1101 adjudication plus B5-1107 authorization) and pre-derives the
exact edit the gated B5-1433 row applies, so its owner never re-derives the
change under clock pressure. B5-1433 remains gated claim ONLY after B5-1047 is
DONE plus B5-1101 unblocked; both rows, and B5-1045 upstream, stay byte-identical
and untouched here.

## Target record

- File: `b5ccg/resources/cards/deluxe.json`
- Card id: `de_event_armistice` (B5-1411 recorded it at line 3248; anchor on the
  card id plus key name, never the line number, which can drift)
- Field: `"timing": "ANY"` — the only record in the 829-card pool carrying a
  `timing` key (B5-1101 finding; premiere twin `event_armistice` has none; zero
  code consumers, two prose DeckLoader comments only)

## Exact proposed diff

`"timing"` is the **last** key of the card object, so the one-field deletion is
a two-line change: the `timing` line is removed and the trailing comma on the
preceding `text` line must go with it, or the file stops parsing. Byte-exact
before/after field lines (4-space indent, as recorded by B5-1411):

```diff
     "faction": "ANY",
     "set": "DELUXE",
     "imageKey": "armistice",
-    "text": "Play before a conflict resolves. Cancel one Military conflict. No Aftermath cards may be played. (Deluxe text change: the player who played Armistice gains 1 Influence.)",
-    "timing": "ANY"
+    "text": "Play before a conflict resolves. Cancel one Military conflict. No Aftermath cards may be played. (Deluxe text change: the player who played Armistice gains 1 Influence.)"
   },
```

Apply it by exactly this hunk against the current working tree (which already
carries it uncommitted). Verify the remainder is byte-identical after the edit:
no other key, card, or file changes, and the text value itself is unchanged.

## Verification commands B5-1433 already names

Run these after applying; all are text receipts, none is a new invention:

1. Hunk shape: `git diff -- b5ccg/resources/cards/deluxe.json` shows exactly one
   hunk touching only `de_event_armistice`, matching the diff above (timing line
   removed, text line's trailing comma removed, nothing else).
2. Validating loader: build first (`sh b5ccg/compile.sh` or `b5ccg/compile.bat`,
   JDK 8 `-source 6 -target 6`, expected bootstrap warning), then run the
   DeckLoader `loadBothSets()` path (B5-1411 used the headless harness). Expect
   load success with **zero** UNKNOWN FIELD reports for `timing` — the
   DeckLoader comment names this record as the one that produced the warning;
   after the deletion that warning class is gone.
3. Pool census: pool = **446** with **383 DELUXE** plus **63 premiere-only**
   after title dedup, **zero duplicate titles**, exactly one "Armistice" record
   (the deluxe copy), 829 total records across both files.
4. Grep receipts: `"timing"` occurrences in `b5ccg/resources/cards/deluxe.json`
   = 0 (premiere.json is already 0); `grep -rn "timing" b5ccg/src/` still finds
   only the two prose comments, no consumer.

If any receipt diverges — pool not 446/383/63, a second timing occurrence, or a
second changed hunk — stop and re-classify; do not widen the edit.

## Gates and fences

B5-1433 keeps its own gate: claim only after B5-1047 DONE plus B5-1101
unblocked. This proposal changes neither row, the B5-1045 upstream chain, the
working-tree JSON, or any coordination file; the fenced rows stay byte-identical.

**Reusable lesson:** a one-field JSON deletion that removes the object's last
key is a two-line edit — the trailing comma on the preceding line must go with
it, and the pre-derived diff text must encode that or the gated row's owner
re-derives it under clock pressure and can land invalid JSON.
