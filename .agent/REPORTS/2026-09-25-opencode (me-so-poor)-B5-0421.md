---
author_llm: opencode (me-so-poor / big-pickle)
---

# B5-0421 — Build-hygiene sweep

**Status:** DONE
**Agent:** opencode (me-so-poor) / big-pickle
**Date:** 2026-09-25

## Summary

Full-tree Java 6 construct sweep across `b5ccg/src/` (53 Java files), plus
all three build gates. **No Java 8+ constructs found; all gates green.**

## Java 6 construct grep — commands and results

Swept for: lambdas, method refs (`::`), streams, `computeIfAbsent`/Map
default-methods, `java.util.function`, `@FunctionalInterface`,
try-with-resources, diamond (`new X<>`), and `var` definitions.

| Construct | Command | Hits | Verdict |
|---|---|---|---|
| Method refs | `rg "::"` | 0 | clean |
| Streams | `rg "\.stream\(\)\|\.parallelStream\(\)\|\.Collectors\.\|java\.util\.stream"` | 0 | clean |
| Map default methods (incl. `computeIfAbsent`/`getOrDefault`/`putIfAbsent`) | `rg "computeIfAbsent\|computeIfPresent\|merge(\|getOrDefault\|putIfAbsent\|replaceAll(\|removeIf(\|java\.util\.function"` | 4 | false positives |
| `@FunctionalInterface` | `rg "@FunctionalInterface"` | 0 | clean |
| Try-with-resources | `rg "try\s+\([^)]*=[^)]*\)"` | 0 | clean |
| Diamond | `rg "new (ArrayList|HashMap|…)\s*<>"` | 0 | clean |
| `var` keyword | `rg "\bvar\s+\w+\s*="` | 0 | clean |
| Broad token scan (PowerShell, 123 hits) | `->\s`, `instanceof`, ternaries | 123 | all false positives |

The 4 `getOrDefault`/`replaceAll` hits verified as **Java 6-safe**:
- `DeckLoader.java:321` `getOrDefault` is a **private static helper**
  (`m.get(key)` + null-check), not `Map.getOrDefault` (Java 8).
- `ImageCache.java:26-28` `replaceAll` is the **String regex method**
  (Java 1.4+), not `List.replaceAll` (Java 8).
- The 123 broad-scan hits are comments/strings containing `->` or ` :: `
  (e.g. `HeadlessMultiRoundTest.java:200` log-format doc) plus Java 6
  ternaries and `instanceof` casts — no lambdas.

## Build gates — green

1. `compile.bat` (Windows):
   `Build successful` (1 pre-existing bootstrap warning, unchecked-op note on
   MainWindow — both pre-existing, not hygiene regressions).
2. `compile.sh` via Git Bash (`C:\Program Files\Git\bin\bash.exe`):
   `✓ Build successful. Run with: ./run.sh`
   — WSL bash was unavailable on this host (no /bin/bash in the relay);
   MSYS Git-Bash is the documented portable path (B5-0306).
3. `RUN_TESTS=1 sh compile.sh`:
   - `CONFORMANCE SUITE PASSED (360 checks)`
   - `SMOKE TEST PASSED` (446 cards; round 1 in ~13 s; 22 AI actions,
     29 UI callbacks, 4/4 legal)

## Notes

- No source, resource, or data edits — execution + report only.
- Gates run against the tree as-is (includes this session's B5-0413
  single-line harness fix and B5-0412 docs change).