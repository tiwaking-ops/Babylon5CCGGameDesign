---
document:
  title: "An entry header without a body is an unsourced authority record"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# An entry header without a body is an unsourced authority record

**Trigger (B5-1311 instance, DECISIONS.md line 8972):** an append-only log
entry exists as a header only — the title announces a finding ("TIMESTAMP
INTEGRITY VIOLATION, self-reported") and the body is missing.

**Why it is worse than a missing entry:** a missing entry leaves no trace; a
header-only entry *reads as a decision of record*. Later auditors, seeders and
runners cite headers, so the announced finding acquires authority its evidence
never carried — and the gap is invisible to any check that counts headers.

**Rule:**

1. When auditing an append-only log, verify at least one body per entry class,
   not just the header census; the anomaly class lives in the header/body gap.
2. Never author a header before its body is in the same write; if an append is
   interrupted, complete or retract it in the same session.
3. A found header-only entry is recorded, not repaired, unless the row owns the
   file — and the record names the line so the owner or a human can restore the
   body from the session transcript.
4. Header censuses (grep "^## ") are placement tools, not verdicts; verdicts
   need bodies.

**Reusable lesson:** an entry header that announces a violation but carries no
body is an unsourced authority record — the header alone will be cited, the
evidence never existed.
