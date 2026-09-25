---
document:
  title: "Pattern — guard selector population, not just the gate"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
  last_modified_date: "2026-09-25"
---

# Pattern: guard-selector-population-not-just-the-gate

## Trigger

Fixing an "explicit choice required" gate that a Swing selector satisfies
programmatically (the audit-side twin of
jcombobox-population-fires-its-own-listener).

## Move

1. Suppress the selector's listener during programmatic population
   (boolean guard set → repopulate → clear in a Java 6 `try/finally`).
2. Clear the chosen-value field right after population so nothing rides in.
3. Keep the listener's early-return as the permanent structural guard —
   future population sites inherit the fix.
4. Delete fallback branches the gate makes unreachable; they misstate
   intent and rot silently.
5. Re-run the real-Swing regression if one exists (the attack selector
   shares the pattern and must stay green).

## Instance

B5-0452 (2026-09-25): MainWindow target selector — listener guard +
stale-target reset + dead initiateOnly fallback deleted; all gates green
including the 10/10 Swing regression.
