param([switch]$DryRun)

# B5-1014 isolated seed-branch harness
# Purpose: test that the B5-0900 seed branch produces valid rows
# Scope: synthesize ledger, never mutate live tree

$ErrorActionPreference = 'Stop'

# === HARNESS SETUP ===
$TempRoot = Join-Path ([System.IO.Path]::GetTempPath()) 'b5-1014-harness'
$AgentDir = $TempRoot
$Ledger = Join-Path $AgentDir 'TASK_LEDGER.md'
$ClaimsDir = Join-Path $AgentDir 'CLAIMS'
$HeartbeatsDir = Join-Path $AgentDir 'HEARTBEATS'
$ReportsDir = Join-Path $AgentDir 'REPORTS'

# Clean slate
if (Test-Path $TempRoot) { Remove-Item -LiteralPath $TempRoot -Recurse -Force }
New-Item -ItemType Directory -Path $TempRoot -Force | Out-Null
New-Item -ItemType Directory -Path $ClaimsDir -Force | Out-Null
New-Item -ItemType Directory -Path $ReportsDir -Force | Out-Null

# === SYNTHETIC LEDGER: B5-1000 already closed, B5-1035 exists but we seed a NEW row ===
# The seed branch should create B5-1036 (next available after B5-1035 from TASK_LEDGER.md)
# Format: | id | status | priority | lane | assignee | started | due | scope | note |
# B5-1035 is already seeded in live ledger; we create a fresh ledger for harness isolation

# Create a ledger with ONE seedable row (B5-1036) - we can't seed into existing ids
$ledgerContent = @"
| B5-1036 | DONE | - | - | - | - | - | harness test | seed target for B5-1014 |
"@.Trim() + "`n"

# Empty ledger (queue drained, no OPEN rows) - seed should fire
$ledgerContentEmpty = ""

Set-Content -LiteralPath $Ledger -Value $ledgerContentEmpty -Encoding UTF8

# === OWNER HEARTBEAT (fresh, so seed branch sees truly seedable queue) ===
$freshHBMtime = (Get-Date).ToUniversalTime().AddMinutes(-1).ToString('o')
$freshHB = @"
{"schema_version": 1, "agent_id": "me-so-poor-b5-1014-harness", "utc": "$freshHBMtime", "state": "active", "current_task": null, "live_claims": [], "javac": "1.8.0_292", "notes": "harness heartbeat - fresh to make queue-seedable"}
"@
$freshHB | Set-Content -LiteralPath (Join-Path $HeartbeatsDir "me-so-poor-b5-1014-harness.json") -Encoding UTF8

Write-Host "== B5-1014 Isolate Harness =="
Write-Host "Temp root: $TempRoot"
Write-Host "Ledger path: $Ledger"
Write-Host "Synthetic ledger: EMPTY (no OPEN rows)"
Write-Host "Queue state: DRAINED = seedable"
Write-Host ""

# === COPY RUN-QUEUE WITH HARNESS PATHS ===
$srcPath = 'C:\temp\projects\Babylon5CCGGameDesign\.agent\run-queue.ps1'
$tempScript = Join-Path $TempRoot 'harness-runner.ps1'
Copy-Item -LiteralPath $srcPath -Destination $tempScript -Force

# Inject harness-specific paths at runtime via param block modification
# We'll use dot-sourcing with local overrides

# === LORE: What the seed branch should do ===
# When queue is drained (no OPEN rows without live claims), and Test-SeedableQueue returns true,
# the harness should invoke an agent CLI to seed ONE new OPEN row.
#
# Since we have no real agent CLI, we'll simulate the seed by having the harness
# directly execute the seed prompt template and create a row.

# Actually test the seed branch behavior by importing the functions and testing

Write-Host "Importing run-queue.ps1 functions for testing..."

# Dot-source the script to get its functions, but override paths
$orgPwd = Get-Location
Set-Location $TempRoot

try {
    # Modify script to use harness paths
    $sb = [ScriptBlock]::Create((Get-Content -LiteralPath $srcPath -Raw) -replace "Join-Path `"$AgentDir`\s*'\s*TASK_LEDGER\.md'", "`"$Ledger`"" -replace "Join-Path `"$AgentDir`\s*'\s*CLAIMS'", "`"$ClaimsDir`"" -replace "Join-Path `"$AgentDir`\s*'\s*HEARTBEATS'", "`"$HeartbeatsDir`"" -replace "Join-Path `"$AgentDir`\s*'\s*REPORTS'", "`"$ReportsDir`"")
    . $sb -DryRun -MaxSeedInvocations 1 -MaxIterations 1
} catch {
    Write-Host "ERROR: $_"
    Write-Host $_.ScriptStackTrace
} finally {
    Set-Location $orgPwd
}

Write-Host ""
Write-Host "=== Checking results ==="

# Check if a new row was written
$existingRows = Get-Content -LiteralPath $Ledger -ErrorAction SilentlyContinue
Write-Host "Ledger contents after run:"
$existingRows | ForEach-Object { Write-Host "  $_" }

# Validate the row
if ($existingRows -and $existingRows.Count -gt 0) {
    Write-Host ""
    Write-Host "Validating seeded row..."

    # Row must have single leading pipe, exactly 7 pipes, no duplicate IDs
    $valid = $true
    foreach ($line in $existingRows) {
        $pipeCount = ($line -split '\|').Count - 1
        $leadingDouble = $line -match '^\|\|'

        if (-not ($line -match '^\|')) {
            Write-Host "FAIL: Line missing leading pipe: $line"
            $valid = $false
        }
        if ($pipeCount -ne 7) {
            Write-Host "FAIL: Pipe count is $pipeCount, expected 7: $line"
            $valid = $false
        }
        if ($leadingDouble) {
            Write-Host "FAIL: Double leading pipe: $line"
            $valid = $false
        }
    }

    # Check for duplicate IDs
    $ids = $existingRows | Where-Object { $_ -match 'B5-\d+' } | ForEach-Object { ($_ -split '\|')[-2].Trim() }
    $dupes = $ids | Group-Object | Where-Object Count -gt 1
    if ($dupes) {
        Write-Host "FAIL: Duplicate IDs: $($dupes.Name -join ', ')"
        $valid = $false
    }

    if ($valid) {
        Write-Host "PASS: All seeded rows valid (7 pipes, single leading, no duplicates)"
    }
} else {
    Write-Host "No rows written - seed branch did not fire"
}

Write-Host ""
Write-Host "=== Harness result ==="
Write-Host "Check: ledger has OPEN row for B5-1014 to claim and work"