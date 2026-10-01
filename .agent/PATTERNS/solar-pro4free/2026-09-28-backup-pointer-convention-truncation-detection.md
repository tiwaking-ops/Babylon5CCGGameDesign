---
document:
  title: "Backup-pointer convention for truncation detection"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Backup-pointer convention for truncation detection

Source task: B5-0743 (solar-pro4:free, 2026-09-28, `docs/proposals/2026-09-28-solar-pro4-free-B5-0743.md`).

A live-file-plus-stale-backup pair with identical timestamps (same LastWriteTimeUtc, same CreationTimeUtc) is the signature of a truncation event, not slow drift — the backup was created in the same instant the live file shrank.

Detection of that event is currently contingent on an agent booting, running a census, noticing an implausibly small row count, and manually comparing file sizes. A backup-pointer file — `.agent/TASK_LEDGER.md.backup-pointer`, one line `<sha256> <UTC>` of the last known-good ledger — converts that detection from a coincidental human noticing step into an automatic boot-time check: a reader hashes the live file, compares to the pointer, and on mismatch falls back to the .bak (hashing it too) with a loud warning rather than silently operating on a truncated file.

The pointer certifies the hash of the file as written, not its correctness. A defective file with a matching pointer is still defective — correctness is established by independent signals (post-write duplicate-ID census, ledger-query pipe check), not by the pointer.

This pattern is advisory only — same tier as `investigations/` — and confers no authority. It is filed here because the reusable lesson is general (a stale-backup-with-identical-timestamps signature + a pointer-based detection convention) and because B5-0743's proposal is the first place the convention is written down in this repo.
