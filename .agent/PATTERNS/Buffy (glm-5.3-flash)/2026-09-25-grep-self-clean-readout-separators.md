---
document:
  title: "Pattern — grep-self-clean readouts: separators and probe ctor checks"
  status: "Pattern record (advisory, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: display strings must self-clean against the repo's lint greps

Filed under the standing "Reusable lesson" convention (00_BOOT step 10).
First applied in `.agent/REPORTS/2026-09-25-Buffy-(glm-5.3-flash)-B5-0429.md`.

## The lessons

1. **Lint greps are part of the readout's interface.** The HANDOFF §5
   banned-token grep (`->`, `::`, `stream(`, `computeIfAbsent`,
   `@FunctionalInterface`, `try (`) scans file *content*, not syntax — so a
   display string that merely prints `->` fails the release gate. Pick display
   separators that cannot collide (`SRC to TGT`, not `SRC->TGT`), and run the
   banned-token grep as the FIRST verification step on any touched file,
   before compiling, so a violation is a one-line fix rather than build noise.

2. **Check ctor surfaces by grep, not memory.** Probe fixtures compiled three
   times before running because `LocationCard` takes `Rarity`/`CardSet` enums
   (`Rarity.FIXED`, `CardSet.PREMIERE`), not int/string rarities, and the
   enums package needed its own import. One
   `grep -n "public <Ctor>("` per class beats repeated compile-fix cycles on
   throwaway probes.

3. **State-differential pixel checks beat fixed bands.** Instead of trusting
   exact glyph baselines (text pixels sit ABOVE the baseline, so band math
   drifts), render twice around a state mutation and count target-color pixels
   within a loose region: orange tension row 0 → 232, red war marker 0 → 132,
   back to 0 on reset. Thresholds (+20/+30/±5) make the assertions robust to
   font rendering differences.

## Procedure

1. Run the banned-token grep on the touched file before compiling.
2. Grep each probe-constructed class's public ctors/enums before writing the
   fixture.
3. Paint probes: `update()` + `printAll` onto a BufferedImage; compare pixel
   counts across a state mutation rather than absolute bands; assert both the
   rise (marker appears) and the return (marker clears).
4. Delete scratch files before close-out.
