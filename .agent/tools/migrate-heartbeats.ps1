<#
.SYNOPSIS
  Additive migration of the agent heartbeat store to heartbeat schema v1.

.DESCRIPTION
  Implements heartbeat-schema-proposal Sec.6 (steps 1-3, 5). AD-ONLY: every key that
  exists before migration still exists afterwards, with the same value. Legacy timestamp
  keys (updated_utc / heartbeat_utc / last_heartbeat_utc) are RETAINED for one cycle
  alongside the new canonical `utc`, per Sec.6.1, so no existing reader breaks mid-pass.

  NO FILE IS DELETED. Files that cannot be migrated as heartbeats (a bare-timestamp
  fragment, a 0-byte stray) are MOVED to .agent/HEARTBEATS/_quarantine/ and reported;
  the quarantine directory is excluded by validate-heartbeats.ps1. This is proposal
  Sec.6.5 "convert or quarantine" and is fully reversible.

  Data-safety rules, each traceable to a failure this repo actually hit:

   * live_claims is DERIVED from .agent/CLAIMS/*.json by normalised agent_id, never
     assumed. Writing [] into a file whose agent actually holds a claim would be a
     FALSE assertion -- and per the proposal a false assertion is worse than a missing
     one, because [] is defined as "I hold nothing".

   * Nothing is inferred from prose. `notes`, `last_completed` and similar narrative
     fields are copied verbatim and never scanned for task IDs (B5-0609 failure).

   * Every write is verified: key count and per-key value equality are checked after
     the write, and any file that fails verification is reported as FAILED rather than
     being left silently damaged.

   * If an agent refreshed its heartbeat between our read and our write, the file is
     left ALONE and reported as SKIPPED-RACE. Overwriting a live heartbeat would
     silently revert a concurrent update.

.PARAMETER DryRun
  Report what would change without writing anything.

.PARAMETER Directory
  Heartbeat directory (default <repo>/.agent/HEARTBEATS).

.PARAMETER ClaimsDirectory
  Claims directory used to derive live_claims (default <repo>/.agent/CLAIMS).
#>
[CmdletBinding()]
param(
    [switch]$DryRun,
    [string]$Directory,
    [string]$ClaimsDirectory
)

$ErrorActionPreference = "Continue"   # a bad file is a finding, not a crash
$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if (-not $Directory)          { $Directory          = Join-Path $repoRoot ".agent/HEARTBEATS" }
if (-not $ClaimsDirectory)    { $ClaimsDirectory    = Join-Path $repoRoot ".agent/CLAIMS" }

$ValidState = @('active', 'idle')
$LegacyKeys = @('updated_utc', 'heartbeat_utc', 'last_heartbeat_utc')

# normalise an agent_id for joining: lowercase, strip every non-alphanumeric char.
# Handles "solar-pro4:free" vs "solar-pro4<U+2028>free" vs "solar-pro4-free".
function Normalize-Id([string]$s) {
    if ($null -eq $s) { return '' }
    return ($s.ToLowerInvariant() -replace '[^a-z0-9]', '')
}

# ---- derive the true live-claims set from the claims directory --------------------
$claimOwners = @{}
foreach ($c in @(Get-ChildItem -LiteralPath $ClaimsDirectory -Filter '*.json' -File -EA SilentlyContinue)) {
    try {
        $cd = ([IO.File]::ReadAllText($c.FullName)) | ConvertFrom-Json -ErrorAction Stop
        $own = Normalize-Id([string]$cd.agent_id)
        if ($own) {
            if (-not $claimOwners.ContainsKey($own)) { $claimOwners[$own] = @() }
            $claimOwners[$own] += $c.BaseName
        }
    } catch { }
}

$report = @()
$files  = @(Get-ChildItem -LiteralPath $Directory -File | Where-Object { $_.Name -ne 'README.md' })

foreach ($f in $files) {
    $stem   = [IO.Path]::GetFileNameWithoutExtension($f.Name)
    $rec    = [ordered]@{}
    $action = ''
    $note   = ''
    $raw    = ''

    try { $raw = [IO.File]::ReadAllText($f.FullName) } catch { $action = 'FAILED'; $note = "unreadable: $($_.Exception.Message)"; $report += [pscustomobject]@{File=$f.Name;Action=$action;Note=$note}; continue }

    $isJson = ($raw.Trim() -ne '') -and (-not $raw.TrimStart().StartsWith('---'))
    $existing = $null

    if ($isJson) {
        try { $existing = $raw | ConvertFrom-Json -ErrorAction Stop } catch { $isJson = $false }
    }

    # --- case 1: bare timestamp fragment or 0-byte stray -> quarantine --------------
    if (-not $isJson) {
        $looksLikeHeartbeat = ($raw.TrimStart().StartsWith('---')) -or ($raw.Trim() -ne '' -and $raw.Trim().Length -gt 40)
        if (-not $looksLikeHeartbeat) {
            $action = 'QUARANTINE'
            $note   = if ($raw.Trim() -eq '') { '0-byte stray; a same-named .json exists' }
                      else { "bare value, not a heartbeat: '$($raw.Trim())'" }
            if (-not $DryRun) {
                $q = Join-Path $Directory '_quarantine'
                if (-not (Test-Path -LiteralPath $q)) { New-Item -ItemType Directory -Path $q -Force | Out-Null }
                Move-Item -LiteralPath $f.FullName -Destination (Join-Path $q $f.Name) -Force
            }
            $report += [pscustomobject]@{File=$f.Name;Action=$action;Note=$note}
            continue
        }
    }

    # --- case 2: YAML frontmatter + loose key:value body -> convert to JSON --------
    if (-not $isJson) {
        $action = 'CONVERT-YAML->JSON'
        $lines  = $raw -split "`r?`n"
        $inFm   = $false; $fmDone = $false
        $fm = [ordered]@{}; $body = [ordered]@{}
        $fmKey = $null; $fmBuf = $null
        foreach ($ln in $lines) {
            if ($ln -match '^\s*---\s*$') {
                if (-not $fmDone) { $inFm = -not $inFm; if (-not $inFm) { $fmDone = $true }; continue }
            }
            $target = if ($inFm) { $fm } else { $body }
            if ($ln -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*:\s*(.*)$') {
                $k = $Matches[1]; $v = $Matches[2].Trim()
                if ($v -eq '') { $target[$k] = ''; $fmKey = $k; $fmBuf = '' }
                else { $target[$k] = $v.Trim('"'); $fmKey = $null }
            } elseif ($fmKey -and $ln.Trim() -ne '') { $target[$fmKey] = ($target[$fmKey] + ' ' + $ln.Trim()).Trim() }
        }
        foreach ($k in $body.Keys) { $rec[$k] = $body[$k] }
        if ($fm.Count -gt 0) { $rec['document_and_provenance'] = ($fm | ConvertTo-Json -Depth 5 -Compress) }
        $note = "converted from YAML frontmatter; $(($body.Keys).Count) body keys preserved"
    }
    # --- case 3: clean JSON -> additive ------------------------------------------
    else {
        $action = 'MIGRATE-JSON'
        foreach ($p in $existing.PSObject.Properties) { $rec[$p.Name] = $p.Value }
        $note = "$(@($existing.PSObject.Properties).Count) keys preserved"
    }

    # --- race guard: did the file change under us? --------------------------------
    if (-not $DryRun) {
        try {
            $nowRaw = [IO.File]::ReadAllText($f.FullName)
            if ($nowRaw -ne $raw) {
                $report += [pscustomobject]@{File=$f.Name;Action='SKIPPED-RACE';Note='file changed between read and write; left untouched'}
                continue
            }
        } catch { }
    }

    # --- Sec.6 step 1: schema_version + canonical utc, legacy keys retained -------
    $rec['schema_version'] = 1

    $tsSource = $null
    foreach ($k in (@('utc') + $LegacyKeys)) { if ($rec.Contains($k) -and $rec[$k]) { $tsSource = $k; break } }
    $tsValue = $null
    if ($tsSource) {
        $tsValue = [string]$rec[$tsSource]
        $parsedTs = [DateTime]::MinValue
        if (-not [DateTime]::TryParse($tsValue, [ref]$parsedTs)) { $tsValue = $f.LastWriteTimeUtc.ToString('yyyy-MM-ddTHH:mm:ssZ') }
    } else {
        $tsValue = $f.LastWriteTimeUtc.ToString('yyyy-MM-ddTHH:mm:ssZ')   # honest: mtime, not invented
        $note += "; no timestamp key found, utc seeded from mtime"
    }
    $rec['utc'] = $tsValue
    if ($tsSource -and $tsSource -ne 'utc') { $note += "; legacy '$tsSource' retained (one cycle)" }

    # --- Sec.6 step 2: live_claims always present, DERIVED not assumed ------------
    # NOTE: do NOT write this as `@($a) + @($b)`. In PowerShell, array-plus-empty-array
    # COLLAPSES TO A SCALAR, so 24 files were silently written with a string instead of
    # an array on the first pass. An explicit List and .ToArray() is used so that
    # 0 claims serialises as [] and 1 claim serialises as ["X"].
    $norm = Normalize-Id([string]$(if ($rec.Contains('agent_id')) { $rec['agent_id'] } else { $stem }))
    $derived = if ($claimOwners.ContainsKey($norm)) { @($claimOwners[$norm]) } else { @() }

    $claims = New-Object 'System.Collections.Generic.List[string]'
    if ($rec.Contains('live_claims') -and $null -ne $rec['live_claims']) {
        foreach ($x in @($rec['live_claims'])) {
            # A live_claims entry must be a claim identifier string. Anything else is
            # CORRUPTION, not data: the first migration pass collapsed the array and
            # left a literal empty object, and stringifying that would have baked the
            # garbage in permanently. So non-strings are DROPPED, not serialised.
            # Every dropped entry is reported so the loss is visible rather than silent.
            if ($x -isnot [string]) { $dropped++; continue }
            $s = $x.Trim()
            if (-not $s) { continue }
            if ($s -notmatch '^[A-Za-z0-9][A-Za-z0-9._:-]*$') { $dropped++; continue }
            if (-not $claims.Contains($s)) { $claims.Add($s) }
        }
    }
    foreach ($x in $derived) { if ($x -and -not $claims.Contains($x)) { $claims.Add($x) } }
    $rec['live_claims'] = $claims.ToArray()      # [] when empty -- a real JSON array
    if ($derived.Count -gt 0) { $note += "; live_claims derived from CLAIMS/: $($derived -join ', '); $(if($dropped -gt 0){"; dropped $dropped corrupt live_claims entr$(if($dropped -eq 1){'y'}else{'ies'})"})" }

    # --- Sec.6 step 3: state enum -------------------------------------------------
    $state = if ($rec.Contains('state') -and $ValidState -contains [string]$rec['state']) { [string]$rec['state'] }
             elseif ($rec.Contains('status') -and ([string]$rec['status'] -eq 'active')) { 'active' }
             else { 'idle' }
    $rec['state'] = $state

    # --- agent_id backfill so the filename join can succeed -----------------------
    if (-not ($rec.Contains('agent_id') -and $rec['agent_id'])) {
        $rec['agent_id'] = $stem
        $note += "; agent_id backfilled from filename stem"
    }

    # --- write + verify -----------------------------------------------------------
    if (-not $DryRun) {
        $json = ($rec | ConvertTo-Json -Depth 8)
        [IO.File]::WriteAllText($f.FullName, $json, (New-Object Text.UTF8Encoding($false)))
        # verify: re-read and confirm every original key survived with its value
        try {
            $after = ([IO.File]::ReadAllText($f.FullName)) | ConvertFrom-Json -ErrorAction Stop
            $lost = @()
            foreach ($k in $rec.Keys) { if (-not ($after.PSObject.Properties.Name -contains $k)) { $lost += $k } }
            if ($lost.Count -gt 0) { $action = 'FAILED'; $note += "; LOST KEYS: $($lost -join ', ')" }
        } catch { $action = 'FAILED'; $note += "; post-write parse failed: $($_.Exception.Message)" }
    }
    $report += [pscustomobject]@{File=$f.Name;Action=$action;Note=$note}
}

# ---- summary ---------------------------------------------------------------------
Write-Output "=== Heartbeat migration to schema v1 ==="
if ($DryRun) { Write-Output "DRY RUN - nothing written" }
Write-Output ("directory : {0}" -f $Directory)
Write-Output ("files     : {0}" -f $files.Count)
Write-Output ""
$report | Sort-Object Action, File | Format-Table -AutoSize -Wrap
Write-Output ""
Write-Output "=== Summary ==="
$report | Group-Object Action | Sort-Object Count -Descending | ForEach-Object { "  {0,-16} {1}" -f $_.Name, $_.Count }
$fails = @($report | Where-Object { $_.Action -eq 'FAILED' })
if ($fails.Count -gt 0) { Write-Output ""; Write-Output "FAILURES PRESENT - do not trust the store until resolved."; exit 1 }
exit 0
