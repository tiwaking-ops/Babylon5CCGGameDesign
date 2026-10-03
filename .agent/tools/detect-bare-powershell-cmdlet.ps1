<#
.SYNOPSIS
  B5-1733: detect a bare PowerShell CMDLET presented as a runnable instruction
  inside a fenced code block of any `.agent/*.md`.

.DESCRIPTION
  Why this tool exists. B5-1541 closed the defect class but did not enforce it.
  Measured at seed time (2026-10-02), a repo-wide sweep for a bare PowerShell
  cmdlet in instruction position in `.agent/*.md` returned exactly 2 hits and
  BOTH were deliberate counter-examples written by B5-1541 itself:
  `.agent/AGENT_LOOP.md` line 80 quotes the phrase inside the sentence that
  withdraws it, and `.agent/SHELL.knowledge.md` line 66 shows the failing form
  annotated `# FAILS in bash`. The class was closed but unenforced, so the next
  doc edit could reopen it silently.

  The trap it guards is the B5-0777 shape. There, a bash regex embedded in a
  `.ps1` failed to *parse*, so a governance gate had no working implementation
  and every run read as a pass. The instance measured here is quieter: an agent
  whose only shell is bash copy-pastes a line the docs present as an
  instruction, and bash answers `Select-String: command not found`. The line is
  not syntactically wrong for its shell, it is simply the wrong language, and
  nothing in the repo says so at the moment of the copy.

  WHAT COUNTS AS A FINDING, in three conjuncts, each of which is load-bearing:

    1. INSIDE A FENCE. Only fenced code blocks are instruction position. Prose
       that NAMES a cmdlet is describing it, not prescribing it, and firing on
       prose is how a detector trains its readers to ignore it. This conjunct
       alone retires the AGENT_LOOP.md line-80 counter-example, which sits in
       the LOOP procedure block and begins the line with the word
       `Select-String` while being English about withdrawing the one-liner.

    2. THE LINE BEGINS WITH THE CMDLET. Leading whitespace and the pipeline
       punctuation that precedes a sub-expression (`(`, `|`) are stripped
       first, so both `(Select-String -Path ...` and `| Select-String ...` are
       seen for what they are.

    3. THE TOKEN IS AN INVOCATION, NOT A MENTION. The cmdlet name must be
       followed by whitespace and then one of `-`, `{`, or `.`, i.e. a
       parameter, a scriptblock, or a property access. `Select-String
       one-liner is withdrawn` is English whose first word happens to be a
       cmdlet name; `Select-String -Path x` is a command. This is the
       B5-1541 discipline stated one level up: anchor on the invocation, not on
       the bare name, so repair prose does not self-trigger.

  AND THE LINE DOES NOT NAME AN INTERPRETER. A line beginning `powershell`,
  `pwsh`, `&`, `.`, or `.\` is already shell-agnostic by construction and is
  never a finding, whatever follows it.

  THE COUNTER-EXAMPLE PROBLEM, AND HOW IT IS RESOLVED HERE. B5-1733's own
  seed text names this as the substance of the task: a detector that flags the
  two surviving counter-examples is worse than no detector, because the fix is
  then to delete the honest documentation of the failure. Two rules were
  considered.

    (a) Exempt by fence info string (a ```powershell block is deliberate).
        REJECTED, and the rejection is measured, not stylistic: at commit
        02716363 the pre-B5-1541 trap at `.agent/00_BOOT.md` lines 83-85 sits
        inside a fence whose info string IS `powershell`, so this rule would
        have passed the original defect.

    (b) Exempt by an inline annotation, chosen to be one the repo ALREADY
        USES. `.agent/SHELL.knowledge.md` line 66 carries `# FAILS in bash` on
        the very line this tool would otherwise flag. So the annotation
        convention is `# FAILS` (case-insensitive) on the offending line, or
        `# b5-1733 exempt` for a counter-example whose annotation needs to say
        something else. A line carrying either is a declared counter-example:
        the author has written down that the command does not run in the reader's
        shell, which is precisely the information the detector would otherwise
        be supplying by flagging it.

  Rule (b) is chosen over (a) because it is falsifiable in a way (a) is not.
  The annotation is on the LINE, so it travels with the line when it is quoted,
  and a bare cmdlet that nobody annotated is a finding from the first moment it
  is written rather than from whenever somebody remembers to fence it.

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/detect-bare-powershell-cmdlet.ps1

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/detect-bare-powershell-cmdlet.ps1 -SelfTest

.PARAMETER Root
  Directory to sweep. Defaults to the repo this tool ships in.

.PARAMETER SelfTest
  Build the recovered pre-B5-1541 fixture in a temp directory, run this same
  detector over it, and assert it fires. A check is only worth its cost if it
  can fail (B5-1730), so the fixture is the evidence that this tool is not a
  silent green.

.OUTPUTS
  Exit 0 = no finding (pass condition; a clean sweep still prints its receipts).
  Exit 1 = at least one bare cmdlet in instruction position. Each finding names
           the file, the line number, the cmdlet, and whether an annotation was
           seen and REJECTED (present but not one this tool honours).
  Exit 2 = the root is missing or unreadable, or a file could not be read.
           Deliberately NOT 0: a census that could not read the tree must never
           report a clean tree (same discipline as run-dup-census.ps1,
           check-citations.ps1 and detect-filename-lookalikes.ps1).
#>
[CmdletBinding()]
param(
    [string]$Root,
    [switch]$SelfTest
)

$ErrorActionPreference = 'Stop'

# Approved PowerShell verbs that this repo's instruments actually use, taken as
# a literal list rather than a `[A-Z][a-z]+-[A-Z][a-z]+` shape regex: the shape
# regex also matches ordinary hyphenated English at the head of a line, and a
# detector that fires on prose is a detector nobody reads. Common verbs only --
# Lifecycle, Diagnostic, Data and Security verbs are listed because the
# instruments here use them (Get/Test/Set/New/Write/Read/Out/Convert/Measure/
# Select/Sort/Group/Where/ForEach/Compare/Join/Split/Remove/Copy/Move/Rename/
# Resolve/Export/Import/Invoke/Lock/Unlock/Start/Stop/Wait/Add/Clear/Enable/
# Disable/Enter/Exit/Find/Search/Show/Skip/Split/Step/Switch/Undo/Redo/Push/
# Pop/Optimize/Format/Hide/Close/Open/Reset/Resize/Watch/Assert/Complete/
# Confirm/Deny/Request/Submit/Approve/Grant/Revoke/Block/Protect/Unprotect/
# Publish/Install/Register/Unregister/Checkpoint/Restore/Save/Restore/
# Connect/Disconnect/Read/Write/Send/Receive/Trace/Debug/Measure/Ping/Test/
# Repair/Resolve).
$cmdletVerbs = @(
    'Add','Approve','Assert','Backup','Block','Checkpoint','Clear','Close','Compare','Complete',
    'Confirm','Connect','Convert','Copy','Deny','Disable','Disconnect','Enable','Enter','Exit',
    'Expand','Export','Find','Format','Get','Grant','Group','Hide','Import','Install','Invoke',
    'Join','Lock','Measure','Merge','Move','New','Open','Optimize','Ping','Pop','Protect',
    'Publish','Push','Read','Redo','Remove','Rename','Repair','Request','Reset','Resize',
    'Resolve','Restore','Revoke','Save','Search','Select','Send','Set','Show','Skip','Split',
    'Start','Step','Stop','Submit','Switch','Test','Trace','Undo','Unlock','Unprotect',
    'Unregister','Wait','Watch','Where','Write','ForEach'
)

# CASE-SENSITIVE ON PURPOSE, and the first version of this regex carried (?i)
# and was wrong in a way worth recording, because the failure is silent and
# green. Under (?i) the Noun class [A-Z][A-Za-z]* also matches lowercase, so the
# regex matched the VERB alone: `Select-String -Path x` was read as the token
# `Select` followed by `-String -Path x`, the rest no longer began with
# whitespace plus `-`/`{`/`.`, the invocation test rejected the line as a
# mention, and the detector reported ZERO findings on the recovered
# pre-B5-1541 fixture while its own self-test said it was fine. Dropping the
# flag fixed both the missed detection and the noun capture, which had been
# reporting the finding as `Select` rather than `Select-String`. This is the
# B5-1724 shape -- green, and wrong -- reached here through a regex rather than
# through an unanchored digit match.
$cmdletRegex = '^(' + (($cmdletVerbs | Sort-Object -Unique) -join '|') + ')-[A-Z][A-Za-z]*\b'

# A cmdlet token is an INVOCATION when what follows is a parameter, a scriptblock
# or a property access. Anything else (a bare word, a colon, a comma, a period
# ending a sentence) is a mention.
$invokeRegex = '^[ \t]+(-|\{|\.)'

# Line-leading tokens that already name an interpreter, so the rest of the line
# is shell-agnostic by construction.
$interpreterRegex = '^(powershell|pwsh|&\s|\.\\|\./)'

# Declared counter-example annotations, per the .DESCRIPTION rule (b).
$annotRegex = '(?i)#\s*(FAILS|b5-1733\s+exempt)\b'

# Files excluded from the sweep. Every exclusion is PRINTED with its reason, so
# the sweep can never read as cleaner than it is by virtue of what it skipped --
# the blind-spot discipline of check-citations.ps1.
$excluded = @{
    '.agent/HANDOFF.md'         = 'superseded by the runner; boot forbids reading it, so a finding there is unreachable'
    '.agent/DECISIONS.fixed.md' = 'superseded copy of docs/DECISIONS.md; edits belong to the live file only'
}

function Get-FenceSpans([string[]]$lines) {
    # Returns, for each fenced code block, the 0-based index of its first content
    # line and the index of its last. Content only: the ``` fence markers are
    # instructions delimiters, not commands.
    $spans = New-Object System.Collections.Generic.List[object]
    $openAt = -1
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $t = $lines[$i].TrimStart()
        if ($t.StartsWith('```') -or $t.StartsWith('~~~')) {
            if ($openAt -lt 0) { $openAt = $i } else { $openAt = -1 }
            continue
        }
        if ($openAt -ge 0) {
            $spans.Add([pscustomobject]@{ Open = $openAt; Line = $i })
        }
    }
    return $spans
}

function Invoke-Sweep([string]$dir) {
    $findings = New-Object System.Collections.Generic.List[object]
    $stats    = [ordered]@{ files = 0; fences = 0; lines = 0; exempted = 0 }
    $readErrors = 0

    foreach ($f in (Get-ChildItem -LiteralPath $dir -Filter '*.md' -File | Sort-Object Name)) {
        $rel = '.agent/' + $f.Name
        if ($excluded.ContainsKey($rel)) { continue }
        $stats.files++
        try {
            # Explicit UTF-8, no BOM fallback: the B5-1002 trap, where a
            # BOM-less file read naively decodes as host ANSI and the census
            # measures a different file than the one on disk.
            $lines = [System.IO.File]::ReadAllLines($f.FullName, (New-Object System.Text.UTF8Encoding($false)))
        } catch {
            Write-Output ("[B5-1733] WARNING: unreadable file, NOT counted clean: {0} ({1})" -f $rel, $_.Exception.Message)
            $readErrors++
            continue
        }
        $spans = Get-FenceSpans $lines
        $stats.fences += (($spans | Group-Object Open).Count)
        foreach ($s in $spans) {
            $raw = $lines[$s.Line]
            $stats.lines++
            # Strip indentation and the pipeline punctuation that opens a
            # sub-expression, so the cmdlet is at position zero for the test.
            $t = $raw.TrimStart()
            $t = $t -replace '^[|(\s]+', ''
            if ($t -match $interpreterRegex) { continue }
            if ($t -notmatch $cmdletRegex)   { continue }
            # $Matches[0], not $Matches[1]: group 1 is the VERB capture group
            # around the alternation, so reading it reported every finding as
            # `Select` instead of `Select-String`. The DETECTION was right and
            # the LABEL was wrong, which is its own kind of green lie -- a
            # reader cannot check the finding against the source line when the
            # name on the report is not the name on the line.
            $cmdlet = $Matches[0]
            $rest   = $t.Substring($Matches[0].Length)
            if ($rest -notmatch $invokeRegex) { continue }   # conjunct 3: a mention, not a call
            $annotated = ($raw -match $annotRegex)
            # The exemption is applied HERE rather than merely reported. The
            # first version computed $annotated and then fell through to add the
            # finding anyway, printing "ANNOTATION PRESENT BUT NOT HONOURED"
            # -- which is a true statement and a useless gate, since it fires on
            # the one line in the repo that is already correct and so would have
            # shipped a permanently-red detector whose red meant nothing.
            if ($annotated) {
                $stats.exempted++
                continue
            }
            $findings.Add([pscustomobject]@{
                Rel       = $rel
                LineNo    = $s.Line + 1
                Cmdlet    = $cmdlet
                Annotated = $annotated
                Text      = $raw.Trim()
            })
        }
    }
    return [pscustomobject]@{ Findings = $findings; Stats = $stats; ReadErrors = $readErrors }
}

if ($SelfTest) {
    $tmp = Join-Path ([System.IO.Path]::GetTempPath()) ('b51733-selftest-' + [Guid]::NewGuid().ToString('N').Substring(0, 8))
    New-Item -ItemType Directory -Path $tmp -Force | Out-Null
    try {
        # The three real pre-B5-1541 lines recovered verbatim from commit
        # 02716363 (the verified parent of the user-ordered checkpoint
        # 15ded4ad, which had already absorbed B5-1541's edits -- so `git show
        # HEAD` as a baseline would compare post-edit against post-edit and read
        # clean for the wrong reason, the exact quiet-gate failure B5-1541
        # recorded). Line 1 is the fenced `powershell` block from
        # `.agent/00_BOOT.md` lines 83-85, which is the reason the
        # exempt-by-info-string rule was rejected: this trap announces itself as
        # PowerShell and is still wrong.
        $fixture = @(
            'Run the census:',
            '```powershell',
            "   (Select-String -Path .agent/TASK_LEDGER.md -Pattern '^\|+\s*(B5-[0-9]{4}[a-z]?)\s*\|' -AllMatches).Matches |",
            "     ForEach-Object { `$_.Groups[1].Value } | Group-Object | Where-Object Count -gt 1",
            '```'
        )
        # NOTE the fixture .md files are written DIRECTLY into $tmp, because
        # Invoke-Sweep globs *.md -File non-recursively in the directory it is
        # handed. The first version nested them under $tmp/agent/ and
        # $tmp/agent2/ on the theory that the swept directory is named .agent,
        # so the sweep found no files at all, found 0 findings, and the
        # self-test reported PASS on an empty set -- the
        # absence-of-an-error-is-not-presence-of-a-value shape, where the count
        # is the assertion and only the verdict was being read.
        [System.IO.File]::WriteAllLines((Join-Path $tmp '00_BOOT.md'), $fixture, (New-Object System.Text.UTF8Encoding($false)))
        $r = Invoke-Sweep $tmp
        Write-Output ('[B5-1733] SELF-TEST fixture findings: {0}' -f $r.Findings.Count)
        foreach ($x in $r.Findings) {
            Write-Output ('[B5-1733] SELF-TEST   {0}:{1} {2} annotated={3}' -f $x.Rel, $x.LineNo, $x.Cmdlet, $x.Annotated)
        }
        # Assert the COUNT, not just the verdict: a sweep that read no files
        # reports zero findings and would otherwise read as a pass. This is
        # exactly the B5-1730 lesson about measure-task-cells.ps1 printing
        # FULLY COMPLIES over an empty set.
        if ($r.Stats.files -ne 1) {
            Write-Output ('[B5-1733] SELF-TEST FAILED: the sweep read {0} fixture file(s), expected 1. The 0 findings below are meaningless.' -f $r.Stats.files)
            exit 1
        }
        Write-Output ('[B5-1733] SELF-TEST fixture files read: {0}, fenced lines: {1}' -f $r.Stats.files, $r.Stats.lines)
        if ($r.Findings.Count -lt 1) {
            Write-Output '[B5-1733] SELF-TEST FAILED: the detector did not fire on the recovered pre-B5-1541 lines.'
            Write-Output '[B5-1733] A gate that cannot fail is not a gate. This tool is NOT working.'
            exit 1
        }
        # The fixture is TWO trap lines and both must be caught; asserting
        # "at least one" would let the second line regress unnoticed.
        if ($r.Findings.Count -lt 2) {
            Write-Output ('[B5-1733] SELF-TEST FAILED: caught {0} of the 2 recovered trap lines; the pipeline-continuation form is not being seen.' -f $r.Findings.Count)
            exit 1
        }
        Write-Output '[B5-1733] SELF-TEST PASS: the detector fires on both recovered pre-B5-1541 lines.'

        # Second self-test: the annotated counter-example must NOT fire. A
        # detector whose exemption rule is untested is a detector that will
        # either flag honest documentation or exempt real defects at random.
        # Second self-test: the annotated counter-example must NOT fire, and
        # must be tested ALONE rather than alongside fixture 1, because the
        # first version re-swept $tmp after dropping the second fixture into it
        # and so counted fixture 1's two findings as the second test's result.
        # A control that is contaminated by the positive case cannot distinguish
        # "exempted" from "not reached".
        $tmp2 = Join-Path ([System.IO.Path]::GetTempPath()) ('b51733-selftest2-' + [Guid]::NewGuid().ToString('N').Substring(0, 8))
        New-Item -ItemType Directory -Path $tmp2 -Force | Out-Null
        try {
            $fixture2 = @(
                '```',
                "Select-String -Path .agent/TASK_LEDGER.md -Pattern 'x'   # FAILS in bash",
                '```'
            )
            [System.IO.File]::WriteAllLines((Join-Path $tmp2 'SHELL.knowledge.md'), $fixture2, (New-Object System.Text.UTF8Encoding($false)))
            $r2 = Invoke-Sweep $tmp2
            Write-Output ('[B5-1733] SELF-TEST control files read: {0}, findings: {1} (expect 1 file, 0 findings)' -f $r2.Stats.files, $r2.Findings.Count)
            if ($r2.Stats.files -ne 1 -or $r2.Findings.Count -ne 0) {
                Write-Output '[B5-1733] SELF-TEST FAILED: the # FAILS annotation did not exempt the line it is on.'
                exit 1
            }
            Write-Output '[B5-1733] SELF-TEST PASS: the annotation convention exempts exactly what it claims to.'

            # Third self-test: the MENTION case must not fire. This is the
            # AGENT_LOOP.md line-80 shape -- English prose whose first word is a
            # cmdlet name -- and it is the false positive that would have made
            # this tool noise, so it is tested rather than assumed absent.
            $tmp3 = Join-Path ([System.IO.Path]::GetTempPath()) ('b51733-selftest3-' + [Guid]::NewGuid().ToString('N').Substring(0, 8))
            New-Item -ItemType Directory -Path $tmp3 -Force | Out-Null
            try {
                $fixture3 = @(
                    '```',
                    '    Select-String one-liner is withdrawn, not deprecated: it is PowerShell syntax',
                    '```'
                )
                [System.IO.File]::WriteAllLines((Join-Path $tmp3 'AGENT_LOOP.md'), $fixture3, (New-Object System.Text.UTF8Encoding($false)))
                $r3 = Invoke-Sweep $tmp3
                Write-Output ('[B5-1733] SELF-TEST mention-case findings: {0} (expect 0)' -f $r3.Findings.Count)
                if ($r3.Findings.Count -ne 0) {
                    Write-Output '[B5-1733] SELF-TEST FAILED: prose beginning with a cmdlet name fired. Repair prose would self-trigger.'
                    exit 1
                }
                Write-Output '[B5-1733] SELF-TEST PASS: repair prose does not self-trigger.'
                Write-Output '[B5-1733] SELF-TEST PASS: detector can fire, can stay quiet, and can exempt on annotation. All three observed.'
            } finally {
                Remove-Item -LiteralPath $tmp3 -Recurse -Force -ErrorAction SilentlyContinue
            }
        } finally {
            Remove-Item -LiteralPath $tmp2 -Recurse -Force -ErrorAction SilentlyContinue
        }
    } finally {
        Remove-Item -LiteralPath $tmp -Recurse -Force -ErrorAction SilentlyContinue
    }
    exit 0
}

if (-not $Root) { $Root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot) }
if (-not (Test-Path -LiteralPath $Root -PathType Container)) {
    Write-Output ("[B5-1733] EXIT 2: root is not a readable directory: {0}" -f $Root)
    exit 2
}
$agentDir = Join-Path $Root '.agent'
if (-not (Test-Path -LiteralPath $agentDir -PathType Container)) {
    Write-Output ("[B5-1733] EXIT 2: .agent is not a readable directory under {0}" -f $Root)
    exit 2
}

$r = Invoke-Sweep $agentDir

Write-Output ("[B5-1733] root        : {0}" -f $agentDir)
Write-Output ("[B5-1733] swept files : {0} (.agent/*.md, non-recursive)" -f $r.Stats.files)
Write-Output ("[B5-1733] fenced lines: {0} in {1} fence(s)" -f $r.Stats.lines, $r.Stats.fences)
Write-Output ("[B5-1733] exclusions  : {0}" -f $excluded.Count)
foreach ($k in ($excluded.Keys | Sort-Object)) {
    Write-Output ("[B5-1733]   EXCLUDED {0} -- {1}" -f $k, $excluded[$k])
}
Write-Output ("[B5-1733] findings    : {0}" -f $r.Findings.Count)
Write-Output ("[B5-1733] exempted    : {0} line(s) carrying a declared counter-example annotation" -f $r.Stats.exempted)
Write-Output ('[B5-1733] encoding    : UTF-8 read via File.ReadAllLines, no BOM fallback (B5-1002)')

if ($r.ReadErrors -gt 0) {
    Write-Output ('[B5-1733] EXIT 2: {0} file(s) unreadable, so the sweep was NOT complete.' -f $r.ReadErrors)
    exit 2
}

if ($r.Findings.Count -gt 0) {
    Write-Output ''
    foreach ($x in $r.Findings) {
        $note = ''
        if ($x.Annotated) { $note = '  [ANNOTATION PRESENT BUT NOT HONOURED -- fix the annotation, do not silence the tool]' }
        Write-Output ('[B5-1733] FINDING {0}:{1} bare cmdlet {2}{3}' -f $x.Rel, $x.LineNo, $x.Cmdlet, $note)
        Write-Output ('[B5-1733]         {0}' -f $x.Text)
    }
    Write-Output ''
    Write-Output '[B5-1733] REPAIR: prefix the command with `powershell -NoProfile -ExecutionPolicy Bypass -File`'
    Write-Output '[B5-1733] and hand it a SCRIPT (the shipped wrappers under .agent/tools/), never a bare cmdlet.'
    Write-Output '[B5-1733] A deliberate counter-example keeps the line and adds `# FAILS` (or `# b5-1733 exempt`).'
    exit 1
}

exit 0