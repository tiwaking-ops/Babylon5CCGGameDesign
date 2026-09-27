<#
.SYNOPSIS
  Queue runner for the Babylon 5 CCG autonomous-agent pipeline.

.DESCRIPTION
  Repeatedly invokes an agent CLI, ONE ledger task per invocation, until the
  queue is drained or progress stalls. Fresh process per task: a CLI crash
  only ever loses one task's in-memory work, and long-context bloat never
  accumulates. All coordination stays file-based (TASK_LEDGER / CLAIMS /
  HEARTBEATS), so resumed iterations lose nothing.

  Stop conditions (checked after every iteration):
    a) No OPEN task without a live claim remains -> "queue drained".
    b) $BlockedStreakLimit consecutive iterations flip nothing to DONE
       (e.g. every code task BLOCKED on the same red gate) -> stop.
    c) $MaxIterations reached -> stop (overseer re-invokes).

  Claim TTL rule (mirrors .agent/00_BOOT.md step 10 and the B5-0597
  three-signal lesson): a claim is LIVE when the NEWEST of its own
  started_utc and its owner's heartbeat mtime falls inside its ttl_min, and a
  live claim is never stolen. Both signals matter: reading started_utc alone
  declared a working agent free the moment it wrote a placeholder timestamp,
  and disagreed with .agent/tools/ledger-query.ps1 about the same claim. Future-
  dated claims (clock skew - seen before, e.g. B5-0317) are treated as live.
  Stale claims are left for the agent to reap per protocol, not reaped here.

.EXAMPLE
  # Dry run: show what would be claimed, invoke nothing.
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/run-queue.ps1 -DryRun

.EXAMPLE
  # Real run, default 10 iterations, custom CLI:
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/run-queue.ps1 `
    -AgentCli 'freebuff' -AgentArgs '--yes'
#>
param(
  [int]$MaxIterations = 10,
  [int]$BlockedStreakLimit = 3,
  [string]$AgentCli = 'freebuff',
  [string]$AgentArgs = '',
  [int]$CooldownSeconds = 5,
  [switch]$DryRun
)

$ErrorActionPreference = 'Stop'
$AgentDir  = $PSScriptRoot
$RepoRoot  = Split-Path -Parent $AgentDir
$Ledger    = Join-Path $AgentDir 'TASK_LEDGER.md'
$ClaimsDir = Join-Path $AgentDir 'CLAIMS'
$hbDir     = Join-Path $AgentDir 'HEARTBEATS'

$LedgerStatuses = @('OPEN','CLAIMED','DONE','BLOCKED','SUPERSEDED','VOID')

function Get-LedgerRows {
  $rows = @()
  foreach ($line in (Get-Content -LiteralPath $Ledger)) {
    # Anchor tolerantly: 1 or more leading pipes (double-pipe defect class) and an
    # optional letter suffix on the id (B5-0202c, B5-0329a, ...). The suffix class
    # was still invisible after the leading-pipe fix, because \d+ cannot match "c".
    if ($line -match '^\|+\s*(B5-[0-9]{4}[a-z]?)\s*\|') {
      $parts = $line -split '\|'
      if ($parts.Count -ge 4) {
        # Derive Id AND Status the same tolerant way. Reading a fixed cell index is
        # wrong on every double-pipe row: for "|| B5-0604 | OPEN | ..." the split
        # yields ['','',' B5-0604 ',' OPEN '], so $parts[2] is the ID, not the status.
        # That made Status unmatchable against 'OPEN', so the queue-drained stop
        # condition fired while claimable OPEN rows were on disk.
        $idPart = $null; $statusPart = $null
        foreach ($p in $parts) {
          $tp = $p.Trim()
          if ($null -eq $idPart -and $tp -match '^B5-[0-9]{4}[a-z]?$') { $idPart = $tp }
          if ($null -eq $statusPart -and $LedgerStatuses -contains $tp) { $statusPart = $tp }
          if ($idPart -and $statusPart) { break }
        }
        if ($idPart) {
          $rows += [pscustomobject]@{
            Id     = $idPart
            Status = $statusPart
          }
        }
      }
    }
  }
  # Self-check. Compare against a DELIBERATELY PERMISSIVE pattern, not the pattern
  # under test: the previous check counted matches with the same regex that built
  # $rows, so it compared a set against itself and could never detect a row the
  # regex could not see. It reported 312==312 while 9 rows carried a corrupt status.
  $permissive = 0
  foreach ($ln in (Get-Content -LiteralPath $Ledger)) {
    if ($ln -match '^\|+\s*B5-[0-9]{4}[a-z]?\s*\|') { $permissive++ }
  }
  if ($permissive -ne $rows.Count) {
    Write-Warning ("Get-LedgerRows: returned " + $rows.Count + " rows but a permissive scan found " + $permissive + " candidate row lines -- a row filter is silently hiding rows.")
  }
  $noStatus = @($rows | Where-Object { -not $_.Status })
  if ($noStatus.Count -gt 0) {
    Write-Warning ("Get-LedgerRows: " + $noStatus.Count + " row(s) parsed with no recognisable status: " + (($noStatus | ForEach-Object { $_.Id }) -join ', '))
  }
  # Distinct-ID assertion (B5-0622). The permissive check above compares two counts
  # of ROWS, so two well-formed rows sharing one task ID sail straight through it.
  # That class is not cosmetic: downstream, $statusOf[$r.Id] = $r.Status means the
  # second row silently overwrites the first, and one task stops existing as far as
  # the gate and lane tables are concerned - it can never be offered, and its
  # status is never read. Name the offenders so the collision is visible at census
  # time. This MUST NOT change exit behaviour for a healthy ledger.
  $dupIds = @($rows | Group-Object Id | Where-Object { $_.Count -gt 1 })
  if ($dupIds.Count -gt 0) {
    $dupText = (($dupIds | ForEach-Object { $_.Name + ' x' + $_.Count }) -join ', ')
    Write-Warning ("DUPLICATE TASK ID: " + $dupText + " -- a duplicate ID silently drops a task from the queue, because status is keyed by ID and the last row wins. Exactly one writer should renumber, and MUST diverge to a NON-ADJACENT id: renumbering into the slot the other writer just vacated deadlocks, since they will usually move there too. Leave the other row byte-identical. See 00_BOOT.md step 9 and B5-0622.")
  }
  return $rows
}

function Get-TaskNumber {
  # Numeric sort/lane key for a task ID. MUST tolerate the letter-suffixed id
  # class (B5-0202c, B5-0329a, B5-0330a, B5-0331a): Get-LedgerRows accepts that
  # shape, so every numeric use of the id has to accept it too. A bare
  # [int]($Id -replace 'B5-','') throws on '0202c' and took the whole runner
  # down on any OPEN suffixed row (B5-0624). Extract the leading digit run
  # instead, so 'B5-0202c' -> 202 and sorts with its numeric siblings.
  param([string]$Id)
  # Anchor on the number AFTER the B5- prefix. An unanchored '(\d+)' matches the
  # '5' inside 'B5' first, so EVERY id would score 5 and the sort would silently
  # degrade to file order -- a worse failure than the crash it replaced, because
  # nothing goes red.
  $m = [regex]::Match([string]$Id, '^B5-(\d+)')
  if ($m.Success) { return [int]$m.Groups[1].Value }
  return 0
}

function Get-NormName([string]$name) {
  # Punctuation-normalised key for joining an agent_id to a heartbeat FILENAME.
  # Copied verbatim from .agent/tools/ledger-query.ps1 (Get-NormName) on purpose:
  # the two tools must agree, and a second hand-rolled variant is how they came to
  # disagree in the first place. Strips colons, U+2028/U+2029, spaces, dashes,
  # underscores and dots; lowercases; keeps letters and digits only. This is what
  # lets 'solar-pro4:free' match the file 'solar-pro4<U+2028>free.json'.
  $sb = New-Object System.Text.StringBuilder
  foreach ($ch in $name.ToCharArray()) {
    $c = [int]$ch
    if ($c -eq 0x2028 -or $c -eq 0x2029) { continue }
    if ($ch -match '[\p{L}\p{N}]') { [void]$sb.Append($ch.ToString().ToLowerInvariant()) }
  }
  return $sb.ToString()
}

function Get-HeartbeatIndex {
  # normalised agent key -> NEWEST heartbeat mtime for that key. Rebuilt on every
  # call rather than cached: a single run may iterate many tasks over many minutes,
  # and a cache captured at start-up would report a growing agent as silent. ~30
  # files per call is not worth the staleness.
  $idx = @{}
  if (Test-Path -LiteralPath $hbDir) {
    Get-ChildItem -LiteralPath $hbDir -Filter "*.json" -ErrorAction SilentlyContinue | ForEach-Object {
      $key = Get-NormName $_.BaseName
      if (-not $idx.ContainsKey($key) -or $_.LastWriteTimeUtc -gt $idx[$key]) { $idx[$key] = $_.LastWriteTimeUtc }
    }
  }
  return $idx
}

function Test-LiveClaim {
  # Liveness = the NEWEST of (a) the claim's own started_utc, falling back to the
  # claim file's mtime when that will not parse, and (b) the OWNER's heartbeat mtime
  # -- matched through Get-NormName, so 'solar-pro4:free' finds
  # 'solar-pro4<U+2028>free.json'. Whichever is youngest decides. This mirrors
  # .agent/tools/ledger-query.ps1 exactly (lines 79-129) and implements the B5-0597
  # rule, which the previous version of this function ignored: it read started_utc
  # ALONE, so a real agent mid-task could be declared free the moment it wrote a
  # placeholder timestamp. Concretely, a live hermes run on B5-0631 wrote
  # started_utc 2026-09-27T00:00:00Z, which this function called 287 minutes stale
  # while ledger-query correctly called the same claim LIVE off a 7-minute heartbeat.
  # Two shipped tools, two truths about one claim; that is the bug this closes.
  param([string]$TaskId)
  $claimPath = Join-Path $ClaimsDir ($TaskId + '.json')
  if (-not (Test-Path -LiteralPath $claimPath)) { return $false }
  try {
    $j = Get-Content -LiteralPath $claimPath -Raw | ConvertFrom-Json
    $ttl = 30
    if ($j.ttl_min) { $ttl = [int]$j.ttl_min }
    $now = (Get-Date).ToUniversalTime()

    $started = $null
    if ($j.started_utc) {
      try {
        $started = [System.DateTime]::Parse(
          [string]$j.started_utc,
          [System.Globalization.CultureInfo]::InvariantCulture,
          [System.Globalization.DateTimeStyles]::RoundtripKind).ToUniversalTime()
      } catch { $started = $null }
    }
    if (-not $started) { $started = (Get-Item -LiteralPath $claimPath).LastWriteTimeUtc }
    $newest = $started

    $owner = [string]$j.agent_id
    if ($owner) {
      $hb = Get-HeartbeatIndex
      $key = Get-NormName $owner
      if ($hb.ContainsKey($key) -and $hb[$key] -gt $newest) { $newest = $hb[$key] }
    }
    # NOTE a deliberate, documented divergence from ledger-query: that tool reports
    # UNKNOWN when no heartbeat matches the owner, because it is a reporting tool and
    # UNKNOWN is a verdict it can print. Here the question is binary -- offer this
    # task, or not -- so an owner who never wrote a heartbeat at all falls back to
    # the claim's own age. Returning "not live" in that case would let one
    # heartbeat-less claim block its task forever, which is a worse failure than the
    # one being fixed. When NOTHING can be determined (unreadable claim) we still
    # refuse to hand the task out, per 00_BOOT step 10.
    if ($started -gt $now) { return $true }  # future-dated (clock skew): treat as live
    return (($now - $newest).TotalMinutes -lt $ttl)
  } catch {
    return $true  # unreadable claim file: do not steal it
  }
}

function Get-ClaimableOpenTasks {
  # Prerequisite gates (from the QUEUE overseer notes in TASK_LEDGER.md):
  # a task is offered only when every listed predecessor reads DONE.
  $Prereqs = @{
    'B5-0368' = @('B5-0367');
    'B5-0370' = @('B5-0368');
    'B5-0371' = @('B5-0368', 'B5-0369');
    'B5-0372' = @('B5-0362', 'B5-0364', 'B5-0365', 'B5-0366');
    'B5-0373' = @('B5-0369');
    'B5-0374' = @('B5-0369');
    'B5-0377' = @('B5-0362', 'B5-0364', 'B5-0365', 'B5-0366');
    'B5-0378' = @('B5-0370', 'B5-0371');
    'B5-0384' = @('B5-0362');
    'B5-0389' = @('B5-0362', 'B5-0364', 'B5-0365', 'B5-0366')
  }
  $statusOf = @{}
  foreach ($r in (Get-LedgerRows)) { $statusOf[$r.Id] = $r.Status }
  $rows = Get-LedgerRows | Where-Object { $_.Status -eq 'OPEN' }
  $free = @()
  foreach ($r in $rows) {
    if (Test-LiveClaim -TaskId $r.Id) { continue }
    $gated = $false
    if ($Prereqs.ContainsKey($r.Id)) {
      foreach ($p in $Prereqs[$r.Id]) {
        if ($statusOf[$p] -ne 'DONE') { $gated = $true; break }
      }
    }
    if ($gated) {
      Write-Host ("Skipping gated " + $r.Id + " (predecessor not DONE).")
    } else {
      $free += $r
    }
  }
  $sorted = @($free | Sort-Object { Get-TaskNumber $_.Id })
  # Serialize the shared-code lanes (one writer per lane, lowest number first):
  # engine/model 0367-0376, ai 0377-0378, ui 0379-0381. Only the lane head is
  # offered; docs/harness/report/proposal rows are lane-free. A lane with a
  # LIVE claim on any of its OPEN rows is occupied: offer nothing from it.
  $laneRanges = @{
    'eng' = 367..376;
    'ai'  = 377..378;
    'ui'  = 379..381
  }
  $occupied = @()
  foreach ($lane in $laneRanges.Keys) {
    foreach ($n in $laneRanges[$lane]) {
      $lid = 'B5-' + $n.ToString('0000')
      if ($statusOf.ContainsKey($lid) -and
          ($statusOf[$lid] -eq 'OPEN' -or $statusOf[$lid] -eq 'CLAIMED')) {
        if (Test-LiveClaim -TaskId $lid) { $occupied += $lane; break }
      }
    }
  }
  foreach ($lane in $occupied) {
    Write-Host ("Lane " + $lane + " occupied by a live claim; holding its tasks.")
  }
  $usedLanes = @()
  $picked = @()
  foreach ($r in $sorted) {
    $n = Get-TaskNumber $r.Id
    $lane = ''
    if ($n -ge 367 -and $n -le 376) { $lane = 'eng' }
    elseif ($n -ge 377 -and $n -le 378) { $lane = 'ai' }
    elseif ($n -ge 379 -and $n -le 381) { $lane = 'ui' }
    if ($lane -ne '' -and (($usedLanes -contains $lane) -or ($occupied -contains $lane))) { continue }
    if ($lane -ne '') { $usedLanes += $lane }
    $picked += $r
  }
  return $picked
}

$TaskPromptTemplate = @'
Boot per .agent/00_BOOT.md, then read .agent/AGENT_LOOP.md and execute its
IDENTITY AND FILENAMES rules and its close-out checklist for this one task:
__TASK_ID__ only. (The claim file was already checked by the runner - re-verify
.agent/CLAIMS/__TASK_ID__.json is absent before creating your own claim, and
re-read the row to confirm it still reads OPEN.) Work ONLY inside the claimed
scope, verify per the row's gate, then close out fully (TASK_LEDGER.md row,
docs/DECISIONS.md entry, .agent/REPORTS/<date>-<sanitised-agent-id>-__TASK_ID__.md
with one "Reusable lesson" line filed as a NEW file under
.agent/PATTERNS/<agent-id>/, delete your claim file, refresh your heartbeat on the
binding schema in .agent/HEARTBEATS/README.md). Single task, then exit. If the
gate is red from out-of-scope in-flight edits, mark BLOCKED per 00_BOOT step 8 and
release - do not fix outside your scope. Use your name and version as agent_id.
Do NOT read .agent/HANDOFF.md: it is superseded and its state section is stale.
'@

$stagnant = 0
for ($i = 1; $i -le $MaxIterations; $i++) {
  $free = Get-ClaimableOpenTasks
  if (-not $free -or $free.Count -eq 0) {
    Write-Output "[$i] Queue drained: no OPEN task without a live claim. Done."
    break
  }
  $pick = $free[0]
  $doneBefore = @(Get-LedgerRows | Where-Object { $_.Status -eq 'DONE' }).Count
  Write-Output "[$i] Claiming lane for $($pick.Id) ($($free.Count) claimable OPEN)."

  $prompt = $TaskPromptTemplate.Replace('__TASK_ID__', $pick.Id)
  if ($DryRun) {
    Write-Output "[$i] DRY RUN: would invoke '$AgentCli $AgentArgs' for $($pick.Id)."
    continue
  }

  $argList = @()
  if ($AgentArgs -ne '') { $argList += $AgentArgs }
  # Native-argument safety (B5-0628). Windows PowerShell 5.1 does NOT escape
  # embedded double quotes when handing an argument to a native executable: the
  # quotes act as argument delimiters, so a prompt containing them is split into
  # several argv entries and a subcommand-style CLI reads a trailing fragment as
  # a command name. Reproduced with `hermes -z` on this very prompt: a task
  # template containing the phrase "Reusable lesson" exited 2 with
  # "is not a `hermes` command", while the identical prompt with its double
  # quotes removed ran to completion and exited 0. Newlines alone were tested
  # separately and are SAFE, so line breaks are preserved and only the quotes
  # are neutralised - this is the minimal fix the evidence supports, not a
  # blanket rewrite of the prompt. Single quotes read the same to the model.
  $safePrompt = $prompt -replace '"', "'"
  $argList += $safePrompt
  & $AgentCli @argList
  $exitCode = $LASTEXITCODE
  Write-Output "[$i] Agent exit code: $exitCode."

  $doneAfter = @(Get-LedgerRows | Where-Object { $_.Status -eq 'DONE' }).Count
  if ($doneAfter -gt $doneBefore) { $stagnant = 0 }
  else {
    $stagnant++
    Write-Output "[$i] No DONE flip this iteration (stagnant streak: $stagnant/$BlockedStreakLimit)."
  }
  if ($stagnant -ge $BlockedStreakLimit) {
    Write-Output "[$i] Stopping: $BlockedStreakLimit consecutive iterations with no DONE flip. Likely a shared red gate - inspect compile output before re-running."
    break
  }
  if ($i -lt $MaxIterations) { Start-Sleep -Seconds $CooldownSeconds }
}
Write-Output "run-queue finished."
