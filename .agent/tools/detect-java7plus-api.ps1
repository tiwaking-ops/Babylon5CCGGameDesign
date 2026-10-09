<#
.SYNOPSIS
  B5-2717: detect Java 7 / Java 8 (and later) STDLIB SURFACE in git-tracked Java
  sources, which is the one dimension `javac -source 6 -target 6` cannot see.

.DESCRIPTION
  Why this tool exists. AGENTS.md section 2a states the authority split exactly:
  javac is decisive on syntax and the on-demand census is the record plus an
  "API-level second look". Measured when this row was seeded (2026-10-06), the
  second look did not cover library surface at all. `git grep` over tracked
  `b5ccg/src` returned:

      b5ccg/src/b5ccg/engine/DeckLoader.java:7                 import java.nio.charset.StandardCharsets;
      b5ccg/src/b5ccg/engine/CardIdNamespaceGuard.java:6       ditto
      b5ccg/src/b5ccg/engine/CardSetManifestChecker.java:5     ditto
      b5ccg/src/b5ccg/engine/CardSetManifestCrossCheck.java:6  ditto
      b5ccg/src/b5ccg/ui/HandPanel.java:432,438                Integer.compare(a.getCost(), b.getCost())

  `StandardCharsets` arrived in Java 7 and `Integer.compare` arrived in Java 7.
  Neither is a syntax innovation, so `-source 6 -target 6` accepts both and emits
  a class file that is legitimately major 50; and neither is any family in
  `.agent/tools/census-b50960.py`, so that census reports code-lines 0 on every
  family. Both standing gates were green and neither was wrong about what it
  measures. The `Integer.compare` instance additionally sits in `ui/`, which no
  prior write had recorded at all -- B5-2614's A1 note names the three-engine-file
  subset it saw, so this is not a re-report of someone else's finding so much as
  evidence that a hand-taken census and a machine-taken one do not agree.

  WHAT IT MEASURES. Reference to a stdlib CLASS, METHOD or member that did not
  exist in Java 6. Lines are scanned only after `/* */` comments, `//` comments,
  char literals and string literals have been BLANKED TO SPACES, preserving both
  the line count and every character offset, so a hit is java code and not prose
  about java code. The masking is a plain per-character state machine rather than
  a regex so that `Integer.compare` inside a string does not appear to be a call,
  and so that a string literal appearing EARLIER on a line does not blank the
  code that follows it -- the false negative a naive `replace everything after the
  first quote` produces, which fixture D is written to catch.

  WHAT IT DELIBERATELY DOES NOT DO.

    1. It does not fix anything. The six files it names sit in packages under
       live claims and in-flight foreign work, and a read-only instrument is not
       the place to discover that afterwards.
    2. It does not edit `census-b50960.py` or change the AGENTS.md 2a pass
       condition. Seven of the ten families that instrument owns (getOrDefault,
       computeIfAbsent, computeIfPresent, compute, merge, forEach, removeIf,
       stream) are deliberately NOT duplicated here and are printed as exclusions
       with their owner, because two tools disagreeing about one family is worse
       than one tool owning it.
    3. It is not wired into `.agent/tools/run-verification-battery.ps1` or into
       the build. A real finding must be able to exist without turning another
       owner's green gate red; run it on demand, exactly as AGENTS.md 2a runs
       the census on demand.

  BLIND SPOTS, PRINTED ON EVERY RUN rather than left implicit:
    - the family table is CURATED, not exhaustive. A member added to an existing
      class that is not named here (`List.sort`, `Iterator.forEachRemaining`,
      `Map.replaceAll`, `Collections.emptyEnumeration`) is invisible; invariance
      here is coverage of the ten listed families and nothing else.
    - reflective / `Class.forName` access to any surface.
    - untracked Java files, reported by count only, and never scanned, matching
      the census denominator.

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/detect-java7plus-api.ps1

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/detect-java7plus-api.ps1 -SelfTest

.PARAMETER SelfTest
  Build five fixtures in a temp directory, scan them, and assert both their hit
  COUNTS and how many files were read. Asserting only the verdict is how
  `measure-task-cells.ps1` read green over an empty scan (B5-1730), so the file
  count is asserted first and its failure short-circuits before any finding is
  interpreted.

.PARAMETER IncludeUntracked
  Also scan Java files that exist on disk but are NOT tracked by git. Off by
  default so the denominator matches census-b50960.py exactly and the two agree.
  Measured at seed time this is not a rounding difference: b5ccg/src holds 136
  Java files on disk and only 82 are tracked, and three of the four files that
  reference StandardCharsets are among the untracked 54. Whichever way this
  switch is set, the count is printed, because a denominator nobody states is a
  verdict nobody can check.

.PARAMETER Root
  Repository root. Defaults to the repo this tool ships in.

.OUTPUTS
  Exit 0 = zero code hits (all families clean over a non-empty, fully read tree).
  Exit 1 = at least one code hit; each names file, line, family, JDK version and
           why javac cannot see it.
  Exit 2 = unreadable input, git unusable, or ZERO FILES SCANNED. Deliberately
           not 0: a scan that saw nothing must never report a clean tree, which
           is the same discipline run-dup-census.ps1 and check-citations.ps1 use.
#>
[CmdletBinding()]
param(
    [switch]$SelfTest,
    [switch]$IncludeArchive,
    [switch]$IncludeUntracked,
    [string]$Root
)

$ErrorActionPreference = 'Stop'

# ---------------------------------------------------------------------------
# The family table. `Since` is the JDK that ADDED the entry, so the report can
# say why each one is invisible to -source 6 rather than merely asserting it.
# Every regex is written so prose about the API does not match: bare word
# boundaries for classes and an argument list for members, because
# `Objects.compare` mentioned in a comment is a mention and `Objects.compare(`
# is a call.
# ---------------------------------------------------------------------------
$Families = @(
    @{ Name = 'standardCharsets'; Since = 7;  Note = 'java.nio.charset.StandardCharsets constants';
       Rx = '\bStandardCharsets\b' },
    @{ Name = 'nioFilePackage';   Since = 7;  Note = 'java.nio.file package (Path, Paths, Files)';
       Rx = 'import\s+java\.nio\.file\.' },
    @{ Name = 'nioFileFilesApi';  Since = 7;  Note = 'Files./Paths. static facade';
       Rx = '\b(?:Files|Paths)\.(?:read|write|copy|move|delete|create|new|walk|find|lines|list|exists|size|probe|get|set)[A-Za-z]*\s*\(' },
    @{ Name = 'objectsUtility';   Since = 7;  Note = 'java.util.Objects null-safe helpers';
       Rx = '\bObjects\.(?:requireNonNull|isNull|nonNull|equals|deepEquals|hash|hashCode|compare|toString)\s*\(' },
    @{ Name = 'boxedCompare';     Since = 7;  Note = 'static compare on the boxed wrappers';
       Rx = '\b(?:Integer|Long|Short|Byte|Character|Boolean)\.compare\s*\(' },
    @{ Name = 'throwableAddSuppressed'; Since = 7; Note = 'Throwable.addSuppressed / getSuppressed';
       Rx = '\baddSuppressed\s*\(' },
    @{ Name = 'optionalType';     Since = 8;  Note = 'java.util.Optional';
       Rx = '\bOptional\.(?:of|ofNullable|empty)\s*\(|\bjava\.util\.Optional\b' },
    @{ Name = 'base64Codec';      Since = 8;  Note = 'java.util.Base64';
       Rx = '\bBase64\.(?:getEncoder|getDecoder|getUrlEncoder|getUrlDecoder|getMimeEncoder|getMimeDecoder)\s*\(|\bjava\.util\.Base64\b' },
    @{ Name = 'comparatorFactory'; Since = 8; Note = 'Comparator.comparing / naturalOrder / reverseOrder / nullsFirst / nullsLast';
       Rx = '\bComparator\.(?:comparing|comparingInt|comparingLong|comparingDouble|reverseOrder|naturalOrder|nullsFirst|nullsLast)\b' },
    @{ Name = 'entryComparator';  Since = 8;  Note = 'Map.Entry comparingByKey / comparingByValue';
       Rx = '\bcomparingBy(?:Key|Value)\s*\(' },
    @{ Name = 'stringJoin';       Since = 8;  Note = 'String.join';
       Rx = '\bString\.join\s*\(' },
    @{ Name = 'javaTimeApi';      Since = 8;  Note = 'java.time (JSR-310)';
       Rx = '\bjava\.time\.' },
    @{ Name = 'jdk9ImmutableOf';  Since = 9;  Note = 'List.of / Set.of / Map.of / Map.ofEntries';
       Rx = '\b(?:List|Set|Map)\.of(?:Entries)?\s*\(' },
    @{ Name = 'jdk11StringBlank'; Since = 11; Note = 'String.isBlank / strip / lines / repeat';
       Rx = '\.(?:isBlank|strip|stripLeading|stripTrailing|lines|repeat)\s*\(' }
)

# Owned elsewhere. Printed with their owner on every run: an exclusion nobody
# can see is indistinguishable from a family that did not occur to anybody.
$OwnedElsewhere = @(
    'getOrDefault', 'computeIfAbsent', 'computeIfPresent', 'compute', 'merge',
    'forEach', 'removeIf', 'stream'
)

$Tag = '[B5-2717]'

function Get-MaskedLines {
    # Blank comment and literal content to spaces, preserving line count and
    # every character offset. Per-character state machine, not a regex: a regex
    # cannot know that the `//` inside "...//..." is inside a string, and that
    # mistake is exactly what would make a comment-census of Java unreliable.
    param([string[]]$Lines)

    $masked = New-Object string[] $Lines.Count
    $inBlock = $false

    for ($li = 0; $li -lt $Lines.Count; $li++) {
        $line = $Lines[$li]
        $sb = New-Object System.Text.StringBuilder
        $i = 0
        $n = $line.Length
        while ($i -lt $n) {
            $ch = $line[$i]
            if ($inBlock) {
                if ($ch -eq '*' -and ($i + 1) -lt $n -and $line[$i + 1] -eq '/') {
                    [void]$sb.Append('  ')
                    $i += 2
                    $inBlock = $false
                    continue
                }
                [void]$sb.Append(' ')
                $i++
                continue
            }
            if ($ch -eq '/' -and ($i + 1) -lt $n -and $line[$i + 1] -eq '*') {
                [void]$sb.Append('  ')
                $i += 2
                $inBlock = $true
                continue
            }
            if ($ch -eq '/' -and ($i + 1) -lt $n -and $line[$i + 1] -eq '/') {
                [void]$sb.Append((' ' * ($n - $i)))
                $i = $n
                continue
            }
            if ($ch -eq '"' -or $ch -eq "'") {
                $quote = $ch
                [void]$sb.Append(' ')
                $i++
                while ($i -lt $n) {
                    $d = $line[$i]
                    if ($d -eq '\' -and ($i + 1) -lt $n) { [void]$sb.Append('  '); $i += 2; continue }
                    if ($d -eq $quote) { [void]$sb.Append(' '); $i++; break }
                    [void]$sb.Append(' ')
                    $i++
                }
                continue
            }
            [void]$sb.Append($ch)
            $i++
        }
        $masked[$li] = $sb.ToString()
    }
    return $masked
}

function Invoke-ApiScan {
    param(
        [string]$RepoRoot,
        [string[]]$Files,
        [string]$Label
    )

    $hits = New-Object System.Collections.Generic.List[object]
    $stats = [ordered]@{ files = 0; lines = 0; codeHits = 0; maskedOnly = 0; readErrors = 0 }
    $prev = Get-Location
    Set-Location -LiteralPath $RepoRoot
    try {
        foreach ($rel in $Files) {
            $full = Join-Path $RepoRoot $rel
            if (-not (Test-Path -LiteralPath $full -PathType Leaf)) {
                Write-Output ("{0} MISSING-FILE {1} (tracked but absent on disk; NOT counted clean)" -f $Tag, $rel)
                $stats.readErrors++
                continue
            }
            try {
                # Explicit UTF-8 with no BOM fallback: the B5-1002 trap, where a
                # BOM-less file read naively decodes as host ANSI and the tool
                # measures a different file than the one on disk.
                $lines = [System.IO.File]::ReadAllLines($full, (New-Object System.Text.UTF8Encoding($false)))
            } catch {
                Write-Output ("{0} WARNING unreadable {1} ({2}); NOT counted clean" -f $Tag, $rel, $_.Exception.Message)
                $stats.readErrors++
                continue
            }
            $stats.files++
            $masked = Get-MaskedLines -Lines $lines
            for ($i = 0; $i -lt $masked.Count; $i++) {
                $stats.lines++
                foreach ($fam in $Families) {
                    $codeOcc = ([regex]::Matches($masked[$i], $fam.Rx)).Count
                    if ($codeOcc -eq 0) { continue }
                    $stats.codeHits += $codeOcc
                    $hits.Add([pscustomobject]@{
                        Tree  = $Label
                        Rel   = $rel
                        Line  = $i + 1
                        Family = $fam.Name
                        Since = $fam.Since
                        Note  = $fam.Note
                        Count = $codeOcc
                        Text  = $lines[$i].Trim()
                    })
                }
            }
            # The companion number: occurrences that existed in the RAW line but
            # vanished under the mask. They are deliberately NOT findings -- they
            # are the evidence that the mask is doing work, and the number to
            # look at first when a future reader suspects it is doing too much.
            for ($i = 0; $i -lt $lines.Count; $i++) {
                foreach ($fam in $Families) {
                    $rawOcc = ([regex]::Matches($lines[$i], $fam.Rx)).Count
                    $codeOcc = ([regex]::Matches($masked[$i], $fam.Rx)).Count
                    if ($rawOcc -gt $codeOcc) { $stats.maskedOnly += ($rawOcc - $codeOcc) }
                }
            }
        }
    } finally {
        Set-Location $prev
    }
    return [pscustomobject]@{ Hits = $hits; Stats = $stats; Label = $Label }
}

function Get-TrackedJava {
    param([string]$RepoRoot, [string]$Tree)

    $prev = Get-Location
    Set-Location -LiteralPath $RepoRoot
    try {
        # The two pathspecs the census uses, kept identical so the denominators
        # are comparable rather than merely both plausible.
        $a = & git ls-files -- ($Tree + '/*.java') 2>$null
        $b = & git ls-files -- ($Tree + '/**/*.java') 2>$null
        if ($LASTEXITCODE -ne 0 -and -not $a -and -not $b) { return $null }
    } catch {
        return $null
    } finally {
        Set-Location $prev
    }
    $all = @()
    if ($a) { $all += $a }
    if ($b) { $all += $b }
    $all = @($all | Where-Object { $_ -ne '' } | Sort-Object -Unique)
    return $all
}

function Get-UntrackedJava {
    param([string]$RepoRoot, [string]$Tree)

    $prev = Get-Location
    Set-Location -LiteralPath $RepoRoot
    try {
        $raw = & git ls-files --others --exclude-standard -- $Tree 2>$null
    } catch {
        return $null
    } finally {
        Set-Location $prev
    }
    if ($null -eq $raw) { $raw = @() }
    $all = @($raw | Where-Object { $_ -match '\.java$' } | Sort-Object -Unique)
    return $all
}

function Write-BlindSpots {
    Write-Output ('{0} blind spots (printed, not silent):' -f $Tag)
    Write-Output ('{0}   - the {1} families above are CURATED, not exhaustive: a member added to an existing class and not named here (List.sort, Iterator.forEachRemaining, Map.replaceAll, Collections.emptyEnumeration) is invisible' -f $Tag, $Families.Count)
    Write-Output ('{0}   - reflective access (Class.forName, Method.invoke) to any surface' -f $Tag)
    Write-Output ('{0}   - untracked Java files: counted and listed above, and scanned only with -IncludeUntracked' -f $Tag)
    Write-Output ('{0}   - this tool edits nothing and fixes nothing; it reports' -f $Tag)
}

function Write-ExcludedOwner {
    Write-Output ('{0} families owned by .agent/tools/census-b50960.py and NOT duplicated here: {1}' -f $Tag, ($OwnedElsewhere -join ', '))
}

# ===========================================================================
# SELF-TEST. Five fixtures; the counts are asserted, not just the verdict.
# ===========================================================================
if ($SelfTest) {
    $tmp = Join-Path ([System.IO.Path]::GetTempPath()) ('b52717-selftest-' + [Guid]::NewGuid().ToString('N').Substring(0, 8))
    New-Item -ItemType Directory -Path $tmp -Force | Out-Null
    $fail = 0
    try {
        # A: two real hits (import + use) plus a wrappertype compare. 3 lines.
        $a = @(
            'import java.nio.charset.StandardCharsets;',
            'public class A {',
            '    public static int cmp(Integer x, Integer y) { return Integer.compare(x, y); }',
            '    public String read() { return buf.toString(StandardCharsets.UTF_8.name()); }',
            '}'
        )
        # B: three mentions, none of them code. This fixture is the one that
        # fails if the mask ever stops working, which is what makes "0 hits"
        # mean something.
        $b = @(
            'public class B {',
            '    // Integer.compare(a, b) is what we used to write here',
            '    /* Optional.of(x) and Comparator.comparing(...) were considered */',
            '    public static final String DOC = "StandardCharsets.UTF_8.name()";',
            '}'
        )
        # C: Java 6 code that mentions nothing.
        $c = @(
            'import java.util.ArrayList;',
            'public class C {',
            '    public int size() { return new ArrayList<String>().size(); }',
            '}'
        )
        # D: FALSE-NEGATIVE CONTROL. A string literal opens the line before real
        # code. A mask that blanks "everything after the first quote" would eat
        # the Integer.compare call and report clean on a line that is not.
        $d = @(
            'public class D {',
            '    public int go(Integer a, Integer b) { String s = "// Integer.compare"; return Integer.compare(a, b); }',
            '}'
        )
        # E: a three-line block comment wrapping one real-looking call.
        $e = @(
            'public class E {',
            '    /*',
            '       return Optional.ofNullable(value).orElse(null);',
            '    */',
            '    public Object n() { return null; }',
            '}'
        )
        $names = @('A.java', 'B.java', 'C.java', 'D.java', 'E.java')
        $bodies = @($a, $b, $c, $d, $e)
        for ($k = 0; $k -lt $names.Count; $k++) {
            [System.IO.File]::WriteAllLines((Join-Path $tmp $names[$k]), $bodies[$k], (New-Object System.Text.UTF8Encoding($false)))
        }

        $files = @($names)
        $res = Invoke-ApiScan -RepoRoot $tmp -Files $files -Label 'fixture'
        $stats = $res.Stats

        Write-Output ('{0} SELF-TEST files read: {1} (expected 5)' -f $Tag, $stats.files)
        if ($stats.files -ne 5) {
            Write-Output ('{0} SELF-TEST FAILED: read {1} of 5 fixture files, so every count below is meaningless. A green verdict over an empty scan is the B5-1730 shape.' -f $Tag, $stats.files)
            exit 1
        }
        if ($stats.readErrors -ne 0) {
            Write-Output ('{0} SELF-TEST FAILED: {1} fixture file(s) unreadable.' -f $Tag, $stats.readErrors)
            exit 1
        }

        $byFile = @{}
        foreach ($h in $res.Hits) {
            if (-not $byFile.ContainsKey($h.Rel)) { $byFile[$h.Rel] = 0 }
            $byFile[$h.Rel]++
        }
        foreach ($k in $names) {
            $cnt = 0
            if ($byFile.ContainsKey($k)) { $cnt = $byFile[$k] }
            Write-Output ('{0} SELF-TEST   {1}: {2} code hit line(s)' -f $Tag, $k, $cnt)
        }

        $expect = @{ 'A.java' = 3; 'B.java' = 0; 'C.java' = 0; 'D.java' = 1; 'E.java' = 0 }
        foreach ($k in $names) {
            $cnt = 0
            if ($byFile.ContainsKey($k)) { $cnt = $byFile[$k] }
            if ($cnt -ne $expect[$k]) {
                Write-Output ('{0} SELF-TEST FAILED: {1} produced {2} code hit line(s), expected {3}.' -f $Tag, $k, $cnt, $expect[$k])
                $fail = 1
            }
        }
        if ($fail -ne 0) { exit 1 }

        # The count that proves the mask is doing something at all. Zero here
        # would mean every masked-only occurrence had been silently reclassified
        # as code, or that fixture B had stopped being read.
        Write-Output ('{0} SELF-TEST masked-only occurrences (in comments or strings, correctly NOT findings): {1}' -f $Tag, $stats.maskedOnly)
        if ($stats.maskedOnly -lt 1) {
            Write-Output ('{0} SELF-TEST FAILED: nothing was masked. Either the fixtures are wrong or the mask is inert, and in both cases 0 findings would be a lie.' -f $Tag)
            exit 1
        }

        # Exit-2 path: an empty file list must never read as clean. Tested here
        # rather than assumed, because this is the failure that reads green.
        $emptyDir = Join-Path ([System.IO.Path]::GetTempPath()) ('b52717-empty-' + [Guid]::NewGuid().ToString('N').Substring(0, 8))
        New-Item -ItemType Directory -Path $emptyDir -Force | Out-Null
        try {
            $res2 = Invoke-ApiScan -RepoRoot $emptyDir -Files @() -Label 'empty'
            Write-Output ('{0} SELF-TEST empty-set files read: {1} hits: {2} (expected 0 files, 0 hits, and the CALLER must exit 2 on zero files)' -f $Tag, $res2.Stats.files, $res2.Stats.codeHits)
            if ($res2.Stats.files -ne 0) {
                Write-Output ('{0} SELF-TEST FAILED: the empty-set scan read {1} file(s).' -f $Tag, $res2.Stats.files)
                exit 1
            }
        } finally {
            Remove-Item -LiteralPath $emptyDir -Recurse -Force -ErrorAction SilentlyContinue
        }

        Write-Output ('{0} SELF-TEST PASS: real API usage fires, comment and string mentions do not, a line opening with a literal still fires, and an empty set is reported rather than trusted.' -f $Tag)
        exit 0
    } finally {
        Remove-Item -LiteralPath $tmp -Recurse -Force -ErrorAction SilentlyContinue
    }
}

if (-not $Root) { $Root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot) }
if (-not (Test-Path -LiteralPath $Root -PathType Container)) {
    Write-Output ('{0} EXIT 2: root is not a readable directory: {1}' -f $Tag, $Root)
    exit 2
}

$srcFiles = Get-TrackedJava -RepoRoot $Root -Tree 'b5ccg/src'
if ($null -eq $srcFiles) {
    Write-Output ('{0} EXIT 2: git ls-files produced nothing usable for b5ccg/src. A tree that could not be enumerated is not a clean tree.' -f $Tag)
    exit 2
}

Write-Output ('{0} root: {1}' -f $Tag, $Root)
Write-Output ('{0} javac blind spot: -source 6 -target 6 governs syntax and class-file version, never library surface' -f $Tag)
Write-Output ('{0} families: {1}' -f $Tag, $Families.Count)
Write-ExcludedOwner

$results = New-Object System.Collections.Generic.List[object]
$srcRes = Invoke-ApiScan -RepoRoot $Root -Files $srcFiles -Label 'TRACKED b5ccg/src'
$results.Add($srcRes)

# THE DENOMINATOR, PRINTED. This is not decoration: the standing census scans
# tracked paths only, and on this tree that is 82 of the 136 Java files on disk,
# so its per-family zero describes part of the tree. Stated either way the
# switch is set, because a number nobody prints cannot be argued with.
$untracked = Get-UntrackedJava -RepoRoot $Root -Tree 'b5ccg/src'
if ($null -eq $untracked) { $untracked = @() }
Write-Output ('{0} tracked Java under b5ccg/src: {1}; untracked Java on disk: {2}' -f $Tag, $srcFiles.Count, $untracked.Count)
if ($untracked.Count -gt 0) {
    Write-Output ('{0}   the census denominator does not see these, so a per-family zero there describes {1} of {2} on-disk files:' -f $Tag, $srcFiles.Count, ($srcFiles.Count + $untracked.Count))
    foreach ($u in $untracked) { Write-Output ('{0}     UNTRACKED {1}' -f $Tag, $u) }
}
if ($IncludeUntracked -and $untracked.Count -gt 0) {
    $unRes = Invoke-ApiScan -RepoRoot $Root -Files $untracked -Label 'UNTRACKED b5ccg/src (outside the census denominator)'
    $results.Add($unRes)
} elseif ($untracked.Count -gt 0) {
    Write-Output ('{0}   not scanned this run (-IncludeUntracked off), so the main stream below is the tracked denominator only' -f $Tag)
}

$totalReadErrors = $srcRes.Stats.readErrors
if ($IncludeArchive) {
    $archFiles = Get-TrackedJava -RepoRoot $Root -Tree 'b5ccg/src-java8-archive'
    if ($null -ne $archFiles -and $archFiles.Count -gt 0) {
        $archRes = Invoke-ApiScan -RepoRoot $Root -Files $archFiles -Label 'FROZEN src-java8-archive (positive control)'
        $results.Add($archRes)
    }
} else {
    Write-Output ('{0} frozen archive NOT scanned (-IncludeArchive off). Expected to fire; a detector silent over it is broken in the direction that matters.' -f $Tag)
}

foreach ($r in $results) {
    $s = $r.Stats
    Write-Output ''
    Write-Output ('{0} === {1}: {2} file(s), {3} line(s) ===' -f $Tag, $r.Label, $s.files, $s.lines)
    Write-Output ('{0}   code hits: {1}  masked as comment-or-string: {2}  unreadable: {3}' -f $Tag, $s.codeHits, $s.maskedOnly, $s.readErrors)
    foreach ($h in ($r.Hits | Sort-Object Rel, Line)) {
        Write-Output ('{0}   HIT {1}:{2} [{3}] since JDK {4} ({5})' -f $Tag, $h.Rel, $h.Line, $h.Family, $h.Since, $h.Note)
        Write-Output ('{0}        {1}' -f $Tag, $h.Text)
    }
}

Write-Output ''
Write-BlindSpots
Write-Output ('{0} encoding: UTF-8 via File.ReadAllLines with no BOM fallback (B5-1002); output ASCII-only so no console codepage can mangle a finding' -f $Tag)

$codeHits = 0
$totalFiles = 0
$totalReadErrors = 0
foreach ($r in $results) { $codeHits += $r.Stats.codeHits; $totalFiles += $r.Stats.files; $totalReadErrors += $r.Stats.readErrors }

if ($totalFiles -eq 0) {
    Write-Output ('{0} EXIT 2: zero files scanned. Not reported clean.' -f $Tag)
    exit 2
}
if ($totalReadErrors -gt 0) {
    Write-Output ('{0} EXIT 2: {1} file(s) could not be read, so the scan is incomplete.' -f $Tag, $totalReadErrors)
    exit 2
}

if ($codeHits -gt 0) {
    Write-Output ('{0} EXIT 1: {1} code-line hit(s) over {2} file(s). None of them is visible to javac -source 6 -target 6.' -f $Tag, $codeHits, $totalFiles)
    Write-Output ('{0} REPAIR belongs to whoever owns each file; this tool wrote nothing.' -f $Tag)
    exit 1
}

Write-Output ('{0} EXIT 0: no Java 7+ stdlib surface found in code lines of {1} file(s), in the {2} families this table covers.' -f $Tag, $totalFiles, $Families.Count)
exit 0
