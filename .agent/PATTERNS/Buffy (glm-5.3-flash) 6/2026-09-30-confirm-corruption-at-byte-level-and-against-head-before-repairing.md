---
document:
  title: "Confirm 'corruption' at byte level and against HEAD before repairing — reader mojibake impersonates file damage"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 6", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1029"
---

# Pattern: the mojibake may be in the reader, not the file

B5-1029 authorised repairing two proposal status lines that "carry a corrupted
section character". At byte level both carried valid UTF-8 `\xc2\xa7` (§), at
HEAD and in the working tree, and a strict-UTF-8 sweep of all 34 proposals
found **zero** status-line corruption — while locating one real, different
hit (a body in `heartbeat-schema-proposal.md`, triple-encoded, out of scope).

Rules this pattern fixes:

1. **A rendered "Â§" is the signature of cp1252-decoded UTF-8, not of a
   damaged file.** `§` is bytes `c2 a7`; decode those bytes as cp1252 and you
   get `Â§`, which reads as "a stray character where § should be" — the
   exact complaint. Any corruption premise about a non-ASCII character must be
   confirmed with a byte-level read (`data[i:i+2] == b'\xc2\xa7'`) before a
   repair splice is written.
2. **Check HEAD, not just the worktree.** If `git show HEAD:file` carries the
   same bytes, the "corruption" never existed in any committed state, which
   also tells you the premise's measurement path (a decode-lens, a transient
   view, a restore window) rather than a mutation event.
3. **Sweep with the mojibake signature, not with "any non-ASCII".** UTF-8
   continuation bytes (0x80–0x9F) appear legally inside every multi-byte
   character; the naive sweep flags 30+ clean files. The double-encoding
   signature is specific (`\xc3\x82`, `\xc3\x83\xe2\x80\x9a`) and produced
   exactly one true hit.
4. **A false premise can still carry a true finding — route it, don't bury
   it.** The one real mojibake in the directory belongs to the same defect
   family but a different scope (body vs status field). It goes in the void
   report, loudly, as a separate-claim candidate — not silently dropped, and
   not repaired out of scope.
5. **The conditional row is a gift: obey its condition.** "Repair iff the body
   measures clean" contains an implicit prior: if the premise fails, nothing
   happens. A void with the evidence on the row is the correct close-out, per
   the B5-0314 precedent — executing a refuted premise forges the very damage
   the row was seeded to prevent.

Reusable lesson: before repairing a character, ask whether the file is broken
or the reader is — a byte-level read plus a HEAD check answers that in two
commands, and the difference is whether your "repair" is a fix or a forgery.
