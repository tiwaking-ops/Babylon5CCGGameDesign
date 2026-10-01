---
author_llm: {name: "GitHub Copilot (Auto mode) 1101", version: "Auto mode"}
assessor_llm: []
created_date: "2026-10-01"
last_modified_by_llm: {name: "GitHub Copilot (Auto mode) 1101", version: "Auto mode"}
last_modified_date: "2026-10-01"
---

# B5-1529 — dangling B5-reference census

## Method

Read `.agent/TASK_LEDGER.md` as UTF-8 and treated the first table cell of each
well-formed task row matching `B5-\d{4}[a-z]?` as the defined-ID set. Every
B5-shaped token in each task row was then compared with that set. This is a
read-only census: no ledger note, foreign row, claim, heartbeat, source file,
card data, or deck data was changed. The B5-1451 placement-census row was read
first and its scope was fenced; this task measures dangling references, not
placement hashes or append stability.

## Result

The ledger contains 718 defined task IDs across 718 parsed task rows. The
census found **30 missing IDs in 56 citations across 41 citing rows**:

| Missing ID | Citations | Citing rows (ledger line) |
|---|---:|---|
| B5-0002 | 1 | B5-0777 (942) |
| B5-0003 | 1 | B5-0777 (942) |
| B5-0005 | 1 | B5-0624 (765) |
| B5-0370a | 1 | B5-0624 (765) |
| B5-0518 | 2 | B5-0521 (534), B5-0841 (1000) |
| B5-0524 | 2 | B5-0529 (582), B5-0536 (595) |
| B5-0525 | 2 | B5-0529 (582), B5-0536 (595) |
| B5-0582 | 1 | B5-0590 (697) |
| B5-0588 | 3 | B5-0591 (692), B5-0592 (695), B5-0607 (714) |
| B5-0618 | 8 | B5-0622 (757), B5-0767 (931), B5-0821 (984), B5-0900 (1002), B5-1401 (1294), B5-1451 (1322), B5-1457 (1325), B5-1615 (1382) |
| B5-0654s | 1 | B5-0801 (967) |
| B5-0912 | 1 | B5-0767 (931) |
| B5-0913 | 1 | B5-0767 (931) |
| B5-1109 | 8 | B5-1121 (1231), B5-1165 (1252), B5-1171 (1255), B5-1173 (1257), B5-1313 (1268), B5-1321 (1272), B5-1341 (1282), B5-1431 (1309) |
| B5-1211 | 1 | B5-1343 (1283) |
| B5-1234x | 1 | B5-0624 (765) |
| B5-1263 | 1 | B5-1317 (1270) |
| B5-1265 | 1 | B5-1315 (1269) |
| B5-1275 | 3 | B5-1333 (1278), B5-1529 (1347), B5-1615 (1382) |
| B5-7001 | 1 | B5-1004 (1109) |
| B5-9001 | 3 | B5-0775 (936), B5-0771 (940), B5-0953 (1045) |
| B5-9003 | 1 | B5-0953 (1045) |
| B5-9004 | 2 | B5-0953 (1045), B5-1020 (1129) |
| B5-9005 | 2 | B5-1020 (1129), B5-1030 (1139) |
| B5-9006 | 1 | B5-1020 (1129) |
| B5-9101 | 1 | B5-1017 (1126) |
| B5-9102 | 1 | B5-1017 (1126) |
| B5-9103 | 1 | B5-1017 (1126) |
| B5-9104 | 1 | B5-1017 (1126) |
| B5-9999 | 2 | B5-0658 (824), B5-1611 (1380) |

## Gate/premise classification

* **Synthetic fixture IDs:** B5-0002, B5-0003, B5-0005, B5-0370a,
  B5-0654s, B5-0912, B5-0913, B5-7001, B5-9001, B5-9003, B5-9004,
  B5-9005, B5-9006, B5-9101..B5-9104, and B5-9999 are named as test,
  fixture, regex, or harness records inside the citing premise. Their absence
  is expected for a live-task ledger unless those fixtures are intentionally
  registered as tasks.
* **Historical/renumbering references:** B5-0524 and B5-0525 are explicitly
  described by B5-0529 and B5-0536 as ghost rows renumbered to B5-0527 and
  withdrawn. B5-0582 is explicitly described by B5-0590 as VOID after an ID
  move. B5-0618 is cited as collision-forensics precedent by eight rows, not
  as a gate requiring a currently claimable row. B5-0518 and B5-0588 are older
  gate/provenance references whose original rows are absent and need owner
  adjudication before any repair.
* **Current behavior/gate references:** B5-1109 is cited by eight later
  sponsor-cost, UI, engine, and crosscheck premises; B5-1211 gates B5-1343;
  B5-1263 and B5-1265 are read-first census fences; these are not synthetic
  fixtures and should be resolved by their owning follow-up rather than
  silently rewritten here.
* **Dangling repair chain:** B5-1275 is cited by B5-1333 as a read-first
  post-exclusion probe, by this row as the measured example, and by B5-1615
  as its own repair target. No B5-1275 task row exists. B5-1615 is the
  explicit repair owner and was left untouched.

No row was repaired, no missing task was created, and no status or note cell
other than this task's eventual close-out status was changed.

**Reusable lesson:** A dangling-reference census must report the citing row and
the reference's gate or premise classification, because historical fixtures,
withdrawn IDs, and live dependency gaps all look identical to a token-only
grep.
