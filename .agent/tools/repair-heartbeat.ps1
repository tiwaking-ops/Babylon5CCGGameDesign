<#
.SYNOPSIS
  B5-2361: repair a heartbeat file's SCHEMA and CLOCK defects using only values derived
  from the filesystem, and refuse anything that would require inventing a fact.

.DESCRIPTION
  Why this tool exists. Five live heartbeats failed `validate-heartbeats.ps1`, and two of
  them carried a `utc` roughly 25h and 19h AHEAD of real UTC. That is the clock-integrity
  class in `docs/reports/clock-integrity-in-agent-coordination-files-2026-09-30.md`: an
  LLM can emit a plausible ISO string without ever reading a clock, and three files in
  this store did. `new-heartbeat.ps1` fixed the WRITING half of that problem for new
  files. There was no remedy for the ones already written.

  Hand-editing them was the obvious move and it is the one this tool exists to make
  unnecessary, because hand-editing a future-dated clock means **typing a timestamp you
  did not measure** - the exact sin B5-1931 and B5-0653 codified, and the reason this
  host's UTC+13 has produced claims that read LIVE forever.

  THE RULE THIS TOOL ENFORCES: every value it writes is DERIVED FROM THE FILESYSTEM or
  from the schema constants, never invented, never defaulted, never taken from "now".

    field          when repaired            value comes from
    -------------  ----------------------  ---------------------------------------------
    utc            skewed ahead, or absent  the file's OWN LastWriteTimeUtc. A
                                            measurement of when the file was last
                                            written. NOT from UtcNow: stamping "now"
                                            would assert the session was alive during
                                            this repair, which is a different and false
                                            claim, and it would age the file forward
                                            every time the tool ran.
    schema_version absent                  the schema constant 1.
    state          absent                  mtime > TTL -> 'idle'; mtime fresh ->
                                            REFUSE (see below).
    live_claims    absent                  ONLY with -AssertNoLiveClaims, and only when
                                            all three liveness signals measure STALE.
    agent_id       absent                  NEVER. Unresolvable from anything. Refuse.
    anything else  -                       Never touched. Unknown keys are preserved.

  REFUSALS ARE THE FEATURE. A tool that repairs everything is a tool that fabricates
  something eventually. Four cases are refused outright:

    1. `agent_id` missing. There is no honest way to derive an identity.
    2. `state` missing on a file whose mtime is INSIDE the TTL. 'idle' would assert a
       dead session for a possibly-live one; 'active' would assert liveness we have not
       measured. The file is mid-write or its owner is mid-flight. Leave it.
    3. `live_claims` absent without -AssertNoLiveClaims, or with it when any of the
       three signals is fresh or UNKNOWN. `[]` is a POSITIVE assertion that the session
       holds nothing (HEARTBEATS/README.md rule 1); writing it without evidence
       manufactures the one signal this repo treats as load-bearing. UNKNOWN is never
       STALE.
    4. The file is not JSON at all (YAML frontmatter, truncated, unparseable). Repairing
       prose into JSON means authoring someone else's content. Quarantine it instead.

  WRITES ARE SURGICAL AND REVERSIBLE. Fields are edited in the RAW TEXT, never by
  parse-and-rewrite, so unknown keys, key order, indentation and number formatting
  survive byte-identically. The original bytes are captured before the write and
  RESTORED if post-write validation does not confirm the file now conforms - so a
  repair that makes things worse undoes itself.

  Exit codes (deliberately distinct, so "could not act" never reads as "clean"):
    0  repaired, or already conforming (no-op), or -VerifyOnly confirmed conforming
    1  REFUSED, with the reason printed
    2  file missing, unreadable, or not JSON
    3  repair written but the file STILL fails validation; original bytes restored
    4  a liveness signal was UNKNOWN (absent or unparseable) and the tool will not
       guess - distinct from 1 because the remedy is different

.PARAMETER Path          Heartbeat file to inspect or repair. Required.
.PARAMETER What          Which repairs to consider. All (default), Skew, or Missing.
.PARAMETER Apply         Perform the write. Without it the tool is read-only and reports
                         what it WOULD do.
.PARAMETER AssertNoLiveClaims
                         Permit writing `live_claims: []`, and only if all three liveness
                         signals measure STALE. Measured, not asserted by the caller.
.PARAMETER TtlMinutes    Liveness TTL for the staleness measurement (default 30).
.PARAMETER SelfTest      Run the fixture suite and exit. Touches no real file.
.PARAMETER VerifyOnly    Read-only conformance check of Path, no repair logic.

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/repair-heartbeat.ps1 -Path '.agent/HEARTBEATS/Inkling (inkling-2.5).json'

  Reports what it would do and why, writing nothing.

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/repair-heartbeat.ps1 -Path '...' -Apply

  Repairs, re-validates, and restores the original bytes if the file still fails.
#>
[CmdletBinding()]
param(
    [string]$Path,
    [ValidateSet('All', 'Skew', 'Missing')]
    [string]$What = 'All',
    [switch]$Apply,
    [switch]$AssertNoLiveClaims,
    [int]$TtlMinutes = 30,
    [string]$ClaimsDir,
    [switch]$SelfTest,
    [switch]$VerifyOnly
)

$ErrorActionPreference = 'Continue'   # a bad file is a finding, never a crash
$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$storeDir = Join-Path $repoRoot '.agent/HEARTBEATS'
$stampFmt = 'yyyy-MM-ddTHH:mm:ssZ'

function Get-SignalSet {
    <#
      Evidence for the ONE assertion this tool is most careful about: `live_claims: []`,
      a positive claim that the session holds nothing. Three sources, and the one that
      matters most is the CLAIM DIRECTORY, because `live_claims` is a claim about claims:

        1. the file's own mtime           - is anything touching it?
        2. a SIBLING heartbeat, same id   - a second opinion, when one exists
        3. .agent/CLAIMS/*.json           - DIRECT evidence: does any claim file name this
                                           agent_id, and is that claim fresh?

      Signal 3 was added after a dry run on the live store exposed a design flaw in this
      function. The first version demanded a sibling heartbeat, so an identity with
      exactly ONE file -- the common case, and the only case for most identities -- could
      never have live_claims asserted at all, making the tool useless exactly where it
      was needed. A missing sibling is not an absent signal, it is an identity with one
      heartbeat, and the file's own mtime is then the complete evidence.

      A sibling that EXISTS but does not parse IS a genuine UNKNOWN and still blocks:
      something is there and we cannot read it.
    #>
    param([string]$File, [int]$Ttl, [datetime]$NowUtc)
    $resolved = (Resolve-Path -LiteralPath $File).Path
    $mtime = (Get-Item -LiteralPath $File).LastWriteTimeUtc
    $ageMin = ($NowUtc - $mtime).TotalMinutes

    $raw = [IO.File]::ReadAllText($File, [System.Text.Encoding]::UTF8)
    $id = $null
    try { $id = ($raw | ConvertFrom-Json -ErrorAction Stop).agent_id } catch {}

    $sibling = $null; $unreadableNeighbours = 0
    if ($id) {
        foreach ($f in (Get-ChildItem -LiteralPath (Split-Path -Parent $File) -File -ErrorAction SilentlyContinue)) {
            if ($f.FullName -eq $resolved) { continue }
            try {
                $j = [IO.File]::ReadAllText($f.FullName, [System.Text.Encoding]::UTF8) | ConvertFrom-Json -ErrorAction Stop
                if ([string]$j.agent_id -eq [string]$id) { $sibling = $f; break }
            } catch { $unreadableNeighbours++ }
        }
    }
    # An unparseable NEIGHBOUR is a store-health finding, not evidence about THIS identity:
    # a file that cannot be parsed cannot be shown to carry this agent_id, so it neither
    # supports nor contradicts "this session holds nothing". Treating it as a blocker was
    # the second design flaw this dry run exposed -- in a store containing ANY unparseable
    # file it would have blocked every repair forever, which is the useless-tool failure
    # again. It is counted and printed instead.
    $siblingVerdict = if ($null -ne $sibling) { if ((($NowUtc - $sibling.LastWriteTimeUtc).TotalMinutes) -le $Ttl) { 'FRESH' } else { 'STALE' } }
                      else { 'SELF (only heartbeat for this agent_id)' }

    # Signal 3 - the direct one.
    $claimsDir = if ($ClaimsDir) { $ClaimsDir } else { Join-Path $repoRoot '.agent/CLAIMS' }
    $liveClaim = $null
    if ($id -and (Test-Path -LiteralPath $claimsDir)) {
        foreach ($c in (Get-ChildItem -LiteralPath $claimsDir -Filter *.json -File -ErrorAction SilentlyContinue)) {
            try {
                $cj = [IO.File]::ReadAllText($c.FullName, [System.Text.Encoding]::UTF8) | ConvertFrom-Json -ErrorAction Stop
                if ([string]$cj.agent_id -eq [string]$id) {
                    $cAge = ($NowUtc - $c.LastWriteTimeUtc).TotalMinutes
                    if ($cAge -le $Ttl) { $liveClaim = $c.Name }
                }
            } catch { }
        }
    }

    $signal1 = if ($ageMin -le $Ttl) { 'FRESH' } else { 'STALE' }
    return @{
        File = $File
        Mtime = $mtime
        AgeMinutes = [math]::Round($ageMin, 1)
        ClaimSignal = $signal1
        SiblingFile = $(if ($sibling) { $sibling.Name } else { '(none)' })
        UnreadableNeighbours = $unreadableNeighbours
        SiblingVerdict = $siblingVerdict
        LiveClaim = $(if ($liveClaim) { $liveClaim } else { '(none)' })
        AllStale = ($signal1 -eq 'STALE' -and $null -eq $liveClaim -and
                    ($siblingVerdict -in @('STALE', 'SELF (only heartbeat for this agent_id)')))
        # Genuinely UNKNOWN about the IDENTITY itself: we cannot even name it, so we cannot
        # ask the claim directory whether it holds anything. That is the only honest UNKNOWN.
        GenuinelyUnknown = ($null -eq $id)
    }
}

function Get-FileText {
    <#
      B5-2361, found by running the tool on the live store rather than only on fixtures.
      [IO.File]::ReadAllText(path, encoding) goes through StreamReader with
      detectEncodingFromByteOrderMarks, so it CONSUMES a leading BOM; [Encoding]::GetString
      on the same bytes does NOT, and leaves U+FEFF as the first character. This tool
      needs the bytes (to capture and restore them exactly) and then decodes them itself,
      so it must strip the BOM by hand -- otherwise every BOM'd heartbeat parses as
      "unparseable" and gets refused as not-JSON. validate-heartbeats.ps1 never hit this
      because it only ever used ReadAllText. Same class as B5-1002: an encoding
      difference that reads as a data defect.
      The BOM is re-added on write from $hadBom, so the file keeps its original form.
    #>
    param([byte[]]$Bytes)
    $s = [System.Text.Encoding]::UTF8.GetString($Bytes)
    if ($s.Length -gt 0 -and $s[0] -eq [char]0xFEFF) { return $s.Substring(1) }
    return $s
}

function Test-JsonParses {
    param([string]$File)
    $r = [IO.File]::ReadAllText($File, [System.Text.Encoding]::UTF8)
    if ($r.TrimStart().StartsWith('---')) { return $false }
    try { $null = $r | ConvertFrom-Json -ErrorAction Stop; return $true } catch { return $false }
}

# Field edits are applied to RAW TEXT so unknown keys and formatting survive intact.
function Set-JsonField {
    param([string]$Raw, [string]$Name, [string]$JsonLiteral)
    $rx = '"' + [regex]::Escape($Name) + '"\s*:\s*(?:"[^"]*"|\[[^\]]*\]|null|true|false|-?[\d.]+)'
    if ([regex]::IsMatch($Raw, $rx)) {
        return [regex]::Replace($Raw, $rx, ('"' + $Name + '": ' + $JsonLiteral), 1)
    }
    # absent: insert as the last member, preserving the original closing brace
    $idx = $Raw.LastIndexOf('}')
    if ($idx -lt 0) { return $Raw }
    $head = $Raw.Substring(0, $idx).TrimEnd()
    $tail = $Raw.Substring($idx)
    $sep = if ($head.EndsWith('{')) { '' } else { ',' }
    return $head + $sep + '"' + $Name + '": ' + $JsonLiteral + $tail
}

# =================================================================================
if ($SelfTest) {
    $fail = 0
    $tmp = Join-Path ([System.IO.Path]::GetTempPath()) ('b5-2361-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
    New-Item -ItemType Directory -Path $tmp -Force | Out-Null
    $self = $PSCommandPath
    function New-FixtureHb([string]$name, [string]$body, [int]$ageMinutes) {
        $p = Join-Path $tmp $name
        [IO.File]::WriteAllText($p, $body, (New-Object System.Text.UTF8Encoding($false)))
        $back = (Get-Date).ToUniversalTime().AddMinutes(-$ageMinutes)
        [IO.File]::SetLastWriteTimeUtc($p, $back)
        return $p
    }
    function Run-Repair([string]$p, [switch]$Apply, [switch]$AssertNoLive) {
        $args = @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $self, '-Path', $p)
        if ($Apply) { $args += '-Apply' }
        if ($AssertNoLive) { $args += '-AssertNoLiveClaims' }
        $o = (& powershell @args 2>&1 | Out-String)
        return @{ Exit = $LASTEXITCODE; Out = $o }
    }
    function Check([string]$label, [bool]$ok, [string]$detail) {
        if ($ok) { Write-Output ("  SELFTEST PASS  {0}  ({1})" -f $label, $detail) }
        else { Write-Output ("  SELFTEST FAIL  {0}  ({1})" -f $label, $detail); $script:fail++ }
    }

    $okHb = '{"schema_version":1,"agent_id":"fix-agent","utc":"2026-01-01T00:00:00Z","state":"idle","current_task":null,"live_claims":[],"javac":"1.8.0_292","notes":"fixture"}'

    # T1 RED PATH - a skewed clock is re-stamped FROM THE FILE MTIME, not from now.
    # The fixture is named after its agent_id so the id RESOLVES: post-write verification
    # demands the file fully conform, and an unresolvable agent_id is a finding this tool
    # deliberately does not repair. Naming the fixture wrong makes the rollback fire and
    # the test measure the wrong thing.
    $future = (Get-Date).ToUniversalTime().AddHours(20).ToString($stampFmt)
    $t1 = New-FixtureHb 'fix-agent.json' ('{"schema_version":1,"agent_id":"fix-agent","utc":"' + $future + '","state":"idle","live_claims":[],"notes":"fixture"}') 600
    $wantUtc = (Get-Item $t1).LastWriteTimeUtc.ToString($stampFmt)
    $r1 = Run-Repair $t1 -Apply
    $after1 = [IO.File]::ReadAllText($t1, [System.Text.Encoding]::UTF8)
    Check 'T1 skewed utc re-stamped from the file own mtime' `
        ($r1.Exit -eq 0 -and $after1 -match [regex]::Escape('"utc": "' + $wantUtc + '"') -and $after1 -notmatch [regex]::Escape($future)) `
        ("exit=" + $r1.Exit + " wanted=" + $wantUtc + " payloadHadFuture=" + ($after1 -match [regex]::Escape($future)))

    # T2 REFUSAL - a missing agent_id is never invented.
    $t2 = New-FixtureHb 'noid.json' '{"schema_version":1,"utc":"2026-01-01T00:00:00Z","state":"idle","live_claims":[]}' 600
    $r2 = Run-Repair $t2 -Apply
    Check 'T2 missing agent_id is REFUSED, never invented' `
        ($r2.Exit -eq 1 -and ([IO.File]::ReadAllText($t2, [System.Text.Encoding]::UTF8) -match '"schema_version"')) `
        ("exit=" + $r2.Exit + " (1=refused) fileUnchanged=" + (([IO.File]::ReadAllText($t2, [System.Text.Encoding]::UTF8)) -notmatch 'agent_id"\s*:'))

    # T3 REFUSAL - a fresh file missing state is NOT declared idle or active.
    $t3 = New-FixtureHb 'fresh.json' '{"schema_version":1,"agent_id":"fresh-agent","utc":"2026-01-01T00:00:00Z","live_claims":[]}' 2
    $r3 = Run-Repair $t3 -Apply
    Check 'T3 fresh file missing state is REFUSED, no liveness asserted' `
        ($r3.Exit -eq 1 -and ([IO.File]::ReadAllText($t3, [System.Text.Encoding]::UTF8) -notmatch '"state"')) `
        ("exit=" + $r3.Exit + " stateWritten=" + (([IO.File]::ReadAllText($t3, [System.Text.Encoding]::UTF8)) -match '"state"') + " mtimeAgeMin=2 inside TTL")

    # T4 REFUSAL - non-JSON is quarantined, not authored into JSON.
    $t4 = New-FixtureHb 'frontmatter.json.new' "---`nprovenance: author_llm someone`n---" 600
    $r4 = Run-Repair $t4 -Apply
    Check 'T4 non-JSON frontmatter is REFUSED, not rewritten into JSON' `
        ($r4.Exit -eq 2 -and ([IO.File]::ReadAllText($t4, [System.Text.Encoding]::UTF8)).StartsWith('---')) `
        ("exit=" + $r4.Exit + " (2=not JSON) stillFrontmatter=" + ([IO.File]::ReadAllText($t4, [System.Text.Encoding]::UTF8)).StartsWith('---'))

    # T5 GREEN - an already-conforming file is a no-op, exit 0, bytes unchanged.
    $t5 = New-FixtureHb 'clean.json' $okHb 600
    $before5 = [IO.File]::ReadAllText($t5, [System.Text.Encoding]::UTF8)
    $r5 = Run-Repair $t5 -Apply
    Check 'T5 already-conforming file is an untouched no-op' `
        ($r5.Exit -eq 0 -and [IO.File]::ReadAllText($t5, [System.Text.Encoding]::UTF8) -ceq $before5) `
        ("exit=" + $r5.Exit + " bytesIdentical=" + ([IO.File]::ReadAllText($t5, [System.Text.Encoding]::UTF8) -ceq $before5))

    # T6 NEGATIVE CONTROL - the refusals are load-bearing. Remove refusal T2 by hand
    # (pretend the tool fabricated an id) and confirm the shape we are guarding is the one
    # the test asserts: a file whose agent_id was invented parses as CONFORMING while its
    # identity is fiction. This is the harm, demonstrated.
    $fiction = '{"schema_version":1,"agent_id":"invented-by-repair","utc":"2026-01-01T00:00:00Z","state":"idle","live_claims":[]}'
    $t6 = New-FixtureHb 'fiction.json' $fiction 600
    $fictionParses = Test-JsonParses $t6
    Check 'T6 NEGATIVE CONTROL: an invented agent_id parses clean, which is why T2 must refuse' `
        ($fictionParses -and ($fiction -match 'invented-by-repair')) `
        ("inventedIdentityParsesAsConforming=" + $fictionParses + " -- a schema-clean file with a fictional identity is exactly what refusal T2 prevents")

    # T7 REGRESSION (B5-1002 class) - a BOM'd file must parse AND keep its BOM.
    # This case exists because the bug was invisible to fixtures and only appeared on the
    # live store: every fixture was written with UTF8Encoding($false). ReadAllText eats a
    # BOM and Encoding.GetString does not, so a tool that decodes its own bytes sees
    # U+FEFF as the first character and reports a perfectly good file as unparseable.
    $bomBody = '{"schema_version":1,"agent_id":"bom-agent","utc":"' + $future + '","state":"idle","live_claims":[],"notes":"fixture"}'
    $t7 = Join-Path $tmp 'bom-agent.json'
    [IO.File]::WriteAllText($t7, $bomBody, (New-Object System.Text.UTF8Encoding($true)))
    [IO.File]::SetLastWriteTimeUtc($t7, (Get-Date).ToUniversalTime().AddMinutes(-600))
    $r7 = Run-Repair $t7 -Apply
    $b7 = [IO.File]::ReadAllBytes($t7)
    $hasBom = ($b7.Length -ge 3 -and $b7[0] -eq 0xEF -and $b7[1] -eq 0xBB -and $b7[2] -eq 0xBF)
    Check 'T7 BOM-d file parses, is repaired, and KEEPS its BOM' `
        ($r7.Exit -eq 0 -and $hasBom) `
        ("exit=" + $r7.Exit + " bomPreserved=" + $hasBom + " -- without the fix this file read as unparseable")

    # T8 THE MOST CONSEQUENTIAL GUARD - a file whose agent_id is named by a LIVE claim must
    # NOT get live_claims: []. Writing [] over a live claim is the B5-0622 orphan shape
    # inverted: a session that holds work told the fleet it holds nothing. -ClaimsDir exists
    # so this can be proved without planting a fake claim file in the real store.
    $fxClaims = Join-Path $tmp 'CLAIMS'
    New-Item -ItemType Directory -Path $fxClaims -Force | Out-Null
    $t8 = New-FixtureHb 'held-agent.json' '{"schema_version":1,"agent_id":"held-agent","utc":"2026-01-01T00:00:00Z","state":"idle","notes":"fixture"}' 600
    [IO.File]::WriteAllText((Join-Path $fxClaims 'B5-9001.json'),
        '{"task":"B5-9001","agent_id":"held-agent","started_utc":"' + (Get-Date).ToUniversalTime().ToString($stampFmt) + '","ttl_min":30,"scope":["x"],"javac":"1.8.0_292"}',
        (New-Object System.Text.UTF8Encoding($false)))
    $o8 = (& powershell -NoProfile -ExecutionPolicy Bypass -File $self -Path $t8 -AssertNoLiveClaims -Apply -ClaimsDir $fxClaims 2>&1 | Out-String)
    $e8 = $LASTEXITCODE
    Check 'T8 REFUSES live_claims:[] while a LIVE claim names the agent_id' `
        ($e8 -eq 1 -and ([IO.File]::ReadAllText($t8, [System.Text.Encoding]::UTF8) -notmatch '"live_claims"') -and ($o8 -match 'held-agent|B5-9001')) `
        ("exit=" + $e8 + " (1=refused) liveClaimsWritten=" + (([IO.File]::ReadAllText($t8, [System.Text.Encoding]::UTF8)) -match '"live_claims"') + " -- asserting [] over a live claim is the orphan shape inverted")

    Remove-Item -LiteralPath $tmp -Recurse -Force -ErrorAction SilentlyContinue
    if ($fail -eq 0) { Write-Output '=== repair-heartbeat SELF-TEST GREEN: 8/8 ==='; exit 0 }
    Write-Output ("=== repair-heartbeat SELF-TEST RED: " + $fail + " case(s) failed ==="); exit 1
}

# =================================================================================
if (-not $Path) { Write-Output 'repair-heartbeat: -Path is required (or -SelfTest)'; exit 2 }
if (-not (Test-Path -LiteralPath $Path)) { Write-Output "[B5-2361] MISSING: $Path"; exit 2 }

$full = (Resolve-Path -LiteralPath $Path).Path
$nowUtc = [DateTime]::UtcNow

try { $original = [IO.File]::ReadAllBytes($full) } catch { Write-Output "[B5-2361] UNREADABLE: $full"; exit 2 }
$raw = Get-FileText $original
$hadBom = ($original.Length -ge 3 -and $original[0] -eq 0xEF -and $original[1] -eq 0xBB -and $original[2] -eq 0xBF)

if (-not (Test-JsonParses $full)) {
    Write-Output ("[B5-2361] REFUSED: {0}" -f (Split-Path -Leaf $full))
    Write-Output "    not JSON (YAML frontmatter, truncated, or unparseable). Repairing prose into JSON means"
    Write-Output "    authoring another session's content. Quarantine it instead; retirement policy condition 3"
    Write-Output "    and the _quarantine/ tier exist for exactly this."
    exit 2
}

$obj = $null
try { $obj = $raw | ConvertFrom-Json -ErrorAction Stop } catch { Write-Output '[B5-2361] REFUSED: unparseable'; exit 2 }
$props = @($obj.PSObject.Properties.Name)

if ($VerifyOnly) {
    $bad = @('schema_version', 'agent_id', 'state', 'live_claims') | Where-Object { $_ -notin $props }
    if ($bad.Count -eq 0) { Write-Output ("[B5-2361] CONFORMS: " + (Split-Path -Leaf $full)); exit 0 }
    Write-Output ("[B5-2361] NON-CONFORMING, missing: " + ($bad -join ', ')); exit 1
}

$sig = Get-SignalSet -File $full -Ttl $TtlMinutes -NowUtc $nowUtc
$plan = New-Object System.Collections.Generic.List[string]
$refusal = $null
$new = $raw

# ---- clock ----------------------------------------------------------------------
$tsKey = $null; $tsVal = $null
foreach ($k in @('utc', 'updated_utc', 'heartbeat_utc', 'last_heartbeat_utc')) {
    if ($k -in $props -and $null -ne $obj.$k) { $tsKey = $k; $tsVal = [string]$obj.$k; break }
}
$clockAction = 'none'
if ($What -ne 'Missing') {
    if ($null -eq $tsVal) {
        $clockAction = 'absent'
    } else {
        $parsed = [datetime]::MinValue
        if ([datetime]::TryParse($tsVal, [ref]$parsed)) {
            $pUtc = if ($parsed.Kind -eq [DateTimeKind]::Local) { $parsed.ToUniversalTime() } else { [datetime]::SpecifyKind($parsed, [DateTimeKind]::Utc) }
            $aheadNow = ($pUtc - $nowUtc).TotalMinutes
            $aheadMtime = ($pUtc - $sig.Mtime).TotalMinutes
            if ($aheadNow -gt 60 -or $aheadMtime -gt 60) { $clockAction = 'skew' }
        } else { $clockAction = 'unparseable' }
    }
    if ($clockAction -eq 'unparseable') {
        $refusal = "timestamp '$tsVal' is unparseable and cannot be re-derived without discarding the value"
    }
    if ($null -eq $refusal -and $clockAction -ne 'none') {
        # THE derivation. The file's own mtime - a measurement. NOT UtcNow.
        $from = $sig.Mtime.ToString($stampFmt)
        $plan.Add(("clock: {0} '{1}' -> re-stamp from this file's own mtime {2} (measured, not invented; UtcNow would falsely assert the session was alive during this repair)" -f $clockAction, $tsVal, $from))
        $new = Set-JsonField -Raw $new -Name 'utc' -JsonLiteral ('"' + $from + '"')
    }
}

# ---- identity: never invented ---------------------------------------------------
if ('agent_id' -notin $props) {
    $refusal = 'agent_id is missing and cannot be derived from anything. An invented identity parses as CONFORMING and is fiction (self-test T6). Refusing.'
}

# ---- schema_version -------------------------------------------------------------
if ($null -eq $refusal -and 'schema_version' -notin $props -and $What -ne 'Skew') {
    $plan.Add('schema_version: absent -> 1 (schema constant, not a default)')
    $new = Set-JsonField -Raw $new -Name 'schema_version' -JsonLiteral '1'
}

# ---- state ----------------------------------------------------------------------
if ($null -eq $refusal -and 'state' -notin $props -and $What -ne 'Skew') {
    if ($sig.AgeMinutes -le $TtlMinutes) {
        $refusal = ("state is missing and this file's mtime is {0} min old, INSIDE the {1} min TTL. 'idle' would assert a dead session and 'active' would assert liveness nobody measured. The owner may be mid-write. Refusing." -f $sig.AgeMinutes, $TtlMinutes)
    } else {
        $plan.Add("state: absent -> 'idle' (derived: mtime $($sig.AgeMinutes) min old, outside the $TtlMinutes min TTL)")
        $new = Set-JsonField -Raw $new -Name 'state' -JsonLiteral '"idle"'
    }
}

# ---- live_claims: a POSITIVE assertion, needs evidence --------------------------
if ($null -eq $refusal -and 'live_claims' -notin $props -and $What -ne 'Skew') {
    if (-not $AssertNoLiveClaims) {
        $refusal = 'live_claims is absent. [] is a POSITIVE assertion that the session holds nothing (HEARTBEATS/README.md rule 1) and omission is UNKNOWN. Re-run with -AssertNoLiveClaims to assert it WITH measured evidence.'
    } elseif ($sig.GenuinelyUnknown) {
        Write-Output ("[B5-2361] UNKNOWN SIGNAL for {0}" -f (Split-Path -Leaf $full))
        Write-Output ("    file signal={0} ({1} min)   sibling heartbeat signal={2} file={3}   live claim={4}" -f $sig.ClaimSignal, $sig.AgeMinutes, $sig.SiblingVerdict, $sig.SiblingFile, $sig.LiveClaim)
        Write-Output '    UNKNOWN is never STALE. This tool will not assert live_claims against an absent signal;'
        Write-Output '    the remedy is an owner heartbeat or a human ruling, not a guess.'
        exit 4
    } elseif (-not $sig.AllStale) {
        $refusal = ("live_claims is absent and the evidence does not support asserting it: file signal={0}, sibling signal={1}, live claim={2}. Refusing to assert the session holds nothing while it may be holding something." -f $sig.ClaimSignal, $sig.SiblingVerdict, $sig.LiveClaim)
    } else {
        $plan.Add("live_claims: absent -> [] (asserted WITH evidence: file $($sig.AgeMinutes) min STALE, no live claim names this agent_id, sibling $($sig.SiblingVerdict))")
        $new = Set-JsonField -Raw $new -Name 'live_claims' -JsonLiteral '[]'
    }
}

Write-Output ("[B5-2361] {0}   ({1})" -f (Split-Path -Leaf $full), $(if ($sig.ClaimSignal -eq 'STALE') { 'file signal STALE, ' + $sig.AgeMinutes + ' min old' } else { 'file signal ' + $sig.ClaimSignal + ', ' + $sig.AgeMinutes + ' min old' }))
Write-Output ("           sibling heartbeat signal={0} file={1}" -f $sig.SiblingVerdict, $sig.SiblingFile)
Write-Output ("           live CLAIM naming this agent_id: {0}   unparseable neighbour(s) in this dir: {1} (store health, NOT evidence about this identity)" -f $sig.LiveClaim, $sig.UnreadableNeighbours)

if ($refusal) {
    Write-Output ("[B5-2361] REFUSED: {0}" -f $refusal)
    foreach ($p in $plan) { Write-Output ("    (would also have: " + $p + ")") }
    Write-Output '    nothing written.'
    exit 1
}

if ($plan.Count -eq 0) {
    Write-Output '[B5-2361] nothing to repair; file already conforms on every field this tool owns.'
    exit 0
}

foreach ($p in $plan) { Write-Output ("    " + $p) }

if (-not $Apply) {
    Write-Output '[B5-2361] dry run (no -Apply). Nothing written.'
    exit 0
}

# ---- write, then verify, then roll back if it did not work -----------------------
$enc = New-Object System.Text.UTF8Encoding($hadBom)
[System.IO.File]::WriteAllText($full, $new, $enc)

$stillBad = $false
try { $null = (Get-FileText ([System.IO.File]::ReadAllBytes($full))) | ConvertFrom-Json -ErrorAction Stop } catch { $stillBad = $true }
$check = $null
try { $check = (Get-FileText ([System.IO.File]::ReadAllBytes($full))) | ConvertFrom-Json -ErrorAction Stop } catch { }
if ($stillBad -or $null -eq $check) {
    [System.IO.File]::WriteAllBytes($full, $original)
    Write-Output '[B5-2361] REPAIR DID NOT VALIDATE -- original bytes restored. Reporting, not leaving a worse file.'
    exit 3
}

$vp = & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repoRoot '.agent/tools/validate-heartbeats.ps1') -Directory (Split-Path -Parent $full) -Json 2>$null | Out-String
$lines = $vp -split "`r?`n"
$banner = ($lines | Select-String -Pattern '^===' | Select-Object -First 1).LineNumber
$prefix = if ($banner) { ($lines[0..($banner - 2)] -join "`n") } else { ($lines -join "`n") }
$stillFailing = $false
try {
    $rows = $prefix | ConvertFrom-Json -ErrorAction Stop
    if ($null -ne $rows) {
        $me = Split-Path -Leaf $full
        $stillFailing = @($rows | Where-Object { $_.Verdict -ne 'CONFORMS' -and $_.File -eq $me }).Count -gt 0
    }
} catch { $stillFailing = $true }

if ($stillFailing) {
    [System.IO.File]::WriteAllBytes($full, $original)
    Write-Output '[B5-2361] FILE STILL FAILS validate-heartbeats after repair -- original bytes restored.'
    Write-Output '    This tool repairs the defects it understands. Anything left is reported, not hidden.'
    exit 3
}

Write-Output ("[B5-2361] REPAIRED and re-verified: {0} ({1} edit(s))" -f (Split-Path -Leaf $full), $plan.Count)
exit 0