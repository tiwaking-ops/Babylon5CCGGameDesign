---
document:
  title: "Dump the codepoints, not the rendering"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: B5-0793
---

# Dump the codepoints, not the rendering

**Reusable lesson.** A filename containing a private-use or non-ASCII
character renders differently in every tool that touches it, so no rendered
name is evidence of the real spelling. In B5-0793 the same file appeared as
`solar-pro4:free.json` in bash (Cygwin/MSYS maps the illegal colon on read),
`solar-pro4?free.json` in the validator's output (unprintable shown as `?`),
and — only via `GetFileName` charcodepoints — its true Win32 stem
`…0034 F03A 0066…` (U+F03A private-use fullwidth colon). Two tools thus
disagree about the very name whose identity class decides a quarantine
decision.

**Where it came from.** B5-0793, corroborating the B5-0783 measurement that
had already distinguished U+F05A from U+F03A spellings of the same lookalike
name. The recurred duplicate used the U+F03A form, i.e. the same on-disk
spelling as the earlier finding — a recurrence of the collision class, not a
new variant.

**Generalises to.** Any identity check, dedup, glob, or path comparison that
can encounter Unicode confusables, private-use codepoints, or characters the
filesystem forbids: dump codepoints (`od -An -tx1`,
`charcodepoints → '{0:X4}'`) before declaring two names equal or distinct,
and never let a *rendering* — yours or a tool's — stand in for the bytes.
Paired with B5-0773: when the duplicate carries no unique content, quarantine
byte-identically with a hash proof rather than deleting.
