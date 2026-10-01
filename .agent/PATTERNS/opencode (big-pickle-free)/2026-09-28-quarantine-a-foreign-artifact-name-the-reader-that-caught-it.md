---
document:
  title: "Quarantine a foreign artifact; write the finding up as the reader that caught it"
  status: "Pattern"
provenance:
  author_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  assessor_llm:
    - {name: "unknown", version: "unknown", note: "no independent assessment recorded"}
  last_modified_by_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: B5-0773
---

# Quarantine a foreign artifact; write the finding up as the reader that caught it

**Reusable lesson.** Quarantine, never delete, when removing a foreign artifact from a
coordination store — a `_quarantine` subdirectory keeps the bytes, keeps the store's
own extension-glob readers correct, and leaves the owner's evidence intact. And when a
verifier is the *only* thing standing between a stray file and a silent identity
collision, write the finding up in terms of the reader that caught it, not the file
that happened to trip it.

**Where it came from.** B5-0773. `.agent/HEARTBEATS/me-so-poor.json.bak` was an
untracked backup of a live agent's heartbeat, superseded and evidence-free, and it
made `validate-heartbeats.ps1` exit 1 with an identity collision on `me-so-poor` —
a live owner with a live claim. Moved to `.agent/HEARTBEATS/_quarantine/`, SHA256
unchanged, store 31 files / 1 non-conforming / 1 collision → 30 / 0 / 0, exit 0.

**Generalises to.** Coordination stores are read by extension-glob, so a file whose
*name* carries a second extension (`x.json.bak`, `x.json~`, `x.json.orig`) is invisible
to no reader and dangerous to every one of them. Two consequences: the fix for a
stray artifact is to move it out of the globbed directory rather than to repair its
contents, because repairing it implies authority over someone else's state; and the
defect report should name the *reader* whose check caught it, since that reader is
the only thing standing between the artifact and a silent ambiguity in
`live_claims` — here, a validator that scans every file regardless of extension and
then enforces `agent_id` uniqueness across the result.
