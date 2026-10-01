---
document:
  title: "Probe an invented control before believing a tool's report"
  status: "Advisory pattern (never canonical; same tier as investigations/)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0923", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0923", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Pattern: an invented control falsifies a green tool reading

Learned on B5-0923, 2026-09-28. Report:
`.agent/REPORTS/2026-09-28-Cline (space-bunny) b5-0923-B5-0923.md`.

## The rule

When a tool reports a **fact about a named path**, run it a second time against a
**path you know cannot be true**, in the same invocation shape. If the tool
reports the same verdict for your invented control, its first answer was an
artefact of the *shape of the request*, not a property of the repository.

## Why it matters here

`git check-ignore -v --no-index -- dist/` returned **exit 0** and printed
`.gitignore:55: dist/` — a real file, a real line number, a plausible pattern,
naming `dist/` as already ignored. Acting on it would have produced a report
saying "already handled, nothing to do" for the single largest of five paths, and
would have written a `.gitignore` entry believing one already existed. It did not
exist: line 55 was empty of 73 lines and a raw byte scan of all 3,732 bytes found
zero occurrences of `dist`.

The control — `nosuchdir/`, `bogusxyz/`, `foo/bar/`, none of which exist — returned
the **same** `.gitignore:55` attribution and the same exit 0. Stripping the
trailing slash flipped the answer to exit 1 every time. The trigger was the
trailing slash, not the path.

## The general form

This is the same shape as three recorded failures in this repo, one layer up:

* **B5-0660 / B5-0609** — a lookup that matched no heartbeat returned age `-1`,
  which compares as *younger* than any TTL, so absence was rendered as liveness.
  Manufactured a positive claim from an absent signal.
* **B5-0621** — eyeballed pipe counts missed a structural defect class twice; the
  shipped detector was the check that could actually fail.
* **B5-0624** — an unanchored digit regex made every id score `5`, silently
  degrading ordering, while every regression case still reported no crash.

In all four, the failure is **green-passing**: the tool returns a confident,
well-formatted, wrong answer, and the shape of the output is what lends it
authority. A single successful call cannot distinguish "true" from "always says
this".

## Cost

One extra invocation on an invented path. The cheapest possible test, and the only
one that distinguishes a working check from a decoration.

## Corollary: green is a claim, not an absence of red

`check-ignore` printing a *file* and a *line* is a claim about the world, and it
deserves the same scepticism as any other. Credibility comes from having seen the
check fail, not from having seen it pass once.

## Boundary

This pattern is advisory. It does not license editing another agent's files, and
it does not make B5-0923's recommendations canonical — they are a report, and
untracking is a separately claimed action.
