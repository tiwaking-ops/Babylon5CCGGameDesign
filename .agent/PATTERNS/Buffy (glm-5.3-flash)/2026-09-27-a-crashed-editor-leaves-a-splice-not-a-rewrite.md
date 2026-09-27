---
document:
  title: "A crashed editor leaves a splice, not a rewrite"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A crashed editor leaves a splice, not a rewrite

**Source task:** B5-0679 (second reap-and-complete of a crashed solar-pro4:free
session today). **Record:** 2026-09-27, Buffy (glm-5.3-flash).

## The lesson

The inherited `AIPlayer.java` looked catastrophic — "reached end of file while
parsing", a duplicate method, a duplicated offer block, brace depth −1 — but
every defect was one event viewed from four angles: an editor died mid-surgery
after pasting new content but before removing the replaced block and closing
the file. The completion was therefore a *splice*: restore the missing tail
byte-identically from `git show HEAD` at a verified comment-seam, remove only
what the diff proved duplicated (comparing semantics before choosing between
two versions), and let a brace-depth census confirm closure — never re-derive
the author's intent or re-type their code.

## The transferable rule

When you inherit a file that no longer parses from a crashed writer:

1. **Diff against HEAD first** — the insertion is usually one contiguous
   region; everything HEAD still has is reference material, not wreckage.
2. **Splice at a verified seam** — restore the missing tail from HEAD only
   where head and tail meet at a structural boundary (comment banner,
   method close), and check the junction before writing.
3. **Census, don't chase** — a brace-depth probe (`awk` counting
   `{`/`}`) finds class-level damage instantly; parse-error line numbers
   point at the symptom, never the wound.
4. **Deduplicate by comparison** — where the crash left two versions of a
   helper, compare semantics across *every call site* before keeping one;
   keep the committed copy unless the new one is strictly better, and say
   which you kept and why in the report.

## Anti-patterns this heads off

- Reverting the whole file to HEAD "to get green again" — that erases the
  owner's entire landed leg and hands their task back to square one.
- Hand-fixing parse errors top-down from the compiler's line numbers — the
  first error is downstream of the wound; the brace census finds the wound.
- Choosing between duplicate helpers by position rather than by semantics —
  the copy that compiles and the copy that is correct are not always the same.

**Filed alongside:** `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0679.md`
(and its sibling lesson, a reap is a handoff — 2026-09-27).
