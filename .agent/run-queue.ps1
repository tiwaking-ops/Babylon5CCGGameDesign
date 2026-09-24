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

  Claim TTL rule (mirrors .agent/00_BOOT.md step 9): a claim file younger
  than its ttl_min is LIVE and is never stolen. Future-dated claims
  (clock skew - seen before, e.g. B5-0317) are treated as live. Stale
  claims are left for the agent to reap per protocol, not reaped here.

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

function Get-LedgerRows {
  $rows = @()
  foreach ($line in (Get-Content -LiteralPath $Ledger)) {
    if ($line -match '^\|\s*(B5-\d+)\s*\|') {
      $parts = $line -split '\|'
      if ($parts.Count -ge 4) {
        $rows += [pscustomobject]@{
          Id     = $parts[1].Trim()
          Status = $parts[2].Trim()
        }
      }
    }
  }
  return $rows
}

function Test-LiveClaim {
  param([string]$TaskId)
  $claimPath = Join-Path $ClaimsDir ("$TaskId.json")
  if (-not (Test-Path -LiteralPath $claimPath)) { return $false }
  try {
    $j = Get-Content -LiteralPath $claimPath -Raw | ConvertFrom-Json
    $ttl = 30
    if ($j.ttl_min) { $ttl = [int]$j.ttl_min }
    $started = [System.DateTime]::Parse(
      [string]$j.started_utc,
      [System.Globalization.CultureInfo]::InvariantCulture,
      [System.Globalization.DateTimeStyles]::RoundtripKind).ToUniversalTime()
    $now = (Get-Date).ToUniversalTime()
    if ($started -gt $now) { return $true }  # future-dated (clock skew): treat as live
    return (($now - $started).TotalMinutes -lt $ttl)
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
  $sorted = @($free | Sort-Object { [int]($_.Id -replace 'B5-', '') })
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
    $n = [int]($r.Id -replace 'B5-', '')
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
Read .agent/HANDOFF.md and complete task __TASK_ID__ only: boot per 00_BOOT.md
(claim file already checked by the runner - re-verify .agent/CLAIMS/__TASK_ID__.json
is absent before creating your own claim), work ONLY inside the claimed scope,
verify per the row's gate, then close out fully (TASK_LEDGER.md row, docs/DECISIONS.md
entry, .agent/REPORTS/<date>-<agent-id>-__TASK_ID__.md, delete your claim file,
refresh your heartbeat). Single task, then exit. If the gate is red from
out-of-scope in-flight edits, mark BLOCKED per step 7 and release - do not fix
outside your scope. Use your name and version as agent_id.
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
  $argList += $prompt
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
