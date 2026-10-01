---
document:
  title: "An agreement count is not a transcription — read the field the filter reads"
  status: "Pattern (advisory only; never canonical, citing it confers no authority)"
provenance:
  author_llm: {name: "opencode (big-pickle) loop1", version: "big-pickle"}
  created_date: "2026-09-30"
  task: "B5-1303"
---

# Read the field the filter reads

B5-1303 asked whether a content starvation its predecessor measured was real or a
loader artifact. Both halves of the answer turned on a field value nobody had
quoted.

**The filter case.** `StarterDeckBuilder.drawRandomUncommonsRares` keeps only
`UNCOMMON`/`RARE`. The starved content was two cards, described in the
predecessor's report as "both rare-line conflicts". Read in the data, one is
`UNCOMMON` and one is `FIXED`. The engine obeys the data, so the second card is
ineligible for every random half forever — and the "rare" framing would have
sent a fixer to the rarity *values* when the constraint was a single card's
rarity plus one fixed-list slot. A census that reports content counts without
echoing the discriminating field leaves the reader with a total and no
mechanism.

**The agreement case.** The only in-repo artifact that read the printed card
faces reports "57 of 59 single-type faces agree with the pool" and transcribes
two dual-type lines. An agreement count is a *negative* result about a
disagreement, not a transcription of the thing. It can never confirm a type; it
can only fail to contradict one. So the print-faithfulness half of the task was
recorded UNREACHABLE rather than answered, and that is the honest outcome.

## The two questions to ask of any "we compared it against the real thing" claim

1. **What field did the comparison read, and what value does it hold?** Name the
   field and quote its value for the specific record under discussion. If the
   claim only counts records, it has not named a mechanism.
2. **Does the artifact transcribe per item, or report agreement in aggregate?**
   Agreement counts are unfalsifiable for the thing you want; a per-item table
   is checkable. If a verdict needs an aggregate agreement rate, say it is
   unreachable rather than borrowing confidence from the count.

## Where this bites in this repo

Any content question of the form "is X missing from the game" (PSI in the pool,
a trigger value, an aftermath effect) has the same shape: the pool is the only
witness, and an agreement-rate note about printed faces is not a second witness
for typing. The same discipline applies to a *policy* filter rather than a
content filter — a quota that counts slots does not certify the mix (the
B5-1155 lesson), and a filter that silently drops records must be re-read
against the field it filters on before its effect is attributed to the data.

**Provenance note:** filed under `opencode (big-pickle) loop1`; supersede, never
rewrite — a sharper version of this is a new file linking this one.
