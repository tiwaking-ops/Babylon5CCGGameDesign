---
document:
  title: "B5-1147 — the B5-1107 deletion verified landed-clean by an independent run of the receipt method"
  status: "DONE 2026-09-30"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  claimed_at: "2026-09-30T07:14:35Z"
  instrument: ".agent/tmp_b51043/DupTitleProbe.java through DeckLoader.loadBothSets, whole stderr captured (the B5-1104 receipt method B5-1107 itself used)"
---

# B5-1147 — landed clean

## The receipts (this run, whole-stderr capture)

| check | expected | observed | verdict |
|---|---|---|---|
| total UNKNOWN FIELD lines | 5,396 (5,398 − 2 timing) | **5,396** | ✓ |
| `UNKNOWN FIELD: timing` lines | 0 | **0** | ✓ |
| pool census | 446 / 383 deluxe / 63 premiere-only / 0 dups | `pool=446 duplicateTitles=0 unionTitles=446 poolMissingFromUnion=0 deluxeOnlyTitles=0` | ✓ |
| armistice records load | both twins, without the key | armistice appears only in the expected-set noise (`id`/`title`/`type` × 2 passes = 12 lines, both `event_armistice` and `de_event_armistice`) — **no `timing` line** | ✓ |

**Verdict: landed-clean.** The B5-1107 deletion is on the tree, the load
is warning-free for `timing`, the armistice twins parse and enter the pool,
and the remaining 5,396 lines are exactly the known expected-set noise
class (the B5-1068/B5-1111 six-set-adds fix, untouched here).

Note on the row's "zero UNKNOWN FIELD lines" phrasing: the strict zero is
unreachable until B5-1068 lands, because the noise class fires on every
standard field (the B5-1111 decomposition). The meaningful assertion —
*zero for the deleted key* — is measured and holds. This is the row's own
escape hatch ("report landed-clean or regressed with the exact stderr line
if any") applied honestly rather than literally.

Execution only: no src or data edit, no commit, no push.

**Reusable lesson:** a verification row that inherits a numeric receipt
should assert the receipt's meaningful clause, not its loosest phrasing —
"zero warnings" was never the promise; "the deleted key's warnings are
gone and nothing else moved" was.
