$root = Join-Path $env:TEMP ("b5660e-" + [guid]::NewGuid().ToString("N").Substring(0,8))
$agent = Join-Path $root ".agent"
$reports = Join-Path $root "REPORTS"
New-Item -ItemType Directory -Force -Path (Join-Path $agent "CLAIMS"), (Join-Path $agent "HEARTBEATS"), $reports | Out-Null
$now = [System.DateTime]::UtcNow
$stale = $now.AddHours(-6).ToString("yyyy-MM-ddTHH:mm:ssZ")
@("| ID | Status | Task | Scope | Claim | Verified |", "|---|---|---|---|---|---|", "| B5-0912 | OPEN | t | t | - | - |") | Set-Content (Join-Path $agent "TASK_LEDGER.md") -Encoding UTF8
@{ task="B5-0912"; agent_id="hb-never"; started_utc=$stale; ttl_min=30; scope=@("t"); javac="1" } | ConvertTo-Json -Compress | Set-Content (Join-Path (Join-Path $agent "CLAIMS") "B5-0912.json") -Encoding ASCII
$null = New-Item -ItemType File -Force -Path (Join-Path $reports "2026-09-27-hb-never-B5-0912.md")
Copy-Item (Join-Path (Get-Location).Path ".agent/run-queue.ps1") (Join-Path $agent "r.ps1")
$out = & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $agent "r.ps1") -DryRun 2>&1
($out | Out-String) -split "`n" | Where-Object { $_ -match "B5-0912|drained|Claiming|VERBOSE|reports at" } | ForEach-Object { Write-Output $_ }
Remove-Item $root -Recurse -Force
