#Requires -Version 3.0
<#
.SYNOPSIS
  B5-2746: prove the standing Java 6 census scans exactly the .java set that
  compile.sh hands to javac -- and say so loudly when it stops doing that.

.DESCRIPTION
  Why this tool exists. Until B5-2719 the census counted only tracked files: it read
  82 .java paths under `b5ccg/src` while the build compiled 138, so the API-level
  second look -- the one thing this instrument does that `javac -source 6` cannot,
  because `Map.getOrDefault` compiles clean at source level 6 -- covered 59 percent
  of the tree and nobody noticed for days. B5-2719 widened the file set to an os.walk
  of the working tree and closed that gap. What B5-2719 did NOT close is the reason
  the gap could exist unnoticed: NOTHING ASSERTED THE TWO NUMBERS WERE THE SAME.
  The census printed a denominator; compile.sh printed its own source count; two
  programs produced them and no third ever compared them.

  Measured today, 2026-10-06, before writing a line of this file:

      census DENOMINATOR b5ccg/src : 139 on disk, 82 tracked, 57 untracked
      GNU find b5ccg/src           : 139
      AGENTS.md section 2a records : 138 on disk, 82 tracked, 56 untracked
                                     (re-measured the SAME morning, B5-2719)

  One .java file appeared between those two measurements and the governance
  paragraph in AGENTS.md was already wrong. It was never "wrong" maliciously or
  carelessly -- a prose paragraph cannot track a directory listing, which is exactly
  why the current numbers belong to a program that runs, not to a paragraph that
  was true when it was written.

  WHAT THIS TOOL DOES. It computes the file set FOUR ways and requires agreement:

    1. PS     Get-ChildItem -Recurse -Filter *.java    (PowerShell's own enumerator)
    2. CENSUS the `DENOMINATOR b5ccg/src:` line parsed out of a FRESH run of
              .agent/tools/census-b50960.py. Deliberately parsed from real output
              rather than reimplemented: a second copy of the walk rules is how two
              tools drift into disagreeing (B5-0777).
    3. FIND   `find src -name "*.java" | wc -l`, the command compile.sh itself runs
              (compile.sh line 26). Only when `bash` is on PATH; otherwise this
              signal reads UNKNOWN and is NOT silently counted as zero.
    4. GIT    `git ls-files -- b5ccg/src` filtered to .java. This is the historical
              denominator, kept as a signal rather than dropped, because (a) it is
              the number a regression to tracked-only would reproduce and (b) its
              divergence-from-tree is the untracked count the old instrument hid.

  Asserted equal: PS == CENSUS == FIND(when known); GIT == CENSUS tracked;
  CENSUS disk == CENSUS tracked + CENSUS untracked; every tracked path present in
  the tree; and -ExpectedTree, when supplied, == every tree signal. A zero anywhere
  is NOT agreement: with 139 files expected, a zero means the signal failed to run,
  and it must not be allowed to read as clean (the measure-task-cells lesson: a
  green verdict above a zero count is meaningless).

  THE `find` TRAP, corrected after MEASURING rather than asserting. Windows ships
  C:\Windows\System32\find.exe, a TEXT-SEARCH program unrelated to GNU find, and
  Git for Windows ships C:\Program Files\Git\usr\bin\find.exe. Which one the bare
  name resolves to depends entirely on PATH ORDER and is therefore a property of the
  host, not of this repo -- here (2026-10-06, Get-Command find -All) GNU find wins,
  but a host where System32 comes first gets the text-search tool, which would hand
  back its own exit status and no count at all. Either way the shell must not decide
  it by accident, so this tool never invokes bare `find`: it locates `bash`, then
  runs GNU find INSIDE that shell, converting the Windows path to its MSYS form.
  Same failure class as B5-1541 (a command meaning something different in the shell
  an agent actually has), recorded with what was measured rather than what was
  assumed, because a wrong justification in a header is worse than none.

  Read-only. Writes nothing, edits nothing, touches no .java file.

.OUTPUTS
  Exit 0  every signal agrees (pass). Numbers are printed so they are quotable.
  Exit 1  divergence found; each disagreement is printed with both sides.
  Exit 2  an input was missing or unreadable (repo root, b5ccg/src, the census
          script, or the `py`/`python` launcher). Deliberately NOT 0: a guard that
          could not measure must never report agreement.

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/census-denominator-guard.ps1
.EXAMPLE
  # paste the N from compile.sh's "Compiling N source files..." line
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/census-denominator-guard.ps1 -ExpectedTree 139
.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/census-denominator-guard.ps1 -SelfTest
#>
[CmdletBinding()]
param(
    # Optional build-authored number: the N printed by compile.sh as
    # "Compiling N source files...". Turning the build's own count into an input is
    # the whole point -- otherwise the build and the census stay two unconnected facts.
    [int] $ExpectedTree = -1,

    # Prove this guard can go red. See Invoke-SelfTest.
    [switch] $SelfTest
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$TAG = '[B5-2746]'

$censusRootArg = 'b5ccg/src'

function Write-Line([string] $m) { Write-Output ("{0} {1}" -f $TAG, $m) }

function ConvertTo-MsysPath([string] $Path) {
    # Git Bash needs /c/temp/... form. A Windows path handed to `cd` inside bash
    # either works by luck or produces an empty result; neither is acceptable when
    # the count is evidence.
    $p = $Path -replace '\\', '/'
    if ($p -match '^[A-Za-z]:') {
        $p = '/' + $p.Substring(0, 1).ToLower() + $p.Substring(2)
    }
    return $p
}

function Get-PowerShellTreeCount {
    $files = @(Get-ChildItem -LiteralPath $treeRoot -Recurse -Force -File -Filter '*.java' -ErrorAction Stop)
    return $files.Count
}

function Get-GitTrackedPaths {
    $raw = & git -C "$repoRoot" ls-files -- "$censusRootArg" 2>$null
    if ($LASTEXITCODE -ne 0) { throw "git ls-files exited $LASTEXITCODE" }
    $paths = @()
    foreach ($line in @($raw)) {
        $t = $line.ToString().Trim()
        if ($t.Length -gt 0 -and $t.ToLowerInvariant().EndsWith('.java')) { $paths += $t }
    }
    return $paths
}

function Resolve-PythonLauncher {
    foreach ($name in @('py', 'python', 'python3')) {
        $cmd = Get-Command $name -ErrorAction SilentlyContinue
        if ($null -ne $cmd) { return $cmd.Source }
    }
    return $null
}

function Get-CensusCounts {
    # Runs the real instrument and parses its real output. Windows shell split, so
    # $LASTEXITCODE is the census's own verdict and never this tool's opinion.
    $launcher = Resolve-PythonLauncher
    if ($null -eq $launcher) { throw 'no python launcher found (tried py, python, python3)' }

    # Load-bearing, not decoration: the census prints raw tracked source lines that
    # carry box-drawing glyphs, and the default cp1252 console raises
    # UnicodeEncodeError without this (AGENTS.md section 2a, measured at B5-1012).
    $saved = $env:PYTHONIOENCODING
    $env:PYTHONIOENCODING = 'utf-8'
    try {
        Push-Location -LiteralPath $repoRoot
        try {
            $out = & $launcher "$censusScript" 2>&1
            $code = $LASTEXITCODE
        } finally {
            Pop-Location
        }
    } finally {
        $env:PYTHONIOENCODING = $saved
    }

    # The census exits 1 when a family reports code lines above zero. That is a
    # finding about CONSTRUCTS, not about the denominator, so it does not decide
    # this guard's outcome; parse regardless and report both.
    $disk = $null; $tracked = $null; $untracked = $null
    foreach ($line in @($out)) {
        $s = $line.ToString()
        if ($s -match '^DENOMINATOR\s+b5ccg/src:\s+(\d+)\s+\.java on disk,\s+(\d+)\s+tracked by git,\s+(\d+)\s+untracked') {
            $disk      = [int] $Matches[1]
            $tracked   = [int] $Matches[2]
            $untracked = [int] $Matches[3]
        }
    }
    if ($null -eq $disk) { throw 'census ran but printed no DENOMINATOR b5ccg/src line' }
    return @{ disk = $disk; tracked = $tracked; untracked = $untracked; exit = $code }
}

function Get-GnuFindCount {
    # UNKNOWN is a real answer. A signal that did not run is never 0, because 0
    # participates in comparisons and UNKNOWN must not.
    $bash = Get-Command bash -ErrorAction SilentlyContinue
    if ($null -eq $bash) { return $null }
    try {
        $posix = ConvertTo-MsysPath $srcRoot
        $out = & bash -lc ("cd '{0}' && find src -name '*.java' -type f | wc -l" -f $posix) 2>$null
        $n = ([string] ($out -join '')).Trim()
        if ($n -match '^\d+$') { return [int] $n }
        return $null
    } catch {
        return $null
    }
}

function Compare-Signals {
    param(
        [int] $PsTree,
        [int] $CensusDisk,
        $FindTree,                 # $null = UNKNOWN
        [int] $GitTracked,
        [int] $CensusTracked,
        [int] $CensusUntracked,
        [int] $TrackedMissingFromTree,
        [int] $Expected = -1
    )

    $bad = New-Object System.Collections.ArrayList

    if ($PsTree -ne $CensusDisk) {
        [void] $bad.Add(("tree/PowerShell {0} vs census on-disk {1}" -f $PsTree, $CensusDisk))
    }
    if ($null -ne $FindTree) {
        if ($FindTree -ne $CensusDisk) {
            [void] $bad.Add(("tree/GNU-find {0} vs census on-disk {1}" -f $FindTree, $CensusDisk))
        }
        if ($FindTree -ne $PsTree) {
            [void] $bad.Add(("tree/GNU-find {0} vs tree/PowerShell {1}" -f $FindTree, $PsTree))
        }
    }
    if ($GitTracked -ne $CensusTracked) {
        [void] $bad.Add(("git tracked {0} vs census tracked {1}" -f $GitTracked, $CensusTracked))
    }
    $sum = $CensusTracked + $CensusUntracked
    if ($sum -ne $CensusDisk) {
        [void] $bad.Add(("census tracked+untracked {0} != census on-disk {1}" -f $sum, $CensusDisk))
    }
    if ($TrackedMissingFromTree -ne 0) {
        [void] $bad.Add(("{0} git-tracked .java file(s) absent from the working tree" -f $TrackedMissingFromTree))
    }
    if ($Expected -ge 0) {
        foreach ($pair in @(@('tree/PowerShell', $PsTree), @('tree/GNU-find', $FindTree), @('census on-disk', $CensusDisk))) {
            if ($null -ne $pair[1] -and [int] $pair[1] -ne $Expected) {
                [void] $bad.Add(("{0} {1} != ExpectedTree {2}" -f $pair[0], $pair[1], $Expected))
            }
        }
    }

    # Zero is never agreement. 139 files do not vanish politely.
    if ($PsTree -le 0)      { [void] $bad.Add(("tree/PowerShell count is {0}: the signal did not measure" -f $PsTree)) }
    if ($CensusDisk -le 0)  { [void] $bad.Add(("census on-disk count is {0}: the signal did not measure" -f $CensusDisk)) }

    return @($bad)
}

function Measure-Once {
    # Returns a result object; split from the printing so -SelfTest can reuse it.
    if (-not (Test-Path -LiteralPath $treeRoot))     { throw "missing tree root: $treeRoot" }
    if (-not (Test-Path -LiteralPath $censusScript)) { throw "missing census script: $censusScript" }

    $psTree  = Get-PowerShellTreeCount
    $census  = Get-CensusCounts
    $findTree = Get-GnuFindCount
    $gitPaths = Get-GitTrackedPaths

    # Any tracked .java missing from the tree means git tracks a file the build
    # cannot compile. Small, cheap, and the reverse direction of the old blind spot.
    $missing = 0
    foreach ($p in $gitPaths) {
        $full = Join-Path $repoRoot $p
        if (-not (Test-Path -LiteralPath $full)) { $missing++ }
    }

    return @{
        PsTree       = $psTree
        FindTree     = $findTree
        GitTracked   = $gitPaths.Count
        CensusDisk   = $census.disk
        CensusTracked = $census.tracked
        CensusUntracked = $census.untracked
        TrackedMissing = $missing
        CensusExit   = $census.exit
    }
}

function Invoke-SelfTest {
    Write-Line 'SELFTEST: a guard never seen red is not evidence (AGENT_LOOP, three lies #3).'

    # Part 1: the comparator, fed a synthetic divergence. This proves the DECISION
    # LOGIC can fail, which is the part that decides the exit code.
    # Wrapped in @() on purpose: an EMPTY divergence list unrolls to nothing on
    # return, so the caller would see $null and read it as 'did not measure'
    # instead of 'measured and agreed'. A single finding unrolls to a scalar with
    # no .Count under StrictMode for the same reason.
    $synthetic = @(Compare-Signals -PsTree 139 -CensusDisk 82 -FindTree 139 -GitTracked 82 `
                                  -CensusTracked 82 -CensusUntracked 0 -TrackedMissingFromTree 0)
    if ($synthetic.Count -eq 0) {
        Write-Line 'SELFTEST FAIL part 1: injected 139-vs-82 divergence and the comparator agreed.'
        $script:SelfTestExit = 1
        return
    }
    Write-Line ("SELFTEST part 1 red: comparator reported {0} divergence(s) for a synthetic 139/82 tree." -f $synthetic.Count)
    foreach ($d in $synthetic) { Write-Line ("           {0}" -f $d) }

    # Part 2: the REAL path, ending in a real exit code from a child process. Same
    # enumerators, same parsing, only the ExpectedTree input is wrong by one.
    $real = Measure-Once
    if ($real.PsTree -le 0) { Write-Line 'SELFTEST FAIL part 2: the real tree measured 0.'; $script:SelfTestExit = 1; return }

    $child = & powershell -NoProfile -ExecutionPolicy Bypass -File $PSCommandPath -ExpectedTree ($real.PsTree + 1)
    $childCode = $LASTEXITCODE
    foreach ($l in @($child)) { Write-Output $l }
    if ($childCode -ne 1) {
        Write-Line ("SELFTEST FAIL part 2: ExpectedTree off by one gave exit {0}, expected 1." -f $childCode)
        $script:SelfTestExit = 1
        return
    }
    Write-Line ("SELFTEST part 2 red: ExpectedTree {0} against a real tree of {1} exited 1." -f ($real.PsTree + 1), $real.PsTree)

    # Part 3: the agreeing case must still agree, or the guard is merely always-red.
    $agree = @(Compare-Signals -PsTree 139 -CensusDisk 139 -FindTree 139 -GitTracked 82 `
                               -CensusTracked 82 -CensusUntracked 57 -TrackedMissingFromTree 0)
    if ($agree.Count -ne 0) {
        Write-Line 'SELFTEST FAIL part 3: an agreeing signal set was reported as divergent.'
        $script:SelfTestExit = 1
        return
    }
    Write-Line 'SELFTEST part 3 green: an agreeing set is still reported as agreement.'

    Write-Line 'SELFTEST PASS: red is reachable and green is reachable.'
    $script:SelfTestExit = 0
}

# ------------------------------- main -------------------------------

# Repo root is derived from the script's own location, not from the caller's working
# directory: the census must run from the root for its relative paths, and a tool whose
# answer depends on where you stood when you typed it is not measuring what it claims to
# measure. Derived inside a try because the derivation can FAIL, and a failure here must
# read as exit 2 rather than as a PowerShell parameter-binding error with exit 1 --
# measured: run from C:\tmp it previously died with "Cannot bind argument to parameter
# 'Path' because it is an empty string" before reaching any of its own checks.
try {
    $repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
    if ([string]::IsNullOrWhiteSpace($repoRoot)) {
        throw ("no repo root above this script ({0}); it belongs in .agent/tools/" -f $PSScriptRoot)
    }
    $srcRoot      = Join-Path $repoRoot 'b5ccg'                 # what compile.sh cd's into
    $treeRoot     = Join-Path $srcRoot 'src'                    # what compile.sh finds inside
    $censusScript = Join-Path $repoRoot '.agent/tools/census-b50960.py'
} catch {
    Write-Line ("EXIT 2 unreadable input: {0}" -f $_.Exception.Message)
    Write-Line 'nothing compared; this is not agreement.'
    exit 2
}

# POWERSHELL OUTPUT TRAP, measured while writing this file, because it makes a
# self-test silently worthless. There are TWO independent ways to lose everything a
# helper function printed:
#
#   * `$r = Foo` captures Foo's ENTIRE success stream -- including everything Foo's
#     own helpers wrote -- so `Write-Output` inside Foo is never seen by anyone.
#   * `exit (Foo)` discards Foo's printed output while still exiting with Foo's
#     return value.
#
# Both were reproduced minimally here (`$r = Foo` with a Write-Line helper printed
# nothing; the direct-Write-Output version printed). Either one alone turns this
# guard's self-test into a thing that emits no evidence and exits 0, which is the
# exact green-and-silent shape this file exists to prevent. So the self-test writes
# its own lines through the ordinary path and reports its verdict through a
# SCRIPT-SCOPED variable that no assignment can swallow.
$script:SelfTestExit = 2   # default to failure; only a completed run sets 0

if ($SelfTest) {
    try {
        Invoke-SelfTest
    } catch {
        Write-Line ("SELFTEST ABORTED: {0}" -f $_.Exception.Message)
        $script:SelfTestExit = 2
    }
    exit $script:SelfTestExit
}

try {
    $m = Measure-Once
} catch {
    Write-Line ("EXIT 2 unreadable input: {0}" -f $_.Exception.Message)
    Write-Line 'nothing compared; this is not agreement.'
    exit 2
}

$findShown = if ($null -ne $m.FindTree) { [string] $m.FindTree } else { 'UNKNOWN (bash not on PATH)' }

Write-Line '=== JAVA 6 CENSUS DENOMINATOR GUARD ==='
Write-Line ("tree/PowerShell   Get-ChildItem -Recurse -Filter *.java : {0}" -f $m.PsTree)
Write-Line ("tree/GNU-find     find src -name *.java (compile.sh)    : {0}" -f $findShown)
Write-Line ("tree/census       DENOMINATOR line of a fresh census run : {0} on disk, {1} tracked, {2} untracked" -f $m.CensusDisk, $m.CensusTracked, $m.CensusUntracked)
Write-Line ("tracked/git       git ls-files -- b5ccg/src             : {0}" -f $m.GitTracked)
Write-Line ("tracked missing from tree                              : {0}" -f $m.TrackedMissing)
if ($ExpectedTree -ge 0) { Write-Line ("ExpectedTree (caller-supplied, e.g. compile.sh)        : {0}" -f $ExpectedTree) }
if ($m.CensusExit -ne 0) {
    Write-Line ("note: census-b50960.py itself exited {0}; that is a CONSTRUCT finding, not a denominator one." -f $m.CensusExit)
}

$bad = @(Compare-Signals -PsTree $m.PsTree -CensusDisk $m.CensusDisk -FindTree $m.FindTree `
                         -GitTracked $m.GitTracked -CensusTracked $m.CensusTracked `
                         -CensusUntracked $m.CensusUntracked -TrackedMissingFromTree $m.TrackedMissing `
                         -Expected $ExpectedTree)

if ($bad.Count -eq 0) {
    Write-Line ("AGREE: the census scans exactly the .java set compile.sh compiles ({0} file(s), {1} untracked)." -f $m.CensusDisk, $m.CensusUntracked)
    if ($m.CensusUntracked -gt 0) {
        Write-Line ("       Those {0} untracked files are inside the gate set with no commit behind them; that is this repo's normal state, printed because it used to be invisible." -f $m.CensusUntracked)
    }
    Write-Line '-- ENCODING: UTF-8 (explicit; PYTHONIOENCODING=utf-8 around the census) --'
    exit 0
}

Write-Line ("DIVERGENCE: {0} disagreement(s) between enumerators." -f $bad.Count)
foreach ($d in $bad) { Write-Line ("  - {0}" -f $d) }
Write-Line 'action: re-measure and repair whatever moved; do NOT reconcile native numbers in AGENTS.md to this output without re-running the guard.'
exit 1
