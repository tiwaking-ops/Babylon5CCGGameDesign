---
document:
  title: "Pattern: a replica rule is a second rule, and a HEAD diff will lie to you"
  status: "Advisory (shared pattern store - never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  provenance_note: "Advisory only, per AGENTS.md section 6. Never canonical; citing confers no authority."
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
assessor_llm: []
---

# A replica rule is a second rule, and a HEAD diff will lie to you

**Observed:** B5-0775, 2026-09-28. `census-crosscheck.ps1` mirrors two other
census tools so it can diff them. Its `Get-Verdict-L` replica was missing the
report-mtime signal that the real `ledger-query.ps1` has used since B5-0660. The
tools therefore disagreed on exactly the tasks the third signal exists to protect
— and the cross-check reported a DIVERGENT that it had *invented itself*. A
cross-check's red is supposed to mean "go look at these two real tools"; when the
red is self-manufactured, every finding it prints costs someone an investigation
into a non-bug, and the habit of trusting it decays.

The second half is about the proof, and it is the half that is easy to get wrong.
My first before/after compared `git show HEAD:...` against the working tree, and
it went red — but for the *wrong reason*: the working tree already carried
uncommitted work from two other agents (B5-0658 and B5-0771), so that red
attributed their behaviour change to my fix. In a shared-file repo with a dirty
tree, **HEAD is not your baseline.** The honest control is to take the current
tree and remove exactly your own hunk, leaving every other uncommitted line in
place. Then the red is provably caused by the lines you wrote.

Related, from the same task: a check must be able to fail. The fix could have
silently made the cross-check agree with everything, which is indistinguishable
from a repair. So the fixture set needs a genuine divergence that must *still*
exit 1 — otherwise you have proven only that the tool agrees with itself.
