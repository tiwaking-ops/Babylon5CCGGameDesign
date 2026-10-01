---
document:
  title: "A synthetic fixture id is a second id space, not a dead registration"
  status: "current"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 11", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  supersedes: none
  related: ["B5-1301", "B5-1125", "B5-0556"]
---

# Pattern: fixture ids are a second id space

## The failure this records

B5-1125's registration census counted card-id-shaped string literals in
`CardEffects.java` against **one id space: the card JSON files**. 45 of 46
literals resolved; the 46th, `enh_mines_rt`, resolved to zero records and was
flagged as "a registration for a card that does not exist — dead code that reads
as coverage." B5-1301 was seeded to adjudicate it.

The adjudication inverted the flag: `enh_mines_rt` is the B5-0556 Mines
Round-Trip **fixture id**, constructed in suite code
(`HeadlessConformanceTest.java` line 1816) and registered precisely so the
round-trip scenario's return-damage assertion (line 1858) can fire. Deleting it
would have turned the suite red while "removing dead code."

## The shape

Any repo with (a) an id-keyed registry in src and (b) a suite that constructs
synthetic model objects has **two legitimate id spaces**:

1. the data files (records a player can draw), and
2. the suite's synthetic fixtures (ids that exist only inside test code).

A census that enumerates only space 1 will classify every fixture registration
in space 2 as dead — and the flagged entries are the *load-bearing* ones, because
their only consumers are assertions.

## The check that would have caught it

Before classifying a registration dead, grep the id across the **whole src tree**,
not just the data files. A hit inside a `new XxxCard(...)` constructor call in
suite code is construction, not reference — the id is minted there, and the
registration is its contract with the engine. Corroborate with the receipt
corpus: a DONE close-out saying "register `<id>` in the static initializer" is
the provenance record.

## Anti-pattern to avoid

The complementary error is worse: treating "it appears in the suite" as license
to ignore a registration. A fixture id is live *only while its scenario lives* —
if the scenario is later deleted, the registration genuinely becomes dead. The
correct audit therefore pairs every suite-minted id with the line numbers of the
assertions that depend on it (here: 1750, 1816, 1858), so the next census can
verify the consumer still exists.

**Reusable lesson:** enumerate every id space that can legitimately mint ids —
data files AND suite fixtures — before classifying a registration dead, and pair
each fixture registration with the assertion lines that keep it alive.
