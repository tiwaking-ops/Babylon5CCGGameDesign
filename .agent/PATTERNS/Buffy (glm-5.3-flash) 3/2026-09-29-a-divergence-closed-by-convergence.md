---
document:
  title: "A divergence is closed by convergence, not by verdicts"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 3", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  task: "B5-0953"
---

# A divergence is closed by convergence, not by verdicts

Traces to: B5-0953 (reconcile the census-crosscheck divergence on B5-0481).

Two shipped tools disagreed about the same claim and the temptation is to pick
a winner and patch each symptom until the diff goes quiet. What actually closes
the class is converging the wrong copy onto the right one — same guard order,
same marker strings, same verdict shapes — so the crosscheck that diffs the two
copies becomes a *proof of agreement* instead of a permanent referee. The
marker strings matter more than they look: `suppressed(no-heartbeat)` versus a
paraphrase like `suppressed(missing heartbeat)` reads as a divergence to the
very instrument that is supposed to certify the repair, and the repair then
never reads green.

Two secondary findings rode along. First, a "deliberate divergence" comment is
a decision recorded in code, and it rots silently: the B5-0649 claim-age
fallback note in run-queue.ps1 and the "run-queue is still TWO-signal" note in
ledger-query.ps1 both described a state that ended the moment this repair
landed, and a reader who trusts the note over the behaviour inherits a defect
that no longer exists. When you change one side of a documented disagreement,
change the documentation in the same pass. Second, a harness that paraphrases
the function it verifies is verifying a different function — extract the real
text and run that.

**Reusable lesson:** repair the copy that is wrong by converging it on the copy
that is right, marker strings and guard order included, so the shipped
crosscheck turns from referee into proof; and retire the divergence's
documentation in the same edit that retires the divergence.
