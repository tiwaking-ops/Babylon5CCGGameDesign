#Requires -Version 3.0
<#
.SYNOPSIS
    Line-ending drift checker for shared append targets (B5-1499).

.DESCRIPTION
    Read-only. For each path it reports three facts and one verdict:

      * WORKING  - the dominant line ending of the file on disk right now.
      * HEAD     - the dominant line ending of the committed blob at HEAD.
      * UNIFORM  - whether the working file is internally uniform.
      * VERDICT  - MIXED, DRIFT, MATCH, or UNTRACKED.

    Verdicts:
      MIXED     the working file contains BOTH CRLF and bare-LF terminators.
                This is the dangerous one: it is not a formatting drift, it is a
                file being rewritten by two writers with different conventions
                inside a single append target.
      DRIFT     the working file is uniform but its ending differs from the
                HEAD blob. Legitimate but worth naming: the whole file was
                converted at some point.
      MATCH     uniform and equal to HEAD.
      UNTRACKED not in the HEAD tree, so there is no canonical to compare
                against (freshly created files). Reported, never a failure.

    B5-1453 measured two whole-file ending flips inside 50 minutes on
    docs/DECISIONS.md and named the convention that prevents them: sample the
    dominant ending immediately before the write and match it; appends are
    byte-appends, never rewrites. This tool is the measurement half of that
    convention - it is the receipt that makes a flip attributable after the fact.

    HEAD bytes are read through `git cat-file blob` and counted as BYTES, never
    as text. Decoding to a string first would let the reader normalise the
    endings and report the canonical ending as whatever the working copy
    happened to use - the instrument would then agree with the file it is
    supposed to be auditing.

    Exit codes (same 0-1-2 discipline as run-dup-census.ps1):
      0  every path is MATCH, UNTRACKED, or absent-and-skipped (no findings)
      1  at least one path is DRIFT or MIXED (findings; see the table)
      2  a required input was missing or unreadable (distinct from a clean pass,
         so a checker that could not read its input never reports as clean)

.PARAMETER Path
    One or more files to check. Defaults to the shared append targets this repo
    treats as byte-append surfaces.

.PARAMETER Root
    Repository root used to resolve HEAD. Defaults to the parent of this script's
    parent directory (.agent/tools -> .agent -> repo root).

.PARAMETER SelfTest
    Build a throwaway git repository under $ScratchRoot with an LF-committed
    fixture, then prove all four verdicts on scratch copies. Exits non-zero if
    any verdict does not come back as specified. Writes only inside $ScratchRoot.

.PARAMETER ScratchRoot
    Directory the self-test creates and removes. Never overlaps a scanned path.

.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/line-ending-check.ps1

.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/line-ending-check.ps1 -SelfTest

.NOTES
    Never edits, and never writes to, any path it scans. The self-test writes
    only under -ScratchRoot, which it also removes on success.
#>
[CmdletBinding()]
param(
    [string[]] $Path,
    [string]   $Root,
    [switch]   $SelfTest,
    [string]   $ScratchRoot
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = if ($Root) { $Root } else { Split-Path (Split-Path $PSScriptRoot -Parent) -Parent }

if (-not $ScratchRoot) {
    $ScratchRoot = Join-Path $env:TEMP ('b51499-selftest-' + [Guid]::NewGuid().ToString('N').Substring(0, 8))
}

# ── byte-level ending census ──────────────────────────────────────────────────

# Returns a hashtable: crlf, lf (bare), cr (lone), dominant, uniform, total.
function Get-EndingCensus {
    param([byte[]] $Bytes)

    $crlf = 0; $bareLf = 0; $loneCr = 0
    for ($i = 0; $i -lt $Bytes.Length; $i++) {
        if ($Bytes[$i] -eq 13) {
            # CR is only a terminator when followed by LF; a lone CR is counted
            # separately rather than folded into either LF family.
            if (($i + 1) -lt $Bytes.Length -and $Bytes[$i + 1] -eq 10) {
                $crlf++
                $i++
            } else {
                $loneCr++
            }
        } elseif ($Bytes[$i] -eq 10) {
            $bareLf++
        }
    }

    $lfTotal = $crlf + $bareLf
    if ($lfTotal -eq 0) {
        $dominant = if ($loneCr -gt 0) { 'CR' } else { 'NONE' }
    } elseif ($crlf -gt $bareLf) {
        $dominant = 'CRLF'
    } elseif ($bareLf -gt $crlf) {
        $dominant = 'LF'
    } else {
        $dominant = 'TIE'
    }

    # Uniform means every terminator belongs to the dominant LF family, and the
    # file ends with one if it has any. A file with no terminator at all is
    # uniform (there is nothing to disagree about).
    $uniform = $true
    if ($lfTotal -gt 0) {
        $minor = if ($dominant -eq 'CRLF') { $bareLf } else { $crlf }
        $uniform = ($minor -eq 0) -and ($loneCr -eq 0)
    } elseif ($loneCr -gt 0) {
        $uniform = $false
    }

    return @{
        crlf     = $crlf
        lf       = $bareLf
        cr       = $loneCr
        lfTotal  = $lfTotal
        dominant = $dominant
        uniform  = $uniform
        bytes    = $Bytes.Length
    }
}

# Reads a git blob as raw bytes. Decoding to text here would defeat the tool.
function Get-HeadBlobBytes {
    param([string] $RepoRoot, [string] $RelPath)

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName               = 'git'
    $psi.Arguments              = 'cat-file blob "HEAD:' + $RelPath.Replace('\', '/') + '"'
    $psi.WorkingDirectory       = $RepoRoot
    $psi.UseShellExecute        = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError  = $true
    $psi.CreateNoWindow         = $true

    $proc = [System.Diagnostics.Process]::Start($psi)
    $ms = New-Object System.IO.MemoryStream
    $proc.StandardOutput.BaseStream.CopyTo($ms)
    $stderr = $proc.StandardError.ReadToEnd()
    $proc.WaitForExit()

    if ($proc.ExitCode -ne 0) {
        return $null   # absent from HEAD, not an error
    }
    return $ms.ToArray()
}

# HEAD ending for a path, or $null when the path is not in the HEAD tree.
function Get-HeadEnding {
    param([string] $RepoRoot, [string] $FullPath)

    $rel = $FullPath
    $prefix = $RepoRoot.TrimEnd('\', '/') + [System.IO.Path]::DirectorySeparatorChar
    if ($rel.StartsWith($prefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        $rel = $rel.Substring($prefix.Length)
    }

    $bytes = Get-HeadBlobBytes -RepoRoot $RepoRoot -RelPath $rel
    if ($null -eq $bytes) { return $null }

    $census = Get-EndingCensus -Bytes $bytes
    return [pscustomobject] @{
        ending   = $census.dominant
        uniform  = $census.uniform
        crlf     = $census.crlf
        lf       = $census.lf
        bytes    = $census.bytes
    }
}

function Format-Census {
    param([hashtable] $Census)
    return ('CRLF={0} LF={1} CR={2}' -f $Census.crlf, $Census.lf, $Census.cr)
}

# ── the check ────────────────────────────────────────────────────────────────

function Invoke-LineEndingCheck {
    param([string] $RepoRoot, [string[]] $Paths)

    $rows = @()
    foreach ($p in $Paths) {
        $full = if ([System.IO.Path]::IsPathRooted($p)) { $p } else { Join-Path $RepoRoot $p }
        if (-not (Test-Path -LiteralPath $full)) {
            $rows += [pscustomobject] @{
                Path = $p; Working = 'ABSENT'; Head = '-'; Uniform = '-'
                Verdict = 'ABSENT'; Detail = 'not on disk'
            }
            continue
        }

        $bytes = [System.IO.File]::ReadAllBytes($full)
        $wc = Get-EndingCensus -Bytes $bytes
        $head = Get-HeadEnding -RepoRoot $RepoRoot -FullPath $full

        if (-not $wc.uniform) {
            $verdict = 'MIXED'
            $detail = 'both terminator families present in one file'
        } elseif ($null -eq $head) {
            $verdict = 'UNTRACKED'
            $detail = 'no HEAD blob to compare'
        } elseif ($head.ending -eq $wc.dominant) {
            $verdict = 'MATCH'
            $detail = 'working matches HEAD'
        } else {
            $verdict = 'DRIFT'
            $detail = ('working {0} vs HEAD {1}' -f $wc.dominant, $head.ending)
        }

        $rows += [pscustomobject] @{
            Path    = $p
            Working = $wc.dominant
            Head    = if ($null -eq $head) { '-' } else { $head.ending }
            Uniform = if ($wc.uniform) { 'yes' } else { 'no' }
            Verdict = $verdict
            Detail  = ('{0}; {1}' -f (Format-Census -Census $wc), $detail)
        }
    }
    return $rows
}

# ── self-test ─────────────────────────────────────────────────────────────────

# Exit status is carried in a script-scoped variable, NOT as the function's
# return value. Assigning the function's output to a variable would swallow
# every Write-Output inside it - the self-test would print nothing and still
# report success, which is the one failure mode a self-test cannot have.
$script:SelfTestFailures = @()

function Invoke-SelfTest {
    param([string] $RepoRoot, [string] $Scratch)

    Write-Output '=== line-ending-check self-test ==='
    Write-Output ('scratch: {0}' -f $Scratch)
    Write-Output ('repo:    {0}' -f $RepoRoot)

    if (Test-Path -LiteralPath $Scratch) { Remove-Item -LiteralPath $Scratch -Recurse -Force }
    New-Item -ItemType Directory -Path $Scratch -Force | Out-Null
    $repo = Join-Path $Scratch 'repo'
    New-Item -ItemType Directory -Path $repo -Force | Out-Null

    function Invoke-Git {
        param([string] $Dir, [string[]] $GitArgs)
        $psi = New-Object System.Diagnostics.ProcessStartInfo
        $psi.FileName         = 'git'
        # Quote any argument carrying a space: -m 'lf baseline' otherwise arrives
        # at git as two arguments and the second is read as a pathspec.
        $psi.Arguments        = (($GitArgs | ForEach-Object {
            if ($_ -match '\s') { '"' + $_ + '"' } else { $_ }
        }) -join ' ')
        $psi.WorkingDirectory = $Dir
        $psi.UseShellExecute  = $false
        $psi.RedirectStandardOutput = $true
        $psi.RedirectStandardError  = $true
        $psi.CreateNoWindow   = $true
        $proc = [System.Diagnostics.Process]::Start($psi)
        $out = $proc.StandardOutput.ReadToEnd()
        $proc.StandardError.ReadToEnd() | Out-Null
        $proc.WaitForExit()
        if ($proc.ExitCode -ne 0) {
            throw ('git {0} failed in {1}' -f ($GitArgs -join ' '), $Dir)
        }
        return $out
    }

    Invoke-Git $repo @('init', '-q') | Out-Null
    Invoke-Git $repo @('config', 'user.email', 'selftest@example.invalid') | Out-Null
    Invoke-Git $repo @('config', 'user.name', 'selftest') | Out-Null
    Invoke-Git $repo @('config', 'core.autocrlf', 'false') | Out-Null

    # Commit ALL three fixtures with LF endings, so HEAD says LF for each of
    # them and the three verdicts differ only by what the working copy now
    # holds. Committing only lf.txt would make the other two UNTRACKED and the
    # DRIFT case would never be exercised - the self-test would "pass" while
    # testing nothing. core.autocrlf=false keeps git from normalising on add.
    foreach ($name in @('lf.txt', 'crlf.txt', 'mixed.txt')) {
        [System.IO.File]::WriteAllText((Join-Path $repo $name), ("a`nb`nc`n"))
    }
    Invoke-Git $repo @('add', 'lf.txt', 'crlf.txt', 'mixed.txt') | Out-Null
    Invoke-Git $repo @('commit', '-q', '-m', 'lf baseline') | Out-Null

    # working copies: LF (match), CRLF (drift), mixed. mixed.txt is now TRACKED,
    # so MIXED must outrank the HEAD comparison - it does, because the
    # uniformity test runs first.
    [System.IO.File]::WriteAllText((Join-Path $repo 'lf.txt'), ("a`nb`nc`n"))
    [System.IO.File]::WriteAllBytes((Join-Path $repo 'crlf.txt'),
        [System.Text.Encoding]::UTF8.GetBytes("a`r`nb`r`nc`r`n"))
    [System.IO.File]::WriteAllBytes((Join-Path $repo 'mixed.txt'),
        [System.Text.Encoding]::UTF8.GetBytes("a`r`nb`nc`n"))

    $expect = @{ 'lf.txt' = 'MATCH'; 'crlf.txt' = 'DRIFT'; 'mixed.txt' = 'MIXED' }
    $rows = Invoke-LineEndingCheck -RepoRoot $repo -Paths @('lf.txt', 'crlf.txt', 'mixed.txt')

    $rows | Format-Table -AutoSize | Out-String | Write-Output

    $failures = @()
    foreach ($r in $rows) {
        $want = $expect[$r.Path]
        $ok = ($r.Verdict -eq $want)
        Write-Output ('{0,-10} want={1,-10} got={2,-10} {3}' -f `
            $r.Path, $want, $r.Verdict, $(if ($ok) { 'OK' } else { 'MISMATCH' }))
        if (-not $ok) { $failures += ('{0}: want {1}, got {2}' -f $r.Path, $want, $r.Verdict) }
    }

    # A fourth case: untracked and uniform must read UNTRACKED, proving the tool
    # does not call every new file a finding.
    $clean = Join-Path $repo 'clean.txt'
    [System.IO.File]::WriteAllText($clean, ("x`ny`n"))
    $untracked = Invoke-LineEndingCheck -RepoRoot $repo -Paths @('clean.txt')
    $untracked | Format-Table -AutoSize | Out-String | Write-Output
    if ($untracked[0].Verdict -ne 'UNTRACKED') {
        $failures += ('clean.txt: want UNTRACKED, got {0}' -f $untracked[0].Verdict)
        Write-Output ('clean.txt   want=UNTRACKED  got={0}  MISMATCH' -f $untracked[0].Verdict)
    } else {
        Write-Output 'clean.txt   want=UNTRACKED  got=UNTRACKED  OK'
    }

    Remove-Item -LiteralPath $Scratch -Recurse -Force

    if ($failures.Count -gt 0) {
        Write-Output ''
        Write-Output 'SELFTEST FAILED:'
        foreach ($f in $failures) { Write-Output ('  ' + $f) }
        $script:SelfTestFailures = $failures
        return
    }
    Write-Output ''
    Write-Output 'SELFTEST PASSED - all four verdicts reproduced (MATCH, DRIFT, MIXED, UNTRACKED).'
}

# ── main ─────────────────────────────────────────────────────────────────────

if ($SelfTest) {
    Invoke-SelfTest -RepoRoot $repoRoot -Scratch $ScratchRoot
    if ($script:SelfTestFailures.Count -gt 0) { exit 1 }
    exit 0
}

if (-not $Path -or @($Path).Count -eq 0) {
    $Path = @(
        'docs/DECISIONS.md',
        '.agent/TASK_LEDGER.md',
        '.agent/00_BOOT.md',
        '.agent/AGENT_LOOP.md',
        'AGENTS.md',
        '.agent/run-queue.ps1',
        '.agent/tools/run-dup-census.ps1',
        '.agent/tools/dup-census.ps1',
        '.agent/tools/ledger-query.ps1',
        '.agent/tools/validate-heartbeats.ps1'
    )
}

if (-not (Test-Path -LiteralPath $repoRoot)) {
    Write-Error ('repo root not found: {0}' -f $repoRoot)
    exit 2
}

$missing = @($Path | Where-Object { -not (Test-Path -LiteralPath $_) -and -not (Test-Path -LiteralPath (Join-Path $repoRoot $_)) })
# @() on the result too: a single-path call unrolls to a scalar under
# Set-StrictMode, and $rows.Count then fails after the table has already printed.
$rows = @(Invoke-LineEndingCheck -RepoRoot $repoRoot -Paths $Path)

$rows | Format-Table -AutoSize | Out-String | Write-Output

$findings = @($rows | Where-Object { $_.Verdict -eq 'DRIFT' -or $_.Verdict -eq 'MIXED' })

Write-Output ('scanned {0} path(s): {1} MATCH, {2} DRIFT, {3} MIXED, {4} UNTRACKED, {5} ABSENT' -f
    $rows.Count,
    @($rows | Where-Object { $_.Verdict -eq 'MATCH' }).Count,
    @($rows | Where-Object { $_.Verdict -eq 'DRIFT' }).Count,
    @($rows | Where-Object { $_.Verdict -eq 'MIXED' }).Count,
    @($rows | Where-Object { $_.Verdict -eq 'UNTRACKED' }).Count,
    @($rows | Where-Object { $_.Verdict -eq 'ABSENT' }).Count)

if ($findings.Count -gt 0) {
    Write-Output ''
    foreach ($f in $findings) {
        Write-Output ('FINDING {0} {1} - {2}' -f $f.Verdict, $f.Path, $f.Detail)
    }
    Write-Output ''
    Write-Output ('FAIL - {0} finding(s). B5-1453 convention: sample the dominant ending immediately before the write and match it; appends are byte-appends, never rewrites.' -f $findings.Count)
    exit 1
}

if ($missing.Count -gt 0) {
    Write-Output ''
    Write-Output 'NOTE: paths not found on disk (skipped, not findings):'
    foreach ($m in $missing) { Write-Output ('  ' + $m) }
}

Write-Output ''
Write-Output 'PASS - no DRIFT and no MIXED among the scanned paths.'
exit 0