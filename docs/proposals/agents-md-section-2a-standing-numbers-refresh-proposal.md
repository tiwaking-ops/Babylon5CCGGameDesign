---
document:
  title: "AGENTS.md section 2a standing-numbers refresh — corrected paragraph"
  status: "Proposal (never truth until merged; docs/proposals/ confers no authority)"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free) 16", version: "kilo-auto/free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Kilo (kilo-auto/free) 16", version: "kilo-auto/free"}
  created_date: "2026-10-02"
  last_modified_date: "2026-10-02"
---

# Proposal: refresh the AGENTS.md section 2a standing numbers

**This document proposes text. It does not change `AGENTS.md`.** Per the row's
scope (`do NOT edit AGENTS.md itself`) and per `AGENTS.md` §3 (`docs/proposals/` =
candidates, never truth until merged + compiled), the corrected paragraph below is
offered for a human or a separately-claimed row to merge. Nothing here is in
force.

## What triggered it

`B5-2089`, whose premise is `B5-2023`'s measurement: tracked Java under
`b5ccg/src` is 67, not the 65 recorded in §2a, because
`b5ccg/src/b5ccg/engine/B51823DeckCensus.java` is now tracked; and arrow prose is
51 lines / 65 occurrences, not the recorded 50 / 64.

**Re-measured this pass, 2026-10-02, with the shipped instrument:**

```
PYTHONIOENCODING=utf-8 py .agent/tools/census-b50960.py
```

Receipts, quoted from the run:

```
src file census: 67 {'(root)': 1, 'ai': 1, 'engine': 22, 'model': 37, 'ui': 5, 'util': 1}
archive file census: 32
=== TRACKED JAVA b5ccg/src 67 files ===
-- arrow lines-with-hits 53 occurrences 67 code-lines 0
```

`B5-2023`'s two numbers are **confirmed**. One further number moved since, and it
is the one that matters most, because it is the reason §2a's explanation is now
wrong rather than merely out of date — see Finding 2.

## Finding 1 — the two numbers B5-2023 measured are confirmed, and arrow prose has moved again

| quantity | §2a records | B5-2023 measured | measured this pass |
|---|---:|---:|---:|
| tracked Java, `b5ccg/src` | 65 | 67 | **67** |
| tracked Java, archive | 32 | 32 | **32** |
| arrow **prose** lines (occurrences) | 50 (64) | 51 (65) | **53 (67)** |
| arrow `code-lines` | 0 | 0 | **0** |
| `getOrDefault` code-lines | 14 | 14 | **14** |
| qualified `.getOrDefault(` | 0 | 0 | **0** |

The pass condition is unchanged and still met: **`code-lines 0` for every family**,
and the frozen archive still reads the opposite on every family (arrow 14,
methodref 9, stream 8, `computeIfAbsent` 2, `@FunctionalInterface` 1,
try-with-resources 1, diamond 32), which is the instrument's built-in validation.

Arrow prose grew from 51 to 53 lines and 65 to 67 occurrences since B5-2023 — two
more comment-band lines. All 53 are `PROSE` per the census; the row's expected
value ("grows with comments") is the right expectation and there is nothing to
report beyond the new figure.

The 14 `getOrDefault` hits remain **13 unqualified call sites plus the private
static helper's own declaration**, all inside `engine/DeckLoader.java` (`:317`,
`:318`, `:319`, `:320`, `:321`, `:322`, `:348`, `:359`, `:364`, `:372`, `:373`,
`:374`, `:562`, `:598`). A literal search for `.getOrDefault(` across
`b5ccg/src` returns **no files found**, which is the API-level signal and is still
0.

## Finding 2 — §2a's *explanation* of the denominators is now wrong, not just stale

§2a currently says:

> The census counts tracked `.java` paths, not every path under each directory.
> `git ls-files -- b5ccg/src` currently returns 67 paths because two tracked
> coordination JSON files remain under `b5ccg/src/.agent/`; the archive returns 33
> paths because its tracked `README.md` is not Java. The working tree has one
> additional untracked Java source,
> `b5ccg/src/b5ccg/engine/B51823DeckCensus.java`, so the live source tree has 66
> Java files while the census intentionally scans the 65 tracked Java paths.

Measured with `git ls-files -- b5ccg/src`:

| measurement | value |
|---|---:|
| tracked paths under `b5ccg/src` | **116** |
| of which `.java` | 67 |
| of which `.class` (tracked build output) | **47** |
| of which coordination JSON | 2 |
| Java files **on disk** under `b5ccg/src` | **78** |
| untracked Java on disk | **11** |
| archive tracked paths | 33 (32 Java + `README.md`) — **unchanged, still true** |

Two sentences in that paragraph are now false:

1. **"`git ls-files -- b5ccg/src` currently returns 67 paths"** — it returns
   **116**. The coincidence that made the old number look like a deliberate
   cross-check (67 paths, 65 Java, 2 JSON) has dissolved: **47 tracked `.class`
   build outputs** now sit under `b5ccg/src/b5ccg/engine/` and
   `b5ccg/src/b5ccg/model/`, and they are the reason the path count is 116.
   These are build artifacts under the source tree — they are not the census's
   concern, but they are the reason the path/Java arithmetic in §2a no longer
   reconciles, and a reader who runs the command to check the number will get 116
   and no explanation.
2. **"the live source tree has 66 Java files"** — it has **78**. And the gap is
   no longer one file: 11 Java sources on disk are untracked, namely
   `ai/AIDecisionEngine.java`, `ai/AIMemory.java`, `engine/B51975ReplayProbe.java`,
   `engine/B51996FactionStateProbe.java`, `engine/FactionState.java`,
   `engine/FactionStateBook.java`, `engine/MiniJson.java`,
   `engine/ReplayRecorder.java`, `model/SustainedAction.java`,
   `model/enums/MarkType.java`, `model/enums/SustainedActionType.java`.
   67 + 11 = 78. `B51823DeckCensus.java` is no longer among them — it is tracked
   now, which is exactly the change that made `B5-2023`'s 67 correct.

The archive sentence needs no change: `git ls-files -- b5ccg/src-java8-archive`
still returns 33 paths, 32 Java plus the tracked `README.md`, and the archive has
no untracked Java (32 on disk, 32 tracked).

**The practical consequence to state plainly:** the census scans **67** tracked
Java paths while the live source tree contains **78**. Eleven compiled-and-tested
Java sources — including the whole `FactionState`/`FactionStateBook` pair, the
replay probe, and both `SustainedAction` files this pass's own census read — are
invisible to the standing gate by construction, because it enumerates tracked
paths. That is a real coverage statement and §2a should say it, rather than
describing a one-file delta that no longer exists.

## Proposed replacement text

For the paragraph beginning `The census counts tracked `.java` paths` through the
end of the archive parenthetical, and for the two table rows that changed:

> The census counts tracked `.java` paths, not every path under each directory.
> As of 2026-10-02, 67 Java sources under `b5ccg/src` are tracked and 32 under
> the frozen archive. `git ls-files -- b5ccg/src` returns **116** paths, not 67:
> 47 of those are tracked `.class` build outputs sitting under the source tree and
> 2 are coordination JSON under `b5ccg/src/.agent/`. The archive returns 33 paths
> because its tracked `README.md` is not Java.
>
> The working tree holds **78** Java sources under `b5ccg/src` — 11 more than the
> census scans. Those 11 are untracked, so the standing gate cannot see them by
> construction: `ai/AIDecisionEngine`, `ai/AIMemory`,
> `engine/B51975ReplayProbe`, `engine/B51996FactionStateProbe`,
> `engine/FactionState`, `engine/FactionStateBook`, `engine/MiniJson`,
> `engine/ReplayRecorder`, `model/SustainedAction`, `model/enums/MarkType`,
> `model/enums/SustainedActionType`. A Java 6 construct introduced in any of them
> would compile green and pass `javac -source 6` while never appearing in this
> census. Tracking them, or enumerating the census over on-disk sources instead of
> tracked ones, is the change that would close the gap; neither is done here.

Table rows to change (all other rows stand):

| family | §2a records | measured 2026-10-02 |
|---|---|---|
| arrow **prose** lines | 50 (64 occurrences) | **53 (67 occurrences)** |
| everything else | — | unchanged, and `code-lines 0` still holds |

And the header line `Standing state, re-measured 2026-10-02 (65 tracked Java files;
32 archive Java files)` becomes `Standing state, re-measured 2026-10-02 (67 tracked
Java files; 32 archive Java files)`.

## Options considered and rejected

* **Delete the denominators entirely** and leave only the pass condition
  (`code-lines 0`). Rejected: the numbers are the row that lets a reader check the
  census is actually scanning something, and §2a's own text explains why the census
  exists separately from `javac`. A standing gate with no stated denominator is the
  B5-1725 silent-green shape with the receipt removed.
* **Point §2a at `B5-2023`'s report and stop restating numbers.** Rejected for the
  same reason in a different costume — arrow prose has already moved once since
  `B5-2023` (51→53 lines, 65→67 occurrences), so a pointer would be stale within a
  day and the governance file would carry no current figure at all.
* **Fix the tracked `.class` files first, then restate.** This is the better end
  state — 47 build outputs under `b5ccg/src` is a real hygiene problem — but it is
  a `git rm --cached` operation on 47 paths, it is not this row's scope
  (`docs/proposals` only), and the *numbers* are wrong either way until someone
  acts. The paragraph above is correct whether or not the `.class` files are later
  removed; if they are removed, `git ls-files` returns 69 and only the 116 needs
  revisiting.
* **Edit `AGENTS.md` now.** Rejected explicitly by the row and by §3.

## Recommendation

Merge the paragraph, then open a separate claimed row for the 47 tracked `.class`
files under `b5ccg/src` — that is a change-tracking question (is Git tracking
build output?) rather than a governance-text question, and it is the larger of the
two findings.

**Reusable lesson:** when a governance file reconciles two numbers that *happen*
to be close (67 paths, 65 Java, 2 JSON), record the reason they differ, not just
the values — because the reconciliation is what breaks. Here a third, unrelated
change (tracked `.class` build output) moved the path count to 116 while leaving
the Java count at 67, so the paragraph that existed to make the numbers checkable
became the thing that could not be checked.