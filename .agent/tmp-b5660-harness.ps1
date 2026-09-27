# B5-0660 acceptance harness (Buffy (glm-5.3-flash), 2026-09-27).
# Runs a COPY of the live runner against a synthetic ledger/claims/heartbeats/
# REPORTS sandbox in %TEMP%. Verifies the three-signal triad and UNKNOWN
# semantics. Never mutates the live tree.
$ErrorActionPreference = "Stop"

$root    = Join-Path $env:TEMP ("b5660-" + [guid]::NewGuid().ToString("N").Substring(0,8))
$agent   = Join-Path $root ".agent"
$reports = Join-Path $root "REPORTS"
New-Item -ItemType Directory -Force -Path (Join-Path $agent "CLAIMS") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $agent "HEARTBEATS") | Out-Null
New-Item -ItemType Directory -Force -Path $reports | Out-Null

$now = [System.DateTime]::UtcNow
$fmt       = "yyyy-MM-ddTHH:mm:ssZ"
$stale     = $now.AddHours(-6).ToString($fmt)      # ~6h old, genuine
$staleHb   = $now.AddMinutes(-90).ToString($fmt)   # stale heartbeat timestamp
$freshHbM  = $now.AddMinutes(-2)                   # fresh heartbeat mtime

# Self-assert: harness timestamps parse
foreach ($t in @($stale, $staleHb)) {
  $p = [System.DateTime]::Parse($t, [System.Globalization.CultureInfo]::InvariantCulture,
        [System.Globalization.DateTimeStyles]::RoundtripKind).ToUniversalTime()
  if ($p -ge $now) { throw "harness bug: $t should be in the past" }
}
Write-Output "HARNESS: generated timestamps parse OK"

$ledger = Join-Path $agent "TASK_LEDGER.md"
@(
  "| ID | Status | Task | Scope | Claim | Verified |",
  "|---|---|---|---|---|---|",
  "| B5-0911 | OPEN | stale claim + stale hb + FRESH report | t | - | - |",
  "| B5-0912 | OPEN | stale claim + NO heartbeat + fresh report | t | - | - |",
  "| B5-0913 | OPEN | stale claim + stale hb + NO report (in-flight, no report yet) | t | - | - |"
) | Set-Content -LiteralPath $ledger -Encoding UTF8

function New-Claim([string]$id, [string]$owner) {
  @{ task = $id; agent_id = $owner; started_utc = $stale; ttl_min = 30; scope = @("t"); javac = "1.8.0_292" } |
    ConvertTo-Json -Compress | Set-Content -LiteralPath (Join-Path (Join-Path $agent "CLAIMS") ($id + ".json")) -Encoding ASCII
}
New-Claim "B5-0911" "hb-old"
New-Claim "B5-0912" "hb-never"
New-Claim "B5-0913" "hb-old"

# Heartbeats: hb-old is STALE (written 90 min ago); hb-never does not exist.
$hbOld = @{ schema_version = 1; agent_id = "hb-old"; utc = $staleHb; state = "busy"; live_claims = @(); javac = "1.8.0_292"; notes = "harness" } |
  ConvertTo-Json -Compress
[System.IO.File]::WriteAllText((Join-Path (Join-Path $agent "HEARTBEATS") "hb-old.json"), $hbOld)

# Reports: fresh report for 0911 (mtime now); fresh report for 0912 too
# (its owner finished but never had a heartbeat); NONE for 0913.
$null = New-Item -ItemType File -Force -Path (Join-Path $reports "2026-09-27-hb-old-B5-0911.md")
$null = New-Item -ItemType File -Force -Path (Join-Path $reports "2026-09-27-hb-never-B5-0912.md")

# Copy the live runner INTO the sandbox .agent (it derives paths from $PSScriptRoot)
$liveScript = Join-Path (Get-Location).Path ".agent/run-queue.ps1"
$copy = Join-Path $agent "run-queue-copy.ps1"
$scriptText = [System.IO.File]::ReadAllText($liveScript)
# B5-0653 suppression replica note: the sandbox REPORTS dir sits at repo root
# (sibling of .agent), matching the live layout.
$null = $scriptText
[System.IO.File]::WriteAllText($copy, $scriptText)
Write-Output ("HARNESS: reports dir = " + $reports)
Write-Output ("HARNESS: report files present = " + ((Get-ChildItem $reports -Filter *.md).Count))

$out = & powershell -NoProfile -ExecutionPolicy Bypass -File $copy -DryRun -Verbose 2>&1
$text = ($out | Out-String)

function Test-OfferedId([string]$id) {
  foreach ($l in ($text -split "`n")) {
    if ($l -match "would invoke" -and $l -match [regex]::Escape($id)) { return $true }
  }
  return $false
}

Write-Output "=== CASES ==="
# V1 (row's first verify case): fresh report + stale claim + stale heartbeat
# still reads LIVE -> not offered.
$v1 = Test-OfferedId "B5-0911"
Write-Output ("V1 fresh report rescues stale claim+hb : offered=" + $v1 + " (expected False) " + $(if (-not $v1) {"PASS"} else {"FAIL"}))

# V2: missing heartbeat reads UNKNOWN (warned), report signal still decides.
$v2 = Test-OfferedId "B5-0912"
$v2warned = ($text -match "B5-0660.*B5-0912.*heartbeat UNKNOWN")
Write-Output ("V2 missing heartbeat = UNKNOWN warned  : offered=" + $v2 + " warned=" + $v2warned + " (expected offered=False, warned=True) " + $(if ((-not $v2) -and $v2warned) {"PASS"} else {"FAIL"}))

# V3 (row's third verify case): an in-flight task with NO report yet, stale
# claim + stale heartbeat -> reads STALE here and IS offered (the runner is
# the offer path; the never-block-forever rule). This proves the report
# signal does not over-block a task that has no report yet.
$v3 = Test-OfferedId "B5-0913"
Write-Output ("V3 in-flight, no report yet            : offered=" + $v3 + " (expected True -- report signal does not over-block) " + $(if ($v3) {"PASS"} else {"FAIL"}))

# Unparseable claim -> UNKNOWN never LIVE: task not offered, warning emitted.
# (Reuse the sandbox: corrupt 0912's claim, rerun, then check.)
[System.IO.File]::WriteAllText((Join-Path (Join-Path $agent "CLAIMS") "B5-0912.json"), "{ not json ")
$out2 = & powershell -NoProfile -ExecutionPolicy Bypass -File $copy -DryRun 2>&1
$text2 = ($out2 | Out-String)
$v4offered = ($text2 -match "would invoke.*B5-0912")
$v4warned = ($text2 -match "B5-0660.*B5-0912.*UNPARSEABLE")
Write-Output ("V4 unparseable claim = UNKNOWN, never LIVE: offered=" + $v4offered + " warned=" + $v4warned + " (expected False/True) " + $(if ((-not $v4offered) -and $v4warned) {"PASS"} else {"FAIL"}))

Remove-Item -LiteralPath $root -Recurse -Force
Write-Output "HARNESS: sandbox cleaned"
