<#
.SYNOPSIS
  Per-agent clock-offset audit for coordination-file timestamps (advisory evidence).
.DESCRIPTION
  Read-only. For every .agent/CLAIMS/*.json and .agent/HEARTBEATS/*.json, compares
  three readings of the same event:
    * PAYLOAD   - the agent-written timestamp (claim started_utc / heartbeat utc)
    * MTIME     - the filesystem write time, which the writing process cannot forge
    * REAL      - this script's [DateTime]::UtcNow
  The discriminator is the PAYLOAD-vs-MTIME gap, not the payload-vs-real gap.
  A large negative payload-vs-real gap with payload==mtime is an IDLE agent, which
  is correct behaviour. A large payload-vs-MTIME gap is a DISAGREEING agent, which
  is a clock-integrity defect: no amount of idleness explains a file whose contents
  claim a moment its own inode says it was written elsewhere.
#>
param([int]$TtlMinutes = 30, [int]$BulkClusterMin = 5)

$ErrorActionPreference = "Stop"
# $PSScriptRoot = <repo>\.agent\tmp-clock-audit ; two levels up is the repo root.
$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$now = [DateTime]::UtcNow

function Get-Payload($f) {
    try {
        $j = Get-Content -Raw -Encoding UTF8 -LiteralPath $f.FullName | ConvertFrom-Json
        if ($j.started_utc) { return @{ V = ([DateTime]::Parse($j.started_utc).ToUniversalTime()); Id = $j.agent_id; Kind = "claim" } }
        if ($j.utc)         { return @{ V = ([DateTime]::Parse($j.utc).ToUniversalTime());         Id = $j.agent_id; Kind = "heartbeat" } }
    } catch { }
    return $null
}

$rows = @()
foreach ($f in (Get-ChildItem -LiteralPath (Join-Path $root ".agent/CLAIMS") -Filter "B5-*.json" -ErrorAction SilentlyContinue)) {
    $p = Get-Payload $f; if (-not $p) { continue }
    $rows += [pscustomobject]@{ File=$f.Name; Kind=$p.Kind; Agent=$p.Id; Payload=$p.V; Mtime=$f.LastWriteTimeUtc }
}
foreach ($f in (Get-ChildItem -LiteralPath (Join-Path $root ".agent/HEARTBEATS") -Filter "*.json" -ErrorAction SilentlyContinue)) {
    if ($f.Name -eq "_registry.json") { continue }
    $p = Get-Payload $f; if (-not $p) { continue }
    $rows += [pscustomobject]@{ File=$f.Name; Kind=$p.Kind; Agent=$p.Id; Payload=$p.V; Mtime=$f.LastWriteTimeUtc }
}

# --- BULK-TOUCH DETECTION -------------------------------------------------------
# A shared mtime across many files is not many writes; it is one operation touching
# many files (checkout, restore, copy, sync). Measured on this repo: 93 of 131
# heartbeat files share the mtime 2026-09-29 08:44 with no git commit at that hour.
# For those files the mtime is NOT a per-write record, so the payload-vs-mtime gap
# carries no information about the agent's clock and must not be scored as a defect.
# Classifying them as defects is how a naive offset sweep manufactures ~90 false
# alarms, so they are reported as a separate, non-alarming class.
$mtimeGroups = @{}
foreach ($r in $rows) {
    $k = $r.Mtime.ToString("yyyy-MM-dd HH:mm")
    if (-not $mtimeGroups.ContainsKey($k)) { $mtimeGroups[$k] = 0 }
    $mtimeGroups[$k]++
}
$bulk = New-Object 'System.Collections.Generic.HashSet[string]'
foreach ($k in $mtimeGroups.Keys) { if ($mtimeGroups[$k] -ge $BulkClusterMin) { [void]$bulk.Add($k) } }

Write-Output ("REAL UTC {0}   host-local {1}   TZ {2}   TTL {3} min" -f $now.ToString("yyyy-MM-ddTHH:mm:ssZ"), (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss"), [System.TimeZoneInfo]::Local.Id, $TtlMinutes)
Write-Output ""
Write-Output ("BULK-TOUCHED mtime clusters (>= {0} files sharing one minute) -- mtime is not a per-write record for these:" -f $BulkClusterMin)
foreach ($k in ($bulk | Sort-Object)) { Write-Output ("   {0}  : {1} file(s)" -f $k, $mtimeGroups[$k]) }
Write-Output ""
Write-Output "CLASSES:"
Write-Output ("   IN-BULK-CLUSTER  mtime was bulk-assigned; payload-vs-mtime gap is uninformative, excluded from the integrity verdict.")
Write-Output ("   IDLE             payload agrees with mtime and both are old. Correct behaviour for a stopped session.")
Write-Output ("   DISAGREE         payload and mtime differ beyond tolerance and mtime looks individually written. Clock-integrity defect.")
Write-Output ""
Write-Output ("{0,-42} {1,-9} {2,9} {3,9} {4,10}  {5}" -f "FILE","KIND","PAYLOAD","MTIME","P-vs-M","VERDICT")
Write-Output ("-" * 108)

$disagree = @(); $inBulk = @(); $idle = 0
foreach ($r in ($rows | Sort-Object -Property @{Expression={ [math]::Abs(($_.Payload - $_.Mtime).TotalMinutes) }; Descending=$true})) {
    $gap = ($r.Payload - $r.Mtime).TotalMinutes
    $age = ($now - $r.Payload).TotalMinutes
    $tol = if ($r.Kind -eq "claim") { $TtlMinutes } else { 5 }
    if ($bulk.Contains($r.Mtime.ToString("yyyy-MM-dd HH:mm"))) {
        $verdict = "IN-BULK-CLUSTER"; $inBulk += $r
    } elseif ([math]::Abs($gap) -gt $tol) {
        $verdict = "DISAGREE ({0:N1} min)" -f $gap; $disagree += $r
    } else {
        $verdict = if ($age -gt $TtlMinutes) { "idle ({0:N0} min)" -f $age } else { "ok (live, age {0:N1} min)" -f $age }
        if ($age -gt $TtlMinutes) { $idle++ }
    }
    Write-Output ("{0,-42} {1,-9} {2,9} {3,9} {4,10}  {5}" -f `
        $r.File, $r.Kind, $r.Payload.ToString("MM-dd HH:mm"), $r.Mtime.ToString("MM-dd HH:mm"), ("{0:N1}" -f $gap), $verdict)
}

Write-Output ""
Write-Output ("-- TOTALS: {0} parsed | {1} DISAGREE | {2} in-bulk-cluster | {3} idle | {4} live" -f $rows.Count, $disagree.Count, $inBulk.Count, $idle, ($rows.Count - $disagree.Count - $inBulk.Count - $idle))
if ($disagree.Count -gt 0) {
    Write-Output ""
    Write-Output "-- DISAGREE detail, the per-agent offset table a human needs:"
    foreach ($d in $disagree) {
        Write-Output ("   {0,-42} agent={1,-30} payload={2} mtime={3} gap={4,9:N1} min  payload-vs-real={5,9:N1} min" -f `
            $d.File, $d.Agent, $d.Payload.ToString("yyyy-MM-ddTHH:mm:ssZ"), $d.Mtime.ToString("yyyy-MM-ddTHH:mm:ssZ"), ($d.Payload - $d.Mtime).TotalMinutes, ($d.Payload - $now).TotalMinutes)
    }
}
Write-Output ""
Write-Output "This script is read-only and writes nothing. It is evidence for docs/reports/, not a gate."

