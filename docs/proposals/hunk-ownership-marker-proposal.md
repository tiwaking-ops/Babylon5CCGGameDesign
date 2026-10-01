---
document:
  title: "Hunk-ownership marker: an unambiguous, machine-readable label on every uncommitted hunk"
  status: "Proposal — confers no authority until adjudicated and compiled into governance"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free) 1", version: "kilo-auto/free"}
  last_modified_by_llm: {name: "Kilo (kilo-auto/free) 1", version: "kilo-auto/free"}
  created_date: "2026-10-01"
task: B5-1465
amendment_section: "docs/reports/autonomous-pm-system-amendment-2026-09-30.md section 6.4"
---

# Hunk-ownership marker

**The gap this addresses** (amendment §6.4, verbatim): *"Dirty-tree triage load.
B5-1415 classified 13 foreign hunks across 4 files read-only; the convention that
saved it was in-file row labels. Fundamental fix: every uncommitted hunk SHOULD
carry its owning row id in a comment, so triage never needs style attribution."*

The row's framing — "so triage never needs style attribution" — is right about the
goal and imprecise about the mechanism. **What B5-1415 actually relied on was not
the presence of a row id; it was being able to tell the owner apart from the
citations.** Those are different properties, and only the second one makes triage
mechanical. This proposal specifies the second, and measures the first.

## What is already true (measured 2026-10-01, on the tree as it stood)

45 hunks across the seven dirty `b5ccg/src/` files, 792 added lines. Per-hunk
label census over **added lines only** (B5-0755's method — context lines carry the
old file's comments and will attribute new bytes to unrelated old tasks):

| File | Hunks | ≥1 row id | 0 row ids | >1 row id |
|---|---|---|---|---|
| `ai/AIPlayer.java` | 17 | 12 | 5 | 2 |
| `engine/DeckLoader.java` | 4 | 3 | 1 | 1 |
| `engine/CardEffects.java` | 5 | 5 | 0 | 5 |
| `engine/GameController.java` | 2 | 2 | 0 | 0 |
| `engine/RulesEngine.java` | 2 | 2 | 0 | 0 |
| `ui/MainWindow.java` | 13 | 8 | 5 | 6 |
| `ui/GameBoardPanel.java` | 2 | 1 | 1 | 1 |
| **total** | **45** | **33 (73%)** | **12 (27%)** | **15 (33%)** |

Two numbers to hold at once, because together they are the argument:

- **73% of hunks carry a row id.** The convention is real and widespread; this is
  not a proposal to start something nobody does.
- **33% of hunks carry more than one row id.** `engine/CardEffects.java` is 5 of
  5. So on a third of the tree, "does this hunk mention a row id?" answers *yes*
  while "which row owns it?" is still open.

A presence test therefore passes on a third of the corpus while the question it was
meant to answer is unanswered. **Any automated checker built on "has a label"
would be green on those 15 hunks and wrong on them.** B5-1415 got the right answer
because an agent read the comments and reasoned about which id was doing the
claiming and which were citing a contract — that reasoning is the actual mechanism,
and it is manual.

## The rule

Every hunk an agent leaves uncommitted MUST carry **one** ownership marker in the
form:

```
// OWNER: B5-NNNN
```

Placement, in priority order:

1. **Immediately above the first line the hunk adds** — inside the method, adjacent
   to the change, so `git diff -U0` output carries it in the same hunk.
2. If the first added line is inside a method whose body begins elsewhere, the
   marker goes on the line directly above the first *added* line, indented to that
   line. Indentation does not matter; adjacency does.
3. A whole-new method may carry the marker in its javadoc instead, provided the
   javadoc's **first** line contains it (so a `-U0` hunk still sees it).

### Exactly one, and it is the owner

- **Exactly one `OWNER:` marker per hunk.** Two owners means two agents' work in
  one hunk; that is a split the author should make as two commits or two hunks, not
  something the marker should paper over.
- A `OWNER:` marker names the row the author was working, **not** a row the code
  depends on. Citing B5-0691's engine law or B5-0351's difficulty contract inside
  the comment is correct and required — those are citations. Writing `OWNER:
  B5-0691` there is false, and it is precisely the confusion the marker format
  exists to make impossible: `OWNER:` is a reserved token, and a citation is
  written in any other form.
- **No `OWNER:` marker on a mechanical edit.** A pure import, a pure reformat, or
  a build-tool artefact carries no marker and is reported as such. This is what
  makes "zero markers on a file" a meaningful answer rather than an ambiguous one.

### Retagging

An author who retrofits a marker to already-committed code is not writing an
ownership claim; they are filling in the record. **Retagging is permitted and
costs nothing**, because the marker is commentary and `git diff` remains the
arbiter of what changed. This is what makes adoption incremental: the 33% of the
tree whose comments already name the right row can be tagged in one pass with no
code change at all, and the 12 unlabelled hunks are then the *only* ones needing
real forensic work.

## The triage procedure this enables

Replaces "read the diff and reason about style" with five deterministic steps:

```
for each hunk in git diff -U0 <file>:
    owners = row ids on ADDED lines matching /OWNER:\s*(B5-\d{4})/
    if owners is empty      -> UNLABELLED:  needs forensic attribution
    if |owners| > 1         -> CONFLICT:    needs split before it can be attributed
    else                    -> attributed to owners[0]; confirm that row's status
```

Every branch is mechanical. The only judgement left is the branch that says
"needs a human", and it is reached by *absence or multiplicity*, never by reading
prose and guessing.

Two safety properties, both deliberate:

- **A label is a claim, not proof.** The procedure confirms the named row exists
  and reads its status; it does not conclude the work was done. A hunk labelled
  `OWNER: B5-1047` where B5-1047 reads OPEN is the exact B5-1415 observation —
  work product sitting in the tree under a row whose status disagrees — and the
  procedure surfaces it rather than hiding it.
- **The marker is on added lines only** (B5-0755). Scanning the whole file will
  attribute old hunks to whatever comment happens to precede them, which is the
  bug that rule already exists to prevent.

## Cost, and the honest counter-argument

One comment line per hunk. Against that:

- **It is a comment convention, so it is unenforceable in the way a schema is.**
  Nothing fails if an agent omits it. A rule that only ever produces a finding is
  a rule that trains the fleet to ignore findings, so this proposal does **not**
  propose a gate: no check, no doc-gate failure, no task blocked on it.
- **It can go stale.** A hunk edited by a later agent may legitimately need a
  different owner, and a stale marker is worse than a missing one because it is
  believed. Mitigation: the marker names a *row*, and row status is the live
  signal — a stale marker points at a row whose status has moved, which is
  visible. A marker that survives into a commit is harmless (it is then just a
  comment) and should not be stripped.
- **It adds a token that looks like data to readers who do not know the
  convention.** Accepted; that is the cost of a convention, and `OWNER:` is
  self-describing enough to be self-explaining.

**Verdict: adopt the convention; do not build a gate for it.** The value is
entirely in the 15 multi-id and 12 unlabelled hunks, which are exactly the ones
that currently cost an agent an hour of reading. A gate would cost more in false
positives than the triage it saves.

## What adoption looks like

1. A later, separately-claimed task adds the convention to `AGENTS.md` (one line)
   and to `guidelines/Guidelines.md` (the placement rules above).
2. A retagging pass walks the seven dirty files and inserts `// OWNER:` on every
   hunk whose owner is already established by report — 33 hunks, no judgement.
3. The 12 unlabelled hunks get reported as `UNLABELLED` and attributed or marked
   orphan on their own merits.

Step 3 is the honest cost, and it is paid by whoever triages next, once.

**Reusable lesson:** a convention measured only by presence is a convention that
passes on the cases it was meant to catch — 73% labelled and 33% ambiguous are
both true, and only the second number describes the work. Measure the property
you need (one owner, distinguishable from a citation), not the one that is easy
to count.