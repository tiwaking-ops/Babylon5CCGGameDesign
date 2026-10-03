param(
    [string]$HeartbeatsDir = "C:\temp\projects\Babylon5CCGGameDesign\.agent\HEARTBEATS",
    [string]$RetiredDir = "C:\temp\projects\Babylon5CCGGameDesign\.agent\HEARTBEATS\_retired",
    [string]$QuarantineDir = "C:\temp\projects\Babylon5CCGGameDesign\.agent\HEARTBEATS\_quarantine"
)

$files = Get-ChildItem "$HeartbeatsDir\*.json" | Where-Object { 
    $_.Name -notmatch '^_' -and 
    $_.Name -ne 'README.md' -and 
    $_.Name -ne '_registry.json' 
}

$results = @()
$moved = @()

foreach ($f in $files) {
    $content = Get-Content $f.FullName -Raw -Encoding UTF8
    try {
        $json = $content | ConvertFrom-Json -ErrorAction Stop
    } catch {
        Write-Warning "Failed to parse $($f.Name): $_"
        $results += [pscustomobject]@{
            File = $f.Name
            State = "PARSE_ERROR"
            LiveClaims = -1
            AgeHours = [math]::Round(((Get-Date).ToUniversalTime() - $f.LastWriteTimeUtc).TotalHours, 1)
            InQuarantine = $false
            IsTombstone = $false
            Eligible = $false
            SHA256 = ""
            Note = "JSON parse error"
        }
        continue
    }
    
    $state = if ($json.state) { $json.state } else { "missing" }
    $liveClaims = if ($json.live_claims) { @($json.live_claims).Count } else { 0 }
    $utc = if ($json.utc) { $json.utc } else { "missing" }
    $mtime = $f.LastWriteTimeUtc
    $ageHours = [math]::Round(((Get-Date).ToUniversalTime() - $mtime).TotalHours, 1)
    $sha256 = (Get-FileHash $f.FullName -Algorithm SHA256).Hash
    $inQuarantine = Test-Path "$QuarantineDir\$($f.Name)"
    $isTombstone = ($f.Name -eq 'Cline (space-bunny) b5-0941.json')
    
    $eligible = ($state -eq 'idle') -and ($liveClaims -eq 0) -and ($ageHours -gt 24) -and (-not $inQuarantine) -and (-not $isTombstone)
    
    $note = ""
    if ($state -ne 'idle') { $note += "state!=idle; " }
    if ($liveClaims -ne 0) { $note += "liveClaims>0; " }
    if ($ageHours -le 24) { $note += "age<=24h; " }
    if ($inQuarantine) { $note += "inQuarantine; " }
    if ($isTombstone) { $note += "tombstone(R5/R6); " }
    if ($note -eq "") { $note = "ELIGIBLE" }
    
    $results += [pscustomobject]@{
        File = $f.Name
        State = $state
        LiveClaims = $liveClaims
        AgeHours = $ageHours
        InQuarantine = $inQuarantine
        IsTombstone = $isTombstone
        Eligible = $eligible
        SHA256 = $sha256.Substring(0,16) + "..."
        Note = $note
    }
    
    if ($eligible) {
        $dest = Join-Path $RetiredDir $f.Name
        $shaBefore = $sha256
        Move-Item -LiteralPath $f.FullName -Destination $dest -Force
        $shaAfter = (Get-FileHash $dest -Algorithm SHA256).Hash
        $match = ($shaBefore -eq $shaAfter)
        $moved += [pscustomobject]@{
            File = $f.Name
            SHA256_Before = $shaBefore
            SHA256_After = $shaAfter
            Match = $match
            MTime = $mtime
            State = $state
            LiveClaims = $liveClaims
            AgeHours = $ageHours
        }
        Write-Host "MOVED: $($f.Name) | age=${ageHours}h | state=$state | liveClaims=$liveClaims | SHA256 match=$match"
    }
}

$results | Format-Table -AutoSize
Write-Host "`n=== SUMMARY ==="
Write-Host "Total files scanned: $($results.Count)"
Write-Host "Eligible: $($results.Where({$_.Eligible}).Count)"
Write-Host "Moved: $($moved.Count)"
Write-Host "Not eligible: $($results.Where({-not $_.Eligible}).Count)"

# Output detailed moved list for report
$moved | ForEach-Object {
    Write-Host "MOVED_DETAIL: $($_.File) | mtime=$($_.MTime) | age=${_..AgeHours}h | state=$($_.State) | liveClaims=$($_.LiveClaims) | sha256=$($_.SHA256_Before)"
}