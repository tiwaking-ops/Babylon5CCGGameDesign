<#
.SYNOPSIS
  B5-2571: detect uncommitted hunks that do not carry exactly one hunk-ownership
  marker, per the marker grammar in
  docs/proposals/hunk-ownership-marker-verification-proposal.md.

.DESCRIPTION
  Why this tool exists. docs/proposals/hunk-ownership-marker-proposal.md (B5-1465)
  measured the triage problem: over 45 uncommitted hunks in the seven dirty
  b5ccg/src files, 73% carried at least one row id and 33% carried MORE than one.
  A naive "does this hunk mention a row id?" test is green on that 33% while the
  question it meant to answer -- WHICH row owns the hunk -- is unanswered.
  B5-1465 proposed the OWNER: B5-NNNN comment convention and a triage procedure,
  and explicitly declined to build a gate.

  B5-2571 is the follow-up: implement the detector that triage procedure
  describes, and a way to verify that every uncommitted hunk carries a marker.
  This tool is that detector. It is deliberately an ON-DEMAND INSTRUMENT with an
  exit-code contract, not a wired gate: it is NOT registered in
  .agent/tools/run-verification-battery.ps1 and nothing in the build calls it.
  B5-1465's cost argument -- a comment convention that can only ever produce
  findings trains the fleet to ignore findings -- still holds for a BLOCKING
  check, so this closes the B5-2571 request without reversing B5-1465's verdict.
  An instrument you run while triaging is not a gate; a check wired into the boot
  battery is. Wiring it in is a separate, adjudicable decision.

  WHAT IT MEASURES. For each hunk of git diff -U0, over ADDED LINES ONLY
  (B5-0755: context lines carry the old file's comments and would attribute new
  bytes to unrelated old tasks):

    * exactly one OWNER: B5-NNNN  -> ATTRIBUTED  (passes)
    * no owner, >= 1 MECHANICAL:  -> MECHANICAL  (declared; passes)
    * no owner, no declaration    -> UNLABELLED  (finding)
    * >1 OWNER:, or owner+mech    -> AMBIGUOUS   (finding)

  The AMBIGUOUS branch is the whole point: it is the branch a mere presence test
  cannot reach, and it is the branch B5-1465 measured on 15 of 45 hunks.

  THE MECHANICAL: TOKEN is introduced here and is NEW relative to B5-1465, which
  said only that a mechanical edit "carries no marker". That left no way for a
  detector to tell a deliberately unmarked mechanical edit from an unmarked real
  change, so a detector without it would emit unavoidable false positives. It is
  a SEPARATE token on purpose: B5-1465 defines OWNER: as reserved for a row id,
  so OWNER: MECHANICAL would violate the grammar it defines.

  EXIT CONTRACT (the exit code is the contract; stdout carries a banner):
    0  no findings: every hunk classified ATTRIBUTED or MECHANICAL
    1  at least one UNLABELLED or AMBIGUOUS hunk
    2  the diff could not be read (not a git repo, git unavailable, git failed).
       Exit 2 is a false pass and must NEVER be read as clean.

  ZERO HUNKS IS VACUOUS. A clean tree yields 0 hunks and exit 0, which is true
  but uninformative -- a parser bug reads identically. Run -SelfTest to prove the
  parser classifies non-trivially before trusting a zero: the self-test feeds
  synthetic diffs and asserts every branch fires at least once.

.PARAMETER Root      Repository root. Defaults to the script's grandparent directory.
.PARAMETER Ref       Git ref to diff against (default HEAD), or an explicit sha.
                     Must be non-empty: an empty value cannot be forwarded through
                     -File, so the index-only diff (git diff with no ref) is an
                     in-process-only mode.
.PARAMETER Path      Pathspec(s) limiting the scan. Default b5ccg/src, the code tree
                     the convention is written for. Pass '*' to scan the whole tree
                     (an empty string cannot be forwarded through -File; '*' is the
                     command-line form, '@()' the in-process one).
.PARAMETER SelfTest  Run the synthetic-diff parser self-test and exit; needs no repo.

.OUTPUTS
  Exit 0 no findings / 1 findings / 2 diff unreadable.
#>
[CmdletBinding()]
param(
    [string]$Root,
    [string]$Ref = 'HEAD',
    [string[]]$Path = @('b5ccg/src'),
    [switch]$SelfTest
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version 2

$tag = '[B5-2571]'

# Grammar, defined once so the self-test and the live scan cannot disagree.
# Width: B5-1611 rules a task id is B5- + decimal with NO width limit, so \d{4,}
# is forward-compatible across the B5-9999 boundary; the optional [a-z]? suffix
# is the B5-NNNNa form .agent/tools/new-claim.ps1 accepts. The trailing lookahead
# stops B5-2571 matching inside B5-25711.
$ownerRegex = [regex]'OWNER:\s*(B5-\d{4,}[a-z]?)(?![0-9a-z])'
$mechRegex  = [regex]'MECHANICAL:\s*\S'

function Invoke-HunkScan {
    # Pure function: unified-diff lines in, classified hunks out. Factored so the
    # self-test exercises the SAME code path the live scan uses -- a self-test on a
    # separate parser would validate the test, not the tool.
    param([string[]]$DiffLines)

    $hunks = New-Object System.Collections.Generic.List[object]
    $curFile = '(unknown)'
    $curHunk = $null

    foreach ($line in $DiffLines) {
        if ($null -eq $line) { continue }
        if ($line.StartsWith('+++ ')) {
            $p = $line.Substring(4).Trim()
            if ($p.StartsWith('b/')) { $p = $p.Substring(2) }
            if ($p -ne '/dev/null' -and $p.Length -gt 0) { $curFile = $p }
            $curHunk = $null
            continue
        }
        if ($line.StartsWith('--- ')) { continue }
        if ($line.StartsWith('@@')) {
            $curHunk = [pscustomobject]@{
                File       = $curFile
                Header     = $line
                Owners     = (New-Object System.Collections.Generic.List[string])
                OwnerCount = 0
                MechCount  = 0
                Added      = 0
            }
            $hunks.Add($curHunk) | Out-Null
            continue
        }
        if ($line.StartsWith('+')) {
            if ($null -eq $curHunk) { continue }
            $text = $line.Substring(1)
            $curHunk.Added = $curHunk.Added + 1
            foreach ($m in $ownerRegex.Matches($text)) {
                $curHunk.OwnerCount = $curHunk.OwnerCount + 1
                $curHunk.Owners.Add($m.Groups[1].Value) | Out-Null
            }
            if ($mechRegex.IsMatch($text)) { $curHunk.MechCount = $curHunk.MechCount + 1 }
        }
    }

    $results = New-Object System.Collections.Generic.List[object]
    foreach ($h in $hunks) {
        $distinct = @($h.Owners | Sort-Object -Unique)
        if ($h.OwnerCount -eq 0 -and $h.MechCount -gt 0) {
            $verdict = 'MECHANICAL'
        } elseif ($h.OwnerCount -eq 0) {
            $verdict = 'UNLABELLED'
        } elseif ($h.OwnerCount -eq 1 -and $distinct.Count -eq 1 -and $h.MechCount -eq 0) {
            $verdict = 'ATTRIBUTED'
        } else {
            $verdict = 'AMBIGUOUS'
        }
        $results.Add([pscustomobject]@{
            File       = $h.File
            Header     = $h.Header
            Verdict    = $verdict
            OwnerList  = ($distinct -join ',')
            OwnerCount = $h.OwnerCount
            MechCount  = $h.MechCount
            Added      = $h.Added
        }) | Out-Null
    }

    # ToArray() is load-bearing: `@($genericList)` throws "Argument types do not
    # match" in PowerShell 5.1 -- the array subexpression cannot copy a
    # List[object], so returning the list here crashed the caller at its `@(...)`.
    # Returning a plain object[] makes @() a no-op copy instead.
    return [pscustomobject]@{ Hunks = $results.ToArray() }
}

function Test-Fixture {
    param(
        [string]$Name,
        [string[]]$Diff,
        [int]$ExpectHunks,
        [string[]]$ExpectVerdicts
    )
    $scan = Invoke-HunkScan -DiffLines $Diff
    $hunks = @($scan.Hunks)
    if ($hunks.Count -ne $ExpectHunks) {
        Write-Host ("$tag SELFTEST FAIL {0}: expected {1} hunk(s), got {2}" -f $Name, $ExpectHunks, $hunks.Count)
        return $false
    }
    $pass = $true
    for ($i = 0; $i -lt $ExpectVerdicts.Count; $i++) {
        if ($hunks[$i].Verdict -ne $ExpectVerdicts[$i]) {
            Write-Host ("$tag SELFTEST FAIL {0}: hunk {1} expected {2}, got {3}" -f $Name, $i, $ExpectVerdicts[$i], $hunks[$i].Verdict)
            $pass = $false
        }
    }
    if ($pass) {
        Write-Host ("$tag SELFTEST ok   {0}: {1} hunk(s) [{2}]" -f $Name, $ExpectHunks, ($ExpectVerdicts -join ','))
    }
    return $pass
}


if ($SelfTest) {
    $ok = $true

    # 1. One owner on an added line -> ATTRIBUTED.
    $ok = (Test-Fixture -Name 'attributed' -ExpectHunks 1 -ExpectVerdicts @('ATTRIBUTED') -Diff @(
        'diff --git a/x b/x',
        '--- a/x',
        '+++ b/x',
        '@@ -1,0 +2,2 @@',
        '+    // OWNER: B5-2571',
        '+    int y = 1;'
    )) -and $ok

    # 2. Added lines, no marker -> UNLABELLED (the detector can FIRE).
    $ok = (Test-Fixture -Name 'unlabelled' -ExpectHunks 1 -ExpectVerdicts @('UNLABELLED') -Diff @(
        'diff --git a/x b/x',
        '--- a/x',
        '+++ b/x',
        '@@ -1,0 +2,1 @@',
        '+    int y = 1;'
    )) -and $ok

    # 3. Two DIFFERENT owners in one hunk -> AMBIGUOUS (the branch presence misses).
    $ok = (Test-Fixture -Name 'two-owners-ambiguous' -ExpectHunks 1 -ExpectVerdicts @('AMBIGUOUS') -Diff @(
        '@@ -1,0 +2,2 @@',
        '+    // OWNER: B5-2571',
        '+    // OWNER: B5-9999'
    )) -and $ok

    # 4. The SAME owner twice is still not "exactly one" -> AMBIGUOUS.
    $ok = (Test-Fixture -Name 'same-owner-twice-ambiguous' -ExpectHunks 1 -ExpectVerdicts @('AMBIGUOUS') -Diff @(
        '@@ -1,0 +2,2 @@',
        '+    // OWNER: B5-2571',
        '+    // OWNER: B5-2571'
    )) -and $ok

    # 5. Declared mechanical edit -> MECHANICAL (passes; the detector stays QUIET).
    $ok = (Test-Fixture -Name 'mechanical' -ExpectHunks 1 -ExpectVerdicts @('MECHANICAL') -Diff @(
        '@@ -1,0 +2,1 @@',
        '+    // MECHANICAL: import reorder only, no behaviour change'
    )) -and $ok

    # 6. Owner AND mechanical on one hunk -> AMBIGUOUS (mutually exclusive tokens).
    $ok = (Test-Fixture -Name 'owner-plus-mechanical-ambiguous' -ExpectHunks 1 -ExpectVerdicts @('AMBIGUOUS') -Diff @(
        '@@ -1,0 +2,2 @@',
        '+    // OWNER: B5-2571',
        '+    // MECHANICAL: and a tidy'
    )) -and $ok

    # 7. A CITATION without the OWNER: token does not fire (B5-1465: OWNER: is
    #    reserved; a citation is written in any other form). B5 id present, no marker.
    $ok = (Test-Fixture -Name 'citation-does-not-fire' -ExpectHunks 1 -ExpectVerdicts @('UNLABELLED') -Diff @(
        '@@ -1,0 +2,1 @@',
        '+    // depends on the B5-0691 engine law and B5-0351'
    )) -and $ok

    # 8. Per-hunk separation across one file: three hunks, three verdicts.
    $ok = (Test-Fixture -Name 'three-hunks-separated' -ExpectHunks 3 -ExpectVerdicts @('ATTRIBUTED', 'UNLABELLED', 'MECHANICAL') -Diff @(
        '+++ b/x',
        '@@ -1,0 +2,1 @@',
        '+    // OWNER: B5-2571',
        '@@ -9,0 +11,1 @@',
        '+    int z = 2;',
        '@@ -20,0 +22,1 @@',
        '+    // MECHANICAL: whitespace'
    )) -and $ok

    # 9. A whole-new-file hunk (the -U0 form) is scanned, not skipped.
    $ok = (Test-Fixture -Name 'new-file-hunk' -ExpectHunks 1 -ExpectVerdicts @('ATTRIBUTED') -Diff @(
        'diff --git a/new b/new',
        '--- /dev/null',
        '+++ b/new',
        '@@ -0,0 +1,2 @@',
        '+    // OWNER: B5-2571',
        '+    int q = 3;'
    )) -and $ok

    # 10. Zero hunks is a real, countable answer (the vacuous case named in the header).
    $ok = (Test-Fixture -Name 'zero-hunks' -ExpectHunks 0 -ExpectVerdicts @() -Diff @(
        'diff --git a/x b/x',
        'index 111..222 100644'
    )) -and $ok

    if (-not $ok) {
        Write-Host "$tag SELFTEST FAILED: at least one branch classified unexpectedly."
        exit 1
    }
    Write-Host "$tag SELFTEST PASS: ATTRIBUTED, MECHANICAL, UNLABELLED and AMBIGUOUS all observed;"
    Write-Host "$tag   citations do not fire; a repeated owner is ambiguous; zero hunks is countable."
    exit 0
}


if (-not $Root) { $Root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot) }
if (-not (Test-Path -LiteralPath $Root -PathType Container)) {
    Write-Output ("$tag EXIT 2: root is not a readable directory: {0}" -f $Root)
    exit 2
}

$gitArgs = @('-C', $Root, 'diff', '--no-color', '-U0')
if ($Ref) { $gitArgs += $Ref }
$gitArgs += '--'
if ($Path -and @($Path).Count -gt 0 -and $Path[0]) { $gitArgs += $Path }

$diffLines = $null
try {
    $diffLines = @(& git @gitArgs 2>$null)
} catch {
    Write-Output ("$tag EXIT 2: could not run git diff: {0}" -f $_.Exception.Message)
    exit 2
}
if ($LASTEXITCODE -ne 0) {
    Write-Output ("$tag EXIT 2: git diff exited {0}; the scan did NOT complete." -f $LASTEXITCODE)
    exit 2
}

$scan   = Invoke-HunkScan -DiffLines $diffLines
$hunks  = @($scan.Hunks)
$attributed = @($hunks | Where-Object { $_.Verdict -eq 'ATTRIBUTED' }).Count
$mech       = @($hunks | Where-Object { $_.Verdict -eq 'MECHANICAL' }).Count
$unlabelled = @($hunks | Where-Object { $_.Verdict -eq 'UNLABELLED' }).Count
$ambiguous  = @($hunks | Where-Object { $_.Verdict -eq 'AMBIGUOUS' }).Count
$findings   = @($hunks | Where-Object { $_.Verdict -eq 'UNLABELLED' -or $_.Verdict -eq 'AMBIGUOUS' })

Write-Output ("$tag root       : {0}" -f $Root)
Write-Output ("$tag ref        : {0}" -f $Ref)
Write-Output ("$tag pathspec   : {0}" -f ((@($Path)) -join ' '))
Write-Output ("$tag hunks      : {0}" -f $hunks.Count)
Write-Output ("$tag attributed : {0}" -f $attributed)
Write-Output ("$tag mechanical : {0}" -f $mech)
Write-Output ("$tag findings   : {0} (unlabelled {1}, ambiguous {2})" -f $findings.Count, $unlabelled, $ambiguous)
Write-Output ("$tag method     : added lines only (B5-0755); grammar OWNER: B5-NNNN and MECHANICAL: <why>")

if ($hunks.Count -eq 0) {
    Write-Output ("$tag NOTE: 0 hunks examined -> the clean result is VACUOUS; run -SelfTest to prove the parser is non-trivial.")
}

if ($findings.Count -gt 0) {
    Write-Output ''
    foreach ($x in $findings) {
        Write-Output ("$tag {0}  {1}  {2}" -f $x.Verdict, $x.File, $x.Header)
        if ($x.Verdict -eq 'AMBIGUOUS') {
            Write-Output ("$tag    owners=[{0}] ownerMarkers={1} mechanicalMarkers={2}" -f $x.OwnerList, $x.OwnerCount, $x.MechCount)
        }
    }
    Write-Output ''
    Write-Output ("$tag REPAIR: put exactly one OWNER: B5-NNNN comment on an added line of each hunk,")
    Write-Output ("$tag   or declare a genuine mechanical edit with a MECHANICAL: <why> comment.")
    Write-Output ("$tag   Split any AMBIGUOUS hunk so each carries one owner; never normalise blindly.")
    exit 1
}

exit 0

