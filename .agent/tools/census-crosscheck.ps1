<#
.SYNOPSIS
  Read-only cross-check: .agent/run-queue.ps1 vs .agent/tools/ledger-query.ps1 (B5-0651).
.DESCRIPTION
  Task 1 of docs/proposals/tool-rule-convergence-proposal.md section 5. For every
  ledger row it computes each census tool's verdict BY THAT TOOL'S OWN LOGIC and
  diffs the two. Compared rules:

    row-visible    tolerant row regex: run-queue Get-LedgerRows (anchor ^\|+ plus
                   split-parts >= 4) vs ledger-query rowRegex (no parts floor).
    status         run-queue cell scan (first ID-shaped cell, first status-token
                   cell) vs ledger-query cell-after-ID.
    pipe-shape     leading-pipe run: run-queue tolerates any run silently (only a
                   global count warning) vs ledger-query flags doubleLead. Any
                   multi-lead row is therefore a disagreement by construction.
    norm-name      Get-NormName carried verbatim in both tools since B5-0649; the
                   two copies are executed separately and compared, so a future
                   one-sided edit turns this red.
    claim-liveness run-queue Test-LiveClaim (newest of started_utc-or-mtime and
                   owner heartbeat mtime, per-claim ttl_min, corrupt claim = do
                   not steal = live, future-dated = live) vs ledger-query
                   verdict (same newest-of-two-ages rule, fixed TtlMinutes,
                   corrupt claim = UNKNOWN never LIVE per B5-0953, no matching
                   heartbeat = UNKNOWN). Every one of those parentheticals is a
                   live divergence surface. History: until B5-0953 an owner with
                   no heartbeat file read UNKNOWN here and fell back to claim age
                   in run-queue -- the DELIBERATE B5-0649 divergence, reported
                   by this tool like any other disagreement, the operator
                   deciding. B5-0953 retired it: both tools now answer UNKNOWN
                   for a missing or unjoinable owner signal, and the claim is
                   unstealable, so this surface is closed rather than
                   forgiven.
    suppression    (B5-0658) each tool's own Get-CensusSuppression replicated
                   verbatim: both THREE-signal (claim+heartbeat+report) since
                   B5-0660/B5-0771/B5-0775, and both suppress on a missing or
                   unjoinable owner heartbeat since B5-0953 (previously
                   run-queue fell back to claim age there and read reportable
                   where ledger-query suppressed -- the B5-0481 divergence
                   B5-0953 exists to reconcile). Diffs suppression verdict per
                   row id; disagreement = divergence.

  Output: one line per disagreement as
    ID | rule | run-queue: value | ledger-query: value
  Exit 0 = tools agree. Exit 1 = any disagreement. Exit 2 = input missing.
  Strictly read-only: reads the ledger, CLAIMS and HEARTBEATS; writes nothing.

  Known non-goals (proposal Task 1 scope): keys 3 and 4 of the liveness
  protocol, the claims-first census protocol, refactoring either tool.

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/census-crosscheck.ps1
.EXAMPLE
  # Fixture run in an isolated temp tree (never the live tree):
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/census-crosscheck.ps1 `
    -LedgerPath $fx/ledger.md -ClaimsDir $fx/claims -HbDir $fx/heartbeats
#>
param(
  [string]$LedgerPath = "",
  [string]$ClaimsDir = "",
  [string]$HbDir = "",
  [int]$TtlMinutes = 30
)

$ErrorActionPreference = "Stop"
$FutureTimestampToleranceMinutes = 60
$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if ($LedgerPath -eq "") { $LedgerPath = Join-Path $repoRoot ".agent/TASK_LEDGER.md" }
if ($ClaimsDir -eq "") { $ClaimsDir = Join-Path $repoRoot ".agent/CLAIMS" }
if ($HbDir -eq "") { $HbDir = Join-Path $repoRoot ".agent/HEARTBEATS" }
$now = [DateTime]::UtcNow

if (-not (Test-Path -LiteralPath $LedgerPath)) { Write-Error "ledger not found: $LedgerPath"; exit 2 }

$LedgerStatuses = @("OPEN", "CLAIMED", "DONE", "BLOCKED", "SUPERSEDED", "VOID")

# --- run-queue Get-NormName, verbatim logic (run-queue.ps1 lines 131-145) ---
function Get-NormName-R([string]$name) {
  $sb = New-Object System.Text.StringBuilder
  foreach ($ch in $name.ToCharArray()) {
    $c = [int]$ch
    if ($c -eq 0x2028 -or $c -eq 0x2029) { continue }
    if ($ch -match '[\p{L}\p{N}]') { [void]$sb.Append($ch.ToString().ToLowerInvariant()) }
  }
  return $sb.ToString()
}

# --- ledger-query Get-NormName, verbatim logic (ledger-query.ps1 lines 65-74) ---
function Get-NormName-L([string]$name) {
  $sb = New-Object System.Text.StringBuilder
  foreach ($ch in $name.ToCharArray()) {
    $c = [int]$ch
    if ($c -eq 0x2028 -or $c -eq 0x2029) { continue }
    if ($ch -match '[\p{L}\p{N}]') { [void]$sb.Append($ch.ToString().ToLowerInvariant()) }
  }
  return $sb.ToString()
}

# B5-1002: encoding pinned to explicit UTF-8 (see the same note in
# ledger-query.ps1: a naive read reports 1600 C1 marks on docs/DECISIONS.md
# where an explicit UTF-8 read reports 0 on the same bytes).
$lines = [System.IO.File]::ReadAllLines($LedgerPath, [System.Text.Encoding]::UTF8)

# --- Tool R rows: run-queue Get-LedgerRows logic (run-queue.ps1 lines 54-82) ---
$rowsR = @{}
$leadR = @{}
foreach ($line in $lines) {
  if ($line -match '^\|+\s*(B5-[0-9]{4}[a-z]?)\s*\|') {
    $parts = $line -split '\|'
    if ($parts.Count -ge 4) {
      $idPart = $null; $statusPart = $null
      foreach ($p in $parts) {
        $tp = $p.Trim()
        if ($null -eq $idPart -and $tp -match '^B5-[0-9]{4}[a-z]?$') { $idPart = $tp }
        if ($null -eq $statusPart -and $LedgerStatuses -contains $tp) { $statusPart = $tp }
        if ($idPart -and $statusPart) { break }
      }
      if ($idPart) {
        $rowsR[$idPart] = $statusPart
        $lm = [regex]::Match($line, '^(\|+)')
        $leadR[$idPart] = $lm.Groups[1].Value.Length
      }
    }
  }
}

# --- Tool L rows: ledger-query rowRegex logic (ledger-query.ps1 lines 44-62) ---
$rowRegexL = [regex]'^(\|+)\s*(B5-[0-9]{4}[a-z]?)\s*\|([^|]*)'
$rowsL = @{}
$leadL = @{}
$pipesL = @{}
foreach ($line in $lines) {
  $m = $rowRegexL.Match($line)
  if (-not $m.Success) { continue }
  $id = $m.Groups[2].Value.Trim()
  $rowsL[$id] = $m.Groups[3].Value.Trim()
  $leadL[$id] = $m.Groups[1].Value.Length
  $pipesL[$id] = ([regex]::Matches($line, '\|')).Count
}

# --- Tool R claims: Test-LiveClaim inputs (run-queue.ps1 lines 174-200) ---
function Get-Verdict-R([string]$taskId) {
  $claimPath = Join-Path $ClaimsDir ($taskId + ".json")
  if (-not (Test-Path -LiteralPath $claimPath)) { return "unclaimed" }
  try {
    $j = Get-Content -LiteralPath $claimPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $ttl = 30
    if ($j.ttl_min) { $ttl = [int]$j.ttl_min }
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
    $ownerSignal = $null
    if ($owner) {
      if (Test-Path -LiteralPath $HbDir) {
        $key = Get-NormName-R $owner
        $best = [DateTime]::MinValue
        Get-ChildItem -LiteralPath $HbDir -Filter "*.json" -ErrorAction SilentlyContinue | ForEach-Object {
          if ((Get-NormName-R $_.BaseName) -eq $key) {
            if ($_.LastWriteTimeUtc -gt $best) { $best = $_.LastWriteTimeUtc }
          }
        }
        if ($best -ne [DateTime]::MinValue) { $ownerSignal = $best }
      }
    }
    # B5-0953: mirror the repaired Test-LiveClaim. The owner heartbeat is a
    # REQUIRED signal, so its absence (or an agent_id that cannot be joined to
    # one) is UNKNOWN -- never a claim-age fallback, which is the B5-0609
    # defect class in offer form. Verdict strings match ledger-query.ps1's
    # per-row verdicts so the diff below compares like with like; the report
    # scan is reached only when the required signal exists, because a report
    # is contributing-only and cannot rescue a missing required one.
    if ($null -eq $ownerSignal) {
      if ($owner) { return "unknown:no-heartbeat" }
      return "unknown:agent_id-unreadable"
    }
    if ($ownerSignal -gt $newest) { $newest = $ownerSignal }
    # B5-0660 reconcile: run-queue Test-LiveClaim is now THREE-signal (claim,
    # owner heartbeat, and the newest report matching the task id). Mirror the
    # report signal here so the two tools compare the same triad.
    # B5-0771: this was Join-Path $repoRoot 'REPORTS', the repo root rather than
    # .agent/REPORTS, so the guard below skipped the signal and this tool agreed
    # with a wrong run-queue instead of exposing the disagreement. Same correct
    # form as $ReportsDir at line 239 of this file.
    $reportsDir = Join-Path $repoRoot ".agent/REPORTS"
    if (Test-Path -LiteralPath $reportsDir) {
      $pattern = '*' + $taskId + '*.md'
      Get-ChildItem -LiteralPath $reportsDir -Filter $pattern -File -ErrorAction SilentlyContinue | ForEach-Object {
        if ($_.LastWriteTimeUtc -gt $newest) { $newest = $_.LastWriteTimeUtc }
      }
    }
    if (($started - $now).TotalMinutes -gt $FutureTimestampToleranceMinutes) {
      return ("unknown:started_utc-future-skew-over-" + $FutureTimestampToleranceMinutes + "-minutes")
    }
    if ($started -gt $now) { return ("live(started-future,ttl " + $ttl + ")") }
    $ageMin = [math]::Round(($now - $newest).TotalMinutes, 1)
    if ($ageMin -lt $ttl) { return ("live(age " + $ageMin + ",ttl " + $ttl + ")") }
    return ("stale(age " + $ageMin + ",ttl " + $ttl + ")")
  } catch {
    return "unknown:unreadable-claim"
  }
}

# --- Tool L claims + heartbeat index (ledger-query.ps1 lines 76-103) ---
$claimsL = @{}
if (Test-Path -LiteralPath $ClaimsDir) {
  Get-ChildItem -LiteralPath $ClaimsDir -Filter "B5-*.json" | ForEach-Object {
    try {
      $c = Get-Content -Raw -Encoding UTF8 -LiteralPath $_.FullName | ConvertFrom-Json
      $started = $null
      if ($c.started_utc) {
        try { $started = [DateTime]::Parse($c.started_utc).ToUniversalTime() } catch { $started = $null }
      }
      if (-not $started) { $started = $_.LastWriteTimeUtc }
      $claimsL[$_.BaseName] = [pscustomobject]@{
        Owner   = $c.agent_id
        Started = $started
      }
    } catch { }
  }
}
$hbL = @{}
if (Test-Path -LiteralPath $HbDir) {
  Get-ChildItem -LiteralPath $HbDir -Filter "*.json" | ForEach-Object {
    $key = Get-NormName-L $_.BaseName
    if (-not $hbL.ContainsKey($key) -or $_.LastWriteTimeUtc -gt $hbL[$key]) {
      $hbL[$key] = $_.LastWriteTimeUtc
    }
  }
}

function Get-Verdict-L([string]$taskId) {
  if (-not $claimsL.ContainsKey($taskId)) { return "unclaimed" }
  $claim = $claimsL[$taskId]
  $owner = $claim.Owner
  if (-not $owner) { return "unknown:agent_id-unreadable" }
  $normOwner = Get-NormName-L $owner
  if (-not $hbL.ContainsKey($normOwner)) { return "unknown:no-heartbeat" }
  if (($claim.Started - $now).TotalMinutes -gt $FutureTimestampToleranceMinutes) {
    return ("unknown:started_utc-future-skew-over-" + $FutureTimestampToleranceMinutes + "-minutes")
  }
  $claimAge = [math]::Round(($now - $claim.Started).TotalMinutes, 1)
  $hbAge = [math]::Round(($now - $hbL[$normOwner]).TotalMinutes, 1)
  $newestMin = [math]::Min([double]$claimAge, [double]$hbAge)
  # B5-0775: the THIRD signal, replicated verbatim from ledger-query.ps1 lines
  # 263-268. Until this line, Get-Verdict-L folded only claim age and heartbeat
  # age into newestMin while the rule it claims to replicate folds the newest
  # report mtime for this task id in as well, so the cross-check compared a
  # two-signal verdict against a three-signal one: on a task whose claim and owner
  # heartbeat are both outside the TTL while a same-id report is fresh, ledger-query
  # reads LIVE and this copy read STALE, and the tool reported a DIVERGENT that was
  # an artefact of the replica rather than a real disagreement. The report index is
  # the same $reportsL this file already builds for Get-Suppression-L, so this is
  # the identical signal, not a paraphrase of it. Report is contributing-only: its
  # absence mid-task is the normal case, so the guard is a ContainsKey test and
  # never an absent-signal-as-fresh inversion.
  if ($reportsL.ContainsKey($taskId)) {
    $rptAge = [math]::Round(($now - $reportsL[$taskId]).TotalMinutes, 1)
    $newestMin = [math]::Min($newestMin, [double]$rptAge)
  }
  if ($newestMin -lt $TtlMinutes) { return ("live(claim " + $claimAge + ",hb " + $hbAge + ")") }
  return ("stale(claim " + $claimAge + ",hb " + $hbAge + ")")
}

function Get-Word([string]$verdict) {
  if ($verdict -eq "unclaimed") { return "unclaimed" }
  if ($verdict.StartsWith("live")) { return "live" }
  if ($verdict.StartsWith("stale")) { return "stale" }
  return "unknown"
}

# --- Rule 6: claims-first suppression (B5-0658) ---
# Replicate each tool's Get-CensusSuppression verbatim and diff the results.
# run-queue.ps1 Get-CensusSuppression: two-signal (claim + heartbeat), falls
#   back to claim age when no heartbeat exists for the owner.
# ledger-query.ps1 Get-CensusSuppression: three-signal (claim + heartbeat +
#   report), suppresses immediately when no heartbeat exists for the owner.

$ReportsDir = Join-Path $repoRoot ".agent/REPORTS"

# Build report index for ledger-query side (third signal per B5-0659).
$reportsL = @{}
if (Test-Path -LiteralPath $ReportsDir) {
  Get-ChildItem -LiteralPath $ReportsDir -Filter "*.md" -ErrorAction SilentlyContinue | ForEach-Object {
    $stem = $_.BaseName
    foreach ($m in [regex]::Matches($stem, 'B5-[0-9]{4}[a-z]?')) {
      $rid = $m.Value
      if (-not $reportsL.ContainsKey($rid) -or $_.LastWriteTimeUtc -gt $reportsL[$rid]) {
        $reportsL[$rid] = $_.LastWriteTimeUtc
      }
    }
  }
}

# Build heartbeat index for run-queue side (uses Get-NormName-R).
$hbR = @{}
if (Test-Path -LiteralPath $HbDir) {
  Get-ChildItem -LiteralPath $HbDir -Filter "*.json" -ErrorAction SilentlyContinue | ForEach-Object {
    $k = Get-NormName-R $_.BaseName
    if (-not $hbR.ContainsKey($k) -or $_.LastWriteTimeUtc -gt $hbR[$k]) {
      $hbR[$k] = $_.LastWriteTimeUtc
    }
  }
}

function Get-Suppression-R {
  # Replicates run-queue.ps1 Get-CensusSuppression (B5-0953 form): THREE-signal
  # (claim + heartbeat + report, per B5-0660/B5-0771), with the owner heartbeat
  # REQUIRED: a missing or unjoinable owner signal SUPPRESSES, never falls
  # back to claim age. Marker strings match Get-Suppression-L below exactly so
  # the diff compares like with like.
  param([string]$taskId)
  $claimPath = Join-Path $ClaimsDir ($taskId + ".json")
  if (-not (Test-Path -LiteralPath $claimPath)) { return $null }
  try {
    $j = Get-Content -LiteralPath $claimPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $ttl = 30
    if ($j.ttl_min) { $ttl = [int]$j.ttl_min }
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
    if ($started -gt $now) { return "suppressed(future-dated)" }
    $owner = [string]$j.agent_id
    if (-not $owner) { return "suppressed(unreadable-agent_id)" }
    $k = Get-NormName-R $owner
    if (-not $hbR.ContainsKey($k)) { return "suppressed(no-heartbeat)" }
    $newest = $started
    if ($hbR[$k] -gt $newest) { $newest = $hbR[$k] }
    # Signal (c): the NEWEST report matching this task id. A close-out report
    # is liveness evidence exactly like a heartbeat: report-only tasks (no
    # compile, quick execution) finish in a burst and their claim+heartbeat
    # can both lag while the work is genuinely done or in flight.
    if (Test-Path -LiteralPath $ReportsDir) {
      $pattern = '*' + $taskId + '*.md'
      Get-ChildItem -LiteralPath $ReportsDir -Filter $pattern -File -ErrorAction SilentlyContinue | ForEach-Object {
        if ($_.LastWriteTimeUtc -gt $newest) { $newest = $_.LastWriteTimeUtc }
      }
    }
    if (($now - $newest).TotalMinutes -lt $ttl) { return "suppressed($owner)" }
    return "reportable"
  } catch {
    return "suppressed(unreadable-claim)"
  }
}

function Get-Suppression-L {
  # Replicates ledger-query.ps1 Get-CensusSuppression (lines 160-230): three-
  # signal, suppresses immediately when no heartbeat for the owner exists.
  param([string]$taskId)
  $claimPath = Join-Path $ClaimsDir ($taskId + ".json")
  if (-not (Test-Path -LiteralPath $claimPath)) { return $null }
  try {
    $c = Get-Content -Raw -Encoding UTF8 -LiteralPath $claimPath | ConvertFrom-Json
    $owner = $c.agent_id
    $ttl = $TtlMinutes
    if ($c.ttl_min) { $ttl = [int]$c.ttl_min }
    $started = $null
    if ($c.started_utc) {
      try { $started = [DateTime]::Parse($c.started_utc).ToUniversalTime() } catch { $started = $null }
    }
    if (-not $started) { $started = (Get-Item -LiteralPath $claimPath).LastWriteTimeUtc }
    if ($started -gt $now) { return "suppressed(future-dated)" }
    if (-not $owner) { return "suppressed(unreadable-agent_id)" }
    $k = Get-NormName-L $owner
    if (-not $hbL.ContainsKey($k)) { return "suppressed(no-heartbeat)" }
    $claimAge = [math]::Round(($now - $started).TotalMinutes, 1)
    $hbAge = [math]::Round(($now - $hbL[$k]).TotalMinutes, 1)
    $newestMin = [math]::Min([double]$claimAge, [double]$hbAge)
    if ($reportsL.ContainsKey($taskId)) {
      $rptAge = [math]::Round(($now - $reportsL[$taskId]).TotalMinutes, 1)
      $newestMin = [math]::Min($newestMin, $rptAge)
    }
    if ($newestMin -lt $ttl) { return "suppressed($owner)" }
    return "reportable"
  } catch {
    return "suppressed(unreadable-claim)"
  }
}

# --- diff over the union of IDs ---
$allIds = @()
foreach ($id in $rowsR.Keys) { if ($allIds -notcontains $id) { $allIds += $id } }
foreach ($id in $rowsL.Keys) { if ($allIds -notcontains $id) { $allIds += $id } }
$allIds = @($allIds | Sort-Object)

$divs = @()
foreach ($id in $allIds) {
  $seenR = $rowsR.ContainsKey($id)
  $seenL = $rowsL.ContainsKey($id)
  if ($seenR -ne $seenL) {
    $vr = "hidden"
    if ($seenR) { $vr = "seen" }
    $vl = "hidden"
    if ($seenL) { $vl = "seen" }
    $divs += ("{0} | row-visible | run-queue: {1} | ledger-query: {2}" -f $id, $vr, $vl)
    continue
  }
  $stR = $rowsR[$id]
  if ($null -eq $stR) { $stR = "<none>" }
  $stL = $rowsL[$id]
  if ($stR -ne $stL) {
    $divs += ("{0} | status | run-queue: {1} | ledger-query: {2}" -f $id, $stR, $stL)
  }
  $lr = $leadR[$id]
  $ll = $leadL[$id]
  if ($lr -ne $ll) {
    $divs += ("{0} | pipe-shape | run-queue: lead-run {1} | ledger-query: lead-run {2}" -f $id, $lr, $ll)
  } elseif ($lr -gt 1) {
    $divs += ("{0} | pipe-shape | run-queue: multi-lead tolerated | ledger-query: doubleLead yes (pipes {1})" -f $id, $pipesL[$id])
  }
  $ivr = Get-Verdict-R $id
  $ivl = Get-Verdict-L $id
  if ((Get-Word $ivr) -ne (Get-Word $ivl)) {
    $divs += ("{0} | claim-liveness | run-queue: {1} | ledger-query: {2}" -f $id, $ivr, $ivl)
  }
}

# --- Rule 6 suppression diff (B5-0658) ---
# Compare each tool's own Get-CensusSuppression verdict per row.
foreach ($id in $allIds) {
  $ivR = Get-Suppression-R $id
  $ivL = Get-Suppression-L $id
  if ($ivR -eq $null -and $ivL -eq $null) { continue }
  if ($ivR -ne $null -and $ivL -eq $null) {
    $divs += ("{0} | suppression | run-queue: {1} | ledger-query: not-in-census" -f $id, $ivR)
  } elseif ($ivR -eq $null -and $ivL -ne $null) {
    $divs += ("{0} | suppression | run-queue: not-in-census | ledger-query: {1}" -f $id, $ivL)
  } elseif ($ivR -ne $ivL) {
    $divs += ("{0} | suppression | run-queue: {1} | ledger-query: {2}" -f $id, $ivR, $ivL)
  }
}

# --- norm-name drift: every owner string and heartbeat stem through both copies ---
$names = @()
foreach ($f in (Get-ChildItem -LiteralPath $ClaimsDir -Filter "B5-*.json" -ErrorAction SilentlyContinue)) {
  try {
    $o = (Get-Content -Raw -Encoding UTF8 -LiteralPath $f.FullName | ConvertFrom-Json).agent_id
    if ($o -and ($names -notcontains $o)) { $names += $o }
  } catch { }
}
foreach ($f in (Get-ChildItem -LiteralPath $HbDir -Filter "*.json" -ErrorAction SilentlyContinue)) {
  if ($names -notcontains $f.BaseName) { $names += $f.BaseName }
}
foreach ($n in $names) {
  $nr = Get-NormName-R $n
  $nl = Get-NormName-L $n
  if ($nr -ne $nl) {
    $divs += ("- | norm-name | run-queue: '{0}' -> '{1}' | ledger-query: '{0}' -> '{2}'" -f $n, $nr, $nl)
  }
}

if ($divs.Count -eq 0) {
  Write-Output ("census-crosscheck: CONSISTENT -- {0} row(s), run-queue and ledger-query agree." -f $allIds.Count)
  # B5-1002: encoding receipt -- both replicated logics read explicit UTF-8.
  Write-Output "-- ENCODING: UTF-8 (explicit; ReadAllLines(path, UTF8) + Get-Content -Encoding UTF8) --"
  exit 0
}
foreach ($d in ($divs | Sort-Object)) { Write-Output $d }
Write-Output ("census-crosscheck: DIVERGENT -- {0} disagreement(s) across {1} row(s)." -f $divs.Count, $allIds.Count)
# B5-1002: encoding receipt -- both replicated logics read explicit UTF-8.
Write-Output "-- ENCODING: UTF-8 (explicit; ReadAllLines(path, UTF8) + Get-Content -Encoding UTF8) --"
exit 1
