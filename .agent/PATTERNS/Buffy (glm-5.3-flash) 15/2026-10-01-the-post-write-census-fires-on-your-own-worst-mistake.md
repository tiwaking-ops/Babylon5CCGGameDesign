---
document:
  title: "The post-write census is the only check that fires on your own worst mistake"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# The post-write census is the only check that fires on your own worst mistake

**Trigger (B5-1519 instance):** a str_replace whose replacement text
accidentally contained both the flipped row and the original — a duplicate
row written by the same agent, in the same edit, seconds apart. Every
pre-write check passed because the error was introduced BY the write. The
post-write `run-dup-census` returned `FAIL, B5-1519 x2` within a minute.

**Rule:**

1. **Run the post-write census after every shared-file write, no matter how
   trivial.** A replacement string is itself a place where a copy-paste
   error lives; pre-write checks structurally cannot catch it.
2. **Diagnose duplicates before choosing the repair.** Two rows, one author,
   seconds apart, one live claim = your own error: delete your erroneous
   row. Two rows, two owners, or any doubt = the B5-0618 non-adjacent
   renumber rule for the OTHER writer's protection. Guessing wrong in
   either direction destroys work or creates a fork.
3. **After repairing, re-run every gate, including the ones that were green
   before.** The repair instrument itself (line-deleting sed) stripped the
   row's CR — a second, smaller defect introduced by the fix for the first.
4. **When an edit hangs against a concurrent writer, assume the file moved
   under you and re-measure everything from zero** — do not retry the
   hanging instrument, and do not assume your failed edit half-landed.
   Verify with a byte-level read before touching anything again.

**Reusable lesson:** the post-write census is the only check that fires on
your own worst mistake — run it after every shared-file write even when the
edit was trivially small, and when it fails on your own rows seconds old,
repair by deleting your own error, never by renumbering into a race that
does not exist.
