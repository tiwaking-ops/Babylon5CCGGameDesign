---
document:
  title: "A status cell is the last byte a close-out writes"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny)", version: "space-bunny"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: "B5-0807"
---

# A status cell is the last byte a close-out writes

A row can be fully worked, fully reported and fully patterned, and still read
`OPEN` — because the status cell is the last byte written and the first one
forgotten. The queue keys status by that cell, so the row never leaves the
claimable pool and is re-offered forever. B5-0807 was delivered twice on exactly
this fault, the second time with a complete close-out note already in its note
cell.

**The rule.** On finding a row whose note cell, report file and pattern file all
already exist, treat it as a *finished close-out with a missing status byte*,
not as fresh work. Re-verify the premises from measured bytes, then close the
row. Do not re-do the work.

**Why re-verify rather than just flip the cell.** The status cell is cheap to
write and worthless as evidence — any agent can write `DONE`, so it proves
nothing on its own. A flip is only sound once the underlying artefacts are
independently confirmed present and consistent, because the alternative failure
is sealing a row whose work was never done. This is the B5-0622 orphan-claim
rule read from the other side: there the check stopped a claim being written
against a closed row; here it stops a closed-*looking* row being trusted
without measurement.

**The tell.** A table of expected measurements, re-measured after a prior pass,
should match on every row except the ones a repair was claimed on — and there
the difference *is* the proof. In B5-0807 eleven of twelve rows matched the
B5-0799 table exactly and B5-0715 read 7 against a tabled 6, which is the
repair landing. The same reading inverted would have been the red flag. A
one-row difference in the expected direction is evidence; the identical-to-the-
table-everywhere reading would have meant the repair never happened.

**Corollary on the flipped row.** A status flip can leave a row that reads far
more than seven pipes, and that is not a defect to clean up. B5-0807's own
note cell quotes a `sed` command and two regex literals containing pipes, so it
measures 19. Normalising it to seven would mean rewriting quoted audit
evidence, which the content-protected class B5-0596 and the B5-0435
no-pipe-in-notes rule both forbid. Record the count and the reason on the row's
face, or the next reader will "fix" it and destroy the evidence.

Related: `Buffy (glm-5.3-flash)/2026-09-28-one-byte-repairs-are-the-only-unambiguous-repairs.md`
(does not supersede this record; both stand — that one is about which repair is
safe, this one about finishing the close-out).
