---
document:
  title: "B5-1027 close-out — ledger mojibake round-depth census"
  status: "Close-out report (observation, no authority)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
---

# B5-1027 — Round-depth census of the ledger's C1-mojibake class

Task: read-only analysis of `.agent/TASK_LEDGER.md` — measure the round depth
of every C1-marked line under the B5-0989 guarded inverse, prove reversibility
per round where reversal exists, and name the lines no algorithm can recover.

## Instrument (named, per the B5-1001 rule)

- File read as raw bytes; decoded `codecs.decode(raw, "utf-8")` strict (raises
  on invalid input). All counting on that decode.
- Marked line = contains at least one C1 control (U+0080–U+009F).
- Double-mojibake start marker: derived programmatically as the cp1252
  rendering of the UTF-8 bytes of `â€` (the single-mojibake start of any
  `E2 80 xx` sequence) = codepoints U+00C3 U+00A2 U+00E2 U+201A U+00AC.
- Guarded inverse (one round, per char): C1 → its raw byte; ASCII → itself;
  else → cp1252 strict encode; whole line then UTF-8 strict decode; non-no-op
  required; any round failure is terminal for that line.
- Script re-runnable from `.agent/tmp_b51027_round_depth.py` (read-only; no
  arguments, no writes).

## Baseline reproduction (instrument validated)

77 marked lines, 143 C1 marks — exact match to the seed measurement and the
B5-1001 measurement. Double-mojibake start markers measured 132 under the
derived literal (B5-1001 rendered it as ~150; the row's marker was quoted in
prose, and the derivation below is the settling evidence).

## Round-depth distribution (the deliverable)

| Depth | Lines | Share of 77 |
|---|---|---|
| 1 | 5 (lines 68, 111, 143, 148, 182) | 6.5% |
| 2 | 0 | 0% |
| 3 | 1 (line 80) | 1.3% |
| unrecoverable | 71 | 92.2% |

All 71 unrecoverable lines are stuck at round 0: their remaining C1 set is
**exactly {U+009D}** (135 residues total; per-line counts 1–6). 56 of 71 have
every residue directly after an ASCII `--`. 15 lines additionally carry
*recoverable* single-mojibake `¬â€\x9d` (right-double-quote) sequences that
the truncation bytes on the same line make unreachable to the guarded inverse.

## Reversal proof per round (6 recoverable lines)

For each round: the output string's UTF-8 bytes are byte-identical to the
deterministic per-char map of the input, so the decode is a strict bijection
with a unique inverse; B5-0989 asserts all pass (decodes, no residual C1,
non-no-op, ASCII payload identical). Traces (C1 before→after):
68: (1→0); 111: (1→0); 143: (1→0); 148: (1→0); 182: (1→0); 80: (3→3)→(3→1)→(1→0).

A first proof formulation (re-decoding the output's bytes through cp1252) was
itself wrong — it chokes on legitimate UTF-8 continuation bytes — and was
replaced by the byte-identity form. Recorded because a proof that cannot run
is not a proof.

## Why the 71 are unrecoverable (the distinction the row asked for)

Byte `9D` is valid in neither UTF-8 nor cp1252: the `E2 80` prefix of the
original right-double-quote was destroyed in an earlier pass. Information is
gone; no number of rounds restores it. These lines need a human-authored
replacement (or an explicitly authorised character decision), not a repair.
Named lines (71): 3, 39, 43, 47, 50, 52, 53, 54, 57, 58, 60–65, 70–75, 77, 81,
85, 87, 90, 94, 96, 97, 100, 104, 108, 112, 114, 124, 126, 133, 140–142, 144–146,
149–165, 167–169, 181, 193, 200, 202, 215, 223, 227.

## Algorithm proposal (the one permitted)

Two-phase, per line, gated on: 0 guarded failures; per-round byte-identity
reversal proofs; ASCII payload identical; zero churn on unmarked lines; no
character guessing ever.

- Phase 1: run the B5-0989 guarded inverse to its fixpoint on any line whose
  stuck-residual set is empty.
- Phase 2: halt on any line whose stuck-residual set is exactly {U+009D} and
  emit it to a named hand-repair list.

On the current file the algorithm terminates holding 71 lines for human
authorship — the correct fixpoint for this class. Practical order note: repair
the 6 recoverable lines first; lines 52 and 158 each mask one further
recoverable sequence behind their truncation bytes.

## Provenance and gates

- Claim `.agent/CLAIMS/B5-1027.json` taken 2026-09-29T08:12:00Z (measured
  clock), released at close-out.
- Row updated in place; `run-dup-census.ps1` exit 0 post-write; own row reads
  pipeCount 7 / doubleLead no via `ledger-query.ps1 -Status "DONE"` (also read
  byte-identical from disk). The `suppressed-live-claim` footer on that read
  was my own claim, released immediately after.
- No reap, no foreign claim/heartbeat/report/pattern touched, no commit, no push.

## Reusable lesson

An instrument that runs an inherited recipe must first prove it implements
that recipe: my first census run scored 0 of 77 reversible because the script
applied a whole-line cp1252 encode instead of the B5-0989 per-char guarded
inverse, and only the seed-baseline reproduction check (77/143) caught it —
reproduce the baseline before you publish a new number.
