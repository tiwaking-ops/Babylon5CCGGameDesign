---
document:
  title: "Proposal — wire verify_task.py into the standing verification battery and the boot procedure"
  status: "Proposal"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free 28"}
  assessor_llm: []
  created_date: "2026-10-03"
  last_modified_date: "2026-10-03"
---

# Wire `verify_task.py` into the battery and the boot procedure

**Origin row:** `B5-2175`. **Upstream measurement:** `B5-1951`
(`.agent/REPORTS/2026-10-02-Kilo (kilo-auto-free) 13-B5-1951.md`), which
classified `verify_task.py` as one of 6 of 17 `.agent/tools/` instruments with
**no call site of any kind** — not battery-invoked, not named as an invocation
by any procedure document, not called by another tool.

**This row edited nothing.** No tool, no procedure document, no ledger row but
its own, no card data. Every line reference below was read this session.

---

## 1. Recommendation

Wire it at **both** tiers, because B5-1951's finding is precisely that it has
neither, and the two tiers answer different questions:

| tier | what it buys | where the change lands |
|---|---|---|
| **battery** | the Java 6 + provenance gate runs in one command with a single verdict | `.agent/tools/run-verification-battery.ps1` |
| **boot procedure** | a session that never opens the battery still runs the gate | `.agent/00_BOOT.md` |

Add it as **instrument 8, after instrument 7 (`check-citations`) and before the
`census-crosscheck` bonus** — last among the real instruments, so the cheap
discriminators run first and a reader sees the fast failures before the slow
aggregate. Do **not** renumber the existing seven; the battery's instrument
names are the labels its expected-red entries are keyed by
(`$ExpectedRed['validate-heartbeats']` at line 155), and renumbering them would
decouple the labels from the declaration.

---

## 2. Why it is worth wiring at all, stated precisely

`verify_task.py` is the only shipped instrument that covers two classes nothing
else in the battery covers:

- **Java 6 construct violations** — 11 blocking patterns
  (`verify_task.py:31`–`:43`), plus a self-proving negative control at
  `:263`–`:301` that asserts its own `try_with_resources` regex can still
  detect a real violation and still reject the four measured false-positive
  shapes. No other battery instrument reads `b5ccg/src` at all.
- **Governance-tier provenance** — `author_llm` presence across root,
  `guidelines/`, `docs/`, `docs/proposals/`, `docs/reports/`, `.agent/` and
  `.agent/tools/` (`:233`–`:249`).

The battery currently verifies structure (ids, pipes, heartbeat schemas,
citation presence). It verifies **nothing about the Java source** and
**nothing about provenance**. That is the gap.

---

## 3. Measured behaviour of the instrument, 2026-10-03

Run on this host, `javac 1.8.0_292`:

```
py .agent/tools/verify_task.py     ->  exit 0
```

Three facts from that one run that the wiring has to respect:

**(a) It is not a build gate, and wiring it adds no new red.**
`compile.bat` failed on this tree (the in-flight `ui/MainWindow.java:2922`
`DeckBuilderDialog` refactor, 2 errors) and `verify_task.py` still exited 0.
The reason is its own line 202: a non-zero build is appended to `report_only`,
not to `blocking`. So instrument 8 does **not** duplicate instrument 2, and
wiring it while the tree is build-red leaves the battery exactly as red as it
already was. This is worth stating because the opposite is easy to assume.

**(b) `ALL CHECKS PASSED` is the last stdout line even when findings were
printed above it.** On this run the instrument printed three report-only
blocks and then `ALL CHECKS PASSED`:

- `Main.java has 34 unexpected diff lines vs archive (DETECTOR DEFECT: positional compare, not edit-distance…)`
- `compile.bat failed with exit code 1: …`
- `4 governance-tier .md files missing author_llm provenance …`

This is the silent-green shape `00_BOOT.md` step 9 already records for
`run-dup-census.ps1` (B5-1732: *"Empty output is NOT the pass condition … was
withdrawn as wrong"*). **The probe must read `$LASTEXITCODE` and must never
grep stdout for `PASSED`.** Wiring it in without that discipline would add an
instrument whose own success banner contradicts its own findings.

**(c) Report-only findings do not move the exit code.** `sys.exit(0)` at
`:325` is reached whenever `blocking` is empty, no matter how many
`report_only` entries exist. So wiring this instrument makes the **blocking**
classes standing. It does **not** make the provenance class or the
`Main.java` detector defect standing — those stay invisible to any exit code.
Say so in the procedure text, or the wiring will be credited with more than it
delivers.

---

## 4. The exact change to `run-verification-battery.ps1`

Five edits. Line numbers are the file as read this session.

### 4a. Layout gate — add the tool, so a missing file is exit 2

`$required` at lines 208–220. Add one line, in the same `Join-Path` style:

```powershell
  (Join-Path $repoRoot '.agent\tools\verify_task.py'),
```

Without this, a renamed or deleted `verify_task.py` makes the probe silently
skip the instrument and the battery reports GREEN while not running it — which
is the exact failure the layout gate exists to prevent (see its own comment at
lines 213–215 for the `check-citations.ps1` precedent).

### 4b. The probe — a `.py` instrument needs `py`, not `powershell -File`

Every existing probe shells `powershell -NoProfile -ExecutionPolicy Bypass -File`.
This one is Python, and `python` / `python3` are **not on `PATH` on this host**
(`.agent/SHELL.knowledge.md:139`–`:164`, human-approved B5-1991). The runnable
form is `py`. Place after instrument 7, at line 538:

```powershell
# 8. Java 6 construct + governance provenance gate (B5-2175).
# Designed 0. Deliberately NO ExpectedRed entry: the instrument reads 0 on the
# live tree as of 2026-10-03, so any nonzero is a NEW blocking finding.
# Judge the EXIT CODE, never stdout: the instrument prints its report-only
# findings and then the line "ALL CHECKS PASSED" (verify_task.py:325), so a
# stdout grep reads green over a screenful of findings. Report-only findings do
# not move the exit code at all, so this instrument makes the BLOCKING classes
# standing and leaves the provenance class report-only.
Test-Instrument -Name 'verify_task' -Designed 0 -Detail '0 clean / 1 blocking Java 6 or negative-control failure / 2 unreadable; report-only findings do not move the code; judge the exit code, never the ALL CHECKS PASSED line' -Probe {
  # PYTHONIOENCODING is scoped to this probe and is precautionary, not measured
  # necessary on 2026-10-03: that run's findings were all ASCII. It is required
  # anyway because the instrument prints source lines verbatim
  # (verify_task.py:115, :127, :131, :184) and the Windows console defaults to
  # cp1252, which raised UnicodeEncodeError during the B5-1012 census re-run.
  # Same rule as AGENTS.md section 2a for census-b50960.py.
  $prev = $env:PYTHONIOENCODING
  $env:PYTHONIOENCODING = 'utf-8'
  try {
    & py (Join-Path $repoRoot '.agent\tools\verify_task.py') | Out-Null
    $LASTEXITCODE
  } finally {
    if ($null -eq $prev) { Remove-Item Env:\PYTHONIOENCODING -ErrorAction SilentlyContinue }
    else { $env:PYTHONIOENCODING = $prev }
  }
} | Out-Null
```

The `try`/`finally` restore matters: the battery is itself invoked from
PowerShell sessions that may rely on the ambient value, and a probe that leaks
an env var into the caller's session is a second-order version of the same bug.

### 4c. Header prose — add the eighth instrument to the enumerated list

Lines 11–17 enumerate the instruments in a comment. Add:

```
    8. .agent\tools\verify_task.py         Java 6 construct + provenance gate (B5-2175)
```

Note for the editor: **do not restate a total count.** B5-1951 measured that
the header's counts were already stale — it declared 7 instruments against 5
tools in `.agent/tools` plus `run-queue.ps1`, and a sibling document
(`SHELL.knowledge.md:27`–`:32`) shipped "nine `.ps1` plus one `.py`" against a
measured 12 and 2. An enumerated list plus a number is two facts to keep
aligned; the list alone is one.

### 4d. Version string

Line 121: `$batteryVersion = 'B5-1019 + B5-1067 + B5-1927 2026-10-01'`. Append
this row's id and today's date when the change lands. This is the string the
battery prints as its own version receipt (line 469).

### 4e. Expected-red declaration — **none**

`$ExpectedRed` (lines 154–198) needs no new entry. `verify_task.py` reads 0 on
the live tree today, designed 0 equals observed 0, and `Test-Instrument`
(`:248`) flips any undeclared nonzero to RED. Adding a standing-red entry for
an instrument that is green would be the B5-1067 mistake in reverse: it would
declare a red that does not exist and teach the next session to re-declare it.

---

## 5. Known cost: the build runs twice

`verify_task.py` shells `compile.bat` internally at lines 199–204. As
instrument 8 it therefore builds the tree a second time in the same battery
run, and instrument 2 already built it.

**Not fixable in this wiring.** Suppressing the inner build means editing the
instrument, which this row's scope forbids and which the battery must not do
in any case — the battery's own contract is that it "REPORTS and does not ACT"
(lines 24–26), and an instrument edit is an act. Cost is time, not
correctness: the second build is idempotent and its verdict is discarded by
`report_only`.

**Separate row, if wanted:** give `verify_task.py` a `-SkipCompile` switch and
have the probe pass it, so the battery's instrument 2 remains the single build
authority. That is a tool edit and needs its own claim.

---

## 6. The exact change to `.agent/00_BOOT.md`

The boot tier is prose, so this is an exact replacement, not a script change.
`AGENTS.md` §2a already owns the on-demand Java 6 census command and already
records the load-bearing `PYTHONIOENCODING` and the `py` launcher; the new text
must not contradict either.

Add as a new numbered step after the present step 3a (the task-cell budget
check), so the numbering of steps 4–11 does not shift — `00_BOOT.md`'s own
`assessor_llm` ledger records B5-0614 inserting a census step and renumbering
5–11, and a second renumber is churn:

> **3b. Java 6 construct and provenance gate (on demand — not wired into the
> build).** `verify_task.py` is battery instrument 8 as of B5-2175; run it
> directly when you want the answer without the other seven:
>
> ```
> PYTHONIOENCODING=utf-8 py .agent/tools/verify_task.py
> ```
>
> Use `py` — `python` and `python3` are not on `PATH` on this host
> (`.agent/SHELL.knowledge.md`). `PYTHONIOENCODING=utf-8` is precautionary here
> for the same reason it is load-bearing for `census-b50960.py`: the script
> prints source lines verbatim and the console defaults to cp1252.
>
> **Judge the exit code, never the last stdout line.** `0` clean, `1` blocking
> findings, `2` unreadable (never clean). The script prints its **report-only**
> findings and then the line `ALL CHECKS PASSED`, so a stdout read is green over
> a screenful of findings. Report-only findings do **not** move the exit code:
> `Main.java` positional-diff noise and missing `author_llm` in the governance
> tiers are reported, never gated. This instrument gates **blocking Java 6
> constructs only**.
>
> `javac -source 6 -target 6` stays decisive on syntax; this gate is the
> API-level and provenance second look. The two are not substitutes — a real
> Java 8 construct fails the build, and `Map.getOrDefault` passes the build
> clean at `-source 6`.

And in step 9's battery paragraph, the sentence that currently reads
*"It is instrument 7 of the standing verification battery"* becomes *"It is
instrument 7 of the standing verification battery, alongside `verify_task.py`
as instrument 8."* No other step-9 wording changes.

---

## 7. Acceptance criteria

1. `py .agent/tools/verify_task.py` is reachable from the battery by a **path
   segment**, not by a substring. B5-1951 measured that a plain containment
   test is the wrong test here: `run-dup-census.ps1` contains
   `dup-census.ps1` as a substring, so the naive scan reported a fully-wired
   fleet and zero orphans.
2. `$required` lists `verify_task.py`, so a missing or renamed tool is **exit 2**,
   not a silent skip.
3. **The new instrument can be observed RED.** Add a `-SelfTestVerifyTask`
   switch following the `-SelfTestCitations` shape (lines 416–464), and drive
   the instrument against a `%TEMP%` fixture via its own
   `BABYLON5_PROJECT_ROOT` env var (`verify_task.py:27`). The fixture must
   contain, or the instrument exits 2 rather than the 1 you are trying to prove:
   - `b5ccg/src/` with at least 10 `.java` files (else checklist 1 goes
     report-only, `:97`), one of which contains a real `->` lambda;
   - `b5ccg/src-java8-archive/` non-empty (else `:101` → `unreadable` → exit 2);
   - `docs/DECISIONS.md` present (else `:218` → `unreadable` → exit 2).

   `unreadable` is tested **before** `blocking` at `:312`–`:314`, so an
   incomplete fixture proves nothing. Register the proof under the real
   instrument name `verify_task`, not a fixture name — the `-SelfTestCitations`
   comment at lines 442–444 explains why: a fixture name absent from
   `$ExpectedRed` would go RED on the scalar branch too and prove nothing.
4. The battery verdict is unchanged **on a build-red tree**: instrument 8 must
   not add a red that instrument 2 does not already report, because
   `verify_task.py:202` puts a failed build in `report_only`.
5. The battery's own version receipt names this change.

---

## 8. What this proposal does NOT do

- It does not edit `verify_task.py`, the battery, `00_BOOT.md`, or any other
  tool or procedure document. Every edit above is described, not applied.
- It does not add the `-SkipCompile` switch of §5, which is a separate row.
- It does not make the report-only classes standing. See §3(c): wiring this
  instrument gates blocking Java 6 constructs, and nothing else.
- It does not claim the six unwired tools of B5-1951 are all fixable this way.
  Five of the six are named in this proposal's evidence only as context;
  `migrate-heartbeats.ps1` carries a live risk (B5-1945: mutating,
  non-idempotent, no ownership fence) that is a reason to **keep** it unwired,
  not to wire it.

**Reusable lesson:** an instrument's own success banner can contradict its own
findings — `ALL CHECKS PASSED` printed after three report-only blocks. Wiring a
tool into a gate inherits that contradiction, so the wiring must name which
signal is authoritative (the exit code) and which is decoration (the banner),
and must state plainly which finding classes the exit code cannot see.