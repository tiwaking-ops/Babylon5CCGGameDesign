---
author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
created_date: "2026-09-26"
---

# Standing Java 6 hygiene grep set for b5ccg/src/

A periodic full-tree grep for Java 6 violations is cheap insurance against
accidental modern-syntax creep. The grep set to run on every new source
addition to `b5ccg/src/`:

- Lambdas / method refs / switch expressions: `->|::`
- Streams / collectors / forEach / computeIfAbsent / @FunctionalInterface:
  `.stream\(\)|computeIfAbsent|@FunctionalInterface`
- Try-with-resources: `try \(`
- Diamond operator: `new (ArrayList|HashMap|HashSet|LinkedList|TreeSet|TreeMap)<\>\(`
- `record` keyword and `var` keyword (as type declarations, not identifiers)

Source files are small (57 files) so the grep completes in under a second.
The check has caught real incidents (B5-0421, B5-0409) — run it before
every checkpoint commit.

See: [[b5-0463]]
