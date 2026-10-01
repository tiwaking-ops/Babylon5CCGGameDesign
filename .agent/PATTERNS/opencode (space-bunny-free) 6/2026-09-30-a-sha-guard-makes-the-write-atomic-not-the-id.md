---
author_llm: opencode (space-bunny-free) 6
version: space-bunny-free
utc: "2026-09-30T06:05:00Z"
supersedes: null
---

# A SHA guard makes the write atomic, not the ID

**Reusable lesson.** Before appending rows to a shared ledger, a SHA-256
pre-check on the file plus a post-write duplicate-ID census look like two halves
of one safety mechanism. They are not, and the gap between them is exactly where
the collision happens.

- The **SHA guard** answers: *did the file change under me?* It makes the write
  safe against a concurrent append landing in the read-modify-write window.
- The **ID census** answers: *is this identifier mine?* Nothing before the write
  can answer that, because two agents can both measure the same ID free inside
  the same window. The thing being raced is another *reader*, not a stale file.

So a pre-write "is this ID free?" check is **necessary and not sufficient**, and
the post-write census is not a formality you run when you feel like it — it is
the only instrument in the sequence that can detect the failure the pre-write
check is structurally unable to.

**What to do when it fires.** Diverge to a **non-adjacent** ID and leave the other
writer's row byte-identical, status untouched. Do **not** renumber into the slot
they just vacated — they will usually move there too, and the two of you
deadlock. Do **not** edit or delete their row, and do **not** flip their status
"to be safe": an unedited row is a live offer, whereas a status you changed on
someone else's row is an orphan they cannot complete and did not create.

**The tell that you got this right** is not a clean census alone. It is that the
note you leave behind says *N* rows where you planned *M*, and explains the
difference by measurement. A wave note that quietly matches the plan while the
row count does not is worse than no note, because it launders a collision into
an apparent success.

**Corollary — do not trust your own ad-hoc census over the shipped one.** In the
same pass, a hand-rolled pipe recount read 571 defective rows where the shipped
detector read 11 reportable and 6 content-exempt: my recount was counting the
detector's own output columns. The expensive-sounding ad-hoc check was wrong and
the boring shipped tool was right. Reach for the repo's instrument first, and
only hand-roll when it genuinely cannot answer the question — and when you do,
say which instrument produced every number you publish.
