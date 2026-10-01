---
document:
  title: "A file no tool can open is found by what can"
  task: "B5-1006"
  date: "2026-09-29"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 4", version: "glm-5.3-flash"}
---

# A file no tool can open is found by what can

**One-line lesson:** a Windows reserved-name file is not an anomaly to delete
blind — it is a fossil of a broken command, and its content names the creator;
and the fix is proven by a full tree walk succeeding, never by the file's
mere absence.

## Shape of the case

`nul` at the repo root, 142 bytes. .NET refused even the `\\?\` device
namespace (`FileStream was asked to open a device that was not a file`), but
MSYS `cat` read it: three lines of `FINDSTR: Cannot open ...` stderr. Creator
identified — a `findstr` symbol census (the B5-0969 family) redirected to
`nul` from an MSYS shell, where cmd discards the stream but MSYS creates a
real file. The command was malformed before the redirect (the `I:/` argument),
so the redirect just preserved its stderr as a permanent, untrackable,
unopenable artifact.

Cross-tool visibility asymmetry is the diagnostic itself: `ls`/`cat`/`rm` via
MSYS see it; .NET and (therefore) any tooling built on it cannot. `git
ls-files --error-unmatch` and `git log` proved it was never tracked — the
plain class, not the worse tracked class.

## What worked

- Read before delete: the content was the only evidence of which command
  created it, exactly as the row predicted.
- Preserve the 142 bytes under `tmp-scans/` (satisfying reversibility) while
  deleting the hazard — an untrackable file cannot be reviewed or cleaned up,
  so it cannot be kept "in case."
- Prove the fix by running every tree-walking instrument to completion
  (dup census, full ledger walk, heartbeat validation, build) — the row's
  point is that a tool that could not see the file is the thing being fixed.
- No `.gitignore` line: hiding is advisory (B5-0430 tier), the file is gone,
  and the creator is documented in the ledger note instead.

## Related records

- B5-0969 (the findstr/ResolveConflict census family the fossil came from),
  B5-0430 (advisory tier of hiding mechanisms), B5-1007 (the sibling 9-byte
  zip stub, same wave).
