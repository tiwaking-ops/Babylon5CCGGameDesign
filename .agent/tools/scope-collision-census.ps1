#Requires -Version 3.0
<#
.SYNOPSIS
    B5-2311: report every pair of LIVE claim files whose scopes collide on a
    one-writer scope root, and exit non-zero when two writers collide.

.DESCRIPTION
    Why this instrument exists.

    AGENTS.md section 5 says "One writer per scope (engine/, model/, ai/, ui/)".
    Every census in this repo answers a DIFFERENT question. run-queue.ps1 -DryRun
    and ledger-query.ps1 mark a ROW `suppressed-live-claim`: they answer "is this
    task taken?". They say nothing about "is this SCOPE taken?", because a claim
    names a scope in free text and no tool reads two claims against each other.

    That gap has a measured cost. On 2026-10-03, B5-2281 claimed
    "b5ccg/src/b5ccg/engine/" at 02:24:30Z while claim B5-2249 held scope "engine"
    from 02:17:46Z. Same directory, two spellings, seven minutes of two writers on
    one scope. The row census showed B5-2249 as suppressed-live-claim, which is
    true and useless: the row being claimed was a different row. A naive string
    comparison would ALSO have missed it, because "engine" is a substring of the
    longer string but not equal to it.

    So the rule this tool applies is deliberately not string equality. It maps
    each claim's free-text scope onto the SET OF ONE-WRITER ROOTS it touches, by
    recognising `engine`, `model`, `ai` and `ui` as path segments or whole words
    anywhere in the string. "b5ccg/src/b5ccg/engine/", "engine", and
    "b5ccg/src/b5ccg/engine/ Headless plus Test files only" therefore all map to
    {engine}, and collide.

    Two writers on one root is the violation and exits 1. A writer and a reader on
    one root is ADVISORY and does not fail: a read-only claim fences nothing, and
    this repo already leans on that distinction in row scopes ("engine read-only",
    "fences ... read-only"). Reporting those without failing keeps the exit code
    meaning exactly one thing.

.LIVENESS
    Three signals, never one, per .agent/HEARTBEATS/README.md: the claim file's
    own mtime, the owner's heartbeat mtime, and the newest report matching the
    task id. A claim is LIVE while the NEWEST of the three is inside the TTL.
    STALE means at least one signal was found and none is inside the TTL.
    UNKNOWN means no signal was found at all, and an absent signal is never LIVE
    and never evidence of a collision.

.PARAMETER ClaimsDir      Defaults to <repo>/.agent/CLAIMS
.PARAMETER HeartbeatsDir  Defaults to <repo>/.agent/HEARTBEATS
.PARAMETER ReportsDir     Defaults to <repo>/.agent/REPORTS
.PARAMETER TtlMinutes     Claim TTL, default 30 (matches new-claim.ps1)
.PARAMETER SelfTest       Build colliding and clean fixtures in a temp directory
                          and assert this tool reports red on the first and green
                          on the second. A gate never observed red is not a gate
                          (AGENT_LOOP.md, "The three ways this loop has lied to
                          itself", item 3), so run this before trusting an exit 0.

.OUTPUTS
    Exit 0 = no two live claims write the same one-writer scope root.
    Exit 1 = at least one such pair (writer/writer), listed with both claim paths.
    Exit 2 = the claims directory is missing or empty, or a source is unreadable.
             2 is a FALSE PASS and must never be read as clean.
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

# AGENTS.md section 5, verbatim: one writer per scope.
$OneWriterRoots = @('engine', 'model', 'ai', 'ui')

# A scope string that says it only reads is a reader. These are the phrasings this
# repo's own claim files use; an unrecognised scope is treated as a WRITER, which
# is the conservative direction: it can only ever report more, never less.
$ReadOnlyMarkers = @(
    'read-only', 'read only', 'readonly',
    'no src edits', 'no edits', 'ledger read-only', 'no harness edits'
)

function Get-RepoRoot {
    return (Split-Path -Parent (Split-Path -Parent $PSScriptRoot))
}

function Get-ClaimField {
    <#
      Read a property that may be absent, WITHOUT tripping Set-StrictMode.

      This helper exists because of a measured self-inflicted defect in the first
      draft of this tool. A single try/catch wrapped BOTH ConvertFrom-Json AND the
      property reads, so StrictMode's "property not found" on a perfectly valid
      claim file was reported as "UNPARSEABLE". The first real run silently skipped
      three claims, one of which (B5-1803) is named in AGENTS.md section 5 as a live
      claim with an invented timestamp. A census that quietly drops files it
      misreads is worse than no census: it returns a green exit code it did not earn.

      Returning $null for an absent field is what makes "absent" distinguishable
      from "corrupt", which is the distinction this whole tool rests on.
    #>
    param($Obj, [string[]] $Names)
    foreach ($n in $Names) {
        $p = $Obj.PSObject.Properties[$n]
        if ($null -ne $p -and $null -ne $p.Value) { return $p.Value }
    }
    return $null
}

function Get-ClaimVerdict {
    <#
      Classify one claim file. Three outcomes matter and they are NOT
      interchangeable:

        LIVE     identity established, no terminal status, a signal inside the TTL.
                  Only LIVE claims can collide.
        RELEASED identity established but status is terminal (BLOCKED/RELEASED/
                  DONE/CLOSED). Per .agent/AGENT_LOOP.md a red gate means
                  "BLOCKED, release, stop item only", and the claim FILE can
                  survive that release. new-claim.ps1 refuses a row while any
                  file with that task id exists, so released residue is normal --
                  and counting it as a live writer would make this tool
                  permanently red on every previously-blocked row. Reported, not
                  failed on, and never a writer.
        UNKNOWN  identity could not be established at all: the JSON is corrupt,
                  or it carries neither task nor agent_id. Absence is never LIVE.

      Liveness deliberately uses the claim file's MTIME and NOT the `started_utc`
      in the payload. AGENTS.md section 5 (B5-1931) records that a hand-typed
      started_utc is an unread measurement, and that on this UTC+13 host stamping
      local time puts a claim ~13 h AHEAD, which makes its age negative and its
      row permanently unreclaimable. A payload stamp is a claim about the clock; a
      file mtime is a measurement of the clock. Only the latter is a liveness signal.
    #>
    param(
        [string]   $Path,
        [int]      $TtlMinutes,
        [hashtable] $HeartbeatIndex   # normalised owner key -> newest mtime
    )

    $now = [DateTime]::UtcNow
    $signals = @()

    $claimMtime = $null
    try { $claimMtime = (Get-Item -LiteralPath $Path).LastWriteTimeUtc }
    catch { $claimMtime = $null }
    if ($claimMtime -ne $null) {
        $signals += [pscustomobject]@{ name = 'claim'; mtime = $claimMtime }
    }

    # (1) JSON integrity, isolated. Only genuine corruption lands here.
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
        # be PROVABLY harmless. If its raw text carries a terminal status, its owner
        # already let go, so it can never be a live writer no matter what the rest
        # of the file says. That distinction is load-bearing, and it was measured:
        # .agent/CLAIMS/B5-2002.json is genuinely corrupt (its assessor_llm element
        # object is never closed, so the final '}' closes the inner object and the
        # top-level object is left open -- confirmed against a second, independent
        # JSON reader), and a naive guard treats it as blocking forever. Its status
        # is BLOCKED and its scope is engine/CardEffects.java, so it could never
        # collide with a live writer anyway. A gate that stays red forever on
        # unrelated residue is a gate nobody runs, which is the same defect this
        # repo keeps recording against gates that cannot be executed.
        #
        # Anything unreadable WITHOUT a terminal status still drives exit 2. Only a
        # provably released file is allowed to be skipped.
        $rawProbe = ''
        try { $rawProbe = [IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8) } catch { $rawProbe = '' }
        $terminal = [regex]::Match($rawProbe, '"(?:status|state)"\s*:\s*"(blocked|released|done|closed|complete)"', 'IgnoreCase')
        if ($terminal.Success) {
            return @{
                verdict = 'RELEASED'; category = 'TERMINAL-STATUS-UNPARSED'
                task = $null; owner = $null; scope = $null
                status = $terminal.Groups[1].Value.ToLowerInvariant()
                parseError = $parseError; ageMin = $null; signals = @()
            }
        }
        return @{
            verdict = 'UNKNOWN'; category = 'CORRUPT-JSON'
            task = $null; owner = $null; scope = $null; status = $null
            parseError = $parseError; ageMin = $null; signals = @()
        }
    }

    # (2) Identity. Two schemas are in live use in .agent/CLAIMS: new-claim.ps1
    # writes "task", hand-authored and BLOCKED-release files write "task_id".
    # Reading only one of them is what the first draft got wrong.
    $task  = Get-ClaimField -Obj $obj -Names @('task', 'task_id', 'taskId')
    $owner = Get-ClaimField -Obj $obj -Names @('agent_id', 'agentId', 'owner')
    $status = Get-ClaimField -Obj $obj -Names @('status', 'state')
    $scopeField = Get-ClaimField -Obj $obj -Names @('scope', 'scopes')

    $task  = if ($null -eq $task)  { $null } else { [string]$task }
    $owner = if ($null -eq $owner) { $null } else { [string]$owner }
    $status = if ($null -eq $status) { '' } else { ([string]$status).ToLowerInvariant() }

    # Claim files in this repo carry `scope` as either a bare string or an ARRAY of
    # strings. Joining is what lets one claim naming four directories collide with
    # four separate single-directory claims.
    $scopeRaw = $null
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
            ageMin = $null; signals = @()
        }
    }

    # (3) Terminal status means the owner already let go. Reported, never a writer.
    if ($status -in @('blocked', 'released', 'done', 'closed', 'complete')) {
        return @{
            verdict = 'RELEASED'; category = 'TERMINAL-STATUS'
            task = $task; owner = $owner; scope = $scopeRaw; status = $status
            parseError = $null; ageMin = $null; signals = @()
        }
    }

    if ($owner) {
        $key = Get-NormKey $owner
        if ($HeartbeatIndex.ContainsKey($key)) {
            $signals += [pscustomobject]@{
                name  = 'heartbeat'
                mtime = $HeartbeatIndex[$key]
            }
        }
    }

    $reportAge = $null
    if ($script:ReportsDir -and (Test-Path -LiteralPath $script:ReportsDir)) {
        $newest = $null
        foreach ($f in @(Get-ChildItem -LiteralPath $script:ReportsDir -Filter "*$task*.md" -ErrorAction SilentlyContinue)) {
            if ($null -eq $newest -or $f.LastWriteTimeUtc -gt $newest) {
                $newest = $f.LastWriteTimeUtc
            }
        }
        if ($newest -ne $null) {
            $reportAge = ($now - $newest).TotalMinutes
            $signals += [pscustomobject]@{ name = 'report'; mtime = $newest }
        }
    }

    if ($signals.Count -eq 0) {
        return @{
            verdict = 'UNKNOWN'; category = 'NO-SIGNAL'
            task = $task; owner = $owner; scope = $scopeRaw; status = $status
            parseError = $null; ageMin = $null; signals = @()
        }
    }

    $newest = $signals | Sort-Object -Property mtime -Descending | Select-Object -First 1
    $ageMin = ($now - $newest.mtime).TotalMinutes
    $verdict = if ($ageMin -le $TtlMinutes) { 'LIVE' } else { 'STALE' }

    return @{
        verdict = $verdict; category = $verdict
        task = $task; owner = $owner; scope = $scopeRaw; status = $status
        parseError = $null; ageMin = $ageMin
        signals = @($signals | ForEach-Object { $_.name })
    }
}

function Get-NormKey {
    <#
      Normalised identity key, matching the Get-NormName convention used by
      run-queue.ps1 and ledger-query.ps1: letters and digits only, so
      "... free 2" and "... free2" cannot be two owners.
    #>
    param([string] $Name)
    if (-not $Name) { return '' }
    return ([regex]::Replace($Name.ToLowerInvariant(), '[^a-z0-9]', ''))
}

function Get-ScopeRoots {
    <#
      The set of one-writer roots a free-text scope string touches.

      Recognition is per-token, not substring: a root counts when it appears as a
      whole word or as a path segment, so "engine" matches "engine",
      "b5ccg/src/b5ccg/engine/" and "engine/ Headless plus Test files only", and
      does NOT match "engineer" or "ai" inside "maintain". Splitting on every
      non-alphanumeric character and comparing tokens is what makes the two
      spellings of the same directory collide, which is the whole defect class.

      Returns the roots as a single pipe-delimited STRING, never a collection.
      That is deliberate and it is the second collection bug this tool hit. An
      empty collection emits nothing through the PowerShell pipeline, so the
      caller holds $null and `.Count` throws under Set-StrictMode; returning
      `,$array` fixes that but then double-wraps when the call site also wraps
      with @(), producing roots [System.Object[]] whose single element is an
      array -- and `-contains 'engine'` then silently returns $false over a REAL
      collision. A delimited string survives both cases: '' for none, 'engine'
      for one, 'engine|model' for several. The caller splits it.
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
    param([string] $Scope)
    if (-not $Scope) { return $false }
    $lowered = $Scope.ToLowerInvariant()
    foreach ($marker in $ReadOnlyMarkers) {
        if ($lowered.Contains($marker)) { return $true }
    }
    return $false
}

function Get-HeartbeatIndex {
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
        if (-not $owner) { continue }          # a filename is not an identity
        $key = Get-NormKey $owner
        if (-not $index.ContainsKey($key) -or $f.LastWriteTimeUtc -gt $index[$key]) {
            $index[$key] = $f.LastWriteTimeUtc
        }
    }
    return $index
}

function Invoke-Census {
    <#
      The census itself. Fills $script:CensusResult rather than RETURNING it.

      Three separate bugs in this file all came from returning collections across
      a PowerShell function boundary: an empty collection arriving as $null, a
      comma-wrapped array double-wrapping into [System.Object[]], and a hashtable
      parameter failing to bind a [PSCustomObject]. Each produced a wrong or
      missing result while looking like it worked, and the third one aborted the
      run on a released-claim line. The fix is to stop returning collections at
      all: one script-scope result object, read in place. Nothing else in this
      script crosses a function boundary with a collection.
    #>
    param([string] $Claims, [string] $Heartbeats, [string] $Reports, [int] $Ttl)

    $script:ReportsDir = $Reports
    $result = @{
        claimsRead   = 0
        corrupt      = @()
        releasedUnparsed = @()
        noIdentity   = @()
        released     = @()
        noScope      = @()
        live         = @()
        writeWrite   = @()
        writeRead    = @()
    }

    if (-not $Claims -or -not (Test-Path -LiteralPath $Claims)) { $script:CensusResult = $result; return }
    $files = @(Get-ChildItem -LiteralPath $Claims -Filter '*.json' -File -ErrorAction SilentlyContinue)
    if ($files.Count -eq 0) { $script:CensusResult = $result; return }

    $hbIndex = Get-HeartbeatIndex -Dir $Heartbeats

    foreach ($f in $files) {
        $result.claimsRead++
        $v = Get-ClaimVerdict -Path $f.FullName -TtlMinutes $Ttl -HeartbeatIndex $hbIndex

        switch ($v.category) {
            'CORRUPT-JSON' {
                # The tool could not read this claim at all, so it cannot certify
                # that no collision exists. This is a FALSE PASS risk and drives
                # exit 2; it is never silently skipped and never failed on as a
                # collision.
                $result.corrupt += [pscustomobject]@{
                    path = $f.Name; error = $v.parseError
                }
                continue
            }
            'NO-IDENTITY' {
                $result.noIdentity += [pscustomobject]@{
                    path = $f.Name; error = $v.parseError
                }
                continue
            }
            'TERMINAL-STATUS' {
                $result.released += [pscustomobject]@{
                    path = $f.Name; task = $v.task; owner = $v.owner
                    status = $v.status; scope = $v.scope
                }
                continue
            }
            'TERMINAL-STATUS-UNPARSED' {
                # Unreadable, but provably released, so it cannot be a writer.
                # Reported loudly because the file still needs repair by its owner.
                $result.releasedUnparsed += [pscustomobject]@{
                    path = $f.Name; status = $v.status; error = $v.parseError
                }
                continue
            }
            'NO-SIGNAL' {
                $result.noIdentity += [pscustomobject]@{
                    path = $f.Name; error = 'identity present but no liveness signal found'
                }
                continue
            }
        }

        if ($v.verdict -ne 'LIVE') { continue }   # STALE: not a writer

        # Split the delimited string, dropping the empty case so `.Count` is honest.
        # -split on '' yields a one-element array holding '', so the Where-Object
        # filter is load-bearing, not decoration.
        $roots = @((Get-ScopeRoots -Scope $v.scope) -split '\|' | Where-Object { $_ -ne '' })
        if ($roots.Count -eq 0) {
            # A live claim that names no one-writer root (docs, harness, .agent)
            # cannot collide under this rule. Listed so its exclusion is auditable
            # rather than invisible.
            $result.noScope += [pscustomobject]@{
                path = $f.Name; task = $v.task; owner = $v.owner; scope = $v.scope
            }
            continue
        }

        $entry = [pscustomobject]@{
            path   = $f.Name
            task   = $v.task
            owner  = $v.owner
            ageMin = [math]::Round($v.ageMin, 1)
            roots  = @($roots)
            writes = (-not (Test-ReadOnlyScope -Scope $v.scope))
            scope  = $v.scope
        }
        $result.live += $entry
    }

    for ($i = 0; $i -lt $result.live.Count; $i++) {
        for ($j = $i + 1; $j -lt $result.live.Count; $j++) {
            $a = $result.live[$i]; $b = $result.live[$j]
            $shared = @($a.roots | Where-Object { $b.roots -contains $_ })
            if ($shared.Count -eq 0) { continue }
            if ($a.writes -and $b.writes) {
                $result.writeWrite += [pscustomobject]@{
                    roots = $shared
                    a = ("{0} ({1}, {2})" -f $a.path, $a.owner, $a.task)
                    b = ("{0} ({1}, {2})" -f $b.path, $b.owner, $b.task)
                }
            } elseif ($a.writes -or $b.writes) {
                $result.writeRead += [pscustomobject]@{
                    roots = $shared
                    writer = $(if ($a.writes) { "{0} ({1})" -f $a.path, $a.owner } else { "{0} ({1})" -f $b.path, $b.owner })
                    reader = $(if ($a.writes) { "{0} ({1})" -f $b.path, $b.owner } else { "{0} ({1})" -f $a.path, $a.owner })
                }
            }
        }
    }
    $script:CensusResult = $result
}

function Write-CensusReport {
    # Reads $script:CensusResult in place. No parameter, no collection crossing a
    # function boundary -- see the note on Invoke-Census.
    #
    # The result variable is named $res, NOT $R, and that is load-bearing.
    # PowerShell variable names are CASE-INSENSITIVE, so `foreach ($r in $R.x)`
    # makes $r and $R THE SAME VARIABLE: the loop silently overwrites the result
    # table with the last element it visited, and the next $R.corrupt throws
    # "property not found". That is exactly the failure this function hit. $res
    # cannot collide with any of the loop variables below ($l, $n, $rr, $cc, $ad).
    $res = $script:CensusResult

    Write-Output ("[B5-2311] claim files read : {0}" -f $res.claimsRead)
    Write-Output ("[B5-2311] live claims      : {0}" -f $res.live.Count)
    Write-Output ("[B5-2311] TTL              : {0} min (three-signal: claim, owner heartbeat, report)" -f $script:TtlUsed)

    foreach ($l in $res.live) {
        Write-Output ("[B5-2311] LIVE  {0,-10} owner {1,-42} age {2,6} min  roots [{3}]  {4}  scope: {5}" -f `
            $l.task, $l.owner, $l.ageMin, ($l.roots -join ','),
            $(if ($l.writes) { 'WRITER' } else { 'reader ' }), $l.scope)
    }
    foreach ($n in $res.noScope) {
        Write-Output ("[B5-2311] LIVE  {0,-10} owner {1,-42} names no one-writer root, cannot collide: {2}" -f $n.task, $n.owner, $n.scope)
    }
    foreach ($rr in $res.released) {
        Write-Output ("[B5-2311] RELEASED {0,-10} owner {1,-42} status {2} -- not a writer, file residue from a released claim: {3}" -f $rr.task, $rr.owner, $rr.status, $rr.scope)
    }
    foreach ($rr in $res.releasedUnparsed) {
        Write-Output ("[B5-2311] RELEASED-UNPARSED {0} -- corrupt JSON, but its raw text carries status {1}, so it cannot be a live writer. Its owner should still repair it: {2}" -f $rr.path, $rr.status, $rr.error)
    }
    foreach ($cc in $res.corrupt) {
        Write-Output ("[B5-2311] CORRUPT CLAIM {0} -- {1}" -f $cc.path, $cc.error)
        Write-Output "[B5-2311]   unreadable: this tool CANNOT certify the census while it stands. Drives exit 2 (a false pass), never a collision."
    }
    foreach ($n in $res.noIdentity) {
        Write-Output ("[B5-2311] NO-IDENTITY {0} -- {1}" -f $n.path, $n.error)
        Write-Output "[B5-2311]   an absent signal is never live and never evidence of a collision. Drives exit 2."
    }

    foreach ($ad in $res.writeRead) {
        Write-Output ("[B5-2311] ADVISORY writer/reader on [{0}]: {1} writes, {2} reads. Not a violation -- a read-only claim fences nothing." -f `
            ($ad.roots -join ','), $ad.writer, $ad.reader)
    }
    foreach ($cc in $res.writeWrite) {
        Write-Output ("[B5-2311] COLLISION two live WRITERS on [{0}]:" -f ($cc.roots -join ','))
        Write-Output ("[B5-2311]   A {0}" -f $cc.a)
        Write-Output ("[B5-2311]   B {0}" -f $cc.b)
        Write-Output "[B5-2311]   AGENTS.md section 5: one writer per scope. Leave both rows byte-identical and let each owner act; do NOT reap."
    }
}

# ── self-test ────────────────────────────────────────────────────────────────────
function Invoke-SelfTest {
    $script:TtlUsed = 30
    $tmp = Join-Path ([IO.Path]::GetTempPath()) ("b52311-selftest-" + [Guid]::NewGuid().ToString('N').Substring(0, 8))
    New-Item -ItemType Directory -Path $tmp | Out-Null
    $failures = 0
    try {
        function New-Fixture {
            param([string] $Dir, [string] $Name, [string] $Task, [string] $Owner, [string[]] $Scope)
            $p = Join-Path $Dir $Name
            $obj = [ordered]@{
                task = $Task; agent_id = $Owner
                started_utc = [DateTime]::UtcNow.ToString('yyyy-MM-ddTHH:mm:ssZ')
                ttl_min = 30; scope = $Scope; javac = '1.8.0_292'
            }
            [IO.File]::WriteAllText($p, ($obj | ConvertTo-Json -Depth 4), (New-Object System.Text.UTF8Encoding($false)))
        }

        # FIXTURE 1 -- two live WRITERS on `engine`, spelled two different ways.
        # This is the exact B5-2281 shape: "b5ccg/src/b5ccg/engine/" versus "engine".
        $d1 = Join-Path $tmp 'collide'
        New-Item -ItemType Directory -Path $d1 | Out-Null
        New-Fixture -Dir $d1 -Name 'B5-9001.json' -Task 'B5-9001' -Owner 'Selftest Alpha' -Scope @('b5ccg/src/b5ccg/engine/')
        New-Fixture -Dir $d1 -Name 'B5-9002.json' -Task 'B5-9002' -Owner 'Selftest Beta'  -Scope @('engine')
        Invoke-Census -Claims $d1 -Heartbeats (Join-Path $tmp 'nobody') -Reports (Join-Path $tmp 'noreports') -Ttl 30
        $r1 = $script:CensusResult
        $ok1 = ($r1.writeWrite.Count -eq 1)
        Write-Output ("[B5-2311] SELFTEST 1 two live writers on engine, spelled differently -> expect 1 collision, got {0}: {1}" -f $r1.writeWrite.Count, $(if ($ok1) { 'PASS' } else { 'FAIL' }))
        if (-not $ok1) { $failures++ }

        # FIXTURE 2 -- two live WRITERS on DISJOINT roots must NOT collide.
        $d2 = Join-Path $tmp 'disjoint'
        New-Item -ItemType Directory -Path $d2 | Out-Null
        New-Fixture -Dir $d2 -Name 'B5-9003.json' -Task 'B5-9003' -Owner 'Selftest Gamma' -Scope @('b5ccg/src/b5ccg/ui/')
        New-Fixture -Dir $d2 -Name 'B5-9004.json' -Task 'B5-9004' -Owner 'Selftest Delta' -Scope @('engine')
        Invoke-Census -Claims $d2 -Heartbeats (Join-Path $tmp 'nobody') -Reports (Join-Path $tmp 'noreports') -Ttl 30
        $r2 = $script:CensusResult
        $ok2 = ($r2.writeWrite.Count -eq 0)
        Write-Output ("[B5-2311] SELFTEST 2 disjoint roots -> expect 0 collisions, got {0}: {1}" -f $r2.writeWrite.Count, $(if ($ok2) { 'PASS' } else { 'FAIL' }))
        if (-not $ok2) { $failures++ }

        # FIXTURE 3 -- a WRITER against a READ-ONLY claim is ADVISORY, not a
        # collision. Without this, the exit code would fire on the read-only
        # fencing this repo's own row scopes rely on.
        $d3 = Join-Path $tmp 'writerreader'
        New-Item -ItemType Directory -Path $d3 | Out-Null
        New-Fixture -Dir $d3 -Name 'B5-9005.json' -Task 'B5-9005' -Owner 'Selftest Writer' -Scope @('engine')
        New-Fixture -Dir $d3 -Name 'B5-9006.json' -Task 'B5-9006' -Owner 'Selftest Reader' -Scope @('engine plus model read-only, no src edits')
        Invoke-Census -Claims $d3 -Heartbeats (Join-Path $tmp 'nobody') -Reports (Join-Path $tmp 'noreports') -Ttl 30
        $r3 = $script:CensusResult
        $ok3 = ($r3.writeWrite.Count -eq 0 -and $r3.writeRead.Count -eq 1)
        Write-Output ("[B5-2311] SELFTEST 3 writer vs reader on engine -> expect 0 collisions and 1 advisory, got {0}/{1}: {2}" -f $r3.writeWrite.Count, $r3.writeRead.Count, $(if ($ok3) { 'PASS' } else { 'FAIL' }))
        if (-not $ok3) { $failures++ }

        # FIXTURE 4 -- substring discipline: "engineer" and "maintain" must not
        # be read as the `engine` root, and an unreadable claim must not fail.
        $d4 = Join-Path $tmp 'substring'
        New-Item -ItemType Directory -Path $d4 | Out-Null
        New-Fixture -Dir $d4 -Name 'B5-9007.json' -Task 'B5-9007' -Owner 'Selftest Epsilon' -Scope @('engine')
        New-Fixture -Dir $d4 -Name 'B5-9008.json' -Task 'B5-9008' -Owner 'Selftest Zeta'    -Scope @('maintain the engineer docs')
        [IO.File]::WriteAllText((Join-Path $d4 'B5-9009.json'), '{ not json', (New-Object System.Text.UTF8Encoding($false)))
        Invoke-Census -Claims $d4 -Heartbeats (Join-Path $tmp 'nobody') -Reports (Join-Path $tmp 'noreports') -Ttl 30
        $r4 = $script:CensusResult
        $ok4 = ($r4.writeWrite.Count -eq 0 -and $r4.corrupt.Count -eq 1)
        Write-Output ("[B5-2311] SELFTEST 4 substring discipline plus a corrupt claim -> expect 0 collisions and 1 corrupt, got {0}/{1}: {2}" -f $r4.writeWrite.Count, $r4.corrupt.Count, $(if ($ok4) { 'PASS' } else { 'FAIL' }))
        if (-not $ok4) { $failures++ }

        # FIXTURE 5 -- an EMPTY claims directory is exit 2, never a clean pass.
        $d5 = Join-Path $tmp 'empty'
        New-Item -ItemType Directory -Path $d5 | Out-Null
        Invoke-Census -Claims $d5 -Heartbeats (Join-Path $tmp 'nobody') -Reports (Join-Path $tmp 'noreports') -Ttl 30
        $r5 = $script:CensusResult
        $ok5 = ($r5.claimsRead -eq 0)
        Write-Output ("[B5-2311] SELFTEST 5 empty claims dir -> expect 0 claims read (caller exits 2, a false pass), got {0}: {1}" -f $r5.claimsRead, $(if ($ok5) { 'PASS' } else { 'FAIL' }))
        if (-not $ok5) { $failures++ }

        # FIXTURE 6 -- REGRESSION for the defect the first real run exposed. Two
        # live WRITERS on engine, one spelled with "task" and one with "task_id".
        # A reader that only knows "task" drops the second file and returns a green
        # exit code over a real collision. This fixture must be RED.
        $d6 = Join-Path $tmp 'taskschema'
        New-Item -ItemType Directory -Path $d6 | Out-Null
        New-Fixture -Dir $d6 -Name 'B5-9010.json' -Task 'B5-9010' -Owner 'Selftest Task'  -Scope @('b5ccg/src/b5ccg/engine/')
        $legacy = [ordered]@{
            task_id = 'B5-9011'; agent_id = 'Selftest TaskId'
            started_utc = [DateTime]::UtcNow.ToString('yyyy-MM-ddTHH:mm:ssZ')
            status = 'claimed'; scope = 'engine'
        }
        [IO.File]::WriteAllText((Join-Path $d6 'B5-9011.json'), ($legacy | ConvertTo-Json -Depth 4), (New-Object System.Text.UTF8Encoding($false)))
        Invoke-Census -Claims $d6 -Heartbeats (Join-Path $tmp 'nobody') -Reports (Join-Path $tmp 'noreports') -Ttl 30
        $r6 = $script:CensusResult
        $ok6 = ($r6.writeWrite.Count -eq 1)
        Write-Output ("[B5-2311] SELFTEST 6 REGRESSION task vs task_id schema -> expect 1 collision, got {0}: {1}" -f $r6.writeWrite.Count, $(if ($ok6) { 'PASS' } else { 'FAIL' }))
        if (-not $ok6) { $failures++ }

        # FIXTURE 7 -- released residue is NOT a writer. A red gate leaves a
        # BLOCKED claim file behind and new-claim.ps1 still refuses that row, so
        # counting residue as live would make this tool permanently red on every
        # previously-blocked row.
        $d7 = Join-Path $tmp 'released'
        New-Item -ItemType Directory -Path $d7 | Out-Null
        New-Fixture -Dir $d7 -Name 'B5-9012.json' -Task 'B5-9012' -Owner 'Selftest Writer' -Scope @('engine')
        $blocked = [ordered]@{
            task_id = 'B5-9013'; agent_id = 'Selftest Blocked'
            started_utc = [DateTime]::UtcNow.ToString('yyyy-MM-ddTHH:mm:ssZ')
            status = 'BLOCKED'; scope = 'engine'
        }
        [IO.File]::WriteAllText((Join-Path $d7 'B5-9013.json'), ($blocked | ConvertTo-Json -Depth 4), (New-Object System.Text.UTF8Encoding($false)))
        Invoke-Census -Claims $d7 -Heartbeats (Join-Path $tmp 'nobody') -Reports (Join-Path $tmp 'noreports') -Ttl 30
        $r7 = $script:CensusResult
        $ok7 = ($r7.writeWrite.Count -eq 0 -and $r7.released.Count -eq 1)
        Write-Output ("[B5-2311] SELFTEST 7 BLOCKED residue vs a live writer on engine -> expect 0 collisions and 1 released, got {0}/{1}: {2}" -f $r7.writeWrite.Count, $r7.released.Count, $(if ($ok7) { 'PASS' } else { 'FAIL' }))
        if (-not $ok7) { $failures++ }

        # FIXTURE 8 -- the unparsed-terminal fallback, both branches. This is the
        # B5-2002 case: corrupt JSON whose raw text still says status BLOCKED must
        # be released (and must NOT block certification), while corrupt JSON with no
        # terminal status must stay corrupt and keep driving exit 2. Without the
        # first branch this tool is red forever on unrelated residue; without the
        # second it would certify a census it could not actually read.
        $d8 = Join-Path $tmp 'unparsedterminal'
        New-Item -ItemType Directory -Path $d8 | Out-Null
        [IO.File]::WriteAllText((Join-Path $d8 'B5-9014.json'), '{"task_id":"B5-9014","status":"BLOCKED","scope":"engine","note":"unclosed', (New-Object System.Text.UTF8Encoding($false)))
        [IO.File]::WriteAllText((Join-Path $d8 'B5-9015.json'), '{"task_id":"B5-9015","agent_id":"Selftest Unknown","scope":"engine","note":"unclosed', (New-Object System.Text.UTF8Encoding($false)))
        Invoke-Census -Claims $d8 -Heartbeats (Join-Path $tmp 'nobody') -Reports (Join-Path $tmp 'noreports') -Ttl 30
        $r8 = $script:CensusResult
        $ok8 = ($r8.releasedUnparsed.Count -eq 1 -and $r8.corrupt.Count -eq 1 -and $r8.writeWrite.Count -eq 0)
        Write-Output ("[B5-2311] SELFTEST 8 unparsed terminal-status fallback -> expect 1 released-unparsed, 1 corrupt, 0 collisions, got {0}/{1}/{2}: {3}" -f $r8.releasedUnparsed.Count, $r8.corrupt.Count, $r8.writeWrite.Count, $(if ($ok8) { 'PASS' } else { 'FAIL' }))
        if (-not $ok8) { $failures++ }

        if ($failures -eq 0) {
            Write-Output '[B5-2311] SELFTEST PASS -- the tool was observed RED on fixtures 1 and green on 2-5, so an exit 0 from a real run is evidence.'
            Write-Output '-- ENCODING: UTF-8 (explicit) --'
            exit 0
        }
        Write-Output ("[B5-2311] SELFTEST FAIL -- {0} fixture(s) did not behave as specified." -f $failures)
        Write-Output '-- ENCODING: UTF-8 (explicit) --'
        exit 1
    } finally {
        Remove-Item -Recurse -Force -LiteralPath $tmp -ErrorAction SilentlyContinue
    }
}

# ── main ─────────────────────────────────────────────────────────────────────────
$repoRoot = Get-RepoRoot
if (-not $ClaimsDir)      { $ClaimsDir      = Join-Path $repoRoot '.agent/CLAIMS' }
if (-not $HeartbeatsDir)  { $HeartbeatsDir  = Join-Path $repoRoot '.agent/HEARTBEATS' }
if (-not $ReportsDir)     { $ReportsDir     = Join-Path $repoRoot '.agent/REPORTS' }
$script:TtlUsed = $TtlMinutes

if ($SelfTest) { Invoke-SelfTest }

if (-not (Test-Path -LiteralPath $ClaimsDir)) {
    Write-Output ("[B5-2311] MISSING claims directory: {0} -- absence is UNKNOWN, never a clean pass." -f $ClaimsDir)
    Write-Output '-- ENCODING: UTF-8 (explicit) --'
    exit 2
}

Invoke-Census -Claims $ClaimsDir -Heartbeats $HeartbeatsDir -Reports $ReportsDir -Ttl $TtlMinutes
$result = $script:CensusResult
Write-CensusReport -R $result

if ($result.claimsRead -eq 0) {
    Write-Output '[B5-2311] no claim files found -- a census that read nothing must never report as clean.'
    Write-Output '-- ENCODING: UTF-8 (explicit) --'
    exit 2
}

if ($result.corrupt.Count -gt 0 -or $result.noIdentity.Count -gt 0) {
    Write-Output ("[B5-2311] FALSE-PASS GUARD -- {0} corrupt and {1} identity-less claim file(s) could not be read, so this run CANNOT certify that no live writer pair exists. Exit 2 is a warning, not a pass." -f $result.corrupt.Count, $result.noIdentity.Count)
    Write-Output '-- ENCODING: UTF-8 (explicit) --'
    exit 2
}

if ($result.writeWrite.Count -gt 0) {
    Write-Output ("[B5-2311] FAIL -- {0} colliding writer pair(s) on a one-writer scope." -f $result.writeWrite.Count)
    Write-Output '-- ENCODING: UTF-8 (explicit) --'
    exit 1
}

Write-Output '[B5-2311] PASS -- no two live claims write the same one-writer scope.'
Write-Output '-- ENCODING: UTF-8 (explicit) --'
exit 0
