---
document:
  title: "Cold-boot sequence for autonomous agents"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm:
    - {name: "Solar Pro4", version: "solar-pro4:free"}
    - {name: "opencode (space-bunny-free)", version: "space-bunny-free" — heartbeat schema pointer added to step 3, human-approved 2026-09-27}
    - {name: "opencode (space-bunny-free)", version: "space-bunny-free" - claim row-status precondition (step 6) and post-write duplicate-ID census (step 9), human-approved 2026-09-27 (B5-0622)}
    - {name: "muse-spark", version: "muse-spark-1.3-contributor-free" - pipe-integrity seeding rule in step 9 (B5-0621)}
    - {name: "opencode (space-bunny-free)", version: "space-bunny-free", passes: 1, last_pass: "2026-09-27", note: "edit: claims-first census rule in steps 4 and 9, human-approved 2026-09-27 (B5-0657)"}
    - {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash", passes: 2, last_pass: "2026-09-27", note: "edit: step 6 started_utc guidance line (B5-0653); step 10 three-signal restatement (B5-0660)"}
    - {name: "opencode (big-pickle-free)", version: "big-pickle-free", passes: 1, last_pass: "2026-09-27", note: "edit: qualified the three bare .agent/TASK_LEDGER.md references in steps 4, 9 and 10, where step 4 already carried the qualified form two lines below the bare one (B5-0693)"}
    - {name: "opencode (big-pickle-free)", version: "big-pickle-free", passes: 2, last_pass: "2026-09-28", note: "edit: step 9 now points at the repaired run-dup-census.ps1 wrapper ahead of the inline bash one-liner and records its 0/1/2 exit contract (B5-0777)"}
    - {name: "opencode (space-bunny-free) 2", version: "space-bunny-free", passes: 1, last_pass: "2026-09-28", note: "edit: step 3 gained the agent_id-names-your-session line carrying R1-R6 by reference, human ruling 2026-09-28 (B5-0785)"}
    - {name: "opencode (space-bunny-free) 8", version: "space-bunny-free", passes: 1, last_pass: "2026-10-01", note: "edit: step 3 adopts the new tool-stamped heartbeat writer and step 6 adopts the tool-stamped claim creator as the default path, both human-approved 2026-10-01 (B5-1915); step 3 also records that validate-heartbeats.ps1 is known-red so its exit 1 is not read as news about one's own file"}
    - {name: "opencode (space-bunny-free) 8b", version: "space-bunny-free", passes: 1, last_pass: "2026-10-01", note: "edit: step 9 gains the citation-presence check alongside the duplicate-ID census and the pipe detector, human-approved 2026-10-01 (B5-1927), closing the gap where DONE rows cited files that had never been created"}
    - {name: "Hermes (stealth-space-bunny-alpha) 1733", version: "stealth-space-bunny-alpha", passes: 1, last_pass: "2026-10-02", note: "edit: step 9 gains the B5-1733 bare-cmdlet detector, so the B5-1541 shell trap is enforced rather than remembered, with the annotation convention it depends on stated here (B5-1733)"}
  last_modified_by_llm: {name: "Hermes (stealth-space-bunny-alpha) 1733", version: "stealth-space-bunny-alpha"}
  created_date: "2026-09-21"
  last_modified_date: "2026-10-02"
---

# 00_BOOT — read this first, every session

1. Read `AGENTS.md`, `guidelines/Guidelines.md`, `docs/DECISIONS.md`.
2. Read `.agent/TASK_LEDGER.md` + list `.agent/CLAIMS/*.json` +
   `.agent/HEARTBEATS/*.json`. A task with a live claim (age < 30 min) is taken.
3. Run `javac -version` (expect JDK 8, e.g. `1.8.0_292`) and record it in your
   heartbeat. Build gate is `b5ccg/compile.bat` (or `compile.sh`):
   `javac -source 6 -target 6`, stdlib only.
   Heartbeat format: `.agent/HEARTBEATS/README.md` is binding — strict JSON with
   `schema_version`, `agent_id`, `utc` (the only canonical timestamp field),
   `state`, and `live_claims` (always present; `[]` asserts you hold nothing).
   `notes` is prose and is never parsed.

   **Write the heartbeat with the shipped writer, never by hand (human-approved
   2026-10-01, B5-1915).** `utc` is the field every liveness decision depends on,
   and a hand-authored one is the weakest link in the design — a model can emit a
   plausible ISO string without ever reading a clock:

   ```
   powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/new-heartbeat.ps1 -AgentId "<agent-id>" -State active -LiveClaims B5-NNNN -Javac 1.8.0_292 -Notes "<prose>"
   ```

   The tool stamps `utc` from `[DateTimeOffset]::UtcNow`, refuses to write a file
   held by a different `agent_id` (00_BOOT step 3's "never edit another agent's
   heartbeat" as code, not prose), and accepts the write only if the payload is
   **not ahead of the real clock** and agrees with its own file mtime. Refreshing
   your own file is expected and allowed. `-VerifyOnly` inspects any heartbeat
   read-only; exit `2` means MISSING, which is `UNKNOWN`, never healthy. Treat a
   non-zero exit as "not written" and report the explicit error — never
   substitute a hand-typed `utc`. There was **no** tool-stamped heartbeat writer
   before this one, so every heartbeat in the store was hand-authored; that is why
   three of them carried `utc` values 186–852 min ahead of real UTC.

   Check the store with
   `powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/validate-heartbeats.ps1`;
   it is read-only and exits non-zero on any non-conforming or unparseable file.
   Note that gate is **known-red** (19 non-conforming files as of 2026-10-01, see
   `docs/reports/clock-integrity-in-agent-coordination-files-2026-09-30.md` §6.3),
   so read its per-file output rather than treating exit 1 as news about your file.
   **Your `agent_id` names your session, not your model** (human ruling 2026-09-28,
   R1–R6 in `.agent/HEARTBEATS/README.md`): `client (model)` + a discriminator no
   running instance is using, carrying **at least one letter or digit** because
   `Get-NormName` strips everything else and a punctuation-only discriminator is no
   discriminator at all. Never reuse or retro-rename an existing id, and never edit
   another agent's heartbeat — not even a colliding one. If
   `validate-heartbeats.ps1` reports an IDENTITY COLLISION, the two files are
   reported, not merged: log it and leave both byte-identical.
3a. **Task-cell budget check (on demand, when you write or seed ledger rows —
   B5-1471 / B5-1730).** The budget for NEW ledger rows adopted by B5-1471 is
   mean <= 764, median <= 479, p90 <= 1879, max <= 4397 characters in the Task
   cell. Measure it with the shipped instrument:

   ```
   powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/measure-task-cells.ps1
   ```

   It measures every row with an id above 1471 and prints a per-row table plus a
   `VERDICT` line. **Run it after seeding, not before** — it measures the ledger as
   it stands. Note for anyone repairing it: B5-1730 found this tool had never
   executed (it failed to parse, then matched zero rows while still printing a
   green verdict). A "FULLY COMPLIES" line is only meaningful if the `Count:`
   above it is non-zero; **check the count, not just the verdict**. The retired
   `B5-1014-harness.ps1` is now at `.agent/tools/_retired/` (archival, byte-
   identical) and is not to be run.

4. Obtain the OPEN-task census ONLY by running the shared census tool:
   `powershell -NoProfile -ExecutionPolicy Bypass -File .agent/run-queue.ps1 -DryRun`
   There is **no bash equivalent** — `.agent/run-queue.sh` has never existed in
   any commit and is not planned (B5-1541). The command above is
   **shell-agnostic**: it names the `powershell` executable by path-independent
   name and a `-File` script, so it runs unchanged from PowerShell, from Git
   Bash, and from `cmd.exe`. Invoke it that way rather than pasting PowerShell
   *cmdlets* into a bash prompt, which is the B5-1541 failure:
   `Select-String: command not found`. If your tool hands you a bash shell,
   that is a property of your tool, not of this repo — read
   `.agent/SHELL.knowledge.md`. Do NOT read or
   grep `.agent/TASK_LEDGER.md` directly to find OPEN tasks — hand-rolled censuses miss
   double-pipe rows and other structural defects, silently hiding claimable work.
   The one-liner `rg -o '^\|+(?:\s*)(B5-\d+)(?:\s*\|)\s*\|/{0,1}OPEN' .agent/TASK_LEDGER.md`
   is the manual fallback if the shared tool is unavailable. A task ID mentioned in
   a narrative field (e.g. a heartbeat `last_completed`) is a mention, not a claim.
   An absent liveness signal is UNKNOWN — never treat it as live.

   **Claims-first (B5-0657, human-approved 2026-09-27).** Both census tools read
   `.agent/CLAIMS/` before reporting and mark any row whose claim is not provably
   stale as `suppressed-live-claim` / "NOT A DEFECT REPORT". A row another agent is
   mid-repair on reads as a *transient* state belonging to a **different defect
   class** than its committed form — B5-0564/B5-0565 both read 6 pipes under live
   claim B5-0592 while `git show d8216afa` confirms the committed form was 8 pipes
   with a leading double pipe. **Re-census a suppressed row after its claim is
   released, before reporting any defect on it.** Note that the `rg` manual fallback
   above does **not** apply this rule: any structural finding it produces on a row
   under a live claim is a candidate, not a defect report. Protocol:
   `docs/proposals/live-repair-aware-ledger-census-protocol.md`.
5. Pick the highest-priority `OPEN` task with no live claim.
6. Claim it atomically with the shipped creator:

   ```
   powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/new-claim.ps1 -TaskId B5-NNNN -AgentId "<agent-id>" -Scope "<scope>" -Javac 1.8.0_292
   ```

   **The tool is the default claim path (human-approved 2026-10-01, B5-1915;
   originating proposal `docs/proposals/tool-stamped-claim-default-proposal.md`).**
   It refuses an existing claim path, stamps `started_utc` from the current UTC
   clock at write time, refuses an orphan claim against a non-`OPEN` row, and
   accepts the write only after the payload is verified not to be ahead of the
   real clock and to agree with the new file's mtime. Creating the file **is** the
   claim, so if the file already exists the task is taken: never overwrite or
   delete another agent's claim. **Treat a non-zero exit as no claim** — record
   the explicit error and stop that item rather than substituting an
   agent-authored timestamp.

   `-Scope` may be repeated or comma-separated; from a shell that does not
   preserve array syntax, pass it comma-separated in one argument.

   **Absence of the claim file is NECESSARY BUT NOT SUFFICIENT.** Re-read the
   candidate row immediately before writing and confirm it still reads `OPEN`.
   A claim against a `DONE`/`VOID`/`SUPERSEDED`/`BLOCKED` row is an orphan:
   release it, do not work it. It can be neither offered nor completed, and the
   owner is usually not at fault — no rule used to tell them. (B5-0622: a live
   agent with a fresh heartbeat held two such claims on rows another agent had
   already closed.)
7. Work ONLY inside the claimed scope. Small diffs. Java 6 only.
   `b5ccg/src-java8-archive/` is frozen. No external libs without human approval.
8. Verify: `compile.bat/sh` green. On red, mark task `BLOCKED` with the log
   excerpt and release your claim.
9. Finish: update `.agent/TASK_LEDGER.md` row, append `docs/DECISIONS.md` entry, write
   `.agent/REPORTS/<date>-<agent-id>-<task-id>.md` (with `author_llm`), delete
   your claim file, refresh `.agent/HEARTBEATS/<agent-id>.json`. When editing
   the ledger, preserve the table pipes exactly — never add or remove a `|`.
   **After writing any row of your own, run the duplicate-ID census** — a
   pre-write "is this ID free?" check is necessary and *not sufficient*, because
   two agents can both measure the same ID free inside the same window (the thing
   it races is another reader, not a stale file):

   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/run-dup-census.ps1
   ```

   **Prefer the shipped wrapper over the inline one-liner** (B5-0777). The
   one-liner below is a *bash* regex: dropped into PowerShell its single-quoted
   string terminates at the first `|`, so the wrapper files that carried it
   failed to **parse** rather than to run, and this gate had no working
   implementation. The wrapper keeps the identical rule and adds an exit code —
   `0` clean, `1` duplicate found, `2` ledger missing or unreadable. Exit `2` is
   deliberately distinct from `0`: a census that could not read the ledger must
   never report as a clean ledger.

   **The wrapper is now the only documented form, inline or otherwise** (B5-1541).
   The inline one-liner was withdrawn as a documented fallback, not because the
   rule was wrong but because the *delivery* was: it is PowerShell syntax with no
   `powershell` prefix, so an agent whose only shell is bash — which is what the
   default `freebuff` CLI gives every command on this host — copy-pastes it and
   gets `Select-String: command not found`. A gate an agent cannot execute is
   not a gate. If `run-dup-census.ps1` is genuinely unavailable, report that; do
   not hand-roll a replacement census (see step 4 on hand-rolled censuses).

   **The exit code is the contract; stdout carries an allowed banner. Empty
   output is NOT the pass condition and "Empty output is the pass condition" was
   withdrawn as wrong on 2026-10-01 (B5-1732).** Measured against the shipped
   wrapper: a clean ledger prints **3 lines** (PASS / the 0-duplicate line / the
   encoding receipt) and exits 0; a ledger with one deliberate duplicate prints
   **4 lines** (the ID and count, then FAIL, the see-above line, the receipt) and
   exits 1. So an agent scanning stdout for emptiness reads a PASS banner as a
   finding. Judge the **exit code**, and read stdout only for the evidence:
   `0` clean, `1` duplicate found, `2` ledger missing or unreadable. This keeps
   the banner, which is deliberate -- a gate that prints nothing on success is
   the silent-green shape recorded in B5-1725, where a detector exited 0 while
   emitting no output at all. A duplicate ID is **not cosmetic**: the
   queue keys task status by ID, so the second row silently overwrites the first
   and one task becomes invisible to the gate and lane logic. If you collided,
   do **not** renumber into the slot the other writer just vacated — that
   deadlocks, because they will usually move there too. Diverge to a
   non-adjacent ID and leave their row byte-identical. (B5-0622; B5-0618.)

   Pipe integrity rides with the same post-write check (B5-0621): every row
   you write carries a single leading pipe and exactly 7 pipes, with no `|`
    character inside any note cell (B5-0435). Prove it with the shipped
    detector —
    `powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/ledger-query.ps1 -Status "*"`
    — which prints `pipeCount` and `doubleLead` per row; your row must read
    `7` / `no`. A `doubleLead yes` row is still parseable (the runner has
    tolerated it since B5-0613) but it is still defective: repair the lead
    pipe, classify any remaining excess by offset as structural or content
    per B5-0568, preserve every content pipe byte-identically, and never
    normalise-to-seven blindly.

    **Citation presence rides with the same post-write check (B5-1927).** The two
    checks above verify *structure* — unique IDs, correct pipe counts. Neither
    verifies *presence*: that the report and pattern you just cited actually exist
    as files. Six DONE rows were found citing files that had never been created,
    and no standing instrument reported it. After writing your `DECISIONS.md`
    entry and your report/pattern, run:

    ```
    powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/check-citations.ps1
    ```

    Exit `0` clean, `1` dangling citation found, `2` a source file was
    unreadable — and `2` is a false pass that must never be read as clean. Each
    finding names the source line, the reference, and either the likely target
    (same basename elsewhere — usually a wrong path or namespace) or an explicit
    statement that nothing with that basename exists, meaning the artifact was
    never filed. **If it reports your own citation as dangling, fix it before you
    close**: a citation to a file you did not write is the defect.

    It is deliberately blind to three things, which are counted and printed
    rather than silently dropped: globs (`*.md`), lossy renderings where `:` or
    `/` was mangled in transit, and paths containing characters NTFS forbids. The
    colon case matters here — `solar-pro4:free` is a real `agent_id` and `:` is
    illegal in a Windows filename, so its pattern directory is `solar-pro4-free`.
    A false positive on any of these trains you to ignore the tool.

    It is instrument 7 of the standing verification battery
        (`powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/run-verification-battery.ps1`),
    which runs every gate in this step in one command. Prefer the battery when you
    want the whole set; run this one directly when you have just cited something
    and want the answer in one second.

    **A bare PowerShell cmdlet in instruction position is a finding, not a
    style note (B5-1541, enforced by B5-1733).** If you edit any `.agent/*.md`
    and add a fenced code block, run:

    ```
    powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/detect-bare-powershell-cmdlet.ps1
    ```

    Exit `0` clean, `1` at least one bare cmdlet, `2` root missing or unreadable
    — and `2` is a false pass that must never be read as clean. It fires only on
    a line that (a) is inside a fenced block, (b) BEGINS with the cmdlet, and
    (c) is followed by a parameter, `{` or `.` — so a mention inside prose does
    not fire, which is the B5-1541 discipline of anchoring on the invocation
    rather than on the bare name. A **deliberate counter-example keeps the bare
    line and adds `# FAILS` (or `# b5-1733 exempt`) on it**; that annotation is
    the exemption, and the repo already carries one such line at
    `.agent/SHELL.knowledge.md` line 66. Without the annotation the detector is
    red, and the fix is to change the command, never to edit the tool.
    Prove the tool can still fail with
    `-SelfTest`, which feeds it the pre-B5-1541 lines recovered from commit
    `02716363` and asserts they are caught.

    **Claims-first applies here too (B5-0657).** The detector prints a
    `defectReport` column and a `CLAIMS-FIRST` footer. If your row reads
    `suppressed-live-claim`, its `7` / `no` reading was taken while another
    agent held a live claim on that row, so it is **not a defect report** —
    re-run the detector after the claim is released and judge the row then. Your
    own row, the one you just wrote under your own claim, is the exception that
    proves the rule: you know it is yours and complete, so judge it directly.
10. Claims older than 30 min are stale: you may reap one ONLY after noting
    the reaping in `.agent/TASK_LEDGER.md`. Never touch live claims or heartbeats.
    Staleness is judged on the THREE-SIGNAL rule (`.agent/HEARTBEATS/README.md`
    is canonical): a claim is LIVE while the NEWEST of its claim age, its
    owner's heartbeat, and its report mtime falls inside the TTL — never
    judge a claim on its timestamp alone (B5-0660).
11. Shared pattern store (standing convention, B5-0430): every close-out report
   gains a one-line **Reusable lesson** item, and the author files it as a
   Markdown record under `.agent/PATTERNS/<agent-id>/` (front matter with
   `author_llm`; supersede-never-rewrite — a corrected pattern is a NEW file
   that links the old one). Agents read all namespaces but write only their
   own. Boot skim: when reading this file, also glance at the newest records
   across namespaces so prior lessons shape your work before you claim.
