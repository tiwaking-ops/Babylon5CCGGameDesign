<#
.SYNOPSIS
  Read-only conformance-suite coverage census (B5-0633).
.DESCRIPTION
  Enumerates every check( site in the conformance suite, extracts the
  section-code FIRST ARGUMENT regardless of quoting style (straight double,
  straight single, curly open/close, backtick, first argument possibly on a
  following line), prints per-code totals and a per-test-method breakdown,
  and prints the count of sites whose first argument could not be parsed so
  a silently-zero result is visible as a broken parse rather than authority.

  Tolerates letter-suffixed codes (D12b, STH-AI, AMT2, ...): any parsed first
  argument is reported verbatim, no pattern filter is applied.

  MUST NOT read or match narrative fields for coverage (the B5-0609 defect
  class): only the first string argument of a check( call is read; the label
  and condition arguments are never scanned for codes.

  STRICTLY READ-ONLY: this tool writes nothing anywhere. Verification is done
  against a synthetic fixture in a temp directory, never by mutating the
  live suite.
.PARAMETER Path
  Suite file to census. Default: the live HeadlessConformanceTest.java.
.PARAMETER Code
  Optional section-code filter (case-insensitive exact match). When set, the
  per-code table and per-method breakdown list only that code, and matching
  site lines are printed.
.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/suite-coverage.ps1
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/suite-coverage.ps1 -Code AMT2
#>
param(
    [string]$Path = "",
    [string]$Code = ""
)

$ErrorActionPreference = "Stop"

if ($Path -eq "") {
    $repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
    $Path = Join-Path $repoRoot "b5ccg/src/b5ccg/engine/HeadlessConformanceTest.java"
}
if (-not (Test-Path $Path)) {
    Write-Output "SUITE-COVERAGE: file not found: $Path"
    exit 2
}

$lines = [System.IO.File]::ReadAllLines($Path)

$methodRegex = [System.Text.RegularExpressions.Regex]'^\s*(public|private)\s+static\s+void\s+([A-Za-z0-9_]+)\s*\('
$checkRegex  = [System.Text.RegularExpressions.Regex]'(?<![A-Za-z0-9_])check\s*\('

# Quoting styles: opener -> closer. Variant quoting (B5-0594 gap 5) is the
# whole reason this tool exists, so curly quotes and singles are first-class.
$closers = @{}
$closers['"'] = '"'
$closers["'"] = "'"
$closers[[string][char]0x201C] = [string][char]0x201D   # " "
$closers[[string][char]0x2018] = [string][char]0x2019   # ' '
$closers['`'] = '`'

$sites = New-Object System.Collections.ArrayList
$failedSites = New-Object System.Collections.ArrayList
$currentMethod = "(before first test method)"

for ($i = 0; $i -lt $lines.Count; $i++) {
    $line = $lines[$i]
    $mm = $methodRegex.Match($line)
    if ($mm.Success) { $currentMethod = $mm.Groups[2].Value }

    foreach ($cm in $checkRegex.Matches($line)) {
        # Walk forward (across up to 20 following lines) to the first
        # non-whitespace char after the opening paren.
        $li = $i
        $col = $cm.Index + $cm.Length
        $opener = $null
        for ($guard = 0; $guard -lt 20; $guard++) {
            while ($col -lt $lines[$li].Length -and [char]::IsWhiteSpace($lines[$li][$col])) { $col++ }
            if ($col -lt $lines[$li].Length) { $opener = [string]$lines[$li][$col]; break }
            $li++
            if ($li -ge $lines.Count) { break }
            $col = 0
        }

        if ($null -eq $opener -or -not $closers.ContainsKey($opener)) {
            [void]$failedSites.Add([pscustomobject]@{ Line = $i + 1; Method = $currentMethod })
            continue
        }

        $closer = $closers[$opener]
        $sb = New-Object System.Text.StringBuilder
        $c2 = $col + 1
        $done = $false
        $endLine = $li
        while (-not $done) {
            while ($c2 -lt $lines[$li].Length) {
                $ch = $lines[$li][$c2]
                if (([string]$ch) -eq $closer) { $done = $true; break }
                if ($opener -eq '"' -and ([string]$ch) -eq '\') { $c2 += 2; continue }   # escaped char
                [void]$sb.Append($ch)
                $c2++
            }
            if (-not $done) {
                $li++
                if ($li -ge $lines.Count) { $li = $lines.Count - 1; break }
                $c2 = 0
                $endLine = $li
            }
        }
        if (-not $done) {
            [void]$failedSites.Add([pscustomobject]@{ Line = $i + 1; Method = $currentMethod })
            continue
        }
        [void]$sites.Add([pscustomobject]@{
            Code   = $sb.ToString()
            Method = $currentMethod
            Line   = $i + 1
        })
    }
}

$totalSites    = $sites.Count + $failedSites.Count
$parsedCount   = $sites.Count
$failedCount   = $failedSites.Count

Write-Output "=== Suite coverage census (B5-0633) ==="
Write-Output "file          : $Path"
Write-Output "check( sites  : $totalSites"
Write-Output "parsed        : $parsedCount"
Write-Output "UNPARSABLE    : $failedCount   <- must be visible; 0 is only correct if every site truly parses"

if ($Code -ne "") {
    $target = $sites | Where-Object { $_.Code -ieq $Code }
    Write-Output ""
    Write-Output "--- filter: code '$Code' ---"
    Write-Output ("matches: " + @($target).Count)
    foreach ($t in $target) {
        Write-Output ("  line " + $t.Line + "  in " + $t.Method)
    }
}

Write-Output ""
Write-Output "--- per-code totals ---"
$sites | Group-Object Code | Sort-Object Name | ForEach-Object {
    Write-Output ("  " + $_.Name + "  " + $_.Count)
}

Write-Output ""
Write-Output "--- per-test-method breakdown ---"
$sites | Group-Object Method | Sort-Object Name | ForEach-Object {
    $method = $_.Name
    $codes = $_.Group | Group-Object Code | Sort-Object Name |
        ForEach-Object { $_.Name + " x" + $_.Count }
    Write-Output ("  " + $method + ": " + $_.Count + "  (" + ($codes -join ", ") + ")")
}

if ($failedCount -gt 0) {
    Write-Output ""
    Write-Output "--- unparsable sites (first argument not a string literal) ---"
    foreach ($f in $failedSites) {
        Write-Output ("  line " + $f.Line + "  in " + $f.Method)
    }
}

exit 0
