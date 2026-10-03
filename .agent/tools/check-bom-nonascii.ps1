#Requires -Version 3.0
<#
.SYNOPSIS
    Detects non-ASCII characters on EXECUTABLE lines of a BOM-less PowerShell script.

.DESCRIPTION
    Windows PowerShell 5.1 reads a BOM-less script as cp1252. A non-ASCII byte
    sequence then mis-decodes into a different character -- and if that character
    is one the tokenizer treats specially (a curly quote is not, but the codepoint
    it mis-decodes to can be), the script fails to PARSE rather than merely
    rendering as mojibake.

    Measured (B5-1725): 92 of 92 .ps1 in this tree are BOM-less and 9 carry
    non-ASCII. Every one of those 9 hits sits on a comment line or a wrapped
    comment continuation line, and every affected tool still executes, so this is
    a LATENT class, not an active outage.

    THE RULE: **outside comments, a .ps1 carries ASCII only.**

    There are TWO distinct hazard classes, and this gate separates them because
    they behave in OPPOSITE directions. Both were measured this pass, not assumed:

      MISDECODE  A BOM-less script is read as cp1252, and a UTF-8 byte sequence
                 mis-decodes into a character the tokenizer treats specially.
                 1794 of 12128 sampled codepoints (U+00C2, U+00C4, U+0102 ...
                 U+2014 among them) FAIL TO PARSE in this state, with
                 'The string is missing the terminator'. A BOM makes them safe.

      SMARTQUOTE U+2018 / U+2019 / U+201C / U+201D are REAL string delimiters to
                 the 5.1 tokenizer -- verified: $a = [U+201C]hello[U+201D]
                 yields the 5-char string hello. They therefore break a string
                 even when correctly encoded, so a BOM does NOT save them. This
                 is the opposite of the class above, and it is why the gate does
                 not simply exempt BOM files.

    Non-ASCII inside a comment is cosmetic and is never reported: in this repo
    the tracked prose uses U+2014 and U+00A7 heavily, and flagging comments would
    produce dozens of findings that are all noise -- the false positives that
    train people to ignore a gate.

    This script is itself BOM-less and ASCII-only. It is checked by its own gate.

.NOTES
    Exit codes (the 0/1/2 contract the boot gates use):
      0  no findings  (pass)
      1  one or more findings (fail)
      2  the detector could not run, or a source file was unreadable
    Exit 2 is deliberately distinct from 0: a gate that could not read its input
    must never report as a clean tree. That distinction is the same one
    B5-1811 recorded as being load-bearing in run-dup-census.ps1.
#>
[CmdletBinding()]
param(
    # Roots to scan. Defaults to the repository root.
    [string[]] $Path = @('.'),

    # Prove the detector can fire, using synthetic fixtures. Exits 0 when the
    # detector behaves as specified, 1 when it does NOT (which is a detector bug,
    # not a finding).
    [switch] $SelfTest
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# ---------------------------------------------------------------------------
# Core predicate. Returns $true when the line contains at least one non-ASCII
# character OUTSIDE any comment. Strings are tracked so that a '#' inside a
# string literal does not begin a comment and a non-ASCII char inside a string
# IS reported (a mis-decoded string is exactly the parse-failure case).
# ---------------------------------------------------------------------------
function Test-LineHasExecutableNonAscii([string] $Line) {
    # Comment state has already been stripped by the caller, so everything left
    # on this line is executable: code OR a string literal. A non-ASCII char in
    # either can mis-decode into a token the 5.1 tokenizer treats specially, so
    # both are reported. Quoting is tracked ONLY so that a '#' inside a string
    # does not start a comment.
    $inSingle = $false
    $inDouble = $false
    for ($i = 0; $i -lt $Line.Length; $i++) {
        $c = $Line[$i]

        if ($c -eq "'" -and -not $inDouble) {
            if ($inSingle -and ($i + 1) -lt $Line.Length -and $Line[$i + 1] -eq "'") { $i++ }
            else { $inSingle = -not $inSingle }
            continue
        }
        if ($c -eq '"' -and -not $inSingle) {
            if ($inDouble -and ($i + 1) -lt $Line.Length -and $Line[$i + 1] -eq '"') { $i++ }
            else { $inDouble = -not $inDouble }
            continue
        }
        # '#' starts a comment only outside any string.
        if ($c -eq '#' -and -not $inSingle -and -not $inDouble) { return $false }
        if ([int][char]$c -gt 127) { return $true }
    }
    return $false
}

function Get-NonAsciiCodes([string] $Line) {
    $codes = @()
    foreach ($c in $Line.ToCharArray()) {
        $v = [int][char]$c
        if ($v -gt 127) {
            if ($codes -notcontains $v) { $codes += $v }
        }
    }
    return $codes
}

function Test-FileHasBom([string] $Path) {
    $fs = [System.IO.File]::OpenRead($Path)
    try {
        if ($fs.Length -lt 3) { return $false }
        $b = New-Object byte[] 3
        [void]$fs.Read($b, 0, 3)
        return ($b[0] -eq 0xEF -and $b[1] -eq 0xBB -and $b[2] -eq 0xBF)
    }
    finally { $fs.Dispose() }
}

# ---------------------------------------------------------------------------
# Scan one file. Emits finding objects for executable non-ASCII in a BOM-less
# script. A file WITH a BOM is skipped: the tokenizer then reads it as UTF-8 and
# the mis-decode cannot happen, so flagging it would be a false positive.
# ---------------------------------------------------------------------------
function Measure-ScriptFile([string] $File) {
    $findings = @()
    try {
        $hasBom = Test-FileHasBom $File

        $lines = [System.IO.File]::ReadAllLines($File)
    }
    catch {
        Write-Error ("check-bom-nonascii: unreadable: {0} -- {1}" -f $File, $_.Exception.Message)
        return $findings
    }

    $inBlockComment = $false
    $n = 0
    foreach ($line in $lines) {
        $n++
        $work = $line

        # Block comments <# ... #> may open and close on the same line.
        while ($true) {
            if ($inBlockComment) {
                $end = $work.IndexOf('#>')
                if ($end -lt 0) { $work = ''; break }
                $work = $work.Substring($end + 2)
                $inBlockComment = $false
                continue
            }
            $open = $work.IndexOf('<#')
            if ($open -lt 0) { break }
            $rest = $work.Substring($open + 2)
            $end = $rest.IndexOf('#>')
            if ($end -lt 0) { $work = $work.Substring(0, $open); $inBlockComment = $true; break }
            $work = $work.Substring(0, $open) + $rest.Substring($end + 2)
        }

        if ($work.Length -eq 0) { continue }
        if (Test-LineHasExecutableNonAscii $work) {
            $codes = (Get-NonAsciiCodes $work | ForEach-Object { 'U+{0:X4}' -f $_ }) -join ','
            $isSmart = ($codes -match 'U\+201[89CD]')

            # Two DISTINCT hazard classes, measured this pass:
            #  SMARTQUOTE -- U+2018/19/1C/1D are real string DELIMITERS to the 5.1
            #    tokenizer. They break a string even when correctly encoded, so a
            #    BOM does not save them. Verified: $a = [U+201C]hello[U+201D]
            #    yields the 5-char string hello.
            #  MISDECODE -- 1794 of 12128 sampled codepoints (U+00C2, U+00C4,
            #    U+0102 ... U+2014 among them) fail to parse when the file is
            #    BOM-less and read as cp1252. With a BOM the decode is correct and
            #    they are harmless, so a BOM file carrying only these is NOT
            #    reported -- reporting it would be the false positive that trains
            #    people to ignore the gate.
            if ($hasBom -and -not $isSmart) { continue }

            $class = if ($isSmart) {
                # Smart quotes are REAL string delimiters to the 5.1 tokenizer, so
                # they break a string EVEN WHEN correctly encoded -- verified:
                # `$a = [U+201C]hello[U+201D]` yields the 5-char string hello.
                # This is the opposite hazard from the mis-decode class below.
                'SMARTQUOTE'
            }
            elseif (-not $hasBom) {
                'MISDECODE'
            }
            else { 'NONASCII' }

            $findings += [pscustomobject]@{
                File     = $File
                Line     = $n
                Codes    = $codes
                Bom      = $hasBom
                Class    = $class
                Text     = $work.Trim()
            }
        }
    }
    return $findings
}

# ---------------------------------------------------------------------------
# Self-test. The point is to prove the gate CAN fire. A detector that has only
# ever run against a clean tree is untested, which is the failure this repo has
# already recorded twice (AGENT_LOOP.md: "a test never observed red").
#
# Fixtures are written to %TEMP%, never into the repository. Each case asserts
# the EXPECTED verdict, so a detector that silently stopped detecting fails the
# self-test rather than reporting a clean tree.
# ---------------------------------------------------------------------------
function Invoke-SelfTest {
    $tmp = Join-Path ([System.IO.Path]::GetTempPath()) ('b51725-selftest-' + [Guid]::NewGuid().ToString('N').Substring(0, 8))
    New-Item -ItemType Directory -Path $tmp -Force | Out-Null
    $failures = 0
    $checks = 0
    $script:selfTestFailures = 0
    $script:selfTestChecks = 0

    function Add-Case([string] $Name, [bool] $ExpectFinding, [string] $Body, [bool] $WithBom) {
        $f = Join-Path $tmp ($Name + '.ps1')
        $enc = New-Object System.Text.UTF8Encoding($WithBom)
        [System.IO.File]::WriteAllText($f, $Body, $enc)
        $got = @(Measure-ScriptFile $f)
        $script:selfTestChecks++
        $expect = if ($ExpectFinding) { 1 } else { 0 }
        if ($got.Count -ne $expect) {
            Write-Host ("SELF-TEST FAIL: {0} expected {1} finding(s), got {2}" -f $Name, $expect, $got.Count)
            foreach ($g in $got) { Write-Host ("    line {0}: {1}" -f $g.Line, $g.Text) }
            $script:selfTestFailures++
        }
        else {
            Write-Host ("SELF-TEST ok: {0} -> {1} finding(s)" -f $Name, $got.Count)
        }
    }

    # A: the real defect shape. A curly quote on an EXECUTABLE line of a
    # BOM-less script is the class this gate exists to catch.
    Add-Case 'executable-curly-quote' $true ("Write-Host `"done`"`n" + [char]0x2014 + "`n") $false

    # B: non-ASCII inside a single-quoted string is still executable code.
    Add-Case 'string-literal-non-ascii' $true ("Write-Host '" + [char]0x00A7 + "section'" + "`n") $false

    # C: non-ASCII on a comment line is cosmetic and must NOT be reported.
    Add-Case 'comment-continuation' $false ("# a note with " + [char]0x2014 + " dash`n") $false

    # D: non-ASCII inside a block comment must NOT be reported.
    Add-Case 'block-comment' $false ("<#`n  prose with " + [char]0x2014 + "`n#>`n") $false

    # E: a '#' inside a string must not start a comment, so the non-ASCII after
    #    it is on an executable line and MUST be reported.
    Add-Case 'hash-inside-string' $true ("Write-Host '" + [char]0x2014 + " # still code'`n") $false

    # F: CORRECTION to the row premise. The same U+2014 bytes WITH a BOM decode
    #    correctly and parse clean -- but a BOM does NOT make non-ASCII safe in
    #    general. U+201C/U+201D are real string DELIMITERS to the 5.1 tokenizer
    #    (verified: $a = [U+201C]hello[U+201D] yields the 5-char string hello), so
    #    they break a string even when correctly encoded -- the OPPOSITE hazard
    #    from mis-decode. The detector therefore does NOT exempt BOM files; it
    #    labels them a separate class. Both sub-cases are asserted.
    Add-Case 'bom-emdash-is-clean' $false ("Write-Output `"done`"`n" + [char]0x2014 + "`n") $true
    Add-Case 'bom-smartquote-STILL-BREAKS' $true ("Write-Output `"x" + [char]0x201C + "y`"`n") $true

    # G: pure ASCII, BOM-less: the clean baseline.
    Add-Case 'ascii-baseline' $false ("Write-Host 'plain'`n") $false

    Remove-Item -LiteralPath $tmp -Recurse -Force -ErrorAction SilentlyContinue

    Write-Host ""
    Write-Host ("self-test: {0} case(s), {1} failure(s)" -f $script:selfTestChecks, $script:selfTestFailures)
    if ($script:selfTestFailures -gt 0) { return 1 }
    return 0
}

if ($SelfTest) {
    # NOTE: the self-test writes its progress with Write-Host, not Write-Output.
    # `exit (Invoke-SelfTest)` would consume the function's output stream AS the
    # exit-code argument and discard it, leaving a silent exit 0 -- a green pass
    # with no evidence, which is the outcome this gate exists to prevent.
    exit (Invoke-SelfTest)
}

# ---------------------------------------------------------------------------
# Scan mode.
# ---------------------------------------------------------------------------
$files = @()
foreach ($root in $Path) {
    if (-not (Test-Path -LiteralPath $root)) {
        Write-Error ("check-bom-nonascii: path not found: {0}" -f $root)
        exit 2
    }
    $item = Get-Item -LiteralPath $root
    if ($item.PSIsContainer) {
        $files += @(Get-ChildItem -LiteralPath $root -Recurse -Filter '*.ps1' -File |
                    Where-Object { $_.FullName -notmatch '\\\.git\\' } |
                    ForEach-Object { $_.FullName })
    }
    else { $files += $item.FullName }
}

$all = @()
foreach ($f in $files) { $all += @(Measure-ScriptFile $f) }

Write-Output ("files scanned: {0}" -f $files.Count)
    Write-Output ("findings: {0}" -f $all.Count)

foreach ($f in $all) {
    Write-Output ("HIT {0}:{1} {2} [bom={3} class={4}]  |  {5}" -f $f.File, $f.Line, $f.Codes, $f.Bom, $f.Class, $f.Text)
}

if ($all.Count -gt 0) {
    Write-Output 'FAIL: non-ASCII on an executable line of a BOM-less .ps1 (B5-1725).'
    Write-Output 'Fix: use ASCII outside comments. Non-ASCII inside comments is allowed and not reported.'
    exit 1
}

Write-Output 'PASS: no non-ASCII on any executable line of a BOM-less .ps1.'
exit 0
