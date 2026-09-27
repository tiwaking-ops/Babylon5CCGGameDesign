<#
.SYNOPSIS
  Read-only ledger census + claim-liveness query tool (B5-0609).
.DESCRIPTION
  Prints one line per row of .agent/TASK_LEDGER.md matching an optional status
  filter (default OPEN) as: ID | status | pipeCount | doubleLead | claimOwner |
  claimAgeMin | hbAgeMin | verdict [reason]

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

    $out += ("{0} | {1} | {2} | {3} | {4} | {5} | {6} | {7} {8}" -f `
        $r.Id, $r.Status, $r.Pipes, $(if ($r.Double) { "yes" } else { "no" }),
        $owner, $claimAge, $hbAge, $verdict, $(if ($reason) { "[$reason]" } else { "" }))
}

if ($out.Count -eq 0) { Write-Output "No rows match status filter '$Status'."; exit 0 }
$out | Sort-Object | ForEach-Object { $_.TrimEnd() }
Write-Output ("-- {0} row(s) matching status '{1}' --" -f $out.Count, $Status)