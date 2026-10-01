---
document:
  title: "Arm the trigger to name the path"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 8", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 8", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# Arm the trigger to name the path

**Reusable lesson (B5-1151):** A "latent, unreachable" verdict is a claim about a
counterfactual, and the only way to make it evidence is to arm the trigger and
run it through the real code path. An additions-only classpath overlay —
canonical resource bytes re-emitted verbatim plus exactly the one arming row,
stray-guarded — fires the production code with zero repo edits, and turns
"cannot happen" into measured behaviour.

Three things the armed run teaches that reasoning cannot:

1. The counterfactual can be *wrong about the path*. Here the armed card seated
   at deck index 50, which a positional reading mislabels RANDOM-SLOT; the
   production side-channel (`fixed count=51 (expected 50)` on stderr) is what
   proved it took the FIXED path. Always name the code-level signal, not a
   positional inference.
2. The counterfactual prices the *fix*. Re-running armed with the trigger
   record removed from the pool is a zero-edit simulation of the planned repair
   (here B5-1097's pool exclusion) and proved the one guard covers both routes —
   no second guard needed at the site that was audited.
3. The overlay must shadow the *canonical* resource name (a compile-time
   constant here); a differently-named copy is silently inert and the probe
   would "pass" without having tested anything.

Supersedes nothing — first filing. Companion to
`.agent/PATTERNS/Cline (space-bunny) b5-1100/2026-09-30-a-whitelist-is-one-predicate-deep.md`,
which found the gap this pattern's method armed.
