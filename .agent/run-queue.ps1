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

  Implausible started_utc refusal (B5-0653): a started_utc that parses but
  cannot be a just-now claim - exactly midnight UTC or at/before the epoch -
  makes Test-LiveClaim DECLINE TO OFFER the task with a loud warning naming
  the task id, owner and value. Never auto-repaired (editing another agent's
  claim is forbidden); never silently passed (a silent pass is how the
  original defect hid). Ambiguity resolves toward NOT OFFERING: a wrongly-
  stale live claim costs a collision and destroyed work, a wrongly-implausible
  claim costs one blocked task. Accepted cost: a genuine claim written in the
  exact second of midnight, ~1 in 86400, is declined until re-claimed.

  Claims-first census rule (B5-0657, human-approved 2026-09-27): the three
  structural warnings Get-LedgerRows emits are DEFECT REPORTS, so they are
  suppressed on any row whose claim is not provably stale and re-censused after
  the claim releases. A row mid-repair reads as a transient state belonging to a
  different defect class than its committed form - B5-0564/B5-0565 both read 6
  pipes under live claim B5-0592 while the committed form was 8 pipes with a
  leading double pipe. See Get-CensusSuppression and
  docs/proposals/live-repair-aware-ledger-census-protocol.md.

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

function Get-CensusSuppression {
  # Claims-first census suppression (B5-0657, human-approved 2026-09-27,
  # docs/proposals/live-repair-aware-ledger-census-protocol.md).
  # taskId -> owner for every claim that is NOT provably stale.
  #
  # Why this runner needs it: Get-LedgerRows below is a structural census of the
  # ledger, and its three warnings ARE defect reports. Run while another agent holds
  # a live claim on the very row being censused, they manufacture false defects,
  # because a row mid-repair reads as a transient state belonging to a DIFFERENT
  # defect class than its committed form. Recorded instance: B5-0564 and B5-0565 both
  # read 6 pipes mid-repair under live claim B5-0592, which looks exactly like the
  # missing-trailing-delimiter class, while git show d8216afa confirms the committed
  # form was 8 pipes with a leading double pipe. This is the mirror image of the
  # pre-write-grep lesson: a pre-write census is not a defect report either.
  #
  # Copied verbatim in semantics from Get-CensusSuppression in
  # .agent/tools/ledger-query.ps1, for the same reason Get-NormName is copied there:
  # the two census tools must agree about which rows are reportable, and a second
  # hand-rolled variant is how they came to disagree in the first place. The
  # shared-library leg of docs/proposals/tool-rule-convergence-proposal.md section 6
  # is what retires this duplication; until it lands, the copy is the fix and
  # .agent/tools/census-crosscheck.ps1 is what proves the copies still agree.
  #
  # "Provably stale" means every DETERMINABLE signal is older than the claim's own
  # ttl_min. An absent owner heartbeat is absent information and is never read as
  # staleness: that is the B5-0609 defect class, where a lookup matching nothing
  # returned -1 and -1 compares as younger than any TTL, manufacturing LIVE out of
  # an absent signal. Fail-safe direction is silence, because the protocol's remedy
  # for a suppressed row is re-census after release, not a report.
  # B5-0660: the signal set is now the full THREE-signal triad (claim,
  # owner heartbeat, and the newest report matching the task id), matching
  # Test-LiveClaim so the offer decision and the census suppression read the
  # same evidence. A fresh close-out report on a task whose claim and
  # heartbeat both lag is liveness evidence.
  $sup = @{}
  if (-not (Test-Path -LiteralPath $ClaimsDir)) { return $sup }
  $now = (Get-Date).ToUniversalTime()
  $hb = Get-HeartbeatIndex
  foreach ($f in (Get-ChildItem -LiteralPath $ClaimsDir -Filter 'B5-*.json' -ErrorAction SilentlyContinue)) {
    $id = $f.BaseName
    $owner = $null
    $started = $null
    $ttl = 30
    try {
      $j = Get-Content -LiteralPath $f.FullName -Raw | ConvertFrom-Json
      $owner = [string]$j.agent_id
      if ($j.ttl_min) { $ttl = [int]$j.ttl_min }
      if ($j.started_utc) {
        try {
          $started = [System.DateTime]::Parse(
            [string]$j.started_utc,
            [System.Globalization.CultureInfo]::InvariantCulture,
            [System.Globalization.DateTimeStyles]::RoundtripKind).ToUniversalTime()
        } catch { $started = $null }
      }
    } catch {
      $sup[$id] = '<unreadable claim file>'
      continue
    }
    if (-not $started) { $started = $f.LastWriteTimeUtc }
    if ($started -gt $now) { $sup[$id] = $owner; continue }   # future-dated clock skew: not a defect
    $newest = $started
    if ($owner) {
      $k = Get-NormName $owner
      if ($hb.ContainsKey($k) -and $hb[$k] -gt $newest) { $newest = $hb[$k] }
    }
    $reportsDir = Join-Path (Split-Path -Parent $AgentDir) 'REPORTS'
    if (Test-Path -LiteralPath $reportsDir) {
      $pattern = '*' + $id + '*.md'
      Get-ChildItem -LiteralPath $reportsDir -Filter $pattern -File -ErrorAction SilentlyContinue | ForEach-Object {
        if ($_.LastWriteTimeUtc -gt $newest) { $newest = $_.LastWriteTimeUtc }
      }
    }
    if (($now - $newest).TotalMinutes -lt $ttl) { $sup[$id] = $owner }
  }
  return $sup
}

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
  # Claims-first suppression set (B5-0657), computed once per call rather than per
  # row. Read BEFORE any warning below is emitted, because those warnings are defect
  # reports and a defect report on a row someone is mid-repair on is a false positive.
  $sup = Get-CensusSuppression

  # Self-check. Compare against a DELIBERATELY PERMISSIVE pattern, not the pattern
  # under test: the previous check counted matches with the same regex that built
  # $rows, so it compared a set against itself and could never detect a row the
  # regex could not see. It reported 312==312 while 9 rows carried a corrupt status.
  $permissive = 0
  foreach ($ln in (Get-Content -LiteralPath $Ledger)) {
    if ($ln -match '^\|+\s*B5-[0-9]{4}[a-z]?\s*\|') { $permissive++ }
  }
  if ($permissive -ne $rows.Count) {
    # This check is an aggregate COUNT comparison, so the offending row cannot be
    # named from it. When a live claim exists the honest reading is "this mismatch
    # may be a row mid-repair", not "the ledger is broken" -- so it is reported as
    # not-a-defect-report and the suppressed rows are named for re-census.
    if ($sup.Count -gt 0) {
      Write-Warning ("Get-LedgerRows: returned " + $rows.Count + " rows but a permissive scan found " + $permissive + " candidate row lines. NOT A DEFECT REPORT: " + $sup.Count + " row(s) hold a non-stale claim (" + (($sup.Keys | Sort-Object) -join ', ') + ") and a row mid-repair can move this count. Re-census those rows after the claim releases before treating this as a real defect.")
    } else {
      Write-Warning ("Get-LedgerRows: returned " + $rows.Count + " rows but a permissive scan found " + $permissive + " candidate row lines -- a row filter is silently hiding rows.")
    }
  }
  $noStatus = @($rows | Where-Object { -not $_.Status })
  $noStatusReported = @($noStatus | Where-Object { -not $sup.ContainsKey($_.Id) })
  $noStatusSuppressed = @($noStatus | Where-Object { $sup.ContainsKey($_.Id) })
  if ($noStatusReported.Count -gt 0) {
    Write-Warning ("Get-LedgerRows: " + $noStatusReported.Count + " row(s) parsed with no recognisable status: " + (($noStatusReported | ForEach-Object { $_.Id }) -join ', '))
  }
  if ($noStatusSuppressed.Count -gt 0) {
    Write-Warning ("Get-LedgerRows: " + $noStatusSuppressed.Count + " row(s) with no recognisable status are UNDER A LIVE CLAIM and are NOT a defect report: " + (($noStatusSuppressed | ForEach-Object { $_.Id }) -join ', ') + ". Re-census after the claim releases.")
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
    # A duplicate ID is only actionable if no live claim covers the row: under a live
    # claim the second row is most likely a half-written row the owner is mid-repair
    # on, and reporting it would manufacture a defect (B5-0657). Genuine collisions
    # still print in full, with the same instruction as before.
    $dupLive = @($dupIds | Where-Object { $sup.ContainsKey($_.Name) })
    $dupReal = @($dupIds | Where-Object { -not $sup.ContainsKey($_.Name) })
    if ($dupReal.Count -gt 0) {
      $dupText = (($dupReal | ForEach-Object { $_.Name + ' x' + $_.Count }) -join ', ')
      Write-Warning ("DUPLICATE TASK ID: " + $dupText + " -- a duplicate ID silently drops a task from the queue, because status is keyed by ID and the last row wins. Exactly one writer should renumber, and MUST diverge to a NON-ADJACENT id: renumbering into the slot the other writer just vacated deadlocks, since they will usually move there too. Leave the other row byte-identical. See 00_BOOT.md step 9 and B5-0622.")
    }
    if ($dupLive.Count -gt 0) {
      Write-Warning ("DUPLICATE TASK ID, NOT A DEFECT REPORT: " + ((($dupLive | ForEach-Object { $_.Name + ' x' + $_.Count }) -join ', ')) + " -- these row(s) hold a non-stale claim, so the duplicate is most likely a half-written row being repaired. Re-census after the claim releases before renumbering anything.")
    }
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
  # Liveness = the NEWEST of the THREE signals (B5-0660, completing the
  # B5-0597 three-signal rule): (a) the claim's own started_utc (falling back
  # to the claim file's mtime when that will not parse), (b) the OWNER's
  # heartbeat mtime -- matched through Get-NormName so 'solar-pro4:free'
  # finds 'solar-pro4<U+2028>free.json' -- and (c) the NEWEST report mtime
  # matching the task id in .agent/REPORTS/. Whichever is youngest decides.
  # History: the original read started_utc ALONE (B5-0649 closed that); then
  # claim+heartbeat (two signals); a report-only task's close-out lands in
  # REPORTS and keeps the claim honest even when both other signals lag, so
  # the third signal completes the triad the HEARTBEATS README defines.
  # B5-0660 UNKNOWN semantics for the binary offer decision:
  #   - owner heartbeat ABSENT -> the claim-age fallback stays (documented
  #     B5-0649 divergence) BUT the verdict is recorded as UNKNOWN-with-
  #     reason and reported in the warning line; a heartbeat-less claim is
  #     still offered on its claim age so it cannot block forever.
  #   - claim file UNPARSEABLE -> NOT offerable (UNKNOWN is never LIVE).
  #   - implausible started_utc -> refused per B5-0653 (below).
  param([string]$TaskId)
  $claimPath = Join-Path $ClaimsDir ($TaskId + '.json')
  if (-not (Test-Path -LiteralPath $claimPath)) { return $false }
  try {
    $j = Get-Content -LiteralPath $claimPath -Raw | ConvertFrom-Json
    $ttl = 30
    if ($j.ttl_min) { $ttl = [int]$j.ttl_min }
    $now = (Get-Date).ToUniversalTime()

    $started = $null
    $startedParsed = $null
    if ($j.started_utc) {
      try {
        $started = [System.DateTime]::Parse(
          [string]$j.started_utc,
          [System.Globalization.CultureInfo]::InvariantCulture,
          [System.Globalization.DateTimeStyles]::RoundtripKind).ToUniversalTime()
        $startedParsed = $started
      } catch { $started = $null }
    }
    if (-not $started) { $started = (Get-Item -LiteralPath $claimPath).LastWriteTimeUtc }

    # B5-0653: an IMPLAUSIBLE started_utc is a data-quality refusal, not a
    # liveness verdict. A value that parses but cannot be a just-now claim --
    # exactly midnight UTC (the placeholder default) or at/before the Unix
    # epoch -- must not be consumed as the claim's age, because a two-signal
    # liveness check is only as good as its weaker signal. Direction rule:
    # every ambiguity resolves toward NOT OFFERING, since wrongly calling a
    # live claim stale costs a collision and destroyed work, while wrongly
    # calling an implausible claim stale costs one blocked task a human
    # clears in seconds. We decline to offer and warn loudly, naming the id,
    # the owner and the value; we never repair or rewrite the claim file
    # (editing another agent's claim is forbidden). Accepted cost: a genuine
    # claim written in the exact second of midnight UTC, roughly 1 in 86400,
    # is declined until the owner re-claims with a real timestamp.
    if ($null -ne $startedParsed) {
      $unixEpoch = [System.DateTime]::new(1970, 1, 1, 0, 0, 0, [System.DateTimeKind]::Utc)
      $isMidnight = ($startedParsed.Hour -eq 0 -and $startedParsed.Minute -eq 0 -and $startedParsed.Second -eq 0)
      if ($isMidnight -or ($startedParsed -le $unixEpoch)) {
        $why = "[B5-0653] task " + $TaskId + " NOT OFFERED: claim started_utc " +
          "[" + [string]$j.started_utc + "] owner [" + [string]$j.agent_id + "] " +
          "parses but is IMPLAUSIBLE as a just-now claim (midnight/epoch placeholder). " +
          "Re-claim with the actual current UTC time."
        Write-Warning $why
        # Treated as LIVE (unstealable), not as free: returning $false here
        # would hand the task out. The candidate-list filter in
        # Get-ClaimableOpenTasks (Get-ImplausibleClaimTaskIds) is the primary
        # refusal; this path is the backstop for any other caller.
        return $true
      }
    }
    $newest = $started

    # Signal (c): the NEWEST report matching this task id. A close-out report
    # is liveness evidence exactly like a heartbeat: report-only tasks (no
    # compile, quick execution) finish in a burst and their claim+heartbeat
    # can both lag while the work is genuinely done or in flight.
    $reportsDir = Join-Path (Split-Path -Parent $AgentDir) 'REPORTS'
    Write-Verbose ("[B5-0660] scanning reports at " + $reportsDir + " for " + $TaskId)
    $reportNewest = $null
    if (Test-Path -LiteralPath $reportsDir) {
      $pattern = '*' + $TaskId + '*.md'
      Get-ChildItem -LiteralPath $reportsDir -Filter $pattern -File -ErrorAction SilentlyContinue |
        ForEach-Object { if ($null -eq $reportNewest -or $_.LastWriteTimeUtc -gt $reportNewest) { $reportNewest = $_.LastWriteTimeUtc } }
    }
    if ($null -ne $reportNewest -and $reportNewest -gt $newest) { $newest = $reportNewest }

    $owner = [string]$j.agent_id
    if ($owner) {
      $hb = Get-HeartbeatIndex
      $key = Get-NormName $owner
      if ($hb.ContainsKey($key)) {
        if ($hb[$key] -gt $newest) { $newest = $hb[$key] }
      } else {
        # B5-0660: heartbeat ABSENT is UNKNOWN-with-reason, no longer a silent
        # fall-back. The claim-age fallback STAYS for the offer decision (a
        # heartbeat-less claim must not block its task forever), but the
        # uncertainty is REPORTED, not hidden.
        Write-Warning ("[B5-0660] task " + $TaskId + ": owner heartbeat UNKNOWN (no heartbeat file for [" + $owner + "]); deciding on claim/report age.")
      }
    }
    # NOTE the B5-0649 divergence note for the no-heartbeat case is retained:
    # ledger-query prints UNKNOWN; here the offer decision needs a boolean, so
    # the fallback is claim/report age -- but the UNKNOWN is now REPORTED.
    if ($started -gt $now) { return $true }  # future-dated (clock skew): treat as live
    return (($now - $newest).TotalMinutes -lt $ttl)
  } catch {
    # B5-0660: an UNPARSEABLE claim is UNKNOWN, and UNKNOWN is never LIVE.
    # Returning $true here kept a corrupted claim unstealable, which was safe
    # against collisions but silently ZEROED the liveness signal; a corrupted
    # claim now blocks the task loudly instead, per 00_BOOT step 10 as amended.
    Write-Warning ("[B5-0660] task " + $TaskId + ": claim file UNPARSEABLE -- UNKNOWN is never LIVE; task NOT offered until the claim is fixed or reaped by its owner.")
    return $false
  }
}

# B5-0653: scan claim files for a started_utc that parses but is IMPLAUSIBLE
# as a just-now claim -- exactly midnight UTC (the placeholder default) or
# at/before the Unix epoch. Returns the task ids whose claims carry one.
# We never repair or rewrite the claim file (editing another agent's claim is
# forbidden); the owner re-claims with a real timestamp and the task returns
# to the queue on the next pass.
function Get-ImplausibleClaimTaskIds {
  $ids = @()
  if (-not (Test-Path -LiteralPath $ClaimsDir)) { return $ids }
  $unixEpoch = [System.DateTime]::new(1970, 1, 1, 0, 0, 0, [System.DateTimeKind]::Utc)
  Get-ChildItem -LiteralPath $ClaimsDir -Filter 'B5-*.json' -ErrorAction SilentlyContinue | ForEach-Object {
    try {
      $c = Get-Content -LiteralPath $_.FullName -Raw | ConvertFrom-Json
      $s = $null
      if ($c.started_utc) {
        try {
          $s = [System.DateTime]::Parse(
            [string]$c.started_utc,
            [System.Globalization.CultureInfo]::InvariantCulture,
            [System.Globalization.DateTimeStyles]::RoundtripKind).ToUniversalTime()
        } catch { $s = $null }
      }
      if ($null -ne $s) {
        $isMidnight = ($s.Hour -eq 0 -and $s.Minute -eq 0 -and $s.Second -eq 0)
        if ($isMidnight -or ($s -le $unixEpoch)) {
          $ids += $_.BaseName
          Write-Warning (("[B5-0653] task " + $_.BaseName + " NOT OFFERED: claim started_utc [" +
            [string]$c.started_utc + "] owner [" + [string]$c.agent_id +
            "] parses but is IMPLAUSIBLE as a just-now claim (midnight/epoch placeholder). " +
            "Re-claim with the actual current UTC time."))
        }
      }
    } catch { }   # unreadable claim: never steal it, never warn here
  }
  return $ids
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
  # B5-0657 claims-first suppression: a row whose claim is not provably stale
  # is mid-repair by a live agent -- never offered, never a defect report.
  $sup = Get-CensusSuppression
  # B5-0653: a claim whose started_utc is IMPLAUSIBLE (midnight/epoch
  # placeholder) makes its task un-offerable. Excluded from the candidate
  # list here so it cannot head-slot every iteration; Test-LiveClaim also
  # refuses it as live so no other path hands it out. Both paths warn.
  $implausible = Get-ImplausibleClaimTaskIds
  $free = @()
  foreach ($r in $rows) {
    if ($implausible -contains $r.Id) { continue }
    if ($sup.ContainsKey($r.Id)) { continue }
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
scope, verify per the row's gate, then close out fully (.agent/TASK_LEDGER.md row,
docs/DECISIONS.md entry, .agent/REPORTS/<date>-<sanitised-agent-id>-__TASK_ID__.md
with one "Reusable lesson" line filed as a NEW file under
.agent/PATTERNS/<agent-id>/, delete your claim file, refresh your heartbeat on the
binding schema in .agent/HEARTBEATS/README.md). Single task, then exit. If the
gate is red from out-of-scope in-flight edits, mark BLOCKED per .agent/00_BOOT.md step 8 and
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
