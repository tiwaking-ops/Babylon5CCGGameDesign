---
document:
  title: "A file mtime shared by ninety files in the store is a checkout stamp, not evidence about any one of them"
  status: "Advisory (patterns store — same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "opencode (big-pickle) rel1008", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (big-pickle) rel1008", version: "big-pickle"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
---

# A shared mtime is a checkout stamp, not a fact about a file

**Reusable lesson.** Before you use a file's modification time as evidence about
*that file*, measure how many other files in the same store share the exact same
mtime to the millisecond. If most of them do, the mtime records when the tree was
materialised on disk, not when the file's content was written, and it carries no
information about the individual file at all.

## What happened

Releasing the `B5-1008` claim, I reached first for the claim file's mtime:
`2026-09-29T08:44:38Z`, which against a `09:31Z` wall clock looks like a clean
38-minute-old write and reads as strong corroboration that the claim was live
recently.

It was wrong. The check that killed it:

```
90 of 94 files in .agent/HEARTBEATS share mtime 2026-09-29T08:44:38Z to the millisecond
 2 live claims written minutes apart have genuinely distinct mtimes
```

So `08:44:38Z` is a bulk materialisation stamp on this tree. It tells you when the
checkout appeared; it says nothing about when anything was claimed. The two live
claims are the control: real writes, distinct times, both recent.

## Why an mtime like this is the dangerous kind of wrong

A missing measurement is honest — you notice you do not have it. A *plausible*
measurement is not. `08:44:38Z` parses, it is recent, it is roughly a session
length, and it agrees with the story I was already telling. It needed no
counter-evidence to be believed, which is exactly what makes it expensive. The
positive control (two claims with real distinct mtimes) is what distinguished
"the store shares a stamp" from "this file is genuinely recent", and it took one
command to run.

## The rule

1. **Sample the clock in the same command block as the write.** Use
   `[DateTimeOffset]::UtcNow`; `Get-Date` returns local time, and on this host that
   is UTC+13. A timestamp captured in an earlier command block is a different
   clock reading wearing the same clothes.
2. **Never infer liveness from a file mtime without a control.** Count how many
   siblings share the value. A value shared by ~95% of a directory is a property
   of the directory.
3. **Prefer payload over metadata where both exist.** The heartbeat's own `utc`
   field was real, past, and 56.8 minutes stale — the opposite verdict from what a
   forged claim field says. Payload beat mtime here in both directions.
4. **Record the correction, do not quietly drop the wrong reading.** The
   `B5-1008` ledger note and the `DECISIONS.md` entry both name the bad first
   reading explicitly, because a future agent will hit the same mtime and needs
   to know it is already been checked.

**Supersedes / relates to:** `.agent/PATTERNS/Buffy (glm-5.3-flash) 2/2026-09-28-measure-the-clock-at-the-write.md`
— that pattern names the clock half of the fix; this one covers the *metadata*
half, where a real timestamp on disk silently describes a different event than the
one you are investigating.

---

# Second lesson, same session: "not offered" is not evidence about the row

**Reusable lesson.** When a runner declines to hand out a row, do not read the
non-offer as a property of the row. Enumerate every filter between "the row is
open" and "the offer was printed", and check the one that fired. The filters are
where the answer is, and the visible symptom is the same for all of them.

## What happened

After releasing the `B5-1008` claim, the dry run emitted **zero** `B5-0952`
warnings — the fix clearly worked — but `B5-1008` was still absent from the 12
rows offered. My first instinct was to write "offers B5-1008 again" and stop.
That would have been a false receipt: the warning's absence is a statement about
the *warning*, not about the *offer*.

The real blocker was `Test-OfferMarkerAvailable` (run-queue.ps1:887). A per-task
`%TEMP%\run-queue-offers-*\B5-1008.offer` marker, written 07:45:59Z by `powershell`
**pid 7216, still alive 120 minutes later**, made line 908 return `false`. The row
was claimable the whole time; a stale lock was eating the offer.

## The rule

1. **A suppressed offer and a defective row produce the same output.** Enumerate
   the filters — status, implausible-claim scan, census suppression, live-claim
   test, prereq gate, offer-marker liveness — and identify which one fired. Six
   filters, one output line.
2. **Find a positive proof that does not go through the broken path.** The
   `B5-1008.offer` marker's *existence* is the proof: markers are only created by
   `Select-OfferRow` at line 930, which runs after `Get-ClaimableOpenTasks` has
   already filtered the row. Artefact of the mechanism, not of the symptom.
3. **Check a negative result before writing a positive claim.** I had a real
   result (warning gone) and an unrelated symptom (row absent) and nearly merged
   them into one sentence that was half true.
4. **Correct in place, appending.** Both the ledger note and the `DECISIONS.md`
   entry carry an explicit correction rather than being rewritten — a reader who
   saw the first version must be able to see it was wrong and how.

## The underlying defect, left for a human

A marker whose owner is a long-lived shell that no longer intends the offer
suppresses a claimable row **indefinitely**. The PID-liveness test is correct for
a short lane process and wrong for a long-lived one: a lane that is merely *idle*
is indistinguishable from a lane that is *gone*. The same shape as the
`me-so-poor` claim — a signal that reads healthy and is not. Not fixed here; no
tool edited, no row edited.

