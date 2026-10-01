---
document:
  title: "Seed-10 close-out: 12 OPEN rows plus B5-0622 divergence"
provenance:
  author_llm: {name: "muse-spark (muse-spark-1.3) seed-10", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-10-01"
---

# Seed-10 report (on user order: seed lots of tasks)

## What landed
12 rows as OPEN, claimable, seeding pass only. No claim held during the pass and none of the 12 worked. Own rows only; no foreign row, claim, heartbeat, report, or pattern touched; no src or data edit; no commit; no push.

Final own set: B5-1507 (BLOCKED-chain heads inventory), B5-1509 (DECISIONS truncation reconstruction sources), B5-1513 (random-10 cost distribution), B5-1515 (aftermath assert inventory), B5-1517 (stderr-only guards), B5-1519 (validator FUTURE SKEW findings), B5-1521 (offer-marker lifecycle proposal), B5-1523 (title-dedup audit), B5-1525 (UI enablement audit), B5-1527 (row-weight compliance), B5-1529 (dangling B5 references, diverged from 1505), B5-1531 (OWNER marker coverage, diverged from 1511).

Each row fences the OPEN rows it consumes rather than duplicates (B5-1323, B5-1331, B5-1343, B5-1345, B5-1435, B5-1439, B5-1443, B5-1445, B5-1451, B5-1457, B5-1471, B5-1501, BLOCKED B5-1319/B5-1321) and bounds itself: one scope, read-only or docs-only or proposal-only, report plus pattern plus heartbeat.

## Verification
- javac 1.8.0_292.
- Pre-append hash 4914D5026575040F05131CB76282CE0D89E897198C85A6A252C377AB1977D0C0 sampled stable twice.
- All 12 IDs pre-checked at zero ledger hits plus zero claim files seconds before the append; max at seed time B5-1501 with a three-slot buffer plus odd spacing.
- Post-write run-dup-census initially FAIL (B5-1505 x2, B5-1511 x2): a concurrent foreign wave landed B5-1505 (DECISIONS regrowth watch), B5-1508, B5-1511 (orientation block), B5-1514 after this pass appended.
- B5-0622 applied: own B5-1505 diverged to non-adjacent B5-1529, own B5-1511 to B5-1531 (both pre-checked free, zero hits plus no claim files); foreign rows left byte-identical with status untouched.
- Post-divergence run-dup-census PASS exit 0, 0 duplicates; ledger-query reads all 12 own rows 7 pipes doubleLead no UNCLAIMED reportable.
- Live claims fenced by scope and left byte-identical (B5-1401 Kilo-live, B5-1451 me-so-poor-live, B5-1501 Copilot-live, UNKNOWN B5-1405/B5-1447/B5-1475).

## Reusable lesson
A pre-write free-ID check is necessary and not sufficient because the race is against another reader, so the post-write duplicate census is the real gate and the divergence must move the detector's own rows, never the foreign ones.
