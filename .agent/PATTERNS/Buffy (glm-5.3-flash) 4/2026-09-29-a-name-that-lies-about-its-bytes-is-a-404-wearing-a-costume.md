---
document:
  title: "A name that lies about its bytes is a 404 wearing a costume"
  task: "B5-1007"
  date: "2026-09-29"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 4", version: "glm-5.3-flash"}
---

# A name that lies about its bytes is a 404 wearing a costume

**One-line lesson:** when a file's name asserts a format or an artefact,
verify the bytes against the name (magic header, size floor) before trusting
either — a stub saved under a real artefact's name is worse than a missing
file, because tooling branches on filenames and will be wrong.

## Shape of the case

`tesseract-ocr-w64-5.3.3.zip`, 9 bytes. `od -c` read `Not Found` — an HTTP
404 body saved by a fetch tool that wrote the error page to the output path
instead of failing. No `PK` magic. The disposition followed the row's
taxonomy: state 3 (lying stub), not a truncated download or placeholder;
`grep -rni tesseract` over ledger and docs found no consumer, so no follow-up
row was seeded; `git ls-files --error-unmatch` proved it never tracked; the
stub was deleted rather than renamed, because a renamed stub still advertises
an artefact it does not contain.

## What worked

- Read the bytes before choosing a branch: magic-header check is one command
  and converts three hypotheses into one.
- Check for consumers before treating the artefact as needed — the absence of
  any reference outside the hygiene row is what licensed deletion instead of
  completion of the download.
- Respect the governance boundary the row drew: a large binary landing in the
  repo is its own decision, never a side effect of a cleanup, so no download
  and no follow-up seed were performed here.

## Related records

- B5-1006 (the `nul` fossil — same wave, same lesson family: read the bytes,
  name the creator, prove the tree walk after).
