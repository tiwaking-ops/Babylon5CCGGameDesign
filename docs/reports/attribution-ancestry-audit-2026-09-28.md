---
document:
  title: "B5-0959 close-out — the ancestry rule exercised over every attribution the B5-0921 report and B5-0925 entry make"
  status: "Report (no authority; observations and test results only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0959"
---

# B5-0959 close-out — running the rule until it had to produce a value

**Claim:** `.agent/CLAIMS/B5-0959.json` (`Buffy (glm-5.3-flash)`, 18:37:15Z). Released
at close-out. Row re-read `OPEN` immediately before claiming; gate (B5-0948 DONE)
verified in the ledger before the claim.
**Scope held:** read-only git inspection + this report + one pattern + heartbeat +
DECISIONS entry (written — the rule WAS missed somewhere, see verdict 1). No edits to
B5-0921/B5-0925 artifacts, no `git rm`, no `.gitignore` edit, no commit, no push.

## The battery: every commit named in the two artifacts, both refs, this pass

Commits named in attributions: `d1d0c6ff`, `da58390f`, `786b34a3`, `7f8f1e33`,
`59c4f251` (B5-0921 report); `d1d0c6ff`, `da58390f`, `786b34a3` (B5-0925 entry);
plus the corrected introducer `418664de` (B5-0948). Hex-string sweep of both
artifacts confirms that is the complete set.

| commit | subject / role as attributed | ancestor of HEAD | ancestor of origin/main |
|---|---|---|---|
| `d1d0c6ff` | "Add files from Figma Make", 2026-09-21 — the scaffold import | **TRUE** | **TRUE** |
| `7f8f1e33` | "B5-0399: Working-tree checkpoint commit", 2025-09-25 → 2026-09-25 — conformance snapshots | **TRUE** | **TRUE** |
| `59c4f251` | "B5-0485 (solar-pro4:free): Working-tree checkpoint — … java …", 2026-09-26 — 0-byte `java` | **TRUE** | **TRUE** |
| `418664de` | "B5-0529 checkpoint (me-so-poor unknown)…", 2026-09-26 — the sole `node_modules` introducer | **TRUE** | **TRUE** |
| `da58390f` | "reply hello" — falsely attributed as introducer, deposed by B5-0948 | FALSE (dangling checkpoint) | FALSE (dangling checkpoint) |
| `786b34a3` | "reply hello" — same | FALSE (dangling checkpoint) | FALSE (dangling checkpoint) |

`da58390f`/`786b34a3` resolve to `refs/koda/checkpoints/01a0e660…` and
`…/01a0e66e…` — re-measured, not quoted from B5-0948.

## Verdicts

1. **THE RULE WAS MISSED SOMEWHERE — this is the row's finding.** The B5-0948
   correction applied its own new rule to `da58390f`/`786b34a3` (deposition), and to
   `7f8f1e33` (verification of B5-0921's one *true* attribution). It never recorded an
   ancestry result for `d1d0c6ff` or `59c4f251` — both attributed in B5-0921 as
   introductions ("brought in package.json, src/…", "Swept in by checkpoint 59c4f251")
   with no ancestry beside them, and neither appears anywhere in the B5-0948 report.
   The governing prose ("prove a commit is an ancestor of the ref you are discussing
   BEFORE calling it an introduction") was written as a gate; the execution ran it on
   two commits. A rule that decays into decoration: exactly what this row exists to
   catch, found in the very artifact that wrote the rule.
2. **All standing attributions SURVIVE the audit anyway.** Both previously-unverified
   attributions measure TRUE/TRUE this pass, so the B5-0921 report is *factually*
   right about `d1d0c6ff` and `59c4f251` — but two of its five named commits carried
   no recorded evidence chain until now. Right by luck is not right by verification;
   the audit's value is the recorded chain, not a change of verdict.
3. **Byte counts reproduce.** 68,117 tracked files; 67,258 under `node_modules`
   (98.74%); 201,791,663 on-disk bytes — **byte-identical to B5-0948, my own B5-0949
   report, and the B5-0949 proposal** (that proposal's two counts re-verified against
   its text, closing the citation chain). The B5-0921 summary's object-store figure
   (~56.2 MB packed + loose) is the one number in the artifact set with no recorded
   derivation; the method note about the 65.6 MB gap stands as-is.
4. **No missed attribution beyond the two artifacts.** The exhaustive sweep
   (`git log -- node_modules/` = exactly 1 commit, `--diff-filter=A` = same, no
   `node_modules` entry in `.gitignore`) bounds the attribution surface; the hex sweep
   found no other commit named as an attribution. B5-0948's koda refs
   (`01a0e660`/`01a0e66e`) are ref *names*, not attributions.
5. **Phase 1 / Phase 2 soundness: unchanged and still sound.** Phase 1's command
   sequence never depended on the attribution and its approval argument is now
   *stronger* — every introduction it cites (`418664de`, `7f8f1e33`, `59c4f251`)
   measures TRUE/TRUE on both refs. Phase 2 (history rewrite from `d1d0c6ff^`) needs
   its named base verified only at execution time (`d1d0c6ff` verified TRUE/TRUE in
   this audit's table above); it remains OUT OF SCOPE without explicit human approval.

## Verification

- Every table cell above measured this session via
  `git merge-base --is-ancestor <c> {HEAD,origin/main}`; subjects/dates via
  `git log -1 --format`. `d1d0c6ff` regression check: `git ls-files package.json`
  still resolves (path exists in HEAD).
- `b5ccg/compile.sh` exit 0, "Build successful" (JDK 1.8.0_292, `-source 6`) —
  measured this session; no `src` touched by this claim.

## Reusable lesson

Filed as a NEW record under `.agent/PATTERNS/Buffy (glm-5.3-flash)/`
(`2026-09-28-a-rule-is-only-what-it-covered.md`): when you write a rule mid-audit,
re-run the audit against the rule — the corrected report itself becomes the next
unverified artifact, and the set of things the rule *could* apply to is always larger
than the set that prompted it.
