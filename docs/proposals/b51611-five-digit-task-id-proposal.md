---
document:
  title: "B5-1611 — five-digit B5-ids: rendering and regex-width proposal"
  status: "Proposal (candidate; no authority until merged and compiled)"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free) 6", version: "kilo-auto/free"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Proposal: how a B5-id above B5-9999 must render, and where regex width is hard-coded

Task **B5-1611**, proposal only. **No shared tool was edited**, no ledger row was
edited, no card data touched. Every claim below is a measurement taken
2026-10-01 against `b5ccg`-independent coordination files.

---

## 0. Headline: this is a pre-problem, not an incident

Measured over `.agent/TASK_LEDGER.md`: **719 rows, 715 distinct ids, max id
B5-1641.** The highest id ever issued is `B5-1641`.

* Headroom to the first blocked id: **9999 − 1641 = 8358 ids**.
* Today's seeding waves are 12 rows each. At that rate the four-digit space
  outlives any foreseeable horizon.

So **nothing is broken now**, and this proposal deliberately does **not** ask
for an emergency change. It exists because the failure mode when the boundary
is crossed is **silent**, and because one fix is already available today at
near-zero risk (§3). Applying it *before* B5-10000 exists is the whole point:
after the boundary, the repair cannot be a quiet regex edit, because by then
some tasks will already be invisible to the gate.

---

## 1. The rendering rule this proposal adopts

> **A task id is `B5-` + the decimal number, with no leading zeros, no width
> padding, and an optional single lowercase letter suffix.**

Consequences, all of which follow from the one sentence above:

| Rule | Rationale |
|---|---|
| No zero padding above 4 digits | `B5-10000`, never `B5-010000`. Padding makes the id a *string* convention rather than a number, and every width change then needs a second migration. |
| Width is **not** fixed at 4 | The number is the identity. `{4}` is an artefact of the era when ids were 1–9999, never a requirement. |
| Suffix stays `[a-z]?` — **at most one letter** | The suffix is a disambiguator for follow-up rows (`B5-0202c`, `B5-0329a`, `B5-0330a`, `B5-0331a` all exist today). One letter is the measured de-facto convention. |
| Suffix is **part of the id** | §2 shows what happens when a reader drops it. |
| Ledger rows, claim filenames, heartbeat `current_task`, report filenames and all census regexes use **one** spelling of the id | One spelling per identity is the repo's existing R1–R7 discipline; widening it to the numeric form changes nothing about it. |

### 1.1 Sorting and ordering consequence

Ids must order **numerically, not lexically**. `B5-10000` sorts *before*
`B5-9999` under a plain string sort, which inverts the queue at exactly the
boundary. Any tool that orders ids as strings will silently mis-order the whole
tail. Note that `.agent/run-queue.ps1:339` already anchors numerically:

```powershell
$m = [regex]::Match([string]$Id, '^B5-(\d+)')
```

— it captures digits and compares the captured number, not the whole string.
That is the **correct** pattern and should be the model; see §3.

---

## 2. Measured: the shipped pattern cannot see a five-digit row

The width-4 pattern is hard-coded in **five** coordination files across **11**
sites. All were measured by reading the shipped source; the behavioural
consequence was measured separately on a synthetic fixture.

| File | Line | Site | Role |
|---|---|---|---|
| `.agent/run-queue.ps1` | 244 | `'^\|+\s*(B5-[0-9]{4}[a-z]?)\s*\|'` | row identity for the queue |
| `.agent/run-queue.ps1` | 255 | `'^B5-[0-9]{4}[a-z]?$'` | task-id-only cell recovery |
| `.agent/run-queue.ps1` | 279 | `'^\|+\s*B5-[0-9]{4}[a-z]?\s*\|'` | permissive-row count |
| `.agent/tools/ledger-query.ps1` | 105 | `'^(\|+)\s*(B5-[0-9]{4}[a-z]?)\s*\|([^|]*)'` | `pipeCount` / `doubleLead` detector row regex |
| `.agent/tools/ledger-query.ps1` | 42 | `rg -o … '^\|+\s*B5-[0-9]{4}[a-z]?\s*\|'` | documented manual fallback |
| `.agent/tools/ledger-query.ps1` | 262 | `[regex]::Matches($stem, 'B5-[0-9]{4}[a-z]?')` | **report-mtime index (three-signal liveness)** |
| `.agent/tools/dup-census.ps1` | 62 | `'^\|+\s*(B5-[0-9]{4}[a-z]?)\s*\|'` | duplicate-ID census |
| `.agent/tools/census-crosscheck.ps1` | 107, 113, 127, 293 | four sites mirroring the above | cross-check tool |
| `.agent/tools/new-claim.ps1` | 69 | `'^B5-[0-9]{4}[a-z]?$'` | **claim-creation gate — hard REFUSAL** |

### 2.1 Behavioural measurement (synthetic fixture, outside the repo)

A five-row fixture was built and both regex forms run over it:

| id | hard-coded `{4}` | general `[0-9]+` |
|---|---|---|
| `B5-1609` | `True` | `True` |
| `B5-9999` | `True` | `True` |
| `B5-10000` | **`False`** | `True` |
| `B5-10000a` | **`False`** | `True` |
| `B5-12345` | **`False`** | `True` |

**2 of 5 rows matched by the shipped pattern; 3 rows invisible.** Running the
duplicate-ID logic over the fixture reported `ids seen: B5-1609, B5-9999` and
`duplicates found: none` — i.e. it prints a clean verdict while three real
tasks are absent from its input.

That is precisely the failure class this repo has already paid for twice. A
census that cannot see a row cannot report it as a duplicate (B5-0618: *"status
is keyed by id, so the second row silently overwrites the first and one task
becomes invisible to the gate and lane logic"*), and an undetectable row is
strictly worse than a detectable duplicate, because a duplicate at least prints.

### 2.2 `new-claim.ps1` would refuse to create the claim

Line 69 exits 1 with `REFUSED: task id 'B5-10000' is not a B5-NNNN[a] id.`
This is the one site that fails **loudly and correctly** — a hard refusal is
better than a silent miss — but it means the boundary is a **hard stop**: the
first five-digit task cannot be claimed at all until the gate is widened.
Per `00_BOOT.md` step 6, absence of the claim file is necessary but not
sufficient, so a refused claim would stall that task rather than mis-file it.

### 2.3 The three-signal liveness join degrades first

`ledger-query.ps1:262` builds the report-mtime index from report **filenames**.
A five-digit report would not be indexed, so that player's report mtime reads
as absent → `UNKNOWN`. Per `HEARTBEATS/README.md`, `UNKNOWN` is never `STALE`,
so the *conservative* direction is preserved — no false reaps. But the
consequence is that a completed five-digit task contributes no report signal,
and a claim on it is judged on two signals instead of three.

---

## 3. Recommended change (NOT applied — proposal only)

**One rule, applied to all 11 sites:**

```
B5-[0-9]{4}[a-z]?     →     B5-[0-9]+[a-z]?
```

**One rule, applied to `new-claim.ps1:69` only** (the `+` is deliberate there,
because the gate must *accept* a wider id than the census requires):

```
^B5-[0-9]{4}[a-z]?$   →     ^B5-[0-9]+[a-z]?$
```

and its error string `B5-NNNN[a]` → `B5-<digits>[a]`.

### 3.1 Why `[0-9]+` and not `[0-9]{4,}` or `[0-9]{2,}`

* `[0-9]{4,}` still encodes 4 as a floor and needs a second edit at 5 digits.
* `[0-9]{2,}` invents a lower bound the ledger does not honour — ids below
  B5-1000 are legitimate history (`B5-0202` exists).
* `[0-9]+` needs no future edit. The only cost is that it also accepts a
  hypothetical `B5-7`, which is a *creation*-time concern, not a *reading*-time
  one: a malformed id cannot exist in a row without someone having written it.

### 3.2 The three-signal join is exempt from the literal edit

`ledger-query.ps1:262` matches ids out of report filenames, where an
occurrences scan is correct and `[0-9]+` is also the right width — so the same
edit applies. No exemption is needed; flagging it because it is the site where
the consequence (§2.3) is least visible.

### 3.3 Ordering must be audited, not just width

§1.1's numeric-ordering requirement is a **separate** change from the regex
width and should be verified per site after the width edit: any site that
sorts ids as strings must sort the captured number instead. `run-queue.ps1:339`
already does. **Sites not yet audited for ordering:** `dup-census.ps1:62`,
`census-crosscheck.ps1`, `ledger-query.ps1:105`.

---

## 4. Measured: a reader that drops the suffix invents duplicate IDs

While measuring §2 I ran a deliberately broader regex (`B5-[0-9]+`, no suffix
class) over the live ledger as a sanity check. It reported **4 duplicate ids**;
the shipped census reports **0**.

The four were `B5-0202`, `B5-0329`, `B5-0330`, `B5-0331` — and inspection shows
each is a *real distinct row*, not a collision:

| id reported "twice" | the actual second row |
|---|---|
| `B5-0202` | `B5-0202c` (line 51) |
| `B5-0329` | `B5-0329a` (line 106) |
| `B5-0330` | `B5-0330a` (line 103) |
| `B5-0331` | `B5-0331a` (line 105) |

This is why the suffix class must survive any widening: a naive
`B5-([0-9]+)` capture would have filed four false duplicate-ID defects against
the governance ledger today, and each would be a **misdiagnosis of healthy
rows**. It is also the honest counterpart to §2.1 — the shipped `{4}[a-z]?`
pattern is correct about the suffix and wrong only about width, and a fix that
widens the digits while dropping `[a-z]?` trades one silent failure for four
loud false ones.

**Recommendation:** widen digits, keep `[a-z]?`, and treat "the suffix class was
dropped" as a named regression class for any future id-regex edit.

---

## 5. Adoption checklist (for whoever implements this)

1. Apply §3's two edits across all 11 measured sites.
2. Re-run the §2.1 fixture and assert **5 of 5** rows now match — the current
   instrument cannot fail, so the assertion must name the count.
3. Re-run `run-dup-census.ps1` (expect `PASS`, 0 duplicates) and
   `ledger-query.ps1 -Status "*"` (expect every row `pipeCount 7` /
   `doubleLead no`, count unchanged at 719).
4. Add a five-digit row to a **fixture ledger** and assert the census sees it —
   never to the live ledger, which is append-only and 719 rows deep.
5. Audit numeric ordering at the three sites named in §3.3.
6. Log in `docs/DECISIONS.md` and release the claim.

**Do not** change any existing id, filename, or report name. This is purely the
width a *future* id may use; R5 (never retro-rename) applies unchanged.

---

**Reusable lesson:** a regex that encodes the current maximum width of a value
is not a format rule, it is a countdown — and the correct moment to widen it is
while the boundary is still hypothetical, because afterwards the repair can no
longer be a quiet edit.

**Report:** `.agent/REPORTS/2026-10-01-Kilo (kilo-auto-free) 6-B5-1611.md`
**Pattern:** `.agent/PATTERNS/Kilo (kilo-auto-free) 6/2026-10-01-a-regex-that-encodes-the-current-max-width-is-a-countdown.md`