<#
.SYNOPSIS
  B5-1462: create a task claim file whose started_utc is stamped by THIS TOOL
  from DateTimeOffset UtcNow — never agent prose — and accept the write only
  when the payload agrees with the file's own mtime.

.DESCRIPTION
  The clock-integrity report (docs/reports/clock-integrity-in-agent-
  coordination-files-2026-09-30.md, amending manual §7.5/§11) measured the
  self-reported timestamp as the weakest link in file-based liveness: an LLM
  can emit a plausible ISO string without reading a clock, and B5-1047's
  future-dated claim still blocks a whole gate chain. Rule: either the tool
  writes the timestamp, or the decision reads the filesystem instead.

  Until this tool existed there was NO claim-creation path in tooling —
  .agent/run-queue.ps1 only reads claims (offer/liveness/refusal), and every
  claim file on disk was hand-authored. This adds the creation half:

   * Create mode (default): refuses an existing claim file (creating the file
     IS the claim, per .agent/CLAIMS/README.md — the tool never overwrites),
     stamps started_utc from [DateTimeOffset]::UtcNow at the moment of
     writing, writes UTF-8 strict JSON (task, agent_id, started_utc, ttl_min,
     scope, javac), then runs the ACCEPTANCE CHECK: the written payload's
     started_utc must agree with the new file's LastWriteTimeUtc within
     tolerance, so one liveness signal (a tool-stamped payload) survives
     bulk-mtime events per clock-report rule §6.3.
   * -VerifyOnly: runs the same payload-vs-mtime agreement check against an
     EXISTING claim file, read-only. Exit 0 = AGREE, 1 = DISAGREE or
     unparseable, 2 = missing. Missing is exit 2 deliberately: an absent
     signal must be distinct from a disagreeing one (the B5-0597 class —
     an absent signal is UNKNOWN, never silently folded into another verdict).

   * Reap semantics are NEVER touched (B5-1462 fence; B5-1008 admin-release
     precedent stays the only non-owner removal path). The tool creates or
     reads; it never deletes, and it never edits the ledger.

  Exit codes: 0 created+accepted / verified AGREE; 1 refused (exists, invalid
  id, acceptance disagreement, unparseable); 2 missing file or claims dir.

.PARAMETER TaskId      Task id (B5-NNNN or B5-NNNNa).
.PARAMETER AgentId     Claiming agent_id, verbatim.
.PARAMETER Scope       Scope strings recorded in the claim.
.PARAMETER TtlMinutes  Claim TTL (default 30).
.PARAMETER Javac       Toolchain version recorded in the claim.
.PARAMETER ClaimsDir   Defaults to <repo>/.agent/CLAIMS.
.PARAMETER VerifyOnly  Check an existing claim's payload-vs-mtime agreement instead of creating.
.PARAMETER ToleranceMinutes  Payload-vs-mtime acceptance tolerance (default 60, matched to the runner's future-timestamp bound).

.OUTPUTS
  Exit 0 = claim created and accepted, or existing claim verified AGREE.
  Exit 1 = refused or DISAGREE. Exit 2 = missing (VerifyOnly) / claims dir absent.
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$TaskId,
    [string]$AgentId,
    [string[]]$Scope = @('unspecified'),
    [int]$TtlMinutes = 30,
    [string]$Javac = '1.8.0_292',
    [string]$ClaimsDir,
    [switch]$VerifyOnly,
    [int]$ToleranceMinutes = 60
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if (-not $ClaimsDir) { $ClaimsDir = Join-Path $repoRoot '.agent/CLAIMS' }

if ($TaskId -notmatch '^B5-[0-9]{4}[a-z]?$') {
    Write-Error ("[B5-1462] REFUSED: task id '{0}' is not a B5-NNNN[a] id." -f $TaskId)
    exit 1
}

# Shared acceptance probe: newest-tool-written payload vs filesystem mtime.
# One sampled DateTimeOffset drives both the write and the check, so a healthy
# host measures a near-zero delta and any bulk-mtime event still leaves the
# payload as the surviving, trustworthy signal.
# Diagnostics go through Write-Host, NOT Write-Output: a function whose output
# pipeline carries strings cannot be used as `exit (fn)` -- PowerShell captures
# the strings into the expression value and the int return is lost inside an
# Object[] (self-caught in the B5-1462 fixture run: every verdict coerced to 0).
function Test-PayloadMtimeAgreement {
    param([string]$Path, [int]$ToleranceMinutes)
    try {
        $raw = [IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
    } catch {
        Write-Host ("[B5-1462] DISAGREE(UNREADABLE): {0} -- {1}" -f $Path, $_.Exception.Message)
        return 1
    }
    try {
        $j = $raw | ConvertFrom-Json -ErrorAction Stop
    } catch {
        Write-Host ("[B5-1462] DISAGREE(UNPARSEABLE): {0} -- {1}" -f $Path, $_.Exception.Message.Split([char]10)[0])
        return 1
    }
    if (-not $j.started_utc) {
        Write-Host ("[B5-1462] DISAGREE(NO-PAYLOAD): {0} carries no started_utc field." -f $Path)
        return 1
    }
    # Strongly typed init: PS 5.1 cannot resolve TryParse(String, ref) against a
    # null-typed variable (the same convention validate-heartbeats.ps1 uses).
    $parsed = [DateTime]::MinValue
    if (-not [DateTime]::TryParse([string]$j.started_utc, [ref]$parsed)) {
        Write-Output ("[B5-1462] DISAGREE(UNPARSEABLE-TIMESTAMP): '{0}'" -f [string]$j.started_utc)
        return 1
    }
    $payloadUtc = $(if ($parsed.Kind -eq [DateTimeKind]::Local) { $parsed.ToUniversalTime() } else { [DateTime]::SpecifyKind($parsed, [DateTimeKind]::Utc) })
    $mtimeUtc = (Get-Item -LiteralPath $Path).LastWriteTimeUtc
    $deltaMin = ($payloadUtc - $mtimeUtc).TotalMinutes
    Write-Host ("[B5-1462] payload started_utc [{0}] vs file mtime [{1}] delta {2:N3} min (tolerance {3} min)" -f `
        [string]$j.started_utc, $mtimeUtc.ToString('yyyy-MM-ddTHH:mm:ssZ'), $deltaMin, $ToleranceMinutes)
    if ([math]::Abs($deltaMin) -gt $ToleranceMinutes) {
        Write-Host "[B5-1462] DISAGREE: payload-vs-mtime delta beyond tolerance. The payload is not a surviving liveness signal; re-create the claim with the tool."
        return 1
    }
    Write-Host "[B5-1462] AGREE: payload-vs-mtime within tolerance; the tool-stamped payload survives as a liveness signal."
    return 0
}

if (-not (Test-Path -LiteralPath $ClaimsDir)) {
    Write-Error ("[B5-1462] claims directory not found: {0}" -f $ClaimsDir)
    exit 2
}

$path = Join-Path $ClaimsDir ($TaskId + '.json')

if ($VerifyOnly) {
    if (-not (Test-Path -LiteralPath $path)) {
        Write-Output ("[B5-1462] MISSING: {0} -- absence is UNKNOWN, never AGREE and never DISAGREE." -f $path)
        exit 2
    }
    $rc = Test-PayloadMtimeAgreement -Path $path -ToleranceMinutes $ToleranceMinutes
    exit $rc
}

# ---- create mode ----------------------------------------------------------------
if (Test-Path -LiteralPath $path) {
    # Atomicity per CLAIMS/README: the file's existence IS the claim. Never
    # overwrite, never merge -- refuse and let the agent pick another task.
    Write-Output ("[B5-1462] REFUSED: {0} already exists (claim taken). No file was modified." -f $path)
    exit 1
}

if (-not $AgentId) {
    Write-Error "[B5-1462] REFUSED: -AgentId is required in create mode."
    exit 1
}

$now = [DateTimeOffset]::UtcNow          # B5-1462: the TOOL authors the timestamp, not the agent
$stamp = $now.ToString('yyyy-MM-ddTHH:mm:ssZ')
$claim = [ordered]@{
    task        = $TaskId
    agent_id    = $AgentId
    started_utc = $stamp
    ttl_min     = $TtlMinutes
    scope       = $Scope
    javac       = $Javac
}
$json = ($claim | ConvertTo-Json -Depth 4)
[IO.File]::WriteAllText($path, $json, [System.Text.Encoding]::UTF8)   # B5-1002 encoding convention
Write-Output ("[B5-1462] CREATED {0} (started_utc tool-stamped {1})" -f $path, $stamp)

$rc = Test-PayloadMtimeAgreement -Path $path -ToleranceMinutes $ToleranceMinutes
exit $rc
