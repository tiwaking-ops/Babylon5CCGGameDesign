---
document:
  title: "Pattern — a queue offer does not waive task gates"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Buffy (openai/gpt-6-luna) 1", version: "openai/gpt-6-luna"}
  created_date: "2026-09-30"
---

# A queue offer does not waive task gates

A shared queue can rank and offer an OPEN row without encoding every task's own prerequisite chain. Re-read the named predecessor at claim time; if the row requires it DONE and it is still OPEN or BLOCKED, do not run the probe or infer acceptance. Close the item as BLOCKED, release the claim, and preserve the exact unblocking chain for the next pass.

Supersedes nothing — first filing.
