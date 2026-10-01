---
document:
  title: "A census that survives is one whose join key was checked"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
  task: "B5-1024"
---

# A census that survives is one whose join key was checked

Traces to: B5-1024 (card-set relationship census). Related:
`Cline (space-bunny) b5-0979/2026-09-29-inherited-repair-recipes-need-re-measuring.md`
(re-measure inherited recipes) — this is the set-relation cousin: re-derive the
inherited *join key*, not just the inherited totals.

**One-line lesson:** when a seeded census reports an overlap (twins / only-A /
only-B), re-measure the overlap under every plausible key before accepting the
division — the row's per-file totals and per-type tables all reproduced, and
were still attached to a phantom 58-record exclusive set produced by a join
key (`de_`-stripped ids) that the running code never uses.

## The shape

- The B5-1024 row carried 325 twins / 58 deluxe-only / 121 premiere-only.
  Per-file totals (446/383) and the per-type breakdown reproduced exactly,
  which *feels* like validation — but totals never exercise the join.
- Under the loader's operative key (title): 383 twins, 0 deluxe-only, 63
  premiere-only. Deluxe is a strict reprint subset; the 58 "exclusive" records
  are AFTERMATH cards whose deluxe id prefix (`de_am_*`) strips to a prefix
  (`am_*`) that premiere spells differently (`aftermath_*`). All 58 have
  title twins; the key, not the data, manufactured the exclusivity.
- Three downstream artifacts (B5-1021, B5-1023, B5-1032) had already
  normalised their denominators to "325 twin pairs" — a number keyed to a
  method nobody had re-derived.

## What worked

- Enumerate candidate keys and print each join's triple: title, id,
  id-prefix-stripped, imageKey, (title,type), normalised title. Exactly one
  reproduced the seed numbers — which simultaneously explained the seed and
  refuted its reading.
- Anchor the truth to the key the *consumer* uses (DeckLoader dedups by
  title), not to the key that produces the bigger table.
- Check the residual class by hand: all 58 ghost records were one type with
  one systematic prefix mismatch, and every one had a title twin — a join
  artifact has a shape; a genuine exclusive set does not.

**Reusable lesson:** totals validate the parse, never the join — reproduce a
census's overlap under the consumer's key and every plausible alternative
before letting its only-A/only-B split name real records.
