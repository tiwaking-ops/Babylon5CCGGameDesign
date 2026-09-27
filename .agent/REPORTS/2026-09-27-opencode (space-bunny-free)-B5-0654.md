---
document:
  title: "B5-0654 — B5-0388 authenticity migration: adoption record"
  status: "Report (observation and test results; no authority per AGENTS.md §3)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0654 — B5-0388 authenticity migration: adoption record

Human ruling 2026-09-27: **APPROVED** — the Premiere and Deluxe card pool is the
game's authored design layer and is not migrated to printed-card values.

Docs only. `docs/DECISIONS.md` and one proposal file. No card JSON, no `b5ccg/src/`,
no rulebook text, no commit.

## The distinction this task exists to keep clean

There were two separate things in the proposal directory, and conflating them would
have produced a false record:

- **B5-0388 the task** — already `DONE` since 2026-09-24. GPT-6 Codex wrote the
  proposal. Nothing was outstanding about the *work*.
- **B5-0388 the decision** — the proposal recommends a direction, and a proposal
  confers no authority until adopted. The direction had been open for three days,
  carried as "deferred pending a human goal decision" through four separate seeding
  passes.

So the ledger row was closed and the question was still unanswered. Recording the
ruling as "B5-0388 DONE" would have been a lie twice over; the accurate entry is that
the *task* was done and the *decision* has now been made.

## What I wrote, and what I deliberately did not

Into `docs/DECISIONS.md`: the ruling, the six operating rules as canonical, and — the
part that matters most — an explicit statement of what the ruling does **not** do.

It promotes no card values. Every stat, text and cost in the pool stays exactly as
authored. This is a statement about the present project goal, not a data migration.
Stating that explicitly is the difference between a decision and a change, and a
reader who skims a log entry titled "authenticity migration ADOPTED" could easily
conclude that cards were rewritten. They were not.

I did not rewrite the four historical seeding-pass notes that recorded B5-0388 as
deferred. A decision log records what was believed when it was believed; editing
those lines would erase the record that a question genuinely sat open for three days
across four independent agents, which is the useful part.

## No rework, and I checked rather than assumed

The obvious risk with a ruling like this is that it invalidates work already on disk.
I read the affected rows instead of reasoning about it:

- **B5-0385** rewrote Premiere Commercial Telepaths as an IP-safe paraphrase and
  fixed Zack Allen → Zack Allan. That is precisely the "isolated IP-safety fix" the
  adopted rule (5) describes, which explicitly says such fixes "do not imply approval
  for a wholesale migration".
- **B5-0396** did the same for the differently-worded Deluxe variant.
- The **cost-only backfill** is the "additive metadata work" that rule (3) keeps
  separate from stat and text migration.

The ruling ratifies work already on disk. B5-0385's row also noted the Deluxe variant
was "flagged for B5-0388's migration audit rather than edited speculatively" — that
audit is what this proposal was, it recommended against wholesale migration, and the
human agreed. The flag is answered, not left dangling.

## The one sentence I had to reconcile

The proposal's "Scope and status" section read "It remains a proposal and does not
itself promote any data values to canonical truth." Accurate on its own terms, but it
would have sat three lines above a Disposition section saying ADOPTED, and read as a
contradiction.

I rewrote that paragraph rather than leaving it, because a document that contradicts
itself three lines apart is worse than either version alone. The rewrite keeps the
original claim intact — the document still promotes no data values — and adds the
reason it remains true *because* of the ruling, not in spite of it.

The rest of the body is untouched. Original `author_llm` (GPT-6 Codex) preserved; my
pass recorded as an `assessor_llm` entry, since editing another's document is exactly
what that field is for.

## Evidence does not age into a myth

Reiterated in the DECISIONS entry so the decision cannot later be misread as a
judgment that the pool *is* faithful: 439/446 titles matched, **0 of 87** character
stat blocks matched, **0 of 439** pool texts were identical, 438/439 below 0.5
similarity. Those numbers are why wholesale adoption was rejected. Adopting the
current direction does not soften them, and the pool is to be described as an authored
adaptation.

## The reopening bar is untouched

The proposal's "conditions for revisiting printed fidelity" list stands unchanged. A
future migration still needs per-card source mapping with ambiguous matches left
unresolved rather than guessed, a reviewed affected-field list, an engine-hook
semantics map, a regression plan, a rights-safe text plan, a separately claimed data
task, and a green Java 6 build with the relevant suite. Adopting the present goal is
not a permanent refusal of printed fidelity — and saying so now, while the decision is
fresh, is what stops the deferral from being re-raised as if it had never been
answered.

## Verification

`b5ccg/compile.bat` green, `Build successful`, JDK 1.8.0_292 — recorded as a
tree-health reading, not as this task's gate, since no code is in scope. The DECISIONS
entry and the proposal disposition were re-read after writing to confirm the ruling, the
six rules, the not-does-not paragraph and the status line all agree with each other.
Ledger row: duplicate-ID census empty, `7` pipes, `doubleLead no`.

## Reusable lesson

Adopting a direction is not performing the work it describes, and a log entry that
does not say which one it is will be read as the other. When a decision is "we are not
doing the big migration", the most important sentence in the record is the one
enumerating what the ruling deliberately leaves unchanged — because a reader who
believes cards were rewritten will either distrust the record or go looking for the
diff. Ratify the work already on disk explicitly, too: a new ruling looks like it
invalidates prior art unless you say which prior art it blesses.

Filed as `.agent/PATTERNS/opencode (space-bunny-free)/2026-09-27-adopting-a-direction-is-not-performing-the-migration.md`.
