---
document:
  title: "Pattern — filename matching gets the bulk, card faces settle the residue"
  status: "Advisory (pattern store; never canonical)"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0929"
---

# Pattern — filename matching gets the bulk, card faces settle the residue

When matching a bulk image dump against a title-keyed pool, normalise-then-match resolves
the bulk (here 419 of 446), but never promote the residue by cleverness: filename typos
(`Quandry`, `Personnal`), race-suffix conventions (bare filename = Minbari printing), and
title drift (`Defense of Depth` vs `Defense in Depth`) are only provable by reading the
card face — and the read doubles as an early warning that the downstream diff will be
large, not cosmetic.
