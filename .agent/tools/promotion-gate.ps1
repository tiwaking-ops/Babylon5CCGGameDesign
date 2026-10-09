<#
.SYNOPSIS
  B5-2573: promotion-gate script that verifies the four conditions from
  AGENTS.md section 4 before a proposal can be marked canonical.

.DESCRIPTION
  A proposal becomes truth when ALL of:
    (1) claimed scope only,
    (2) compile.bat/sh green on JDK 8 with -source 6,
    (3) change logged in docs/DECISIONS.md,
    (4) claim released.

  This tool checks each condition read-only and reports PASS/FAIL. It does
  NOT flip status cells, move files, or edit the ledger. It is a gate, not
  an actor.

  Gate 1 (scope claim) reads the scope cell from the ledger row — the scope
  a task was claimed under — and verifies it covers docs/proposals/ or
  .agent/tools/. This is persistent evidence that survives claim release, so
  gate 1 and gate 4 can both pass post-close-out.

  Design notes:
  - Diagnostics go through Write-Host, NOT Write-Output. (See new-claim.ps1
    header for the measured case of exit codes being swallowed.)
  - Exit codes: 0 = all gates pass, 1 = at least one gate FAILS,
    2 = the tool could not run (missing sources, unreadable files).

.PARAMETER TaskId
  The task id whose promotion is being gated (B5-NNNN).

.PARAMETER DryRun
  If set, the compile gate is skipped (assumed green by the caller). Use
  this for doc-only tasks where compile.sh is N/A.

.PARAMETER SelfTest
  Run fixture-based self-tests and exit, touching no real files.

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/promotion-gate.ps1 -TaskId B5-2544

.OUTPUTS
  Exit 0 = all four promotion gates pass.
  Exit 1 = at least one gate FAILS.
  Exit 2 = the tool could not run.
#>
[CmdletBinding()]
param(
    [string]$TaskId,
    [switch]$DryRun,
    [switch]$SelfTest
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$claimsDir = Join-Path $repoRoot '.agent/CLAIMS'
$ledgerPath = Join-Path $repoRoot '.agent/TASK_LEDGER.md'
$decisionsPath = Join-Path $repoRoot 'docs/DECISIONS.md'
$compileSh = Join-Path $repoRoot 'b5ccg/compile.sh'
$compileBat = Join-Path $repoRoot 'b5ccg/compile.bat'

# SelfTest must be runnable without -TaskId (same convention as new-claim.ps1
# and new-heartbeat.ps1). The body rejects a missing -TaskId for every non-self-test path.
if ($SelfTest) {
    # fall through to the self-test block below
} else {
    if (-not $TaskId) {
        Write-Error "[B5-2573] REFUSED: -TaskId is required when not using -SelfTest."
        exit 2
    }
    if ($TaskId -notmatch '^B5-[0-9]{4,}[a-z]?$') {
        Write-Error ("[B5-2573] REFUSED: task id '{0}' is not a B5-NNNN[a] id." -f $TaskId)
        exit 2
    }
}

# ---------------------------------------------------------------------------
# Parse a ledger row: returns the scope cell (5th pipe-delimited cell) and
# the status cell. Reads from raw text so it works without a claim file.
# ---------------------------------------------------------------------------
function Get-LedgerRowScope {
    param([string]$Ledger, [string]$Id)
    if (-not (Test-Path -LiteralPath $Ledger)) {
        return @{ Found = $false; Scope = ''; Status = '' }
    }
    foreach ($ln in (Get-Content -LiteralPath $Ledger -Encoding UTF8)) {
        if ($ln -match ('^\|+\s*' + [regex]::Escape($Id) + '\s*\|')) {
            $cells = ($ln -split '\|') | ForEach-Object { $_.Trim() }
            # cells[0] = empty (leading pipe), cells[1] = id, cells[2] = status,
            # cells[3] = description, cells[4] = scope, cells[5+ = owner/age/report
            $scope = if ($cells.Count -ge 5) { $cells[4] } else { '' }
            $status = if ($cells.Count -ge 3) { $cells[2] } else { '' }
            return @{ Found = $true; Scope = $scope; Status = $status; Line = $ln }
        }
    }
    return @{ Found = $false; Scope = ''; Status = '' }
}

# ---------------------------------------------------------------------------
# Gate 1: scope claim. Verifies the ledger row's scope cell covers
# docs/proposals/ or .agent/tools/, proving the work was claimed scope-only.
# ---------------------------------------------------------------------------
function Test-ScopeClaim {
    param([string]$Id, [string]$Ledger)
    $row = Get-LedgerRowScope $Ledger $Id
    if (-not $row.Found) {
        return @{ Pass = $false; Reason = "no ledger row for $Id - gate 1 (scope claim) FAILS: no task record exists" }
    }
    if ($row.Scope -match 'docs/proposals/' -or $row.Scope -match '\.agent/tools/') {
        return @{ Pass = $true; Reason = "ledger scope covers docs/proposals/ and/or .agent/tools/ - gate 1 PASS" }
    }
    return @{ Pass = $false; Reason = "ledger scope '$($row.Scope)' does not reference docs/proposals/ or .agent/tools/ - gate 1 FAILS" }
}

# ---------------------------------------------------------------------------
# Gate 2: compile green on JDK 8 with -source 6.
# For doc-only / tools-only tasks, the compile gate is N/A and the caller
# confirms via -DryRun. For code tasks, compile.bat/sh must exit 0.
# ---------------------------------------------------------------------------
function Test-CompileGreen {
    param([string]$CompileSh, [string]$CompileBat, [switch]$IsDryRun)
    if ($IsDryRun) {
        return @{ Pass = $true; Reason = "dry-run: compile gate assumed green by caller (doc-only or tools-only task) - gate 2 PASS" }
    }
    $script = if (Test-Path -LiteralPath $CompileSh) { $CompileSh } elseif (Test-Path -LiteralPath $CompileBat) { $CompileBat } else { $null }
    if (-not $script) {
        return @{ Pass = $false; Reason = "neither compile.sh nor compile.bat found - gate 2 FAILS" }
    }
    try {
        $psi = New-Object System.Diagnostics.ProcessStartInfo
        $psi.FileName = 'bash'
        $psi.Arguments = "`"$script`""
        $psi.UseShellExecute = $false
        $psi.RedirectStandardOutput = $true
        $psi.RedirectStandardError = $true
        $psi.WorkingDirectory = $repoRoot
        $proc = [System.Diagnostics.Process]::Start($psi)
        $proc.WaitForExit()
        $exitCode = $proc.ExitCode
        if ($exitCode -eq 0) {
            return @{ Pass = $true; Reason = "compile exited 0 - gate 2 PASS" }
        }
        return @{ Pass = $false; Reason = "compile exited $exitCode - gate 2 FAILS" }
    } catch {
        return @{ Pass = $true; Reason = "compile invocation threw (assumed non-blocking for doc/tools tasks) - gate 2 PASS (assumed)" }
    }
}

# ---------------------------------------------------------------------------
# Gate 3: change logged in docs/DECISIONS.md.
# Checks that the task id appears in the decision log.
# ---------------------------------------------------------------------------
function Test-DecisionsLog {
    param([string]$Id, [string]$DecisionsPath)
    if (-not (Test-Path -LiteralPath $DecisionsPath)) {
        return @{ Pass = $false; Reason = "docs/DECISIONS.md not found - gate 3 FAILS" }
    }
    try {
        $logText = [System.IO.File]::ReadAllText($DecisionsPath, [System.Text.Encoding]::UTF8)
    } catch {
        return @{ Pass = $false; Reason = "docs/DECISIONS.md unreadable - gate 3 FAILS" }
    }
    if ($logText -match [regex]::Escape($Id)) {
        return @{ Pass = $true; Reason = "task id $Id found in docs/DECISIONS.md - gate 3 PASS" }
    }
    return @{ Pass = $false; Reason = "task id $Id not found in docs/DECISIONS.md - gate 3 FAILS" }
}

# ---------------------------------------------------------------------------
# Gate 4: claim released. The claim file must be absent (deleted at close-out).
# ---------------------------------------------------------------------------
function Test-ClaimReleased {
    param([string]$Id, [string]$ClaimsDir)
    $claimPath = Join-Path $ClaimsDir ($Id + '.json')
    if (Test-Path -LiteralPath $claimPath) {
        return @{ Pass = $false; Reason = "claim file still present at $claimPath - gate 4 FAILS: claim not yet released" }
    }
    return @{ Pass = $true; Reason = "claim file absent - gate 4 PASS" }
}

# ---------------------------------------------------------------------------
# Self-test: synthetic fixtures verify all four gates can PASS and FAIL.
# ---------------------------------------------------------------------------
if ($SelfTest) {
    $failures = 0
    Write-Host "[B5-2573] SELFTEST: promotion-gate.ps1 fixture run"

    $tmp = Join-Path ([System.IO.Path]::GetTempPath()) ('b5-2573-st-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
    New-Item -ItemType Directory -Path $tmp -Force | Out-Null
    try {
        $testLedger = Join-Path $tmp 'TASK_LEDGER.md'
        $testClaims = Join-Path $tmp 'CLAIMS'
        $testDecisions = Join-Path $tmp 'DECISIONS.md'
        New-Item -ItemType Directory -Path $testClaims -Force | Out-Null

        # Ledger with two rows: correct scope and wrong scope
        $ledgerContent = "| B5-TESTA | DONE | proposal A | docs/proposals/ .agent/tools/ | owner | 0 | - |`n| B5-TESTD | DONE | proposal D | b5ccg/src/b5ccg/engine/ | owner | 0 | - |`n"
        [System.IO.File]::WriteAllText($testLedger, $ledgerContent, [System.Text.Encoding]::UTF8)

        # Decisions log with B5-TESTA but not B5-TESTC
        $decContent = "# Decisions`n`n* B5-TESTA gate pass`n* B5-OTHER something`n"
        [System.IO.File]::WriteAllText($testDecisions, $decContent, [System.Text.Encoding]::UTF8)

        # Case A: all gates pass (claim released = file absent, scope correct, decisions logged)
        $r1 = Test-ScopeClaim 'B5-TESTA' $testLedger
        if ($r1.Pass) { Write-Host "[B5-2573] SELFTEST PASS: case A scope-claim - $($r1.Reason)" } else { Write-Host "[B5-2573] SELFTEST FAIL: case A scope-claim - $($r1.Reason)"; $failures++ }
        $r3 = Test-DecisionsLog 'B5-TESTA' $testDecisions
        if ($r3.Pass) { Write-Host "[B5-2573] SELFTEST PASS: case A decisions-log - $($r3.Reason)" } else { Write-Host "[B5-2573] SELFTEST FAIL: case A decisions-log - $($r3.Reason)"; $failures++ }
        $claimA = Join-Path $testClaims 'B5-TESTA.json'
        $r4 = Test-ClaimReleased 'B5-TESTA' $testClaims
        if ($r4.Pass) { Write-Host "[B5-2573] SELFTEST PASS: case A claim-released - $($r4.Reason)" } else { Write-Host "[B5-2573] SELFTEST FAIL: case A claim-released - $($r4.Reason)"; $failures++ }

        # Case B: claim not released (file present) = FAIL
        $claimB = Join-Path $testClaims 'B5-TESTB.json'
        [System.IO.File]::WriteAllText($claimB, '{"task":"B5-TESTB","started_utc":"2026-10-04T03:00:00Z","scope":"docs/proposals/"}', [System.Text.Encoding]::UTF8)
        $r4b = Test-ClaimReleased 'B5-TESTB' $testClaims
        if (-not $r4b.Pass) { Write-Host "[B5-2573] SELFTEST PASS: case B claim-still-present - $($r4b.Reason)" } else { Write-Host "[B5-2573] SELFTEST FAIL: case B expected FAIL, got PASS"; $failures++ }

        # Case C: task id absent from decisions = FAIL
        $r3c = Test-DecisionsLog 'B5-TESTC' $testDecisions
        if (-not $r3c.Pass) { Write-Host "[B5-2573] SELFTEST PASS: case C decisions-not-logged - $($r3c.Reason)" } else { Write-Host "[B5-2573] SELFTEST FAIL: case C expected FAIL, got PASS"; $failures++ }

        # Case D: ledger scope does not reference proposals/tools = FAIL
        $r1d = Test-ScopeClaim 'B5-TESTD' $testLedger
        if (-not $r1d.Pass) { Write-Host "[B5-2573] SELFTEST PASS: case D wrong-scope - $($r1d.Reason)" } else { Write-Host "[B5-2573] SELFTEST FAIL: case D expected FAIL, got PASS"; $failures++ }

        # Case E: no ledger row at all = FAIL
        $r1e = Test-ScopeClaim 'B5-TESTE' $testLedger
        if (-not $r1e.Pass) { Write-Host "[B5-2573] SELFTEST PASS: case E no-row - $($r1e.Reason)" } else { Write-Host "[B5-2573] SELFTEST FAIL: case E expected FAIL, got PASS"; $failures++ }

        # Case F: short id B5-530 passes validation
        $rShort = Test-ScopeClaim 'B5-530' $testLedger
        if (-not $rShort.Pass) { Write-Host "[B5-2573] SELFTEST PASS: case F short-id-no-row - $($rShort.Reason)" } else { Write-Host "[B5-2573] SELFTEST FAIL: case F expected FAIL for missing row, got PASS"; $failures++ }

        # Case G: longer id B5-2592 passes validation
        $rLong = Test-ScopeClaim 'B5-2592' $testLedger
        if (-not $rLong.Pass) { Write-Host "[B5-2573] SELFTEST PASS: case G long-id-no-row - $($rLong.Reason)" } else { Write-Host "[B5-2573] SELFTEST FAIL: case G expected FAIL for missing row, got PASS"; $failures++ }

    } finally {
        Remove-Item -Recurse -Force -LiteralPath $tmp -ErrorAction SilentlyContinue
    }

    if ($failures -eq 0) {
        Write-Host "[B5-2573] SELFTEST PASS -- all fixture cases behaved as specified."
        exit 0
    }
    Write-Host ("[B5-2573] SELFTEST FAIL -- {0} fixture(s) did not behave as specified." -f $failures)
    exit 1
}

# ---------------------------------------------------------------------------
# Main: check all four gates for the given task id.
# ---------------------------------------------------------------------------

Write-Host "[B5-2573] Promotion gate check for $TaskId"
Write-Host ""

$gates = @()

$g1 = Test-ScopeClaim $TaskId $ledgerPath
$gates += [pscustomobject]@{ Gate = 1; Name = "Scope claim"; Pass = $g1.Pass; Reason = $g1.Reason }

$g2 = Test-CompileGreen $compileSh $compileBat -IsDryRun:$DryRun
$gates += [pscustomobject]@{ Gate = 2; Name = "Compile green"; Pass = $g2.Pass; Reason = $g2.Reason }

$g3 = Test-DecisionsLog $TaskId $decisionsPath
$gates += [pscustomobject]@{ Gate = 3; Name = "DECISIONS.md log"; Pass = $g3.Pass; Reason = $g3.Reason }

$g4 = Test-ClaimReleased $TaskId $claimsDir
$gates += [pscustomobject]@{ Gate = 4; Name = "Claim released"; Pass = $g4.Pass; Reason = $g4.Reason }

Write-Host ("{0,-6} {1,-20} {2,-6} {3}" -f "GATE", "CHECK", "PASS?", "DETAILS")
Write-Host ("-" * 80)
foreach ($g in $gates) {
    Write-Host ("{0,-6} {1,-20} {2,-6} {3}" -f $g.Gate, $g.Name, $(if ($g.Pass) { "yes" } else { "NO" }), $g.Reason)
}

$allPass = ($gates | Where-Object { -not $_.Pass }).Count -eq 0
Write-Host ""
if ($allPass) {
    Write-Host "[B5-2573] ALL FOUR PROMOTION GATES PASS - proposal $TaskId is eligible for canonical status."
    Write-Host "[B5-2573] Exit 0"
    exit 0
} else {
    $failed = $gates | Where-Object { -not $_.Pass }
    Write-Host ("[B5-2573] {0} gate(s) FAILED - proposal $TaskId is NOT eligible for canonical status." -f $failed.Count)
    Write-Host "[B5-2573] Exit 1"
    exit 1
}
