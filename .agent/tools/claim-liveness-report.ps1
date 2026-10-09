<#
.SYNOPSIS
  B5-2650: read-only three-signal claim liveness reporter over .agent/CLAIMS/*.json.

.DESCRIPTION
  Nothing in this repo surveyed claims that ALREADY exist. new-claim.ps1 refuses a
  bad claim at write time; run-queue.ps1 suppresses rows whose claim is not provably
  stale; 00_BOOT step 10 lets an agent reap a stale claim ONLY after recording the
  three-signal evidence in the ledger - but no shipped instrument prints that
  evidence, so reaping is eyeballed, and every recorded reap disaster was an eyeball
  reading a different signal than the rule names (B5-0597 single-signal reap
  destroyed live work; B5-0609 an absent lookup read as age 0 and scored live
  forever; B5-0660 single-signal staleness; B5-0622 orphan claims on closed rows
  with fresh heartbeats).

  For every claim file this tool prints the three signals the canonical rule names
  (.agent/CLAIMS/README.md, section "Liveness: three signals, never one"):

    1. claim    - started_utc, or the claim file mtime when started_utc is
                  unparseable (the fallback is printed as a fallback);
    2. heartbeat - the OWNER's heartbeat file mtime, resolved through the
                  Amendment A1.1 sanitisation (only ':' and '/' map to '-';
                  spaces and parentheses survive verbatim). REQUIRED: absent or
                  unparseable is UNKNOWN with its reason, never an age (B5-0609);
    3. report   - the newest .agent/REPORTS/ file whose name contains the task id.
                  CONTRIBUTING ONLY: its absence mid-task is the normal case and
                  never makes a claim UNKNOWN.

  plus two checks that ride the same read: an ORPHAN flag when the ledger row is
  not OPEN (B5-0622: a claim on a closed row can never be worked through that
  file), and a SKEW flag when started_utc is AHEAD of the real clock - a negative
  age compares younger than any TTL and would otherwise read LIVE forever
  (B5-1827 measured 779.9 min ahead; B5-1931 names both causes).

  Verdicts use the canonical wording exactly. LIVE = at least one signal found and
  within TTL. STALE = at least one found and none within TTL. UNKNOWN = a required
  signal absent or unparseable. An absent signal is UNKNOWN, never LIVE and never
  STALE. A finding (exit 1) is any non-LIVE verdict, any ORPHAN, any SKEW, an
  unparseable claim file, or a task id with no ledger row.

  This tool NEVER deletes or edits a claim. Reaping stays a claimed, ledger-noted
  act per 00_BOOT step 10; this tool exists so that note can quote measurements
  instead of impressions.

  -SelfTest builds a fixture store (LIVE, STALE, UNKNOWN, ORPHAN, SKEW,
  UNPARSEABLE, ROW-MISSING) and asserts every verdict, flag and finding count.
  Its green is only worth reading because a MUTANT of this file that lets a
  negative age count as fresh was observed failing the SKEW case (red-witness
  recorded in the B5-2650 report).

.OUTPUTS
  Exit 0 = every claim read; all verdicts LIVE with no orphan, skew or parse flag.
  Exit 1 = at least one finding, each printed with its evidence.
  Exit 2 = claims dir, heartbeats dir, reports dir or ledger missing/unreadable.
           Deliberately NOT 0: a census that could not read its inputs must never
           report a clean store (the run-dup-census.ps1 discipline).
#>
[CmdletBinding()]
param(
    [string]$ClaimsDir,
    [string]$HeartbeatsDir,
    [string]$ReportsDir,
    [string]$LedgerPath,
    [switch]$SelfTest
)


$ErrorActionPreference = 'Stop'
$RepoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)

# Rooted overrides are used verbatim; only defaults are derived from the repo
# root (the check-citations.ps1 B5-1925 lesson: Join-Path on an absolute path
# produced <repo>\C:\... nonsense on a perfectly good fixture).
if (-not $ClaimsDir)     { $ClaimsDir = Join-Path $RepoRoot '.agent/CLAIMS' }
if (-not $HeartbeatsDir) { $HeartbeatsDir = Join-Path $RepoRoot '.agent/HEARTBEATS' }
if (-not $ReportsDir)    { $ReportsDir = Join-Path $RepoRoot '.agent/REPORTS' }
if (-not $LedgerPath)    { $LedgerPath = Join-Path $RepoRoot '.agent/TASK_LEDGER.md' }

function Get-SanitisedId([string]$AgentId) {
    # Amendment A1.1: sanitise ONLY ':' and '/' to '-'; every other character,
    # including spaces and parentheses, survives verbatim.
    if ([string]::IsNullOrEmpty($AgentId)) { return $null }
    return $AgentId.Replace(':', '-').Replace('/', '-')
}

function Get-RowStatusMap([string]$Path) {
    # Last row wins for a duplicate id; uniqueness is run-dup-census.ps1's job,
    # not this tool's. Leading pipes are tolerated (\|+ handles doubleLead rows)
    # so one defective lead pipe does not silently hide a row from every lookup.
    $map = @{}
    foreach ($ln in [System.IO.File]::ReadAllLines($Path)) {
        if ($ln -match '^\|+\s*(B5-\d+)\s*\|\s*([A-Za-z]+)\s*\|') {
            $map[$Matches[1]] = $Matches[2].ToUpperInvariant()
        }
    }
    return $map
}

function New-ClaimReportObject([string]$Task, [string]$Agent) {
    return [pscustomobject]@{
        Task       = $Task
        Agent      = $Agent
        Verdict    = 'UNKNOWN'
        Flags      = New-Object System.Collections.ArrayList
        Reasons    = New-Object System.Collections.ArrayList
        ClaimAge   = $null   # minutes; $null = signal not determinable
        ClaimAgeWhy= $null   # 'started_utc' or 'mtime fallback'
        HbAge      = $null
        HbPath     = $null
        ReportAge  = $null
        RowStatus  = $null
        Findings   = 0
        TtlMin     = 30
    }
}

function Test-ClaimLiveness {
    param(
        [string]$ClaimPath,
        [hashtable]$StatusMap,
        [string]$HeartbeatsDir,
        [string]$ReportsDir,
        [int]$TtlDefault,
        [datetime]$NowUtc
    )

    $r = New-ClaimReportObject -Task ((Split-Path $ClaimPath -Leaf) -replace '\.json$', '') -Agent $null

    $json = $null
    try {
        $json = Get-Content -LiteralPath $ClaimPath -Raw -Encoding UTF8 | ConvertFrom-Json
    } catch {
        [void]$r.Flags.Add('UNPARSEABLE')
        [void]$r.Reasons.Add(('claim JSON unparseable: {0}' -f $_.Exception.Message))
        $r.Findings++
        $r.Verdict = 'UNKNOWN'
        return $r
    }

    if ($json.agent_id) { $r.Agent = [string]$json.agent_id }
    if ($json.task) { $r.Task = [string]$json.task }
    $ttl = $TtlDefault
    if ($json.ttl_min -is [int] -and $json.ttl_min -gt 0) { $ttl = [int]$json.ttl_min }
    $r.TtlMin = $ttl

    # Signal 1: claim age. started_utc first, claim file mtime as the documented
    # fallback when started_utc is missing or unparseable (CLAIMS README rule 1).
    $claimKnown = $false
    $when = $null
    if ($json.started_utc) {
        try {
            $when = [datetime]::Parse([string]$json.started_utc,
                [Globalization.CultureInfo]::InvariantCulture,
                [Globalization.DateTimeStyles]::RoundtripKind)
            $r.ClaimAgeWhy = 'started_utc'
        } catch {
            $when = $null
        }
    }
    if ($null -eq $when) {
        try {
            $when = (Get-Item -LiteralPath $ClaimPath).LastWriteTimeUtc
            $r.ClaimAgeWhy = 'mtime fallback'
            [void]$r.Reasons.Add('started_utc missing or unparseable; claim signal taken from file mtime')
        } catch {
            $r.ClaimAgeWhy = $null
        }
    }
    if ($null -ne $when) {
        $claimKnown = $true
        $r.ClaimAge = ($NowUtc - $when.ToUniversalTime()).TotalMinutes
        if ($r.ClaimAge -lt 0) {
            # B5-1931: an ahead-of-clock started_utc reads LIVE forever, because
            # a negative age compares younger than any TTL. Flag it and EXCLUDE
            # it from freshness; never let it be the fresh signal.
            [void]$r.Flags.Add('SKEW')
            [void]$r.Reasons.Add(('started_utc is {0:F1} min AHEAD of real UTC (B5-1931 class); excluded from freshness, never LIVE' -f (-$r.ClaimAge)))
            $r.Findings++
        }
    } else {
        [void]$r.Reasons.Add('claim signal not determinable (no started_utc and mtime unreadable)')
    }

    # Signal 2: OWNER heartbeat mtime - REQUIRED; absent is UNKNOWN, never an age.
    $hbKnown = $false
    $san = Get-SanitisedId $r.Agent
    if ($null -eq $san) {
        [void]$r.Reasons.Add('claim carries no agent_id; heartbeat signal UNKNOWN')
    } else {
        $cand = Join-Path $HeartbeatsDir ($san + '.json')
        if (Test-Path -LiteralPath $cand) {
            $r.HbPath = $san + '.json'
            $r.HbAge = ($NowUtc - (Get-Item -LiteralPath $cand).LastWriteTimeUtc).TotalMinutes
            $hbKnown = $true
        } else {
            [void]$r.Reasons.Add(("heartbeat file absent for sanitised id '{0}' (required signal UNKNOWN, B5-0609)" -f $san))
        }
    }


    # Signal 3: newest report naming this task - CONTRIBUTING ONLY; absence is
    # the normal mid-task case and never yields UNKNOWN by itself.
    $rep = @(Get-ChildItem -LiteralPath $ReportsDir -File -ErrorAction SilentlyContinue |
             Where-Object { $_.Name -like ('*' + $r.Task + '*') } |
             Sort-Object LastWriteTimeUtc -Descending)
    if ($rep.Count -gt 0) {
        $r.ReportAge = ($NowUtc - $rep[0].LastWriteTimeUtc).TotalMinutes
    } else {
        [void]$r.Reasons.Add('report absent (contributing only; normal mid-task)')
    }

    # Row status / ORPHAN (B5-0622): absence of the claim file is necessary but
    # NOT sufficient - the row itself must still read OPEN.
    if ($StatusMap.ContainsKey($r.Task)) {
        $r.RowStatus = $StatusMap[$r.Task]
        if ($r.RowStatus -ne 'OPEN') {
            [void]$r.Flags.Add('ORPHAN')
            [void]$r.Reasons.Add(('ledger row reads {0}, not OPEN: release this claim, do not work it (B5-0622)' -f $r.RowStatus))
            $r.Findings++
        }
    } else {
        $r.RowStatus = 'ROW-MISSING'
        [void]$r.Flags.Add('ROW-MISSING')
        [void]$r.Reasons.Add('no ledger row carries this task id; OPEN cannot be confirmed (B5-0622)')
        $r.Findings++
    }

    # Verdict - canonical wording from .agent/CLAIMS/README.md: LIVE = at least
    # one signal found and within TTL; STALE = at least one found and none
    # within TTL; UNKNOWN = a required signal absent or unparseable.
    $fresh = @()
    if ($claimKnown -and $r.ClaimAge -ge 0 -and $r.ClaimAge -le $ttl) { $fresh += 'claim' }
    if ($hbKnown -and $r.HbAge -ge 0 -and $r.HbAge -le $ttl) { $fresh += 'heartbeat' }
    if ($null -ne $r.ReportAge -and $r.ReportAge -ge 0 -and $r.ReportAge -le $ttl) { $fresh += 'report' }

    if ($fresh.Count -gt 0) {
        $r.Verdict = 'LIVE'
        [void]$r.Reasons.Add(('LIVE: fresh signal(s) within {0} min TTL: {1}' -f $ttl, ($fresh -join ', ')))
    } elseif (-not $hbKnown) {
        $r.Verdict = 'UNKNOWN'
        [void]$r.Reasons.Add('UNKNOWN: required heartbeat signal absent or unparseable - never STALE, never reapsable by this tool (B5-0609)')
        $r.Findings++
    } elseif ($claimKnown -or $null -ne $r.ReportAge) {
        $r.Verdict = 'STALE'
        [void]$r.Reasons.Add(('STALE: no signal within {0} min TTL - reapsable ONLY with a reap note in .agent/TASK_LEDGER.md; this tool deletes nothing' -f $ttl))
        $r.Findings++
    } else {
        $r.Verdict = 'UNKNOWN'
        [void]$r.Reasons.Add('UNKNOWN: no determinable signal at all')
        $r.Findings++
    }

    return $r
}

# ---- -SelfTest: fixture store asserting every verdict (B5-2650) ----
if ($SelfTest) {

    $script:Failed = New-Object System.Collections.ArrayList
    $fxRoot    = Join-Path ([IO.Path]::GetTempPath()) ('b52650-selftest-' + [Guid]::NewGuid().ToString('N'))
    $fxClaims  = Join-Path $fxRoot 'CLAIMS'
    $fxHb      = Join-Path $fxRoot 'HEARTBEATS'
    $fxRep     = Join-Path $fxRoot 'REPORTS'
    $fxLedger  = Join-Path $fxRoot 'TASK_LEDGER.md'
    $now = [datetime]::UtcNow

    function Get-IsoZ([datetime]$WhenUtc) {
        return $WhenUtc.ToString('yyyy-MM-dd') + 'T' + $WhenUtc.ToString('HH:mm:ss') + 'Z'
    }

    function Add-FixtureClaim([string]$Task, [string]$Body) {
        $p = Join-Path $fxClaims ($Task + '.json')
        [IO.File]::WriteAllText($p, $Body, (New-Object System.Text.UTF8Encoding($false)))
    }

    function New-ClaimBody([string]$Task, [string]$Agent, [string]$StartedIso) {
        return ('{{"task": "{0}", "agent_id": "{1}", "started_utc": "{2}", "ttl_min": 30, "scope": ["fixture"], "javac": "1.8.0_292"}}' -f $Task, $Agent, $StartedIso)
    }

    function Add-FixtureHeartbeat([string]$Agent) {
        $p = Join-Path $fxHb ($Agent + '.json')
        [IO.File]::WriteAllText($p, ('{{"schema_version":1,"agent_id":"{0}"}}' -f $Agent), (New-Object System.Text.UTF8Encoding($false)))
        return $p
    }

    try {
        foreach ($d in @($fxClaims, $fxHb, $fxRep)) { [void](New-Item -ItemType Directory -Path $d -Force) }

        # Rows B5-9001..B5-9006 present; B5-9007 deliberately absent (ROW-MISSING).
        $rows = @(
            '| B5-9001 | OPEN | fx | fx | - | - |',
            '| B5-9002 | OPEN | fx | fx | - | - |',
            '| B5-9003 | OPEN | fx | fx | - | - |',
            '| B5-9004 | DONE | fx | fx | - | - |',
            '| B5-9005 | OPEN | fx | fx | - | - |',
            '| B5-9006 | OPEN | fx | fx | - | - |'
        )
        [IO.File]::WriteAllText($fxLedger, (($rows -join "`n") + "`n"), (New-Object System.Text.UTF8Encoding($false)))

        # F1 LIVE: fresh claim, fresh heartbeat, row OPEN, no report (normal mid-task).
        Add-FixtureClaim 'B5-9001' (New-ClaimBody 'B5-9001' 'fx-live' (Get-IsoZ $now.AddMinutes(-5)))
        (Get-Item -LiteralPath (Add-FixtureHeartbeat 'fx-live')).LastWriteTimeUtc = $now.AddMinutes(-5)

        # F2 STALE: claim, heartbeat and report all 120 min old, row OPEN.
        Add-FixtureClaim 'B5-9002' (New-ClaimBody 'B5-9002' 'fx-stale' (Get-IsoZ $now.AddMinutes(-120)))
        (Get-Item -LiteralPath (Add-FixtureHeartbeat 'fx-stale')).LastWriteTimeUtc = $now.AddMinutes(-120)
        $rp2 = Join-Path $fxRep '2026-10-06-fx-B5-9002.md'
        [IO.File]::WriteAllText($rp2, 'fx', (New-Object System.Text.UTF8Encoding($false)))
        (Get-Item -LiteralPath $rp2).LastWriteTimeUtc = $now.AddMinutes(-120)

        # F3 UNKNOWN: claim 120 min old but the REQUIRED heartbeat is absent (B5-0609).
        Add-FixtureClaim 'B5-9003' (New-ClaimBody 'B5-9003' 'fx-unknown' (Get-IsoZ $now.AddMinutes(-120)))

        # F4 ORPHAN: fresh claim on a DONE row (B5-0622).
        Add-FixtureClaim 'B5-9004' (New-ClaimBody 'B5-9004' 'fx-orphan' (Get-IsoZ $now.AddMinutes(-5)))
        (Get-Item -LiteralPath (Add-FixtureHeartbeat 'fx-orphan')).LastWriteTimeUtc = $now.AddMinutes(-5)

        # F5 SKEW: started_utc 13 h AHEAD of the clock, heartbeat stale (B5-1931).
        Add-FixtureClaim 'B5-9005' (New-ClaimBody 'B5-9005' 'fx-skew' (Get-IsoZ $now.AddMinutes(780)))
        (Get-Item -LiteralPath (Add-FixtureHeartbeat 'fx-skew')).LastWriteTimeUtc = $now.AddMinutes(-120)

        # F6 UNPARSEABLE: not JSON at all - a parse failure is a finding, not a crash.
        Add-FixtureClaim 'B5-9006' '{this is not json'

        # F7 ROW-MISSING: fresh claim whose task id appears in no ledger row.
        Add-FixtureClaim 'B5-9007' (New-ClaimBody 'B5-9007' 'fx-norow' (Get-IsoZ $now.AddMinutes(-5)))
        (Get-Item -LiteralPath (Add-FixtureHeartbeat 'fx-norow')).LastWriteTimeUtc = $now.AddMinutes(-5)

        $fxStatus = Get-RowStatusMap $fxLedger

        function Assert-Case([string]$Name, $Got, [string]$ExpectVerdict, [string[]]$ExpectFlags, [int]$ExpectFindings) {
            $errs = @()
            if ($null -eq $Got) {
                $errs += 'no result object'
            } else {
                if ($Got.Verdict -ne $ExpectVerdict) { $errs += ('verdict expected {0} got {1}' -f $ExpectVerdict, $Got.Verdict) }
                foreach ($f in $ExpectFlags) { if (-not $Got.Flags.Contains($f)) { $errs += ('missing flag {0}' -f $f) } }
                if ($Got.Findings -ne $ExpectFindings) { $errs += ('findings expected {0} got {1}' -f $ExpectFindings, $Got.Findings) }
            }
            if ($errs.Count -eq 0) {
                Write-Output ('PASS {0}: verdict={1} flags=[{2}] findings={3}' -f $Name, $Got.Verdict, ($Got.Flags -join ','), $Got.Findings)
            } else {
                Write-Output ('FAIL {0}: {1}' -f $Name, ($errs -join '; '))
                [void]$script:Failed.Add($Name)
            }
        }

        $cases = @(
            @{ N='F1-LIVE-OPEN';        T='B5-9001'; V='LIVE';    F=@();              C=0 },
            @{ N='F2-STALE-OPEN';       T='B5-9002'; V='STALE';   F=@();              C=1 },
            @{ N='F3-UNKNOWN-NO-HB';    T='B5-9003'; V='UNKNOWN'; F=@();              C=1 },
            @{ N='F4-ORPHAN-ON-DONE';   T='B5-9004'; V='LIVE';    F=@('ORPHAN');      C=1 },
            @{ N='F5-SKEW-AHEAD-CLOCK'; T='B5-9005'; V='STALE';   F=@('SKEW');        C=2 },
            @{ N='F6-UNPARSEABLE';      T='B5-9006'; V='UNKNOWN'; F=@('UNPARSEABLE'); C=1 },
            @{ N='F7-ROW-MISSING';      T='B5-9007'; V='LIVE';    F=@('ROW-MISSING'); C=1 }
        )
        $total = 0
        foreach ($c in $cases) {
            $p = Join-Path $fxClaims ($c.T + '.json')
            $got = Test-ClaimLiveness -ClaimPath $p -StatusMap $fxStatus -HeartbeatsDir $fxHb -ReportsDir $fxRep -TtlDefault 30 -NowUtc ([datetime]::UtcNow)
            Assert-Case $c.N $got $c.V $c.F $c.C
            $total += $got.Findings
        }

        # Sanitisation asserts (Amendment A1.1): ONLY ':' and '/' map to '-';
        # spaces and parentheses survive verbatim.
        $sanCases = @(
            @{ In='solar-pro4:free';              Out='solar-pro4-free' },
            @{ In='Kilo (kilo-auto/free)';        Out='Kilo (kilo-auto-free)' },
            @{ In='opencode (space-bunny-free)';  Out='opencode (space-bunny-free)' }
        )
        foreach ($s in $sanCases) {
            $gotSan = Get-SanitisedId $s.In
            if ($gotSan -ne $s.Out) {
                Write-Output ('FAIL sanitise "{0}": expected "{1}" got "{2}"' -f $s.In, $s.Out, $gotSan)
                [void]$script:Failed.Add(('sanitise-' + $s.In))
            } else {
                Write-Output ('PASS sanitise "{0}" -> "{1}"' -f $s.In, $gotSan)
            }
        }

        # Exit-code mapping: 7 total findings across the fixtures must classify
        # the store as exit 1, and 0 findings as exit 0.
        if ($total -ne 7) {
            Write-Output ('FAIL exit-map: expected 7 total findings got {0}' -f $total)
            [void]$script:Failed.Add('exit-map')
        } else {
            Write-Output ('PASS exit-map: total findings {0} -> exit 1 (0 findings would be exit 0)' -f $total)
        }
    } finally {
        Remove-Item -LiteralPath $fxRoot -Recurse -Force -ErrorAction SilentlyContinue
    }

    if ($script:Failed.Count -gt 0) {
        Write-Output ('[B5-2650] SelfTest FAIL: {0} check(s) failed: {1}' -f $script:Failed.Count, ($script:Failed -join ', '))
        exit 1
    }
    Write-Output '[B5-2650] SelfTest PASS: 7 verdict cases, 3 sanitise cases, exit-map; fixture store cleaned up.'
    exit 0
}


# ---- main: read the live store, print the table, exit 0/1/2 ----
foreach ($p in @($ClaimsDir, $HeartbeatsDir, $ReportsDir, $LedgerPath)) {
    if (-not (Test-Path -LiteralPath $p)) {
        Write-Output ('[B5-2650] EXIT 2: input missing or unreadable: {0}' -f $p)
        exit 2
    }
}

try {
    $statusMap = Get-RowStatusMap $LedgerPath
} catch {
    Write-Output ('[B5-2650] EXIT 2: ledger unreadable: {0}' -f $_.Exception.Message)
    exit 2
}

$claimFiles = @(Get-ChildItem -LiteralPath $ClaimsDir -Filter '*.json' -File | Sort-Object Name)
$now = [datetime]::UtcNow
Write-Output ('=== B5-2650 claim-liveness report | now {0}Z | READ-ONLY | deletes nothing ===' -f $now.ToString('yyyy-MM-ddTHH:mm:ss'))
Write-Output ('claims read: {0}' -f $claimFiles.Count)
Write-Output ('{0,-11} {1,-8} {2,-13} {3,-11} {4,-11} {5,-11} {6,-13} {7}' -f 'CLAIM', 'VERDICT', 'FLAGS', 'CLAIM-AGE', 'HEARTBEAT', 'REPORT', 'ROW', 'AGENT')

$results = @()
foreach ($cf in $claimFiles) {
    try {
        $res = Test-ClaimLiveness -ClaimPath $cf.FullName -StatusMap $statusMap -HeartbeatsDir $HeartbeatsDir -ReportsDir $ReportsDir -TtlDefault 30 -NowUtc $now
    } catch {
        $res = New-ClaimReportObject -Task $cf.BaseName -Agent $null
        [void]$res.Flags.Add('UNPARSEABLE')
        [void]$res.Reasons.Add(('evaluation failed: {0}' -f $_.Exception.Message))
        $res.Findings++
        $res.RowStatus = 'EVAL-ERROR'
    }
    $results += $res
    $claimAgeTxt = 'n/a'
    if ($null -ne $res.ClaimAge) { $claimAgeTxt = ('{0:F0}m' -f $res.ClaimAge) }
    $hbAgeTxt = 'UNKNOWN'
    if ($null -ne $res.HbAge) { $hbAgeTxt = ('{0:F0}m' -f $res.HbAge) }
    $repAgeTxt = 'absent'
    if ($null -ne $res.ReportAge) { $repAgeTxt = ('{0:F0}m' -f $res.ReportAge) }
    $flagTxt = '-'
    if ($res.Flags.Count -gt 0) { $flagTxt = ($res.Flags -join ',') }
    Write-Output ('{0,-11} {1,-8} {2,-13} {3,-11} {4,-11} {5,-11} {6,-13} {7}' -f $res.Task, $res.Verdict, $flagTxt, $claimAgeTxt, $hbAgeTxt, $repAgeTxt, $res.RowStatus, $res.Agent)
}

Write-Output ''
$findings = @($results | Where-Object { $_.Findings -gt 0 })
if ($findings.Count -eq 0) {
    Write-Output ('[B5-2650] PASS: {0} claim(s) read; every claim LIVE with no orphan, no skew, no parse failure.' -f $results.Count)
    exit 0
}

Write-Output ('=== FINDINGS ({0} of {1} claim(s)) ===' -f $findings.Count, $results.Count)
foreach ($f in $findings) {
    Write-Output ('--- {0}  verdict={1}  flags=[{2}]  row={3}' -f $f.Task, $f.Verdict, ($f.Flags -join ','), $f.RowStatus)
    $ca = 'n/a'
    if ($null -ne $f.ClaimAge) { $ca = ('{0:F0}m via {1}' -f $f.ClaimAge, $f.ClaimAgeWhy) }
    $ha = 'UNKNOWN (absent or unparseable)'
    if ($null -ne $f.HbAge) { $ha = ('{0:F0}m' -f $f.HbAge) }
    $ra = 'absent (contributing only)'
    if ($null -ne $f.ReportAge) { $ra = ('{0:F0}m' -f $f.ReportAge) }
    Write-Output ('    claim     : {0}' -f $ca)
    Write-Output ('    heartbeat : {0}' -f $ha)
    Write-Output ('    report    : {0}' -f $ra)
    foreach ($why in $f.Reasons) { Write-Output ('    why       : {0}' -f $why) }
}
Write-Output ''
Write-Output ('[B5-2650] FAIL: {0} claim(s) with findings, {1} claim(s) read.' -f $findings.Count, $results.Count)
Write-Output 'This tool deletes nothing: reaping a STALE claim still requires the three-signal evidence'
Write-Output 'recorded in a reap note in .agent/TASK_LEDGER.md (00_BOOT step 10).'
exit 1


