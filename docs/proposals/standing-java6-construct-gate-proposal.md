---
document:
  title: "Standing Java 6 construct gate — promote the B5-0960 census to an on-demand command"
  status: "Proposal — candidate, never truth until merged and compiled"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 6", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1012"
---

# Proposal: a standing Java 6 construct gate

Authored for B5-1012 (close-out 2026-09-30). The row asked three things; this
document answers PART 3 — the standing form. PART 1 (re-census) and PART 2
(violation verdict) live in
`.agent/REPORTS/2026-09-30-Buffy (glm-5.3-flash) 6-B5-1012.md`.

## Recommendation: a documented run-on-demand command, not a new installed gate

Adopt **one canonical command** that re-runs the B5-0960 census against the
current tree on demand, cited from `AGENTS.md` section 2. Do **not** wire it
into `compile.bat`/`compile.sh`, do **not** add it to CI, do **not** create a
new `.agent/tools/` script that shadows the snapshot.

The command (stdlib Python is already the repo's census provenance format,
per `tmp-scans/b50960/census.py`; no external library, so the human
external-library gate is not triggered):

```bash
PYTHONIOENCODING=utf-8 python tmp-scans/b50960/census.py > tmp-scans/b51012/census.txt
```

* `PYTHONIOENCODING=utf-8` is **load-bearing**, not cosmetic: the script prints
  raw source lines and the Windows console default (cp1252) raises
  `UnicodeEncodeError` on the box-drawing glyphs already present in tracked
  comments (this was hit and recorded during the B5-1012 re-run itself). The
  repo's PowerShell tools carry the same explicit-UTF-8 receipt convention.
* Read the output against the standing-state table below. **Pass condition:
  `code-lines 0` on every construct family under `TRACKED b5ccg/src`.**

## Which instrument is authoritative, and what each instrument is for

The row required this to be explicit because "a second opinion that disagrees
with javac is a defect in the second opinion, not in javac":

| instrument | proves | authoritative for | structurally blind to |
|---|---|---|---|
| `javac -source 6 -target 6` (compile gate) | **syntax** of every tracked file | everything the compiler sees: a real violation fails the build — decisive | the classpath/API dimension (`Map.getOrDefault` compiles fine at `-source 6` — the gate cannot see which class owns the method); files not compiled |
| the construct census (masked grep) | **record + API-level signal** | per-construct per-file history; the qualified-vs-unqualified `getOrDefault` distinction the gate cannot make; prose-vs-code deltas over time | nothing decisive — its `code-lines` column is advisory only |

**Therefore: javac is authoritative on violations; the census is authoritative
on the record and on the API-level class the gate cannot see. They are not
rivals — a clean grep is necessary, a red compile is decisive, and the census's
unique contribution is exactly the case where the grep is clean but a future
API-level Java 8-ism slips in (the `getOrDefault` trap, measured and documented
in B5-0960).**

A standing gate that disagrees with javac about *syntax* would be a defect in
the gate by construction; that is why this proposal claims no syntax authority.

## Standing state to expect (measured 2026-09-30, tracked src, 63 files)

| family | expected code-lines | note |
|---|---|---|
| arrow, methodref, stream, computeIfAbsent, computeIfPresent, compute, merge, @FunctionalInterface, try-with-resources, diamond, forEach, removeIf | **0** | any nonzero code-line count is a real finding: name the file/line, do not seed, a red compile would already have caught syntax violations |
| arrow prose lines | ~50 (was 47) | comment bands only; growth is expected as comments accumulate — prose is not a violation |
| getOrDefault | 14, all unqualified helper calls | `DeckLoader.getOrDefault(Map, key, def)` (private static, ~line 481 on the current tree); **qualified `.getOrDefault(` count must remain 0** — that number, not the raw 14, is the API-level signal |

The frozen archive is censused separately by the same script (all families
present as code) and is the instrument's built-in validation: same mask,
opposite verdict, both hand-confirmed in B5-0960. Never edit the archive.

## What this proposal does NOT do

* No edit to `b5ccg/src` (the census was read-only), no build-script change,
  no external library, no new tool file added to the repo (the script already
  exists as census provenance under `tmp-scans/`).
* No automatic enforcement. The value is that the snapshot stops rotting: any
  session can regenerate the census in seconds and diff it against the table
  above instead of re-deriving the methodology from scratch.

## Adoption path (if accepted)

1. Human or maintainer adds the command + standing-state table (or a pointer
   to it) to `AGENTS.md` section 2, with the sentence: "javac is authoritative;
   the census is the record and the API-level second look."
2. `docs/DECISIONS.md` gains the adoption entry; the proposal moves from
   candidate to merged. Until then this document confers no authority.
