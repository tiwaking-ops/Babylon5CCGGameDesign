<#
.SYNOPSIS
  Read-only ledger census + claim-liveness query tool (B5-0609).
.DESCRIPTION
  Prints one line per row of .agent/TASK_LEDGER.md matching an optional status
  filter (default OPEN) as: ID | status | pipeCount | doubleLead | claimOwner |
  claimAgeMin | hbAgeMin | verdict [reason] | defectReport

  Hard rules (B5-0609 spec):
    * Parses ONLY structured row-start fields (^|B5-xxxx|status|). Task IDs that
      appear inside narrative cells (e.g. heartbeat last_completed prose) are
      never treated as rows or claims.
    * The agent_id -> heartbeat filename join normalises punctuation on BOTH
      sides (colons, U+2028/U+2029, spaces, dashes, underscores stripped) so
      "solar-pro4:free" matches the heartbeat file "solar-pro4<U+2028>free.json".
    * Liveness = NEWEST of claim started_utc (or claim file mtime when
      unparseable) and the owner heartbeat file mtime, against the TTL
      (default 30 min). An absent signal prints UNKNOWN with a visible reason
      and is NEVER treated as fresh (a -1 age must not satisfy the test).
    * CLAIMS-FIRST (B5-0657, human-approved 2026-09-27): reads .agent/CLAIMS/
      before reporting and marks any row whose claim is not provably stale as
      `suppressed-live-claim` in the defectReport column, with a footer naming
      the suppressed rows. A row under repair reads as a transient state that
      belongs to a DIFFERENT defect class than its committed form, so reporting
      it manufactures false defects. The suppressed rows must be re-censused
      after the claim releases.
    * Strictly read-only: no ledger, claim, heartbeat, report or pattern writes.
.NOTES
  Manual fallback one-liner (capture-only rg, tolerates leading double pipe):
    rg -o --no-filename '^\|+\s*B5-[0-9]{4}[a-z]?\s*\|' .agent/TASK_LEDGER.md
.PARAMETER Status
  Filter on the status cell (default "OPEN"). Use "*" or "" for all rows.
.PARAMETER TtlMinutes
  Liveness window (default 30, per 00_BOOT step 2).
#>
param(
    [string]$Status = "OPEN",
    [int]$TtlMinutes = 30
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$ledger = Join-Path $repoRoot ".agent/TASK_LEDGER.md"
$claimsDir = Join-Path $repoRoot ".agent/CLAIMS"
$hbDir = Join-Path $repoRoot ".agent/HEARTBEATS"
$now = [DateTime]::UtcNow

if (-not (Test-Path -LiteralPath $ledger)) { Write-Error "ledger not found: $ledger"; exit 2 }

# --- row parsing: structured row-start cells only ---
$rowRegex = [regex]'^(\|+)\s*(B5-[0-9]{4}[a-z]?)\s*\|([^|]*)'
$statusFilter = if ($Status -eq "*" -or $Status -eq "") { $null } else { $Status }

$rows = @()
foreach ($line in [System.IO.File]::ReadAllLines($ledger)) {
    $m = $rowRegex.Match($line)
    if (-not $m.Success) { continue }
    $leading = $m.Groups[1].Value
    $id = $m.Groups[2].Value.Trim()
    $st = $m.Groups[3].Value.Trim()
    if ($statusFilter -and $st -ne $statusFilter) { continue }
    $pipeCount = ([regex]::Matches($line, '\|')).Count
    $rows += [pscustomobject]@{
        Id        = $id
        Status    = $st
        Pipes     = $pipeCount
        Double    = ($leading.Length -gt 1)
    }
}

# --- claim + heartbeat indexing (once) ---
function Get-NormName([string]$name) {
    # strip colons, U+2028/U+2029, spaces, dashes, underscores, dots; lowercase
    $sb = New-Object System.Text.StringBuilder
    foreach ($ch in $name.ToCharArray()) {
        $c = [int]$ch
        if ($c -eq 0x2028 -or $c -eq 0x2029) { continue }
        if ($ch -match '[\p{L}\p{N}]') { [void]$sb.Append($ch.ToString().ToLowerInvariant()) }
    }
    return $sb.ToString()
}

$claims = @{}
if (Test-Path -LiteralPath $claimsDir) {
    Get-ChildItem -LiteralPath $claimsDir -Filter "B5-*.json" | ForEach-Object {
        try {
            $c = Get-Content -Raw -LiteralPath $_.FullName | ConvertFrom-Json
            $started = $null
            if ($c.started_utc) {
                try { $started = [DateTime]::Parse($c.started_utc).ToUniversalTime() } catch { $started = $null }
            }
            if (-not $started) { $started = $_.LastWriteTimeUtc }
            $claims[$_.BaseName] = [pscustomobject]@{
                Owner   = $c.agent_id
                Started = $started
                FileT   = $_.LastWriteTimeUtc
            }
        } catch { }  # corrupt claim file -> simply no claim info
    }
}

$heartbeats = @{}
if (Test-Path -LiteralPath $hbDir) {
    Get-ChildItem -LiteralPath $hbDir -Filter "*.json" | ForEach-Object {
        $key = Get-NormName $_.BaseName
        if (-not $heartbeats.ContainsKey($key) -or $_.LastWriteTimeUtc -gt $heartbeats[$key]) {
            $heartbeats[$key] = $_.LastWriteTimeUtc
        }
    }
}

# --- claims-first census suppression (B5-0657) ---
function Get-CensusSuppression {
    # Live-repair-aware ledger census protocol, human-approved 2026-09-27
    # (docs/proposals/live-repair-aware-ledger-census-protocol.md). Returns
    # taskId -> owner for every claim that is NOT provably stale, so the census
    # never reports a row that another agent is mid-repair on as a defect.
    #
    # Grounding: a census taken while a live claim covers the row reads a
    # TRANSIENT state, and the transient state belongs to a different defect
    # class than the committed form. B5-0564/B5-0565 both read 6 pipes mid-repair
    # under claim B5-0592, which looks like a missing-trailing-delimiter defect;
    # git show d8216afa confirms the committed form was 8 pipes with a leading
    # double pipe. A naive pipe census reports two false defects.
    #
    # Keyed off the FILENAME, before JSON parsing, so a claim file this tool cannot
    # read is ALSO suppressed. An unreadable file is not evidence of a defect, and
    # the fail-safe direction here is toward silence: the protocol's own remedy for
    # a suppressed row is re-census after release, not a report.
    #
    # "Provably stale" means every DETERMINABLE signal is older than the TTL. An
    # absent owner heartbeat is absent information and is never read as staleness,
    # which is the B5-0609 defect class (a lookup matching nothing returned -1, and
    # -1 compares as younger than any TTL, manufacturing LIVE from an absent signal).
    #
    # The predicate is deliberately IDENTICAL to Test-LiveClaim in .agent/run-queue.ps1,
    # including the future-dated and unreadable-claim cases. Two tools disagreeing
    # about which rows are reportable would recreate exactly the defect class this
    # protocol exists to stop, and .agent/tools/census-crosscheck.ps1 is the shipped
    # instrument for proving they agree.
    $sup = @{}
    if (-not (Test-Path -LiteralPath $claimsDir)) { return $sup }
    foreach ($f in (Get-ChildItem -LiteralPath $claimsDir -Filter "B5-*.json")) {
        $id = $f.BaseName
        $owner = $null
        $started = $null
        try {
            $c = Get-Content -Raw -LiteralPath $f.FullName | ConvertFrom-Json
            $owner = $c.agent_id
            $ttl = $TtlMinutes
            if ($c.ttl_min) { $ttl = [int]$c.ttl_min }   # per-claim TTL wins, as in run-queue
            if ($c.started_utc) {
                try { $started = [DateTime]::Parse($c.started_utc).ToUniversalTime() } catch { $started = $null }
            }
        } catch {
            $sup[$id] = "<unreadable claim file>"
            continue
        }
        if (-not $started) { $started = $f.LastWriteTimeUtc }
        if ($started -gt $now) { $sup[$id] = $owner; continue }   # future-dated clock skew: not a defect
        $newest = $started
        if ($owner) {
            $k = Get-NormName $owner
            if ($heartbeats.ContainsKey($k) -and $heartbeats[$k] -gt $newest) { $newest = $heartbeats[$k] }
        }
        if (($now - $newest).TotalMinutes -lt $ttl) { $sup[$id] = $owner }
    }
    return $sup
}

$suppressed = Get-CensusSuppression

# --- per-row verdict ---
$out = @()
foreach ($r in $rows) {
    $claimOrNull = $null
    if ($claims.ContainsKey($r.Id)) { $claimOrNull = $claims[$r.Id] }

    $owner = "-"; $claimAge = "-"; $hbAge = "-"; $verdict = "-"; $reason = ""
    if ($claimOrNull) {
        $owner = $claimOrNull.Owner
        if ($owner) {
            $claimAge = [math]::Round(($now - $claimOrNull.Started).TotalMinutes, 1)
            $normOwner = Get-NormName $owner
            if ($heartbeats.ContainsKey($normOwner)) {
                $hf = $heartbeats[$normOwner]
                $hbAge = [math]::Round(($now - $hf).TotalMinutes, 1)
                $newestMin = [math]::Min([double]$claimAge, [double]$hbAge)
                $verdict = if ($newestMin -lt $TtlMinutes) { "LIVE" } else { "STALE" }
            } else {
                $verdict = "UNKNOWN"
                $reason = "no heartbeat file matching owner '$owner' (normalised '$normOwner'); absent signal never treated as live"
            }
        } else {
            $verdict = "UNKNOWN"
            $reason = "claim file present but agent_id unreadable"
        }
    } else {
        $verdict = "UNCLAIMED"
    }

    # Claims-first census verdict. Deliberately ADDITIVE and independent of the
    # liveness verdict printed above: that verdict is a per-claim liveness report and
    # this column is a per-row DEFECT-REPORTABILITY report. They answer different
    # questions, and keeping them separate means the suppression rule cannot perturb
    # the liveness verdicts the shipped crosscheck already diffs.
    $defect = if ($suppressed.ContainsKey($r.Id)) { "suppressed-live-claim" } else { "reportable" }

    # Compose the verdict cell once so an absent reason leaves no double space. The
    # rendered text is unchanged from the B5-0609 form ("LIVE [reason]"); only the
    # padding differs, and a census whose columns drift apart is a census two
    # readers stop agreeing on.
    $verdictCell = $verdict
    if ($reason) { $verdictCell = $verdict + " [" + $reason + "]" }

    $out += ("{0} | {1} | {2} | {3} | {4} | {5} | {6} | {7} | {8}" -f `
        $r.Id, $r.Status, $r.Pipes, $(if ($r.Double) { "yes" } else { "no" }),
        $owner, $claimAge, $hbAge, $verdictCell, $defect)
}

if ($out.Count -eq 0) { Write-Output "No rows match status filter '$Status'."; exit 0 }
$out | Sort-Object | ForEach-Object { $_.TrimEnd() }
Write-Output ("-- {0} row(s) matching status '{1}' --" -f $out.Count, $Status)

# Disclosure footer (B5-0657). The protocol requires that a census either apply
# claims-first suppression or SAY that it did not. This tool applies it, so the
# footer names what was suppressed and states the duty that discharges it: re-census
# a suppressed row after its claim is released, before calling any defect on it.
$supInView = @($out | Where-Object { $_ -match '\|\s*suppressed-live-claim\s*$' })
if ($supInView.Count -gt 0) {
    $ids = @()
    foreach ($line in $supInView) { $ids += ($line -split '\|')[0].Trim() }
    Write-Output ("-- CLAIMS-FIRST: " + $supInView.Count + " row(s) under a non-stale claim, NOT a defect report: " + ($ids -join ', ') + ". Re-census these rows after the claim is released before reporting any defect on them (live-repair-aware-ledger-census-protocol.md). --")
} else {
    Write-Output "-- CLAIMS-FIRST: 0 rows under a non-stale claim; every row above is reportable. --"
}