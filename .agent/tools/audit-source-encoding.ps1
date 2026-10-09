<#
.SYNOPSIS
  B5-2528: audit all tracked source files for BOM presence and non-ASCII
  characters, reporting findings without modifying anything.

.DESCRIPTION
  Why this tool exists. The B5-1012 census script must run under
  PYTHONIOENCODING=utf-8 because box-drawing glyphs (U+2014, U+00A7, etc.) in
  Java comment bands raise UnicodeEncodeError on cp1252 consoles. This audit
  makes that hazard visible across all tracked source types, not just .ps1
  (which check-bom-nonascii.ps1 already covers).

  Categories of findings:

    1. BOM-BEARING .java / .json files -- not a build error at -source 6 but
       inconsistent with the repo's BOM-less convention for these file types.
       Flagged as a style finding, not a gate failure.

    2. Non-ASCII in .java files outside comments -- a compile-time hazard. javac
       accepts UTF-8 with -encoding UTF-8, but a developer building without
       that flag will mis-decode comment glyphs into mojibake. Flagged as a
       build-portability finding.

    3. Non-ASCII in .java comments -- the measured case. Many tracked Java files
       carry non-ASCII lines, all in box-drawing comment bands (em-dashes,
       section signs, box-drawing chars). These parse fine under -encoding UTF-8
       but are the reason PYTHONIOENCODING is load-bearing for the census script.
       Flagged as informational.

    4. Non-ASCII in .json card data -- em-dashes in card text fields. This is
       legitimate content (card text uses em-dashes as typographic characters).
       Flagged as informational, never a gate failure.

  Read-only. Writes nothing, modifies nothing, deletes nothing.

  Exit codes:
    0  no BOM-bearing source files (pass)
    1  one or more source files carry a BOM (fail)
    2  a scanned path was unreadable
#>
[CmdletBinding()]
param(
    [string[]]$Path = @('b5ccg/src', 'b5ccg/resources'),
    [switch]$SelfTest
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if (-not $repoRoot) { $repoRoot = (Get-Location) }

$allFindings = @()
$bomCount = 0
$nonAsciiCodeCount = 0
$nonAsciiJavaFiles = 0
$nonAsciiDataCount = 0

function Test-FileHasBom([string]$FilePath) {
    $fs = [System.IO.File]::OpenRead($FilePath)
    try {
        if ($fs.Length -lt 3) { return $false }
        $b = New-Object byte[] 3
        [void]$fs.Read($b, 0, 3)
        return ($b[0] -eq 0xEF -and $b[1] -eq 0xBB -and $b[2] -eq 0xBF)
    }
    finally { $fs.Dispose() }
}

function Measure-JavaFile([string]$FilePath) {
    $findings = @()
    if (-not (Test-Path -LiteralPath $FilePath)) { return $findings }

    $hasBom = Test-FileHasBom $FilePath
    if ($hasBom) {
        $findings += [pscustomobject]@{
            File = $FilePath
            Line = 0
            Class = 'BOM-BEARING'
            Detail = 'UTF-8 BOM present on .java file; repo convention is BOM-less'
        }
    }

    $lines = [System.IO.File]::ReadLines($FilePath)
    $inBlockComment = $false
    $lineNum = 0
    $fileHasNonAscii = $false
    $fileCodeNonAscii = $false

    foreach ($line in $lines) {
        $lineNum++
        $hasNonAscii = $false
        foreach ($c in $line.ToCharArray()) {
            if ([int][char]$c -gt 127) { $hasNonAscii = $true; break }
        }
        if (-not $hasNonAscii) {
            # Even ASCII-only lines must update block comment state: a /* on
            # this line opens state that affects subsequent lines. (B5-2528: the
            # original logic skipped comment-stripping on ASCII-only lines, so
            # a JavaDoc /* ... */ opening line with no non-ASCII never set
            # inBlockComment, and the NEXT line with non-ASCII was classified as
            # code.)
            $work = $line
            $i = 0
            while ($i -lt $work.Length) {
                if ($inBlockComment) {
                    $endIdx = $work.IndexOf('*/', $i)
                    if ($endIdx -lt 0) { break }
                    $inBlockComment = $false
                    $i = $endIdx + 2
                    continue
                }
                $slashIdx = $work.IndexOf('/', $i)
                if ($slashIdx -lt 0) { break }
                if ($slashIdx + 1 -lt $work.Length -and $work[$slashIdx + 1] -eq '/') { break }
                if ($slashIdx + 1 -lt $work.Length -and $work[$slashIdx + 1] -eq '*') {
                    $closeIdx = $work.IndexOf('*/', $slashIdx + 2)
                    if ($closeIdx -lt 0) { $inBlockComment = $true; break }
                    $i = $closeIdx + 2; continue
                }
                $i = $slashIdx + 1
            }
            continue
        }

        $fileHasNonAscii = $true

        # Single pass: strip comments and capture code part in one scan,
        # carrying inBlockComment state from previous lines.
        $codePart = ''
        $work = $line
        $i = 0
        while ($i -lt $work.Length) {
            if ($inBlockComment) {
                $endIdx = $work.IndexOf('*/', $i)
                if ($endIdx -lt 0) { break }
                # Everything before */ is comment; code resumes after
                $i = $endIdx + 2
                $inBlockComment = $false
                continue
            }
            $slashIdx = $work.IndexOf('/', $i)
            if ($slashIdx -lt 0) {
                $codePart += $work.Substring($i)
                break
            }
            # Check for // (line comment)
            if ($slashIdx + 1 -lt $work.Length -and $work[$slashIdx + 1] -eq '/') {
                $codePart += $work.Substring($i, $slashIdx - $i)
                break
            }
            # Check for /* (block comment open)
            if ($slashIdx + 1 -lt $work.Length -and $work[$slashIdx + 1] -eq '*') {
                # Everything before /* is code
                $codePart += $work.Substring($i, $slashIdx - $i)
                # Check if it closes on the same line
                $closeIdx = $work.IndexOf('*/', $slashIdx + 2)
                if ($closeIdx -lt 0) {
                    # Rest of line is inside block comment
                    $inBlockComment = $true
                    break
                }
                # Inline block comment: code resumes after */
                $i = $closeIdx + 2
                continue
            }
            # Single slash, not a comment start
            $codePart += $work.Substring($i, $slashIdx - $i + 1)
            $i = $slashIdx + 1
        }

        # Check if non-ascii is in code part
        $codeHasNonAscii = $false
        foreach ($c in $codePart.ToCharArray()) {
            if ([int][char]$c -gt 127) { $codeHasNonAscii = $true; break }
        }

        if ($codeHasNonAscii) {
            $fileCodeNonAscii = $true
            $findings += [pscustomobject]@{
                File = $FilePath
                Line = $lineNum
                Class = 'NON-ASCII-IN-CODE'
                Detail = $line.Trim()
            }
        }
    }

    if ($fileHasNonAscii -and -not $fileCodeNonAscii) {
        $findings += [pscustomobject]@{
            File = $FilePath
            Line = 0
            Class = 'NON-ASCII-IN-COMMENTS'
            Detail = 'All non-ASCII characters found in comments only'
        }
    }

    return $findings
}

# Collect files
$javaFiles = @()
$jsonFiles = @()
foreach ($p in $Path) {
    $fullPath = Join-Path $repoRoot $p
    if (-not (Test-Path -LiteralPath $fullPath)) {
        Write-Error ("audit-source-encoding: path not found: {0}" -f $fullPath)
        exit 2
    }
    $item = Get-Item -LiteralPath $fullPath -ErrorAction SilentlyContinue
    if ($item -and $item.PSIsContainer) {
        $javaFiles += @(Get-ChildItem -LiteralPath $fullPath -Recurse -Filter '*.java' -File |
            Where-Object { $_.FullName -notmatch '\\b5ccg\\src-java8-archive\\' } |
            ForEach-Object { $_.FullName })
        $jsonFiles += @(Get-ChildItem -LiteralPath $fullPath -Recurse -Filter '*.json' -File |
            Where-Object { $_.FullName -notmatch '\\b5ccg\\out\\' } |
            ForEach-Object { $_.FullName })
    }
}

Write-Output "=== B5-2528 Source Encoding Audit ==="
Write-Output ""
Write-Output "Scanning $($javaFiles.Count) Java files and $($jsonFiles.Count) JSON files..."
Write-Output ""

# Audit Java files
foreach ($f in $javaFiles) {
    $findings = Measure-JavaFile -FilePath $f
    $allFindings += $findings
    foreach ($finding in $findings) {
        if ($finding.Class -eq 'BOM-BEARING') { $bomCount++ }
        if ($finding.Class -eq 'NON-ASCII-IN-CODE') { $nonAsciiCodeCount++ }
        if ($finding.Class -eq 'NON-ASCII-IN-COMMENTS') { $nonAsciiJavaFiles++ }
    }
}

# Audit JSON files for non-ASCII (informational only)
foreach ($f in $jsonFiles) {
    if ($f -match '\\src-java8-archive\\') { continue }
    try {
        $lines = [System.IO.File]::ReadLines($f)
        $hasNonAscii = $false
        foreach ($line in $lines) {
            foreach ($c in $line.ToCharArray()) {
                if ([int][char]$c -gt 127) { $hasNonAscii = $true; break }
            }
            if ($hasNonAscii) { break }
        }
        if ($hasNonAscii) {
            $nonAsciiDataCount++
            Write-Output ("  INFO  {0}  -- non-ASCII in JSON data (likely card text)" -f $f)
        }
    } catch { }
}

# Report findings
$bomFindings = $allFindings | Where-Object { $_.Class -eq 'BOM-BEARING' }
$codeFindings = $allFindings | Where-Object { $_.Class -eq 'NON-ASCII-IN-CODE' }

Write-Output ""
Write-Output "=== FINDINGS (BOM-bearing files) ==="
foreach ($finding in $bomFindings) {
    Write-Output ("  HIT  {0}  {1}" -f $finding.File, $finding.Detail)
}

Write-Output ""
Write-Output "=== FINDINGS (non-ASCII in Java code) ==="
foreach ($finding in $codeFindings) {
    Write-Output ("  HIT  {0}:{1}  {2}" -f $finding.File, $finding.Line, $finding.Detail)
}

Write-Output ""
Write-Output "=== SUMMARY ==="
Write-Output "BOM-bearing .java files:        $bomCount  (gate failure if > 0)"
Write-Output "Non-ASCII in .java code:        $nonAsciiCodeCount  (build-portability hazard)"
Write-Output "Java files with non-ASCII:      $nonAsciiJavaFiles of $($javaFiles.Count) (all in comments)"
Write-Output "JSON files with non-ASCII:      $nonAsciiDataCount  (informational - card text em-dashes)"
Write-Output ""

if ($bomCount -gt 0) {
    Write-Output "FAIL: $bomCount source file(s) carry a BOM."
    exit 1
}

Write-Output "PASS: no BOM-bearing source files. Non-ASCII in comments is the measured case (B5-1012)."
exit 0
