<#
.SYNOPSIS
  Read-only validator for the agent heartbeat store (heartbeat-schema-proposal Sec.7).

.DESCRIPTION
  Audits every entry in .agent/HEARTBEATS/ against heartbeat schema v1 and reports
  per-file findings. STRICTLY READ-ONLY: creates, edits, moves and deletes nothing.

  Design rules, each traceable to a failure this repo actually hit:

   * One unparseable file must not abort the audit. A survey using ConvertFrom-Json
     over *.json THREW on the first YAML-frontmatter file and silently returned zero
     rows. Every file is read inside its own try/catch; findings are COLLECTED and the
     exit code is decided at the END. A parse failure is a finding, never a crash.

   * The checks are INDEPENDENT of the writer, so they can go red on real data. A
     self-check whose predicate matches the code it checks is a tautology: the B5-0613
     version reported 312==312 green while 9 rows carried a corrupt status.

   * The FILENAME is not authoritative. Windows forbids both ':' and '/' in filenames
     and four agent_ids contain one or both, so "stem == agent_id" is unsatisfiable
     for them. Resolution order: exact stem match, then an explicit _registry.json
     entry, then a ':'/'/' -> '-' sanitised match (the repo's own report-filename
     convention).

   * live_claims is validated against the RAW TEXT, not the parsed object, because
     PowerShell 5.1 ConvertFrom-Json does not round-trip array-ness reliably.

   * B5-1469: a heartbeat whose utc payload reads more than 60 minutes ahead of
     sampled UtcNow OR more than 60 minutes ahead of its own file mtime is a
     FUTURE SKEW finding, reported with the value and both deltas. Tolerance is
     matched to the runner's 60-minute bound (B5-0952/B5-1466) so the two
     instruments agree. The finding is a data-quality defect, NOT a liveness
     input: mtime stays the sole liveness source per the run-queue heartbeat
     index, and this check never changes TTL or reap semantics.

   * mtime is reported next to the parsed value: during the 2026-09-27 survey mtime
     was the most reliable liveness signal because payloads were malformed in 19 of 32
     files while the filesystem maintained mtime regardless.

   * Unknown/legacy keys are reported as NOTES, not violations. The schema does not
     forbid extra properties, and failing on them would make the tool noise.

.PARAMETER Directory  Heartbeat directory. Default <repo>/.agent/HEARTBEATS
.PARAMETER Json       Emit machine-readable JSON instead of the table.
.PARAMETER TtlMinutes Freshness window for the mtime column (default 30).
.PARAMETER FutureSkewMinutes Future-skew tolerance for the utc payload vs sampled UtcNow and vs the file's own mtime (default 60, matched to the runner bound).

.OUTPUTS
  Exit 0 = every heartbeat conforms. Exit 1 = a non-conforming or unreadable file.
  Exit 2 = the directory is missing or empty.
#>
[CmdletBinding()]
param(
    [string]$Directory,
    [switch]$Json,
    [int]$TtlMinutes = 30,
    [int]$FutureSkewMinutes = 60
)

$ErrorActionPreference = "Continue"      # deliberate: a bad file is a finding, not a crash
$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if (-not $Directory) { $Directory = Join-Path $repoRoot ".agent/HEARTBEATS" }

# ---- canonical schema (heartbeat-schema-proposal Sec.2, as amended 2026-09-27) ----
# 'busy' was added to the enum by human approval after a live agent (opencode
# (me-so-poor), working B5-0620) adopted the schema and chose it. A schema that a
# working agent cannot satisfy is a schema that gets abandoned, not one that gets obeyed.
$ValidState   = @('active', 'idle', 'busy')
$TimestampKey = @('utc', 'updated_utc', 'heartbeat_utc', 'last_heartbeat_utc')  # utc canonical; rest tolerated one cycle
$KnownKey     = @('schema_version','agent_id','utc','state','current_task','live_claims','javac','notes',
                  'updated_utc','heartbeat_utc','last_heartbeat_utc','status','thread_id','model','last_seen_utc',
                  'session_start_utc','tasks_done_this_session','tasks_seeded_this_session','tasks_completed',
                  'scope','task','started_utc','ttl_min','note','completed_tasks','tasks_in_progress',
                  'blocked_count','last_completed','last_compile','conformance','smoke','reusable_lesson',
                  'scope_covered','build_gate_ok','smoke_test_ok','conformance_ok','current_task_id',
                  'state_history','merged_from','session_notes_history')

if (-not (Test-Path -LiteralPath $Directory)) { Write-Error "Heartbeat directory not found: $Directory"; exit 2 }

# ---- agent_id -> filename registry (authoritative for ids a filename cannot hold) --
$registry = $null
$regPath  = Join-Path $Directory '_registry.json'
if (Test-Path -LiteralPath $regPath) {
    try {
        $registry = @{}
        # B5-1002: encoding pinned to explicit UTF-8 (heartbeats are strict JSON, UTF-8 per HEARTBEATS/README.md).
        $rm = ([IO.File]::ReadAllText($regPath, [System.Text.Encoding]::UTF8) | ConvertFrom-Json -ErrorAction Stop).map
        if ($null -ne $rm) { $rm.PSObject.Properties | ForEach-Object { $registry[$_.Name] = $_.Value } }
    } catch { $registry = $null }
}

$now   = [DateTime]::UtcNow
# B5-1062: recurse into _quarantine/. A quarantined file is out of the live store by
# decision, but a lookalike-codepoint name is a DEFECT OF THE NAME, not of the
# location, and the only cross-file evidence of the collision is the sibling's presence.
# Without -Recurse this tool reported 0 collisions on a store that held a live one.
# -File keeps subdirectories from being returned; '*.json' filtering is done below
# rather than via -Filter so that non-.json entries stay visible as EXTENSION findings.
$files = @(Get-ChildItem -LiteralPath $Directory -File -Recurse | Where-Object { $_.Name -ne 'README.md' -and $_.Name -ne '_registry.json' })
if ($files.Count -eq 0) { Write-Error "Heartbeat directory is empty: $Directory"; exit 2 }

$results = @()

foreach ($f in $files) {
    $issues = New-Object System.Collections.Generic.List[string]
    $notes  = New-Object System.Collections.Generic.List[string]
    $data   = $null
    $raw    = ''

    try { $raw = [IO.File]::ReadAllText($f.FullName, [System.Text.Encoding]::UTF8) } catch { $issues.Add("UNREADABLE: $($_.Exception.Message)"); $raw = '' }

    $stem   = [IO.Path]::GetFileNameWithoutExtension($f.Name)
    $ageMin = [int](($now - $f.LastWriteTimeUtc).TotalMinutes)
    $format = 'unknown'; $tsValue = $null; $tsKey = $null

    if ($raw.Trim() -eq '') {
        $format = 'empty'; $issues.Add('EMPTY: file has no content')
    } elseif ($raw.TrimStart().StartsWith('---')) {
        $format = 'YAML frontmatter'
        $issues.Add('FORMAT: YAML frontmatter, not JSON -- unreadable by any JSON reader')
        if ($raw -match '(?m)^\s*heartbeat_utc\s*:\s*"?([^"\r\n]+)') { $tsKey = 'heartbeat_utc'; $tsValue = $Matches[1].Trim() }
    } else {
        try { $data = $raw | ConvertFrom-Json -ErrorAction Stop; $format = 'JSON' }
        catch { $format = 'unparseable'; $issues.Add("PARSE FAILED: " + $_.Exception.Message.Split([char]10)[0]) }
    }

    if ($null -ne $data) {
        $props = @($data.PSObject.Properties.Name)

        if ('schema_version' -notin $props) { $issues.Add('schema_version missing (pre-schema file)') }

        foreach ($k in $TimestampKey) { if ($k -in $props -and $null -ne $data.$k) { $tsKey = $k; $tsValue = [string]$data.$k; break } }
        if ($null -eq $tsValue) {
            $issues.Add('TIMESTAMP MISSING: no utc / updated_utc / heartbeat_utc / last_heartbeat_utc')
        } else {
            $parsed = [DateTime]::MinValue
            if (-not [DateTime]::TryParse($tsValue, [ref]$parsed)) { $issues.Add("TIMESTAMP UNPARSEABLE: '$tsValue'") }
            else {
                # B5-1469: heartbeat-side future-skew gate, the complement of the
                # claims-side B5-1466 rule in the census tools. A payload clocked more
                # than $FutureSkewMinutes ahead of sampled UtcNow or of its own file
                # mtime is the B5-1008/B5-0952 clock-defect class wearing a heartbeat:
                # report it with the value and both deltas. It is a FINDING only -- it
                # never feeds liveness; mtime stays the sole liveness source.
                $parsedUtc = if ($parsed.Kind -eq [DateTimeKind]::Local) { $parsed.ToUniversalTime() } else { [DateTime]::SpecifyKind($parsed, [DateTimeKind]::Utc) }
                $aheadOfNowMin   = ($parsedUtc - $now).TotalMinutes
                $aheadOfMtimeMin = ($parsedUtc - $f.LastWriteTimeUtc).TotalMinutes
                if ($aheadOfNowMin -gt $FutureSkewMinutes -or $aheadOfMtimeMin -gt $FutureSkewMinutes) {
                    $issues.Add(("FUTURE SKEW: utc '{0}' is {1:N1} min ahead of sampled UtcNow and {2:N1} min ahead of its own file mtime (tolerance {3} min). Finding only -- never feeds liveness; mtime stays the sole liveness source per the run-queue heartbeat index." -f $tsValue, $aheadOfNowMin, $aheadOfMtimeMin, $FutureSkewMinutes))
                }
            }
            if ('utc' -notin $props) { $notes.Add("legacy timestamp field '$tsKey' (tolerated one cycle); canonical field is 'utc'") }
        }

        if ('agent_id' -notin $props) {
            $issues.Add('agent_id missing')
        } else {
            $id = [string]$data.agent_id
            $sanitised = ($id -replace '[:/]', '-')
            $viaRegistry = ($null -ne $registry -and $registry.ContainsKey($id) -and $registry[$id] -eq $f.Name)
            if ($id -ne $stem -and -not $viaRegistry -and $sanitised -ne $stem) {
                $issues.Add("agent_id '$id' matches no known file (stem='$stem', sanitised='$sanitised', registered=$viaRegistry)")
            }
            elseif ($viaRegistry) { $notes.Add("agent_id resolved via _registry.json (filename cannot express this id)") }
        }

        $lm = [regex]::Match($raw, '"live_claims"\s*:\s*(\[[^\]]*\]|null|")')
        if (-not $lm.Success) {
            $issues.Add('live_claims missing -- MUST be present; [] asserts holding nothing, omission is UNKNOWN')
        } elseif ($lm.Groups[1].Value -notmatch '^\[') {
            $issues.Add("live_claims is not a JSON array (found: $($lm.Groups[1].Value))")
        }

        if ('state' -notin $props) { $issues.Add('state missing (expected active|idle|busy)') }
        elseif ($ValidState -notcontains [string]$data.state) { $issues.Add("state '$($data.state)' not in enum ($($ValidState -join '|'))") }

        $unknown = @($props | Where-Object { $_ -notin $KnownKey })
        if ($unknown.Count -gt 0) { $notes.Add("legacy/unknown keys: " + ($unknown -join ', ')) }
    }

    if ($f.Extension -ne '.json') { $issues.Add("EXTENSION: '$($f.Name)' is not a .json file") }

    # B5-1062: a lookalike codepoint in the FILENAME is a finding in its own right.
    # The cross-file agent_id check below cannot see it: a U+F03A colon satisfies every
    # Windows filename constraint, so 'solar-pro4<U+F03A>free.json' parses, and if the
    # correctly-spelled sibling is absent from the scanned set the store reports
    # 0 collisions while two sessions share one identity. Measured on the live store
    # before this check existed: 94 files / 0 collisions, with the sibling sitting
    # in _quarantine/ that the non-recursive listing never reached.
    # NOTE: no \u{...} astral escape here. .NET's regex engine rejects the brace form
    # outright ("Insufficient hexadecimal digits"), and a regex that throws inside
    # Matches() returns 0 findings -- a validator that silently reports "clean" on the
    # exact input it was written to catch. Astral PUA is matched by explicit surrogate
    # pairs below instead.
    $lookalikes = @(
        [regex]::Matches($f.Name, '[\uE000-\uF8FF]')                       # BMP private use
        [regex]::Matches($f.Name, '\uD800[\uDC00-\uDFFF]')                 # astral PUA, low surrogate
        [regex]::Matches($f.Name, '\u2028|\u2029|\u00A0')                 # line/para sep, NBSP
    ) | ForEach-Object { $_ } | Where-Object { $_ -ne $null }
    if (@($lookalikes).Count -gt 0) {
        $cps = (@($lookalikes) | ForEach-Object { 'U+{0:X4}' -f [int][char]$_.Value } | Sort-Object -Unique) -join ', '
        $issues.Add("FILENAME LOOKALIKE: '$($f.Name)' contains non-ASCII codepoint(s) $cps -- a PUA or separator character that Windows accepts in a filename but that reads as ':' or '/' and produces a DIFFERENT stem for the SAME agent_id (R7). Sanitise to the documented ':'/'/' -> '-' form; do not mint a second file.")
    }

    $results += [pscustomobject]@{
        File         = $f.Name
        Format       = $format
        AgentId      = $(if ($null -ne $data -and $data.PSObject.Properties.Name -contains 'agent_id') { [string]$data.agent_id } else { $null })
        State        = $(if ($null -ne $data -and $data.PSObject.Properties.Name -contains 'state') { [string]$data.state } else { $null })
        TimestampKey = $tsKey
        TimestampUtc = $tsValue
        AgeMinutes   = $ageMin
        Verdict      = $(if ($issues.Count -eq 0) { 'CONFORMS' } else { 'NON-CONFORMING' })
        IssueCount   = $issues.Count
        Issues       = $issues
        NoteCount    = $notes.Count
        Notes        = $notes
    }
}

# ---- store-level invariant: every agent_id must resolve to EXACTLY ONE file ---------
# This is the enforceable form of the amended rule. The per-file check above confirms an
# agent_id resolves to SOME file; this confirms it resolves to only ONE. Two files
# asserting the same identity make live_claims ambiguous no matter how well-formed
# either file is -- the ambiguity is in the store, not in the row.
$byAgent = @{}
foreach ($r in $results) {
    if ([string]::IsNullOrEmpty($r.AgentId)) { continue }
    $k = $r.AgentId
    if (-not $byAgent.ContainsKey($k)) { $byAgent[$k] = @() }
    $byAgent[$k] += $r.File
}
$collisions = @($byAgent.GetEnumerator() | Where-Object { $_.Value.Count -gt 1 })

# ---- report -----------------------------------------------------------------------
$bad = @($results | Where-Object { $_.Verdict -ne 'CONFORMS' })
$ok  = @($results | Where-Object { $_.Verdict -eq 'CONFORMS' })

if ($Json) {
    $results | ConvertTo-Json -Depth 5
} else {
    Write-Output "=== Heartbeat schema validation ==="
    Write-Output ("directory : {0}" -f $Directory)
    Write-Output ("registry  : {0}" -f $(if ($null -ne $registry) { "$($registry.Count) agent_id -> filename entries" } else { 'absent' }))
    Write-Output ("files     : {0}   conforming: {1}   non-conforming: {2}" -f $results.Count, $ok.Count, $bad.Count)
    Write-Output ("identity  : {0} distinct agent_id   collisions: {1}" -f $byAgent.Count, $collisions.Count)
    Write-Output ("TTL       : {0} min" -f $TtlMinutes)
    # B5-1002: encoding receipt -- every file above was read as explicit UTF-8.
    Write-Output "encoding  : UTF-8 (explicit; [IO.File]::ReadAllText(path, UTF8))"
    Write-Output ""
    Write-Output ("{0,-44} {1,-40} {2,-9} {3,-8} {4}" -f 'FILE', 'AGENT_ID', 'MTIME_MIN', 'STATE', 'VERDICT')
    Write-Output ("-" * 118)
    foreach ($r in ($results | Sort-Object Verdict, File)) {
        Write-Output ("{0,-44} {1,-40} {2,-9} {3,-8} {4}" -f `
            $r.File, $(if ($r.AgentId) { $r.AgentId } else { '-' }), $r.AgeMinutes, $(if ($r.State) { $r.State } else { '-' }), $r.Verdict)
    }
    if ($bad.Count -gt 0) {
        Write-Output ""
        Write-Output "=== Findings ==="
        foreach ($r in $bad) {
            Write-Output ""
            Write-Output ("[{0}]  ({1} issue{2})" -f $r.File, $r.IssueCount, $(if ($r.IssueCount -eq 1) { '' } else { 's' }))
            foreach ($i in $r.Issues) { Write-Output ("    - {0}" -f $i) }
        }
    }
    if (@($results | Where-Object { $_.NoteCount -gt 0 }).Count -gt 0) {
        Write-Output ""
        Write-Output "=== Notes (informational, not violations) ==="
        foreach ($r in ($results | Where-Object { $_.NoteCount -gt 0 } | Sort-Object File)) {
            foreach ($n in $r.Notes) { Write-Output ("  {0,-44} {1}" -f $r.File, $n) }
        }
    }
}

if ($collisions.Count -gt 0) {
    Write-Output ""
    Write-Output "=== IDENTITY COLLISIONS (one agent_id claiming multiple files) ==="
    Write-Output "    live_claims cannot be trusted for these agents: the store cannot say which file is authoritative."
    foreach ($c in ($collisions | Sort-Object Name)) {
        Write-Output ("  {0}" -f $c.Key)
        foreach ($fn in $c.Value) { Write-Output ("      -> {0}" -f $fn) }
    }
}

if ($bad.Count -gt 0) { exit 1 }
if ($collisions.Count -gt 0) { exit 1 }
exit 0
