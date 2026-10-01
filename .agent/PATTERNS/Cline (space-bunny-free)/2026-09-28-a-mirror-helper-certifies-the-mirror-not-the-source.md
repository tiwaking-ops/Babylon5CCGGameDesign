---
document:
  title: "A harness that mirrors the helper under test certifies the mirror"
  status: "Reusable pattern"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Verify the real method, not your re-implementation of it

Learned on B5-0739, the same session that produced
[a null-coalesced lookup hides a failing assertion](2026-09-28-a-null-coalesced-lookup-hides-a-failing-assertion.md).
Two different routes to the same place: a check that certifies nothing.

## The shape

To call a private method from a reflection probe you need a `leader` argument.
The cheap way is to re-implement its selection rule:

```java
static Player leadingOf(GameState st, Player p) {          // my mirror
    Player lead = p;
    for (Player q : st.getPlayers())
        if (q.getInfluence() > lead.getInfluence()) lead = q;
    return lead;
}
```

The production `leadingPlayer` had been changed to rank by `getPower()`. My mirror
still ranked by `getInfluence()`. Every check using it passed — because **no
fixture had a POWER bonus**, so `Power == Influence` everywhere and the two
rankings were indistinguishable by construction.

The mirror was not merely redundant. It was *blind exactly where the production
code had been changed*, and the run was explicitly scoped to that change.

## The fix

Invoke the real method; it is private, not inaccessible:

```java
java.lang.reflect.Method leadM = AIPlayer.class.getDeclaredMethod(
        "leadingPlayer", GameState.class, Player.class);
leadM.setAccessible(true);
Object realLeader = leadM.invoke(ai, fst, pSelf);
```

The same run then produced a real discriminator: a board where the influence gap
is 8 and the power gap is 5, straddling a threshold of 6. The influence reading
offers SURRENDER; the power reading withholds it. One boolean separates the two
implementations, and it now reads from the source.

## The general rule

**A mirror helper in a harness is a second, untested copy of the thing under
test, and it is blind precisely where the production code is interesting.** If
you catch yourself re-deriving a rule to feed a reflective call, that is the
signal to reflect the rule's owner instead. `setAccessible(true)` costs one line
and removes an entire class of silent divergence — the copy can never drift,
because there is no copy.

Corollary, and the tell: if your fixtures all sit at the degenerate point of a
model (Power == Influence, tension 0, unrest at its floor), then *no* check you
write over them can distinguish a changed implementation from an unchanged one.
Green over a degenerate fixture is not weak evidence — it is **zero** evidence
about the change you were asked to verify.

This is a sharper form of the same lesson the other pattern records. Both are
instances of a check that reports success without having observed the value it
claims to measure.
