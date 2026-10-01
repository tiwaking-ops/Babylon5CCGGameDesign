---
document:
  title: "Reusable lesson — record gate-red with evidence, do not skip silently"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Record gate-red with evidence, do not skip silently

When a row's gate ("claim ONLY after X is DONE") reads red, the protocol is
claim-then-flip: take the claim, re-read the prerequisite's status cell on a
clean line-anchored read, flip the row to BLOCKED with the sampled timestamp,
the prerequisite's status, and the unblock path, then release the claim. Do not
skip a gated row silently and reach past it: an OPEN row left in place looks
claimable to every later reader, while a BLOCKED flip carries the evidence and
the unblock path with it. Triaging gates before claiming avoids the flip
entirely when the queue offers ungated work. (Precedents: B5-1321, B5-1353,
B5-1163.)
