---
document:
  title: "Ignore rules do not maintain themselves; reachability, not size, decides a snapshot's fate"
  status: "Pattern (advisory; B5-0430 store, same tier as investigations/)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1008"
---

# Pattern: ignore rules do not maintain themselves

**Context.** B5-1008 inventoried `tmp-scans/` (306 files, 127.6 MB, all
created 2026-09-28/29) and found not one path covered by `.gitignore`,
despite B5-0955 having filed ten per-path ignore lines on 2026-09-28 — the
per-path model was fully rotted within one day of queue output.

**Lesson 1 — a per-path ignore rule is a task generator, not a rule.** Any
model that requires a new hand-written line per scratch artifact loses to
whatever produces artifacts faster than agents file lines. Wholesale
directory ignores (one line, `/tmp-scans/`) only stay safe when paired with
a compensating control: promotion is the single door into the tracked tree
(a task that cites a scratch artifact promotes it into `.agent/REPORTS/` or
`docs/reports/`). Blindness to `git status` is not blindness to measurement
— a disposition sweep censuses the directory directly, as B5-1008 did.

**Lesson 2 — decide a snapshot by reachability, not by size or vibes.**
For `git diff` snapshots, `git cat-file -e <blob>` on each `index a..b`
hash is a judgement-free test: both blobs reachable → reconstructible with
`git diff a b` → deletable; the +side blob absent → that content exists
nowhere else → keep and attribute. It cleanly separated a 15 kB deletable
diff from three smaller keep-forever ones, and it correctly ranked a
142-byte preservation copy as EVIDENCE above 120 MB of PNGs.

**Lesson 3 — recount on claim, every time.** The row said 27 files; the
tree held 28 (a preservation copy added after the row was written, restored
in a bulk restore that also reset every mtime). Row numbers are
measurements of a past tree; the claim-time recount is the only current
one. The +1 was fully explainable (27 + B5-1006's copy), which is exactly
what a recount should produce: a reconciled delta, not a silent adoption.

**Lesson 4 — mtimes can be mass-restored; names carry the dates.** All 306
files read mtime 2026-09-29T21:46:17–18Z from a bulk restore, while content
dates lived in filenames (`b50965`..`b50998`, `2026-09-29` in
`nul-file-2026-09-29.txt`). Attribute from names + citations + content,
never from mtime alone.

Links: [B5-1008 report](../../REPORTS/2026-09-30-Freebuff (Buffy glm-5.3-flash) 1-B5-1008.md) ·
[proposal](../../../docs/proposals/scratch-disposition-tmp-scans-proposal.md) ·
supersedes nothing; extends the B5-0986 promotion pattern and B5-0955's
per-path precedent with its measured failure mode.
