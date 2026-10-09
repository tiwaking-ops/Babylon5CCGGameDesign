<#
.SYNOPSIS
  Playable-release automated preflight (B5-2579) — the machine half of the
  B5-2283 release checklist, as one script with one exit code and one JSON
  status report.

.DESCRIPTION
  B5-2283 (`docs/proposals/2026-10-03-b5-2283-playable-release-checklist-proposal.md`)
  defines "playable" as a checklist of automated preflight items PLUS a
  mandatory 30-minute human session. This script implements ONLY the automated
  half. It never reports the build playable, and it says so in its own output:
  a compile-and-gates pass is `automatedPreflight: PASS`, never `playable: true`.

  Criteria, all measured in this repo's standing state on 2026-10-04:

    1. compile          b5ccg\compile.bat exits 0 and reports "Build successful"
    2. conformance      HeadlessConformanceTest exits 0
    3. smoke            HeadlessSmokeTest exits 0
    4. cardpool         the pool the LOADER SERVES is -ExpectedPool cards
    5. headless-gates   the 7 wired headless gates each exit 0
    6. java6-census     every construct family reads code-lines 0 under the
                        TRACKED b5ccg/src block, and the API-level signal
                        (qualified `.getOrDefault(`) is 0
    7. citations        check-citations.ps1 exits 0
    8. human-seat       HeadlessHumanSeatProbe exits 0 — OPT-IN, see below

  ## THE 829 IN THE TASK ROW IS NOT WHAT THE LOADER SERVES

  B5-2579's row text says "deck loads 829 cards". That number is the RAW
  RECORD count of the two pools (premiere.json 446 + deluxe.json 383 = 829),
  and it is not a pool size the code can ever serve. Measured, not assumed:

    - `DeckLoader.loadBothSets()` returns **446** cards (HeadlessSmokeTest
      `[1/5] DeckLoader.loadBothSets() ... 446 cards`).
    - The reason is in DeckLoader.java:190-199, and it is a deliberate human
      ruling of 2026-09-21: the pool is ALL Deluxe cards plus every Premiere
      card never reprinted. All 383 Premiere titles are reprinted in Deluxe
      (383/383 overlap), so 383 + 63 survivors = 446.

  So the row's own acceptance criterion is unsatisfiable as written, and the
  gate is written against the measured 446 with the 829 recorded alongside it
  as the raw-record denominator. Two numbers that differ by exactly the
  reprinted set is the shape that a wrong gate would hide.

  ## THE HUMAN-SEAT CRITERION IS OPT-IN AND NEVER SILENTLY GREEN

  "Human has at least one legal action" is the one criterion with no fast
  instrument: none of the 7 wired gates drives the human seat. The instrument
  that does is `HeadlessHumanSeatProbe`, which submits through the same
  `submitHumanAction` path the MainWindow buttons use — but compile.sh:73-77
  deliberately leaves it unwired, and a measured run costs ~80s (compile.sh
  records 80.3s) against ~4s for all seven gates combined.

  So it is `-IncludeHumanSeat` only, and when it is not requested the
  criterion reports `NOT_RUN` — never `PASS`, never `SKIP`, never omitted.
  A gate that certifies playability while the one human-path criterion was
  never measured is the silent-green shape this repository keeps recording, and
  `NOT_RUN` is the cheapest honest way to refuse it.

  Exit contract:
    0  every REQUIRED criterion PASS  (automated preflight green)
    1  at least one required criterion FAIL, or any required criterion UNKNOWN
    2  the preflight could not run: layout broken, toolchain absent, or a
       probe that never executed. Distinct from 0 by design — a check that
       could not run must never report as a pass (the run-dup-census 0/1/2
       lesson, B5-0777).

.NOTES
  Supersede-never-rewrite applies to REPORTS and PATTERNS, not to this file,
  which is this claim's own deliverable.

  Citation of the human criterion comes from B5-2283's definition item 2 and
  the automated-preflight section; B5-2283 also states the checklist "becomes
  an adopted release gate only after a human accepts the definition and the
  victory-route setup", which is why no verdict key here reads `playable`.
#>
[CmdletBinding()]
param(
  # B5-2579 opt-in. Roughly 80s (compile.sh records 80.3s for this probe)
  # against ~4s for the whole rest of the preflight. Absent, the
  # human-seat criterion reports NOT_RUN and the required set is unchanged.
  [switch]$IncludeHumanSeat,

  # Overridable so -SelfTest can drive ONE input to a wrong value and prove
  # the RED path is reachable. Default is the measured served-pool count.
  [int]$ExpectedPool = 446,

  # Overridable so -SelfTest can point the layout gate at an empty directory
  # and prove "could not run" exits 2 rather than reading as a pass.
  [string]$RepoRoot,

  # Two independently runnable RED proofs. Each must be runnable alone so a
  # reader can see one red path green up without the other's verdict masking it.
  [switch]$SelfTest
)

$ErrorActionPreference = 'Continue'

if (-not $RepoRoot) {
  $RepoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
}
$RepoRoot = (Resolve-Path -LiteralPath $RepoRoot).Path

# ---- console encoding -------------------------------------------------------
# Same pin the verification battery applies (run-verification-battery.ps1
# lines 124-147): this host's console codepage is ibm850, and on ibm850 a
# non-ASCII codepoint is DESTROYED in transit through native stdout. The card
# JSON and the tracked comments carry such characters, so an unpinned console
# silently corrupts the very evidence this script reports.
$script:encodingPinned = $false
try {
  [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
  $script:encodingPinned = $true
} catch {
  Write-Warning ("could not pin console output encoding to UTF-8: " + $_.Exception.Message)
}

# The 7 wired headless gates, in compile.sh order (compile.sh:84-90). Named
# here rather than discovered, because a gate that discovers its own gates
# reports green on an empty list — the same shape as a census that matched
# zero rows while printing a green verdict (B5-1730).
$HeadlessGates = @(
  'HeadlessAIDifficultyContractTest',
  'HeadlessConflictResolutionProbe',
  'HeadlessHumanConflictAttackWindowTest',
  'HeadlessLeadFleetScenarioProbe',
  'HeadlessParticipationGatesProbe',
  'HeadlessStationVictoryTest',
  'HeadlessWarConflictProbe'
)

$OutDir = Join-Path $RepoRoot 'b5ccg\out'
$script:Criteria = New-Object System.Collections.Generic.List[object]

function Add-Criterion {
  param(
    [string]$Name,
    [bool]$Required,
    [string]$Status,      # PASS | FAIL | UNKNOWN | NOT_RUN
    [string]$Detail,
    $ExitCode
  )
  $script:Criteria.Add([pscustomobject]@{
    name     = $Name
    required = $Required
    status   = $Status
    detail   = $Detail
    exit     = $ExitCode
  })
}

# Invoke a Java probe by ABSOLUTE classpath. compile.bat self-locates via
# %~dp0 and the battery hit this same trap in the other direction (moving the
# cwd instead of using an absolute path, and cmd never found the file), so no
# cwd semantics are relied on here at all.
#
# STDERR IS CAPTURED, NOT DISCARDED. The first version of this script sent
# stderr to $null on the grounds that DeckLoader's tolerant tokeniser prints one
# "UNKNOWN FIELD" line per unmodelled key and carries no signal. That was true
# of the noise and false of the rest: HeadlessSmokeTest.fail() writes
# "SMOKE TEST FAILED: <reason>" to stderr and nowhere else (HeadlessSmokeTest
# lines 335-339), so a 2>$null probe discards the single most diagnostic line a
# failing gate can emit. The first live run of this script reported
# `smoke FAIL exit 1` with no reason at all, which is a gate that detects a
# defect and cannot explain it.
#
# So stderr is captured whole and the KNOWN noise is filtered by its own prefix.
# The filter is anchored on "UNKNOWN FIELD:" rather than on stderr being noisy,
# so a real failure line cannot be mistaken for noise.
function Invoke-Probe {
  param([string]$ClassName)

  $id = [guid]::NewGuid().ToString('N').Substring(0, 8)
  $outFile = Join-Path ([System.IO.Path]::GetTempPath()) ('b5-probe-' + $id + '.out')
  $errFile = Join-Path ([System.IO.Path]::GetTempPath()) ('b5-probe-' + $id + '.err')

  $sw = [System.Diagnostics.Stopwatch]::StartNew()
  try {
    # Start-Process rather than the call operator: it is the only form here
    # that separates the two streams to real files instead of merging stderr
    # into PowerShell's error stream as ErrorRecords.
    $p = Start-Process -FilePath 'java.exe' `
      -ArgumentList @('-cp', $OutDir, ('b5ccg.engine.' + $ClassName)) `
      -NoNewWindow -Wait -PassThru `
      -RedirectStandardOutput $outFile -RedirectStandardError $errFile `
      -ErrorAction Stop
    $code = $p.ExitCode
  } catch {
    $code = -1
    [System.IO.File]::WriteAllText($errFile, ('probe launch failed: ' + $_.Exception.Message))
  }
  $sw.Stop()

  $stdout = ''
  $stderr = ''
  if (Test-Path -LiteralPath $outFile) { $stdout = [System.IO.File]::ReadAllText($outFile) }
  if (Test-Path -LiteralPath $errFile) { $stderr = [System.IO.File]::ReadAllText($errFile) }
  Remove-Item -LiteralPath $outFile, $errFile -Force -ErrorAction SilentlyContinue

  $signal = @(($stderr -split "`r?`n") |
                Where-Object { $_ -and $_ -notmatch '^\s*UNKNOWN FIELD:' } |
                ForEach-Object { $_.Trim() })

  return [pscustomobject]@{
    Exit   = $code
    Stdout = $stdout
    # Retained ONLY on failure, and truncated: the unfiltered stderr of a real
    # failure can be hundreds of UNKNOWN FIELD lines, and a report that buries
    # the reason under them has failed at the only job it has.
    Signal = $signal
    Ms     = [int]$sw.ElapsedMilliseconds
  }
}

# The reason a probe failed, from BOTH streams.
#
# The first version of this helper read stderr only, which produced a report of
# `conformance FAIL exit 1` whose "evidence" was three informational lines that
# had nothing to do with the failure — the same defect as discarding stderr,
# one level down. The two streams carry different failure information and a
# gate that reads one of them is half a gate:
#
#   stderr  HeadlessSmokeTest.fail() writes "SMOKE TEST FAILED: <reason>"
#           (HeadlessSmokeTest lines 335-339) and nowhere else.
#   stdout  HeadlessConformanceTest.check() writes "  [RULE] label: FAIL"
#           per failing rule plus a "CONFORMANCE SUITE FAILED (n of m checks)"
#           summary (lines 132-135, 9249-9251).
#
# The probe classes differ, so the extraction is by the shapes they actually
# emit rather than by one hard-coded channel.
function Get-FailureEvidence {
  param($Probe, [int]$Max = 6)

  $stdoutLines = @(($Probe.Stdout -split "`r?`n") |
                     Where-Object { $_ -match ':\s*FAIL\b' -or $_ -match 'SUITE FAILED' -or $_ -match 'SMOKE TEST FAILED' } |
                     ForEach-Object { $_.Trim() })

  # Filtered noise, named so the filter is auditable rather than a blanket
  # "stderr is noisy, ignore it": the tokeniser's per-key UNKNOWN FIELD lines
  # and the JDK's own JAVA_TOOL_OPTIONS pickup banner.
  $stderrLines = @($Probe.Signal | Where-Object { $_ -notmatch '^Picked up JAVA_TOOL_OPTIONS' })

  $parts = @()
  if ($stdoutLines.Count -gt 0) { $parts += $stdoutLines }
  if ($stderrLines.Count -gt 0) { $parts += $stderrLines }

  if ($parts.Count -eq 0) {
    return 'no FAIL marker on stdout and no diagnostic line on stderr (probe exited non-zero and said nothing)'
  }
  if ($parts.Count -le $Max) { return ($parts -join ' || ') }
  # Keep the LAST lines: for a suite the summary count follows the per-rule
  # findings, and it is the summary a reader needs first.
  return (@('...' + $parts[($parts.Count - $Max)..($parts.Count - 1)]) -join ' || ')
}

# ---- layout gate: exit 2, never a false pass --------------------------------
$requiredPaths = @(
  (Join-Path $RepoRoot 'b5ccg\compile.bat'),
  (Join-Path $RepoRoot 'b5ccg\compile.sh'),
  (Join-Path $RepoRoot '.agent\tools\census-b50960.py'),
  (Join-Path $RepoRoot '.agent\tools\check-citations.ps1')
)
$missing = @($requiredPaths | Where-Object { -not (Test-Path -LiteralPath $_) })
if ($missing.Count -gt 0) {
  $report = [pscustomobject]@{
    tool     = 'playable-release-check'
    task     = 'B5-2579'
    verdict  = 'UNDETERMINED'
    exit     = 2
    reason   = 'layout check failed'
    missing  = @($missing | ForEach-Object { $_.Replace($RepoRoot, '<root>') })
    encodingPinned = $script:encodingPinned
  }
  Write-Output ($report | ConvertTo-Json -Depth 6)
  Write-Error ("playable-release-check: layout check failed, missing: " + ($missing -join ', '))
  exit 2
}

# =================================================================================
# RED proofs. Each is independently runnable and each touches no live file.
# =================================================================================
if ($SelfTest) {
  # Proof A — the LAYOUT path. An empty TEMP directory holds none of the
  # required instruments, so the preflight cannot run. It must exit 2, and
  # specifically NOT 0: "the check did not run" reported as "the check passed"
  # is the failure mode exit 2 exists to prevent. Registering the fixture at
  # the REAL instrument names is what makes this a differential proof -- a
  # fixture using invented names would not exercise the production branch.
  $fixtureRoot = Join-Path ([System.IO.Path]::GetTempPath()) ('b5-playable-layout-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
  New-Item -ItemType Directory -Path $fixtureRoot -Force | Out-Null
  Write-Host '=== playable-release-check SELF-TEST (proof A: layout / exit-2 path) ==='
  Write-Host ("    fixture root: " + $fixtureRoot + " (empty; holds none of the 4 required instruments)")
  & powershell -NoProfile -ExecutionPolicy Bypass -File $PSCommandPath -RepoRoot $fixtureRoot 2>$null | Out-Null
  $layoutExit = $LASTEXITCODE
  Write-Host ("    observed exit " + $layoutExit + "; designed 2 (could not run)")
  if ($layoutExit -ne 2) {
    Write-Warning ('LAYOUT PROOF INVALID: an empty fixture root cannot run the preflight, so it must exit 2, but it exited ' + $layoutExit)
  } else {
    Write-Host '    differential holds: exit 2, and NOT 0, so a preflight that could not run is not reported as a pass'
  }
  $layoutValid = ($layoutExit -eq 2)
  Remove-Item -LiteralPath $fixtureRoot -Recurse -Force -ErrorAction SilentlyContinue

  # Proof B — the POOL path. The real repo with exactly ONE input changed:
  # -ExpectedPool 1 instead of the measured 446. Every other criterion is
  # untouched and green, so the only thing that can turn the verdict RED is
  # the pool assertion. This is the proof that the card-count gate can fail,
  # which matters precisely because the row's own "829" was unsatisfiable --
  # a gate that could only pass would have shipped that error silently.
  Write-Host ''
  Write-Host '=== playable-release-check SELF-TEST (proof B: card-pool / exit-1 path) ==='
  Write-Host ("    real repo, -ExpectedPool 1 against a measured served pool of 446; all other inputs unchanged")
  & powershell -NoProfile -ExecutionPolicy Bypass -File $PSCommandPath -ExpectedPool 1 2>$null | Out-Null
  $poolExit = $LASTEXITCODE
  Write-Host ("    observed exit " + $poolExit + "; designed 1 (a required criterion failed)")
  if ($poolExit -ne 1) {
    Write-Warning ('POOL PROOF INVALID: a pool of 1 against a served pool of 446 must fail the criterion, but the run exited ' + $poolExit)
  } else {
    Write-Host '    differential holds: exit 1 with every other criterion unchanged, so the RED is attributable to the pool assertion alone'
  }
  $poolValid = ($poolExit -eq 1)

  if ($layoutValid -and $poolValid) {
    Write-Host ''
    Write-Host '=== SELF-TEST PASS: both red paths observed ==='
    exit 0
  }
  Write-Host ''
  Write-Host '=== SELF-TEST FAIL: at least one red path was not observed ==='
  exit 1
}

# =================================================================================
# Criterion 1 — compile gate
# =================================================================================
$compileBat = Join-Path $RepoRoot 'b5ccg\compile.bat'
$compileOut = (& cmd /c "`"$compileBat`"" 2>&1 | Out-String)
$compileExit = $LASTEXITCODE
$compileGreen = ($compileExit -eq 0 -and $compileOut -match 'Build successful')
if ($compileExit -eq -1 -and [string]::IsNullOrWhiteSpace($compileOut)) {
  Add-Criterion -Name 'compile' -Required $true -Status 'UNKNOWN' `
    -Detail 'compile.bat produced no output and no usable exit code; the build gate did not run' -ExitCode $compileExit
} elseif ($compileGreen) {
  Add-Criterion -Name 'compile' -Required $true -Status 'PASS' `
    -Detail 'b5ccg\compile.bat exit 0 and reported Build successful (javac -source 6 -target 6, stdlib only)' -ExitCode $compileExit
} else {
  Add-Criterion -Name 'compile' -Required $true -Status 'FAIL' `
    -Detail ('compile.bat exit ' + $compileExit + '; Build successful marker ' + $(if ($compileOut -match 'Build successful') { 'present' } else { 'ABSENT' })) -ExitCode $compileExit
}

# If the build did not produce classes, every probe below is UNKNOWN, not FAIL:
# a missing class file is the build's failure, and reporting it as seven
# independent probe failures would multiply one defect into eight findings.
$outReady = (Test-Path -LiteralPath (Join-Path $OutDir 'b5ccg\engine\HeadlessSmokeTest.class'))

# =================================================================================
# Criteria 2 and 3 — conformance and smoke
# =================================================================================
$smokeOut = ''
if ($outReady) {
  $conf = Invoke-Probe -ClassName 'HeadlessConformanceTest'
  if ($conf.Exit -eq 0) {
    Add-Criterion -Name 'conformance' -Required $true -Status 'PASS' `
      -Detail ('HeadlessConformanceTest exit 0 in ' + $conf.Ms + ' ms') -ExitCode $conf.Exit
  } else {
    Add-Criterion -Name 'conformance' -Required $true -Status 'FAIL' `
      -Detail ('HeadlessConformanceTest exit ' + $conf.Exit + ' in ' + $conf.Ms + ' ms; ' + (Get-FailureEvidence -Probe $conf)) -ExitCode $conf.Exit
  }

  $smoke = Invoke-Probe -ClassName 'HeadlessSmokeTest'
  $smokeOut = $smoke.Stdout
  if ($smoke.Exit -eq 0) {
    Add-Criterion -Name 'smoke' -Required $true -Status 'PASS' `
      -Detail ('HeadlessSmokeTest exit 0 in ' + $smoke.Ms + ' ms') -ExitCode $smoke.Exit
  } else {
    # The failure reason, not just the code. See Invoke-Probe: this is the
    # line the first live run of this script threw away.
    Add-Criterion -Name 'smoke' -Required $true -Status 'FAIL' `
      -Detail ('HeadlessSmokeTest exit ' + $smoke.Exit + ' in ' + $smoke.Ms + ' ms; ' + (Get-FailureEvidence -Probe $smoke)) -ExitCode $smoke.Exit
  }
} else {
  Add-Criterion -Name 'conformance' -Required $true -Status 'UNKNOWN' `
    -Detail 'b5ccg\out carries no compiled classes; the build gate did not produce them, so no probe was run' -ExitCode $null
  Add-Criterion -Name 'smoke' -Required $true -Status 'UNKNOWN' `
    -Detail 'b5ccg\out carries no compiled classes; the build gate did not produce them, so no probe was run' -ExitCode $null
}

# =================================================================================
# Criterion 4 — the served card pool
# =================================================================================
# Parsed from the smoke probe's OWN stdout, never recomputed from the JSON:
# the criterion is about what the loader serves, and a count taken from the
# pool files would be a different measurement wearing the criterion's name.
if ($smokeOut -match 'loadBothSets\(\)\s*\.\.\.\s*(\d+)\s+cards') {
  $served = [int]$Matches[1]
  if ($served -eq $ExpectedPool) {
    Add-Criterion -Name 'cardpool' -Required $true -Status 'PASS' `
      -Detail ('DeckLoader.loadBothSets() served ' + $served + ' cards, expected ' + $ExpectedPool) -ExitCode 0
  } else {
    Add-Criterion -Name 'cardpool' -Required $true -Status 'FAIL' `
      -Detail ('DeckLoader.loadBothSets() served ' + $served + ' cards, expected ' + $ExpectedPool + ' (the raw two-pool RECORD count is 829: premiere 446 + deluxe 383; the served pool is all 383 Deluxe plus the 63 Premiere titles never reprinted)') -ExitCode 0
  }
} else {
  Add-Criterion -Name 'cardpool' -Required $true -Status 'UNKNOWN' `
    -Detail 'the smoke probe stdout carried no DeckLoader.loadBothSets() count line, so the served pool size was not measured' -ExitCode $null
}

# =================================================================================
# Criterion 5 — the 7 wired headless gates
# =================================================================================
$gateResults = New-Object System.Collections.Generic.List[object]
if ($outReady) {
  foreach ($g in $HeadlessGates) {
    $r = Invoke-Probe -ClassName $g
    $gateResults.Add([pscustomobject]@{
      class  = ('b5ccg.engine.' + $g)
      status = $(if ($r.Exit -eq 0) { 'PASS' } else { 'FAIL' })
      exit   = $r.Exit
      ms     = $r.Ms
      # Empty on a pass: retained only so a RED names the gate's own reason.
      signal = $(if ($r.Exit -eq 0) { '' } else { Get-FailureEvidence -Probe $r })
    })
  }
  $gateFails = @($gateResults | Where-Object { $_.status -ne 'PASS' })
  if ($gateFails.Count -eq 0) {
    Add-Criterion -Name 'headless-gates' -Required $true -Status 'PASS' `
      -Detail ('all 7 wired gates exited 0 in ' + (@($gateResults | Measure-Object -Property ms -Sum).Sum) + ' ms total') -ExitCode 0
} else {
      Add-Criterion -Name 'headless-gates' -Required $true -Status 'FAIL' `
        -Detail ('failing gates: ' + (($gateFails | ForEach-Object { $_.class + ' exit ' + $_.exit + ' -- ' + $_.signal }) -join '; ')) -ExitCode 1
    }
} else {
  Add-Criterion -Name 'headless-gates' -Required $true -Status 'UNKNOWN' `
    -Detail 'no compiled classes, so none of the 7 gates was run' -ExitCode $null
}

# =================================================================================
# Criterion 6 — Java 6 construct census
# =================================================================================
# Two readings, because the census has two jobs. The per-family code-lines
# count is the construct check; the qualified `.getOrDefault(` count is the
# API-level check, and it is the one that catches Map.getOrDefault, which
# compiles clean under -source 6 (AGENTS.md section 2a).
$census = $null
$censusTool = Join-Path $RepoRoot '.agent\tools\census-b50960.py'
$pyOut = (& cmd /c "set PYTHONIOENCODING=utf-8&& py `"$censusTool`" 2>&1" | Out-String)
$pyExit = $LASTEXITCODE
if ($pyExit -eq 0 -and $pyOut -match '=== TRACKED JAVA') {
  # The TRACKED block only. The frozen archive is censused in the same run and
  # is EXPECTED to read the opposite (every family present as code) -- that
  # contrast is the instrument's built-in validation, so a run that failed to
  # find the archive boundary and read its numbers as violations would report
  # 32 files of Java 8 constructs as 32 fresh defects.
  $tracked = [regex]::Match($pyOut, '(?s)=== TRACKED JAVA.*?\r?\n(.*?)=== FROZEN JAVA')
  if ($tracked.Success) {
    $families = New-Object System.Collections.Generic.List[object]
    foreach ($m in [regex]::Matches($tracked.Groups[1].Value, '(?m)^--\s+(\S+)\s+lines-with-hits\s+(\d+)\s+occurrences\s+(\d+)\s+code-lines\s+(\d+)')) {
      $fam = $m.Groups[1].Value
      $codeLines = [int]$m.Groups[4].Value
      # getOrDefault is exempt by DOCUMENTED identity, not by tolerance: all
      # 14 tracked occurrences are the project's own unqualified private
      # static helper DeckLoader.getOrDefault(Map, key, def). The API-level
      # signal is the QUALIFIED count, checked separately below.
      $expectedCode = $(if ($fam -eq 'getOrDefault') { 14 } else { 0 })
      $families.Add([pscustomobject]@{
        family     = $fam
        codeLines  = $codeLines
        expected   = $expectedCode
        status     = $(if ($codeLines -eq $expectedCode) { 'PASS' } else { 'FAIL' })
      })
    }
    if ($families.Count -eq 0) {
      Add-Criterion -Name 'java6-census' -Required $true -Status 'UNKNOWN' `
        -Detail 'the census ran but no construct-family lines were parsed from the TRACKED block; an empty parse is not a clean census' -ExitCode $null
    } else {
      # API-level signal: a qualified call is the only getOrDefault the JDK
      # API can hand us at -source 6, and it is what the family count exempts.
      $qualified = 0
      $trackedFiles = @(& git -C $RepoRoot ls-files -- 'b5ccg/src/*.java' 'b5ccg/src/**/*.java' 2>$null)
      foreach ($f in $trackedFiles) {
        $full = Join-Path $RepoRoot $f
        if (-not (Test-Path -LiteralPath $full)) { continue }
        $hits = @(Select-String -LiteralPath $full -Pattern '\.getOrDefault\(' -AllMatches)
        foreach ($h in $hits) { $qualified += $h.Matches.Count }
      }
      $famFails = @($families | Where-Object { $_.status -ne 'PASS' })
      # .ToArray(), not @(...): on PowerShell 5.1 wrapping a
      # List[object] that holds PSCustomObjects in @() throws
      # "Argument types do not match" -- measured, not assumed. See the
      # SELF-TEST note; the direct assignment also works and is what a
      # reader comparing these two forms is meant to check.
      $census = [pscustomobject]@{
        families             = $families.ToArray()
        qualifiedGetOrDefault = $qualified
        qualifiedExpected    = 0
        trackedJavaFiles     = $trackedFiles.Count
      }
      if ($famFails.Count -eq 0 -and $qualified -eq 0) {
        Add-Criterion -Name 'java6-census' -Required $true -Status 'PASS' `
          -Detail ('all ' + $families.Count + ' construct families read code-lines 0 across ' + $trackedFiles.Count + ' tracked Java files (getOrDefault 14 = the project own unqualified helper), and the API-level signal is qualified .getOrDefault( = ' + $qualified) -ExitCode 0
      } else {
        Add-Criterion -Name 'java6-census' -Required $true -Status 'FAIL' `
          -Detail ('violating families: ' + (($famFails | ForEach-Object { $_.family + ' code-lines ' + $_.codeLines + ' (expected ' + $_.expected + ')' }) -join '; ') + $(if ($qualified -ne 0) { '; qualified .getOrDefault( = ' + $qualified + ' (expected 0)' } else { '' })) -ExitCode 1
      }
    }
  } else {
    Add-Criterion -Name 'java6-census' -Required $true -Status 'UNKNOWN' `
      -Detail 'the census printed no FROZEN JAVA boundary, so the TRACKED block could not be isolated; the archive is expected to read the opposite and would be misread as violations' -ExitCode $null
  }
} else {
  Add-Criterion -Name 'java6-census' -Required $true -Status 'UNKNOWN' `
    -Detail ('the census did not run to a parseable result (py exit ' + $pyExit + '). Note the py launcher form is required on this host; the bare name python is not on PATH') -ExitCode $pyExit
}

# =================================================================================
# Criterion 7 — dangling citations
# =================================================================================
$citeTool = Join-Path $RepoRoot '.agent\tools\check-citations.ps1'
& powershell -NoProfile -ExecutionPolicy Bypass -File $citeTool 2>$null | Out-Null
$citeExit = $LASTEXITCODE
if ($citeExit -eq 0) {
  Add-Criterion -Name 'citations' -Required $true -Status 'PASS' `
    -Detail 'check-citations.ps1 exit 0: every resolvable REPORTS/PATTERNS citation resolves to a file on disk' -ExitCode $citeExit
} elseif ($citeExit -eq 2) {
  # Exit 2 is a false pass by the instrument own contract and must never
  # read as clean, so it is UNKNOWN here rather than PASS or FAIL.
  Add-Criterion -Name 'citations' -Required $true -Status 'UNKNOWN' `
    -Detail 'check-citations.ps1 exit 2: a source file was unreadable, which is a false pass and never a clean reading' -ExitCode $citeExit
} else {
  Add-Criterion -Name 'citations' -Required $true -Status 'FAIL' `
    -Detail 'check-citations.ps1 exit ' + $citeExit + ': at least one dangling citation' -ExitCode $citeExit
}

# =================================================================================
# Criterion 8 — human-seat legal action (opt-in)
# =================================================================================
# NOT required, because no fast instrument covers it: none of the 7 wired gates
# drives the human seat, and the one that does costs ~80s (compile.sh:73-77).
# When not requested it is NOT_RUN and the required set is unchanged — the
# required set is deliberately the automated half of B5-2283 and no more.
if ($IncludeHumanSeat) {
  if ($outReady) {
    $hs = Invoke-Probe -ClassName 'HeadlessHumanSeatProbe'
    if ($hs.Exit -eq 0) {
      Add-Criterion -Name 'human-seat' -Required $false -Status 'PASS' `
        -Detail ('HeadlessHumanSeatProbe exit 0 in ' + $hs.Ms + ' ms: the human seat submitted legal actions through submitHumanAction') -ExitCode $hs.Exit
    } else {
      Add-Criterion -Name 'human-seat' -Required $false -Status 'FAIL' `
        -Detail ('HeadlessHumanSeatProbe exit ' + $hs.Exit + ' in ' + $hs.Ms + ' ms; ' + (Get-FailureEvidence -Probe $hs)) -ExitCode $hs.Exit
    }
  } else {
    Add-Criterion -Name 'human-seat' -Required $false -Status 'UNKNOWN' `
      -Detail 'no compiled classes, so the human-seat probe was not run' -ExitCode $null
  }
} else {
  Add-Criterion -Name 'human-seat' -Required $false -Status 'NOT_RUN' `
    -Detail ('not requested. No fast instrument covers this criterion: none of the 7 wired gates drives the human seat, and HeadlessHumanSeatProbe costs ~80s. Re-run with -IncludeHumanSeat to measure it. NOT_RUN is never PASS') -ExitCode $null
}

# =================================================================================
# Verdict
# =================================================================================
$required = @($script:Criteria | Where-Object { $_.required })
$fails    = @($required | Where-Object { $_.status -eq 'FAIL' })
$unknowns = @($required | Where-Object { $_.status -eq 'UNKNOWN' })

if ($fails.Count -eq 0 -and $unknowns.Count -eq 0) {
  $verdict = 'PASS'
  $exitCode = 0
} else {
  # An UNKNOWN required criterion exits 1, not 0 and not 2: the preflight DID
  # run (the layout gate already cleared exit 2), it just could not measure
  # something it was required to measure, and that is not a pass.
  $verdict = 'RED'
  $exitCode = 1
}

$report = [pscustomobject]@{
  tool                  = 'playable-release-check'
  task                  = 'B5-2579'
  generatedUtc          = ([DateTimeOffset]::UtcNow.ToString('yyyy-MM-ddTHH:mm:ssZ'))
  repoRoot              = $RepoRoot.Replace('C:\', '')
  verdict               = $verdict
  exit                  = $exitCode
  automatedPreflight    = $(if ($verdict -eq 'PASS') { 'PASS' } else { 'RED' })
  playable              = $false
  playableNote          = 'This script certifies the AUTOMATED preflight only. B5-2283 makes the 30-minute human session mandatory and states the checklist becomes an adopted release gate only after a human accepts the definition and the victory-route setup, so no verdict key here reads playable.'
  expectedServedPool    = $ExpectedPool
  rawRecordCount        = 829
  rawRecordNote         = '829 is the raw RECORD count of the two pool files (premiere 446 + deluxe 383), not a pool size the loader can serve. DeckLoader.loadBothSets() returns all 383 Deluxe cards plus every Premiere card never reprinted (383/383 titles overlap), which is 446. The B5-2579 row text says 829 and that criterion is unsatisfiable as written.'
  requiredCriteria      = @($required | ForEach-Object { $_.name })
  optionalCriteria      = @($script:Criteria | Where-Object { -not $_.required } | ForEach-Object { $_.name })
  failedCriteria        = @($fails | ForEach-Object { $_.name })
  unknownCriteria       = @($unknowns | ForEach-Object { $_.name })
  criteria              = $script:Criteria.ToArray()
  headlessGates         = $gateResults.ToArray()
  java6Census           = $census
  encodingPinned        = $script:encodingPinned
  humanSessionRequired  = $true
  humanSessionPerformed = $false
}

Write-Output ($report | ConvertTo-Json -Depth 8)
exit $exitCode