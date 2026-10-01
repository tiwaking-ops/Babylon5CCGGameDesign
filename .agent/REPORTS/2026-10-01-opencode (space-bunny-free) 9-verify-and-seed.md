---
document:
  title: "Repo verification pass and two seeded rows (B5-1928, B5-1929)"
  status: "Report"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 9", version: "space-bunny-free"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_by_llm: {name: "opencode (space-bunny-free) 9", version: "space-bunny-free"}
  last_modified_date: "2026-10-01"
---

# Repo verification pass — gates green, two rows seeded

## Verification battery: GREEN

`powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/run-verification-battery.ps1`
exit 0, `=== BATTERY GREEN ===`, all eight instruments at designed-or-declared codes:

| instrument | designed | observed | verdict |
|---|---|---|---|
| javac -version | 0 | 0 | GREEN (1.8.0_292) |
| b5ccg compile.bat | 0 | 0 | GREEN |
| run-dup-census | 0 | 0 | GREEN |
| ledger-query -Status * | 0 | 0 | GREEN |
| validate-heartbeats | 0 | 1 | GREEN (declared standing red, set gate clean) |
| run-queue -DryRun | 0 | 0 | GREEN |
| check-citations | 0 | 0 | GREEN |
| census-crosscheck (bonus) | 0 | 0 | GREEN |

**The GREEN is falsifiable, not decorative.** All three battery RED paths were
executed and each differential held:

- `-SelfTest` -> RED via synthetic duplicate-ID fixture (scalar path).
- `-SelfTestSet` -> RED with the scalar alone reading GREEN, isolating the set gate
  (the path that actually failed in B5-1067).
- `-SelfTestCitations` -> RED via the real B5-1925 dangling-citation shapes.

Java 6 census `PYTHONIOENCODING=utf-8 py .agent/tools/census-b50960.py`: 65 tracked
files, every construct family `code-lines 0`; arrow prose 50 lines / 64 occurrences
in comment bands only. Qualified `.getOrDefault(` = 0, unqualified = 14 (the
project's own `DeckLoader.getOrDefault` helper). Matches the AGENTS.md section 2a
standing table exactly. The frozen archive reads the opposite on every family, which
is the instrument's built-in validation.

`py .agent/tools/verify_task.py` exit 0, ALL CHECKS PASSED.

## One earlier claim of mine was wrong, and it mattered

I first reported `docs/DECISIONS.md` as having "lost ~47 KB" of uncommitted content.
**That was a misread, and acting on it would have destroyed data.** Measured properly:

| | HEAD | working tree |
|---|---|---|
| entries | 292 | 261 |
| distinct B5 ids | 387 | **491** |
| ids only in HEAD | — | 267 |
| ids only in tree | — | **371** |

The working tree is a **diverged later generation**, not a damaged copy. It already
carries 175 entries headed `restored after the 2026-10-01T03:44:59Z truncation` from
DONE row B5-1481. The apparent "loss" was 267 unrestored ids against 371 newer ones —
`git checkout HEAD -- docs/DECISIONS.md` would have silently destroyed all 371.

What is genuinely unowned is the **encoding damage on every append since that
recovery**: 5 stacked UTF-8 BOMs at byte 0 (HEAD has 0), whole-file LF->CRLF flip
(HEAD is pure LF), and cp1252 double-decoding (HEAD carries 1226 clean em-dashes; the
tree carries the mojibake form). Neighbouring rows do not cover it: B5-1693 is line
endings only, B5-1725 is BOM-less PowerShell *parsing*, and B5-0989's guarded repair
is DONE against an older generation. Seeded as **B5-1928**, census-only and
read-only.

## Provenance finding decomposed: 8 flagged, 4 false positives

`.agent/tools/verify_task.py:245` requires a brace, a `name:` key, a `version:` key and
double quotes:

```python
provenance_re = re.compile(r"author_llm:\s*\{[^}]*name:\s*\"[^\"]+\".*?version:\s*\"[^\"]+\"[^}]*\}", re.DOTALL)
```

AGENTS.md section 1 line 27 mandates `author_llm: <name> (<version>)` — the
parenthesized form — and line 33 shows flow-mapping for the `unknown` case. The
checker encodes one serialization, not the rule, so it rejects the dialect AGENTS.md
itself specifies. `BABYLON5_CCG_RULEBOOK.md` is the clearest instance: it carries
`author_llm: Muse Spark (muse-spark-1.3-contributor-free)` and is flagged anyway.

| file | reality |
|---|---|
| `BABYLON5_CCG_RULEBOOK.md` | format mismatch — provenance present |
| `docs/proposals/2026-09-25-solar-pro4-free-B5-0422.md` | format mismatch (also italic-wrapped) |
| `docs/proposals/2026-09-25-solar-pro4-free-B5-0428.md` | format mismatch |
| `docs/proposals/2026-10-01-b51349-...md` | `author_llm: me-so-poor` — no version; minor real gap |
| `.agent/HelloWorld.md` | none — stray scratch ("Hello World") |
| `ATTRIBUTIONS.md` | none — Figma Make tool artifact, arguably out of scope |
| `koda-memory.md` | none — scratch |
| `loop-prompt.md` | none — scratch |

Not 8 of a kind: 4 format mismatches, 2 real gaps, 2 out-of-scope. On user ruling
2026-10-01 to edit the minority files rather than widen the checker, seeded as
**B5-1929**.

## Reusable lesson

**A byte-count or size delta between a working tree and HEAD is a measurement, not a
diagnosis.** `HEAD 674398 bytes` against `632860` in the tree reads as "47 KB lost"
and invites a restore that would have destroyed 371 newer entries. Count *identifiers*
the encoding cannot touch — `B5-\d{4}` was unaffected by the mojibake — and the same
comparison reads as a diverged later generation. The repo's own history is the receipt:
B5-1669 already recorded that mention-presence is not restoration-presence, and the
inverse held here, where presence in the tree is not restoration of HEAD.

Second lesson, narrower: **I reported a gate's output before reading the gate's
contract.** The battery header said `16 non-conforming` against a declared inventory of
15, which reads as drift; it is 15 distinct names over 16 files because
`_quarantine/solar-pro4.json` shares a basename with the live one. Confirmed by dumping
codepoints rather than by trusting the console.

## Gates run this pass

| gate | result |
|---|---|
| `run-verification-battery.ps1` | exit 0, GREEN |
| battery `-SelfTest` / `-SelfTestSet` / `-SelfTestCitations` | all three RED as designed |
| `census-b50960.py` | all families `code-lines 0`, qualified `getOrDefault` 0 |
| `compile.bat` | exit 0 |
| `verify_task.py` | exit 0 |
| `run-dup-census.ps1` | PASS, 0 duplicates |
| `ledger-query.ps1 -Status OPEN` | B5-1928 / B5-1929 read `7` / `no` |
| `check-citations.ps1` | PASS, 238 refs scanned, 0 dangling |
| `run-queue.ps1 -DryRun` | claimable OPEN 61 -> 63; both new rows visible |

## Bounds

No commit, no push. `docs/DECISIONS.md` **not** written — the B5-1928 row is census-only
by construction. Ledger change is two appended OPEN rows, byte-appended in CRLF to
match the file's dominant ending, both re-verified at 7 pipes / single lead. No
existing row, claim, or heartbeat was edited. Heartbeat
`opencode (space-bunny-free) 9.json` written by the shipped tool, now `idle` holding
no claim.