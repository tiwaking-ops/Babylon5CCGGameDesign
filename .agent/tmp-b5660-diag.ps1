$root = Join-Path $env:TEMP ("b5660d-" + [guid]::NewGuid().ToString("N").Substring(0,8))
$agent = Join-Path $root ".agent"
$reports = Join-Path $root "REPORTS"
New-Item -ItemType Directory -Force -Path (Join-Path $agent "CLAIMS"), (Join-Path $agent "HEARTBEATS"), $reports | Out-Null
$now = [System.DateTime]::UtcNow
$stale = $now.AddHours(-6).ToString("yyyy-MM-ddTHH:mm:ssZ")
@("| ID | Status | Task | Scope | Claim | Verified |", "|---|---|---|---|---|---|", "| B5-0913 | OPEN | t | t | - | - |") | Set-Content (Join-Path $agent "TASK_LEDGER.md") -Encoding UTF8
@{ task="B5-0913"; agent_id="hb-old"; started_utc=$stale; ttl_min=30; scope=@("t"); javac="1" } | ConvertTo-Json -Compress | Set-Content (Join-Path (Join-Path $agent "CLAIMS") "B5-0913.json") -Encoding ASCII
@{ schema_version=1; agent_id="hb-old"; utc=$now.AddMinutes(-90).ToString("yyyy-MM-ddTHH:mm:ssZ"); state="busy"; live_claims=@() } | ConvertTo-Json -Compress | Set-Content (Join-Path (Join-Path $agent "HEARTBEATS") "hb-old.json") -Encoding ASCII
$live = Join-Path (Get-Location).Path ".agent/run-queue.ps1"
Copy-Item $live (Join-Path $agent "r.ps1")
$out = & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $agent "r.ps1") -DryRun 2>&1
($out | Out-String) -split "`n" | Where-Object { $_ -match "B5-0913|drained|Claiming" } | ForEach-Object { Write-Output $_ }
Remove-Item $root -Recurse -Force
