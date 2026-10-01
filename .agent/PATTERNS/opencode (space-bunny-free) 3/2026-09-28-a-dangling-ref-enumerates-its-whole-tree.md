---
document:
  title: "A dangling ref enumerates its whole tree, so its diff looks exactly like an introduction"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 3", version: "space-bunny-free"}
  last_modified_by_llm: {name: "opencode (space-bunny-free) 3", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A dangling ref enumerates its whole tree, so its diff looks like an introduction

*Advisory pattern, same tier as `investigations/` — never canonical. Namespace:
`opencode (space-bunny-free) 3`. First record in this namespace.*

**The failure.** Tooling that keeps worktree snapshots as `refs/*/checkpoints/*`
creates commits that record an entire directory as an addition. `git show
--name-only <snapshot>` therefore lists every tracked file, and `--diff-filter=A`
still reports *all* of them as added, because the snapshot has no parent commit
carrying them. A path enumeration of a dangling ref is **indistinguishable from
a real introduction** unless ancestry is checked.

Measured on this repo: `da58390f` and `786b34a3`, cited in a DONE report as the
commits that introduced 67,258 `node_modules` files, are
`refs/koda/checkpoints/01a0e660-…` and `refs/koda/checkpoints/01a0e66e-…`.
`A` = 67,258, `D` = 0, `M` = 0 — a textbook introduction, by the numbers. They
are not ancestors of `HEAD` or `origin/main`. The real introduction was
`418664de`, an ancestor of both.

**The rule.**

1. Before calling a commit the introducing commit, run
   `git merge-base --is-ancestor <c> <ref>`. Exit 0 = yes; anything else = no.
2. For a claim about *published* history, run it against `origin/main` as well.
   Being an ancestor of `HEAD` is not being published.
3. `git rev-list --all --count <c>` returning a number proves only that *some*
   ref reaches it — and a checkpoint ref reaches itself. It is not evidence of
   membership in the project.
4. Read the ref name before reading the diff. `refs/…/checkpoints/…` is a
   snapshot by construction. A path with `/checkpoints/` in it should never be
   cited as a provenance claim without the ancestry test in step 1.

**Why this class is worth a rule.** Every gate in this repo is blind to it.
`compile.bat` does not read history. `run-dup-census.ps1` does not.
`validate-heartbeats.ps1` does not. Build green, census PASS, heartbeats clean
and a provenance claim false are fully compatible, and a rerun of the gates
would reproduce the green and the error together. The only defence is that the
provenance claim is tested at the place it is made.

**The second-order trap.** The false attribution was consumed downstream: a
later row cited it as *counter-evidence* about human intent ("a human touched
the scaffold after the import"). Correction therefore had to cover four sites,
one of which reasoned about intent rather than repeating a hash. When correcting
a provenance error, search for its **consumers**, not just its restatements —
grep the hash, then read the sentences that lean on it.

**Supersede, never rewrite.** The correction was filed as a new DECISIONS entry
and left the original report, ledger note and entry byte-identical (AGENTS.md 1a
rule 4). A reader who finds the old claim must be able to find the retraction
next to it.
