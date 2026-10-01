---
document:
  title: "A null-coalesced lookup turns a broken assertion into a passing one"
  status: "Reusable pattern"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Assert the key exists before you assert anything about its value

Learned on B5-0739 (AI difficulty-contract verification), where the single most
convincing result in the run turned out to be false.

## The shape

A check reads a count out of a map and coalesces the miss to zero:

```java
Integer bld = hist.get("BUILD_INFLUENCE/led:amb_cA");
int bldPicks = bld == null ? 0 : bld.intValue();
check("EASY does not chase the war-favoured BUILD_INFLUENCE",
      bldPicks < RUNS * 35 / 100);
```

The real key was `"BUILD_INFLUENCE/amb_cA"` — no `led:` prefix. The lookup missed,
the count read **0/600**, and the check **passed**: it reported *perfect* neutrality
for a behaviour the probe was built to detect. A scorer-aware EASY would have read
~600/600, so the assertion would have caught the regression it existed for — if the
key had been right.

The failure is not the typo. It is that `0` and "key absent" are the same value,
and **0 is the value that makes the assertion succeed.** The bug is only
discoverable by an input that should have failed and didn't.

## The fix

Find the key structurally, then gate the value assertion on the lookup:

```java
String bldKey = null;
for (String k : hist.keySet())
    if (k.startsWith("BUILD_INFLUENCE")) bldKey = k;
check("B4a the key is actually present (anti-vacuous lookup guard)", bldKey != null);
int bldPicks = bldKey == null ? -1 : hist.get(bldKey).intValue();
check("B4 EASY does not chase it", bldPicks >= 0 && bldPicks < RUNS * 35 / 100);
```

The repaired check reads a **less impressive** 103/600 against a uniform
expectation of ~120. That is the point: it is a real measurement, and the earlier
`0/600` was not a better result but an absent one.

## The general rule

Any assertion whose *pass* condition coincides with a missing or defaulted input is
vacuous. Before shipping a check that reads a lookup, ask what value a total miss
would produce, and confirm that value fails. If it passes, the check is measuring
the absence of a key and calling it a property of the system.

This is AGENT_LOOP's "a test never observed red is not evidence" reached from the
other side: that clause warns about a check that has never failed; this one is
about a check that cannot fail because its failure mode is its success mode.

**Corollary — a green gate is not evidence until you have seen it go red.** I found
this only because three unrelated fixture bugs (a `setSplitTension` clamp at 5, a
`raiseTension` that does not enter war, and an arithmetic coincidence) forced
iteration. Had the first run been green, B4 would have shipped green and wrong.
