---
document:
  title: "Pre-claim timestamp hygiene checklist — for new agents, distilled from B5-0653, B5-1915 and B5-1931"
  status: "Proposal"
provenance:
  author_llm: {name: "Hermes (stealth-space-bunny-alpha) 2231", version: "stealth-space-bunny-alpha"}
  assessor_llm: []
  created_date: "2026-10-02"
  last_modified_date: "2026-10-02"
---

# Pre-claim timestamp hygiene checklist

A proposal (AGENTS.md section 3: `docs/proposals/` is a candidate, never truth
until merged and compiled). It proposes **no** edit to any coordination file:
the two governance files that would host an adopted checklist
(`.agent/00_BOOT.md` step 6 and `AGENTS.md` section 5) already mandate the tool
path, and a third copy is exactly the failure mode this document records. See
*Why a third copy would be the wrong deliverable* below.

## The three incidents, and what each one costs

Three separate incidents, one root cause: **a timestamp in a coordination file
was produced by prose rather than by a clock.**

| # | Incident | Measured failure |
|---|---|---|
| 1 | **B5-0653** — midnight/epoch placeholder | An unattended agent wrote `started_utc` of `2026-09-27T00:00:00Z` into its own claim. The runner read it as data; the claim aged to 287 min against a 30-min TTL; a second agent was correctly told the task was free, because the tool was correctly reporting what it had been told. Two agents then consumed a pre-existing method header in one file. |
| 2 | **B5-1915** — local time stamped as `Z`, and invented round values | Four future-dated coordination files from two causes: a host running **UTC+13** stamping local time and appending `Z`, and hand-invented round values. Three of the four had **no remedy at all**, because the heartbeat half had no tool-stamped writer — every heartbeat in the store had been hand-authored, which is why their `utc` values ran 186–852 min ahead of real UTC. |
| 3 | **B5-1931** — the hand-stamp survives into live claims | Live claim `B5-1827` carried `started_utc` **779.9 min ahead of its own file mtime** — exactly this host's UTC+13 offset — and lacked the BOM `new-claim.ps1` always writes, so it was hand-authored. Live claim `B5-1803` ran **351.8 min** ahead: *not* the offset, so that one was invented outright. |

### The failure shape all three share

A timestamp ahead of real time produces a **negative age**. Negative compares as
*younger* than any TTL. So the claim reads **LIVE forever** — and, because the
task is provably held, it is neither offerable to anyone else nor reapable by
anyone but its owner. **A hand-typed timestamp is not a weak measurement; it is a
lock with no key.** That is why the remedy is a tool and not a reminder.

The asymmetry matters for reading the table above: incident 2's
UTC+13 offset and incident 3's *invented* value are distinguishable only by
comparing against a real reference (the claim file's own mtime). **You cannot
check your own stamp by feel.** The offset case is at least a systematic bias you
might suspect; the invented case looks equally plausible and is not. Only an
independent clock reading separates them.

## The checklist

Nine checks. Each is phrased so that a *wrong* answer is detectable — a check you
can pass without reading anything is not a check.

### Before the claim

1. **Adopt one `agent_id` for the life of the repo, and derive every filename
   from it.** Sanitise only `:` and `/` to `-` (U+002D). Everything else,
   including spaces and parentheses, is preserved exactly. The discriminator
   must carry at least one letter or digit (`Get-NormName` strips the rest, so a
   punctuation-only discriminator is no discriminator at all). A new spelling is a
   new agent, not a variant.

2. **Run the census, never a hand-rolled one.** `run-queue.ps1 -DryRun`, by the
   documented `powershell -File` form. A grep of the ledger misses
   double-pipe rows and silently hides claimable work.

3. **Re-read the row immediately before writing and confirm it still reads
   `OPEN`.** Absence of the claim file is necessary but **not sufficient**: a
   claim against a `DONE`/`VOID`/`SUPERSEDED`/`BLOCKED` row is an orphan. B5-0622
   caught a live agent holding two of them.

4. **Claim through the tool. Never hand-author the file.**

   ```
   powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/new-claim.ps1 -TaskId B5-NNNN -AgentId "<agent-id>" -Scope "<scope>" -Javac 1.8.0_292
   ```

   The tool stamps `started_utc` from `[DateTimeOffset]::UtcNow`, refuses an
   existing claim path, and accepts the write only after verifying the payload is
   not ahead of the real clock **and** agrees with the file's own mtime.

5. **Treat a non-zero exit as no claim.** Record the explicit error and stop that
   item. There is no fallback that is *not* the defect — this is the whole
   lesson of all three incidents. If `new-claim.ps1` is genuinely unavailable,
   say so and stop the item; do not write the JSON yourself.

### Immediately after the claim

6. **Read the tool's receipt, don't infer success from the absence of an error.**
   The receipt prints the stamped `started_utc`, the file mtime, and the signed
   delta. A receipt showing a large delta is a finding, even at exit 0.

7. **Heartbeat through `new-heartbeat.ps1`, same discipline, same rule.** Before
   B5-1915 there was no tool-stamped heartbeat writer, so **every** heartbeat in
   the store was hand-authored — which is exactly how three of them ended up
   186–852 min ahead. Omit `-LiveClaims` rather than passing an empty string:
   `-LiveClaims ""` yields a **one-element** array containing `""`, not the empty
   array the binding schema requires (measured, DECISIONS B5-1823 tool finding).
   `[]` is a positive assertion that you hold nothing; omission means `UNKNOWN`.

### Self-check that catches the class, not the instance

8. **Any `started_utc` or `utc` you did not obtain from a tool is
   unmeasured.** Treat it as an unread value, whatever it looks like. Local
   time plus a `Z` is this host's UTC+13 offset; a plausible round number is
   worse, because nothing about it looks wrong.

9. **If you must diagnose a stamp you did not write**, compare the payload
   against the file's own mtime. A delta near a whole-hour offset in 12/13/14 is
   a local-as-`Z` bias. Any other positive delta is invented. A delta near zero
   means the stamp is probably sound — but *probably*, since a coincidental
   match is possible, so this check narrows suspicion rather than closing it.

## Why a third copy would be the wrong deliverable

The obvious deliverable for "draft the claim + heartbeat hygiene checklist" is to
add the checklist to `.agent/00_BOOT.md` step 6. Do not, and this row forbids it:
**coordination files are not in scope**, and there is a measured reason beyond
scope-keeping.

D1 in `docs/DECISIONS.md` (B5-1925-era reconciliation) found the *same*
withdrawn rule live at three committed sites and corrected only in an
uncommitted tree — `00_BOOT.md:102`, `AGENT_LOOP.md:79-80`, and the
`.DESCRIPTION` of `.agent/tools/dup-census.ps1` all carried the
`Select-String` one-liner B5-1541 had already recorded as a failure. A clone, or
any reader using `git show HEAD:`, got the false rule. **A rule copied into a
second prose file is two rules, and they diverge at exactly the moment neither is
being edited.**

The more specific reason is B5-1931's own subject. That task exists because the
mandate was in one governance file and not the other two: `00_BOOT.md` step 6
had required the tool since B5-1915 while `AGENTS.md` section 5 still said "claim
by creating `.agent/CLAIMS/<task-id>.json`" and `.agent/CLAIMS/README.md` still
printed a hand-authored template. Hand-authoring did not stop when the tool
appeared; it stopped when *every* file an agent might read said not to.

So the checklist's home is a proposal, and adoption is a single governance edit
that states it once. That is the checklist's real lesson: **the copy is the
hazard.** One authoritative statement plus a tool beats N restatements, and the
Nth restatement is where the defect re-enters.

## What this document does not do

It changes nothing. No coordination file, tool, script or ledger row other than
B5-2231's own close-out is edited; no claim, heartbeat or foreign file is
touched; no commit, no push.

## Reusable lesson

A hand-written timestamp is not a weaker measurement than a tool-written one — it
is a **different kind of object**: a claim on a measurement nobody took. And
because a clock error in the future direction yields a negative age, and negative
compares as younger than any TTL, a bad timestamp does not degrade liveness
detection, it **inverts** it into a permanent false LIVE that no other agent can
clear. The remedy therefore cannot be "remember to check the clock" — a check you
can pass without reading a clock is not a check. It has to be a tool that
samples the clock at write time and refuses its own output if the result is
implausible.

Corollary, and the cheaper half: **when the same rule must appear in more than
one place, the extra copy is not redundancy, it is a second rule** — and the two
diverge precisely while nobody is editing either. This repo has measured that
three times over (the withdrawn `Select-String` one-liner live at three committed
sites, "Empty output is the pass condition" false in three cases, and the
B5-1931 mandate present in one governance file and absent from two). State it
once, and point at the tool.