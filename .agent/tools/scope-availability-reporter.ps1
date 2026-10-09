#Requires -Version 3.0
<#
.SYNOPSIS
    B5-2594: report which one-writer scopes are HELD by a live claim and which
    are FREE, with the holder's agent_id and the minutes until the claim expires.

.DESCRIPTION
    Why this instrument exists.

    AGENTS.md section 5 makes every scope one-writer, and every EXISTING census
    answers a different question:

      run-queue.ps1 -DryRun       "is this ROW taken?"
      ledger-query.ps1            "is this ROW well-formed?"
      scope-collision-census.ps1  "do TWO LIVE CLAIMS collide on one scope?"

    None of them answers the question an agent asks first when choosing work:
    "which of engine/ model/ ai/ ui/ is free RIGHT NOW?" That needs the row
    census, the scope collision census and a manual read of three liveness
    signals per claim. This tool collapses it to one command, one line per scope.

    IT IS AN ADVISORY REPORTER, NOT A GATE. That is a deliberate design
    statement, not a gap. A reporting instrument needs one honest way to fail, so
    the exit codes are:

      0  the report is COMPLETE -- every claim file was read and classified.
      2  the report is INCOMPLETE -- the claims directory is missing, no claim
         files were found, or at least one claim could not be parsed or carried
         no identity. Exit 2 is a FALSE PASS and must never be read as
         "every scope is FREE".

    There is deliberately NO exit 1. This tool never judges, and an exit code
    meaning "I found something wrong" is exactly how a reporter gets read as a
    gate -- an agent would then treat "1" as a veto on a scope it has already
    established is free. The one thing that looks like a failure here, two
    writers on one scope, is scope-collision-census.ps1's exit 1 and stays
    there. Do not add an exit 1 here; that duplication is what produced the
    census divergence recorded in census-crosscheck.ps1.

    SCOPE RECOGNITION IS COPIED, NOT INVENTED. The one-writer root list, the
    per-token scope-to-root mapping and the read-only markers below are carried
    from scope-collision-census.ps1 (B5-2311), for the same reason run-queue.ps1
    copies them (B5-2422): a third hand-rolled variant is how the two existing
    census tools came to disagree in the first place. If that tool changes its
    root list or its markers, these must change with it. Recognition is
    per-TOKEN, not substring, so "b5ccg/src/b5ccg/engine/" and "engine" resolve
    to the same root while "engineer", and the "ai" inside "maintain", do not
    match at all.
.LIVENESS
    Three signals, never one, per .agent/HEARTBEATS/README.md and
    .agent/CLAIMS/README.md: the claim file's own mtime, the owner's heartbeat
    mtime, and the newest report matching the task id. A claim is LIVE while the
    NEWEST of those is inside the TTL -- newest, because the point of the rule is
    that a worker still refreshing its heartbeat is still working however old its
    claim file has become. Taking the OLDEST (the minimum age, or equivalently the
    maximum timestamp) INVERTS the rule and reports a claim FREE while its owner
    is demonstrably still running. The draft that previously occupied this
    filename did exactly that, which is why the arithmetic here is spelled out
    rather than left implicit.

    Verdicts, per .agent/CLAIMS/README.md:
      LIVE     a signal inside the TTL. Only a LIVE claim holds a scope.
      STALE    at least one signal found, none inside the TTL. Holds nothing.
      UNKNOWN  no signal at all, or the file cannot be parsed, or it carries no
                identity. An absent signal is never LIVE and never STALE, and a
                scope whose only claim is UNKNOWN is reported UNKNOWN, never
                FREE: "I could not read this" is not the same fact as "nobody is
                writing there".
      RELEASED terminal status (blocked/released/done/closed/complete) is residue
                from a released claim, normal because new-claim.ps1 refuses a row
                while any file with that task id exists. It holds no scope, and it
                is listed so a permanent red is never mistaken for a busy scope.

    CLOCK INTEGRITY. Liveness is measured from the claim file's MTIME, never from
    the payload's started_utc, for the reason recorded in AGENTS.md section 5
    (B5-1931): a hand-typed started_utc is an unread measurement, and on this
    UTC+13 host stamping local time puts a claim ~13 h ahead, so its age is
    negative and the claim reads LIVE forever. A file mtime is a measurement of
    the clock. A future-dated started_utc is DETECTED and reported rather than
    trusted or ignored silently.

    THE ONE DIVERGENCE FROM scope-collision-census.ps1 IS DELIBERATE and is
    recorded here rather than papered over. That tool treats a missing owner
    heartbeat as merely a missing contributing signal, because it only asks "can
    these two claims be SHOWN to collide?". This tool is asked "can this scope be
    SHOWN to be free?", and falling back to the claim age when the owner signal
    is unjoinable is precisely the B5-0609/B5-0953 bug wearing a different hat. So
    here a claim whose owner heartbeat cannot be joined is reported as UNKNOWN,
    which blocks the FREE verdict for the roots it claims.

.PARAMETER ClaimsDir      Defaults to <repo>/.agent/CLAIMS
.PARAMETER HeartbeatsDir  Defaults to <repo>/.agent/HEARTBEATS
.PARAMETER ReportsDir     Defaults to <repo>/.agent/REPORTS
.PARAMETER TtlMinutes     Claim TTL, default 30 (matches new-claim.ps1)
.PARAMETER SelfTest       Build held/free/unknown fixtures in a temp directory
                          and assert each verdict. A gate never observed RED is
                          not a gate (AGENT_LOOP.md, "The three ways this loop
                          has lied to itself", item 3), so run this before
                          trusting a real run.

.OUTPUTS
    One line per scope:
      SCOPE   VERDICT   HOLDER   EXPIRES   SIGNAL
    where VERDICT is HELD / FREE / UNKNOWN and EXPIRES is the whole minutes of
    TTL left on the newest signal, or "-".

.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/scope-availability-reporter.ps1
.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/scope-availability-reporter.ps1 -SelfTest
#>
[CmdletBinding()]
param(
    [string] $ClaimsDir,
    [string] $HeartbeatsDir,
    [string] $ReportsDir,
    [int]    $TtlMinutes = 30,
    [switch] $SelfTest
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# AGENTS.md section 5, verbatim: one writer per scope. Copied from
# scope-collision-census.ps1 -- see the .DESCRIPTION note on not inventing a
# third variant.
$OneWriterRoots = @('engine', 'model', 'ai', 'ui')

# Copied verbatim from scope-collision-census.ps1. An unrecognised scope is
# treated as a WRITER, which can only ever report more, never less.
$ReadOnlyMarkers = @(
    'read-only', 'read only', 'readonly',
    'no src edits', 'no edits', 'ledger read-only', 'no harness edits'
)

# A payload started_utc more than this many minutes AHEAD of the wall clock is
# reported as a clock defect. Same value and same reasoning as run-queue.ps1's
# FutureTimestampToleranceMinutes (B5-0952).
$FutureTimestampToleranceMinutes = 60
function Get-RepoRoot {
    return (Split-Path -Parent (Split-Path -Parent $PSScriptRoot))
}

function Get-ClaimField {
    <#
      Read a property that may be absent, WITHOUT tripping Set-StrictMode, so
      that "absent" stays distinguishable from "corrupt".

      This helper exists because of a measured defect in the first draft of the
      sibling census: a single try/catch wrapped BOTH ConvertFrom-Json AND the
      property reads, so StrictMode's "property not found" on a perfectly valid
      claim file was reported as UNPARSEABLE, and three claims were silently
      skipped from a real run. A census that quietly drops files it misreads is
      worse than no census: it returns a green exit code it did not earn.
    #>
    param($Obj, [string[]] $Names)
    foreach ($n in $Names) {
        $p = $Obj.PSObject.Properties[$n]
        if ($null -ne $p -and $null -ne $p.Value) { return $p.Value }
    }
    return $null
}

function Get-NormKey {
    <#
      Normalised identity key, matching the Get-NormName convention used by
      run-queue.ps1, ledger-query.ps1 and scope-collision-census.ps1: letters and
      digits only, so "... free 2" and "... free2" cannot be two owners.
    #>
    param([string] $Name)
    if (-not $Name) { return '' }
    return ([regex]::Replace($Name.ToLowerInvariant(), '[^a-z0-9]', ''))
}

function Get-ScopeRoots {
    <#
      The one-writer roots a free-text scope string touches, per-token, returned
      as a pipe-delimited STRING rather than a collection.

      Copied from scope-collision-census.ps1 including its stated reason for the
      string return: an empty PowerShell collection emits nothing across a
      function boundary, so the caller holds $null and .Count throws under
      StrictMode; a comma-wrapped array double-wraps instead, and -contains then
      silently returns $false over a REAL conflict. A delimited string survives
      both cases: '' for none, 'engine' for one, 'engine|model' for several.
    #>
    param([string] $Scope)
    $out = @()
    if (-not $Scope) { return '' }
    $tokens = [regex]::Split($Scope.ToLowerInvariant(), '[^a-z0-9]+')
    foreach ($root in $OneWriterRoots) {
        if ($tokens -contains $root) { $out += $root }
    }
    return ($out -join '|')
}

function Test-ReadOnlyScope {
    <#
      A scope string that says it only reads is a reader, and a read-only claim
      fences nothing -- it must not be counted as holding a scope. Copied from
      scope-collision-census.ps1, which draws the same distinction for the same
      reason: this repo's own row scopes use the phrasing ("engine read-only",
      "fences ... read-only").
    #>
    param([string] $Scope)
    if (-not $Scope) { return $false }
    $lowered = $Scope.ToLowerInvariant()
    foreach ($marker in $ReadOnlyMarkers) {
        if ($lowered.Contains($marker)) { return $true }
    }
    return $false
}

function Get-HeartbeatIndex {
    <#
      Normalised owner key -> that owner's NEWEST heartbeat mtime.

      Two details are load-bearing. Several live files can carry one agent_id in
      this store (the identity and collision history in
      .agent/HEARTBEATS/README.md), so the newest mtime wins rather than the
      first file enumerated -- taking the first would make liveness depend on
      directory order. And a filename is not an identity: a file with no
      readable agent_id contributes nothing, because keying a liveness signal on
      its stem is how a report file becomes evidence that somebody is alive.

      _retired/ and _quarantine/ are NOT read -- an archived identity's
      heartbeat signal is UNKNOWN by policy, never LIVE.
    #>
    param([string] $Dir)
    $index = @{}
    if (-not $Dir -or -not (Test-Path -LiteralPath $Dir)) { return $index }
    foreach ($f in @(Get-ChildItem -LiteralPath $Dir -Filter '*.json' -File -ErrorAction SilentlyContinue)) {
        $owner = $null
        try {
            $j = [IO.File]::ReadAllText($f.FullName, [System.Text.Encoding]::UTF8) |
                    ConvertFrom-Json -ErrorAction Stop
            if ($j.agent_id) { $owner = [string]$j.agent_id }
        } catch { $owner = $null }
        if (-not $owner) { continue }
        $key = Get-NormKey $owner
        if (-not $index.ContainsKey($key) -or $f.LastWriteTimeUtc -gt $index[$key]) {
            $index[$key] = $f.LastWriteTimeUtc
        }
    }
    return $index
}
function Get-ClaimVerdict {
    <#
      Classify one claim file into LIVE / STALE / RELEASED / UNKNOWN, together
      with the signal set behind the verdict.

      Returns a HASHTABLE, not a collection: every bug recorded in
      scope-collision-census.ps1's Invoke-Census comment came from returning
      collections across a PowerShell function boundary (an empty collection
      arriving as $null, a comma-wrapped array double-wrapping into
      [System.Object[]], a hashtable parameter failing to bind a PSCustomObject).
      Each produced a wrong or missing result while looking like it worked.
    #>
    param(
        [string]    $Path,
        [int]       $TtlMinutes,
        [hashtable] $HeartbeatIndex
    )

    $now = [DateTime]::UtcNow
    $signals = @()

    # Signal 1: the claim file's own mtime.
    $claimMtime = $null
    try { $claimMtime = (Get-Item -LiteralPath $Path).LastWriteTimeUtc } catch { $claimMtime = $null }
    if ($null -ne $claimMtime) {
        $signals += [pscustomobject]@{ name = 'claim-mtime'; mtime = $claimMtime }
    }

    # (1) JSON integrity, isolated from every property read on purpose.
    #
    # A file that cannot be READ AT ALL is a different fact from a file that
    # reads but does not parse, and the self-test caught this tool collapsing
    # the two into CORRUPT-JSON. The distinction is load-bearing for the owner:
    # "your claim file is gone" and "your claim file is malformed" call for
    # different repairs, and reporting a missing file as malformed sends the
    # owner to edit a file that does not exist.
    if (-not (Test-Path -LiteralPath $Path)) {
        return @{
            verdict = 'UNKNOWN'; category = 'MISSING-FILE'
            task = $null; owner = $null; scope = $null; status = $null
            parseError = 'claim file not present on disk'
            ageMin = $null; minutesLeft = $null
            signals = @(); signalUsed = $null; readOnly = $false
            roots = ''; ownerJoined = $false; futureStampMin = $null
        }
    }

    $obj = $null
    $parseError = $null
    try {
        $obj = [IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8) |
                ConvertFrom-Json -ErrorAction Stop
    } catch {
        $parseError = $_.Exception.Message.Split([char]10)[0]
    }
    if ($parseError) {
        # A corrupt file cannot be classified from parsed JSON -- but it can still
        # be PROVABLY harmless. If its raw text carries a terminal status, its
        # owner already let go, so it cannot be a live holder whatever else is
        # wrong with it. Recorded by scope-collision-census.ps1 against the
        # genuinely corrupt .agent/CLAIMS/B5-2002.json: a gate that stays red
        # forever on unrelated residue is a gate nobody runs. Anything unreadable
        # WITHOUT a terminal status still blocks the FREE verdict (exit 2).
        $rawProbe = ''
        try { $rawProbe = [IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8) } catch { $rawProbe = '' }
        $terminal = [regex]::Match($rawProbe, '"(?:status|state)"\s*:\s*"(blocked|released|done|closed|complete)"', 'IgnoreCase')
        if ($terminal.Success) {
            return @{
                verdict = 'RELEASED'; category = 'TERMINAL-STATUS-UNPARSED'
                task = $null; owner = $null; scope = $null
                status = $terminal.Groups[1].Value.ToLowerInvariant()
                parseError = $parseError; ageMin = $null; minutesLeft = $null
                signals = @(); signalUsed = $null; readOnly = $false
                roots = ''; ownerJoined = $false; futureStampMin = $null
            }
        }
        return @{
            verdict = 'UNKNOWN'; category = 'CORRUPT-JSON'
            task = $null; owner = $null; scope = $null; status = $null
            parseError = $parseError; ageMin = $null; minutesLeft = $null
            signals = @(); signalUsed = $null; readOnly = $false
            roots = ''; ownerJoined = $false; futureStampMin = $null
        }
    }

    # (2) Identity. Two schemas are in live use in .agent/CLAIMS: new-claim.ps1
    # writes "task"/"agent_id"; hand-authored and BLOCKED-release files write
    # "task_id". Reading only one of them is what a first draft got wrong.
    $task   = Get-ClaimField -Obj $obj -Names @('task', 'task_id', 'taskId')
    $owner  = Get-ClaimField -Obj $obj -Names @('agent_id', 'agentId', 'owner')
    $status = Get-ClaimField -Obj $obj -Names @('status', 'state')

    $task   = if ($null -eq $task)   { $null } else { [string]$task }
    $owner  = if ($null -eq $owner)  { $null } else { [string]$owner }
    $status = if ($null -eq $status) { '' }    else { ([string]$status).ToLowerInvariant() }

    # Claim files carry `scope` as either a bare string or an ARRAY of strings.
    # Joining is what lets one claim naming four directories answer for four
    # separate single-directory claims.
    $scopeRaw = $null
    $scopeField = Get-ClaimField -Obj $obj -Names @('scope', 'scopes')
    if ($scopeField -is [string]) {
        $scopeRaw = [string]$scopeField
    } elseif ($null -ne $scopeField) {
        $scopeRaw = (@($scopeField) | ForEach-Object { [string]$_ }) -join ' , '
    }

    if (-not $task -or -not $owner) {
        return @{
            verdict = 'UNKNOWN'; category = 'NO-IDENTITY'
            task = $task; owner = $owner; scope = $scopeRaw; status = $status
            parseError = ("missing task/agent_id (have task={0}, agent_id={1})" -f $task, $owner)
            ageMin = $null; minutesLeft = $null; signals = @(); signalUsed = $null
            readOnly = $false; roots = ''; ownerJoined = $false; futureStampMin = $null
        }
    }

    # (3) Terminal status means the owner already let go. Reported, never a
    # holder: AGENT_LOOP.md says a red gate means "BLOCKED, release, stop that
    # item only", and the claim FILE survives that release.
    if ($status -in @('blocked', 'released', 'done', 'closed', 'complete')) {
        return @{
            verdict = 'RELEASED'; category = 'TERMINAL-STATUS'
            task = $task; owner = $owner; scope = $scopeRaw; status = $status
            parseError = $null; ageMin = $null; minutesLeft = $null
            signals = @(); signalUsed = $null
            readOnly = (Test-ReadOnlyScope -Scope $scopeRaw)
            roots = (Get-ScopeRoots -Scope $scopeRaw)
            ownerJoined = $false; futureStampMin = $null
        }
    }

    # (4) Signal 2: the owner's heartbeat, and whether it could be joined at all.
    $ownerJoined = $false
    if ($owner) {
        $key = Get-NormKey $owner
        if ($HeartbeatIndex.ContainsKey($key)) {
            $ownerJoined = $true
            $signals += [pscustomobject]@{ name = 'heartbeat'; mtime = $HeartbeatIndex[$key] }
        }
    }

    # (5) Signal 3: the report signal, contributing only -- a report is written at
    # close-out, so its absence mid-task is the normal case.
    if ($script:ReportsDir -and (Test-Path -LiteralPath $script:ReportsDir)) {
        $newest = $null
        foreach ($f in @(Get-ChildItem -LiteralPath $script:ReportsDir -Filter "*$task*.md" -File -ErrorAction SilentlyContinue)) {
            if ($null -eq $newest -or $f.LastWriteTimeUtc -gt $newest) { $newest = $f.LastWriteTimeUtc }
        }
        if ($null -ne $newest) {
            $signals += [pscustomobject]@{ name = 'report'; mtime = $newest }
        }
    }

    # (6) Clock defect detection. Reported, never used as a liveness signal.
    $futureStampMin = $null
    $stampRaw = Get-ClaimField -Obj $obj -Names @('started_utc', 'claimed_utc', 'started')
    if ($stampRaw) {
        $parsed = [DateTimeOffset]::MinValue
        if ([DateTimeOffset]::TryParse([string]$stampRaw, [ref]$parsed)) {
            $ahead = ($parsed - [DateTimeOffset]::UtcNow).TotalMinutes
            if ($ahead -gt $FutureTimestampToleranceMinutes) { $futureStampMin = $ahead }
        }
    }

    $roots    = (Get-ScopeRoots -Scope $scopeRaw)
    $readOnly = (Test-ReadOnlyScope -Scope $scopeRaw)

    if ($signals.Count -eq 0) {
        return @{
            verdict = 'UNKNOWN'; category = 'NO-SIGNAL'
            task = $task; owner = $owner; scope = $scopeRaw; status = $status
            parseError = $null; ageMin = $null; minutesLeft = $null
            signals = @(); signalUsed = $null
            readOnly = $readOnly; roots = $roots
            ownerJoined = $ownerJoined; futureStampMin = $futureStampMin
        }
    }

    # THE NEWEST signal decides, not the oldest.
    #
    # Every signal is materialised as a UTC DateTime before comparison, because
    # the file mtimes and the payload stamps are not on the same clock and a
    # local-vs-UTC mix-up inside a Sort-Object silently reorders the whole set.
    # A negative age is possible -- a claim mtime in the future -- and is treated
    # as age 0 (freshest possible), which is the conservative direction for a
    # HELD verdict: it withholds a scope rather than freeing it.
    $newest = $signals | Sort-Object -Property mtime -Descending | Select-Object -First 1
    $ageMin = ($now - $newest.mtime).TotalMinutes
    $verdict = if ($ageMin -le $TtlMinutes) { 'LIVE' } else { 'STALE' }
    $minutesLeft = [math]::Max(0, [math]::Ceiling($TtlMinutes - $ageMin))

    return @{
        verdict = $verdict; category = $verdict
        task = $task; owner = $owner; scope = $scopeRaw; status = $status
        parseError = $null; ageMin = $ageMin; minutesLeft = $minutesLeft
        signals = @($signals | ForEach-Object { $_.name })
        signalUsed = $newest.name
        readOnly = $readOnly; roots = $roots
        ownerJoined = $ownerJoined; futureStampMin = $futureStampMin
    }
}
function Invoke-Report {
    <#
      Build $script:Report in place. No collection crosses a function boundary;
      every bug in scope-collision-census.ps1's Invoke-Census came from doing
      exactly that, so this one writes to script scope instead of returning.
    #>
    param([string] $Claims, [string] $Heartbeats, [string] $Reports, [int] $Ttl)

    $script:ReportsDir = $Reports
    $report = @{
        claimsRead       = 0
        corrupt          = @()
        releasedUnparsed = @()
        noIdentity       = @()
        released         = @()
        stale            = @()
        noRoot           = @()
        holders          = @{}     # root -> list of holder records
        unknown          = @{}     # root -> list of reason strings
        extras           = @()     # live claims naming no one-writer root, or read-only
        clockDefects     = @()
    }

    if (-not $Claims -or -not (Test-Path -LiteralPath $Claims)) { $script:Report = $report; return }
    $files = @(Get-ChildItem -LiteralPath $Claims -Filter '*.json' -File -ErrorAction SilentlyContinue)
    if ($files.Count -eq 0) { $script:Report = $report; return }

    $hbIndex = Get-HeartbeatIndex -Dir $Heartbeats

    foreach ($f in $files) {
        $report.claimsRead++
        $v = Get-ClaimVerdict -Path $f.FullName -TtlMinutes $Ttl -HeartbeatIndex $hbIndex

        switch ($v.category) {
            'CORRUPT-JSON' {
                # The scope this file names cannot be resolved at all, so no FREE
                # verdict may rest on it. Drives exit 2; never silently skipped.
                $report.corrupt += [pscustomobject]@{ path = $f.Name; error = $v.parseError }
                continue
            }
            'NO-IDENTITY' {
                $report.noIdentity += [pscustomobject]@{ path = $f.Name; error = $v.parseError }
                continue
            }
            'TERMINAL-STATUS' {
                $report.released += [pscustomobject]@{
                    path = $f.Name; task = $v.task; owner = $v.owner
                    status = $v.status; scope = $v.scope; roots = $v.roots
                }
                continue
            }
            'TERMINAL-STATUS-UNPARSED' {
                $report.releasedUnparsed += [pscustomobject]@{
                    path = $f.Name; status = $v.status; error = $v.parseError
                }
                continue
            }
        }

        if ($null -ne $v.futureStampMin) {
            $report.clockDefects += [pscustomobject]@{
                path = $f.Name; task = $v.task; owner = $v.owner
                aheadMin = [math]::Round($v.futureStampMin, 1)
            }
        }

        if ($v.verdict -eq 'RELEASED') { continue }

        if ($v.verdict -eq 'STALE') {
            $report.stale += [pscustomobject]@{
                path = $f.Name; task = $v.task; owner = $v.owner
                ageMin = [math]::Round($v.ageMin, 1); scope = $v.scope
            }
            continue
        }

        if ($v.verdict -eq 'UNKNOWN') {
            # UNKNOWN becomes neither FREE nor HELD. It is recorded against the
            # roots the claim names, which is what stops those scopes being
            # certified free on the strength of a file nobody could read.
            $why = $v.category
            if ($v.category -eq 'NO-SIGNAL' -and -not $v.ownerJoined) {
                $why = 'NO-SIGNAL / OWNER-HEARTBEAT-UNJOINABLE'
            }
            foreach ($root in @($v.roots -split '\|' | Where-Object { $_ -ne '' })) {
                if (-not $report.unknown.ContainsKey($root)) { $report.unknown[$root] = @() }
                $report.unknown[$root] += ("{0} ({1})" -f $v.task, $why)
            }
            continue
        }

        # LIVE from here.
        if ($v.readOnly) {
            # A read-only claim fences nothing, so it must not withhold a scope.
            $report.extras += [pscustomobject]@{
                kind = 'READ-ONLY'; task = $v.task; owner = $v.owner; scope = $v.scope
            }
            continue
        }
        if (-not $v.roots) {
            $report.noRoot += [pscustomobject]@{ task = $v.task; owner = $v.owner; scope = $v.scope }
            $report.extras += [pscustomobject]@{
                kind = 'NO-ONE-WRITER-ROOT'; task = $v.task; owner = $v.owner; scope = $v.scope
            }
            continue
        }
        foreach ($root in @($v.roots -split '\|' | Where-Object { $_ -ne '' })) {
            if (-not $report.holders.ContainsKey($root)) { $report.holders[$root] = @() }
            $report.holders[$root] += [pscustomobject]@{
                task = $v.task; owner = $v.owner; minutesLeft = $v.minutesLeft
                signal = $v.signalUsed; scope = $v.scope
            }
        }
    }

    $script:Report = $report
}
function Get-ScopeVerdict {
    <#
      The one place the FREE / HELD / UNKNOWN decision is made, so the printed
      table and the exit code can never disagree about a scope.
    #>
    param([string] $Root, $R)
    if ($R.holders.ContainsKey($Root)) {
        return @{ verdict = 'HELD'; holders = @($R.holders[$Root]) }
    }
    if ($R.unknown.ContainsKey($Root)) {
        return @{ verdict = 'UNKNOWN'; reasons = @($R.unknown[$Root]) }
    }
    return @{ verdict = 'FREE'; holders = @() }
}

function Write-ReportText {
    param($R)

    Write-Output ("[B5-2594] SCOPE AVAILABILITY -- claims read {0}, TTL {1} min, at {2}" -f `
        $R.claimsRead, $script:TtlUsed, ([DateTime]::UtcNow.ToString('yyyy-MM-ddTHH:mm:ssZ')))
    Write-Output ('[B5-2594] {0,-9} {1,-8} {2,-46} {3,-9} {4}' -f 'SCOPE', 'VERDICT', 'HOLDER', 'EXPIRES', 'SIGNAL')

    foreach ($root in $OneWriterRoots) {
        $sv = Get-ScopeVerdict -Root $root -R $R
        if ($sv.verdict -eq 'HELD') {
            $first = $true
            foreach ($h in ($sv.holders | Sort-Object -Property minutesLeft)) {
                Write-Output ('[B5-2594] {0,-9} {1,-8} {2,-46} {3,-9} {4}' -f `
                    $(if ($first) { $root } else { '' }), 'HELD', $h.owner, ("{0}m" -f $h.minutesLeft), $h.signal)
                $first = $false
            }
            if ($sv.holders.Count -gt 1) {
                Write-Output ("[B5-2594]   TWO LIVE HOLDERS on {0} ({1}) -- AGENTS.md section 5 is one writer per scope. .agent/tools/scope-collision-census.ps1 owns that finding and owns the failing exit code; this tool reports it and exits 0." -f `
                    $root, (($sv.holders | ForEach-Object { $_.task }) -join ', '))
            }
        } elseif ($sv.verdict -eq 'UNKNOWN') {
            Write-Output ('[B5-2594] {0,-9} {1,-8} {2,-46} {3,-9} {4}' -f `
                $root, 'UNKNOWN', (($sv.reasons) -join '; '), '-', 'unreadable-claim')
            Write-Output ("[B5-2594]   {0} is NOT certified FREE: an absent or unread signal is UNKNOWN, never STALE (B5-0609/B5-0953). This tool will not call a scope free on a claim it could not read." -f $root)
        } else {
            Write-Output ('[B5-2594] {0,-9} {1,-8} {2,-46} {3,-9} {4}' -f $root, 'FREE', '-', '-', '-')
        }
    }

    foreach ($x in $R.extras) {
        Write-Output ("[B5-2594] {0} {1} -- owner {2} names no one-writer scope root to withhold, so it fences nothing: {3}" -f `
            $x.kind, $x.task, $x.owner, $x.scope)
    }
    foreach ($s in $R.stale) {
        Write-Output ("[B5-2594] STALE {0} -- owner {1}, newest signal {2} min old (TTL {3}). Holds no scope; reaping it is a separate, owner-recorded action, not this tool's to take." -f `
            $s.task, $s.owner, $s.ageMin, $script:TtlUsed)
    }
    foreach ($r in $R.released) {
        Write-Output ("[B5-2594] RELEASED {0} -- owner {1}, status {2}, file residue from a released claim, holds no scope: {3}" -f `
            $r.task, $r.owner, $r.status, $r.scope)
    }
    foreach ($r in $R.releasedUnparsed) {
        Write-Output ("[B5-2594] RELEASED-UNPARSED {0} -- corrupt JSON, but its raw text carries status {1}, so it cannot be a live holder. Its owner should still repair it: {2}" -f `
            $r.path, $r.status, $r.error)
    }
    foreach ($c in $R.clockDefects) {
        Write-Output ("[B5-2594] CLOCK-DEFECT {0} ({1}, owner {2}) -- payload started_utc sits {3} min AHEAD of real UTC, so a negative age would read LIVE forever (B5-1931). Not used as a signal; the file mtime was. Repair by re-creating with new-claim.ps1." -f `
            $c.path, $c.task, $c.owner, $c.aheadMin)
    }
    foreach ($c in $R.corrupt) {
        Write-Output ("[B5-2594] CORRUPT CLAIM {0} -- {1}" -f $c.path, $c.error)
        Write-Output '[B5-2594]   unreadable: the scope it names cannot be resolved, so no FREE verdict may rest on this run. Drives exit 2, which is a false pass.'
    }
    foreach ($n in $R.noIdentity) {
        Write-Output ("[B5-2594] NO-IDENTITY {0} -- {1}" -f $n.path, $n.error)
        Write-Output '[B5-2594]   carries neither task nor agent_id, so even its scope cannot be resolved. Drives exit 2.'
    }
}
# -- self-test -------------------------------------------------------------------
function Invoke-SelfTest {
    <#
      Fixtures in an isolated temp tree, NEVER the live tree. Each case asserts a
      verdict this tool actually produces, and the DIRECTION of each assertion
      matters as much as its value.

      A tool never observed failing is not evidence (AGENT_LOOP.md, "The three
      ways this loop has lied to itself", item 3), so every case here was seen
      failing against the draft that previously occupied this filename.
    #>
    $tmp = Join-Path ([IO.Path]::GetTempPath()) ("b52594-selftest-" + [Guid]::NewGuid().ToString('N').Substring(0, 8))
    New-Item -ItemType Directory -Path $tmp | Out-Null
    $script:TtlUsed = 30
    $failures = 0
    try {
        function New-FixtureClaim {
            param([string] $Dir, [string] $Name, [string] $Task, [string] $Owner, [string[]] $Scope, [string] $Status)
            $obj = [ordered]@{
                task = $Task; agent_id = $Owner
                started_utc = [DateTime]::UtcNow.ToString('yyyy-MM-ddTHH:mm:ssZ')
                ttl_min = 30; scope = $Scope; javac = '1.8.0_292'
            }
            if ($Status) { $obj['status'] = $Status }
            [IO.File]::WriteAllText((Join-Path $Dir $Name), ($obj | ConvertTo-Json -Depth 4), (New-Object System.Text.UTF8Encoding($false)))
        }

        function New-FixtureHeartbeat {
            param([string] $Dir, [string] $Name, [string] $Owner)
            New-Item -ItemType Directory -Path $Dir -Force | Out-Null
            $obj = [ordered]@{
                schema_version = 1; agent_id = $Owner
                utc = [DateTime]::UtcNow.ToString('yyyy-MM-ddTHH:mm:ssZ')
                state = 'active'; live_claims = @('B5-0000'); javac = '1.8.0_292'
            }
            [IO.File]::WriteAllText((Join-Path $Dir $Name), ($obj | ConvertTo-Json -Depth 4), (New-Object System.Text.UTF8Encoding($false)))
        }

        function Age-Fixture {
            param([string] $Path, [int] $Minutes)
            $t = (Get-Item -LiteralPath $Path).LastWriteTimeUtc.AddMinutes(-$Minutes)
            [IO.File]::SetLastWriteTimeUtc($Path, $t)
        }

        $noreports = Join-Path $tmp 'noreports'
        New-Item -ItemType Directory -Path $noreports | Out-Null
        # CASE 1 -- a fresh claim on model/, with a fresh owner heartbeat: HELD.
        $d1 = Join-Path $tmp 'fresh'
        New-Item -ItemType Directory -Path $d1 | Out-Null
        $hb1 = Join-Path $tmp 'hb1'
        New-FixtureHeartbeat -Dir $hb1 -Name 'Alpha.json' -Owner 'Selftest Alpha'
        New-FixtureClaim -Dir $d1 -Name 'B5-9001.json' -Task 'B5-9001' -Owner 'Selftest Alpha' -Scope @('b5ccg/src/b5ccg/model/')
        Invoke-Report -Claims $d1 -Heartbeats $hb1 -Reports $noreports -Ttl 30
        $sv1 = Get-ScopeVerdict -Root 'model' -R $script:Report
        $ok1 = ($sv1.verdict -eq 'HELD' -and $sv1.holders.Count -eq 1)
        Write-Output ("[B5-2594] SELFTEST 1 fresh claim holds model -> expect HELD, got {0}: {1}" -f $sv1.verdict, $(if ($ok1) { 'PASS' } else { 'FAIL' }))
        if (-not $ok1) { $failures++ }

        # CASE 2 -- the same claim aged past the TTL with no joinable owner
        # heartbeat: STALE, holding nothing. An absent owner signal is UNKNOWN and
        # never LIVE, so a stale claim must not withhold a scope.
        $d2 = Join-Path $tmp 'stale'
        New-Item -ItemType Directory -Path $d2 | Out-Null
        New-FixtureClaim -Dir $d2 -Name 'B5-9002.json' -Task 'B5-9002' -Owner 'Selftest Ghost' -Scope @('engine')
        Age-Fixture -Path (Join-Path $d2 'B5-9002.json') -Minutes 45
        Invoke-Report -Claims $d2 -Heartbeats (Join-Path $tmp 'nobody') -Reports $noreports -Ttl 30
        $r2 = $script:Report
        $sv2 = Get-ScopeVerdict -Root 'engine' -R $r2
        $ok2 = ($sv2.verdict -eq 'FREE' -and $r2.stale.Count -eq 1)
        Write-Output ("[B5-2594] SELFTEST 2 claim aged 45 min, no heartbeat -> expect STALE so FREE, got {0} with {1} stale: {2}" -f $sv2.verdict, $r2.stale.Count, $(if ($ok2) { 'PASS' } else { 'FAIL' }))
        if (-not $ok2) { $failures++ }

        # CASE 3 -- a claim on a path outside the four one-writer roots withholds
        # nothing. Over-blocking is a real failure mode for this tool: it would
        # tell an agent to avoid a directory nobody is writing.
        $d3 = Join-Path $tmp 'noroot'
        New-Item -ItemType Directory -Path $d3 | Out-Null
        $hb3 = Join-Path $tmp 'hb3'
        New-FixtureHeartbeat -Dir $hb3 -Name 'Gamma.json' -Owner 'Selftest Gamma'
        New-FixtureClaim -Dir $d3 -Name 'B5-9003.json' -Task 'B5-9003' -Owner 'Selftest Gamma' -Scope @('.agent/TASK_LEDGER.md')
        Invoke-Report -Claims $d3 -Heartbeats $hb3 -Reports $noreports -Ttl 30
        $r3 = $script:Report
        $ok3 = ($r3.holders.Count -eq 0 -and $r3.noRoot.Count -eq 1)
        Write-Output ("[B5-2594] SELFTEST 3 claim names no one-writer root -> expect 0 holders and 1 no-root record, got {0}/{1}: {2}" -f $r3.holders.Count, $r3.noRoot.Count, $(if ($ok3) { 'PASS' } else { 'FAIL' }))
        if (-not $ok3) { $failures++ }

        # CASE 4 -- the B5-2281 shape: "b5ccg/src/b5ccg/engine/" and "engine" are
        # the same one-writer root and must both answer to it. Substring matching
        # would miss this, and per-token matching must not.
        $d4 = Join-Path $tmp 'spelling'
        New-Item -ItemType Directory -Path $d4 | Out-Null
        $hb4 = Join-Path $tmp 'hb4'
        New-FixtureHeartbeat -Dir $hb4 -Name 'Delta.json' -Owner 'Selftest Delta'
        New-FixtureClaim -Dir $d4 -Name 'B5-9004.json' -Task 'B5-9004' -Owner 'Selftest Delta' -Scope @('b5ccg/src/b5ccg/engine/')
        Invoke-Report -Claims $d4 -Heartbeats $hb4 -Reports $noreports -Ttl 30
        $ok4a = ((Get-ScopeVerdict -Root 'engine' -R $script:Report).verdict -eq 'HELD')
        $d4b = Join-Path $tmp 'spelling2'
        New-Item -ItemType Directory -Path $d4b | Out-Null
        New-FixtureClaim -Dir $d4b -Name 'B5-9005.json' -Task 'B5-9005' -Owner 'Selftest Delta' -Scope @('engine')
        Invoke-Report -Claims $d4b -Heartbeats $hb4 -Reports $noreports -Ttl 30
        $ok4b = ((Get-ScopeVerdict -Root 'engine' -R $script:Report).verdict -eq 'HELD')
        $ok4 = ($ok4a -and $ok4b)
        Write-Output ("[B5-2594] SELFTEST 4 both spellings of engine resolve to one root -> expect HELD/HELD, got {0}/{1}: {2}" -f `
            $(if ($ok4a) { 'HELD' } else { 'other' }), $(if ($ok4b) { 'HELD' } else { 'other' }), $(if ($ok4) { 'PASS' } else { 'FAIL' }))
        if (-not $ok4) { $failures++ }
        # CASE 5 -- "engine read-only" is a reader. It fences nothing, so engine must
        # read FREE while that claim is live. A tool that blocks here is strictly
        # less useful than one that never reports.
        $d5 = Join-Path $tmp 'readonly'
        New-Item -ItemType Directory -Path $d5 | Out-Null
        $hb5 = Join-Path $tmp 'hb5'
        New-FixtureHeartbeat -Dir $hb5 -Name 'Epsilon.json' -Owner 'Selftest Epsilon'
        New-FixtureClaim -Dir $d5 -Name 'B5-9006.json' -Task 'B5-9006' -Owner 'Selftest Epsilon' -Scope @('engine read-only')
        Invoke-Report -Claims $d5 -Heartbeats $hb5 -Reports $noreports -Ttl 30
        $r5 = $script:Report
        $sv5 = Get-ScopeVerdict -Root 'engine' -R $r5
        $ok5 = ($sv5.verdict -eq 'FREE' -and $r5.extras.Count -eq 1)
        Write-Output ("[B5-2594] SELFTEST 5 read-only claim does not hold engine -> expect FREE, got {0}: {1}" -f $sv5.verdict, $(if ($ok5) { 'PASS' } else { 'FAIL' }))
        if (-not $ok5) { $failures++ }

        # CASE 6 -- a BLOCKED claim file left on disk is residue from a released
        # claim, not a writer. Counting it would make ui permanently busy.
        $d6 = Join-Path $tmp 'released'
        New-Item -ItemType Directory -Path $d6 | Out-Null
        $hb6 = Join-Path $tmp 'hb6'
        New-FixtureHeartbeat -Dir $hb6 -Name 'Zeta.json' -Owner 'Selftest Zeta'
        New-FixtureClaim -Dir $d6 -Name 'B5-9007.json' -Task 'B5-9007' -Owner 'Selftest Zeta' -Scope @('ui') -Status 'BLOCKED'
        Invoke-Report -Claims $d6 -Heartbeats $hb6 -Reports $noreports -Ttl 30
        $r6 = $script:Report
        $ok6 = ((Get-ScopeVerdict -Root 'ui' -R $r6).verdict -eq 'FREE' -and $r6.released.Count -eq 1)
        Write-Output ("[B5-2594] SELFTEST 6 released residue holds nothing -> expect FREE, got {0}: {1}" -f (Get-ScopeVerdict -Root 'ui' -R $r6).verdict, $(if ($ok6) { 'PASS' } else { 'FAIL' }))
        if (-not $ok6) { $failures++ }

        # CASE 7 -- corrupt JSON with no terminal status. The run must be
        # INCOMPLETE (exit 2), never a clean set of FREE verdicts. The fixture is
        # the shape of the genuinely corrupt .agent/CLAIMS/B5-2002.json.
        $d7 = Join-Path $tmp 'corrupt'
        New-Item -ItemType Directory -Path $d7 | Out-Null
        [IO.File]::WriteAllText((Join-Path $d7 'B5-9008.json'), '{"task":"B5-9008","agent_id":"Selftest Corrupt","scope":"model","note":"unclosed', (New-Object System.Text.UTF8Encoding($false)))
        Invoke-Report -Claims $d7 -Heartbeats (Join-Path $tmp 'nobody') -Reports $noreports -Ttl 30
        $r7 = $script:Report
        $ok7 = ($r7.corrupt.Count -eq 1)
        Write-Output ("[B5-2594] SELFTEST 7 corrupt claim blocks a clean report -> expect 1 corrupt record, got {0}: {1}" -f $r7.corrupt.Count, $(if ($ok7) { 'PASS' } else { 'FAIL' }))
        if (-not $ok7) { $failures++ }

        # CASE 8 -- a payload started_utc 13 h AHEAD, which is this host's offset
        # and the measured B5-1931/B5-1827 defect. It must be REPORTED, and must
        # not be used as the signal: the fixture's file mtime is fresh, so the
        # verdict stays LIVE on mtime and the defect is called out separately.
        $d8 = Join-Path $tmp 'clock'
        New-Item -ItemType Directory -Path $d8 | Out-Null
        $hb8 = Join-Path $tmp 'hb8'
        New-FixtureHeartbeat -Dir $hb8 -Name 'Eta.json' -Owner 'Selftest Eta'
        $future = [DateTime]::UtcNow.AddHours(13).ToString('yyyy-MM-ddTHH:mm:ssZ')
        $obj8 = [ordered]@{
            task = 'B5-9009'; agent_id = 'Selftest Eta'; started_utc = $future
            ttl_min = 30; scope = @('ai'); javac = '1.8.0_292'
        }
        [IO.File]::WriteAllText((Join-Path $d8 'B5-9009.json'), ($obj8 | ConvertTo-Json -Depth 4), (New-Object System.Text.UTF8Encoding($false)))
        Invoke-Report -Claims $d8 -Heartbeats $hb8 -Reports $noreports -Ttl 30
        $r8 = $script:Report
        $ok8 = ($r8.clockDefects.Count -eq 1 -and (Get-ScopeVerdict -Root 'ai' -R $r8).verdict -eq 'HELD')
        Write-Output ("[B5-2594] SELFTEST 8 future-dated started_utc flagged, mtime still governs -> expect 1 clock defect and ai HELD, got {0}/{1}: {2}" -f `
            $r8.clockDefects.Count, (Get-ScopeVerdict -Root 'ai' -R $r8).verdict, $(if ($ok8) { 'PASS' } else { 'FAIL' }))
        if (-not $ok8) { $failures++ }
        # CASE 9 -- THE INVERSION. A claim file 40 minutes old (well past the TTL)
        # whose owner heartbeat was written seconds ago is LIVE, because the rule
        # is the NEWEST of the three signals. The draft that previously occupied
        # this filename took the OLDEST and would have printed FREE here,
        # releasing a directory under a worker that was demonstrably still
        # running. This case is why the file exists; if it ever stops failing,
        # the whole reason the tool was rewritten has been undone.
        $d9 = Join-Path $tmp 'inversion'
        New-Item -ItemType Directory -Path $d9 | Out-Null
        $hb9 = Join-Path $tmp 'hb9'
        New-FixtureHeartbeat -Dir $hb9 -Name 'Theta.json' -Owner 'Selftest Theta'
        New-FixtureClaim -Dir $d9 -Name 'B5-9010.json' -Task 'B5-9010' -Owner 'Selftest Theta' -Scope @('engine/')
        Age-Fixture -Path (Join-Path $d9 'B5-9010.json') -Minutes 40
        Invoke-Report -Claims $d9 -Heartbeats $hb9 -Reports $noreports -Ttl 30
        $r9 = $script:Report
        $sv9 = Get-ScopeVerdict -Root 'engine' -R $r9
        $ok9 = ($sv9.verdict -eq 'HELD')
        Write-Output ("[B5-2594] SELFTEST 9 newest-of not oldest-of: 40-min claim file plus fresh heartbeat -> expect HELD, got {0}: {1}" -f `
            $sv9.verdict, $(if ($ok9) { 'PASS' } else { 'FAIL' }))
        if (-not $ok9) { $failures++ }

        # CASE 10 -- a claim path that cannot be read at all must read UNKNOWN,
        # never FREE and never STALE, and must be reported as MISSING rather than
        # folded into CORRUPT-JSON. The self-test caught this tool making exactly
        # that fold: a claim file that is gone and a claim file that is malformed
        # call for different repairs, and reporting the first as the second sends
        # the owner to edit a file that does not exist. The B5-0609 defect is this
        # call returning an age of -1 or 0 in place of a verdict.
        $direct = Get-ClaimVerdict -Path (Join-Path $tmp 'no-such-claim.json') -TtlMinutes 30 -HeartbeatIndex @{}
        $r10 = Get-ClaimVerdict -Path (Join-Path $d7 'B5-9008.json') -TtlMinutes 30 -HeartbeatIndex @{}
        $ok10 = ($direct.verdict -eq 'UNKNOWN' -and $direct.category -eq 'MISSING-FILE' -and
                 $r10.category -eq 'CORRUPT-JSON')
        Write-Output ("[B5-2594] SELFTEST 10 unreadable claim is UNKNOWN, and missing is distinct from corrupt -> got {0}/{1} for missing and {2} for malformed: {3}" -f `
            $direct.verdict, $direct.category, $r10.category, $(if ($ok10) { 'PASS' } else { 'FAIL' }))
        if (-not $ok10) { $failures++ }

        if ($failures -eq 0) {
            Write-Output '[B5-2594] SELFTEST PASS -- every fixture behaved as specified, so an exit 0 from a real run is evidence.'
            exit 0
        }
        Write-Output ("[B5-2594] SELFTEST FAIL -- {0} fixture(s) did not behave as specified." -f $failures)
        exit 1
    } finally {
        Remove-Item -Recurse -Force -LiteralPath $tmp -ErrorAction SilentlyContinue
    }
}
# -- main ------------------------------------------------------------------------
$repoRoot = Get-RepoRoot
if (-not $ClaimsDir)     { $ClaimsDir     = Join-Path $repoRoot '.agent/CLAIMS' }
if (-not $HeartbeatsDir) { $HeartbeatsDir = Join-Path $repoRoot '.agent/HEARTBEATS' }
if (-not $ReportsDir)    { $ReportsDir    = Join-Path $repoRoot '.agent/REPORTS' }
$script:TtlUsed = $TtlMinutes

if ($SelfTest) { Invoke-SelfTest }

if (-not (Test-Path -LiteralPath $ClaimsDir)) {
    Write-Output ("[B5-2594] MISSING claims directory: {0} -- absence is UNKNOWN, never a clean set of FREE scopes." -f $ClaimsDir)
    exit 2
}

Invoke-Report -Claims $ClaimsDir -Heartbeats $HeartbeatsDir -Reports $ReportsDir -Ttl $TtlMinutes
$result = $script:Report
Write-ReportText -R $result

if ($result.claimsRead -eq 0) {
    Write-Output '[B5-2594] no claim files found -- a report that read nothing must never report every scope FREE.'
    exit 2
}

if ($result.corrupt.Count -gt 0 -or $result.noIdentity.Count -gt 0) {
    Write-Output ("[B5-2594] FALSE-PASS GUARD -- {0} corrupt and {1} identity-less claim file(s) could not be read, so this run CANNOT certify that any scope is FREE. Exit 2 is a warning, not a pass." -f `
        $result.corrupt.Count, $result.noIdentity.Count)
    exit 2
}

$heldRoots = 0
foreach ($root in $OneWriterRoots) { if ($result.holders.ContainsKey($root)) { $heldRoots++ } }
Write-Output ("[B5-2594] COMPLETE -- {0} claim file(s) classified, {1} of {2} one-writer scope(s) currently held. This is a report, not a gate: it exits 0 on a two-holder scope by design, because scope-collision-census.ps1 owns that finding and owns the failing exit code." -f `
    $result.claimsRead, $heldRoots, @($OneWriterRoots).Count)
exit 0
