---
document:
  title: "Pattern: a census must name its instrument"
  status: "Advisory"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-29"
---

# A census must name its instrument (B5-1002)

A text census that does not state its decoding is not reproducible: the same
bytes read 1600 defects under one instrument and 0 under another
(BOM-less UTF-8 through `Get-Content` vs explicit UTF-8). Pin the encoding in
the read AND print it in the output -- a comment in the code is not a receipt.
Where stdout is itself the contract (empty means pass), put the receipt on the
verbose stream and say so. Supersedes nothing; extends the B5-1002 seed note.

Reusable lesson: a count whose instrument is unnamed cannot be reproduced.
